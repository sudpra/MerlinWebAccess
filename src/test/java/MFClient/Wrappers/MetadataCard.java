package MFClient.Wrappers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.How;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;

public class MetadataCard {

	final WebDriver driver;
	WebElement metadataDialog;
	final String CSS_PROPERTY_CONTROL="td[class='mf-dynamic-controlfield']>div[class='mf-lookup-item-cell-image']>span>ul[id='icons']>li[class*='ui-state-default']";
	final String CSS_PROPERTY_CONTROL_VALUE="td[class='mf-dynamic-controlfield']>div[class='mf-lookup-item-cell-content']>span";
	final String CSS_CUSTOMER_CONTROL="td[class='property_control']>div[class='lookup']>div[class='lookupFlexBox'][id*='flexLookupCtrl']";

	public MetadataSuggestions metadataSuggestions;

	/********************************************************************
	 *	Page Factory Elements -Document
	 **********************************************************************/
	@FindBy(how=How.CLASS_NAME,using="img[class='ui-datepicker-trigger']")
	private WebElement docDatePickerObj;

	@FindBy(how=How.CLASS_NAME,using="div[class='date']>input[class='date hasDatepicker']:not([disabled='disabled'])")
	private WebElement dateChecked;

	@FindBy(how=How.ID, using="div[id='ui-datepicker-div']")
	private WebElement docCalendar;

	@FindBy(how=How.CSS,using="select[class='doccard_template']")
	private WebElement selectDocTemplate;

	@FindBy(how=How.CSS,using="select[data-handler='selectMonth']")
	private WebElement selectMonth;

	@FindBy(how=How.CSS,using="select[data-handler='selectYear']")
	private WebElement selectYear;

	@FindBy(how=How.CSS,using="table[id='mf-property-table'][class='mf-dynamic-table']>tbody>tr>td[class='mf-dynamic-namefield']")
	private WebElement metadataTabPropertyNames;

	/**
	 * MetadataCard : Constructor function to instantiate Metadatacard
	 * @param driver Webdriver
	 * @throws Exception
	 */
	public MetadataCard(final WebDriver driver) throws Exception {
		final long startTime = StopWatch.startTime();
		try {
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			this.driver = driver;
			new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(this.driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])"))));

			this.metadataDialog = this.driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])")).findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx'],iframe[src*='objecttemplateselector.html.aspx']"));
			//this.driver.switchTo().frame(metadataDialog);
			Log.event("Metadatacard Instantiated.",StopWatch.elapsedTime(startTime));
		}
		catch (Exception e) {
			Log.event("Error while instantiating metadatacard.",StopWatch.elapsedTime(startTime));
			throw new Exception("Error while instantiating metadatacard..." + e.getMessage());
		}

	}

	/**
	 * MetadataCard : Constructor function to instantiate Metadatacard
	 * @param driver Webdriver
	 * @param sidePane true if metadatacard in sidepane; false if not
	 * @throws Exception
	 */
	public MetadataCard(final WebDriver driver, Boolean sidePane) throws Exception {

		final long startTime = StopWatch.startTime();
		try {
			Utils.fluentWait(driver);
			if(sidePane) {
				this.driver = driver;

				new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
				.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
						(this.driver.findElement(By.cssSelector("div[id='metaCard']"))));

				new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
				.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
						(this.driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"))));

				this.metadataDialog = this.driver.findElement(By.cssSelector("div[id='metaCard']")).findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
				//	this.driver.switchTo().frame(metadataDialog);
			}
			else {
				this.driver = driver;
				new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
				.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
						(this.driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])"))));

				this.metadataDialog = this.driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']")).findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
				//	this.driver.switchTo().frame(metadataDialog);
			}

			metadataSuggestions = new MetadataSuggestions(this.driver, this);

			Log.event("Metadatacard Instantiated.",StopWatch.elapsedTime(startTime));
		}
		catch (Exception e) {
			Log.event("Error while instantiating metadatacard.",StopWatch.elapsedTime(startTime));
			throw new Exception("Error while instantiating metadatacard..." + e.getMessage());
		}

	}

	/**
	 * MetadataCard : Constructor function to instantiate Metadatacard
	 * @param driver Webdriver
	 * @param caption Caption of metadatacard header
	 * @throws Exception
	 */
	public MetadataCard(final WebDriver driver, String caption) throws Exception {
		final long startTime = StopWatch.startTime();
		try {
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			this.driver = driver;

			new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(this.driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])"))));

			List<WebElement> dialogs = this.driver.findElements(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])>div>span[class='ui-dialog-title']")); //Web element div of the error message 

			for(int count = 0; count < dialogs.size(); count++) {
				if(dialogs.get(count).getText().contains(caption)) {
					this.metadataDialog = dialogs.get(count).findElement(By.xpath("..")).findElement(By.xpath(".."));
					break;
				}
			}

			this.metadataDialog = this.metadataDialog.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx'],iframe[src*='objecttemplateselector.html.aspx']"));
			//this.driver.switchTo().frame(metadataDialog);
			Log.event("Metadatacard Instantiated.",StopWatch.elapsedTime(startTime));
		}
		catch (Exception e) {
			Log.event("Error while instantiating metadatacard.",StopWatch.elapsedTime(startTime));
			throw new Exception("Error while instantiating metadatacard..." + e.getMessage());
		}

	}
	/**
	 * isSaveButtonDisplayed : This function is used to find if save button is displayed or not
	 * @return
	 * @throws Exception
	 */

	public boolean isSaveButtonDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement OKBtn= this.driver.findElement(By.cssSelector("button[class*='mf-save-button ui-button-primary']")); //Web element of Save button
			//OKBtn.click();			
			if(OKBtn.isDisplayed())
				return true;
			else
				return false;
		} //End try
		catch(Exception e) { 
			throw new Exception("Exception at Metadatacard.isRequiredProperty : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Metadatacard.isSaveButtonDisplayed : Verified if save button is displayed or not.", StopWatch.elapsedTime(startTime));
		}



	}


	/**
	 * saveAndClose: Clicks Ok button in the metadatacard
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void saveAndClose() throws Exception{

		final long startTime = StopWatch.startTime();

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			this.switchFrame();
			WebElement OKBtn= this.driver.findElement(By.cssSelector("button[class*='mf-save-button ui-button-primary']")); //Web element of Save button
			//OKBtn.click();			
			if(browser.equalsIgnoreCase("safari") || browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()",OKBtn);
			else
				OKBtn.click();


		} //End try
		catch(Exception e){
			throw new Exception ("Exception at Metadatacard.saveAndClose : "+ e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Utils.fluentWait(driver);
			Log.event("OK button clicked/not clicked in the MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}

	} //End saveAndClose

	/**
	 * clickDiscardButton: Click 'Discard' button in the metadatacard
	 * @param driver
	 * @throws Exception 
	 */
	public void clickDiscardButton() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			List<WebElement> discardBtns= this.driver.findElements(By.cssSelector("button[class*='mf-discard-button']"));

			for (WebElement discardBtn : discardBtns)
				if (discardBtn.isDisplayed()) {
					//discardBtn.click();
					ActionEventUtils.click(driver,discardBtn);
					try { Alert alert = driver.switchTo().alert(); alert.accept();	Utils.fluentWait(this.driver);} catch (Exception e1) {}
					return;
				}//End for loop

			throw new Exception ("clickDiscardButton : Discard button does not exists.");

		}//End try
		catch(NoSuchElementException e){
			throw new Exception ("clickDiscardButton : Discard button does not exists.");
		}//End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Discard button is clicked/not clicked in the MetadataCard dialog.",StopWatch.elapsedTime(startTime));
		}

	}//End clickDiscardButton

	/**
	 * cancelAndConfirm: Click 'Cancel' button of MetadataCard
	 * @param driver
	 * @throws Exception 
	 */
	public void cancelAndConfirm() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement btnCancel=driver.findElement(By.cssSelector("button[class='window_cancel'],button[class*='mf-discard-button'][role='button'][class*='ui-button-primary']"));
			//btnCancel.click();//click Cancel button
			ActionEventUtils.click(driver, btnCancel);
			Thread.sleep(1000);
			try{Alert alert = driver.switchTo().alert();alert.accept();}catch(Exception e0){}

		}
		catch(Exception e) {
			throw new Exception("Exception at MetadataCard.cancelAndConfirm : 'Cancel' button is not available/clicked : "+e.getMessage(), e);
		}

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Cancel button is clicked/not clicked on the MetadataCard dialog.",StopWatch.elapsedTime(startTime));
		}

	}

	/**
	 * closeMetadataCard: Click 'Ok' button in the metadatacard
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void closeMetadataCard() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement OKBtn= this.driver.findElement(By.cssSelector("button[class*='mf-save-button ui-button-primary ui-button']")); //Web element of Save button

			if(OKBtn.isDisplayed()) //click Ok Button
				//OKBtn.click();
				ActionEventUtils.click(driver,OKBtn);

		} //End try
		catch(Exception e){
			throw new Exception("Exception at Metadatacard.closeMetadataCard : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("OK button is clicked/not clicked in the MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}

	} //End closeMetadataCard

	/**
	 * propertyExists : To check for the existence of the specified property
	 * @param propName Name of the property
	 * @return true if property exists; false if not
	 */
	public Boolean propertyExists(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.isPropertyExists(propName);

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at Metadatacard.propertyExists : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Metadatacard.propertyExists : Verified if property exists in MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}

	} //propertyExists

	/**
	 * isPropertyExists : To check for the existence of the specified property
	 * @param propName Name of the property
	 * @return true if property exists; false if not
	 */
	private Boolean isPropertyExists(String propName) throws Exception {

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			Utils.fluentWait(driver);
			if(browser.equalsIgnoreCase("edge")){
				JavascriptExecutor js = ((JavascriptExecutor) driver);

				js.executeScript("arguments[0].scrollIntoView(true);",getPropertyElement(propName));

				//((JavascriptExecutor) driver).executeScript(, getPropertyElement(propName));

				return this.getPropertyElement(propName).isDisplayed();
			}
			else
				return this.getPropertyElement(propName).isDisplayed();

		} //End try
		catch(Exception e) {
			return false;
		} //End catch
	} //propertyExists

	/**
	 * isRequiredProperty : To check for the existence of the specified property
	 * @param propName Name of the property
	 * @return true if property required; false if not
	 */
	public Boolean isRequiredProperty(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement(propName);
			WebElement requiredIndicator =  propTable.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='mf-required-indicator']"));

			try{
				if (!requiredIndicator.getAttribute("style").equalsIgnoreCase("display: none;"))
					return true; //Returns required icon is displayed in the metadatacard
				else
					return false;//Returns required icon is not displayed in the metadatacard
			}//End try
			catch(NullPointerException e1)
			{
				if (requiredIndicator.isDisplayed())
					return true;//Returns required icon is displayed in the metadatacard
				else
					return false;//Returns required icon is not displayed in the metadatacard
			}//End catch
			catch(Exception e2){throw e2;}
		} //End try

		catch(Exception e) { 
			throw new Exception("Exception at Metadatacard.isRequiredProperty : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Metadatacard.isRequiredProperty : Verified if required indicator for property exists in MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}

	}//End of isRequiredProperty

	/**
	 * getPropertyInstance : Gets the webelement of the specfied property. NOTE: this method does not switch frame to the metadata card.
	 * @param propName Name of the property
	 * @return Webelement of the property
	 * @throws Exception 
	 */
	public WebElement getPropertyElement(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody"));
			return propTable.findElement(By.xpath("//tr[contains(@class,'mf-property-') and contains(@class,'mf-dynamic-row')]/td[contains(@class, 'mf-dynamic-namefield')]/div/span[contains(@class, 'label') and normalize-space(.) ='" + propName + "']"));

		}
		catch(Exception e) {
			throw new Exception("Exception at Metadatacard.getPropertyElement : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.getPropertyElement : Web element for property in MetadataCard dialog is obtained.", StopWatch.elapsedTime(startTime));
		}

	} //End getPropertyElement

	/**
	 * addNewProperty : Adds new property to the metadatacard
	 * @param propName Name of the property
	 * @return true if property is added to metadatacard; false if not
	 * @throws Exception 
	 */
	private boolean addNewPropToObj(String propName) throws Exception {

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			if(this.isPropertyExists(propName))
				return true;

			int count = 0, snooze = 0;

			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-selector-table']")).findElement(By.cssSelector("tr[class*='mf-add-property-control']")).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']"));
			if ( browser.equalsIgnoreCase("safari") || browser.equalsIgnoreCase("IE")|| browser.equalsIgnoreCase("edge")){
				//propTable.click();
				((JavascriptExecutor) driver).executeScript("arguments[0].click()",propTable);
				Utils.fluentWait(driver);
				((JavascriptExecutor) driver).executeScript("arguments[0].click()",propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
				//ActionEventUtils.click(driver, propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
				Utils.fluentWait(driver);

			}
			else {

				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", propTable);

				propTable.click();

				while (snooze < 3 && this.isAddPropLinkDisplayed())
				{
					propTable.click();
					Thread.sleep(1000);
					snooze++;
				}
				snooze = 0;				
				Utils.fluentWait(driver);

				try
				{
					ActionEventUtils.click(driver, propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));

					while (snooze < 3 && !this.driver.findElement(By.cssSelector("ul[class*='ui-autocomplete']")).isDisplayed())
					{
						ActionEventUtils.click(driver, propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
						Thread.sleep(1000);
						snooze++;
					}
				}catch(Exception e0){}

			}
			WebElement inputTag = propTable.findElement(By.tagName("input")); 
			inputTag.clear();
			inputTag.sendKeys(propName);

			Utils.fluentWait(driver);

			List<WebElement> props = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));

			for(count = 0; count < props.size(); count++) {
				if(props.get(count).getText().trim().equalsIgnoreCase(propName)) {
					//props.get(count).click();
					ActionEventUtils.click(driver,props.get(count));
					break;
				}	
			}

			if (count >= props.size())
				throw new Exception("Property (" + propName + ") is not in the list of properties to add in metadatacard.");

			Utils.fluentWait(driver);
			this.clickPropertySave(propName);
			return this.isPropertyExists(propName);

		} //End try

		catch(Exception e){
			throw new Exception("MetadataCard.addNewProperty : Exception while adding new property. " + e.getMessage(), e);
		} //End catch		

	} //End addNewProperty

	/**
	 * addNewProperty : Adds new property to the metadatacard
	 * @param propName Name of the property
	 * @return true if property is added to metadatacard; false if not
	 * @throws Exception 
	 */
	public boolean addNewProperty(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.addNewPropToObj(propName);

		} //End try

		catch(Exception e){
			throw new Exception("MetadataCard.addNewProperty : Exception while adding new property. " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("New Property is/not added in MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End addNewProperty

	/**
	 * removeProperty : Removes the property from metadatacard
	 * @param propName Name of the property to be removed
	 * @return true if property is removed from metadatacard; false if not
	 * @throws Exception 
	 */
	public boolean removeProperty(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				return true;

			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			WebElement props = propTable.findElement(By.xpath("//tr[contains(@class,'mf-property-') and contains(@class,'mf-dynamic-row')]/td[contains(@class, 'mf-dynamic-namefield')]/div/span[contains(@class, 'label') and normalize-space(.)='"+propName+"']"));
			this.clickProp(propName);
			//props.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='mf-removebutton']")).click();
			ActionEventUtils.click(driver,	props.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='mf-removebutton']")));
			return !this.isPropertyExists(propName);

		} //End try

		catch(Exception e){
			throw new Exception("Exception at MetadataCard.removeProperty : "+e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("Verified if required indicator for property exists in MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End removeProperty

	/**
	 * getLookupAutocompleteInfoString: Returns the info string of autocomplete values for lookup property when something 
	 * has been typed to the field. Possible values are, for example "Searching..." and "No matches".
	 * @return The value of the autocomplete info string. Returns empty string if the info string 
	 * is not displayed at the moment.
	 */
	private String getLookupAutocompleteInfoString(){

		String infoString = "";

		//The element which will contain information about the status of the lookup property value query to the server. For example,
		//this element may contain "No matches", "Searching..." or value of a filtering property. Using findElements method to avoid
		//getting "No such element" exception because this element should not usually be present but only for a short time.
		List<WebElement> autocompleteInfo = this.driver.findElements(By.cssSelector("div[class*='mf-autocomplete-info']"));

		if(autocompleteInfo.size() > 0){

			//If autocomplete info text is available at this point, it can mean that the lookup property value was not was
			//not able to be selected. The query to the server may have taken too much time or it has got stuck somehow.

			try{
				infoString = autocompleteInfo.get(0).getText();
			}
			catch(Exception e1){}

		}

		return infoString;
	}

	/**
	 * setValueToProp: Sets the value to the specified property in the metadatacard 
	 * @param propName Name of the property
	 * @param propValue Value of the property
	 * @return None
	 * @throws Exception 
	 */
	private String setValueToProp(String propName, String propValue) throws Exception{

		try {

			if (!this.isPropertyExists(propName)) {
				if (!this.addNewPropToObj(propName))
					throw new Exception("The Property does not exist in the metadatacard.");
			}

			int count = 0, snooze = 0;
			WebElement props = this.getPropertyElement(propName);
			Utils.fluentWait(driver);

			WebElement field = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			WebElement setButton = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-modify']"));

			while(snooze < 3 && !this.isPropInEditMode(propName))
			{
				if (!this.getValueToProp(propName).equalsIgnoreCase("(varies)")) {
					//setButton.click();
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", setButton);

					ActionEventUtils.click(driver,setButton);
					Utils.fluentWait(driver);
				}
				else{
					ActionEventUtils.click(driver,setButton);
					Utils.fluentWait(driver);
					field.click();
					Utils.fluentWait(driver);
				}
				snooze++;
			}

			String fields = "table[class='ui-datepicker-calendar'],input,textarea";

			if(field.findElements(By.cssSelector(fields)).size() == 0) {
				ActionEventUtils.click(driver,setButton);
				Utils.fluentWait(driver);
			}

			try { field = field.findElement(By.cssSelector("input")); }
			catch (Exception e1) {

				field = field.findElement(By.cssSelector("textarea"));
				if(!field.isEnabled())
					ActionEventUtils.click(driver,field);
				field.clear();
				field.sendKeys(propValue);
				Utils.fluentWait(driver);

				ActionEventUtils.click(driver,setButton);
				return "";
			}

			field.clear();
			field.sendKeys(propValue);
			Utils.fluentWait(driver);

			//Get auto-complete values for lookup property. These elements will only exist if the property is a lookup property.
			List<WebElement> values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));

			//If the property is not a lookup property then this loop won't be entered because there are no elements in the list.
			for(count = 0; count < values.size(); count++) {
				if(values.get(count).getText().trim().equals(propValue)) {
					//values.get(count).click();
					ActionEventUtils.click(driver,values.get(count));
					Utils.fluentWait(driver);
					break;
				}
			}

			String automaticPerMsg = this.getAutoPermissionBubbleMessage();//Gets the automatic permission message if its displayed in the metadatacard

			//Check if lookup property info string is still available. 
			//It may indicate that there was a problem setting lookup property value.
			String infoString = this.getLookupAutocompleteInfoString();

			if(infoString.equals("Searching..."))
				throw new SkipException("Attempted to select value '" + propValue + "' to lookup property '" + propName + "' but info text '" + infoString + "' still remains. This may be due to server performance.");
			else if(infoString.equals("No matches"))
				throw new Exception("Attempted to select value '" + propValue + "' to lookup property '" + propName + "' but there were no matches for such property value.");


			if(count <= values.size())
			{
				this.switchToDefaultContent();
				if (!(MFilesDialog.exists(driver, "Confirm Autofill") || MFilesDialog.exists(driver, "M-Files Web")))
				{
					this.switchFrame();
					ActionEventUtils.click(driver,setButton);
				}
			}

			return automaticPerMsg;
			/*}
			field = field.findElement(By.cssSelector("input"));					
			field.sendKeys(propValue);
			DatePicker datePicker = new DatePicker(this.driver);
			datePicker.SetCalendar(propValue);			
			setButton.click();*/
		}

		catch(Exception e){
			if(e instanceof SkipException)
				throw new SkipException("Exception at MetadataCard.setValueToProp :" + e.getMessage(), e);
			else
				throw new Exception("Exception at MetadataCard.setValueToProp :" + e.getMessage(), e);
		}
	}

	/**
	 * setValueToProp : Sets the property value of the nth index of the property name
	 * @param propName Name of the property
	 * @param propValue Value of the property
	 * @return None
	 * @throws Exception 
	 */
	private void setValueToProp(String propName, String propValue, int index) throws Exception{

		try{

			if (index-1 == 0)
			{
				this.setValueToProp(propName, propValue);
				return;
			}

			if(!this.isPropertyExists(propName)) {
				this.addNewPropToObj(propName);
				if(!this.isPropertyExists(propName))
					throw new Exception("The Property does not exist in the metadatacard.");
			}

			@SuppressWarnings("unused")
			WebElement calendar = null;
			int count = 0;
			WebElement props = this.getPropertyElement(propName);

			WebElement field = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			WebElement setButton = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-modify']"));

			/*Actions clicker = new Actions(driver);
			clicker.moveToElement(field).moveByOffset(1, 1).click().perform();*/
			ActionEventUtils.moveToElementByOffsetAndClick(driver, field, 1, 1);
			Utils.fluentWait(driver);
			List<WebElement> fields = new ArrayList<WebElement>();

			if(field.findElements(By.cssSelector("input")).size() == 0) {
				ActionEventUtils.click(driver,field);
				Utils.fluentWait(driver);
				fields = field.findElements(By.cssSelector("input"));
			}
			else
				fields = field.findElements(By.cssSelector("input"));

			if(fields.size() < index) {
				//this.driver.findElement(By.cssSelector("img[title*='Add field']")).click();
				ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("img[title*='Add field']")));
				Utils.fluentWait(driver);
				fields = field.findElements(By.cssSelector("input"));
			}

			fields.get(index-1).clear();
			fields.get(index-1).sendKeys(propValue);
			Utils.fluentWait(driver);

			List<WebElement> values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));

			for(count = 0; count < values.size(); count++) {
				if(values.get(count).getText().trim().equals(propValue)) {
					//values.get(count).click();
					ActionEventUtils.click(driver,values.get(count));
					Utils.fluentWait(driver);
					break;
				}	
			}

			//Check if lookup property info string is still available. 
			//It may indicate that there was a problem setting lookup property value.
			String infoString = this.getLookupAutocompleteInfoString();

			if(infoString.equals("Searching..."))
				throw new SkipException("Attempted to select value '" + propValue + "' to lookup property '" + propName + "' but info text '" + infoString + "' still remains. This may be due to server performance.");
			else if(infoString.equals("No matches"))
				throw new Exception("Attempted to select value '" + propValue + "' to lookup property '" + propName + "' but there were no matches for such property value.");

			if(count <= values.size())
				ActionEventUtils.click(driver,setButton);
			return;
		}//End try

		catch(Exception e){
			if(e instanceof SkipException)
				throw new SkipException("MetadataCard.setValueToProp : Error while setting data to a property : "+e);
			else
				throw new Exception("MetadataCard.setValueToProp : Error while setting data to a property : "+e);

		}//End catch
	}//End setValueToProp

	/**
	 * setPropertyValue: Sets the value to the specified property in the metadatacard 
	 * @param propName Name of the property
	 * @param propValue Value of the property
	 * @return None
	 * @throws Exception 
	 */
	public String setPropertyValue(String propName, String propValue) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.setValueToProp(propName, propValue);
		}

		catch(Exception e){
			if(e instanceof SkipException)
				throw new SkipException("Exception at MetadataCard.setPropertyValue :" + e.getMessage(), e);
			else
				throw new Exception("Exception at MetadataCard.setPropertyValue :" + e.getMessage(), e);
		}

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setValueToProp: Property value("+ propValue +") is set for the property("+ propName +").", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * setPropertyValue : Sets the property value of the nth index of the property name
	 * @param propName Name of the property
	 * @param propValue Value of the property
	 * @return None
	 * @throws Exception 
	 */
	public void setPropertyValue(String propName, String propValue, int index) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.setValueToProp(propName, propValue, index);
		}

		catch(Exception e){
			if(e instanceof SkipException)
				throw new SkipException("Exception at MetadataCard.setPropertyValue :" + e.getMessage(), e);
			else
				throw new Exception("Exception at MetadataCard.setPropertyValue :" + e.getMessage(), e);
		}

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setPropertyValue: Property value("+ propValue +") is set for the property("+ propName +") at index("+ index +").", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setPropertyValue

	/**
	 * isPropertyInFocus : Checks whether the property is in edit mode
	 * @param propName Name of the property
	 * @return Value of the property
	 * @throws Exception 
	 */
	public boolean isPropertyInFocus(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.isPropInFocus(propName);

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isPropertyInFocus : " + e.getMessage(), e);
		} //End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropertyInFocus : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * isPropInFocus : Checks whether the property is in edit mode
	 * @param propName Name of the property
	 * @return Value of the property
	 * @throws Exception 
	 */
	private boolean isPropInFocus(String propName) throws Exception {

		try {

			if (!this.isPropertyExists(propName))
				throw new Exception("Property(" + propName + ") is does not exist in the metadatacard");

			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table'],div[id='mf-properties-view']"));
			WebElement focus = propTable.findElement(By.xpath("//tr[contains(@class,'mf-property-') and contains(@class,'mf-dynamic-row') and contains(@class, 'mf-editable')]/td[contains(@class, 'mf-dynamic-namefield')]/div/span[contains(@class, 'label') and normalize-space(.) ='"+propName+"']"));

			if (focus.isDisplayed())
				return true;//Returns property is in edit mode
			else
				return false;

		}//End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isPropertyInFocus : " + e.getMessage(), e);
		} //End catch
	}

	/**
	 * getValueToProp : Gets the value of the specified property
	 * @param propName Name of the property
	 * @return Value of the property
	 * @throws Exception 
	 */
	private String getValueToProp(String propName) throws Exception {

		try {

			if(!this.isPropertyExists(propName))
				return "";

			String propValue = "";
			WebElement propTable = this.getPropertyElement(propName);
			WebElement field =  propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("safari"))
			{
				List<WebElement> values = null;
				try
				{
					try
					{
						values = field.findElements(By.cssSelector("div[class*='mf-control mf-dynamic-control'] div[class*='mf-internal-lookups']>div[class*='mf-internal-lookup']"));

						if  (values.size() == 0)
							values = field.findElements(By.cssSelector("div[class*='mf-control mf-dynamic-control'] div[class*='mf-internal-text'],div[class*='mf-autovalue']"));
					}
					catch(Exception e0)
					{
						values = field.findElements(By.cssSelector("div[class*='mf-control mf-dynamic-control'] div[class*='mf-internal-text']"));
					}
				}catch(Exception e0){return "";}

				for (int i = 0; i < values.size(); i++)
				{
					if(values.get(i).isDisplayed())
					{
						propValue += values.get(i).getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim();

						if (propValue.equals("---"))
							break;

						if (i != (values.size()-1))
							if(values.get(i+1).isDisplayed())
								propValue += "\n";
					}
				}
			}
			else
				propValue = field.getText().trim();

			if (propValue.equals("---"))
				return "";
			else 
				return propValue;

		} //End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getValueToProp : " + e.getMessage(), e);
		} //End catch

	} //getProperty

	/**
	 * getValueToProp : Gets the property value of the nth index of the property name 
	 * @param propName - Name of the Property
	 * @param index 	- Index value of the property value
	 * @return Property value of the property at the specified index
	 * @throws Exception 
	 */
	private String getValueToProp(String propName, int index) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			index = index-1;

			if(!this.isPropertyExists(propName))
				return "";

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property

			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0)
				throw new Exception("Property (" + propName + ") is not a multi-value property with index (" + index + ").");

			if (index == 0)
				return (this.getValueToProp(propName));

			String	propValue = field.get(index).getText().trim();

			if(propValue.equals("---"))
				propValue = "";

			return propValue;

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getValueToProp : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.getValueToProp : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getValueToProp

	/**
	 * getPropertyValue : Gets the value of the specified property
	 * @param propName Name of the property
	 * @return Value of the property
	 * @throws Exception 
	 */
	public String getPropertyValue(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.getValueToProp(propName);

		} //End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getPropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //getProperty

	/**
	 * getPropertyValue : Gets the property value of the nth index of the property name 
	 * @param propName - Name of the Property
	 * @param index 	- Index value of the property value
	 * @return Property value of the property at the specified index
	 * @throws Exception 
	 */
	public String getPropertyValue(String propName, int index) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.getValueToProp(propName, index);

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getPropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getPropertyValue

	/**
	 * getPropertyValueIndex : Gets the index of the property value 
	 * @param propName - Name of the Property
	 * @return Index value of the property value
	 * @throws Exception 
	 */
	public int getPropertyValueIndex(String propName, String propValue) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				throw new Exception("Property (" + propName + ") does not exists in metadatacard.");

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property

			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0)
				throw new Exception("Property (" + propName + ") is not a multi-value property.");

			for (int loopIdx=0; loopIdx<field.size(); loopIdx++) {
				if (field.get(loopIdx).getText().trim().equalsIgnoreCase(propValue))
					return loopIdx;
			}

			throw new Exception("Property (" + propName + ") is does not has a value (" + propValue + ") property.");

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getPropertyValueIndex : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyValueIndex : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getPropertyValue

	/**
	 * getPropertyValues : Gets the property value of the nth index of the property name 
	 * @param propName - Name of the Property
	 * @param index 	- Index value of the property value
	 * @return Property value of the property at the specified index
	 * @throws Exception 
	 */
	public ArrayList<String> getPropertyValues(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				throw new Exception("Property (" + propName + ") does not exists in metadatacard.");

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property
			ArrayList<String> propValues = new ArrayList<String>();

			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0)
				throw new Exception("Property (" + propName + ") is not a multi-value property.");

			for (int loopIdx=0; loopIdx<field.size(); loopIdx++)
				propValues.add(field.get(loopIdx).getText().trim());

			return propValues;

		}//End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getPropertyValues : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyValues : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getPropertyValues

	/**
	 * isMultiValueProperty : Checks if the property is MultiValueProperty 
	 * @param propName - Name of the Property
	 * @return property is multi value property or not
	 * @throws Exception 
	 */
	public boolean isMultiValueProperty(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				throw new Exception("Property (" + propName + ") does not exists in metadatacard.");

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property

			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0) {
				Log.event("Property (" + propName + ") is not a multi-value property.");
				return false;
			}
			else
				return true;

		}//End try
		catch(Exception e) {
			if (e.getMessage().contains("no such element"))
				return false;
			else
				throw new Exception ("Exception at MetadataCard.isMultiValueProperty : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isMultiValueProperty : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isMultiValueProperty

	/**
	 * typeInPropertyValue: Types a value to the specified property
	 * @param propName Name of the property
	 * @param propValue Value of the property to be typed
	 * @return None
	 * @throws Exception 
	 **/
	public void typeInPropertyValue(String propName, String propValue) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				this.addNewPropToObj(propName);

			if(!this.isPropertyExists(propName))
				throw new Exception("The Property does not exist in the metadatacard.");

			@SuppressWarnings("unused")
			WebElement calendar = null;
			int count = 0;
			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-property-'][class*='mf-dynamic-row']>td[class*='mf-dynamic-namefield']>div>span[class*='label']"));

			for(count = 0; count < props.size(); count++) {
				if(props.get(count).getText().trim().equals(propName))
					break;
			}

			WebElement field = props.get(count).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			//	field.click();
			ActionEventUtils.click(driver,field);

			Utils.fluentWait(driver);

			try {
				field = field.findElement(By.cssSelector("input"));
			}
			catch (Exception e1) {
				field = field.findElement(By.cssSelector("textarea"));
			}
			field.clear();
			field.sendKeys(propValue);
			Utils.fluentWait(driver);
			Thread.sleep(3000);

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.typeInPropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.typeInPropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End typeInPropertyValue

	/**
	 * typeInPropertyValue: Types a value to the specified property
	 * @param propName Name of the property
	 * @param propValue Value of the property to be typed
	 * @return None
	 * @throws Exception 
	 **/
	public String getPropertyValueListHeader() throws Exception{

		final long startTime = StopWatch.startTime();
		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			this.switchFrame();

			String valueListHeaderTxt = this.driver.findElement(By.cssSelector("div[class*='mf-autocomplete-info']")).getText();
			String value = "";

			if (browser.equalsIgnoreCase("edge"))
			{
				String[] values = valueListHeaderTxt.split("\n");
				for (int i = 0; i < values.length; i++)
				{
					if (!values[i].trim().equals(""))
						value += values[i].trim();

					if (i != (values.length-1) && !values[i].trim().equals(""))
						value += "\n";
				}
				return value;
			}

			return valueListHeaderTxt;

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.typeInPropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.typeInPropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End typeInPropertyValue



	/**
	 * createNewPropertyValue: Clicks new value to the specified property by clicking '+' at the nth index
	 * @param propName Name of the property
	 * @param index position at which new value to be created
	 * @return None
	 * @throws Exception 
	 */
	public void createNewPropertyValue(String propName, int index) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName)) {
				this.addNewPropToObj(propName);

				if(!this.isPropertyExists(propName))
					throw new Exception("The Property does not exist in the metadatacard.");
			}

			int count = 0;
			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-property-'][class*='mf-dynamic-row']>td[class*='mf-dynamic-namefield']>div>span[class*='label']"));

			for(count = 0; count < props.size(); count++) 
				if(props.get(count).getText().trim().equals(propName))
					break;

			WebElement field = props.get(count).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			//field.click();
			ActionEventUtils.click(driver,field);
			Utils.fluentWait(driver);
			List<WebElement> fields = field.findElements(By.cssSelector("input"));

			/*Actions clicker = new Actions(driver);
			clicker.moveToElement(fields.get(index-1)).moveByOffset(1, 1).click().perform();*/

			ActionEventUtils.moveToElementByOffsetAndClick(driver, fields.get(index-1), 1, 1);
			Utils.fluentWait(driver);

			this.driver.findElement(By.cssSelector("img[title*='Add value']")).click();

		}
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.createNewPropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.createNewPropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End createNewPropertyValue

	/**
	 * clickRemoveFieldButton: Clicks Remove Field button in the property
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	private void clickRemoveFieldButton() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			//this.driver.findElement(By.cssSelector("img[title*='Remove field']")).click(); //Clicks Remove field button
			ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("img[title*='Remove field']")));

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickRemoveFieldButton : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.clickRemoveFieldButton : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //clickRemoveFieldButton

	/**
	 * clickAddValueButton: Clicks Add Value button in the property
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void clickAddValueButton(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			//this.driver.findElement(By.cssSelector("img[title*='Add value']")).click(); //Clicks Remove field button
			Utils.fluentWait(driver);
			this.clickProp(propName);
			ActionEventUtils.click(driver, this.driver.findElement(By.cssSelector("img[title*='Add value']")));
			Utils.fluentWait(driver);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickAddValueButton : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickAddValueButton : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //clickAddValueButton

	/**
	 * isPropAddValueDisplayed: Checks if Add Value button is displayed in the metadatacard
	 * @param None
	 * @return True/False: True if Add icon is displayed else False
	 * @throws Exception 
	 */
	public boolean isPropAddValueDisplayed(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.clickProp(propName);
			Utils.fluentWait(driver);
			if(this.driver.findElement(By.cssSelector("img[title*='Add value']")).isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isPropAddValueDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropAddValueDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //clickAddValueButton

	/**
	 * removePropertyValue: Removes the value from the property at the specified nth index
	 * @param propName Name of the property
	 * @param index position at which value to be removed
	 * @return None
	 * @throws Exception 
	 */
	public void removePropertyValue(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!this.isPropertyExists(propName)) //Checks if property exists in the list
				throw  new Exception("Property (" + propName + ") does not exist in the metadatacard.");

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property
			Utils.fluentWait(driver);
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0) //Checks if there are any value in the list
				throw new Exception("Property (" + propName + ") is not a multi-value property.");

			//	field.get(0).click(); //Clicks the specified field
			ActionEventUtils.click(driver,field.get(0));
			Utils.fluentWait(driver);
			this.clickRemoveFieldButton(); //Clicks remove field button
			Utils.fluentWait(driver);
			this.clickPropertySave(propName);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.removePropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.removePropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //removePropertyValue

	/**
	 * removePropertyValue: Removes the value from the property at the specified nth index
	 * @param propName Name of the property
	 * @param index position at which value to be removed
	 * @return None
	 * @throws Exception 
	 */
	public void removePropertyValue(String propName, int index) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			index = index-1;

			if (!this.isPropertyExists(propName)) //Checks if property exists in the list
				throw  new Exception("Property (" + propName + ") does not exist in the metadatacard.");

			WebElement propTable = this.getPropertyElement(propName); //Gets the instance of the property

			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.xpath("..")).findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class='mf-internal-lookup']," +
							"td[class*='mf-dynamic-controlfield']>div[class*='mf-multiselectlookup']>div>div[class='mf-internal-lookup']"));

			if (field.size() == 0) //Checks if there are any value in the list
				throw new Exception("Property (" + propName + ") is not a multi-value property with index (" + index + ").");

			//field.get(index).click(); //Clicks the specified field
			ActionEventUtils.click(driver,field.get(index));
			this.clickRemoveFieldButton(); //Clicks remove field button

			Utils.fluentWait(driver);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.removePropertyValue : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.removePropertyValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //removePropertyValue

	/**
	 * selectPropValueFromDropDown: Selects the property value from drop down list
	 * @param propValue Value to be selected from dropdown
	 * @return None
	 * @throws Exception 
	 */
	public void selectPropValueFromDropDown(String propValue) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			int count = 0;
			List<WebElement> values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));

			for(count = 0; count < values.size(); count++) {
				if(values.get(count).getText().trim().equals(propValue)) {
					//values.get(count).click();
					ActionEventUtils.click(driver,values.get(count));
					break;
				}	
			}//for loop

			if(count == values.size())
				throw new Exception("The value was not listed for this property");
		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.selectPropValueFromDropDown : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.selectPropValueFromDropDown : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End selectPropValueFromDropDown


	/**
	 * getPropValuesFromDropDown: Gets the property value from drop down list
	 * @param propValue Value to be selected from dropdown
	 * @return None
	 * @throws Exception 
	 */
	public ArrayList<String> getPropValuesFromDropDown() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			int count = 0;

			WebElement dropDown = this.driver.findElement(By.cssSelector("ul[class*='ui-autocomplete']"));

			Utils.waitForElement(driver, dropDown);

			List<WebElement> values = dropDown.findElements(By.cssSelector("li[class*='ui-menu-item']>a"));

			ArrayList<String> actualValues = new ArrayList<String>();

			for(count = 0; count < values.size(); count++) 
				actualValues.add(values.get(count).getText().trim());

			return actualValues;

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getPropValuesFromDropDown : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropValuesFromDropDown : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getPropValuesFromDropDown


	/**
	 * clickPropertyField: Click the property field to close edit box
	 * @param propName Name of the property field to click
	 * @return None
	 * @throws Exception 
	 */
	public void clickPropertyField(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if(!this.isPropertyExists(propName))
				this.addNewPropToObj(propName);

			if(!this.isPropertyExists(propName))
				throw new Exception("The Property does not exist in the metadatacard.");

			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			WebElement props = propTable.findElement(By.xpath("//tr[contains(@class,'mf-property-') and contains(@class,'mf-dynamic-row')]/td[contains(@class, 'mf-dynamic-namefield')]/div/span[contains(@class, 'label') and normalize-space(.)='"+propName+"']"));

			/*for(count = 0; count < props.size(); count++) {
 				if(props.get(count).getText().trim().equals(propName))
 					break;
 			}*/

			//props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']")).click();
			ActionEventUtils.click(driver,props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']")));
			Utils.fluentWait(driver);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickPropertyField : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickPropertyField : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End clickPropertyField

	/**
	 * clickPropertySave: Click tick icon at property to save the value
	 * @param propName- Name of the property
	 * @return None
	 * @throws Exception 
	 */
	private void clickPropertySave(String propName) throws Exception{

		try {


			WebElement props = this.getPropertyElement(propName);
			WebElement saveIcon = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-modify']"));
			String parentClass = saveIcon.findElement(By.xpath("..")).getAttribute("class");

			if (parentClass.toLowerCase().contains("mf-accept-viewmode"))
				ActionEventUtils.click(this.driver, saveIcon);

			Utils.fluentWait(driver);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickPropertySave : " + e.getMessage(), e);
		} //End catch
	} //End clickPropertySave

	/**
	 * savePropValue: Click tick icon at property to save the value
	 * @param propName- Name of the property
	 * @return None
	 * @throws Exception 
	 */
	public void savePropValue(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.clickPropertySave(propName);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickPropertyField : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickPropertyField : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End savePropValue

	/**
	 * clickProp: To perform click operation at the specified property
	 * @param propName Name of the property
	 * @return None
	 * @throws Exception 
	 */
	private void clickProp(String propName) throws Exception{

		try {

			if(!this.isPropertyExists(propName)) {
				this.addNewPropToObj(propName);

				if(!this.isPropertyExists(propName))
					throw new Exception("The Property does not exist in the metadatacard.");
			}
			WebElement property = this.getPropertyElement(propName);
			//	this.getPropertyElement(propName).click();
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", property);
			ActionEventUtils.click(driver, property);

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickProp : " + e.getMessage(), e);
		} //End catch
	} //End clickProp

	/**
	 * clickProperty: To perform click operation at the specified property
	 * @param propName Name of the property
	 * @return None
	 * @throws Exception 
	 */
	public void clickProperty(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.clickProp(propName);

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickProperty : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickProperty : "+propName+" is clicked in the metadatacard", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickProperty

	/**
	 * getPropertyFieldsCount : Gets the number of fields to the specified property
	 * @param propName Name of the property
	 * @return None
	 * @throws Exception 
	 */
	public int getPropertyFieldsCount(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement props = this.getPropertyElement(propName);
			List<WebElement> field = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']")).findElements(By.cssSelector("div[class='mf-internal-lookup']"));
			return field.size();

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getPropertyFieldsCount : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyFieldsCount : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} // End getPropertyFieldsCount

	/**
	 * getAvailableAddProperties: Gets the list of available new property list
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public ArrayList<String> getAvailableAddProperties() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			ArrayList<String> properties = new ArrayList<String>();
			int count = 0;
			int snooze = 0;
			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-selector-table']")).findElement(By.cssSelector("tr[class*='mf-add-property-control']")).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']"));

			//propTable.click();
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", propTable);

			ActionEventUtils.click(driver, propTable);
			Utils.fluentWait(driver);

			//ActionEventUtils.click(driver, propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
			propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")).click();
			Utils.fluentWait(driver);

			List<WebElement> props = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));

			while (snooze < 5 && !(props.size() > 0)){
				//ActionEventUtils.click(driver, propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
				propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")).click();
				Utils.fluentWait(driver);
				props = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']"));
				snooze++;
			}

			for(count = 0; count < props.size(); count++) 
				properties.add(props.get(count).getText().trim());

			propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")).click();
			//ActionEventUtils.click(driver,propTable.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));

			return properties;

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getAvailableAddProperties : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getAvailableAddProperties : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getAvailableAddProperties

	/**
	 * getAvailablePropertyValues: Gets the list of available values to the property
	 * @param propName Name of the property
	 * @return List containing values to the property
	 * @throws Exception 
	 */
	public ArrayList<String> getAvailablePropertyValues(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!this.isPropertyExists(propName))
				throw new Exception("Property (" + propName + ") does not exists in the list.");

			ArrayList<String> properties = new ArrayList<String>();
			int count = 0;
			int snooze = 0;

			WebElement props = this.getPropertyElement(propName);
			Utils.fluentWait(driver);
			WebElement field = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));
			WebElement setButton = props.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-modify']"));

			while(!this.isPropInEditMode(propName) && snooze < 3)
			{
				ActionEventUtils.click(driver, field);
				snooze++;
			}
			snooze = 0;
			//ActionEventUtils.click(driver, setButton);

			/*ActionEventUtils.click(driver, field.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
			Utils.fluentWait(driver);
			ActionEventUtils.click(driver, field.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
			Utils.fluentWait(driver);*/
			//ActionEventUtils.click(driver, field.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")));
			field.findElement(By.cssSelector("span[class*='ui-icon-triangle-1-s']")).click();
			Utils.fluentWait(driver);
			List<WebElement> values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']>a"));

			while(values.size() == 0 && snooze < 20) {
				values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']>a"));
				Utils.fluentWait(driver);
				snooze++;
			}

			for(count = 0; count < values.size(); count++) 
				properties.add(values.get(count).getText().trim());

			ActionEventUtils.click(driver, setButton);
			return properties;

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getAvailablePropertyValues : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getAvailablePropertyValues : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //getAvailablePropertyValues

	/**
	 * setWorkflow : Sets the Workflow
	 * @param workflow Name of the workflow
	 * @return None
	 * @throws Exception 
	 */
	public void setWorkflow(String workflow) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!isWorkflowTableDisplayed())
				return;

			WebElement workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-dynamic-row mf-property-38']")); //Workflow field
			ActionEventUtils.click(driver, workflowField);
			//workflowField.click(); //Clicks the workflow field
			Utils.fluentWait(driver);
			workflowField = this.driver.findElement(By.cssSelector("input[class*='mf-property-38-input-0 ui-autocomplete-input']"));
			workflowField.clear();
			workflowField.sendKeys(workflow); //Sets the workflow value
			Utils.fluentWait(driver);
			//this.driver.findElement(By.cssSelector("span[class='mf-workflow-icon']")).click();
			ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("span[class='mf-workflow-icon']")));
			Utils.fluentWait(driver);

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setWorkflow : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setWorkflow : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End setWorkflow

	/**
	 * getWorkflowValue : Gets the  Workflow state
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	private String getWorkflowValue() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!isWorkflowTableDisplayed())
				return "";

			WebElement workflowField; 

			try { 
				workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']>tbody>tr[class*='mf-property-38 mf-editable']")).findElement(By.cssSelector("span[class*='mf-property-38-text-0']"));
			}
			catch (Exception e1) { 
				workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("div[class='mf-internal-empty-lookup-list']"));
			} 			

			String workflowText = workflowField.getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim();

			if (workflowText.equalsIgnoreCase("---"))
				workflowText = "";

			return workflowText;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getWorkflowValue : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.getWorkflowValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getWorkflow

	/**
	 * getWorkflow : Gets the  Workflow state
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getWorkflow() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			Utils.fluentWait(driver);
			this.switchFrame();
			return this.getWorkflowValue();

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getWorkflow : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getWorkflow : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getWorkflow

	/**
	 * setWorkflowState : Set the state for workflowfield
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public void setWorkflowState(String state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!isWorkflowTableDisplayed())
				return;

			WebElement workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99']"));
			int snooze = 0;

			while (snooze < 3)
			{
				try
				{
					if(this.driver.findElement(By.cssSelector("ul[class*='ui-autocomplete']")).isDisplayed())
						break;

					snooze++;

				}
				catch(Exception e0)
				{
					ActionEventUtils.click(driver,workflowField);
					snooze++;
				}		
			}

			if (state.equals("")) {
				WebElement input = this.driver.findElement(By.cssSelector("input[class*='mf-property-99-input-0']"));
				input.clear();
				return;
			}

			Utils.fluentWait(driver);
			List<WebElement> stateList = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li>a"));
			int i = 0;
			for (i = 0; i<stateList.size(); i++) {
				if (stateList.get(i).getText().equalsIgnoreCase(state)) {
					System.out.println(stateList.get(i).getText());
					ActionEventUtils.click(driver,stateList.get(i));
					break;
				}
			}

			if(i >= stateList.size())
				throw new Exception("Workflow state '" + state + "' is not selected.");

			/*workflowField = this.driver.findElement(By.cssSelector("input[class*=''mf-property-99'-input-0']"));
 			workflowField.clear();
 			workflowField.sendKeys(state);
 			Utils.fluentWait(driver);

 			Utils.fluentWait(driver);
 			this.driver.findElement(By.cssSelector("span[class='mf-workflow-icon']")).click();*/

		}
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setWorkflowState : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setWorkflowState : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}

	/**
	 * getWorkflowState : get the workflow state 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	private String  getWorkflowStateValue() throws Exception {

		final long startTime = StopWatch.startTime();

		try{

			if (!isWorkflowTableDisplayed())
				return "";

			WebElement workflowField;

			try {
				workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99 mf-editable']")).findElement(By.cssSelector("div[class*='mf-property-99']"));
				//	workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99 mf-editable']")).findElement(By.cssSelector("div[class*='mf-property-99'],span[class*='mf-property-99']"));
				// workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99']")).findElement(By.cssSelector("span[class*='mf-property-99']"));
			}
			catch (Exception e1) {
				workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("div[class='mf-internal-empty-lookup-list']"));
			}
			/*WebElement workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99']")).findElement(By.cssSelector("span[class*='mf-property-99']"));*/

			String workflowState = workflowField.getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim();

			if (workflowState.equalsIgnoreCase("---"))
				workflowState = "";

			return workflowState;


			/*System.out.println(workflowField.getText());
 			return workflowField.getText().trim();*/

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getWorkflowValue : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.getWorkflowValue : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getWorkflowStateValue

	/**
	 * isWorkflowTableDisplayed : To check whether workflow table displayed or not in the metadatacard
	 * @return
	 */
	private boolean isWorkflowTableDisplayed(){
		try
		{
			WebElement workflowTable = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']"));

			if( workflowTable.isDisplayed())
				return true;

			return false;
		}
		catch(Exception e){
			return false;
		}
	}

	/**
	 * getWorkflowState : get the workflow state 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getWorkflowState() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			Utils.fluentWait(driver);
			this.switchFrame();
			return this.getWorkflowStateValue();

		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getWorkflow : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getWorkflow : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getWorkflowState

	/**
	 * getAvailableWorkflows : get the available workflows in 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String> getAvailableWorkflows() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			ArrayList<String> states = new ArrayList<String>();
			//this.driver.findElement(By.cssSelector("tr[class*='mf-property-38']")).click();
			ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("tr[class*='mf-property-38']")));
			Utils.fluentWait(driver);
			WebElement stateField = this.driver.findElement(By.cssSelector("ul[class*='ui-autocomplete']"));
			List<WebElement> stateList = stateField.findElements(By.cssSelector("li[id*='_']"));

			for(int count = 0; count < stateList.size(); count++) {
				states.add(stateList.get(count).getText().trim());
			}

			return states;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getAvailableWorkflows : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getAvailableWorkflows : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getAvailableWorkflows

	/**
	 * getAvailableStates : get available states 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String> getAvailableStates() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement workflowField = this.driver.findElement(By.cssSelector("table[id='mf-workflow-table']")).findElement(By.cssSelector("tr[class*='mf-property-99']"));

			ActionEventUtils.click(driver,workflowField);
			//workflowField.click();

			Utils.fluentWait(driver);

			WebElement stateField = this.driver.findElement(By.cssSelector("ul[class*='ui-corner-all']"));
			List<WebElement> stateList = stateField.findElements(By.cssSelector("li[id*='_']>a"));
			ArrayList<String> states = new ArrayList<String>();

			for(int count = 0; count < stateList.size(); count++)
				states.add(stateList.get(count).getText().trim());


			return states;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getAvailableStates : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getAvailableStates : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getAvailableStates

	/**
	 * isWorkflowStateEnabled : Verify the workflow state is enabled or not
	 *
	 * @param tabName
	 * @return true if enabled;false if disabled
	 * @throws Exception 
	 */
	public boolean isWorkflowStateEnabled(String state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			//	this.driver.findElement(By.cssSelector("tr[class*='mf-property-99']")).click();
			ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("tr[class*='mf-property-99']")));
			Utils.fluentWait(driver);
			if(this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']")).size() == 0) {
				this.driver.findElement(By.cssSelector("tr[class*='mf-property-99']")).click();
				Utils.fluentWait(driver);
			}
			WebElement stateField = this.driver.findElement(By.cssSelector("ul[class*='ui-autocomplete']"));
			List<WebElement> stateList = stateField.findElements(By.cssSelector("li[id*='_']"));

			for(int count = 0; count < stateList.size(); count++) {
				if(stateList.get(count).getText().trim().equals(state)) {
					if(stateList.get(count).findElement(By.cssSelector("a")).getAttribute("class").equals("ui-state-disabled"))
						return false;
					else
						return true;
				}
			}

			throw new Exception("The Given State '" + state + "' was not available for the selected Workflow");

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isWorkflowStateEnabled : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isWorkflowStateEnabled : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isWorkflowStateEnabled

	/**
	 * isMetadataCardCommentDisplayed : This function is used to verify the metadatacard comment is opened or not
	 *  
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isMetadataCardCommentDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			Utils.fluentWait(driver);

			WebElement commentsInstance = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment']>div[class*='mf-property-33-text-0']"));
			return commentsInstance.isDisplayed();

		}//End try
		catch(Exception e){
			return false;
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isMetadataCardCommentDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * setComments : Set the comments in metadatacard 
	 * @param comments
	 * @return None
	 * @throws Exception 
	 */
	public void setComments(String comments) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			Utils.fluentWait(driver);

			WebElement commentsTextBox = null;
			WebElement toggleButton = null;

			try
			{
				commentsTextBox = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment']>div[class*='mf-property-33-text-0']"));
			}
			catch(Exception e0){
				commentsTextBox = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment'] textarea[class*='mf-property-33-input-0']"));
			}

			// Trying to open comments section if it is not already open in metadata card
			if(!commentsTextBox.isDisplayed())
			{
				// Normal toggle comments button
				toggleButton = this.driver.findElement(By.cssSelector("div[class*='mf-toggleButton'][class*='mf-comments-button']"));

				try {
					ActionEventUtils.click(driver,toggleButton);
				}
				catch (Exception e1) {

					// Toggle comments button when metadata card title is collapsed
					toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-icon-bar']>div[class*='mf-comments-button']"));
					ActionEventUtils.click(driver,toggleButton);
					//toggleButton.click();
				}

				Utils.fluentWait(driver);
			}

			WebElement commentsInstance = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment']>div[class*='mf-property-33-text-0']"));
			ActionEventUtils.click(driver,commentsInstance);
			//commentsInstance.click();
			Utils.fluentWait(driver);

			this.driver.findElement(By.cssSelector("textarea[class*='mf-property-33-input-0']")).sendKeys(comments);
			Utils.fluentWait(driver);

			toggleButton = this.driver.findElement(By.cssSelector("div[class*='mf-toggleButton'][class*='mf-properties-button']"));
			try {
				ActionEventUtils.click(driver,toggleButton);
				//toggleButton.click();
			}
			catch (Exception e1) {
				toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-icon-bar']>div[class*='mf-properties-button']"));
				ActionEventUtils.click(driver,toggleButton);
				//toggleButton.click();
			}

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setComments : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setComments : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setComments

	/**
	 * getComments : get the posted comments in metadatacard
	 * @param None
	 * @return comments
	 * @throws Exception 
	 */
	public ArrayList<String> getComments() throws Exception {
		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			ArrayList<String> comments = new ArrayList<String>();
			WebElement commentsTextBox = null;
			WebElement toggleButton = null;

			try
			{
				commentsTextBox = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment']>div[class*='mf-property-33-text-0']"));
			}
			catch(Exception e0){
				commentsTextBox = this.driver.findElement(By.cssSelector("div[class='mf-internal-container-for-comment'] textarea[class*='mf-property-33-input-0']"));
			}

			// Trying to open comments section if it is not already open in metadata card
			if(!commentsTextBox.isDisplayed())
			{
				// Normal toggle comments button
				toggleButton = this.driver.findElement(By.cssSelector("div[class*='mf-toggleButton'][class*='mf-comments-button']"));

				if(toggleButton.getText().equals("0"))
					return comments;

				if(toggleButton.getText().equals("")) {
					// Toggle comments button when metadata card title is collapsed
					toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-icon-bar']>div[class*='mf-comments-button']"));
				}
				ActionEventUtils.click(driver,toggleButton);
				Utils.fluentWait(driver);

			}

			List<WebElement> commentsInstance = this.driver.findElements(By.cssSelector("ul[class='mf-comments-list']>li>div>div[class='mf-comment-content']"));

			for(int count = 0; count < commentsInstance.size(); count++) {
				comments.add(commentsInstance.get(count).getText().trim());

			}

			toggleButton = this.driver.findElement(By.cssSelector("div[class*='mf-toggleButton'][class*='mf-properties-button']"));
			try {
				//toggleButton.click();
				ActionEventUtils.click(driver,toggleButton);
			}
			catch (Exception e1) {
				toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-icon-bar']>div[class*='mf-properties-button']"));
				//toggleButton.click();
				ActionEventUtils.click(driver,toggleButton);
			}
			return comments;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getComments : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getComments : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getComments

	/**
	 * getCommentCount : get the count for comments
	 * @param None
	 * @return integer(counts)
	 * @throws Exception 
	 */
	public int getCommentCount() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement toggleButton = this.driver.findElement(By.cssSelector("div[class*='mf-toggleButton'][class*='mf-comments-button']"));
			if(toggleButton.getText().equals("")) {
				toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-icon-bar']>div[class*='mf-comments-button']"));
				return Integer.parseInt(toggleButton.getText());
			}
			return Integer.parseInt(toggleButton.getText());

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getCommentCount : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getCommentCount : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getCommentCount

	/**
	 * setPermission: Selects objects 'Permissions'
	 * @param permission Permission to set in metdatacard
	 * @return None
	 * @throws Exception 
	 */
	private void setPermissionInProp(String permission) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			//driver.findElement(By.cssSelector("span[class='mf-permissions']")).click();
			ActionEventUtils.click(driver,driver.findElement(By.cssSelector("span[class='mf-permissions']")));
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			MFilesDialog mFilesDialog = new MFilesDialog(driver, "Permissions");
			mFilesDialog.setPermission(permission);
			mFilesDialog.clickOkButton();
			driver.switchTo().frame(metadataDialog);
			Log.event("Permissions selected for the Document :"+permission,StopWatch.elapsedTime(startTime));

		} //End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setPermissionInProp : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.setPermissionInProp : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setPermission

	public void clickPermissionField() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			ActionEventUtils.click(driver,driver.findElement(By.cssSelector("span[class='mf-permissions']")));

		} //End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.clickPermissionField : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickPermissionField : ", StopWatch.elapsedTime(startTime));
		} //End Finally


	}

	/**
	 * setPermission: Selects objects 'Permissions'
	 * @param permission Permission to set in metdatacard
	 * @return None
	 * @throws Exception 
	 */
	public void setPermission(String permission) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.setPermissionInProp(permission);

		} //End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setPermission : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setPermission : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setPermission

	/**
	 * getPermission: Gets the selected objects 'Permissions'
	 * @param None
	 * @return Permission that is selected in the metadatacard
	 * @throws Exception 
	 */
	private String getPermissionFromProp() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			return driver.findElement(By.cssSelector("span[class='mf-permissions']")).getText().trim();
		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getPermissionFromProp : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.getPermissionFromProp : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getPermission

	/**
	 * getPermission: Gets the selected objects 'Permissions'
	 * @param None
	 * @return Permission that is selected in the metadatacard
	 * @throws Exception 
	 */
	public String getPermission() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.getPermissionFromProp();
		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getPermission : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPermission : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getPermission

	/**
	 * checkInImmediately : Sets check in immediately for metadatacard
	 * @param state
	 * @return
	 * @throws Exception 
	 */
	private void checkInImmediately(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);
			WebElement workflowField = this.driver.findElement(By.cssSelector("input[id='mf-checkinimmediately-checkbox']"));

			if(state && !workflowField.isSelected())
				ActionEventUtils.click(driver,workflowField);
			//workflowField.click();
			else if(!state && workflowField.isSelected())
				ActionEventUtils.click(driver,workflowField);
			//workflowField.click();

			Utils.fluentWait(driver);

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.checkInImmediately : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.checkInImmediately : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setCheckInImmediately

	/**
	 * setCheckInImmediately : Check the check in immediately for metadatacard
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public void setCheckInImmediately(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.checkInImmediately(state);

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setCheckInImmediately : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setCheckInImmediately : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setCheckInImmediately

	/**
	 * isCheckInImmediately : Verify CheckInImmediately is selected in metadata card
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public boolean isCheckInImmediately() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement chkInField = this.driver.findElement(By.cssSelector("input[id='mf-checkinimmediately-checkbox']"));

			return chkInField.isSelected();

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isCheckInImmediately : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isCheckInImmediately : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isCheckInImmediately

	/**
	 * openForEditing : Check the Open for Editing checkbox in metadata card
	 * @param state
	 * @return
	 * @throws Exception 
	 */
	private void openForEditing(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);
			WebElement workflowField = this.driver.findElement(By.cssSelector("input[id='mf-openforedit-checkbox']"));

			if(state && !workflowField.isSelected())
				ActionEventUtils.click(driver,workflowField);
			//workflowField.click();
			else if(!state && workflowField.isSelected())
				ActionEventUtils.click(driver,workflowField);
			//workflowField.click();

			Utils.fluentWait(driver);

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.openForEditing : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.openForEditing : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End setOpenForEditing

	/**
	 * setOpenForEditing : Check the Open for Editing checkbox in metadata card
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public void setOpenForEditing(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.openForEditing(state);

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setOpenForEditing : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setOpenForEditing : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End setOpenForEditing

	/**
	 * isOpenForEditing : verify the Open for editing is selected in metadata card
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public boolean isOpenForEditing() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			Utils.fluentWait(driver);
			WebElement workflowField = this.driver.findElement(By.cssSelector("input[id='mf-openforedit-checkbox']"));

			return workflowField.isSelected();

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isOpenForEditing : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isOpenForEditing : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isOpenForEditing

	/**
	 * setInfo : Sets the metadata information in the metadatacard
	 * @param props Property and its value in the formatted way (PropertyName1::PropertyValue1\nPropertyName2::PropertyValue2)
	 * @return None
	 * @throws Exception
	 * */
	public void setInfo(String props) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			String[] property = props.split("\n");
			//this.switchFrame();

			for(int count = 0; count < property.length; count++) {

				if (property[count].split("::")[0].equalsIgnoreCase("PERMISSION"))
					this.setPermissionInProp(property[count].split("::")[1]);				
				else if (property[count].split("::")[0].equalsIgnoreCase("COMMENTS"))
					this.setPermissionInProp(property[count].split("::")[1]);
				else 
					this.setValueToProp(property[count].split("::")[0], property[count].split("::")[1]);

				Utils.fluentWait(driver);
			} //End for

			Log.event("Metadatacard.setInfo : Properties are set in metadatacard.",StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e){
			if(e instanceof SkipException)
				throw new SkipException("Exception at MetadataCard.setInfo : " + e.getMessage(), e);
			else
				throw new Exception ("Exception at MetadataCard.setInfo : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setInfo : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End setInfo

	/**
	 * getInfo : Gets the metadata information in the metadatacard* @param None
	 * @return Metadatacard information as hashmap
	 * @throws Exception
	 */  
	public ConcurrentHashMap <String, String> getInfo() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			ConcurrentHashMap <String, String> propertyInfo = new ConcurrentHashMap <String, String>(); 
			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-dynamic-row']:not([style*='display: none'])>td[class='mf-dynamic-namefield']>div>span[class*='label']"));


			if((driver.findElement(By.cssSelector("div[class='ui-scrollable']"))).isDisplayed())			
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[1];", this.driver.findElement(By.cssSelector("div[class*='mf-section-properties']>div[class='ui-scrollable']")), 0);

			for(int count = 0; count < props.size(); count++) {
				System.out.println(props.get(count).getText());
				if(!props.get(count).getText().equals(""))
					propertyInfo.put(props.get(count).getText(), this.getValueToProp(props.get(count).getText()));
			}//End for

			propertyInfo.put("Extension", this.getExtension() );
			propertyInfo.put("Workflow", this.getWorkflowValue());
			propertyInfo.put("WorkflowState", this.getWorkflowStateValue());
			propertyInfo.put("Permissions", this.getPermissionFromProp());

			Log.event("Got property information in metadatacard.", StopWatch.elapsedTime(startTime));

			return propertyInfo;

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getInfo : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getInfo : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getInfo

	/**
	 * getMetadatacardProperties : Gets the available properties in the metadatacard* @param None
	 * @return Metadatacard properties as array
	 * @throws Exception
	 */  
	public ArrayList<String> getMetadatacardProperties() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if((driver.findElement(By.cssSelector("div[class='ui-scrollable']"))).isDisplayed())			
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[1];", this.driver.findElement(By.cssSelector("div[class*='mf-section-properties']>div[class='ui-scrollable']")), 0);

			ArrayList<String> properties = new ArrayList<String>(); 
			WebElement propTable = this.driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-dynamic-row']>td[class='mf-dynamic-namefield']>div>span[class*='label']"));

			for(int count = 0; count < props.size(); count++) {
				if(!props.get(count).getText().equals("")){
					if(props.get(count).isDisplayed())
						properties.add(props.get(count).getText());
				}

			}//End for

			return properties;

		} //End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getMetadatacardProperties : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadatacardProperties : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getInfo

	/**
	 * setFavorite : To select or un-select Favorites icon (star) in metadatacard 
	 * @param state true to set as favorite; false to remove the selection in favorite icon
	 * @return None
	 * @throws Exception 
	 */
	public void setFavorite(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			Utils.fluentWait(driver);
			WebElement favIcon = this.driver.findElement(By.cssSelector("div[class*='mf-favoriteobject-button']"));
			JavascriptExecutor executor = (JavascriptExecutor)driver;

			if(state && favIcon.getAttribute("class").contains("ui-state-off")) 
				executor.executeScript("arguments[0].click();", favIcon);
			//favIcon.click();
			else if(!state && !favIcon.getAttribute("class").contains("ui-state-off"))
				executor.executeScript("arguments[0].click();", favIcon);
			//favIcon.click();


		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setFavorite : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setFavorite : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End setFavorite

	/**
	 * isFavorite : To check the status of selection in favorite icon
	 * @param None
	 * @return true if favorite icon is selected; false if not
	 * @throws Exception 
	 */
	public boolean isFavorite() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			boolean fav = false;
			Utils.fluentWait(driver);
			WebElement favIcon = this.driver.findElement(By.cssSelector("div[class*='mf-favoriteobject-button']"));

			if(favIcon.getAttribute("class").contains("ui-state-off"))
				fav = false;
			else
				fav = true;

			return fav;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isFavorite : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isFavorite : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isFavorite

	/**
	 * setFollowFlag : To select or un-select flag icon in metadatacard
	 * @param state true to set as follow flag; false to remove the selection in follow flag
	 * @return None
	 * @throws Exception 
	 */
	public void setFollowFlag(boolean state) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			Utils.fluentWait(driver);
			WebElement favIcon = this.driver.findElement(By.cssSelector("div[class*='mf-followthisobject-button']"));

			if(state && favIcon.getAttribute("class").contains("ui-state-off")) 
				ActionEventUtils.click(driver, favIcon);
			else if(!state && !favIcon.getAttribute("class").contains("ui-state-off"))
				ActionEventUtils.click(driver, favIcon);


		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.setFollowFlag : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.setFollowFlag : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End setFollowFlag

	/**
	 * getFollowFlag : To check the status of selection in follow flag icon
	 * @param None
	 * @return true if follow flag icon is selected; false if not
	 * @throws Exception 
	 */
	public boolean getFollowFlag() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			boolean fav = false;
			WebElement favIcon = this.driver.findElement(By.cssSelector("div[class*='mf-followthisobject-button']"));

			if(favIcon.getAttribute("class").contains("ui-state-off"))
				fav = false;
			else
				fav = true;

			return fav;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getFollowFlag : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getFollowFlag : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getFollowFlag

	/**
	 * getWarningMessage : get the warning message text
	 * @param None
	 * @return Returns warning mesasge
	 * @throws Exception 
	 */
	public String getWarningMessage() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			List<WebElement> propTable = this.driver.findElements(By.cssSelector("tr[class='mf-message-row']"));

			for(WebElement prop: propTable) {
				if(prop.isDisplayed())
					return prop.getText().trim(); 
			}//End for

			return "";

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getWarningMessage : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getWarningMessage : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getWarningMessage

	/**
	 * getMfilesDialogMessage : Gets message from MFiles dialog
	 * @param None
	 * @return Returns warning mesasge
	 * @throws Exception 
	 */
	public String getMfilesDialogMessage() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchToDefaultContent(); //Switches to home page driver
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			String message = mfilesDialog.getMessage();
			return message;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getMfilesDialogMessage : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMfilesDialogMessage : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getWarningMessage

	/**
	 * collapseHeader :  Collapse the Header in Metadatacard
	 * 
	 * @param driver
	 * @param dataValues
	 * @param collapse
	 * @return None
	 * @throws Exception 
	 */
	public void collapseHeader(boolean collapse) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			JavascriptExecutor executor = (JavascriptExecutor)driver;
			WebElement toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-toggleheader-button']"));

			if(collapse && toggleButton.getAttribute("title").equals("Collapse Title"))
				executor.executeScript("arguments[0].click();", toggleButton);
			else if(!collapse && toggleButton.getAttribute("title").equals("Expand Title"))
				executor.executeScript("arguments[0].click();", toggleButton);
		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.collapseHeader : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.collapseHeader : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End collapseHeader

	/**
	 * popOutMetadatacard : Click the popout in metadata card
	 * @param driver
	 * @param dataValues
	 * @param browser
	 * @return HomePage
	 * @throws Exception 
	 */
	public void popOutMetadatacard() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			this.switchFrame();
			//this.driver.findElement(By.cssSelector("div[class='mf-settings-button']")).click();
			if(browser.equalsIgnoreCase("safari"))
				this.driver.findElement(By.cssSelector("div[class='mf-settings-button']")).click();
			else
				ActionEventUtils.click(driver,this.driver.findElement(By.cssSelector("div[class='mf-settings-button']")));
			Utils.fluentWait(driver);
			if(browser.equalsIgnoreCase("safari"))
				this.driver.findElement(By.linkText("Pop Out the Metadata Card")).click();
			else
				ActionEventUtils.click(driver,this.driver.findElement(By.linkText("Pop Out the Metadata Card")));
			//	this.driver.findElement(By.linkText("Pop Out the Metadata Card")).click();
			driver.switchTo().defaultContent();
		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.collapseHeader : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.collapseHeader : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End popOutMetadatacard

	/**
	 * isHeaderCollapsed : Verify header is collapsed in metadatacard
	 * 
	 * @return true if collapsed ; false if not
	 * @throws Exception
	 */
	public boolean isHeaderCollapsed() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement toggleButton = this.driver.findElement(By.cssSelector("div[class='mf-toggleheader-button']"));
			if(this.driver.findElement(By.cssSelector("div[id='metadatacard-0']")).getAttribute("class").toString().contains("mf-header-collapsed") && toggleButton.getAttribute("title").equals("Expand Title"))
				return true;
			else 
				return false;
		}//End try

		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.isHeaderCollapsed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isHeaderCollapsed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isHeaderCollapsed



	/**
	 * getObjectIcon : Get the style attribute of the icon's element. The attribute contains the URL of the used icon.
	 * @return Contents of the style attribute as a String, containing the URL of the icon
	 * @throws Exception 
	 */
	public String  getObjectIcon() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			return this.driver.findElement(By.cssSelector("div[class*='mf-obj-icon']")).getAttribute("style").trim();
		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getObjectIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getObjectIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getObjectIcon 

	/**
	 * getCreatedBy : get the name of created that object
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getCreatedBy() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			return this.driver.findElement(By.id("mf-createdby-control")).getText();
		}//End try
		catch(Exception e) {
			if(e.getClass().toString().toLowerCase().contains("NoSuchElementException"))
				return "";
			else
				throw new Exception ("Exception at MetadataCard.getCreatedBy : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getCreatedBy : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getCreatedBy

	/**
	 * getCreatedDate : Get the created date for object
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String getCreatedDate() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement date = this.driver.findElement(By.cssSelector("span[id='mf-created-control']>span[class='mf-valuefield can-highlight']"));
			String createddate = date.getText();	
			Utils.fluentWait(driver);

			return createddate;

		}
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getCreatedDate : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getCreatedDate : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}

	/**
	 *getLastModifiedBy : get the name for last modified that object
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getLastModifiedBy() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			return this.driver.findElement(By.id("mf-lastmodifiedby-control")).getText();

		}//end try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getLastModifiedBy : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getLastModifiedBy : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getLastModifiedBy

	/**
	 * getLastModifiedDate : get the last modified date for object
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getLastModifiedDate() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.driver.findElement(By.cssSelector("span[id='mf-lastmodified-control']>span[class*='mf-valuefield']")).getText().split(" ")[0];
		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getLastModifiedBy : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getLastModifiedBy : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getLastModifiedDate

	/**
	 * getObjectType : get the object type 
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String  getObjectType() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.driver.findElement(By.id("mf-filetype")).getText();
		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getObjectType : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getObjectType : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getObjectType

	/**
	 * isEditMode : verify the Edit mode is displayed 
	 * 
	 * @param tabName
	 * @return true if displayed;false if not
	 * @throws Exception 
	 */
	public boolean isEditMode() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement workflowField = this.driver.findElement(By.cssSelector("div[id='metadatacard-0']"));

			if(workflowField.getAttribute("class").toString().contains("mf-editmode"))
				return true;
			else
				return false;

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isEditMode : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isEditMode : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isEditMode

	/**
	 * getHeaderColor : get the background color
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String getHeaderColor() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement header = this.driver.findElement(By.cssSelector("div[class='mf-header-bar']"));
			return header.getCssValue("background-color");

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getHeaderColor : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getHeaderColor : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getHeaderColor

	/**
	 * getTitle : get the Title for metadata card
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public String getTitle() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			String title = "";
			title = this.driver.findElement(By.cssSelector("div[class*='mf-filename']")).getText();

			if(title.equals(""))
				title = this.driver.findElement(By.cssSelector("div[class='mf-header-bar']")).findElement(By.cssSelector("div[class*='mf-filename']")).getText();
			System.out.println(title);
			return title;
		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getTitle : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getTitle : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getTitle

	/**
	 * getVersion : get the version for Object
	 * @param tabName
	 * @return version of object
	 * @throws Exception 
	 */
	public int getVersion() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			int version = 0;
			WebElement workflowField = this.driver.findElement(By.cssSelector("span[class*='mf-objectversion']>span[class*='mf-valuefield']"));
			version = Integer.parseInt(workflowField.getText());
			return version;

		}//End try
		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.getVersion : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getVersion : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getVersion


	/**
	 * Verify if object version is displayed or not
	 * 
	 * @return true if displayed; false if not
	 * @throws Exception 
	 */
	public int getObjectID() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement metadata = this.driver.findElement(By.cssSelector("div[id*='metadatacard']>div[id='mf-header']"));
			WebElement objectID = metadata.findElement(By.cssSelector("span[class*='mf-objectid']>span[class*='mf-valuefield']"));
			int ID = Integer.parseInt(objectID.getText());

			return ID;

		}//End try
		catch(Exception e){
			throw new Exception ("Exception at MetadataCard.getObjectID : " + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getObjectID : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isObjectIDDisplayed

	/**
	 * isAssignmentCompleted: Checks if Assignment in completed state
	 * @param None
	 * @return true if assignment completed; false if not
	 * @throws Exception 
	 */
	public Boolean isAssignmentCompleted() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-lookup-item-cell-image']>img[src*='not-approved.png']"));

			if (field.isDisplayed())
				return false;
			else 
				return true;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isAssignmentCompleted : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isAssignmentCompleted : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isAssignmentCompleted

	/**
	 * isNotApprovedDisplayed: Checks if Not Approved button is displayed
	 * @param None
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isNotApprovedDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-test']"));

			//if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())
			if (field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotApprovedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotApprovedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApprovedDisplayed

	/**
	 * isNotApprovedDisplayed: Checks if Not Approved button is displayed
	 * @param None
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isApprovedDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-uncompleted']"));

			//if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())
			if (field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotApprovedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotApprovedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApprovedDisplayed



	/**
	 * isNotApproveDisplayed: Checks if Not Approved button is displayed
	 * @param None
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	private Boolean isNotApproveDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-test']"));

			boolean isDisplayed = true;

			try
			{
				isDisplayed = field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE");
			}
			catch(NullPointerException e0)
			{
				isDisplayed = false;
			}

			if (!isDisplayed && field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())
				//	if (field.findElement(By.cssSelector("img[src*='not-approved.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotApproveDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isNotApproveDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApproveDisplayed



	/**
	 * isNotApprovedDisplayed: Checks if Not Approved button is displayed
	 * @param propertyIndex - Index of an property
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isNotApprovedDisplayed(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-test']"));


			/*if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[title='Mark approved']")).isDisplayed())*/
			if (field.get(propertyIndex).findElement(By.cssSelector("img[title='Mark approved']")).isDisplayed())	
				return true;
			else 
				return false;

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-completed']"));

			if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
				return true;
			else 
				return false;*/

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotApprovedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotApprovedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApprovedDisplayed

	/**
	 * isNotApprovedDisplayed: Checks if Not Approved button is displayed
	 * @param propertyIndex - Index of an property
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	private Boolean isNotApproveDisplayed(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-test']"));

			//if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[title='Mark approved']")).isDisplayed())
			if (field.get(propertyIndex).findElement(By.cssSelector("img[title='Mark approved']")).isDisplayed())
				return true;
			else 
				return false;

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-completed']"));

			if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
				return true;
			else 
				return false;*/

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotApproveDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isNotApproveDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApprovedDisplayed

	/**
	 * isNotApprovedDisplayed: Checks if Not Approved button is displayed
	 * @param propertyIndex - Index of an property
	 * @return true if Not Approved button is displayed; false if not
	 * @throws Exception 
	 */
	private Boolean isVariesIconDisplayed(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-varies']"));

			//if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[title='Mark approved']")).isDisplayed())
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='images/status_blue']")).isDisplayed())
				return true;
			else 
				return false;

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-completed']"));

			if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
				return true;
			else 
				return false;*/

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isVariesIconDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isVariesIconDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotApprovedDisplayed

	/**
	 * isNotRejectedDisplayed: Checks if Not Rejected button is displayed
	 * @param None
	 * @return true if Not Rejected button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isNotRejectedDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-test']"));

			//if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
			if (field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotRejectedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotRejectedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotRejectedDisplayed


	/**
	 * isNotRejectedDisplayed: Checks if Not Rejected button is displayed
	 * @param None
	 * @return true if Not Rejected button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isRejectedDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-test']"));

			//if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
			if (field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotRejectedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotRejectedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotRejectedDisplayed

	/**
	 * isNotRejectDisplayed: Checks if Not Rejected button is displayed
	 * @param None
	 * @return true if Not Rejected button is displayed; false if not
	 * @throws Exception 
	 */
	private Boolean isNotRejectDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");

			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-test']"));

			/*if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())*/
			if (field.findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotRejectDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isNotRejectDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotRejectDisplayed

	/**
	 * isNotRejectedDisplayed: Checks if Not Rejected button is displayed
	 * @param propertyIndex - Index of an property
	 * @return true if Not Rejected button is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isNotRejectedDisplayed(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			WebElement propTable = this.getPropertyElement("Assigned to");

			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-test']"));

			/*if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())*/
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotRejectedDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isNotRejectedDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotRejectedDisplayed

	/**
	 * isNotRejectDisplayed: Checks if Not Rejected button is displayed
	 * @param propertyIndex - Index of an property
	 * @return true if Not Rejected button is displayed; false if not
	 * @throws Exception 
	 */
	private Boolean isNotRejectDisplayed(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");

			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-test']"));

			/*if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())*/
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-rejected.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isNotRejectDisplayed : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isNotRejectDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isNotRejectDisplayed

	/**
	 * isApprovedSelected: Checks if Approve button is selected
	 * @param None
	 * @return true if Approved button is selected; false if not
	 * @throws Exception 
	 */
	public Boolean isApprovedSelected() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			/*if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-completed']"));*/

			/*if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())*/
			if (field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
				return true;
			else 
				return false;
			/*	if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				return false;

			List<WebElement> approvedIcon = driver.findElements(By.cssSelector("img[src*='approved.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : approvedIcon)
				if (loopIcon.isDisplayed()) 
					return true;

			return false;*/

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isApprovedSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isApprovedSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isApprovedSelected

	/**
	 * isApproveSelected: Checks if Approve button is selected
	 * @param None
	 * @return true if Approved button is selected; false if not
	 * @throws Exception 
	 */
	private Boolean isApproveSelected() throws Exception {

		final long startTime = StopWatch.startTime();

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");


		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			if (field.getAttribute("style") != null && field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='mf-assignee-state-completed']"));

			Utils.fluentWait(driver);

			if(browser.equalsIgnoreCase("edge"))
				if(field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
					return true;
				else 
					return false;
			else
				if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
					return true;
				else 
					return false;


			/*	if (field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE"))
				return false;

			List<WebElement> approvedIcon = driver.findElements(By.cssSelector("img[src*='approved.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : approvedIcon)
				if (loopIcon.isDisplayed()) 
					return true;

			return false;*/

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isApproveSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isApproveSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isApproveSelected

	/**
	 * isApprovedSelected: Checks if Approve button is selected for the user with more than one value
	 * @param propertyIndex	- Index of the property value
	 * @return true if Approved button is selected; false if not
	 * @throws Exception 
	 */
	public Boolean isApprovedSelected(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			//if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isApprovedSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isApprovedSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isApprovedSelected

	/**
	 * isCompleteSelected: Checks if Complete button is selected for the user with more than one value
	 * @param propertyIndex	- Index of the property value
	 * @return true if Complete button is selected; false if not
	 * @throws Exception 
	 */
	public Boolean isCompleteSelected(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-completed']"));

			//if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isCompleteSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isCompleteSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isCompleteSelected

	/**
	 * isApproveSelected: Checks if Approve button is selected for the user with more than one value
	 * @param propertyIndex	- Index of the property value
	 * @return true if Approved button is selected; false if not
	 * @throws Exception 
	 */
	private Boolean isApproveSelected(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			List <WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-approved']"));

			//	if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='approved.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isApproveSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isApproveSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isApproveSelected

	/**
	 * isRejectedSelected: Checks if Rejected button is selected
	 * @param None
	 * @return true if Rejected button is selected; false if not
	 * @throws Exception 
	 */
	public Boolean isRejectedSelected() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-rejected']"));

			//	if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())
			if (field.findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isRejectedSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isRejectedSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isRejectedSelected

	/**
	 * isRejectSelected: Checks if Rejected button is selected
	 * @param None
	 * @return true if Rejected button is selected; false if not
	 * @throws Exception 
	 */
	private Boolean isRejectSelected() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("div[class*='mf-assignee-state-rejected']"));

			/*if (!field.getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())*/
			if (field.findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isRejectSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isRejectSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isRejectSelected

	/**
	 * isRejectedSelected: Checks if Rejected button is selected
	 * @param None
	 * @return true if Rejected button is selected; false if not
	 * @throws Exception 
	 */
	public Boolean isRejectedSelected(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-rejected']"));

			/*if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())*/
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())	
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isRejectedSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isRejectedSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isRejectedSelected

	/**
	 * isRejectSelected: Checks if Rejected button is selected
	 * @param None
	 * @return true if Rejected button is selected; false if not
	 * @throws Exception 
	 */
	private Boolean isRejectSelected(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("div[class*='mf-assignee-state-rejected']"));

			/*if (!field.get(propertyIndex).getAttribute("style").toUpperCase().contains("DISPLAY: NONE") && field.get(propertyIndex).findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())*/
			if (field.get(propertyIndex).findElement(By.cssSelector("img[src*='rejected.png']")).isDisplayed())
				return true;
			else 
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.isRejectSelected : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.isRejectSelected : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isRejectedSelected

	/**
	 * clickApproveIcon: Clicks Approve Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickApproveIcon() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			/*List<WebElement> notApprovedIcons = driver.findElements(By.cssSelector("img[src*='not-approved.png'][title='Mark complete']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notApprovedIcons)
				if (loopIcon.isDisplayed())  {
					loopIcon.click();
					break;
				}

			field.findElement(By.cssSelector("img[src*='not-approved.png'][title='Mark complete']")).click(); //Clicks Not Approved icon
			 */

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			List<WebElement> notApprovedIcons = field.findElements(By.cssSelector("img[src*='not-approved.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notApprovedIcons)
				if (loopIcon.isDisplayed())  {
					ActionEventUtils.click(driver,loopIcon);
					//loopIcon.click();
					break;
				}

			return (this.isApproveSelected());

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickApproveIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickApproveIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickApproveIcon


	/**
	 * clickVariesIcon: Clicks varies Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickVariesIcon(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));

			MetadataCard metadatacard = new MetadataCard (this.driver);
			field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-approved.png']")).click(); //Clicks Not Approved icon
			 */

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));


			if (!this.isVariesIconDisplayed(propertyIndex))
				return false;

			List<WebElement> variesIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='images/status_blue.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : variesIcons)
				if (loopIcon.isDisplayed())  {
					//loopIcon.click();
					ActionEventUtils.click(driver,loopIcon);
					break;
				}

			return (this.isVariesIconDisplayed(propertyIndex));

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickVariesIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickVariesIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickApproveIcon

	/**
	 * clickApproveIcon: Clicks Approve Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickApproveIcon(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));

			MetadataCard metadatacard = new MetadataCard (this.driver);
			field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-approved.png']")).click(); //Clicks Not Approved icon
			 */

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));


			if (!this.isNotApproveDisplayed(propertyIndex))
				return false;

			List<WebElement> notApprovedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='not-approved.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notApprovedIcons)
				if (loopIcon.isDisplayed())  {
					//loopIcon.click();
					ActionEventUtils.click(driver,loopIcon);
					break;
				}

			return (this.isApproveSelected(propertyIndex));

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickApproveIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickApproveIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickApproveIcon


	/**
	 * clickCompleteIcon: Clicks Complete Icon in the Assigned to property
	 * @param None
	 * @return true if Complete icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickCompleteIcon(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			if (!this.isCompleteSelected(propertyIndex))
				return false;

			List<WebElement> notApprovedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='not-approved.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notApprovedIcons)
				if (loopIcon.isDisplayed())  {
					//loopIcon.click();
					ActionEventUtils.click(driver,loopIcon);
					break;
				}

			return (this.isCompleteSelected(propertyIndex));

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickApproveIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickApproveIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickCompleteIcon

	/**
	 * clickApproveIcon: Clicks Approve Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickApproveIcon(Boolean isApprove) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));

			MetadataCard metadatacard = new MetadataCard (this.driver);*/

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			if (isApprove && this.isNotApproveDisplayed()) {
				List<WebElement> notApprovedIcons = field.findElements(By.cssSelector("img[src*='not-approved.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notApprovedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						//loopIcon.click();
						break;
					}
			}
			else if (!isApprove && !this.isNotApproveDisplayed()) {
				List<WebElement> notApprovedIcons = field.findElements(By.cssSelector("img[src*='approved.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notApprovedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						//						loopIcon.click();
						break;
					}
			}

			if (isApprove && this.isApproveSelected())
				return true;
			else if (!isApprove && !this.isApproveSelected())
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickApproveIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickApproveIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickApproveIcon

	/**
	 * clickApproveIcon: Clicks Approve Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickApproveIcon(Boolean isApprove, int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			if (isApprove && this.isNotApproveDisplayed(propertyIndex)) {
				List<WebElement> notApprovedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='not-approved.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notApprovedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}
			}
			else if (!isApprove && !this.isNotApproveDisplayed(propertyIndex)) {
				List<WebElement> notApprovedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='approved.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notApprovedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}
			}

			if (isApprove && this.isApproveSelected())
				return true;
			else if (!isApprove && !this.isApproveSelected())
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickApproveIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickApproveIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickApproveIcon

	/**
	 * clickRejectIcon: Clicks Reject Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickRejectIcon() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!this.isNotRejectDisplayed())
				return false;

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));


			field.findElement(By.cssSelector("img[src*='not-rejected.png']")).click(); //Clicks Not Approved icon

			if (this.isRejectedSelected())
				return true;
			else 
				return false;*/

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			List<WebElement> notRejectedIcons = field.findElements(By.cssSelector("img[src*='not-rejected.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notRejectedIcons)
				if (loopIcon.isDisplayed())  {
					ActionEventUtils.click(driver, loopIcon);
					//loopIcon.click();
					break;
				}

			return (this.isRejectSelected());


		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickRejectIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickRejectIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickRejectIcon

	/**
	 * clickRejectIcon: Clicks Reject Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickRejectIcon(int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			/*
			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));

			MetadataCard metadatacard = new MetadataCard (this.driver);

			if (!this.isNotRejectedDisplayed())
				return false;

			field.get(propertyIndex).findElement(By.cssSelector("img[src*='not-rejected.png']")).click(); //Clicks Not Approved icon

			if (this.isRejectedSelected())
				return true;
			else 
				return false;
			 */

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));


			if (!this.isNotRejectDisplayed(propertyIndex))
				return false;

			List<WebElement> notRejectedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='not-rejected.png']")); //Clicks Not Approved icon

			for (WebElement loopIcon : notRejectedIcons)
				if (loopIcon.isDisplayed())  {
					ActionEventUtils.click(driver,loopIcon);
					break;
				}

			return (this.isRejectSelected(propertyIndex));

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickRejectIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickRejectIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickRejectIcon

	/**
	 * clickRejectIcon: Clicks Reject Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickRejectIcon(Boolean isReject) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			/*WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']>div[class*='mf-assignee-state-approved']"));

			MetadataCard metadatacard = new MetadataCard (this.driver);*/

			WebElement propTable = this.getPropertyElement("Assigned to");
			WebElement field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			if (isReject && this.isNotRejectDisplayed()) {
				List<WebElement> notRejectedIcons = field.findElements(By.cssSelector("img[src*='not-rejected.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notRejectedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}

				//field.findElement(By.cssSelector("img[src*='not-rejected.png']")).click(); //Clicks Not Approved icon
			}
			else if (!isReject && !this.isNotRejectDisplayed()) {
				List<WebElement> notRejectedIcons = field.findElements(By.cssSelector("img[src*='rejected.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notRejectedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}
				//field.findElement(By.cssSelector("img[src*='rejected.png']")).click(); //Clicks Not Approved icon
			}

			if (isReject && this.isRejectSelected())
				return true;
			else if (!isReject && !this.isRejectSelected())
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickRejectIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickRejectIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickRejectIcon

	/**
	 * clickRejectIcon: Clicks Reject Icon in the Assigned to property
	 * @param None
	 * @return true if Approve icon is selected; false if not
	 * @throws Exception 
	 */
	public Boolean clickRejectIcon(Boolean isReject, int propertyIndex) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			WebElement propTable = this.getPropertyElement("Assigned to");
			List<WebElement> field = propTable.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).
					findElements(By.cssSelector("td[class*='mf-dynamic-controlfield']>div[class*='mf-control mf-dynamic-control']>div[class*='mf-internal-lookups']>div[class='mf-internal-lookup']>div[class='mf-lookup-item-container']>div[class='mf-lookup-item-row']"));

			if (isReject && this.isNotRejectDisplayed(propertyIndex)) {
				List<WebElement> notRejectedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='not-rejected.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notRejectedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}
			}
			else if (!isReject && !this.isNotRejectDisplayed(propertyIndex)) {
				List<WebElement> notRejectedIcons = field.get(propertyIndex).findElements(By.cssSelector("img[src*='rejected.png']")); //Clicks Not Approved icon

				for (WebElement loopIcon : notRejectedIcons)
					if (loopIcon.isDisplayed())  {
						ActionEventUtils.click(driver,loopIcon);
						break;
					}
			}

			if (isReject && this.isRejectSelected())
				return true;
			else if (!isReject && !this.isRejectSelected())
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickRejectIcon : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickRejectIcon : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End clickRejectIcon

	/**
	 * clickNextBtn : Click Next button in the metadatacard
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	private void nextBtn() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement nextBtn=driver.findElement(By.cssSelector("button[class^='mf-next-button']"));
			//	nextBtn.click();
			ActionEventUtils.click(driver, nextBtn);
			driver.switchTo().defaultContent();
			Log.event("'Next' button clicked on MetadataCard dialog.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.nextBtn : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.nextBtn : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //clickNextBtn

	/**
	 * clickNextBtn : Click Next button in the metadatacard
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickNextBtn() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			this.nextBtn();
		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at MetadataCard.clickNextBtn : " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();  //Switches to home page driver
			Log.event("MetadataCard.clickNextBtn : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //clickNextBtn

	/**
	 * templateDialogExists : To Check if Template dialog exists
	 * @param None
	 * @return 
	 * @return None
	 * @throws Exception
	 */
	private boolean templateDialogExists() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			List<WebElement> FilterList = driver.findElements(By.cssSelector("div[id='mf-bottom-header']>ul[class='mf-filter-buttons']>li"));

			if (FilterList.size() <= 0)
				return false;
			else
				return true;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e) 		{
			throw new Exception ("Exception at MetadataCard.templateDialogExists : " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MetadataCard.templateDialogExists : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End isTemplateDialogExists

	/**
	 * isTemplateDialogExists : To Check if Template dialog exists
	 * @param None
	 * @return 
	 * @return None
	 * @throws Exception
	 */
	public boolean isTemplateDialogExists() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.templateDialogExists();

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e) 		{
			throw new Exception("MetadataCard.isTemplateDialogExists :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.isTemplateDialogExists.", StopWatch.elapsedTime(startTime));
		}

	} //End isTemplateDialogExists

	/**
	 * templateFilter  : Select Template folder in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private Boolean templateFilter(String Filter) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			List<WebElement> FilterList = driver.findElements(By.cssSelector("div[id='mf-bottom-header']>ul[class='mf-filter-buttons']>li")); 

			for(WebElement tab:FilterList) {
				if(tab.getText().trim().equalsIgnoreCase(Filter)) {
					ActionEventUtils.click(driver, tab);
					//tab.click();
					Utils.fluentWait(driver);
					return true;
				}
			}
			return false;
		}//End try
		catch(NoSuchElementException e)
		{
			return false;
		}		
		catch(Exception e)
		{
			throw new Exception("MetadataCard.templateFilter :  " + e.getMessage(), e);
		}
		finally {
			Log.event("Metadatacard.templateFilter.", StopWatch.elapsedTime(startTime));
		}

	} //End selectTemplateFilter

	/**
	 * selectTemplateFilter  : Select Template folder in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public Boolean selectTemplateFilter(String Filter) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			return this.templateFilter(Filter);

		}//End try		
		catch(Exception e) {
			throw new Exception("MetadataCard.selectTemplateFilter :  " + e.getMessage(), e);
		}
		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.selectTemplateFilter.", StopWatch.elapsedTime(startTime));
		}

	} //End selectTemplateFilter

	/**
	 * setTemplate  : Set Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public MetadataCard setTemplate(String template) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();

			if (!this.templateDialogExists() && !this.getTemplateObjectType().equalsIgnoreCase("DOCUMENT"))
				return new MetadataCard(driver);

			if (!this.templateDialogExists() && this.getTemplateObjectType().equalsIgnoreCase("DOCUMENT"))
				throw new Exception("Template dialog is not opened for Document object type.");

			this.templateFilter(Caption.Template.Template_All.Value); // Select template

			if (template.equals("") || template.equals(null)) {
				template = this.getTemplate(0, Caption.Template.Template_Blank.Value);
				this.selectTemplate(template); // Select template
			}
			else
				this.selectTemplate(template, template); // Select template

			this.nextBtn(); // Click Next buttton
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			Utils.fluentWait(driver);
			return new MetadataCard(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.setTemplate :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.setTemplate.", StopWatch.elapsedTime(startTime));
		}

	}//End setTemplate

	/**
	 * setTemplateUsingSearchkey  : Set Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public MetadataCard setTemplateUsingSearchkey(String template, String searchKey) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.selectTemplate(template, searchKey); // Select template 
			this.nextBtn(); // Click Next buttton
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			return new MetadataCard(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.setTemplateUsingSearchkey :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.setTemplateUsingSearchkey.", StopWatch.elapsedTime(startTime));
		}

	}//End setTemplateUsingSearchkey

	/**
	 * setTemplateUsingFilter  : Set Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public MetadataCard setTemplateUsingFilter(String template, String Filter) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.templateFilter(Filter);
			this.selectTemplate(template); // Select template 
			this.nextBtn(); // Click Next buttton
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			return new MetadataCard(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.setTemplateUsingSearchkey :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.setTemplateUsingSearchkey.", StopWatch.elapsedTime(startTime));
		}

	}//End setTemplate

	/**
	 * setTemplateUsingFilterAndSearchKey  : Set Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public MetadataCard setTemplateUsingFilterAndSearchKey(String template, String Filter, String searchKey) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.templateFilter(Filter);
			this.selectTemplate(template, searchKey); // Select template 
			this.nextBtn(); // Click Next buttton
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
			return new MetadataCard(driver);
		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.setTemplateUsingFilterAndSearchKey :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.setTemplateUsingFilterAndSearchKey.", StopWatch.elapsedTime(startTime));
		}

	}//End setTemplateUsingFilterAndSearchKey

	/**
	 * setTemplateUsingClass  : Sets the template after selecting the class
	 * @param ClassToSelect Class from which template to be selected
	 * @return 
	 * @throws Exception
	 */
	public void setTemplateUsingClass(String ClassToSelect) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();
			this.selectClassInTemplate(ClassToSelect); // Select template 
			//this.nextBtn(); // Click Next button
			Utils.fluentWait(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.setTemplateUsingClass :  " + e.getMessage(), e);
		} //End catch

		finally {
			this.switchToDefaultContent();
			Log.event("Metadatacard.setTemplateUsingClass.", StopWatch.elapsedTime(startTime));
		}

	}//End selectTemplate

	/**
	 * selectTemplate  : Select Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private boolean selectTemplate(String template) throws Exception {

		boolean value = false;
		final long startTime = StopWatch.startTime();

		try {

			if(template == "" || template == null)
				return value;

			Utils.fluentWait(driver);
			List<WebElement> options = driver.findElements(By.cssSelector("div[class='mf-listing-content']>ul>li[class*='mf-file-item']:not([style*='display: none;'])"));

			for(int count = 0; count < options.size(); count++) {
				if(options.get(count).getAttribute("title").contains("(."+template+")")) {
					//options.get(count).click();
					ActionEventUtils.click(driver,options.get(count));
					value = true;
					break;
				}
			}

			if(!value) {
				for(int count = 0; count < options.size(); count++) {
					if(options.get(count).getAttribute("title").contains(template)) {
						//options.get(count).click();
						ActionEventUtils.click(driver,options.get(count));
						value = true;
						break;
					}
				}
			}



		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.selectTemplate :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.selectTemplate.", StopWatch.elapsedTime(startTime));
		}

		return value;

	}//End selectTemplate

	/**
	 * selectTemplate  : Select Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private boolean selectTemplate(String template, String searchkey) throws Exception {

		boolean value = false;

		final long startTime = StopWatch.startTime();

		try {

			if(template == "" || template == null)
				return value;

			driver.findElement(By.cssSelector("input[id='mf-template-filter']")).clear();
			driver.findElement(By.cssSelector("input[id='mf-template-filter']")).sendKeys(searchkey);
			Utils.fluentWait(driver);
			List<WebElement> options = driver.findElements(By.cssSelector("div[class='mf-listing-content']>ul>li[class*='mf-file-item']:not([style*='display: none;'])"));

			for(int count = 0; count < options.size(); count++) {
				if(options.get(count).getAttribute("title").contains("(."+template+")")) {
					//options.get(count).click();
					ActionEventUtils.click(driver,options.get(count));
					value = true;
					break;
				}
			}

			if(!value) {
				for(int count = 0; count < options.size(); count++) {
					if(options.get(count).getAttribute("title").contains(template)) {
						ActionEventUtils.click(driver,options.get(count));
						//options.get(count).click();
						value = true;
						break;
					}
				}
			}

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.selectTemplate :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.selectTemplate.", StopWatch.elapsedTime(startTime));
		}
		return value;
	}//End selectTemplate

	/**
	 * selectClassInTemplate  : Select class in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private boolean selectClassInTemplate(String classToSelect) throws Exception {

		boolean value = false;
		final long startTime = StopWatch.startTime();
		try {

			if(classToSelect == "" || classToSelect == null)
				return value;

			driver.findElement(By.cssSelector("input[class='mf-internal-text mf-property-100-input-0 ui-autocomplete-input']")).clear();
			driver.findElement(By.cssSelector("input[class='mf-internal-text mf-property-100-input-0 ui-autocomplete-input']")).sendKeys(classToSelect);
			Utils.fluentWait(driver);
			List<WebElement> values = this.driver.findElements(By.cssSelector("ul[class*='ui-autocomplete']>li[class*='ui-menu-item']>a>span"));
			Thread.sleep(1000);
			for(int count = 0; count < values.size(); count++) {
				if(values.get(count).getText().trim().equals(classToSelect)) {
					ActionEventUtils.click(driver,values.get(count));
					//values.get(count).click();
					Utils.fluentWait(driver);
					this.nextBtn();
					break;
				}	
			}
			Utils.fluentWait(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.selectClassInTemplate :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.selectClassInTemplate.", StopWatch.elapsedTime(startTime));
		}
		return value;
	}//End selectClass

	/**
	 * getTemplate  : Select Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private String getTemplate(int index, String filter) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (filter.equals("") || filter.equals(null))
				filter = Caption.Template.Template_All.Value;

			this.templateFilter(filter);

			List<WebElement> options = driver.findElements(By.cssSelector("div[class='mf-listing-content']>ul>li[class*='mf-file-item']:not([style*='display: none;'])"));

			if (options.size() <= index)
				throw new Exception("Template does not exists.");

			return (options.get(index).getText());


		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.getTemplate :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.getTemplate.", StopWatch.elapsedTime(startTime));
		}

	}//End getTemplate

	/**
	 * getTemplate  : Select Template in Document template dialog
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	private String getTemplateObjectType() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			//return (this.driver.findElement(By.cssSelector("span[class*='mf-header-title'],span[class*='ui-dialog-title']")).getText());

			driver.switchTo().defaultContent();
			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			return (mfilesDialog.getTitle());

		}//End try
		catch (Exception e) {
			throw new Exception("MetadataCard.getTemplateObjectType :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.getTemplateObjectType.", StopWatch.elapsedTime(startTime));
		}

	}//End getTemplate

	/**
	 * getExtension : Gets the extension of the specified property
	 * @param None
	 * @return Extension of the document
	 * @throws Exception 
	 */
	private String getExtension() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement field = this.driver.findElement(By.className("mf-template-info"));
			String extn[] = field.getText().split("\\.");

			if (extn.length > 1)
				return (extn[1].trim().split("\\)")[0]);
			else
				return "";

		} //End try
		catch (Exception e) {
			throw new Exception("MetadataCard.getExtension :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.getExtension.", StopWatch.elapsedTime(startTime));
		}

	} //End getExtension

	/**
	 * switchFrame : Switches frame to metadatacard dialog
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void switchFrame() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.driver.switchTo().frame(metadataDialog);

		} 
		catch (Exception e) {
			throw new Exception("MetadataCard.switchFrame :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.switchFrame.", StopWatch.elapsedTime(startTime));
		}

	} //End switchFrame

	/**
	 * switchToDefaultContent : Switches frame to default content
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void switchToDefaultContent() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.driver.switchTo().defaultContent();

		} 
		catch (Exception e) {
			throw new Exception("MetadataCard.switchToDefaultContent :  " + e.getMessage(), e);
		} //End catch

		finally {
			Log.event("Metadatacard.switchToDefaultContent.", StopWatch.elapsedTime(startTime));
		}

	} //End switchToDefaultContent

	/**
	 * isMetadataCardOpened : TO Check if Metadatacard is opened
	 * @param driver Webdriver
	 * @throws Exception
	 */
	public static Boolean isMetadataCardOpened(final WebDriver driver) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);

			new WebDriverWait(driver, 360).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])"))));

			driver.findElement(By.cssSelector("div[class*='ui-dialog'][role*='dialog']:not([style*='display: none;'])")).findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx'],iframe[src*='objecttemplateselector.html.aspx']"));
			return true;
		}
		catch (Exception e) {
			return false;
		}

		finally {
			Log.event("Metadatacard.isMetadataCardOpened.", StopWatch.elapsedTime(startTime));
		}

	}

	/*------------------------Function required by Smoke test suite--------------------------------------------*/

	/**
	 * Move focus to Metadatacard
	 */
	public void selectMetadataCard() {

		WebElement metadataCardDialog = driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
		driver.switchTo().frame(metadataCardDialog);
	}

	/**
	 * Description: Create a 'Document' Object
	 * @param driver
	 * @param dataValues
	 * @param browser
	 * @return HomePage
	 * @throws Exception 
	 */
	public HomePage createDocumentObject(WebDriver driver,Map <String, String> dataValues) throws Exception{
		final long startTime = StopWatch.startTime();

		try{

			//Select Document Class
			if (!dataValues.get("Class").isEmpty()){
				String documentClass=selectNewObjectClass(dataValues.get("Class"),driver);
				Thread.sleep(500);
				if (documentClass.isEmpty()&& !documentClass.trim().equalsIgnoreCase(dataValues.get("Class")))
					throw new Exception("Could not select Document Class.");
			}

			//Enter Object properties
			String objName=enterObjectProperties("Name or title",dataValues.get("Title"));

			if (!objName.trim().equalsIgnoreCase(dataValues.get("Title").trim()) || objName.trim().isEmpty()) {
				throw new Exception("Object property is not entered correctly or Object value is Empty.");
			}

			//Select Document permissions
			if (!dataValues.get("Permission").isEmpty()){
				Log.message("8. Verify if document Permission is selected.");
				String documentPermissions=selectDocumentPermissions(dataValues.get("Permission"),driver);

				if (documentPermissions.isEmpty())
					throw new Exception("Could not select Document Permissions.");
				Log.message("---Successfully selected the Document Permissions");
			}

			//Select Document Workflow
			if (!dataValues.get("Workflow").isEmpty()){
				Log.message("10. Select Document Workflow.");
				String documentWorkflow= selectDocumentWorkflow(dataValues.get("Workflow"),driver);

				if (documentWorkflow.isEmpty())
					throw new Exception("Could not select Document workflow.");
				Log.message("--Document Workflow has been selected.");
			}

			/*	if ((dataValues.get("isSingleFile").equalsIgnoreCase("Yes")|| dataValues.get("isSingleFile").equalsIgnoreCase("No")) && dataValues.get("isTemplate").equalsIgnoreCase("No"))
				clickUploadBtn(dataValues.get("FileLocation"),dataValues);*/
			//			
			//			try {
			//			    String[] commands = new String[]{};
			//			    // Location of the autoit executable
			//			    if (browser.equalsIgnoreCase("Chrome"))
			//			    	commands = new String[] {"D:\\UploadFileWith_AutoIt1.exe"};
			//			    else if (browser.equalsIgnoreCase("Firefox"))
			//			    	commands = new String[] {"D:\\UploadFileWith_AutoIt2.exe"};
			//			    Runtime.getRuntime().exec(commands);
			//			   
			//			}       
			//			catch (IOException e) 
			//			{
			//				Log.message("Unable to add a file to FileUpload dialog.");
			//			}

			//			try{
			//			//upload file with Sikuli
			//				uploadFilesWithSikuli();
			//			}
			//			catch (Exception e) 
			//			{
			//				e.printStackTrace();
			//			}

			this.setCheckInImmediately(true);
			this.saveAndClose();
			/*			//enable 'checkin Immediately' checkbox
			this.checkInImmediatly(true);
			//Click 'Create' button on metadatacard
			this.saveAndClose();
			 */			
			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='dialog-gradient'][style*='display: block']")));
			Utils.fluentWait(driver);

		}catch(Exception e){
			throw new Exception("Error while creating 'Document' object.");

		}
		Log.event("---Successfully creatd the Document object",StopWatch.elapsedTime(startTime));
		return new HomePage(driver);
	}

	/**
	 * Description: Create a 'Assignment' Object
	 * @param driver
	 * @param dataValues
	 * @return HomePage
	 * @throws Exception 
	 */
	public HomePage createAssignmentObject(WebDriver driver,Map<String,String> dataValues) throws Exception{

		try{

			WebElement metadataCard = driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
			driver.switchTo().frame(metadataCard);

			//Select Assignment Class
			if (!dataValues.get("Class").isEmpty()){
				String assignmentClass=readMetadatacardPropertyValues("Class",dataValues.get("Class"));
				if (assignmentClass.isEmpty()|| !assignmentClass.trim().equalsIgnoreCase(dataValues.get("Class").trim()))
					throw new Exception("Could not select Assignment Class.");

			}

			//Enter Assignment title
			if (!dataValues.get("Title").isEmpty()){
				String assignmentName=enterObjectProperties("Name or title",dataValues.get("Title"));
				if (assignmentName.isEmpty())
					throw new Exception("Could not enter Assignment title");
			}

			if(!dataValues.get("AssignmentDescription").isEmpty()) {
				String assignmentDesc=enterObjectProperties("Assignment description",dataValues.get("AssignmentDescription"));
				if (assignmentDesc.isEmpty())
					throw new Exception("Could not enter Assignment description");
			}
			//Assign To: User
			String user=dataValues.get("AssignedTo");
			user=selectMetadataValuesFromList(driver,user,"Assigned to").trim();
			if (!user.equalsIgnoreCase(dataValues.get("AssignedTo").trim())) {
				throw new Exception("Unable to select 'Assign To' field value");
			}

			//			
			//			//Select Assignment permissions
			//			if (!dataValues.get("Permission").isEmpty()){
			//				Log.message("8. Verify if Assignment Permission is selected.");
			//				String documentPermissions=selectDocumentPermissions(dataValues.get("Permission"),driver);
			//				
			//				if (documentPermissions.isEmpty())
			//					throw new Exception("Could not select Assignment Permissions.");
			//				Log.message("---Successfully selected the Assignment Permissions");
			//			}
			//			//Select Assignment Workflow
			//			if (!dataValues.get("Workflow").isEmpty()){
			//				Log.message("10. Select Assignment Workflow.");
			//				String documentWorkflow= selectDocumentWorkflow(dataValues.get("Workflow"),driver);
			//				
			//				if (documentWorkflow.isEmpty())
			//					throw new Exception("Could not select Assignment workflow.");
			//				Log.message("--Assignment Workflow has been selected.");
			//			}

			//enable 'checkin Immediately' checkbox
			isCheckInImmediately(driver);
			//Click 'Create' button on metadatacard
			clickCreateBtn();

			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='dialog-gradient']:not([style*='display: none;'])")));
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
		}
		catch(Exception e){
			if(e.getClass().toString().contains("NullPointerException")) 
				Log.exception(new Exception("Some data values are Null"),driver);
			else
				throw new Exception("Error while creating 'Assignment' Object.");
		}
		return new HomePage(driver);
	}

	/**
	 * Description: Create a 'Customer' Object
	 * @param driver
	 * @param dataValues
	 * @return HomePage
	 * @throws Exception 
	 */
	public HomePage createCustomerObject(WebDriver driver,Map<String,String> dataValues) throws Exception{

		try{

			WebElement metadataCard = driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
			driver.switchTo().frame(metadataCard);

			String customerClass=readMetadatacardPropertyValues("Class",dataValues.get("Class"));
			if(!customerClass.trim().equalsIgnoreCase(dataValues.get("Class"))) {
				throw new Exception("Could not select Customer Class.");
			}

			//Enter Customer title
			if (!dataValues.get("Title").isEmpty()){
				String customerTitle=enterObjectProperties("Customer name",dataValues.get("Title"));

				if (customerTitle.isEmpty()) {
					throw new Exception("Could not enter Customer Name");
				}
			}

			//			//Select Customer permissions
			//			if (!dataValues.get("Permission").isEmpty()){
			//				Log.message("8. Verify if Customer Permission is selected.");
			//				String documentPermissions=selectDocumentPermissions(dataValues.get("Permission"),driver);
			//				
			//				if (documentPermissions.isEmpty())
			//					throw new Exception("Could not select Customer Permissions.");
			//				Log.message("---Successfully selected the Customer Permissions");
			//			}

			//enable 'checkin Immediately' checkbox
			isCheckInImmediately(driver);
			//Click 'Create' button on metadatacard
			clickCreateBtn();
			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='dialog-gradient'][style*='display: block']")));
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();
		}
		catch(Exception e) {
			if(e.getClass().toString().contains("NullPointerException")) 
				Log.exception(new Exception("Some data values are Null"),driver);
			else
				throw new Exception("Error while creating 'Customer' object.");
		}
		return new HomePage(driver);
	}

	/**
	 * Description: Create a 'Contact Person' Object
	 * @param driver
	 * @param dataValues
	 * @return HomePage
	 * @throws Exception 
	 */
	public HomePage createContactPersonObject(WebDriver driver,Map<String,String> dataValues) throws Exception{
		try{

			WebElement metadataCard = driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
			driver.switchTo().frame(metadataCard);

			//Select Customer Class
			if (!dataValues.get("Class").isEmpty()){
				String documentClass=readMetadatacardPropertyValues("Class",dataValues.get("Class"));
				if (documentClass.isEmpty()&& !documentClass.trim().equalsIgnoreCase(dataValues.get("Class").trim()))
					throw new Exception("Could not select Customer Class.");

			}

			//Enter Customer title
			if (!dataValues.get("Title").isEmpty()){
				//String lastName= enterPropertyValue(driver,dataValues.get("Title"),"Last name");
				String lastName= enterObjectProperties("Last name",dataValues.get("Title"));
				if (lastName.isEmpty() && !lastName.trim().equalsIgnoreCase(dataValues.get("Title").trim()))
					throw new Exception("Could not enter ContactPerson LastName");
			}

			//Select Owner(Customer) Name
			if (!dataValues.get("OwnerOrCustomer").isEmpty()) {
				String ownerName=selectMetadataValuesFromList(driver,dataValues.get("OwnerOrCustomer"),"Owner (Customer)");

				if (ownerName.isEmpty()&& !ownerName.trim().equalsIgnoreCase(dataValues.get("OwnerOrCustomer").trim()))
					throw new Exception("Could not select 'Owner(Customer)'");
			}
			//			//Select Customer permissions
			//			if (!dataValues.get("Permission").isEmpty()){
			//				Log.message("8. Verify if Customer Permission is selected.");
			//				String documentPermissions=selectDocumentPermissions(dataValues.get("Permission"),driver);
			//				
			//				if (documentPermissions.isEmpty())
			//					throw new Exception("Could not select Customer Permissions.");
			//				
			//				Log.message("---Successfully selected the Customer Permissions");
			//			}
			//			
			//			//Select Customer Workflow
			//			if (!dataValues.get("Workflow").isEmpty()){
			//				Log.message("10. Select Customer Workflow.");
			//				String documentWorkflow= selectDocumentWorkflow(dataValues.get("Workflow"),driver);
			//				
			//				if (documentWorkflow.isEmpty())
			//					throw new Exception("Could not select Customer workflow.");
			//				
			//				Log.message("--Customer Workflow has been selected.");
			//			}

			//enable 'checkin Immediately' checkbox
			isCheckInImmediately(driver);

			//Click 'Create' button on metadatacard
			clickCreateBtn();

			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='dialog-gradient'][style*='display: block']")));
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();

		}
		catch(Exception e) {
			if(e.getClass().toString().contains("NullPointerException")) 
				Log.exception(new Exception("Some data values are Null"),driver);
			else
				throw new Exception("Error while creating 'Contact person' Object.");
		}
		return new HomePage(driver);
	}



	/**
	 * Description: Create a 'Document Collection' Object
	 * @param driver
	 * @param dataValues
	 * @return HomePage
	 * @throws Exception 
	 */

	public HomePage createDocumentCollectionObject(WebDriver driver,Map<String,String> dataValues) throws Exception{

		try{

			WebElement metadataCard = driver.findElement(By.cssSelector("iframe[src*='metadatacard.html.aspx']"));
			driver.switchTo().frame(metadataCard);

			//Select Document Collection Class
			if (!dataValues.get("Class").isEmpty()){
				String documentCollecClass=selectNewObjectClass(dataValues.get("Class"),driver);
				if (documentCollecClass.isEmpty()&&!documentCollecClass.trim().equalsIgnoreCase(dataValues.get("Class").trim()))
					throw new Exception("Could not select Document Collection Class.");

			}

			//Enter Document Collection title
			if (!dataValues.get("Title").isEmpty()){
				String documentTitle= enterObjectProperties("Name or title",dataValues.get("Title"));
				if (documentTitle.isEmpty())
					throw new Exception("Could not enter Document Collection title");
			}

			//			//Select Document Collection permissions
			//			if (!dataValues.get("Permission").isEmpty()){
			//				Log.message("8. Verify if document Collection Permission is selected.");
			//				String documentPermissions=selectDocumentPermissions(dataValues.get("Permission"),driver);
			//				if (documentPermissions.isEmpty())
			//					throw new Exception("Could not select Document Collection Permissions.");
			//				Log.message("---Successfully selected the Document Collection Permissions");
			//			}
			//			
			//			//Select Document Collection Workflow
			//			if (!dataValues.get("Workflow").isEmpty()){
			//				Log.message("10. Select Document Collection Workflow.");
			//				String documentWorkflow= selectDocumentWorkflow(dataValues.get("Workflow"),driver);
			//				if (documentWorkflow.isEmpty())
			//					throw new Exception("Could not select Document Collection workflow.");
			//				Log.message("--Document Collection Workflow has been selected.");
			//			}
			//			
			//enable 'checkin Immediately' checkbox
			isCheckInImmediately(driver);
			//Click 'Create' button on metadatacard
			clickCreateBtn();

			//Wait until Metadatacard dialog is closed 
			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='dialog-gradient'][style*='display: block']")));
			Utils.fluentWait(driver);
			driver.switchTo().defaultContent();

		}
		catch(Exception e) {
			if(e.getClass().toString().contains("NullPointerException")) 
				Log.exception(new Exception("Some data values are Null"),driver);
			else
				throw new Exception("Error while creating 'Document Collection' Object.");
		}
		return new HomePage(driver);
	}


	/**
	 * Description: Select Object 'Class' 
	 * @param className
	 * @param driver
	 * @return ClassName
	 * @throws Exception 
	 */
	public String selectObjectClass(String className,WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String selectedClass=null;

		try{

			driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
			WebElement selectDocClass=driver.findElement(By.cssSelector("select[class='doccard_class']"));
			Select docClass=new Select(selectDocClass);
			//get the selected classname
			selectedClass=docClass.getFirstSelectedOption().getText();
			if (!selectedClass.equalsIgnoreCase(className))
				throw new NoSuchElementException("Invalid Object class displayed");
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to select Object Class"),driver);
		}

		Log.event("Selected Document class is :"+selectedClass, StopWatch.elapsedTime(startTime));
		return selectedClass;
	}

	/**
	 * Description: Click 'Calendar' Object
	 * @throws Exception 
	 */
	public void clickCalendarObj() throws Exception
	{
		final long startTime = StopWatch.startTime();
		//click the 'Date' checkbox having current date
		ActionEventUtils.click(driver,docDatePickerObj);
		//	docDatePickerObj.click();
		new WebDriverWait(driver,60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf(docCalendar));
		Log.event("Clicked Calendar Object",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description: Select Object 'Date' in MetadataCard
	 * @param day
	 * @param month
	 * @param year
	 * @throws Exception 
	 */
	public void selectDateFromDocCard(String day,String month,String year) throws Exception{
		final long startTime = StopWatch.startTime();

		try{
			Select dMonth=new Select(selectMonth);
			Select dYear=new Select(selectYear);

			//Select the month&year from dropdown list
			dMonth.selectByVisibleText(month);
			dYear.selectByVisibleText(year);

			//click the 'date' link in calendar
			//driver.findElement(By.linkText(day)).click();
			ActionEventUtils.click(driver,driver.findElement(By.linkText(day)));

			if (!dateChecked.isDisplayed())
				throw new Exception("DocumentDate is not selected.");

		}catch(Exception e){
			e.printStackTrace();
		}
		Log.event("Selected Date from DatePicker Object",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description: Create Document as 'Template' 
	 * @throws Exception 
	 */
	public void chooseDocTemplate() throws Exception{
		final long startTime = StopWatch.startTime();
		try{
			WebElement rdoUseTemplate=driver.findElement(By.cssSelector("input[value='template']"));
			if (!rdoUseTemplate.isSelected()){
				//select the 'Template' radio button
				//rdoUseTemplate.click();
				ActionEventUtils.click(driver,rdoUseTemplate);
			}
		}
		catch(Exception e){
			throw new Exception("Could not find/select the 'Multiple File Document(MFD)' option");
		}
		Log.event("Document Template option choosed.",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description: Create Document as 'MFD'
	 * @throws Exception 
	 */
	public void chooseDocAsMFD() throws Exception
	{
		final long startTime = StopWatch.startTime();
		try{

			WebElement rdoMultiFileDoc=driver.findElement(By.cssSelector("input[value='mfd']"));
			if (!rdoMultiFileDoc.isSelected()){
				//select the 'multipleFile' radio button
				//	rdoMultiFileDoc.click();
				ActionEventUtils.click(driver,rdoMultiFileDoc);
			}
		}
		catch(Exception e){
			throw new Exception("Could not find/select the 'Multiple File Document(MFD)' option");
		}
		Log.event("Document type is selected as 'MFD'.",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description: Create Document as 'SFD'
	 * @throws Exception 
	 */
	public void chooseDocAsSFD() throws Exception
	{
		final long startTime = StopWatch.startTime();
		try{
			WebElement rdoSingleFileDoc=driver.findElement(By.cssSelector("input[value='sfd']"));
			if (!rdoSingleFileDoc.isSelected())
				//select the 'Single File' radio button
				//	rdoSingleFileDoc.click();
				ActionEventUtils.click(driver,rdoSingleFileDoc);
		}
		catch(Exception e){
			Log.exception(new Exception("Could not find/select the 'Single File Document(SFD)' option"),driver);
		}
		Log.event("Document type is selected as 'SFD'.",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description : select the 'Document Date' checkbox
	 * @throws Exception 
	 * 
	 */
	public void checkDocumentDate() throws Exception{
		final long startTime = StopWatch.startTime();
		try{

			WebElement dateCheckBox=driver.findElement(By.cssSelector("input[class='date_check']"));
			//select the 'Date' checkbox
			//dateCheckBox.click();
			ActionEventUtils.click(driver,dateCheckBox);
		}
		catch(Exception e){
			Log.exception(new Exception("Could not find/select the 'Document Date' checkbox"),driver);
		}
		Log.event("The'Document Date' checkbox is selected.",StopWatch.elapsedTime(startTime));
	}

	/**
	 * Description: Select Document 'Template'
	 * @param templateName
	 * @return templateName
	 * @throws Exception 
	 */
	public String selectDocumentTemplate(String templateName) throws Exception{
		final long startTime = StopWatch.startTime();
		//	new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("input[value='template']")))).click();
		ActionEventUtils.click(driver,new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("input[value='template']")))));

		Select docTemplate=new Select(selectDocTemplate);
		int classListItemCount=docTemplate.getOptions().size();
		Log.event("Template List in the Dropdown :"+classListItemCount,StopWatch.elapsedTime(startTime));

		//get the list templates from the dropdown list
		List <WebElement> options = 
				selectDocTemplate.findElements(By.tagName("option")); 

		for(WebElement option : options){ 
			if (option.getText().equals(templateName)){ 
				//Select the 'Template' from dropdownlist
				option.click(); 
				ActionEventUtils.click(driver,option);
				break; 
			} 
		} 

		String selectedTemplate=docTemplate.getFirstSelectedOption().getText();

		if (!selectedTemplate.equalsIgnoreCase(templateName))
			throw new Exception("Could not select Template. ");

		Log.event("Template selected for the Document :"+selectedTemplate,StopWatch.elapsedTime(startTime));
		return selectedTemplate;
	}

	/**
	 * Description: Select Document 'Permissions'
	 * @param Permissions
	 * @param driver
	 * @return permission
	 * @throws Exception 
	 */
	public String selectDocumentPermissions(String Permissions,WebDriver driver) throws Exception{
		final long startTime = StopWatch.startTime();
		String selectedPermission=null;

		try{

			WebElement selectDocPermission=driver.findElement(By.cssSelector("select[class='doccard_permissions']"));


			Select docPermission=new Select(selectDocPermission);
			//Select the object permissions
			docPermission.selectByVisibleText(Permissions);

			selectedPermission=docPermission.getFirstSelectedOption().getText();
			//Verify if selected permissions correctly
			if (!selectedPermission.equalsIgnoreCase(Permissions))
				throw new Exception("Could not select Permission");
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to select Document Permissions"),driver);
		}

		Log.event("Permissions selected for the Document :"+selectedPermission,StopWatch.elapsedTime(startTime));
		return selectedPermission;
	}

	/**
	 * Description: Select Document 'Workflow'
	 * @param workflowName
	 * @return workflow
	 * @throws Exception 
	 */
	public String selectDocumentWorkflow(String workflowName,WebDriver driver) throws Exception{
		final long startTime = StopWatch.startTime();
		String selectedWorkflow=null;
		try{

			WebElement selectDocWF=driver.findElement(By.cssSelector("select[class='doccard_wf']"));
			Select docWrokflow=new Select(selectDocWF);

			//fetch the list of workflows available in dropdown list
			List <WebElement> options = selectDocWF.findElements(By.tagName("option")); 

			for(WebElement option : options) 
			{ 
				if (option.getText().trim().equalsIgnoreCase(workflowName))
				{ 		//Select the specified Workflow
					ActionEventUtils.click(driver,option);
					//option.click(); 
					break; 
				} 
			} 

			selectedWorkflow=docWrokflow.getFirstSelectedOption().getText();
			//verify if workflow selected correctly
			if (!selectedWorkflow.equalsIgnoreCase(workflowName))
				throw new Exception("Could not select Workflow");
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try {
			Log.event("Workflow selected for the Document :"+selectedWorkflow,StopWatch.elapsedTime(startTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return selectedWorkflow;
	}

	/**
	 * Description: Select Document 'Workflow'
	 * @param workflowName
	 * @return workflow
	 * @throws Exception 
	 */
	public String selectWorkflowState(String workflowName,WebDriver driver) throws Exception{
		final long startTime = StopWatch.startTime();
		String selectedWorkflow=null;
		try{

			WebElement selectDocWF=driver.findElement(By.cssSelector("select[class='doccard_wf']"));
			Select docWrokflow=new Select(selectDocWF);

			//fetch the list of workflows available in dropdown list
			List <WebElement> options = selectDocWF.findElements(By.tagName("option")); 

			for(WebElement option : options) 
			{ 
				if (option.getText().trim().equalsIgnoreCase(workflowName))
				{ 		//Select the specified Workflow
					ActionEventUtils.click(driver,option);
					//option.click(); 
					break; 
				} 
			} 

			selectedWorkflow=docWrokflow.getFirstSelectedOption().getText();
			//verify if workflow selected correctly
			if (!selectedWorkflow.equalsIgnoreCase(workflowName))
				throw new Exception("Could not select Workflow");
		}
		catch(Exception e){
			e.printStackTrace();
		}

		try {
			Log.event("Workflow selected for the Document :"+selectedWorkflow,StopWatch.elapsedTime(startTime));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return selectedWorkflow;
	}

	/**
	 * Click the OK button in Upload file dialog box
	 * @throws Exception
	 */
	public void clickOkOnUploadDialog() throws Exception
	{
		try{
			WebElement uploadOkBtn=driver.findElement(By.cssSelector("button[class*='window_ok'][style='display: inline-block;']"));
			ActionEventUtils.click(driver,uploadOkBtn);
			//uploadOkBtn.click();
		}
		catch(Exception e) {
			Log.exception(new Exception("Unable to click 'Ok' button on Upload Popup dialog."),driver);
		}

	}

	/**
	 * Description: Click 'Upload' button of Document Object
	 * @param driver
	 * @throws Exception 
	 */
	public void clickUploadBtn(String fileLocation) throws Exception{
		try{

			//	WebElement fileUpload1 =driver.findElement(By.cssSelector("input[id='file']"));
			WebElement fileUpload1 =driver.findElement(By.cssSelector("button[id='btPickFile']"));
			fileUpload1.sendKeys(fileLocation);

		}
		catch(Exception e){
			Log.exception(new Exception("Unable to click 'Upload' button"),driver);
		}
	}

	/**
	 * Description: Click 'Ok' button
	 * @param driver
	 * @throws Exception 
	 */
	public void clickOKBtn(WebDriver driver) throws Exception{

		final long startTime = StopWatch.startTime();
		try{

			this.switchFrame();
			WebElement OKBtn= this.driver.findElement(By.cssSelector("button[class*='mf-save-button ui-button-primary ui-button']"));
			//click Ok Button
			if(OKBtn.isDisplayed())
				ActionEventUtils.click(driver,OKBtn);
			//OKBtn.click();

		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.clickOKBtn : "+e);
		}
		finally {

			this.switchToDefaultContent();
			Log.event("OK button clicked on the MetadataCard dialog.",StopWatch.elapsedTime(startTime));
		}
	}

	/**
	 * Description: Click 'Cancel' button of MetadataCard
	 * @param driver
	 * @throws Exception 
	 */
	public void clickCancelBtn() throws Exception{
		final long startTime = StopWatch.startTime();
		try {
			this.switchFrame();
			WebElement btnCancel=driver.findElement(By.cssSelector("button[class='window_cancel'],button[class*='mf-discard-button'][role='button'][class*='ui-button-primary']"));
			//click Cancel button
			ActionEventUtils.click(driver,btnCancel);
			//btnCancel.click();
			Utils.fluentWait(driver);

		}
		catch(Exception e) 
		{
			throw new Exception("Exception at MetadataCard.clickCancelBtn : "+e);
		}
		finally {

			this.switchToDefaultContent();
			Log.event("Cancel button clicked on the MetadataCard dialog.",StopWatch.elapsedTime(startTime));
		}
	}

	/**
	 * Description: Enter Object title in MetadataCard
	 * @param name
	 * @param driver
	 * @return Object Title
	 * @throws Exception 
	 */
	public String enterObjectTitle(String name,WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String documentTitle=null;
		try {

			WebElement documentName=driver.findElement(By.cssSelector("div[class='common_control']>input"));
			//Enter object title
			documentName.sendKeys(name);
			documentTitle=documentName.getAttribute("value");
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Unable to enter Object Title"), driver);
		}
		Log.event("Document Title is entered as :"+documentTitle,StopWatch.elapsedTime(startTime));
		return documentTitle;
	}

	/**
	 * Description: Click 'CheckInImmediately' checkbox from MetadataCard
	 * @param driver
	 * @throws Exception 
	 */
	public void isCheckInImmediately(WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		try {
			WebElement checkinChkBox=driver.findElement(By.cssSelector("div[id='mf-checkinimmediately']>input[id='mf-checkinimmediately-checkbox']"));

			//click the CheckInImmediately checkbox
			if (!checkinChkBox.isSelected())
				ActionEventUtils.click(driver,checkinChkBox);
			//checkinChkBox.click();
		}
		catch(Exception e) {
			Log.exception(new Exception("Unable to select 'CheckIn Immediately' checkbox "),driver);
		}

		Log.event("CheckIn Immediately is checked.", StopWatch.elapsedTime(startTime));
	}

	public String enterPropertyValue(WebDriver driver,String objValue,String objProperty) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String propertyValueSelected=null;
		try
		{

			WebElement tableElement=driver.findElement(By.cssSelector("table[class='doccard_middle']"));
			List<WebElement> list= tableElement.findElements(By.cssSelector("tr[class='property']"));
			//navigate through each property in metadatacard and enter the Property value
			for(int i=0;i<list.size();i++)
			{
				WebElement propertyName=list.get(i).findElement(By.cssSelector("td[class='property_name']"));

				System.out.println(propertyName.getText());
				if (propertyName.getText().trim().contains(objProperty.trim())){
					try{
						list.get(i).findElement(By.cssSelector("div[class='common_control']>input")).sendKeys(objValue);
						Thread.sleep(500);
						propertyValueSelected=list.get(i).findElement(By.cssSelector("div[class='common_control']>input")).getAttribute("value");
					}
					catch(NoSuchElementException |InvalidElementStateException e){
						Log.exception(new Exception("Unable to find '"+objProperty+"' field."),driver);
					}

					break;
				}
			}
			Log.event(objProperty+" Property value selected as :"+propertyValueSelected.toString(), StopWatch.elapsedTime(startTime));
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to enter '"+objProperty+"' value."), driver);
		}

		return propertyValueSelected;
	}

	/**
	 * Description: Enter Property Values in MetadataCard
	 * @param driver
	 * @param objValue
	 * @param objProperty
	 * @return Property Value
	 * @throws Exception 
	 */
	public String getPropertyValue(WebDriver driver,String objValue,String objProperty) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String propertyValueSelected=null;
		try
		{

			WebElement tableElement=driver.findElement(By.cssSelector("table[class='doccard_middle']"));
			List<WebElement> list= tableElement.findElements(By.cssSelector("tr[class='property']"));
			//navigate through each property in metadatacard and enter the Property value
			for(int i=0;i<list.size();i++)
			{
				WebElement propertyName=list.get(i).findElement(By.cssSelector("td[class='property_name']"));

				if (propertyName.getText().trim().contains(objProperty.trim())){
					try{
						propertyValueSelected=list.get(i).findElement(By.cssSelector("div[class='common_control']>input")).getAttribute("value");
					}
					catch(NoSuchElementException | InvalidElementStateException e){
						Log.exception(new Exception("Unable to find '"+objProperty+"' field."),driver);
					}
					break;
				}
			}
			Log.event(objProperty+" Property value selected as :"+propertyValueSelected.toString(), StopWatch.elapsedTime(startTime));
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to enter '"+objProperty+"' value."), driver);
		}

		return propertyValueSelected;
	}

	/**
	 * Description: Select 'Property' value from dropdown list of MetadataCard
	 * @param driver
	 * @param objValue
	 * @param objProperty
	 * @return selected 'ObjValue'
	 * @throws Exception 
	 */
	public String selectMetadataValuesFromList(WebDriver driver,String objValue,String objProperty) throws Exception{
		final long startTime = StopWatch.startTime();
		String propertyValueSelected=null;
		WebElement propertyName=null;
		try{
			List<WebElement> list=driver.findElements(By.cssSelector("table[id='mf-property-table']>tbody>tr"));
			for(int i=0;i<list.size();i++)
			{
				propertyName=list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-namefield']"));
				if (propertyName.getText().contains(objProperty)) {
					try{
						//Click the property field, if not enabled 
						if (propertyName.getText().contains("Is supervisor") || propertyName.getText().contains("Owner (Customer)")) {
							//list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).click();
							ActionEventUtils.click(driver,list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")));
							//list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")).click();
							ActionEventUtils.click(driver,list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")));
						}
						else if (!list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")).isDisplayed()) {
							ActionEventUtils.click(driver,list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")));
							//	list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).click();
							ActionEventUtils.click(driver,list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")));
							//list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")).click();
						}
						else {
							//	list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")).click();
							ActionEventUtils.click(driver,list.get(i).findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span")));
						}

						//Select the Property Value from dropdown list

						List<WebElement> propValues=driver.findElements(By.cssSelector("li[class='ui-menu-item']"));
						for(int k=0;k<propValues.size();k++) {
							if (propertyName.getText().contains("Is supervisor") || propertyName.getText().contains("Owner (Customer)")) {
								String propertyValue=propValues.get(k).findElement(By.cssSelector("a")).getText();
								if(propertyValue.trim().equalsIgnoreCase(objValue.trim())) {
									ActionEventUtils.click(driver,propValues.get(k).findElement(By.cssSelector("a")));
									//propValues.get(k).findElement(By.cssSelector("a")).click();
									break;
								}
							}
							else {
								String propertyValue=propValues.get(k).findElement(By.cssSelector("a>span")).getText();
								if(propertyValue.trim().equalsIgnoreCase(objValue.trim())) {
									propValues.get(k).findElement(By.cssSelector("a>span")).click();
									ActionEventUtils.click(driver,propValues.get(k).findElement(By.cssSelector("a>span")));
									break;
								}
							}
						}
						Thread.sleep(200);
						propertyValueSelected=list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']>input")).getAttribute("value");
						list.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']>input")).sendKeys(Keys.RETURN);
						break;
					}
					catch(NoSuchElementException e){
						throw new Exception(objProperty+" Could not be Found in the Dropdownlist.");
					}
				}

			}
			Log.event(objProperty+" Property value selected as :"+propertyValueSelected.toString(), StopWatch.elapsedTime(startTime));
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to select the Property values from Metadatacard dropdown list."),driver);
		}
		return propertyValueSelected;
	}

	/**
	 * Description: Select Object 'Status' from MetadataCard
	 * @param driver
	 * @param objectName
	 * @param status
	 * @return
	 * @throws Exception 
	 */
	public String selectObjectStatus(WebDriver driver,String objectName,String status) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String selectedStatus=null;

		try{

			WebElement projectStatus=driver.findElement(By.cssSelector("select[class='check']"));
			Select projStatus=new Select(projectStatus);

			//Select Object status
			projStatus.selectByVisibleText(status);
			selectedStatus=projStatus.getFirstSelectedOption().getText();

			//Verify if status selected
			if (!selectedStatus.trim().equalsIgnoreCase(status.trim()))
				throw new NoSuchElementException("Could not select Project Status");
			Log.event("Project Status is selected as"+selectedStatus,StopWatch.elapsedTime(startTime));

		}
		catch(Exception e)
		{
			Log.exception(new Exception("Unable to select Object Status."),driver);
		}

		Log.event("Selected "+objectName+" Status as :"+selectedStatus, StopWatch.elapsedTime(startTime));
		return selectedStatus;
	}

	/**
	 * Description: Verify if New Object creation dialog displayed
	 * @param objName
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isObjectCreationdialogDisplayed(String objName) throws Exception {

		final long startTime = StopWatch.startTime();

		try{
			WebElement objCreationDialog=driver.findElement(By.cssSelector("div[class*='ui-draggable']>div[class*='ui-dialog-titlebar']>span"));
			if (objCreationDialog.isDisplayed()&&objCreationDialog.getText().toLowerCase().trim().contains(objName.toLowerCase().trim())){
				Log.event("New '"+objName+"' creation dialog successfully displayed.", StopWatch.elapsedTime(startTime));
				return true;
			}			
		}catch(NoSuchElementException e){
			Log.exception(new Exception(),driver);
		}
		Log.event("Unable to launch New '"+objName+"' creation dialog.", StopWatch.elapsedTime(startTime));
		return false;
	}

	/**
	 * Description: Fetch the Object PropertyIndex
	 * @param propertyName
	 * @return propertyIndex
	 * @throws Exception
	 */
	public int getPropertyNameIndex(String propertyName) throws Exception{
		//local variables
		int propIndex=0;
		try{

			List<WebElement> props=metadataTabPropertyNames.findElements(By.cssSelector("div>span[calss*='label']"));
			for(int i=0;i<props.size();i++)
			{
				if (props.get(i).getAttribute("innerText").trim().equalsIgnoreCase(propertyName.trim())){
					propIndex=i;
					return propIndex;
				}
			}

		}catch(Exception e){
			Log.exception(new Exception("Some error while getting the Index of PropertyName '"+propertyName+"'"),driver);
		}
		return propIndex;
	}

	/**
	 * 
	 * @param className
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public String selectNewObjectTemplateClass(String className,WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String selectedTemplateClass=null;

		try {

			//Select the template class
			WebElement metadataTable=driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody>tr"));
			WebElement arrowClass=metadataTable.findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span"));
			ActionEventUtils.click(driver,arrowClass);
			//arrowClass.click();


			//			WebElement classList=driver.findElement(By.cssSelector("ul[id*='ui-id'][style*='display: block']"));

			List<WebElement> classes=driver.findElements(By.cssSelector("ul[id*='ui-id'][style*='display: block']>li,li"));

			for(WebElement classTitle:classes)
			{
				if (classTitle.getText().equalsIgnoreCase(className))
				{
					WebElement template=classTitle.findElement(By.cssSelector("a>span"));
					Actions action=new Actions(driver);
					action.click(template).sendKeys(Keys.TAB).build().perform();
					break;
				}
			}

			WebElement lblClass=driver.findElement(By.cssSelector("div[class='mf-listing-content']>ul>li[class='mf-separator-item mf-separator-other-templates']>div[class='mf-separator-text'],div[class='mf-listing-content']>ul>li[class='mf-separator-item mf-separator-default-templates']>div"));
			if ((!lblClass.getText().isEmpty()) || (!lblClass.getAttribute("innerText").isEmpty()))
			{	
				if(lblClass.getText().isEmpty())
					selectedTemplateClass=lblClass.getAttribute("innerText").split("-")[1].trim();
				else
					selectedTemplateClass=lblClass.getText().split("-")[1].trim();

			}
		}
		catch(Exception e) 
		{
			Log.exception(new Exception("Some problem in selecting the Object Class"),driver);
		}
		Log.event("Template Class is selected as :"+selectedTemplateClass, StopWatch.elapsedTime(startTime));
		return selectedTemplateClass;
	}

	/**
	 * 
	 * @param className
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public String selectNewObjectClass(String className,WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String selectedClass=null;

		try {

			WebElement metadataTable=driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody>tr"));
			WebElement arrowClass=metadataTable.findElement(By.cssSelector("div[class='mf-lookup-item-row']>div[class='mf-lookup-item-cell-image']>span"));
			ActionEventUtils.click(driver,arrowClass);
			//arrowClass.click();

			//			WebElement classList=driver.findElement(By.cssSelector("ul[id*='ui-id'][style*='display: block']"));
			Thread.sleep(500);

			//			List<WebElement> classes=classList.findElements(By.cssSelector("li"));
			List<WebElement> classes=driver.findElements(By.cssSelector("ul[id*='ui-id'][style*='display: block']>li,li"));
			for(WebElement classTitle:classes)
			{
				if (classTitle.getText().equalsIgnoreCase(className))
				{
					Actions action=new Actions(driver);
					action.click(classTitle).sendKeys(Keys.TAB).build().perform();
					break;
				}
			}

			WebElement lblClass=driver.findElement(By.cssSelector("div[class='mf-lookup-item-cell-content']>span[class*='mf-internal-text']"));
			Log.message("1. lblClass :"+lblClass.getText());
			if (lblClass.getText().isEmpty())
				Log.exception(new Exception("Object Class not selected"),driver);
			else
				//				selectedClass=lblClass.getAttribute("innerText").trim();
				selectedClass=lblClass.getText().trim();
		}
		catch(Exception e) 
		{
			Log.exception(new Exception("Some problem in selecting the Object Class"),driver);
		}
		Log.event("Class is selected as :"+selectedClass, StopWatch.elapsedTime(startTime));
		return selectedClass;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void clickCreateBtn() throws Exception
	{
		final long startTime = StopWatch.startTime();
		try
		{
			this.switchFrame();
			WebElement createBtn=driver.findElement(By.cssSelector("div[id='mf-footer']>div[id='mf-buttons']>button[class*='mf-save-button']>span[class='ui-button-text']"));
			ActionEventUtils.click(driver,createBtn);
			//createBtn.click();
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='ui-draggable']:not([style*='display: none;'])")));

			Utils.fluentWait(driver);
		}
		catch(Exception e)
		{
			throw new Exception("Exception at MetadataCard.clickCreateBtn : "+e);
		}

		finally{

			this.switchToDefaultContent();
			Log.event("'Create' button clicked on MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}
	}

	public String enterObjectProperties(String propName,String propValue) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String propertyEntered=null;
		try
		{


			List<WebElement> propertyList=driver.findElements(By.cssSelector("table[id='mf-property-table']>tbody>tr"));
			for(int i=0;i<propertyList.size();i++)
			{
				WebElement properties=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-namefield']"));
				System.out.println(properties.getText());
				if (properties.getText().trim().contains(propName))
				{
					if(!propName.trim().equalsIgnoreCase("Assignment description")) {
						try {
							propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input")).isDisplayed();
						}
						catch(NoSuchElementException e)
						{
							ActionEventUtils.click(driver,propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class*='mf-internal-text']")));
						}
						catch (Exception e) {
							throw new Exception("Exception at MetadataCard.enterObjectProperties :" + e.getMessage(), e);
						}
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input[class*='mf-internal-text']")).clear();
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input[class*='mf-internal-text']")).sendKeys(propValue);
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input[class*='mf-internal-text']")).sendKeys(Keys.TAB);

						try {
							if(propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).getText().isEmpty())
								propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).getAttribute("innerText");
							else
								propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).getText();
						}
						catch(NullPointerException e)
						{
							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div")).getAttribute("innerText");
						}
						break;
					}//if loop...
					else {
						try {
							propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div>textarea")).isDisplayed();
						}
						catch(NoSuchElementException e)
						{
							ActionEventUtils.click(driver,propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div[class='mf-control-container']")));
						}
						catch (Exception e) {
							throw new Exception("Exception at MetadataCard.enterObjectProperties :" + e.getMessage(), e);
						}
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div>textarea")).clear();
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div>textarea")).sendKeys(propValue);
						propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div>textarea")).sendKeys(Keys.TAB);

						try {
							if(propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div")).getAttribute("innerText").isEmpty())
								propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div")).getText();
							else
								propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div")).getAttribute("innerText");
						}
						catch(NullPointerException e)
						{
							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield noeclip']>div>div>div")).getText();
						}
						break;
					}//else
				}//if loop..
			}//for loop...
			Thread.sleep(200);
			Log.event("Property Value entered as :'"+propertyEntered, StopWatch.elapsedTime(startTime));
		}
		catch(Exception e)
		{
			if(e.getClass().toString().contains("NullPointerException"))
				Log.exception(new Exception("Unable to enter object property '"+propName+"' value"),driver);
			else
				Log.exception(new Exception("Some problem while creating the 'Assignment' object property '"+propName+"' value"),driver);
		}
		return propertyEntered;
	}

	/**
	 * 
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public String selectObjectTemplate(String template) throws Exception {

		final long startTime = StopWatch.startTime();

		String templateSelected=null;

		try {
			this.switchFrame();
			List<WebElement> templateList=driver.findElements(By.cssSelector("div[class='mf-content']>div[class='mf-listing-content']>ul>li[class*='ui-widget mf-file-item']>div"));

			for(int i=0;i<templateList.size();i++)
			{
				if (templateList.get(i).getText().trim().equalsIgnoreCase(template))
				{
					templateSelected=templateList.get(i).getText();
					ActionEventUtils.click(driver,templateList.get(i));
					//templateList.get(i).click();
					break;
				}
			}

		}

		catch(Exception e)
		{
			Log.exception(new Exception("Unable to select the Document template :'"+templateSelected+"'"),driver);
		}
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.selectObjectTemplate : ", StopWatch.elapsedTime(startTime));
		} //End Finally

		return templateSelected;
	}

	/**
	 * 
	 * @param templateType
	 * @throws Exception
	 */
	public void filterObjectTemplates(String templateType) throws Exception {

		final long startTime = StopWatch.startTime();

		try
		{
			this.switchFrame();
			List<WebElement> filters=driver.findElements(By.cssSelector("ul[class='mf-filter-buttons']>li"));
			for(WebElement template:filters)
			{
				if (template.getText().trim().equalsIgnoreCase(templateType))
				{
					ActionEventUtils.click(driver,template);
					//template.click();
					break;
				}
			}
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Unable to click 'template' filters"),driver);
		}

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.filterObjectTemplates : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}

	public String readMetadatacardPropertyValues(String propertyName,String propertyValue) throws Exception
	{
		final long startTime = StopWatch.startTime();
		String propertyEntered=null;
		try
		{
			List<WebElement> propertyList=driver.findElements(By.cssSelector("table[id='mf-property-table']>tbody>tr"));
			for(int i=0;i<propertyList.size();i++)
			{
				WebElement properties=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-namefield']"));
				System.out.print(properties.getText());
				if (properties.getText().trim().contains(propertyName))
				{

					try {
						if(propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']")).getText().isEmpty())
							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']")).getAttribute("innerText");
						else
							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']")).getText();
					}
					catch(Exception e)
					{
						propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div[class='mf-internal-lookup']>div>div>div[class='mf-lookup-item-cell-content']")).getAttribute("innerText");
					}
					//					try {
					//						if(propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input")).getText().isEmpty())
					//							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input")).getAttribute("innerText");
					//						else
					//							propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input")).getText();
					//					}
					//					catch(NullPointerException e)
					//					{
					//						propertyEntered=propertyList.get(i).findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>div>div>div>input")).getAttribute("innerText");
					//					}
					if (propertyEntered.trim().equalsIgnoreCase(propertyValue.trim())) {
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Unable to get the 'PropertyValue'."),driver);
		}
		Log.event("Property Value entered as :'"+propertyEntered+"'", StopWatch.elapsedTime(startTime));
		return propertyEntered;
	}


	//-----------------------------------------END function required for smoke test cases END---------------------//	


	/**
	 * isPropertyDescriptionDisplayed : Verifies if PropertyDescription is displayed in the metadatacard 
	 * 
	 * @param propertyname
	 * @return
	 * @throws Exception
	 */
	public boolean isPropertyDescriptionDisplayed(String propertyname) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			else
				return false;

			WebElement propDescription = this.driver.findElement(By.cssSelector("tbody>tr[class*='mf-description-row'][style='display: table-row;']"));
			System.out.println(propDescription.getText());

			if (propDescription.isDisplayed()) 
				return true;
			else 
				return false;

		}//End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.isPropertyDescriptionDisplayed :" + e.getMessage(), e);
		}//End catch

		finally {
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}

			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropertyDescriptionDisplayed : ", StopWatch.elapsedTime(startTime));
		}//End finally

	}//isPropertyDescriptionDisplayed	

	/**
	 * getPropertyDescriptionValue : Selected property is displayed with particular 'Description' Text
	 * 
	 * @param propertyname
	 * @return
	 * @throws Exception
	 */
	public String getPropertyDescriptionValue(String propertyname) throws Exception {


		final long startTime = StopWatch.startTime();


		try {
			this.switchFrame();

			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			else
				return "";

			WebElement propDescription = this.driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table[class='mf-dynamic-table']>tbody>tr[class*='mf-description-row'][style='display: table-row;']>td>span[class*='mf-property-description']"));

			String propValue = propDescription.getText().trim();//Gets the property description value

			if (propDescription.isDisplayed()) 
				return propValue;//Returns the property description value
			else 
				return "";//Returns the empty value
		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.getPropertyDescriptionValue :" + e.getMessage(), e);
		}//End catch

		finally {
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}

			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyDescriptionValue : ", StopWatch.elapsedTime(startTime));
		}//End finally

	}//getPropertyDescriptionValue  

	/**
	 * clickMetadataDescriptionLink : Click the link from the metadatacard description
	 * @param linkText: String which needs to be clicked
	 * @return Clicks the metadatacard description link text
	 * @throws Exception
	 */
	public void clickMetadataDescriptionLink(String linkText) throws Exception {

		final long startTime = StopWatch.startTime();
		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			this.switchFrame();

			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description'] div[id*='mf-metadatacard-description']"));
			List<WebElement> links = metadataDescription.findElement(By.xpath("..")).findElements(By.cssSelector("a"));

			for(int count = 0; count < links.size(); count++)//Iterate through the each links in the metadatacard description
				if(links.get(count).getText().trim().equalsIgnoreCase(linkText))//Checks the link text to click
				{
					//links.get(count).click();//Clicks the link in the metadatacard
					if(browser.equalsIgnoreCase("ie"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()", links.get(count));
					else
						ActionEventUtils.click(driver,links.get(count));
					break;
				}

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.clickMetadataDescriptionLink :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickMetadataDescriptionLink : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}

	/**
	 * getMetadataDescriptionLinks : Get the available links text from the metadatacard description
	 * @return Returns the metadatacard description link text
	 * @throws Exception
	 */
	public ArrayList<String> getMetadataDescriptionLinks() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();

			ArrayList<String> metadataDescLinks = new ArrayList<String>();

			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description'] div[id*='mf-metadatacard-description']"));
			List<WebElement> links = metadataDescription.findElement(By.xpath("..")).findElements(By.cssSelector("a"));

			for(int count = 0; count < links.size(); count++)
				metadataDescLinks.add(links.get(count).getText());//Adds the link text into the array

			return metadataDescLinks; //Return the links text

		}//End try
		catch(NoSuchElementException e)
		{
			return null;
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.getMetadataDescriptionLinks :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataDescriptionLinks : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}


	/**
	 * This function is used to get the text colour for the Property description
	 * @return metadataDescription text colour
	 * @throws Exception 
	 */
	public String getPropertyDescriptionTextColour(String propertyname) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			else
				return "";

			WebElement propDescription = this.driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table[class='mf-dynamic-table']>tbody>tr[class*='mf-description-row'][style='display: table-row;']>td>span[class*='mf-property-description']"));

			if (propDescription.isDisplayed())
				return propDescription.getCssValue("color");
			else
				return "";

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.getPropertyDescriptionTextColour :" + e.getMessage(), e);
		}//End catch

		finally {
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyDescriptionTextColour : ", StopWatch.elapsedTime(startTime));
		}//End finally
	}//getPropertyDescriptionTextColour


	/**
	 * getPropertyDescriptionBGColor : Gets the Background color of the Property description in the metadatacard
	 * 
	 * @param propertyname
	 * @return
	 * @throws Exception
	 */
	public String getPropertyDescriptionBGColor(String propertyname) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			else
				return "";

			WebElement propDescription = this.driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table[class='mf-dynamic-table']>tbody>tr[class*='mf-description-row'][style='display: table-row;']>td>span[class*='mf-property-description']"));

			if (propDescription.isDisplayed())//Checks if Property description is displayed in the metadatacard 
				return propDescription.getCssValue("background-color");//Returns the background color of the property description
			else 
				return "";
		}//End try

		catch(NoSuchElementException e)
		{
			return "";
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.getPropertyDescriptionBGColor :" + e.getMessage(), e);
		}//End catch

		finally {
			if (this.isPropertyExists(propertyname)) {
				this.clickProp(propertyname);
				Utils.fluentWait(driver);
			}
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyDescriptionBGColor : ", StopWatch.elapsedTime(startTime));
		}//End finally

	}//getPropertyDescriptionValue

	/**
	 * getPropertyDescriptionLink : Get the link text from the corresponding metadata card property
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getPropertyDescriptionLink () throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement propDescription = driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody>tr[class='mf-description-row']"));
			WebElement description = propDescription.findElement(By.cssSelector("td[class='mf-dynamic-controlfield']>span[class*='mf-property-description']>a"));

			if (description.isDisplayed()) {
				String link = description.getText();
				System.out.println(link);
				return link; //Return the link text
			}
			else 
				return "";//Return the null

		}//End try
		catch (Exception e) {
			throw new Exception("Unable to get the description link",e);
		}//End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyDescriptionLink : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	public void getMetadataImageSize() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement imageSize = driver.findElement(By.cssSelector("div[class='left-side']>div[class ='mf-description-image-container']"));
			imageSize.getSize();

		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getMetadataImageSize :" + e.getMessage(), e);
		}
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataImageSize : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}

	/**
	 *  This method is used to get the back ground colour for group header
	 *  
	 * @return groupHeaderColour
	 * @throws Exception 
	 */
	public String getGroupHeaderColour() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement groupHeader = driver.findElement(By.cssSelector("div[class='mf-dynamic-properties']>table"));
			WebElement groupHeaderColour = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-1']>tr[id='propertygroup-title-1']"));
			return groupHeaderColour.getCssValue("background-color");

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getGroupHeaderColour :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getGroupHeaderColour : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getGroupHeaderColour

	/**
	 * This function is used to get the group text colour
	 * @return groupHeaderColour
	 * @throws Exception 
	 */
	public String getGroupTextColour() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement groupHeader = driver.findElement(By.cssSelector("div[class='mf-dynamic-properties']>table"));
			WebElement groupHeaderColour = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-1']>tr[id='propertygroup-title-1']>th"));
			return groupHeaderColour.getCssValue("color").trim();

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getGroupTextColour :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getGroupTextColour : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getGroupTextColour

	/**getGroupText : This function is used to get the property group text in the metadatacard
	 * @param group
	 * @return Group text
	 * @throws Exception 
	 */
	public String getPropertyGroupText(int group) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			this.switchFrame();

			WebElement groupHeader = driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table"));
			WebElement groupHeaderColour = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-" + group + "']>tr[id='propertygroup-title-"+ group +"']>th"));
			return groupHeaderColour.getText().trim();

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getGroupText :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getGroupText : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getGroupText

	/**getGroupText : This function is used to get the property group text in the metadatacard
	 * @param group
	 * @return Group text
	 * @throws Exception 
	 */
	private String getPropGroupText(int group) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			WebElement groupHeader = driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table"));
			WebElement groupHeaderText = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-" + group + "']>tr[id='propertygroup-title-"+ group +"']>th"));

			if (groupHeaderText.isDisplayed())
				return groupHeaderText.getText().trim();

			return "";

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getGroupText :" + e.getMessage(), e);
		}//End catch
		finally {
			Log.event("MetadataCard.getGroupText : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getGroupText

	/**isPropertyGroupDisplayed: This function is used to check if group is displayed in the metadatacard or not
	 *  @param group : Group ID
	 *  @param groupName: Group text
	 *  @return boolean: true if group is displayed in the metadatacard else false
	 * @throws Exception 
	 */
	public boolean isPropertyGroupDisplayed(int group, String groupName) throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();			
			String groupTxt = getPropGroupText(group);//Gets the group text in the metadatacard

			if (groupTxt.equalsIgnoreCase(groupName.trim()))//Checks if group text is same
				return true;//Returns group is displayed in the metadatacard

			return false;//Returns group is not displayed in the metadatacard

		}//End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.isPropertyGroupDisplayed :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropertyGroupDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//isPropertyGroupDisplayed

	/**
	 * This function is used to get the background colour for the 
	 * @return 
	 * @throws Exception 
	 */
	public String getMetadataDescriptionColour() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description']"));
			System.out.println(metadataDescription.getCssValue("background-color"));
			return metadataDescription.getCssValue("background-color").trim();

		}
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getMetadataDescriptionColour :" + e.getMessage(), e);
		}
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataDescriptionColour : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getMetadataDescriptionColour

	/**
	 * This function is used to get the text colour for the Metadatadescription
	 * @return metadataDescription text colour
	 * @throws Exception 
	 */
	public String getMetadataDescriptionTextColour() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description']"));
			System.out.println(metadataDescription.getCssValue("color"));
			return metadataDescription.getCssValue("color");

		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getMetadataDescriptionTextColour :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataDescriptionTextColour : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getMetadataDescriptionColour

	/**
	 * This function is used to get the text colour for the Metadatadescription
	 * @return metadataDescription text colour
	 * @throws Exception 
	 */
	public String getMetadataDescriptionText() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description'][style*='display: block;']"));
			String metadatadescriptionText = metadataDescription.getText().trim().replaceAll("\n", "").replaceAll("\r", "");
			metadatadescriptionText = metadatadescriptionText.replaceAll(" ", "");
			System.out.println(metadatadescriptionText);
			return metadatadescriptionText;


		}//End try
		catch(NoSuchElementException e)
		{
			return "";
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getMetadataDescriptionText :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataDescriptionText : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//getMetadataDescriptionColour

	/**
	 * This function is used to verify if Metadatadescription is displayed in the metadatacard
	 * @throws Exception 
	 * 
	 */
	public boolean metadataDescriptionisDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadataDescription = driver.findElement(By.cssSelector("div[class*='mf-metadatacard-description']"));
			if (metadataDescription.isDisplayed())
				return true;//Returns metadatacard description is displayed in the metadatacard
			else
				return false; //Returns metadatacard description is not displayed in the metadatacard

		}//End try
		catch(Exception e){
			return false;//Returns metadatacard description is not displayed in the metadatacard
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.metadataDescriptionisDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//metadataDescriptionisDisplayed

	/**
	 * Verify if object version is displayed or not
	 * 
	 * @return true if displayed; false if not
	 * @throws Exception 
	 */
	public boolean isObjectVersionDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadata = this.driver.findElement(By.cssSelector("div[id*='metadatacard']>div[id='mf-header']"));
			WebElement objectVersion = metadata.findElement(By.cssSelector("span[class*='mf-objectversion']"));
			if(objectVersion.isDisplayed())
				return true;
			else 
				return false;

		}//End try
		catch(Exception e){
			return false;
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isObjectVersionDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isObjecVersionDisplayed

	/**
	 * Verify if object version is displayed or not
	 * 
	 * @return true if displayed; false if not
	 * @throws Exception 
	 */
	public boolean isObjectIDDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			WebElement metadata = this.driver.findElement(By.cssSelector("div[id*='metadatacard']>div[id='mf-header']"));
			WebElement objectID = metadata.findElement(By.cssSelector("span[class*='mf-objectid']"));
			if(objectID.isDisplayed())
				return true;
			else 
				return false;

		}//End try
		catch(Exception e){
			return false;
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isObjectIDDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isObjectIDDisplayed




	/**
	 * clickPropertyDescriptionLink : Clicks the link text from the corresponding property description
	 * @param propName: Property which have the property description
	 * @param linkText: Link text to be clicked
	 * @return Returns the property description links text in array
	 * @throws Exception
	 */
	public void clickPropertyDescriptionLink(String propName, String linkText) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.switchFrame();		
			this.clickProp(propName);//Clicks the property in the metadatacard

			WebElement propertyDescription = driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody>tr[class='mf-description-row']"));
			List<WebElement> links = propertyDescription.findElement(By.xpath("..")).findElements(By.cssSelector("td[class='mf-dynamic-controlfield']>span[class*='mf-property-description'] a"));
			int count = 0;
			for(count = 0; count < links.size(); count++)
				if(links.get(count).getText().equalsIgnoreCase(linkText)){
					ActionEventUtils.click(driver,links.get(count));
					//links.get(count).click();
					break;
				}

			if(count >= links.size())
				throw new Exception("'" + linkText + "' is not avaiable to click in the property '" + propName + "'");

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.clickPropertyDescriptionLink :" + e.getMessage(), e);
		}//End catch
		finally{
			this.clickProp(propName);//Clicks the property in the metadatacard
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.clickPropertyDescriptionLink: Clicked the Property("+propName+") description link: "+linkText, StopWatch.elapsedTime(startTime));

		}

	}//End clickPropertyDescriptionLink

	/**
	 * getMetadatacardPropertyGroups: This function is used to get the property groups from the metadatacard
	 * @return Returns the list of property groups text available in the metadatacard
	 */
	public ArrayList<String> getMetadatacardPropertyGroups() throws Exception {

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			ArrayList<String> groups = new ArrayList<String>(); 

			WebElement groupHeader = driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table"));
			List<WebElement> groupHeaderText = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElements(By.cssSelector("tbody[id*='mf-property-group']>tr[class='mf-propertygroup-title']>th"));

			for(int count = 0; count < groupHeaderText.size(); count++)
				if (!groupHeaderText.get(count).getText().equalsIgnoreCase(""))
					groups.add(groupHeaderText.get(count).getText());//Adds the property group text into the array

			return groups;//Return the Property groups text available in the metadatacard

		} //End try
		catch(Exception e){
			throw new Exception("getMetadatacardPropertyGroups : Error while getting properties from the metadatacard.");
		} //End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadatacardPropertyGroups : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	} //End getInfo



	/**
	 * getPropertyDescriptionLinks : Gets the all the available links text from the corresponding property description
	 * @param propName: Property which have the property description
	 * @return Returns the property description links text in array
	 * @throws Exception
	 */
	public ArrayList<String> getPropertyDescriptionLinks(String propName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			this.switchFrame();
			ArrayList<String> propertyDescLinks = new ArrayList<String>();

			this.clickProp(propName);//Clicks the property in the metadatacard

			WebElement propertyDescription = driver.findElement(By.cssSelector("table[id='mf-property-table']>tbody>tr[class='mf-description-row']"));
			List<WebElement> links = propertyDescription.findElement(By.xpath("..")).findElements(By.cssSelector("td[class='mf-dynamic-controlfield']>span[class*='mf-property-description']>a"));

			for(int count = 0; count < links.size(); count++)
				propertyDescLinks.add(links.get(count).getText());//Adds the link text into the array

			return propertyDescLinks;//Returns the available property description links text in the metadatcard

		}//End try
		catch(NoSuchElementException e)
		{
			return null;
		}
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.getPropertyDescriptionLinks :" + e.getMessage(), e);
		}//End catch
		finally{
			this.clickProp(propName);//Clicks the property in the metadatacard
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getPropertyDescriptionLinks : ", StopWatch.elapsedTime(startTime));

		}

	}//End getPropertyDescriptionLinks

	/**
	 * This function is used to check the footer is displayed in the metadatacard
	 * @throws Exception 
	 * 
	 * 
	 */
	public boolean isfooterDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			this.switchFrame();
			WebElement footer = driver.findElement(By.cssSelector("div[class='mf-property-footer']"));//Web Element for footer property

			if (!footer.isDisplayed())//Checks if footer is displayed in the metadatacard
				return false;
			else
				return true;

		}
		catch(Exception e) {
			return false;
		}
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isfooterDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally


	}//End isMetadataCardFooterdisplayed

	/**
	 * This function is used to check the Add Property Link is displayed in the metadatacard
	 * @throws Exception 
	 * 
	 * 
	 */
	public boolean isAddPropertyLinkDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			return isAddPropLinkDisplayed();				
		}
		catch(Exception e) {
			throw new Exception("Exception at MetadataCard.isAddPropertyLinkDisplayed: "+e.getMessage());
		}
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isAddPropertyLinkDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isAddPropertyLinkDisplayed

	/**
	 * isAddPropLinkDisplayed: Checks if Add property link is displayed or not in the metadatacard
	 * @return
	 * @throws Exception
	 */
	private boolean isAddPropLinkDisplayed() throws Exception{

		try{
			WebElement addProperty = driver.findElement(By.cssSelector("div[class='mf-addproperty']"));//Web Element for Add property link

			if (addProperty.isDisplayed())//Checks if footer is displayed in the metadatacard
				return true;//Returns Add property link is displayed in the metadatacard
			else
				return false;//Returns Add property link is not displayed in the metadatacard				
		}//End try
		catch(Exception e) {
			return false;
		}//End Catch
	}//End of isAddPropLinkDisplayed

	/**
	 * getMetadataButtonColor : Gets the save button color from the metadatacard
	 * @return Background_Color
	 * @throws Exception 
	 */
	public String getMetadataButtonColor() throws Exception {

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			WebElement button = this.driver.findElement(By.cssSelector("div[id='mf-footer']>div[id='mf-buttons']>button[class*='mf-save-button']"));

			if(button.isDisplayed())//Checks if button is displayed in the metadatacard
				return button.getCssValue("background-color");//Returns the background color of the Save button
			else
				return "";

		}//End try
		catch(Exception e) {
			throw new Exception("Exception at MetadataCard.getMetadataButtonColor :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getMetadataButtonColor : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getMetadataButtonColor

	/**
	 * This function is used to get the width & height of Image
	 * @return ImageSize
	 * @throws Exception 
	 */
	public String getImageSize() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			this.switchFrame();

			WebElement image = driver.findElement(By.cssSelector("div[class='mf-description-image-container']>img[id='mf-description-image']"));
			String ImageSize = image.getCssValue("Width");
			ImageSize += ","+image.getCssValue("height");
			System.out.println(ImageSize);
			return ImageSize;

		}//End try
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.getImageSize :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.getImageSize : ", StopWatch.elapsedTime(startTime));
		} //End Finally
	}//End getImageSize

	/**
	 * isGroupCollapsed : This function is used to check the group is collapsed or not
	 * @param group
	 * @return true if group is collapsed or false group is not collapsed
	 */
	public boolean isPropertyGroupCollapsed(int group) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			this.switchFrame();
			WebElement groupText = driver.findElement(By.cssSelector("div[class='mf-dynamic-properties']>table"));
			WebElement groupCollapse = groupText.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[class*='mf-propertygroup-collapsed']>tr[id='propertygroup-title-"+group+"']"));

			if(groupCollapse.isDisplayed())
				return true;
			else 
				return false;

		}//End try
		catch(Exception e){
			return false;
		}//End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropertyGroupCollapsed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * propertyGroupCollapsed : This function is used to check the group is collapsed or not
	 * @param group
	 * @return true if group is collapsed or false group is not collapsed
	 */
	private boolean propertyGroupCollapsed(int group) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			WebElement groupText = driver.findElement(By.cssSelector("div[class='mf-dynamic-properties']>table"));
			WebElement groupCollapse = groupText.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[class*='mf-propertygroup-collapsed']>tr[id='propertygroup-title-"+group+"']"));

			if(groupCollapse.isDisplayed())
				return true;
			else 
				return false;

		}//End try
		catch(Exception e){
			return false;
		}//End catch

		finally {
			Log.event("MetadataCard.PropertyGroupCollapsed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * expandCollapsedGroup : This function is used to expand the group header
	 * @param group
	 * @throws Exception 
	 */
	public void expandPropertyGroup(int group, boolean expand) throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();

			if (!((!this.propertyGroupCollapsed(group)) && expand)) {// Checks if group is already collapsed or not

				WebElement groupHeader = driver.findElement(By.cssSelector("div[class='mf-dynamic-properties']>table"));
				WebElement groupCollapse = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-" + group + "']>tr[id='propertygroup-title-"+ group +"']>th"));
				//groupCollapse.click();
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", groupCollapse);
				ActionEventUtils.click(driver, groupCollapse);
			}

		}//End try
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.expandPropertyGroup :" + e.getMessage(), e);
		}//End catch

		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.expandPropertyGroup : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End expandPropertyGroup

	/**
	 * propertyExistsInGroup : This function is used to verify if property exists in specified group or not
	 * @param group , propName
	 * @return true if property exists in the group and return false if property is does not exists
	 * @throws Exception 
	 */
	public boolean propertyExistsInGroup(int group,String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			this.switchFrame();
			WebElement groupHeader = driver.findElement(By.cssSelector("div[class*='mf-dynamic-properties']>table"));
			WebElement groupHeadertext = groupHeader.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("tbody[id='mf-property-group-" + group + "']"));

			WebElement property = groupHeadertext.findElement(By.xpath("//tr[contains(@class,'mf-property-') and contains(@class,'mf-dynamic-row')]/td[contains(@class, 'mf-dynamic-namefield')]/div/span[contains(@class, 'label') and normalize-space(.) ='"+propName+"']"));

			if (browser.equalsIgnoreCase("edge"))
				if(!property.isDisplayed())//Verify if property is displayed or not
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", property);

			return property.isDisplayed();

		}//End try
		catch(Exception e){
			return false;
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.propertyExistsInGroup : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End propertyExistsInGroup

	/**
	 * isAutomaticPermissionBubbleDisplayed : This functuion is used to check whether the Automatic permission bubble is displayed or not in the metadatacard
	 * @return Boolean : Is displayed or not
	 * @throws Exception
	 */
	public boolean isAutomaticPermissionBubbleDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			WebElement bubble = this.driver.findElement(By.className("mf-info-bubble"));

			if (!bubble.isDisplayed())
				return false;

			return true;

		}//End try
		catch(Exception e){
			return false;
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isAutomaticPermissionBubbleDisplayed : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End isAutomaticPermissionBubbleDisplayed

	/**
	 * getAutomaticPermissionBubbleMessage : This function is used to gets the automatic permission bubble message from the metadatacard
	 * @return
	 * @throws Exception
	 */
	public String getAutomaticPermissionBubbleMessage() throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			return getAutoPermissionBubbleMessage();


		}//End try
		catch(Exception e){
			return "";
		}//End catch
		finally {
			this.switchToDefaultContent();
			Log.event("MetadataCard.getAutomaticPermissionBubbleMessage : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getAutomaticPermissionBubbleMessage

	/**
	 * getAutomaticPermissionBubbleMessage : This function is used to gets the automatic permission bubble message from the metadatacard
	 * @return
	 * @throws Exception
	 */
	private String getAutoPermissionBubbleMessage() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			String bubbleText = this.driver.findElement(By.cssSelector("div[class='mf-info-bubble']")).getText();/*div[style*='transform: scaleX( 1 );']"))*/			
			return bubbleText;

		}//End try
		catch(Exception e){
			return "";
		}//End catch
		finally {
			Log.event("MetadataCard.getAutoPermissionBubbleMessage : ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//End getAutomaticPermissionBubbleMessage

	/**
	 * isPropertyInEditMode : To check if property in Focus
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public boolean isPropertyInEditMode(String propName) throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			this.switchFrame();
			return (this.isPropInEditMode(propName));

		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.isPropertyInEditMode :" + e.getMessage(), e);
		}//End catch
		finally {
			this.switchToDefaultContent(); //Switches to home page driver
			Log.event("MetadataCard.isPropertyInEditMode : Checked if property is in EditMode or not.. ", StopWatch.elapsedTime(startTime));
		} //End Finally

	}

	/**
	 * isPropInEditMode: To check if property is in Focus in the metadatacard
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	private boolean isPropInEditMode(String propName) throws Exception{

		try
		{
			int count = 0;
			WebElement propTable = driver.findElement(By.cssSelector("table[id='mf-property-table']"));
			List<WebElement> props = propTable.findElements(By.cssSelector("tr[class*='mf-property-'][class*='mf-dynamic-row']>td[class*='mf-dynamic-namefield']>div>span[class*='label']"));

			for(count = 0; count < props.size(); count++) {
				if(props.get(count).getText().trim().equals(propName))
					break;
			}
			WebElement field = props.get(count).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.cssSelector("td[class*='mf-dynamic-controlfield']"));

			if(field.findElement(By.cssSelector("input")).isDisplayed())
				return true;
			else
				return false;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e){
			throw new Exception("Exception at MetadataCard.isPropertyInEditMode :" + e.getMessage(), e);
		}
	}

	/**
	 * markApproveByUser : This function is used for selecting the Approve icon by username
	 * 
	 */
	public Boolean clickVariesIcon(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			return this.clickVariesIcon(userIndex);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.markApproveByUser : "+ e.getMessage(), e);
		}//End catch
	}//End markApproveByUser


	/**
	 * markApproveByUser : This function is used for selecting the Approve icon by username
	 * 
	 */
	public Boolean markApproveByUser(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			return this.clickApproveIcon(userIndex);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.markApproveByUser : "+ e.getMessage(), e);
		}//End catch
	}//End markApproveByUser

	/**
	 * markApproveByUser : This function is used for selecting the reject icon by username
	 * 
	 */
	public Boolean markRejectByUser(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			return this.clickRejectIcon(userIndex);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.markApproveByUser: "+ e.getMessage(), e);
		}//End catch
	}//End markApproveByUser


	/**
	 * isSelectedObjectApprovedByUser : Verify if the assignment is approved by the user by checking both the username 
	 * and the tick icon in the metadata card
	 */
	public Boolean isSelectedObjectApprovedByUser(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			return this.isApprovedSelected(userIndex);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.isSelectedObjectApprovedByUser: "+ e.getMessage(), e);
		}//End catch
	}//End isSelectedObjectApprovedByUser

	/**
	 * isSelectedObjectRejectedByUser : Pops out metadata card of selected object and 
	 * verifies if the assignment is rejected by the user by checking both the username and the cross icon.
	 */
	public Boolean isSelectedObjectRejectedByUser(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			Boolean isRejected = this.isRejectedSelected(userIndex);

			return isRejected;

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.isSelectedObjectRejectedByUser: "+ e.getMessage(), e);
		}//End catch
	}//End isSelectedObjectRejectedByUser

	/**
	 * isSelectedObjectCompletedByUser : Verify if the assignment is completed by the user by checking both the username 
	 * and the tick icon in the metadata card
	 */
	public Boolean isSelectedObjectCompletedByUser(String userFullName) throws Exception{

		try{

			int userIndex = this.getPropertyValueIndex("Assigned to", userFullName);
			return this.isCompleteSelected(userIndex);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at MetadataCard.isSelectedObjectCompletedByUser: "+ e.getMessage(), e);
		}//End catch
	}//End isSelectedObjectCompletedByUser

} //End class MetadataCard
package MFClient.Wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;

public class SearchPanel {

	private final WebDriver driver;

	@FindBy(how=How.CSS,using="div[id='search']>form[id='searchForm']")
	private WebElement searchRow;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");

	/**
	 * SearchPanel : Constructor of SearchPanel wrapper
	 * @param driver - Web driver
	 * @return none
	 * @throws Exception
	 */
	public SearchPanel(final WebDriver driver) throws Exception {
		try {

			this.driver = driver;
			PageFactory.initElements(this.driver, this);

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.SearchPanel  : " +e.getMessage(), e);
		} //End catch
	} //End SearchPanel

	/**
	 * setSearchWord : To set the search word in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public void setSearchWord(String searchWord) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement searchBox = this.searchRow.findElement(By.cssSelector("input[id*='quickSearch_Top_input'], input[id='searchIn_searchString']")); 
			searchBox.clear();
			searchBox.sendKeys(searchWord);
			Utils.fluentWait(this.driver);
			try {
				if(this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']:not([style*='display: none'])")).isDisplayed())
					ActionEventUtils.click(driver, this.searchRow.findElement(By.id("quickSearch_Top_arrow")));
				//this.searchRow.findElement(By.id("quickSearch_Top_arrow")).click();
			}
			catch (Exception e1) {}
			Utils.fluentWait(this.driver);

			Log.event("setSearchWord : Search key word is entered in text box", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setSearchWord  : " +e.getMessage(), e);
		} //End catch

	} //End function setSearchWord

	/**
	 * setSearchWord : To set the search word in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public void setSearchWord(String searchWord, boolean autoFill) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement searchBox = this.searchRow.findElement(By.cssSelector("input[id*='quickSearch_Top_input']")); 
			/*WebElement dropDownList = driver.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']"));
			WebElement dropDown = driver.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']"));*/

			searchBox.clear();
			Log.event("Cleared the search text box..", StopWatch.elapsedTime(startTime));
			searchBox.sendKeys(searchWord);
			Log.event("Search text: "+searchWord+" is entered..", StopWatch.elapsedTime(startTime));
			Utils.fluentWait(this.driver);

			/*if(dropDownList.isDisplayed())
				ActionEventUtils.click(driver, dropDown);*/

			//searchBox.sendKeys(Keys.TAB);

			if(autoFill) {
				while(!searchBox.getAttribute("value").equalsIgnoreCase(searchWord) && !searchBox.getAttribute("value").equals("")) {
					searchBox = this.searchRow.findElement(By.cssSelector("input[id*='quickSearch_Top_input']"));
					searchBox.sendKeys(Keys.END);
					searchBox.sendKeys(Keys.BACK_SPACE);
				}
			}

			Utils.fluentWait(this.driver);

			/*if(dropDownList.isDisplayed())
				ActionEventUtils.click(driver, dropDown);*/

			//searchBox.sendKeys(Keys.TAB);

			Log.event("setSearchWord : Search key word is entered in text box", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at SearchPanel.setSearchWord : "+e);
		} //End catch

	} //End function setSearchWord

	/**
	 * setSearchWordInRightPane : To set the search word in right pane search
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public void setSearchWordInRightPane(String searchWord, boolean autoFill) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			WebElement searchBox = rightPaneSearch.findElement(By.cssSelector("input[id*='quickSearch_right_input']")); 

			searchBox.clear();
			Log.event("Cleared the search text box..", StopWatch.elapsedTime(startTime));
			searchBox.sendKeys(searchWord);
			Log.event("Search text: "+searchWord+" is entered..", StopWatch.elapsedTime(startTime));
			Utils.fluentWait(this.driver);
			searchBox.sendKeys(Keys.TAB);

			if(autoFill) {
				while(!searchBox.getAttribute("value").equalsIgnoreCase(searchWord) && !searchBox.getAttribute("value").equals("")) {
					searchBox = rightPaneSearch.findElement(By.cssSelector("input[id*='quickSearch_right_input']"));
					searchBox.sendKeys(Keys.END);
					searchBox.sendKeys(Keys.BACK_SPACE);
				}
			}

			Utils.fluentWait(this.driver);
			searchBox.sendKeys(Keys.TAB);

			Log.event("setSearchWordInRightPane : Search key word is entered in text box", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at SearchPanel.setSearchWordInRightPane : "+e);
		} //End catch

	} //End function setSearchWordInRightPane

	/**
	 * getSearchHistory : To set the search word in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public List<String> getSearchHistory() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			int snooze = 0;
			while(snooze < 5 && this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']")).getAttribute("style").contains("display: none")) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			List<WebElement> options = this.searchRow.findElement(By.cssSelector("div[id*='quickSearch_Top_ctr']>div[class='listcontent']")).findElements(By.cssSelector("div"));
			List <String> values = new ArrayList<String>();
			String id = "";


			for(int count = 0; count < options.size(); count++) {
				id = options.get(count).getAttribute("id");
				if(id.equals("divider")) 
					break;
				else {
					values.add(options.get(count).getAttribute("val"));
				}
			}

			Utils.fluentWait(this.driver);
			Log.event("getSearchHistory : Search history is get from the top pane", StopWatch.elapsedTime(startTime));
			return values;


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchHistory  : " +e.getMessage(), e);
		} //End catch

	} //End function getSearchHistory


	/**
	 * getSearchHistoryForSearchWord : Enters search word and returns the displayed suggestions from search history
	 * @param searchWord - Search word that is entered to search bar
	 * @return A list of previously used search words as strings
	 * @throws Exception
	 */
	public List<String> getSearchHistoryForSearchWord(String searchWord) throws Exception
	{
		final long startTime = StopWatch.startTime();

		try {


			WebElement searchBox = this.searchRow.findElement(By.cssSelector("input[id*='quickSearch_Top_input']")); 

			searchBox.clear();
			Log.event("Cleared the search text box..", StopWatch.elapsedTime(startTime));
			searchBox.sendKeys(searchWord);
			Log.event("Search text: "+searchWord+" is entered..", StopWatch.elapsedTime(startTime));
			Utils.fluentWait(this.driver);

			return getSearchHistory();

		}
		catch(Exception e){
			throw new Exception("Exception at  SearchPanel.getSearchHistoryForSearchWord  : " +e.getMessage(), e);
		}
	}

	/**
	 * getSearchHistoryInRightPane : To set the search word in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public List<String> getSearchHistoryInRightPane() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(snooze < 5 && rightPaneSearch.findElement(By.cssSelector("div[id='quickSearch_right_ctr']")).getAttribute("style").contains("display: none")) {
				ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			List<WebElement> options = rightPaneSearch.findElement(By.cssSelector("div[id*='quickSearch_right_ctr']>div[class='listcontent']")).findElements(By.cssSelector("div"));
			List <String> values = new ArrayList<String>();
			String id = "";


			for(int count = 0; count < options.size(); count++) {
				id = options.get(count).getAttribute("id");
				if(id.equals("divider")) 
					break;
				else {
					values.add(options.get(count).getAttribute("val"));
				}
			}

			Utils.fluentWait(this.driver);
			Log.event("getSearchHistoryInRightPane : Search history is get from the right pane", StopWatch.elapsedTime(startTime));
			return values;


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchHistoryInRightPane  : " +e.getMessage(), e);
		} //End catch

	} //End function getSearchHistoryInRightPane

	/**
	 * getSearchCriterias : To set the search criterias in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public List<String> getSearchCriterias() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(snooze < 5 && this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']")).getAttribute("style").contains("display: none")) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			List<WebElement> options = this.searchRow.findElement(By.cssSelector("div[id*='quickSearch_Top_ctr']>div[class='listcontent']")).findElements(By.cssSelector("div"));
			List <String> values = new ArrayList<String>();
			String id = "";


			for(int count = 0; count < options.size(); count++) {
				id = options.get(count).getAttribute("id");
				if(id.equals("metaalone") || id.equals("filealone") || id.equals("metacard")) 
					values.add(options.get(count).getAttribute("val"));
			}

			Utils.fluentWait(this.driver);
			Log.event("getSearchCriterias : Search criteria is get from the top pane", StopWatch.elapsedTime(startTime));
			return values;


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchCriterias  : " +e.getMessage(), e);
		} //End catch

	} //End function getSearchCriterias

	/**
	 * isSearchCriteriasDisplayedInRightPane : To set the search criterias in quick search pane
	 * @param searchWord - keyword to search
	 * @return none
	 * @throws Exception
	 */
	public boolean isSearchCriteriasDisplayedInRightPane() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(snooze < 5 && rightPaneSearch.findElement(By.cssSelector("div[id='quickSearch_right_ctr']")).getAttribute("style").contains("display: none")) {
				ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			List<WebElement> options = rightPaneSearch.findElement(By.cssSelector("div[id*='quickSearch_right_ctr']>div[class='listcontent']")).findElements(By.cssSelector("div"));
			String id = "";
			boolean exists = false;

			for(int count = 0; count < options.size(); count++) {
				id = options.get(count).getAttribute("id");
				if(id.equals("metaalone") || id.equals("filealone") || id.equals("metacard")) 
					exists = true;
			}

			Utils.fluentWait(this.driver);
			Log.event("isSearchCriteriasDisplayedInRightPane : Checked Search criteria is displayed or not in the right pane.", StopWatch.elapsedTime(startTime));
			return exists;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.isSearchCriteriasDisplayedInRightPane  : " +e.getMessage(), e);
		} //End catch

	} //End function isSearchCriteriasDisplayedInRightPane

	/**
	 * getSearchWord : To get the search word typed in the search pane
	 * @param none
	 * @return search key word
	 * @throws Exception
	 */
	public String getSearchWord() throws Exception {

		try {

			WebElement searchBox = this.searchRow.findElement(By.cssSelector("input[id*='quickSearch_Top_input']")); 
			return(searchBox.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchWord  : " +e.getMessage(), e);
		} //End catch

	} //End function getSearchWord

	/**
	 * setSearchInType : To Set search in type in the quick search
	 * @param searchIn - Type of search operation to be performed (eg : Search in metadata)
	 * @return none
	 * @throws Exception
	 */
	public void setSearchInType(String searchIn) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String id = "";
			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
			Utils.fluentWait(this.driver);
			int snooze = 0;
			while(this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']")).getAttribute("style").contains("display: none") && snooze < 10) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
				//	this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
				Utils.fluentWait(this.driver);
				snooze++;
			}

			if(searchIn.equals("Search in metadata"))
				id = "metaalone";
			else if(searchIn.equals("Search in file contents"))
				id = "filealone";
			else
				id = "metacard";

			WebElement option = this.searchRow.findElement(By.cssSelector("div[id='"+id+"']"));
			WebElement scroll = option.findElement(By.xpath("..")).findElement(By.xpath(".."));
			snooze = 0;

			while(!option.isDisplayed() && snooze < 400) {
				scroll.sendKeys(Keys.ARROW_DOWN);
				snooze++;
			}

			if(option.isDisplayed())
				if (browser.equalsIgnoreCase("edge") || browser.equalsIgnoreCase("safari"))
					option.click();
				else if(browser.equalsIgnoreCase("firefox"))
					ActionEventUtils.moveToElementAndClick(driver, option);
				else
					ActionEventUtils.click(driver, option);
			//option.click();

			/*List<WebElement> searchelements = this.searchRow.findElements(By.cssSelector("span[class='ddTitleText']")); //Gets all the search types
			int noOfElmnts = searchelements.size();
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(searchIn.toUpperCase())) {
					searchelements.get(itemIdx).click(); //Clicks the item in search type
					break;
				}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
				throw new Exception("Item (" + searchIn + ") does not exists in the list.");*/

			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down

			Log.event("setSearchInType : " + searchIn + " is selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setSearchInType  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchInType

	/**
	 * setSearchInType : To check for existence of Search in type
	 * @param searchIn - Type of search operation to be checked for existence (eg : Search in metadata)
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isSearchInTypeExists(String searchIn) throws Exception {

		final long startTime = StopWatch.startTime();
		boolean result = false;

		try {

			String id = "";
			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
			Utils.fluentWait(this.driver);
			int snooze = 0;
			while(this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']")).getAttribute("style").contains("display: none") && snooze < 10) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
				//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
				Utils.fluentWait(this.driver);
				snooze++;
			}

			if(searchIn.equals("Search in metadata"))
				id = "metaalone";
			else if(searchIn.equals("Search in file contents"))
				id = "filealone";
			else
				id = "metacard";

			WebElement option = this.searchRow.findElement(By.cssSelector("div[id='"+id+"']"));

			result = option.isEnabled();

			/*List<WebElement> searchelements = this.searchRow.findElements(By.cssSelector("span[class='ddTitleText']")); //Gets all the search types
			int noOfElmnts = searchelements.size();
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked
				System.out.println(searchelements.get(itemIdx).getText());
				if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(searchIn.toUpperCase())) 
					break;
			}

			this.searchRow.findElement(By.cssSelector("div[id='searchIn_title']>span[id='searchIn_arrow']")).click();*/ //Clicks to close drop down
			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
			Log.event("setSearchInType : " + searchIn + " is verified for existence.", StopWatch.elapsedTime(startTime));

			/*if (itemIdx >= noOfElmnts)*/ //Checks for the existence of the item to click
			return result;
			/*else
				return true;*/


		} //End try

		catch (Exception e) {
			return false;
		} //End catch

	} //End function SetSearchInType

	/**
	 * clearHistory : To click the clearHistory in the search field drop down list
	 * @throws Exception
	 */
	public void clearHistory() throws Exception {

		try {

			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
			//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(this.searchRow.findElement(By.cssSelector("div[id='quickSearch_Top_ctr']")).getAttribute("style").contains("display: none") && snooze < 10) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")));
				//this.searchRow.findElement(By.cssSelector("span[id='quickSearch_Top_arrow']")).click(); //Clicks to close drop down
				Utils.fluentWait(this.driver);
				snooze++;
			}

			WebElement clearHistory = this.searchRow.findElement(By.cssSelector("div[id='history']")); //Gets all the search types
			//ActionEventUtils.click(driver,clearHistory);
			if(browser.equalsIgnoreCase("firefox"))
				ActionEventUtils.moveToElementAndClick(driver, clearHistory);
			else
				clearHistory.click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.clearHistory  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchInType

	/**
	 * clearSearchHistoryInRightPane : To click the clearHistory in the search field drop down list in rightpane
	 * @throws Exception
	 */
	public void clearSearchHistoryInRightPane() throws Exception {

		try {

			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(rightPaneSearch.findElement(By.cssSelector("div[id='quickSearch_right_ctr']")).getAttribute("style").contains("display: none") && snooze < 10) {
				ActionEventUtils.click(driver, rightPaneSearch.findElement(By.cssSelector("span[id='quickSearch_right_arrow']")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			WebElement clearHistory = rightPaneSearch.findElement(By.cssSelector("div[id='history']")); //Gets all the search types
			ActionEventUtils.click(driver,clearHistory);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.clearHistory  : " +e.getMessage(), e);
		} //End catch

	} //End function clearSearchHistoryInRightPane

	/**
	 * getSearchInType : To get the selected search in type in the quick search
	 * @param none
	 * @return Selected search in type is returned as string
	 * @throws Exception
	 */
	public String getSearchInType() throws Exception {

		try {
			String searchInType = "";
			String icon = this.searchRow.findElement(By.cssSelector("div[class='searchInIcon']")).getAttribute("style"); //Clicks to close drop down
			if(icon.contains("metacard"))
				searchInType = "Search in metadata and file contents";
			else if(icon.contains("filealone"))
				searchInType = "Search in file contents";
			else
				searchInType = "Search in metadata";

			return searchInType;
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchInType  : " +e.getMessage(), e);
		} //End catch

	} //End function getSearchInType

	/**
	 * isAdvancedSearchBtnDisplayed : To check if advanced search button is displayed
	 * @param none
	 * @return true if advanced search button is displayed; false if not
	 * @throws Exception
	 */
	public Boolean isAdvancedSearchBtnDisplayed() throws Exception {

		try {

			WebElement adSearchBtn = this.searchRow.findElement(By.cssSelector("div[class='searchText']")); //Gets the instance of Advanced Search button

			if (adSearchBtn.isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception("Exception at  SearchPanel.isAdvancedSearchBtnDisplayed :" +e.getMessage(), e);
		} //End catch

		return false;

	} //End function isAdvancedSearchBtnDisplayed

	/**
	 * isAdvancedSearchDisplayed : To check if advanced search area is displayed
	 * @param none
	 * @return true if advanced search area is displayed; false if not
	 * @throws Exception
	 */
	public Boolean isAdvancedSearchDisplayed() throws Exception {

		try {

			WebElement searchAdvanced = this.searchRow.findElement(By.cssSelector("div[id='searchAdvanced']"));
			WebElement advancedSearchPane = searchAdvanced.findElement(By.cssSelector("div[id='searchType']"));

			/*if (browser.equalsIgnoreCase("safari")){
				if (!searchAdvanced.getAttribute("style").toUpperCase().contains("DISPLAY: NONE;"))
					return true;
			}
			else*/
			if (advancedSearchPane.isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception("Exception at  SearchPanel.isAdvancedSearchDisplayed : " +e.getMessage(), e);
		} //End catch

		return false;

	} //End function IsAdvancedSearchDisplayed	

	/**
	 * clickAdvancedSearch : To click Advanced Search button in quick search pane
	 * @param none
	 * @return true if advanced search area is opened; false if not
	 * @throws Exception
	 */
	public void clickAdvancedSearch(Boolean isOpen) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			WebElement btnAdSearch = this.searchRow.findElement(By.cssSelector("div[class='searchText']")); //Gets the instance of Advanced Search button

			if (!this.isAdvancedSearchDisplayed() && isOpen)//Clicks to open Advanced Search
				if (browser.equalsIgnoreCase("Safari")){
					Utils.fluentWait(driver);
					((JavascriptExecutor) driver).executeScript("arguments[0].click()",btnAdSearch);
				}
				else
					ActionEventUtils.click(driver, btnAdSearch);

			//btnAdSearch.click();
			else if (this.isAdvancedSearchDisplayed() && !isOpen) //Clicks to close Advanced Search
				//btnAdSearch.click();
				if (browser.equalsIgnoreCase("Safari"))
				{
					Utils.fluentWait(driver);
					((JavascriptExecutor) driver).executeScript("arguments[0].click()",btnAdSearch);
				}
				else
					ActionEventUtils.click(driver, btnAdSearch);

			Log.event("clickAdvancedSearch :Advanced search icon is clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.clickAdvancedSearch  : " +e.getMessage(), e);
		} //End catch

	} //End function ClickAdvancedSearch

	/**
	 * SetSearchType : To set the search type quick search pane
	 * @param searchType - Search type (Eg : Search only: Customers)
	 * @return none
	 * @throws Exception
	 */
	public void setSearchType(String searchType) throws Exception {

		final long startTime = StopWatch.startTime();
		boolean isOpen = false;

		try {

			Utils.fluentWait(driver);

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}

			//this.searchRow.findElement(By.id("fbsearchObjectType_arrow")).click(); //Clicks Down Arrow to open search types
			ActionEventUtils.click(driver,this.searchRow.findElement(By.id("fbsearchObjectType_arrow")));
			//this.searchRow.findElement(By.id("fbsearchObjectType_input")).click(); //Clicks Down Arrow to open search types			
			Utils.fluentWait(this.driver);

			int snooze = 0;
			while(snooze < 10 && this.searchRow.findElement(By.id("fbsearchObjectType_ctr")).getAttribute("style").contains("display: none")) {
				//this.searchRow.findElement(By.id("fbsearchObjectType_input")).click(); //Clicks Down Arrow to open search types			
				ActionEventUtils.click(driver, this.searchRow.findElement(By.id("fbsearchObjectType_input")));
				Utils.fluentWait(this.driver);
				snooze++;
			}

			List<WebElement> searchelements = this.searchRow.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
			int noOfElmnts = searchelements.size();
			int itemIdx = 0;
			String item = "";

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++){ //Loops to identify the instance of the item to be clicked
				System.out.println(searchelements.get(itemIdx).getText());
				item = searchelements.get(itemIdx).getAttribute("val").trim().replaceAll("&nbsp;", " ").trim();

				if (item.toUpperCase().contains(searchType.toUpperCase().trim())) {
					Utils.fluentWait(driver);
					if (browser.equalsIgnoreCase("safari"))
						searchelements.get(itemIdx).click(); //Clicks the item in search type
					else
						ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));			

					return;
				}

			}
			if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
				throw new Exception("Item (" + searchType + ") does not exists in the list.");

			Log.event("setSearchType : " + searchType + " is selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setSearchType  : " +e.getMessage(), e);
		} //End catch

		finally {
			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);
		}

	} //End function SetSearchType

	/**
	 * getSearchType : To get the selected search type quick search pane
	 * @param none
	 * @return searchType - Search type (Eg : Search only: Customers)
	 * @throws Exception
	 */
	public String getSearchType() throws Exception {

		boolean isOpen = false;

		try {

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}
			ActionEventUtils.click(driver, this.searchRow.findElement(By.id("fbsearchObjectType_arrow")));
			//this.searchRow.findElement(By.id("fbsearchObjectType_arrow")).click(); //Clicks Down Arrow to open search types		
			Utils.fluentWait(this.driver);
			WebElement searchelement = this.searchRow.findElement(By.cssSelector("div[class*='ffb-match']")); //Gets all the search types
			String searchType = searchelement.getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim(); //Clicks the item in search type
			ActionEventUtils.click(driver, this.searchRow.findElement(By.id("fbsearchObjectType_input")));
			//	this.searchRow.findElement(By.id("fbsearchObjectType_input")).click();
			return searchType;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchType  : " +e.getMessage(), e);
		} //End catch

		finally {

			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);

		}

	} //End function getSearchType

	/**
	 * setSearchWithInThisFolder : To enable or disable 'Search with in this folder' option
	 * @param isEnable - true to enable; false to disable
	 * @return true if selected; false if not selected
	 * @throws Exception
	 */
	public Boolean setSearchWithInThisFolder(Boolean isEnable) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement searchFolder = this.searchRow.findElement(By.id("searchWithinThisFolder")); //Stores Search with in this folder web element 

			if (!searchFolder.isEnabled()) //Throws exception if Search with in this folder is not enabled
				throw new Exception("Search With in this Folder check box is not enabled.");

			if (!searchFolder.isSelected() && isEnable) //Enables Search within this folder option
				ActionEventUtils.click(driver, searchFolder);
			//searchFolder.click(); 
			else if (searchFolder.isSelected() && !isEnable) //Disables Search within this folder option
				ActionEventUtils.click(driver, searchFolder);

			Log.event("setSearchWithInThisFolder : Search with in this folder is selected.", StopWatch.elapsedTime(startTime));

			return (searchFolder.isSelected());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setSearchWithInThisFolder  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchWithInThisFolder

	/**
	 * setSearchWithInThisFolder : To enable or disable 'Search with in this folder' option
	 * @param isEnable - true to enable; false to disable
	 * @return true if selected; false if not selected
	 * @throws Exception
	 */
	public Boolean getSearchWithInThisFolder() throws Exception {

		boolean isOpen = false;

		try {

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}

			WebElement searchFolder = this.searchRow.findElement(By.id("searchWithinThisFolder")); //Stores Search with in this folder web element 

			if (!searchFolder.isEnabled()) //Throws exception if Search with in this folder is not enabled
				throw new Exception("Search With in this Folder check box is not enabled.");

			return searchFolder.isSelected();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchWithInThisFolder  : " +e.getMessage(), e);
		} //End catch

		finally {
			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);
		}

	} //End function SetSearchWithInThisFolder

	/**
	 * resetAll : To click ResetAll button in the Advanced Search bar
	 * @param none
	 * @return none
	 * @throws Exception
	 */
	public void resetAll() throws Exception {

		final long startTime = StopWatch.startTime();
		boolean isOpen = false;

		try {

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}

			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("div[id='resetSearchButton']")));
			//this.searchRow.findElement(By.cssSelector("div[id='resetSearchButton']")).click(); //Clicks Reset All button
			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(this.driver); 
			mfilesDialog.clickOkButton(); //Clicks Yes button in the confirmation dialog
			Utils.fluentWait(driver);

			if (MFilesDialog.exists(driver))
				throw new Exception("Reset All dialog is not closed.");

			Log.event("resetAll : Reset All button is selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.resetAll  : " +e.getMessage(), e);
		} //End catch

		finally {

			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);
		}

	} //End function ResetAll

	/**
	 * setSearchOption : To select the search option in quick search pane
	 * @param searchOption - Search option (Eg :Values can be All words, Any word or boolean) 
	 * @return true if option is selected; false if not
	 * @throws Exception
	 */
	public Boolean setSearchOption(String searchOption) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			int snooze = 0;
			String selector = "";
			this.clickAdvancedSearch(true); //Clicks to open Advanced Search
			Utils.fluentWait(this.driver);

			switch (searchOption.toUpperCase().trim()) {

			case "ALL WORDS" : {
				selector = "searchAllWordsButton";
				break;
			}		
			case "ANY WORD" : { 
				selector = "searchAnyWordButton";
				break;
			}
			case "BOOLEAN" : { 
				selector = "searchBooleanButton";
				break;
			}
			}
			while(snooze < 10 && !this.searchRow.findElement(By.id(selector)).isSelected()) {
				ActionEventUtils.click(driver, this.searchRow.findElement(By.id(selector)));
				//	this.searchRow.findElement(By.id(selector)).click();
				snooze++;
			}

			Log.event("setSearchOption : " + searchOption + " is selected.", StopWatch.elapsedTime(startTime));
			return (this.searchRow.findElement(By.id(selector)).isSelected());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setSearchOption  : " +e.getMessage(), e);
		} //End catch

	} //End function setSearchOption

	/**
	 * getSelectedSearchOption : To get the selected search option
	 * @param none 
	 * @return Selected search option
	 * @throws Exception
	 */
	public String getSelectedSearchOption() throws Exception {

		try {

			this.clickAdvancedSearch(true); //Clicks to open Advanced Search
			Utils.fluentWait(this.driver);

			if (this.searchRow.findElement(By.id("searchAllWordsButton")).isSelected())
				return "All words";
			else if (this.searchRow.findElement(By.id("searchAnyWordButton")).isSelected())
				return "Any Word";
			else if (this.searchRow.findElement(By.id("searchBooleanButton")).isSelected())
				return "Boolean";	
			else
				return "";

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSelectedSearchOption  : " +e.getMessage(), e);
		} //End catch

	} //End function getSelectedSearchOption

	/**
	 * isSearchOptionSelected : To check if the search option in quick search pane is selected
	 * @param searchOption - Search option (Eg :Values can be All words, Any word or boolean) 
	 * @return true if option is selected; false if not
	 * @throws Exception
	 */
	public Boolean isSearchOptionSelected(String searchOption) throws Exception {

		try {

			this.clickAdvancedSearch(true); //Clicks to open Advanced Search
			Utils.fluentWait(this.driver);

			switch (searchOption.toUpperCase().trim()) {

			case "ALL WORDS" : 
				return (this.searchRow.findElement(By.id("searchAllWordsButton")).isSelected());

			case "ANY WORD" : 
				return (this.searchRow.findElement(By.id("searchAnyWordButton")).isSelected());

			case "BOOLEAN" : 
				return (this.searchRow.findElement(By.id("searchBooleanButton")).isSelected());

			default : 
				throw new Exception ("Search Option (" + searchOption + ") does not exists.");

			} //End Switch case

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.isSearchOptionSelected  : " +e.getMessage(), e);
		} //End catch

	} //End function isSearchOptionSelected

	/**
	 * setAdditionalConditions : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public void setAdditionalConditions(String property, String condition, String propValue) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.clickAdvancedSearch(true);
			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0, snooze = 0;

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

			//Sets Property in the Additional search condition
			//------------------------------------------------
			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']"));
			WebElement searchTextBoxField = searchTextBox.findElement(By.cssSelector("input[placeholder='Select property']"));
			if(property != "") {
				if(searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq") != null) {
					if(!searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq").replaceAll("&nbsp;", " ").trim().equals(property)) {
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
						//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
						//Utils.fluentWait(this.driver);
						searchTextBoxField.click();
						searchTextBoxField.clear();
						searchTextBoxField.sendKeys(property);
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
						//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
						Utils.fluentWait(this.driver);

						while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
							searchTextBox.sendKeys(Keys.END);
							searchTextBox.sendKeys(Keys.BACK_SPACE);
							snooze++;
						}
						Utils.fluentWait(this.driver);

						searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
						Utils.fluentWait(this.driver);
						noOfElmnts = searchelements.size();
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
							if (searchelements.get(itemIdx).getText().replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equals(property.toUpperCase())) {
								Utils.fluentWait(driver);
								if(browser.equalsIgnoreCase("safari"))
									searchelements.get(itemIdx).click();
								else
									ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
								//searchelements.get(itemIdx).click(); //Clicks the item in search type
								break;
							}
						}

						if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
							throw new Exception("Item (" + property + ") does not exists in the list.");
					}
				}
				else {
					searchTextBoxField.click();
					searchTextBoxField.clear();
					searchTextBoxField.sendKeys(property);
					//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
					//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
					Utils.fluentWait(this.driver);

					while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
						searchTextBox.sendKeys(Keys.END);
						searchTextBox.sendKeys(Keys.BACK_SPACE);
						snooze++;
					}
					Utils.fluentWait(this.driver);

					searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
					noOfElmnts = searchelements.size();
					for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
						if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equals(property.toUpperCase())) {
							if(browser.equalsIgnoreCase("safari"))
								searchelements.get(itemIdx).click();
							else
								ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
							break;
							//searchelements.get(itemIdx).click(); //Clicks the item in search type


						}
					}

					if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
						throw new Exception("Item (" + property + ") does not exists in the list.");
				}
			}
			//Sets Condition in the Additional search condition
			//--------------------------------------------------
			searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchConditionType']"));

			ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")));
			//searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")).click(); //Clicks down arrow to open 
			Utils.fluentWait(this.driver);

			searchelements = searchTextBox.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
			noOfElmnts = searchelements.size();

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equals(condition.toUpperCase())) {
					if(browser.equalsIgnoreCase("safari"))
						searchelements.get(itemIdx).click();
					else
						ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
					//searchelements.get(itemIdx).click(); //Clicks the item in search type
					break;
				}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
				throw new Exception("Item (" + condition + ") does not exists in the list.");
			//Sets Value in the Additional search condition
			//---------------------------------------------
			if(propValue.equals("")) {
				try {
					searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " +
							"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));
					searchTextBox.clear();
				}
				catch (Exception e1) {
					return;
				}
			}
			else
				this.setAdvancedSearchValue(propValue);

			Log.event("setAdditionalConditions : Additional conditions are selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdditionalConditions  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * setAdditionalConditions : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public void setAdditionalConditions(String property, String condition, String propValue, int index) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.clickAdvancedSearch(true);
			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0, snooze = 0;

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar
			//ActionEventUtils.click(driver, adSearchBar);
			//adSearchBar.click(); //Clicks Additional search row

			List<WebElement> rows = adSearchBar.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='ddlSearchAdvanced searchRow']"));
			adSearchBar = rows.get(index-1);

			//Sets Property in the Additional search condition
			//------------------------------------------------
			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']"));
			WebElement searchTextBoxField = searchTextBox.findElement(By.cssSelector("input[placeholder='Select property']"));
			if(property != "") {
				if(searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq") != null) {
					if(!searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq").equals(property)) {
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
						//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 

						searchTextBoxField.click();
						searchTextBoxField.clear();
						searchTextBoxField.sendKeys(property);
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
						//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
						Utils.fluentWait(this.driver);

						while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
							searchTextBox.sendKeys(Keys.END);
							searchTextBox.sendKeys(Keys.BACK_SPACE);
							snooze++;
						}
						Utils.fluentWait(this.driver);

						searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
						noOfElmnts = searchelements.size();
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
							if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").toUpperCase().trim().equals(property.toUpperCase())) {
								if(browser.equalsIgnoreCase("safari"))
									searchelements.get(itemIdx).click();
								else
									ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
								//searchelements.get(itemIdx).click(); //Clicks the item in search type
								break;
							}
						}

						if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
							throw new Exception("Item (" + property + ") does not exists in the list.");
					}
				}
				else {
					searchTextBoxField.click();
					searchTextBoxField.clear();
					searchTextBoxField.sendKeys(property);
					//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
					//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
					Utils.fluentWait(this.driver);

					while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
						searchTextBox.sendKeys(Keys.END);
						searchTextBox.sendKeys(Keys.BACK_SPACE);
						snooze++;
					}
					Utils.fluentWait(this.driver);

					searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
					noOfElmnts = searchelements.size();
					for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
						if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").toUpperCase().trim().equals(property.toUpperCase())) {
							if(browser.equalsIgnoreCase("safari"))
								searchelements.get(itemIdx).click();
							else
								ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
							break;
						}
					}

					if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
						throw new Exception("Item (" + property + ") does not exists in the list.");
				}
			}
			//Sets Condition in the Additional search condition
			//--------------------------------------------------
			searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchConditionType']"));

			ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")));
			//searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")).click(); //Clicks down arrow to open 
			Utils.fluentWait(this.driver);


			searchelements = searchTextBox.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
			noOfElmnts = searchelements.size();

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").toUpperCase().trim().equals(condition.toUpperCase())) {
					if(browser.equalsIgnoreCase("safari"))
						searchelements.get(itemIdx).click();
					else
						ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
					//	searchelements.get(itemIdx).click(); //Clicks the item in search type
					break;
				}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
				throw new Exception("Item (" + condition + ") does not exists in the list.");

			//Sets Value in the Additional search condition
			//---------------------------------------------
			if(propValue.equals("")) {
				try {
					searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " +
							"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));
					searchTextBox.clear();
				}
				catch (Exception e1) {
					return;
				}
			}
			else
				this.setAdvancedSearchValue(propValue, index);

			Log.event("setAdditionalConditions : Additional conditions are selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdditionalConditions  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * setAdditionalConditions : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public void setAdditionalConditions(String advancedSearchCdtn) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String advancedSearchItems[] = advancedSearchCdtn.split("\n"); 
			int adRowCt = advancedSearchItems.length;

			this.clickAdvancedSearch(true);
			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0, snooze = 0;

			for (int loopIdx=0; loopIdx<adRowCt; loopIdx++) {

				String rowData[] = advancedSearchItems[loopIdx].split("::");
				String property = rowData[0];
				String condition = rowData[1];
				String propValue = rowData[2];	

				List<WebElement> adSearchBars = this.searchRow.findElements(By.cssSelector("div[id='searchAdvancedConditions']" +
						">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

				WebElement adSearchBar = adSearchBars.get(loopIdx);
				ActionEventUtils.click(driver, adSearchBar);
				//adSearchBar.click(); //Clicks Additional search row

				//Sets Property in the Additional search condition
				//------------------------------------------------
				WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']"));
				WebElement searchTextBoxField = searchTextBox.findElement(By.cssSelector("input[placeholder='Select property']"));
				if(property != "") {
					if(searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq") != null) {
						if(!searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq").equals(property)) {
							//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
							//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open

							searchTextBoxField.click();
							searchTextBoxField.clear();
							searchTextBoxField.sendKeys(property);
							Utils.fluentWait(this.driver);

							while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
								searchTextBox.sendKeys(Keys.END);
								searchTextBox.sendKeys(Keys.BACK_SPACE);
								snooze++;
							}
							Utils.fluentWait(this.driver);

							searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
							noOfElmnts = searchelements.size();
							for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
								if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(property.toUpperCase())) {
									if(browser.equalsIgnoreCase("safari"))
										searchelements.get(itemIdx).click();
									else
										ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
									//	searchelements.get(itemIdx).click(); //Clicks the item in search type
									break;
								}
							}

							if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
								throw new Exception("Item (" + property + ") does not exists in the list.");
						}
					}
					else {
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
						//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 

						searchTextBoxField.click();
						searchTextBoxField.clear();
						searchTextBoxField.sendKeys(property);
						Utils.fluentWait(this.driver);

						while(snooze < 60 &&  !searchTextBoxField.getAttribute("value").equalsIgnoreCase(property) && !searchTextBoxField.getAttribute("value").equals("")) {
							searchTextBox.sendKeys(Keys.END);
							searchTextBox.sendKeys(Keys.BACK_SPACE);
							snooze++;
						}
						Utils.fluentWait(this.driver);

						searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
						noOfElmnts = searchelements.size();
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
							if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(property.toUpperCase())) {
								if(browser.equalsIgnoreCase("safari"))
									searchelements.get(itemIdx).click();
								else
									ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
								//searchelements.get(itemIdx).click(); //Clicks the item in search type
								Utils.fluentWait(this.driver);
								break;
							}
						}
						if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
							throw new Exception("Item (" + property + ") does not exists in the list.");
					}
				}
				//Sets Condition in the Additional search condition
				//--------------------------------------------------
				searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchConditionType']"));
				ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")));
				//searchTextBox.findElement(By.cssSelector("span[class*='ffb-arrow']")).click(); //Clicks down arrow to open 
				Utils.fluentWait(this.driver);


				searchelements = searchTextBox.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
				noOfElmnts = searchelements.size();

				for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(condition.toUpperCase())) {
						if(browser.equalsIgnoreCase("safari"))
							searchelements.get(itemIdx).click();
						else
							ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
						//searchelements.get(itemIdx).click(); //Clicks the item in search type
						break;
					}

				if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
					throw new Exception("Item (" + condition + ") does not exists in the list.");

				//Sets Value in the Additional search condition
				//---------------------------------------------
				if(propValue.equals("")) {
					try {
						searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " +
								"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));
						searchTextBox.clear();
					}
					catch (Exception e1) {
						return;
					}
				}
				else
					this.setAdvancedSearchValue(propValue, adSearchBar);

			} //End for loop

			Log.event("setAdditionalConditions : Additional conditions are selected.", StopWatch.elapsedTime(startTime));

		}//End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdditionalConditions  : " +e.getMessage(), e);
		} //End catch

	} //End function setAdditionalConditions

	/**
	 * isPropertyExists : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public Boolean isPropertyExists(String property) throws Exception {

		try {

			this.clickAdvancedSearch(true);
			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0;

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar
			ActionEventUtils.click(driver, adSearchBar);
			//adSearchBar.click(); //Clicks Additional search row
			Utils.fluentWait(driver);

			//Sets Property in the Additional search condition
			//------------------------------------------------
			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']"));
			ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")));
			//searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open
			Utils.fluentWait(this.driver);
			searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
			noOfElmnts = searchelements.size();
			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
				if (searchelements.get(itemIdx).getAttribute("val").toUpperCase().trim().equals(property.toUpperCase())) 
					return true;
			}

			return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.isPropertyExists  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	public void typeInAdvancedSearchValue(String value) throws Exception {

		try {

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div[id*='flexLookupCtr']>input[id*='input'], " +
					"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

			if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
				return;

			Utils.fluentWait(driver);
			ActionEventUtils.click(driver, searchTextBox);
			//		searchTextBox.click();  
			Utils.fluentWait(driver);
			searchTextBox.sendKeys(value);
			return;
		}

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.typeInAdvancedSearchValue  : " +e.getMessage(), e);
		} //End catch
	}

	public void setAdvancedSearchValue(String value) throws Exception {

		try {

			int snooze = 0;

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div>input[id*='input'],div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

			if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
				return;
			ActionEventUtils.click(driver, searchTextBox);
			//searchTextBox.click();

			if(value == "") {
				for(int count = 0; count < 50; count++)
					searchTextBox.sendKeys(Keys.BACK_SPACE);
				return;
			}

			String propValClassName = searchTextBox.getAttribute("class").toString();

			if (propValClassName.toUpperCase().contains("FFB-SEARCH")) { //Selects the property value if it is dropdown box
				//ActionEventUtils.click(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")));
				//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")).click();  
				searchTextBox.click();
				searchTextBox.clear();
				searchTextBox.sendKeys(value);
				Utils.fluentWait(driver);

				while(snooze < 60 &&  !searchTextBox.getAttribute("value").equalsIgnoreCase(value) && !searchTextBox.getAttribute("value").equals("")) {
					searchTextBox.sendKeys(Keys.END);
					searchTextBox.sendKeys(Keys.BACK_SPACE);
					snooze++;
				}
				Utils.fluentWait(this.driver);

				List<WebElement> searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div"));
				int noOfElmnts = searchelements.size();
				int itemIdx=0;
				for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
					if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(value.toUpperCase())) {
						if(browser.equalsIgnoreCase("safari"))
							searchelements.get(itemIdx).click();
						else
							ActionEventUtils.moveToElementAndClick(driver,searchelements.get(itemIdx));
						//searchelements.get(itemIdx).click();
						break;
					}

				if (itemIdx >= noOfElmnts)
					throw new Exception("Item (" + value + ") does not exists in the list.");	
			}
			else if (propValClassName.toUpperCase().contains("HASDATEPICKER")) { //Selects the property value if it date picker
				ActionEventUtils.moveToElementAndClick(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")));
				//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")).click();
				DatePicker datePicker = new DatePicker(this.driver);
				datePicker.SetCalendar(value);				
			}
			else if (propValClassName.toUpperCase().contains("TYPEDVALUETEXTBOX")) {  //Enters the property value if it value box
				searchTextBox.clear();
				searchTextBox.sendKeys(value);
			}
		}
		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdvancedSearchValue  : " +e.getMessage(), e);
		} //End catch
	}

	public void setAdvancedSearchValue(String value, int index) throws Exception {

		try {

			int snooze = 0;

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

			List<WebElement> rows = adSearchBar.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='ddlSearchAdvanced searchRow']"));
			adSearchBar = rows.get(index-1);

			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div>input[id*='input'],div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

			if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
				return;
			ActionEventUtils.click(driver, searchTextBox);
			//searchTextBox.click();

			if(value == "") {
				for(int count = 0; count < 50; count++)
					searchTextBox.sendKeys(Keys.BACK_SPACE);
				return;
			}

			String propValClassName = searchTextBox.getAttribute("class").toString();

			if (propValClassName.toUpperCase().contains("FFB-SEARCH")) { //Selects the property value if it is dropdown box
				searchTextBox.click();
				searchTextBox.clear();
				searchTextBox.sendKeys(value);
				//ActionEventUtils.click(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")));
				//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")).click();  
				Utils.fluentWait(driver);

				while(snooze < 60 &&  !searchTextBox.getAttribute("value").equalsIgnoreCase(value) && !searchTextBox.getAttribute("value").equals("")) {
					searchTextBox.sendKeys(Keys.END);
					searchTextBox.sendKeys(Keys.BACK_SPACE);
					snooze++;
				}
				Utils.fluentWait(this.driver);

				List<WebElement> searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div"));
				int noOfElmnts = searchelements.size();
				int itemIdx=0;
				for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
					if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equals(value.toUpperCase())) {
						if(browser.equalsIgnoreCase("safari"))
							searchelements.get(itemIdx).click();
						else
							ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
						//	searchelements.get(itemIdx).click();
						break;
					}

				if (itemIdx >= noOfElmnts)
					throw new Exception("Item (" + value + ") does not exists in the list.");	
			}
			else if (propValClassName.toUpperCase().contains("HASDATEPICKER")) { //Selects the property value if it date picker
				ActionEventUtils.moveToElementAndClick(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")));
				//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")).click();
				DatePicker datePicker = new DatePicker(this.driver);
				datePicker.SetCalendar(value);				
			}
			else if (propValClassName.toUpperCase().contains("TYPEDVALUETEXTBOX")) {  //Enters the property value if it value box
				searchTextBox.clear();
				searchTextBox.sendKeys(value);
			}
		}
		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdvancedSearchValue  : " +e.getMessage(), e);
		} //End catch
	}

	public void setAdvancedSearchValue(String value, WebElement adSearchBar) throws Exception {

		try {

			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class*='searchValueTypeHolder']>div>input[id*='input'],div[class*='searchValueTypeHolder']>input"));

			if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
				return;

			ActionEventUtils.click(driver, searchTextBox);
			//	searchTextBox.click();

			if(value == "") {
				for(int count = 0; count < 50; count++)
					searchTextBox.sendKeys(Keys.BACK_SPACE);
				return;
			}

			String propValClassName = searchTextBox.getAttribute("class").toString();

			if (propValClassName.toUpperCase().contains("FFB-SEARCH")) { //Selects the property value if it is dropdown box
				ActionEventUtils.click(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")));
				//	searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")).click();  
				Utils.fluentWait(driver);

				Utils.fluentWait(driver);
				List<WebElement> searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div"));
				int noOfElmnts = searchelements.size();
				int itemIdx=0;
				for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
					if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equals(value.toUpperCase())) {
						if(browser.equalsIgnoreCase("safari"))
							searchelements.get(itemIdx).click();
						else
							ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
						//searchelements.get(itemIdx).click();
						break;
					}

				if (itemIdx >= noOfElmnts)
					throw new Exception("Item (" + value + ") does not exists in the list.");	
			}
			else if (propValClassName.toUpperCase().contains("HASDATEPICKER")) { //Selects the property value if it date picker
				ActionEventUtils.moveToElementAndClick(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")));
				//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")).click();
				DatePicker datePicker = new DatePicker(this.driver);
				datePicker.SetCalendar(value);				
			}
			else if (propValClassName.toUpperCase().contains("TYPEDVALUETEXTBOX")) {  //Enters the property value if it value box
				searchTextBox.clear();
				searchTextBox.sendKeys(value);	
			}
		}
		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdvancedSearchValue  : " +e.getMessage(), e);
		} //End catch
	}

	public List<String> getAdvancedSearchValues() throws Exception {

		try {

			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div[id*='flexLookupCtr']>input[id*='input'], " +
					"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

			if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
				return null;
			ActionEventUtils.click(driver,searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")));
			//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='ffb-arrow out']")).click();  
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			List<String> values = new ArrayList<String>();
			List<WebElement> searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div[class*='row']"));
			int noOfElmnts = searchelements.size();
			int itemIdx=0;
			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
				values.add(searchelements.get(itemIdx).getAttribute("val").replace("&nbsp;", " "));
			return values;	

		}
		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getAdvancedSearchValues  : " +e.getMessage(), e);
		} //End catch
	}

	/**
	 * getAdditionalConditions : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public String getAdditionalConditions() throws Exception {

		String conditions = ""; 

		try {
			WebElement value = null;
			this.clickAdvancedSearch(true);

			List<WebElement> rows = this.searchRow.findElements(By.cssSelector("div[id*='searchPropertyCriterion_']"));

			if(rows.size() == 1)
				return conditions;

			for(int count = 0; count < rows.size()-1; count++) {
				List<WebElement> fields = rows.get(count).findElements(By.cssSelector("div[class*='advancedSearchConditionDiv']"));
				for(int counter = 0; counter < fields.size(); counter++) {
					try {
						value = fields.get(counter).findElement(By.cssSelector("div[class*='ffb-sel']"));
						conditions = conditions + value.getAttribute("val").replaceAll("&nbsp;", " ").trim();
					}
					catch (Exception e1) {
						value = fields.get(counter);
						conditions = conditions + value.getText().replaceAll("&nbsp;", " ").trim();
					}

					if(counter < fields.size()-1)
						conditions = conditions+":";

				}

				if(count < rows.size()-2)
					conditions = conditions + "\n";
			}

			return conditions;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getAdditionalConditions  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * isValueFieldDisplayed : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public Boolean isValueFieldDisplayed(int row) throws Exception {

		try {
			this.clickAdvancedSearch(true);

			List<WebElement> rows = this.searchRow.findElements(By.cssSelector("div[id*='searchPropertyCriterion_']"));
			if(rows.get(row-1).findElement(By.cssSelector("div[class*='searchValueTypeHolder']>div[id*='flexLookupCtr']")).getAttribute("style").contains("display: none"))
				return false;
			else
				return true;

		} //End try

		catch (Exception e) {
			return true;
		} //End catch

	} //End function SetSearchType

	/**
	 * clickSearch : To click Search button in quick search pane
	 * @param none
	 * @return none
	 * @throws Exception
	 */
	public void clickSearch() throws Exception {

		final long startTime = StopWatch.startTime();

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {

			if(browser.equalsIgnoreCase("Edge")){
				Utils.fluentWait(driver);
				/*Actions action = new Actions(this.driver);
				action.doubleClick(this.searchRow.findElement(By.id("searchButton"))).build().perform();*/

				ActionEventUtils.click(driver,this.searchRow.findElement(By.id("searchButton")));
				Utils.fluentWait(driver);
				ActionEventUtils.click(driver,this.searchRow.findElement(By.id("searchButton")));

				/*((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); "
						+ "evt.initMouseEvent('dblclick', true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null); "
						+ "arguments[0].dispatchEvent(evt);", this.searchRow.findElement(By.id("searchButton")));*/


				Utils.fluentWait(driver);
			}
			else{
				Utils.fluentWait(driver);

				ActionEventUtils.click(driver,this.searchRow.findElement(By.id("searchButton")));
				Utils.fluentWait(driver);
			}
			//((JavascriptExecutor) driver).executeScript("arguments[0].click()", this.searchRow.findElement(By.id("searchButton")));


			/*this.searchRow.findElement(By.id("searchButton")).sendKeys(Keys.ENTER);
			this.searchRow.findElement(By.id("searchButton")).sendKeys(Keys.TAB);
			 */

			Log.event("clickSearch : Search button is clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.clickSearch  : " +e.getMessage(), e);
		} //End catch

	} //End function ClickSearch

	/**
	 * search : To perform search with basic options
	 * @param searchWord - Search key word (Eg : test)
	 * @param searchType - Search type (Eg : Search all objects)
	 * @return none
	 * @throws Exception
	 */
	public void search(String searchWord, String searchType) throws Exception {

		//Variable Declaration
		Boolean isOpen = false;

		try {

			String searchInType = this.getSearchInType();//Gets the search in type

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}

			if (searchType.equals(null) || searchType.equals("")) //Sets the search type
				this.setSearchType("Search all objects");
			else
				this.setSearchType(searchType);

			this.setSearchWord(searchWord, true); //Sets the search word without using autofill

			if (browser.equalsIgnoreCase("firefox") && !searchInType.equalsIgnoreCase(this.getSearchInType()))
				this.setSearchInType(searchInType);//Sets the search in type to the actual one

			this.clickSearch(); //Clicks Search button
			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.search  : " +e.getMessage(), e);
		} //End catch

		finally {

			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);

		}

	} //End function ClickSearch

	/**
	 * getSearchTypeOptions : To get all the available options with search type
	 * @param none
	 * @return Array of string with all the available search type
	 * @throws Exception
	 */
	public String[] getSearchTypeOptions() throws Exception {

		try {

			this.clickAdvancedSearch(true);
			ActionEventUtils.click(driver, this.searchRow.findElement(By.id("fbsearchObjectType_input")));
			//	this.searchRow.findElement(By.id("fbsearchObjectType_input")).click(); //Clicks Down Arrow to open search types		

			List<WebElement> searchelements = this.searchRow.findElements(By.className("row")); //Gets all the search types
			int noOfElmnts = searchelements.size();

			String [] availableOptions = new String[noOfElmnts];

			for (int itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				availableOptions[itemIdx] = searchelements.get(itemIdx).getText().toUpperCase().trim();

			ActionEventUtils.click(driver, this.searchRow.findElement(By.id("fbsearchObjectType_input")));
			//this.searchRow.findElement(By.id("fbsearchObjectType_input")).click(); //Clicks Down Arrow to open search types
			this.clickAdvancedSearch(false);
			return availableOptions;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchTypeOptions  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * getSearchInOptions : To get all the available options with search in type
	 * @param none
	 * @return Array of string with all the available search in type
	 * @throws Exception
	 */
	public String[] getSearchInOptions() throws Exception {

		try {
			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("div[id='searchIn_title']>span[id='searchIn_arrow']")));
			//this.searchRow.findElement(By.cssSelector("div[id='searchIn_title']>span[id='searchIn_arrow']")).click();

			List<WebElement> searchelements = this.searchRow.findElements(By.cssSelector("span[class='ddTitleText']")); //Gets all the search types
			int noOfElmnts = searchelements.size();
			String[] availableOptions = new String[noOfElmnts];			

			for (int itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				availableOptions[itemIdx] = searchelements.get(itemIdx).getText().trim();

			//this.searchRow.findElement(By.cssSelector("div[id='searchIn_title']>span[id='searchIn_arrow']")).click();
			ActionEventUtils.click(driver, this.searchRow.findElement(By.cssSelector("div[id='searchIn_title']>span[id='searchIn_arrow']")));
			return availableOptions;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.getSearchInOptions  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * searchOrNavigatetoView : To perform search or navigates to the specified view
	 * @param driver - Web driver
	 * @param viewToNavigate - View to navigate
	 * @param searchWord - Search word if any
	 * @return search type performed
	 * @throws Exception
	 */
	public static String searchOrNavigatetoView(WebDriver driver, String viewToNavigate, String searchWord) throws Exception {

		try {

			HomePage homePage = new HomePage(driver);
			//homePage.taskPanel.clickItem("Home");

			if (searchWord == null)
				searchWord = "";

			if (viewToNavigate.toUpperCase().equals("") || viewToNavigate == null)
				viewToNavigate = "Search all objects";

			if (viewToNavigate.toUpperCase().contains("SEARCH")) 
				homePage.searchPanel.search(searchWord, viewToNavigate); // Search for the documents
			else //Navigates to the specified view
				homePage.listView.navigateThroughView(viewToNavigate);

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in SearchPanel.searchOrNavigatetoView : "+e, e);
		} //End catch

		return viewToNavigate;

	} //End function searchOrNavigatetoView

	/*--------------------------------Functions required for smoke test cases--------------------------------------*/

	/**
	 * Description : Display 'Advanced Search' options
	 * @param driver
	 * @throws Exception 
	 */
	public void showAdvancedSearchOptions(WebDriver driver) throws Exception{
		try{

			//Verify if Advanced SearchIcon' displayed
			Utils.fluentWait(driver);
			WebElement advancedSearchIcon=driver.findElement(By.cssSelector("div[id='openSearchButton'][onclick*='OpenSearch()']"));
			//Click 'Advanced Search' icon 
			ActionEventUtils.click(driver, advancedSearchIcon);
			//advancedSearchIcon.click();
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='searchAdvanced']:not([style*='display: none;'])"))));
			//Verify if 'Advanced search options displayed
			if (!driver.findElement(By.cssSelector("div[id='searchAdvanced']:not([style*='display: none;'])")).isDisplayed()){
				Log.exception(new Exception("Due to some error, Unable to launch 'Advanced Search' options."),driver);
			}
		}
		catch(Exception e){
			if(e.getClass().toString().contains("NoSuchElementException")) {
				Log.exception(new Exception("'Advanced Search' icon not enabled (or) displayed."),driver);
			}
			else
				throw new Exception("Exception at searchPanel.showAdvancedSearchOptions : " + e);
		}
	}

	/**
	 * Description : Select 'Search options' from dropdown list
	 * @param driver
	 * @param object
	 * @return Search option selected
	 * @throws Exception 
	 */
	public String selectSearchOptionsUsingObject(WebDriver driver, MFilesObjectList object, String objName) throws Exception{
		final long startTime = StopWatch.startTime();
		String searchOptionSelected=null;

		/*		try{
			searchOptionSelected=driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).getAttribute("value");
			if (!searchOptionSelected.contains(objName)){
				try{
					driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).click();

				}
				catch(NoSuchElementException e){
					throw new Exception("Advanced Search options dropdownlist not displayed.");
				}
				Actions action=new Actions(driver);
				WebElement searchObject=driver.findElement(By.cssSelector("div[id='"+object.getValue()+"']"));

				try {
					action.moveToElement(driver.findElement(By.cssSelector("div[id='"+object.getValue()+"']"))).build().perform();
				}
				catch(Exception e1) {
					((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", searchObject);
				}
				searchObject.click();

				searchOptionSelected=driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).getAttribute("value");
			}
			else
				Log.event("Search Option is already selected as :"+searchOptionSelected,StopWatch.elapsedTime(startTime));

		}
		catch(Exception e){
			throw new Exception("Unable to find the Search criteria Element/Value in the dropdownlist.");

		}
		return searchOptionSelected;*/

		try{
			searchOptionSelected=driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).getAttribute("value");
			if (!searchOptionSelected.contains(objName)){
				try{
					ActionEventUtils.click(driver, driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")));
					//driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).click();
				}
				catch(NoSuchElementException e){
					throw new Exception("Advanced Search options dropdownlist not displayed.");
				}
				Thread.sleep(500);
				Actions action=new Actions(driver);
				action.moveToElement(driver.findElement(By.cssSelector("div[id='"+object.getValue()+"']"))).build().perform();
				WebElement searchObject=driver.findElement(By.cssSelector("div[id='"+object.getValue()+"']"));
				ActionEventUtils.click(driver, searchObject);
				//searchObject.click();

				searchOptionSelected=driver.findElement(By.cssSelector("input[id='fbsearchObjectType_input']")).getAttribute("value");
			}
			else
				Log.event("Search Option is already selected as :"+searchOptionSelected,StopWatch.elapsedTime(startTime));

		}
		catch(Exception e){
			throw new Exception("Unable to find the Search criteria Element/Value in the dropdownlist.");

		}
		return searchOptionSelected;
	}

	/**
	 * 
	 * @param driver
	 * @throws Exception 
	 */
	public void clickSearchBtn(WebDriver driver) throws Exception
	{
		try
		{
			WebElement searchBtn=driver.findElement(By.cssSelector("input[id='searchButton']"));
			ActionEventUtils.click(driver, searchBtn);
			//	searchBtn.click();
			Utils.fluentWait(driver);
		}
		catch(Exception e) 	{
			throw new Exception("Exception at  SearchPanel.clickSearchBtn  : " +e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return true or false
	 * @throws Exception
	 */
	public Boolean isAdvancedSearchIconDisplayed() throws Exception {
		try {

			WebElement advancedSearchIcon =driver.findElement(By.cssSelector("div[id='openSearchButton'][onclick*='OpenSearch()']"));

			if(advancedSearchIcon.isDisplayed())
				return true;
		}
		catch(Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Unable to find Advanced Search Icon.");
		}
		return false;
	}

	/**
	 * getSearchType : To get the selected search type quick search pane
	 * @param none
	 * @return searchType - Search type (Eg : Search only: Customers)
	 * @throws Exception
	 */
	public Boolean isSearchTypeBoxEnabled() throws Exception {

		boolean isOpen = false;

		try {

			if (!this.isAdvancedSearchDisplayed()) { //Opens Advanced Search
				this.clickAdvancedSearch(true);
				isOpen = true;
			}
			Boolean flag =true;
			flag = this.searchRow.findElement(By.id("fbsearchObjectType_input")).isEnabled(); //Clicks Down Arrow to open search types		
			return flag;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.isSearchTypeBoxEnabled  : " +e.getMessage(), e);
		} //End catch

		finally {

			if (this.isAdvancedSearchDisplayed() && isOpen) //Closes Advanced Search if it is opened here
				this.clickAdvancedSearch(false);

		}

	} //End function getSearchType

	/**
	 * setAdditionalConditionsInRightPane : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public void setAdditionalConditionsInRightPane(String advancedSearchCdtn) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			String advancedSearchItems[] = advancedSearchCdtn.split("\n"); 
			int adRowCt = advancedSearchItems.length;


			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0;
			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));

			for (int loopIdx=0; loopIdx<adRowCt; loopIdx++) {

				String rowData[] = advancedSearchItems[loopIdx].split("::");
				String property = rowData[0];
				String condition = rowData[1];
				String propValue = rowData[2];	



				List<WebElement> adSearchBars = rightPaneSearch.findElements(By.cssSelector("tr[id^='searchPropertyCriterion']"));//Web element of search row advanced bar
				WebElement adSearchBar = adSearchBars.get(loopIdx);
				ActionEventUtils.click(driver, adSearchBar);
				Utils.fluentWait(driver);
				//adSearchBar.click(); //Clicks Additional search row

				//Sets Property in the Additional search condition
				//------------------------------------------------
				WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']>input[type='text']"));
				if(property != "") {
					ActionEventUtils.click(driver,searchTextBox);
					//searchTextBox.click();
					Utils.fluentWait(driver);
					searchTextBox.sendKeys(property);
					Utils.fluentWait(driver);

					List <WebElement> propList = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div>div[class='listcontent']>div"));
					int i=0;

					for (i=0; i<propList.size(); i++) {
						System.out.println(propList.get(i).getText());
						if (propList.get(i).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim().equalsIgnoreCase(property)) {
							//	propList.get(i).click();
							if(browser.equalsIgnoreCase("safari"))
								propList.get(i).click();
							else
								ActionEventUtils.moveToElementAndClick(driver,propList.get(i));
							Utils.fluentWait(driver);
							break;
						}
					}

					if (i >= propList.size())
						throw new Exception("Property (" + property + ") does not exists.");

					/*if(searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq") != null) {
					if(!searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq").equals(property)) {
						searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
						Utils.fluentWait(this.driver);

						Utils.fluentWait(this.driver);
						searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
						noOfElmnts = searchelements.size();
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
							if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(property.toUpperCase())) {
								searchelements.get(itemIdx).click(); //Clicks the item in search type
								break;
							}
						}

						if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
							throw new Exception("Item (" + property + ") does not exists in the list.");
					}
				}
				else {
					searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click(); //Clicks down arrow to open 
					Utils.fluentWait(this.driver);

					Utils.fluentWait(this.driver);
					searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
					noOfElmnts = searchelements.size();
					for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
						if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(property.toUpperCase())) {
							searchelements.get(itemIdx).click(); //Clicks the item in search type
							break;
						}
					}

					if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
						throw new Exception("Item (" + property + ") does not exists in the list.");
				}*/
				}
				//Sets Condition in the Additional search condition
				//--------------------------------------------------
				searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchConditionType']"));
				ActionEventUtils.click(driver, searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")));
				//searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click(); //Clicks down arrow to open 
				Utils.fluentWait(this.driver);


				searchelements = searchTextBox.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
				noOfElmnts = searchelements.size();

				for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(condition.toUpperCase())) {
						if(browser.equalsIgnoreCase("safari"))
							searchelements.get(itemIdx).click();
						else
							ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
						Utils.fluentWait(driver);
						//searchelements.get(itemIdx).click(); //Clicks the item in search type
						break;
					}

				if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
					throw new Exception("Item (" + condition + ") does not exists in the list.");

				//Sets Value in the third triplet search condition
				//------------------------------------------------
				if(propValue.equals("")) {
					try {
						searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " +
								"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));
						searchTextBox.clear();
					}
					catch (Exception e1) {
						return;
					}
				}
				else
					try {

						//		 			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
						//						">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

						searchTextBox = adSearchBar.findElement(By.cssSelector("td[class='searchValueTypeHolder advancedSearchConditionDiv']>div>input[id*='input'],td[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

						if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
							return;
						ActionEventUtils.click(driver, searchTextBox);
						//searchTextBox.click();

						if(propValue == "") {
							for(int count = 0; count < 50; count++)
								searchTextBox.sendKeys(Keys.BACK_SPACE);
							return;
						}

						String propValClassName = searchTextBox.getAttribute("class").toString();

						if (propValClassName.toUpperCase().contains("FFB-SEARCH")) { //Selects the property value if it is dropdown box
							ActionEventUtils.click(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='new_ffb-arrow out']")));
							//	searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click();  
							Utils.fluentWait(driver);

							searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div"));
							noOfElmnts = searchelements.size();
							itemIdx=0;
							for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
								if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(propValue.toUpperCase())) {
									if(browser.equalsIgnoreCase("safari"))
										searchelements.get(itemIdx).click();
									else
										ActionEventUtils.moveToElementAndClick(driver, searchelements.get(itemIdx));
									Utils.fluentWait(driver);									
									//searchelements.get(itemIdx).click();
									break;
								}

							if (itemIdx >= noOfElmnts)
								throw new Exception("Item (" + propValue + ") does not exists in the list.");	
						}
						else if (propValClassName.toUpperCase().contains("HASDATEPICKER")) { //Selects the property value if it date picker
							ActionEventUtils.moveToElementAndClick(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")));
							//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")).click();
							DatePicker datePicker = new DatePicker(this.driver);
							datePicker.SetCalendar(propValue);				
						}
						else if (propValClassName.toUpperCase().contains("TYPEDVALUETEXTBOX")) {  //Enters the property value if it value box
							searchTextBox.clear();
							searchTextBox.sendKeys(propValue);
						}
					}

				catch (Exception e) {
					throw new Exception("Exception at  SearchPanel.setAdditionalConditionsInRightPane  : " +e.getMessage(), e);
				} //End catch
			}
			Log.event("setAdditionalConditions : Additional conditions are selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdditionalConditionsInRightPane  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * setAdditionalConditionsInRightPane : To set the Additional condition value
	 * @param property - Eg: Accepted
	 * @param condition - Eg: is 
	 * @param propValue - Eg: Yes  
	 * @return none
	 * @throws Exception
	 */
	public void setAdditionalConditionsInRightPane(String property, String condition, String propValue) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			List<WebElement> searchelements = new ArrayList<WebElement>();
			int noOfElmnts = 0;
			int itemIdx = 0;
			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			WebElement adSearchBar = rightPaneSearch.findElement(By.cssSelector("tr[id^='searchPropertyCriterion']")); //Web element of search row advanced bar
			ActionEventUtils.click(driver, adSearchBar);
			//adSearchBar.click(); //Clicks Additional search row

			//Sets Property in the Additional search condition
			//------------------------------------------------
			WebElement searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchExpression']>input[type='text']"));

			if (property != "") {

				//((JavascriptExecutor) driver).executeScript("arguments[0].click()",searchTextBox);
				ActionEventUtils.click(driver, searchTextBox);
				//	searchTextBox.click();
				searchTextBox.clear();
				searchTextBox.sendKeys(property);
				Utils.fluentWait(driver);

				List <WebElement> propList = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div>div[class='listcontent']>div[class*='ffb-match']"));
				//List <WebElement> propList = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
				int i=0;

				for (i=0; i<propList.size(); i++) {
					System.out.println(propList.get(i).getText());
					if (propList.get(i).getAttribute("val").replaceAll("&nbsp;", " ").replaceAll("\n", "").replaceAll("\u00A0", "").trim().equalsIgnoreCase(property)) {
						ActionEventUtils.moveToElementAndClick(driver,propList.get(i));
						Utils.fluentWait(driver);
						//((JavascriptExecutor) driver).executeScript("arguments[0].click()",propList.get(i));
						//propList.get(i).click();
						break;
					}
				}

				if (i >= propList.size())
					throw new Exception("Property (" + property + ") does not exists.");

				/*if(searchTextBox.findElement(By.cssSelector("tr[id*='']>td>div[id*='fbSearchProperties']>input[id*='input']")).getAttribute("pq") != null) {
					if(!searchTextBox.findElement(By.cssSelector("input[id*='input']")).getAttribute("pq").equals(property)) {
						searchTextBox.findElement(By.cssSelector("span[class='ffb-arrow out']")).click(); //Clicks down arrow to open 
						Utils.fluentWait(this.driver);

						Utils.fluentWait(this.driver);
						searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
						noOfElmnts = searchelements.size();
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
							if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(property.toUpperCase())) {
								searchelements.get(itemIdx).click(); //Clicks the item in search type
								break;
							}
						}

						if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
							throw new Exception("Item (" + property + ") does not exists in the list.");
					}
				}
				else {
					searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click(); //Clicks down arrow to open 
					Utils.fluentWait(this.driver);

					Utils.fluentWait(this.driver);
					searchelements = searchTextBox.findElement(By.className("listcontent")).findElements(By.cssSelector("div[id*='PV_']")); //Gets all the search types
					noOfElmnts = searchelements.size();
					for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) { //Loops to identify the instance of the item to be clicked 
						if (searchelements.get(itemIdx).getText().toUpperCase().trim().equals(property.toUpperCase())) {
							searchelements.get(itemIdx).click(); //Clicks the item in search type
							break;
						}
					}

					if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
						throw new Exception("Item (" + property + ") does not exists in the list.");
				}*/
			}


			//Sets Condition in the Additional search condition
			//--------------------------------------------------
			searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchConditionType']"));
			//searchTextBox.clear();
			//searchTextBox.sendKeys(condition);
			ActionEventUtils.click(driver,searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")));
			//searchTextBox.findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click(); //Clicks down arrow to open 
			Utils.fluentWait(this.driver);

			searchelements = searchTextBox.findElements(By.cssSelector("div[class*='row']")); //Gets all the search types
			noOfElmnts = searchelements.size();

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (searchelements.get(itemIdx).getAttribute("val").replaceAll("\n", "").replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").toUpperCase().trim().equals(condition.toUpperCase())) {
					/*JavascriptExecutor executor = (JavascriptExecutor)driver;
					executor.executeScript("arguments[0].click();",searchelements.get(itemIdx));*/
					ActionEventUtils.moveToElementAndClick(driver,searchelements.get(itemIdx));
					Utils.fluentWait(driver);
					//searchelements.get(itemIdx).click(); //Clicks the item in search type
					break;
				}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item to click
				throw new Exception("Item (" + condition + ") does not exists in the list.");


			//Sets Value in the third triplet search condition
			//------------------------------------------------
			if(propValue.equals("")) {
				try {
					searchTextBox = adSearchBar.findElement(By.cssSelector("div[class='searchValueTypeHolder advancedSearchConditionDiv']>div, " +
							"div[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));
					searchTextBox.clear();
				}
				catch (Exception e1) {
					return;
				}
			}
			else
				try {

					//		 			WebElement adSearchBar = this.searchRow.findElement(By.cssSelector("div[id='searchAdvancedConditions']" +
					//						">div[class='ddlSearchAdvanced searchRow']")); //Web element of search row advanced bar

					searchTextBox = adSearchBar.findElement(By.cssSelector("td[class='searchValueTypeHolder advancedSearchConditionDiv']>div>input[id*='input'],td[class='searchValueTypeHolder advancedSearchConditionDiv']>input"));

					if (!searchTextBox.isDisplayed()) //If Property value box is not displayed returns
						return;

					ActionEventUtils.click(driver, searchTextBox);	
					//searchTextBox.click();

					if(propValue == "") {
						for(int count = 0; count < 50; count++)
							searchTextBox.sendKeys(Keys.BACK_SPACE);
						return;
					}

					String propValClassName = searchTextBox.getAttribute("class").toString();

					if (propValClassName.toUpperCase().contains("FFB-SEARCH")) { //Selects the property value if it is dropdown box
						searchTextBox.click();
						searchTextBox.clear();
						searchTextBox.sendKeys(propValue);
						Utils.fluentWait(driver);
						//ActionEventUtils.click(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='new_ffb-arrow out']")));
						//	searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("span[class='new_ffb-arrow out']")).click();  
						searchelements = searchTextBox.findElement(By.xpath("..")).findElements(By.cssSelector("div[class='listcontent']>div"));
						noOfElmnts = searchelements.size();
						itemIdx=0;
						for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)
							if (searchelements.get(itemIdx).getAttribute("val").replaceAll("&nbsp;", " ").replaceAll("\n", "").replaceAll("\u00A0", "").toUpperCase().trim().equals(propValue.toUpperCase())) {
								/*JavascriptExecutor executor = (JavascriptExecutor)driver;
								executor.executeScript("arguments[0].click();",searchelements.get(itemIdx));*/
								ActionEventUtils.moveToElementAndClick(driver,searchelements.get(itemIdx));
								Utils.fluentWait(driver);
								//	searchelements.get(itemIdx).click();
								break;
							}

						if (itemIdx >= noOfElmnts)
							throw new Exception("Item (" + propValue + ") does not exists in the list.");	
					}
					else if (propValClassName.toUpperCase().contains("HASDATEPICKER")) { //Selects the property value if it date picker
						ActionEventUtils.moveToElementAndClick(driver, searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")));
						//searchTextBox.findElement(By.xpath("..")).findElement(By.cssSelector("input[class='searchTypedValue typedValueDatePicker hasDatepicker']")).click();
						DatePicker datePicker = new DatePicker(this.driver);
						datePicker.SetCalendar(propValue);				
					}
					else if (propValClassName.toUpperCase().contains("TYPEDVALUETEXTBOX")) {  //Enters the property value if it value box
						searchTextBox.clear();
						searchTextBox.sendKeys(propValue);
					}
				}
			catch (Exception e) {
				throw new Exception("Exception at  SearchPanel.  : " +e.getMessage(), e);
			} //End catch

			Log.event("setAdditionalConditions : Additional conditions are selected.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.setAdditionalConditionsInRightPane  : " +e.getMessage(), e);
		} //End catch

	} //End function SetSearchType

	/**
	 * clickRightPaneSearchButton : To click Search button in right search pane
	 * @param none
	 * @return none
	 * @throws Exception
	 */
	public void clickRightPaneSearchButton() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			WebElement rightPaneSearch = driver.findElement(By.id("rightPaneSearch"));
			//Actions action = new Actions(this.driver);
			//action.doubleClick(this.searchRow.findElement(By.id("searchButton"))).build().perform();
			rightPaneSearch.findElement(By.cssSelector("input[class='searchButton'][type='submit']")).sendKeys(Keys.ENTER);
			Utils.fluentWait(driver);
			rightPaneSearch.findElement(By.cssSelector("input[class='searchButton'][type='submit']")).sendKeys(Keys.TAB);
			Log.event("clickSearch : Search button is clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at  SearchPanel.clickRightPaneSearchButton : "+e);
		} //End catch

	} //End function ClickSearch

	/**
	 * Verify the Metadata tab is selected as default in Search view
	 * @return true if Metadata tab is selected; false if not selected in search view
	 * @throws Exception 
	 */
	public boolean isMetadataTabSelected() throws Exception{

		try{
			WebElement metadata = driver.findElement(By.cssSelector("div[id='metaCard'][aria-hidden='false']"));
			if(metadata.isDisplayed()){
				return true;
			}
			else
				return false;
		}//End try
		catch(Exception e){
			throw new Exception("Exception at  SearchPanel.isMetadataTabSelected : " +e.getMessage(), e);
		}//End catch
	}

	public void quickSearch(String searchWord) throws Exception{

		Log.message("Making quick search using searchword ('" + searchWord + "').");

		this.setSearchWord(searchWord, true);
		this.clickSearch();
	}


} //End class SearchPanel
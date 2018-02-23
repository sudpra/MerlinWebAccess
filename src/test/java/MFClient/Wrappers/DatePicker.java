package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class DatePicker {
	
	WebDriver driver;
	
	@FindBy(how=How.ID, using="ui-datepicker-div")
	public WebElement dateTimePicker;
	
	/**
	 * DatePicker : Constructor to instantiate DatePicker
	 */
	public DatePicker(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(this.driver, this);
	}
	
	/**
	 * DatePickerExists : This method is to Check if date time picker is enabled
	 * @return true if date picker exists; false if date picker does not exists
	 */
	public Boolean DatePickerExists() {
		
		//Variable Declaration
		Boolean isExists = false; //Stores return value
		
		try {
			
			isExists = this.dateTimePicker.isDisplayed() && this.dateTimePicker.isEnabled();
			
		} //End try
		
		catch (Exception e) {
			throw e;	
		} //End catch
	
		return isExists;
		
	} //End DatePickerExists
	
	/**
	 * SelectYear : This method Selects the year from the calendar
	 * @example 2005
	 */
	public void SelectYear(String yearToSelect) throws Exception {
	
		try {
			
			//Variable Declaration
			WebElement yearSelector; //Stores the web-element year selector
			List<WebElement> yearOptions; //Stores all the elements that contains year link
			List<String> years = new ArrayList<String>();
			int snooze = 0;
			int optionCount; //Stores the number of options that are available to select the year
			int itemIdx; //Stores the item index
						
			while(snooze < 50) {
				yearSelector = this.dateTimePicker.findElement(By.cssSelector("select[class='ui-datepicker-year']")); //year selector Web element
				yearOptions = yearSelector.findElements(By.cssSelector("option")); //Gets all the links with the year to select
				optionCount = yearOptions.size(); //Gets the number of years in the list
				years.clear();
				for (itemIdx=0; itemIdx<optionCount; itemIdx++)  //Loops to identify the web-element of the year to be clicked
					years.add(yearOptions.get(itemIdx).getText().toUpperCase().trim());
				
				if(years.indexOf(yearToSelect) != -1) {
					new Select(yearSelector).selectByVisibleText(yearToSelect);
					//yearOptions.get(years.indexOf(yearToSelect)).click();
					return;
				}
				else {
					if(Integer.parseInt(yearToSelect) < Integer.parseInt(years.get(0)))
						new Select(yearSelector).selectByVisibleText(yearOptions.get(0).getText());
					else if(Integer.parseInt(yearToSelect) > Integer.parseInt(years.get(years.size()-1)))
						new Select(yearSelector).selectByVisibleText(yearOptions.get(years.size()-1).getText());
				}
				snooze++;
			}
			
		} //End try
		
		catch (Exception e) {
			throw e;
		} //End catch
		
	} //End SelectYear
	
	/**
	 * SelectMonth : This method Selects the month from the calendar
	 * @example Mar
	 */
	public void SelectMonth(String monthToSelect) throws Exception {
			
		try {
			
			//Variable Declaration
			WebElement monthSelector; //Stores the web-element month selector
			/*List<WebElement> monthOptions; //Stores all the elements that contains month link
			int optionCount; //Stores the number of options that are available to select the month
			int itemIdx; //Stores the item index
*/			
			monthSelector = this.dateTimePicker.findElement(By.cssSelector("select[class='ui-datepicker-month']")); //Month selector Web element
			new Select(monthSelector).selectByVisibleText(monthToSelect);
			/*monthOptions = monthSelector.findElements(By.cssSelector("option")); //Gets all the links with the month to select
			optionCount = monthOptions.size();  //Gets the number of months in the list
			
			for (itemIdx=0; itemIdx<optionCount; itemIdx++)  //Loops to identify the web-element of the month to be clicked
				if (monthOptions.get(itemIdx).getText().toUpperCase().trim().equals(monthToSelect.toUpperCase())) {
					monthOptions.get(itemIdx).click(); //Clicks the month in date selector
					return;
				}
				
			if (itemIdx >= optionCount) //Checks for the existence of the month to click
				throw new Exception("Month (" + monthToSelect + ") does not exists in the list.");*/
			
		} //End try
		
		catch (Exception e) {
			throw e;		
		} //End catch
		
	} //End SelectMonth
	
	/**
	 * SelectDate : This method Selects the date from the calendar
	 * @example 25
	 */
	public void SelectDate(String dateToSelect) throws Exception {
			
		try {
			
			//Variable Declaration
			WebElement dateSelector; //Stores the web-element date selector
			List<WebElement> dateOptions; //Stores all the elements that contains date link
			int optionCount; //Stores the number of options that are available to select the date
			int itemIdx; //Stores the item index
			
			if (dateToSelect.substring(0, 1).matches("0"))
				dateToSelect = dateToSelect.substring(1);
			
			dateSelector = this.dateTimePicker.findElement(By.cssSelector("table[class='ui-datepicker-calendar']>tbody")); //Stores the date web element
			dateOptions = dateSelector.findElements(By.cssSelector("tr>td>a")); //Gets all the web elements with link of date
			optionCount = dateOptions.size(); //Number of available date for the month
			
			for (itemIdx=0; itemIdx<optionCount; itemIdx++)  //Loops to identify the instance of the date to be clicked
				if (dateOptions.get(itemIdx).getText().toUpperCase().trim().equals(dateToSelect.toUpperCase())) {
					ActionEventUtils.click(driver, dateOptions.get(itemIdx));
					//dateOptions.get(itemIdx).click(); //Clicks the date in date picker
					return;
				}
				
			if (itemIdx >= optionCount) //Checks for the existence of the date to click
				throw new Exception("Date (" + dateToSelect + ") does not exists in the calendar.");
			
		} //End try
		
		catch (Exception e) {
			throw e;
		} //End catch
		
	} //End SelectYear
	
	/**
	 * PreviousMonth : This method Clicks the previous month from the calendar
	 * @throws Exception 
	 * @example 25
	 */
	public void PreviousMonth() throws Exception {

		try {
						
			//Clicks previous button from the date selector
			ActionEventUtils.click(driver, this.dateTimePicker.findElement(By.cssSelector("a[class='ui-datepicker-prev ui-corner-all']")));
			//this.dateTimePicker.findElement(By.cssSelector("a[class='ui-datepicker-prev ui-corner-all']")).click();
						
		} //End try
		
		catch (Exception e) {
			throw e;		
		} //End catch
	} //End PreviousMonth
	
	/**
	 * NextMonth : This method Clicks the next month from the calendar
	 * @throws Exception 
	 * @example 25
	 */
	public void NextMonth() throws Exception {
	
		try {
						
			//Clicks next button from the date selector
			ActionEventUtils.click(driver, this.dateTimePicker.findElement(By.cssSelector("a[class='ui-datepicker-next ui-corner-all']")));
			ActionEventUtils.click(driver, this.dateTimePicker.findElement(By.cssSelector("a[class='ui-datepicker-next ui-corner-all']")));
			//this.dateTimePicker.findElement(By.cssSelector("a[class='ui-datepicker-next ui-corner-all']")).click();
						
		} //End try
		
		catch (Exception e) {
			throw e;			
		} //End catch
		
	} //End NextMonth
	
	/**
	 * SetCalendar : This method Selects the date, month, year from the calendar
	 * @example 03-Feb-2014
	 * @return None
	 */
	public void SetCalendar(String dd_mmm_yyyy) throws Exception {
			
		try {
			
			String[] splitDate = dd_mmm_yyyy.split("-");
			
			//Sets the Year in the calendar			
			this.SelectYear(splitDate[2]);
			
			//Sets the Month in the calendar
			this.SelectMonth(splitDate[1]);
			
			//Sets the date in the calendar
			this.SelectDate(splitDate[0]);
			
		} //End try
		
		catch (Exception e) {
			throw e;			
		} //End catch
		
	} //End SetCalendar
	
} //End class DatePicker

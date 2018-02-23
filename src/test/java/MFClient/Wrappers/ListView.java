package MFClient.Wrappers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;

import MFClient.Pages.HomePage;

public class ListView {

	//Variable Declaration
	WebDriver driver=null;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");

	/**
	 * PageFactory element initialization
	 */
	@FindBy(how=How.ID, using="listing")
	private WebElement listing; //Stores the instance of the List

	@FindBy(how=How.ID, using="listingTable")
	private WebElement listingTable; //Stores the instance of the List View Main table

	@FindBy(how=How.ID, using="headerTable")
	private WebElement listingHeader; //Stores the instance of the List View Main table

	@FindBy(how=How.CSS, using="table[id='mainTable']")
	private WebElement listingRows; //Stores the instance of the List View Main table

	/**
	 * ListView : Constructor to instantiate ListView class
	 * @param driver
	 * @throws Exception
	 */
	public ListView(final WebDriver driver) throws Exception {
		try {

			this.driver = driver;
			PageFactory.initElements(this.driver, this);

		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at ListView.ListView : "+ e.getMessage(), e);
		} //End catch

	} //End ListView

	/**
	 * isItemExists : To verify the existence of item in the list view
	 * @param itemName - Name of the item to check for existence
	 * @return true if item exists; false if not
	 * @throws Exception
	 */
	public Boolean isItemExists(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			Utils.fluentWait(driver);
			List<WebElement> listingRows = this.listing.findElements(By.cssSelector("table[id='mainTable'] tr[class*='listing-item tap'],table[id='mainTable'] tr[class*='listing-item tap row_highlight']"));
			int itemCnt = listingRows.size();
			String Item;

			boolean flag=false;//Initiate the boolean variable

			for(int itemIndx=0;itemIndx<itemCnt;itemIndx++){//Get the all Item name which is exists in the listview
				Item = listingRows.get(itemIndx).findElement(By.cssSelector("td>div[class='list_holder name_column']")).getText().replaceAll("&nbsp;", " ").trim();
				if(Item.equalsIgnoreCase(itemName)){//Verify if item exists in the listview
					flag = true;
					break;
				}//End if
				else
					flag = false;
			}//End for

			if(flag==true)//Verify if item exists in the list
			{
				Log.event("ListView.isItemExists : Item (" + itemName + ") exists in the list.",StopWatch.elapsedTime(startTime));
				return true;
			}

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			  if(this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td" +
					"/div[@class='list_holder name_column']/span[text()=" + itemName + "]")).isDisplayed()){

			if(this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td" +
					"/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]")).isDisplayed()) {

				Log.event("ListView.isItemExists : Item (" + itemName + ") exists in the list.",StopWatch.elapsedTime(startTime));
				return true;

			}*/
			else {
				Log.event("ListView.isItemExists : Item (" + itemName + ") does not exists in the list.",StopWatch.elapsedTime(startTime));
				return false;
			}

			/*			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through to identify the existence of item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) 
					return true;
			}

			 */			

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception ("Exception in ListView.isItemExists.", e);
		} //End catch

	} //End isItemExists

	/**
	 * clickItem : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public void shiftclickMultipleItemsByIndex(int startIndex, int endIndex)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			Actions builder = new Actions(driver);

			if (startIndex >= rowCt ||endIndex >= rowCt) //Checks if index to click is less than the number of objects listed
				throw new Exception("Item Index (" + startIndex + ") is greater the number of items (" + rowCt + ").");

			builder.keyDown(Keys.SHIFT)
			.click(itemRows.get(startIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")))
			.keyUp(Keys.SHIFT);
			builder.build().perform();
			Utils.fluentWait(driver);

			builder.keyDown(Keys.SHIFT)
			.click(itemRows.get(endIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")))
			.keyUp(Keys.SHIFT);
			builder.build().perform();

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the items in the index.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.shiftclickMultipleItemsByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function shiftclickMultipleItemsByIndex

	/**
	 * multiSelectByCtrlKey : To multi-select objects randomly using CTRL key
	 * @param noOfItems No of items to select randomly
	 * @return None
	 * @throws Exception
	 */
	public String multiSelectByCtrlKey(int noOfItems)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (noOfItems > rowCt)
				throw new SkipException("No of objects("+noOfItems+") to select using CTRL key is more than the objects("+rowCt+") in the list.");

			Integer[] randItemIds = new Integer[noOfItems];
			int counter = 0;
			int snooze = 0;
			List<Integer> list = Arrays.asList(randItemIds);

			for (counter=0; counter<noOfItems && snooze < noOfItems + 10; counter++) {

				int randNo = Utility.getRandomNumber(0, rowCt-1);

				if (list.contains(randNo))
					counter--;
				else
					randItemIds[counter] = randNo; 
			}

			if (counter < noOfItems)
				throw new Exception("There is very less object to multi-select it in randomly.");

			Arrays.sort(randItemIds);

			String selectedItems = "" ;

			Actions builder = new Actions(driver);

			for (counter=0; counter<noOfItems; counter++) {

				if (counter == 0)
					selectedItems = itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText();
				else
					selectedItems += "::" +itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText();

				builder.keyDown(Keys.CONTROL)
				.click(itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")))
				.keyUp(Keys.CONTROL);
				builder.build().perform();
				Utils.fluentWait(driver);
			}

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the items in the index.", StopWatch.elapsedTime(startTime));

			return selectedItems;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.multiSelectByCtrlKey : "+ e.getMessage(), e);
		} //End catch

	} //End function multiSelectByCtrlKey

	/**
	 * multiSelectRightClickByCtrlKey : To multi-select objects randomly using CTRL key and perform right click operation
	 * @param noOfItems No of items to select randomly
	 * @return None
	 * @throws Exception
	 */

	public String multiSelectRightClickByCtrlKey(int noOfItems)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (noOfItems > rowCt)
				throw new SkipException("No of objects("+noOfItems+") to select using CTRL key is more than the objects("+rowCt+") in the list.");

			Integer[] randItemIds = new Integer[noOfItems];
			int counter = 0;
			int snooze = 0;
			List<Integer> list = Arrays.asList(randItemIds);

			for (counter=0; counter<noOfItems && snooze < noOfItems + 10; counter++) {

				int randNo = Utility.getRandomNumber(0, rowCt-1);

				if (list.contains(randNo))
					counter--;
				else
					randItemIds[counter] = randNo; 
			}

			if (counter < noOfItems)
				throw new Exception("There is very less object to multi-select it in randomly.");

			Arrays.sort(randItemIds);

			String selectedItems = "" ;

			Actions builder = new Actions(driver);

			for (counter=0; counter<noOfItems; counter++) {

				if (counter == 0)
					selectedItems = itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText();
				else
					selectedItems += "::" +itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText();

				builder.keyDown(Keys.CONTROL)
				.click(itemRows.get(randItemIds[counter]).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")))
				.keyUp(Keys.CONTROL);
				builder.build().perform();
				Utils.fluentWait(driver);
			}

			/*	this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']")); 
			List<WebElement> Items = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap row_highlight'] span")); //Stores the web element with tr tag
			int selected = Items.size();

			for (int i=0; i<selected; i++) {

				selectedItems = Items.get(i).getText();
			}*/

			this.rightClickItemByIndex(randItemIds[noOfItems-1]);
			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the items in the index.", StopWatch.elapsedTime(startTime));

			return selectedItems;
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.multiSelectRightClickByCtrlKey : "+ e.getMessage(), e);
		} //End catch

	} //End function multiSelectRightClickByCtrlKey

	/**
	 * clickItem : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public void clickMultipleItems(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String[] items = itemName.split("\n");

			Actions builder = new Actions(driver);
			for(int counter = 0; counter < items.length; counter++) {
				if (!this.isItemExists(items[counter])) //Checks if item exists in the list
					throw new Exception("Item (" + items[counter] + ") does not exists in the list.");

				if(!this.isItemSelected(items[counter])) {
					this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
					/*builder.keyDown(Keys.CONTROL)
					.click(this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(items[counter]) + "]")))
					.keyUp(Keys.CONTROL);*/
					List<WebElement> element = listingRows.findElements(By.cssSelector("tr[class = 'listing-item tap']>td>div>span"));
					int itemCnt =  element.size();
					String item;

					for(int i =0;i<itemCnt;i++){
						item = element.get(i).getText().replaceAll("&nbsp;", " ").trim();	
						if(item.equalsIgnoreCase(items[counter])){

							if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element.get(i));

							builder.keyDown(Keys.CONTROL).click(element.get(i)).keyUp(Keys.CONTROL);
							builder.build().perform();
							Utils.fluentWait(driver);
							break;
						}
					}//End for
				}

			}

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the item(" + itemName + ").", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickMultipleItems : "+ e.getMessage(), e);
		} //End catch

	} //End function clickItem

	/**
	 * clickItem : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public void clickMultipleItems(String[] itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions builder = new Actions(driver);
			for(int counter = 0; counter < itemName.length; counter++) {
				if (!this.isItemExists(itemName[counter])) //Checks if item exists in the list
					throw new Exception("Item (" + itemName[counter] + ") does not exists in the list.");

				if(!this.isItemSelected(itemName[counter])) {
					this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
					/*builder.keyDown(Keys.CONTROL)
					.click(this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(items[counter]) + "]")))
					.keyUp(Keys.CONTROL);*/
					List<WebElement> element = listingRows.findElements(By.cssSelector("tr[class = 'listing-item tap']>td>div>span"));
					int itemCnt =  element.size();
					String item;

					for(int i =0;i<itemCnt;i++){
						item = element.get(i).getText().replaceAll("&nbsp;", " ").trim();	
						if(item.equalsIgnoreCase(itemName[counter])){
							builder.keyDown(Keys.CONTROL).click(element.get(i)).keyUp(Keys.CONTROL);
							builder.build().perform();
							Utils.fluentWait(driver);
							break;
						}
					}//End for
				}

			}

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the item(" + itemName + ").", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickMultipleItems : "+ e.getMessage(), e);
		} //End catch

	} //End function clickItem

	/**
	 * clickMultipleItemsByIndex : To click the multiple items with index using CTRL key
	 * @param index - Index of all the items
	 * @return None
	 * @throws Exception
	 */
	public void clickMultipleItemsByIndex(String index)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String[] items = index.split(",");

			Actions builder = new Actions(driver);
			for(int counter = 0; counter < items.length; counter++) {
				int itemIndex = Integer.parseInt(items[counter]);	
				String itemName = getItemNameByItemIndex(itemIndex);
				if (!this.isItemExists(itemName))//Checks if item exists in the list
					throw new Exception("Item (" + items[counter] + ") does not exists in the list.");
				if(!this.isItemSelected(itemName)) {
					this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
					List<WebElement> listingItems = this.listingRows.findElements(By.cssSelector("tr[class = 'listing-item tap']>td>div"));
					for (int i=0; i < listingItems.size(); i++) {
						if(listingItems.get(i).getText().equalsIgnoreCase(itemName)){
							builder.keyDown(Keys.CONTROL)
							.click(listingItems.get(i))
							.keyUp(Keys.CONTROL);
							builder.build().perform();
							Utils.fluentWait(driver);
						}
					}
				}
			}

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the item(" + items + ").", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickMultipleItemsByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function clickItem

	/**
	 * clickItem : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public Boolean clickItem(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				return false;

			/*if(!this.isItemSelected(itemName)) {
				this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
				this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]")).click();
				System.out.println("Check out to me is clicked");
			}*/

			if(!this.isItemSelected(itemName)) {
				List<WebElement> listingRows = this.listing.findElements(By.cssSelector("table[id='mainTable'] tr[class*='listing-item tap']"));
				int itemCnt = listingRows.size();
				String Item;

				for(int itemIndx=0;itemIndx<itemCnt;itemIndx++){//Get the all Item name which is exists in the listview
					Item = listingRows.get(itemIndx).findElement(By.cssSelector("td>div[class='list_holder name_column']")).getText().replaceAll("&nbsp;", " ").trim();
					if(Item.equalsIgnoreCase(itemName)){
						//	listingRows.get(itemIndx).findElement(By.cssSelector("td>div[class='list_holder name_column']")).click();

						if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", listingRows.get(itemIndx));

						ActionEventUtils.click(driver, listingRows.get(itemIndx).findElement(By.cssSelector("td>div[class='list_holder name_column']")));
						break;
					}
				}
			}

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap']," +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					itemSpan.click();					
					break;
				}

			} //End for
			 */			
			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItem : Clicked the item(" + itemName + ").", StopWatch.elapsedTime(startTime));

			return (this.isItemSelected(itemName)); //	Checks if item is selected

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickItem : "+ e.getMessage(), e);
		} //End catchf

	} //End function clickItem

	/**
	 * clickItemByIndex : To click the item in the specified index
	 * @param itemIndex - Index of an item
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public Boolean clickItemByIndex(int itemIndex)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isColumnExists(Caption.Column.ColumnName.Value))
				throw new Exception("Column " + Caption.Column.ColumnName.Value + ") does not exists.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt) //Checks if index to click is less than the number of objects listed
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
			//itemSpan.click();
			ActionEventUtils.click(driver,itemSpan);

			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItemByIndex : Clicked the index (" + itemIndex + ").", StopWatch.elapsedTime(startTime));

			return (this.isItemSelectedByIndex(itemIndex));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickItemByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function ClickItemByIndex

	/**
	 * clickThumbnailItemByIndex : To click the item in the specified index
	 * @param itemIndex - Index of an item
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public Boolean clickThumbnailItemByIndex(int itemIndex)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			List<WebElement> itemRows = this.listingTable.findElements(By.cssSelector("div[class='thumbDiv']>div>div[class*='list_thumnail_content tap']"));

			if(itemRows.size() == 0)
				itemRows = this.listingTable.findElements(By.cssSelector("div[class='thumbDiv']>div[class*='list_thumnail_content tap'"));

			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt) //Checks if index to click is less than the number of objects listed
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex).findElement(By.className("list_thumbItem"));
			//itemSpan.click();
			ActionEventUtils.click(driver,itemSpan);
			Utils.fluentWait(this.driver);
			Log.event("ListView.clickItemByIndex : Clicked the index (" + itemIndex + ").", StopWatch.elapsedTime(startTime));

			return (this.isThumbnailItemSelectedByIndex(itemIndex));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickThumbnailItemByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function clickThumbnailItemByIndex

	/**
	 * getThumbnailNameByItemIndex : To get name of an item at the specified index
	 * @param itemIndex - index of the item 
	 * @return Name of an item at the specified index
	 * @throws Exception
	 */
	public String getThumbnailNameByItemIndex(int itemIndex)throws Exception {

		try {

			List<WebElement> itemRows = this.listingTable.findElements(By.cssSelector("div[class='thumbDiv']>div[id*='groupHeader_']>div[class='list_thumnail_content tap'"));
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt)
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex);
			return (itemSpan.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getThumbnailNameByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function getThumbnailNameByItemIndex

	/**
	 * isThumbnailItemSelectedByIndex : To Check if item got selected after clicking or right clicking
	 * @param itemIdx - Index of the item 
	 * @return true if item is selected; false if item not selected
	 * @throws Exception
	 */
	public Boolean isThumbnailItemSelectedByIndex(int itemIdx)throws Exception {

		try {

			List<WebElement> itemRows = this.listingTable.findElements(By.cssSelector("div[class='thumbDiv']>div>div[class*='list_thumnail_content tap']"));

			if(itemRows.size() == 0)
				itemRows = this.listingTable.findElements(By.cssSelector("div[class='thumbDiv']>div[class*='list_thumnail_content tap'"));

			String className = itemRows.get(itemIdx).getAttribute("class").toString();

			if (className.toUpperCase().contains("HIGHLIGHT"))
				return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isThumbnailItemSelectedByIndex : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function isThumbnailItemSelectedByIndex

	/**
	 * expandRelations : To click the relation arrow of the specified item
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public void expandRelations(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.clickItem(itemName)) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap']," +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int snooze = 1;

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				snooze = 1;

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {

					WebElement element = itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']"));

					while (!isItemExpanded(itemName) && snooze < 3)
					{
						ActionEventUtils.click(driver, element);
						Utils.fluentWait(driver);
						snooze++;
					}

					break;
				}

			} //End for

			Utils.fluentWait(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.expandRelations : "+ e.getMessage(), e);
		} //End catch

		finally{
			Log.event("ListView.expandRelations : Expanded the item(" + itemName + ").", StopWatch.elapsedTime(startTime));
		}//End Finally

	} //End expandRelations


	/**
	 * isItemExpanded: Checks if the item is in expanded state or not in the list view
	 * @param itemName
	 * @return
	 * @throws Exception
	 */
	public boolean isItemExpanded(String itemName)throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap']," +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {

					WebElement arrowIcon = itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']"));

					String arrowImgSrcAttribute = arrowIcon.getAttribute("src");

					// Checking if object with only relations, relations + files, or only files is expanded
					if (arrowImgSrcAttribute.contains("RelatedObjectsExpanded.png") || arrowImgSrcAttribute.contains("FilesOnlyExpanded.png"))
						return true;//Item in expanded state
				}
			}

			return false;//Item not in expanded state

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isItemExpanded : "+ e.getMessage(), e);
		} //End catch

		finally{
			Log.event("ListView.isItemExpanded : Checked the item(" + itemName + ") is expanded or not.", StopWatch.elapsedTime(startTime));
		}//End Finally

	} //End isItemExpanded

	/**
	 * clickRelationArrowByIndex : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public void expandRelationsByIndex(int index)throws Exception {

		try {

			if (this.itemCount() < index + 1) //Checks if item exists in the list
				throw new Exception("The specified index does not exist in the list.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap']," +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			WebElement itemSpan = itemRows.get(index).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
			//itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']")).click();	
			ActionEventUtils.click(driver,itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']")));
			Utils.fluentWait(driver);					
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.expandRelationsByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function clickItem

	/**
	 * getArrowIcon : To click the specified item in the list
	 * @param itemName - Name of the item to be clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public String getArrowIcon(String itemName)throws Exception {

		String icon = "";
		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			icon = this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]")).findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']")).getAttribute("src");*/

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap']," +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) 
					icon = itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon']")).getAttribute("src");				

			} //End for

		} //End try

		catch (Exception e) {
			return "";
		} //End catch
		return icon;

	} //End function clickItem

	/**
	 * rightClickItem : To right click the item in the list view
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	public Boolean rightClickItem(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				throw new SkipException("Item (" + itemName + ") does not exists in the list.");

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			WebElement itemSpan = this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]"));
			Actions action = new Actions(this.driver);
			try {
				action.moveToElement(itemSpan, 10, itemSpan.getSize().height/2).contextClick().build().perform();
			}
			catch(Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", itemSpan);
			}

			Utils.fluentWait(this.driver);
			Log.event("ListView.rightClickItem : Item (" + itemName + ") is right clicked.", StopWatch.elapsedTime(startTime));
			return (this.isContextMenuExists() && this.isItemSelected(itemName));*/

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute


			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().trim().equalsIgnoreCase(itemName.toUpperCase())) {

					if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
						((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", itemSpan);

					//Actions action = new Actions(this.driver);
					//action.moveToElement(itemSpan, 10, itemSpan.getSize().height/2).contextClick().build().perform();
					ActionEventUtils.moveToElemAndRightClick(driver, itemSpan);
					Utils.fluentWait(this.driver);
					break;
				}

			} //End for
			Log.event("ListView.rightClickItem : Right Clicked the item (" + itemName + ").", StopWatch.elapsedTime(startTime));
			return (this.isContextMenuExists() && this.isItemSelected(itemName));
		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException") || e.getMessage().contains("does not exists in the list")) {
				Log.event("ListView.rightClickItem : Item (" + itemName + ") is not right clicked.", StopWatch.elapsedTime(startTime));
				return false;
			}	
			else
				throw new Exception("Exception at ListView.rightClickItem : "+ e.getMessage(), e);
		} //End catch

	} //End rightClickItem

	/**
	 * getWebElementOfItem : Gets the web element of item
	 * @param itemName - Name of an item
	 * @return WebElement
	 * @throws Exception
	 */
	public WebElement getWebElementOfItem(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();
		WebElement itemElement = null;

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					itemElement =  itemSpan;
					break;
				}

			} //End for

			Log.event("ListView.rightClickItem : Right Clicked the item (" + itemName + ").", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) {
				Log.event("ListView.rightClickItem : Item (" + itemName + ") is not right clicked.", StopWatch.elapsedTime(startTime));
				return null;
			}
			else
				throw new Exception("Exception at ListView.getWebElementOfItem : "+ e.getMessage(), e);
		} //End catch

		return itemElement;

	} //End getWebElementOfItem

	/**
	 * rightClickOnMultiSelectedItem : To right click the item in the list view
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	public Boolean rightClickOnMultiSelectedItem(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String[] items = itemName.split("\n");
			Actions builder = new Actions(driver);

			for(int counter = 0; counter < items.length; counter++) {

				WebElement elementToClick = this.getWebElementOfItem(items[counter]);

				if (elementToClick == null) //Checks if item exists in the list
					throw new Exception("Item (" + items[counter] + ") does not exists in the list.");

				if(!this.isItemSelected(elementToClick)) {
					this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
					builder.keyDown(Keys.CONTROL)
					.click(elementToClick)
					.keyUp(Keys.CONTROL);
					builder.build().perform();
					Utils.fluentWait(driver);
				} 
			}

			WebElement elementToClick = this.getWebElementOfItem(items[items.length-1]);
			builder.keyDown(Keys.CONTROL);

			try {
				builder.moveToElement(elementToClick, 10, elementToClick.getSize().height/2).contextClick().build().perform();
			}
			catch(Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", elementToClick);
			}

			return (this.isContextMenuExists()); 

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) {
				Log.event("ListView.rightClickItem : Item (" + itemName + ") is not right clicked.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else
				throw new Exception("Exception at ListView.rightClickOnMultiSelectedItem : "+ e.getMessage(), e);
		} //End catch

	} //End rightClickItem

	/**
	 * rightClickListview : To right click the list view
	 * @param itemName - Name of an item
	 * @return true if item is right clicked; false if item is not right clicked
	 * @throws Exception
	 */
	public void rightClickListview()throws Exception {

		WebElement listingview = null;
		final long startTime = StopWatch.startTime();

		try {

			// 	this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			listingview = driver.findElement(By.cssSelector("div[class='listing-container']"));
			Actions action = new Actions(this.driver);
			try {
				action.moveToElement(listingview, 10, listingview.getSize().height/2).contextClick().build().perform();
			}
			catch(Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", listingview);
			}

			Utils.fluentWait(driver);
			Log.event("Right clicked in listing view.", StopWatch.elapsedTime(startTime));


		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) {
				Log.event("ListView.rightClickItem : Item (" + listingview + ") is not right clicked.", StopWatch.elapsedTime(startTime));

			}
			else
				throw new Exception("Exception at ListView.rightClickListview : "+ e.getMessage(), e);
		} //End catch

	} //End rightClickItem

	/**
	 * right`ByIndex : To right click the item with the specified index
	 * @param itemIndex - Index of an item to be right clicked
	 * @return true if item in selected state; false if item not in selected state 
	 * @throws Exception
	 */
	public Boolean rightClickItemByIndex(int itemIndex)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isColumnExists(Caption.Column.ColumnName.Value))
				throw new Exception("Column " + Caption.Column.ColumnName.Value + ") does not exists.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt)
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']"));

			if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
				((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", itemSpan);

			Actions action = new Actions(this.driver);
			try {
				action.moveToElement(itemSpan, 10, itemSpan.getSize().height/2).contextClick().build().perform();
			}
			catch(Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", itemSpan);
			}
			Utils.fluentWait(this.driver);
			Log.event("ListView.rightClickItem : Right Clicked the index (" + itemIndex + ").", StopWatch.elapsedTime(startTime));
			return (this.isContextMenuExists() && this.isItemSelectedByIndex(itemIndex));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickItemByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function RightClickItemByIndex

	/**
	 * doubleClickItem : Double clicks the specified item in the list
	 * @param itemName - name of the item to be double clicked
	 * @return true if item is double clicked; false if item not is not double clicked 
	 * @throws Exception
	 */
	public Boolean doubleClickItem(String itemName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			List<WebElement> listItems = this.driver.findElements(By.cssSelector("table[id='mainTable'] tr[class='listing-item tap']"));

			for(int i=0; i<listItems.size(); i++){
				String text = listItems.get(i).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText().replaceAll("&nbsp;", " ").trim();
				if(text.equalsIgnoreCase(itemName)) {
					if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
						((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", listItems.get(i).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")));

					((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); "
							+ "evt.initMouseEvent('dblclick', true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null); "
							+ "arguments[0].dispatchEvent(evt);", listItems.get(i).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")));
					Utils.fluentWait(driver);
					return true;
				}
				//listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click();            	       	
			}



			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			WebElement itemSpan = this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]"));

			((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); "
					+ "evt.initMouseEvent('dblclick', true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null); "
					+ "arguments[0].dispatchEvent(evt);", itemSpan);

			Utils.fluentWait(this.driver);
			 */
			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					Actions action = new Actions(this.driver);
					action.doubleClick(itemSpan).build().perform();
					Utils.fluentWait(this.driver);
					Log.event("ListView.doubleClickItem : Double Clicked the item (" + itemName + ").", StopWatch.elapsedTime(startTime));
					return true;
				}

			} //End for
			 */			
			Log.event("ListView.doubleClickItem : Item (" + itemName + ") is double clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.doubleClickItem : "+ e.getMessage(), e);
		} //End catch


		return false;


	} //End function ClickItem

	/**
	 * doubleClickItemByIndex : Double clicks the item with the specified index
	 * @param itemIndex - index of the item to be double clicked
	 * @return true if item is double clicked; false if item not is not double clicked 
	 * @throws Exception
	 */
	public Boolean doubleClickItemByIndex(int itemIndex)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isColumnExists(Caption.Column.ColumnName.Value))
				throw new Exception("Column " + Caption.Column.ColumnName.Value + ") does not exists.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt)
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

			if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox"))
				((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", itemSpan);

			((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); "
					+ "evt.initMouseEvent('dblclick', true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null); "
					+ "arguments[0].dispatchEvent(evt);", itemSpan);

			Utils.fluentWait(this.driver);
			Log.event("ListView.doubleClickItemByIndex : Index (" + itemIndex + ") in list view is double clicked.", StopWatch.elapsedTime(startTime));
			return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.doubleClickItemByIndex : "+ e.getMessage(), e);
		} //End catch


	} //End function RightClickItemByIndex

	/**
	 * getItemNameByItemIndex : To get name of an item at the specified index
	 * @param itemIndex - index of the item 
	 * @return Name of an item at the specified index
	 * @throws Exception
	 */
	public String getItemNameByItemIndex(int itemIndex)throws Exception {

		try {

			/*if (!this.isColumnExists(Caption.Column.ColumnName.Value))
				throw new Exception("Column " + Caption.Column.ColumnName.Value + ") does not exists.");*/

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'], tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIndex >= rowCt)
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIndex).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
			System.out.println(itemSpan.getText());
			return (itemSpan.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getItemNameByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function getItemNameByItemIndex

	/**
	 * getItemIndexByItemName : To get index of an item
	 * @param itemName - Name of the item 
	 * @return Index of an item
	 * @throws Exception
	 */
	public int getItemIndexByItemName(String itemName)throws Exception {

		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx=0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));

				if (itemSpan.getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase()))
					return itemIdx;

			} //End for

			if (itemIdx >= rowCt) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getItemIndexByItemName : "+ e.getMessage(), e);
		} //End catch

		return -1;

	} //End function getItemIndexByItemName

	/**
	 * isItemSelected : To Check if item got selected after clicking or right clicking
	 * @param itemName - Name of the item 
	 * @return true if item is selected; false if item not selected
	 * @throws Exception
	 */
	public Boolean isItemSelected(String itemName)throws Exception {

		try {

			if (!this.isItemExists(itemName)) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody>tr[class='listing-item tap']"));
			WebElement itemRows = this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]"));
			String className = itemRows.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).getAttribute("class").toString();*/
			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']")); 
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			String className = null;
			int loopIdx = 0;

			for (loopIdx = 0; loopIdx<rowCt; loopIdx++)//Loops through all the items and clicks the specified item
				if (itemRows.get(loopIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']")).getText().trim().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					className = itemRows.get(loopIdx).getAttribute("class").toString();
					break;
				}

			if (loopIdx >= rowCt) 
				throw new Exception ("Item (" + itemName + ") does not exists in the list.");

			if (className.toUpperCase().contains("HIGHLIGHT")&& !className.equals(null))
				return true;


			/*if (className.toUpperCase().contains("HIGHLIGHT") && !itemRows.findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).getCssValue("background-color").equals("transparent"))
				return true;
			 */
		} //End try

		catch (Exception e) {
			if(e.getMessage().contains("Stale element reference"))
				return false;
			else 
				throw new Exception("Exception at ListView.isItemSelected : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function IsItemSelected

	/**
	 * isItemSelected : To Check if item got selected after clicking or right clicking
	 * @param itemName - Name of the item 
	 * @return true if item is selected; false if item not selected
	 * @throws Exception
	 */
	public Boolean isItemSelected(WebElement itemToCheck)throws Exception {

		try {

			String className = itemToCheck.getAttribute("class").toString();

			if (className.toUpperCase().contains("HIGHLIGHT")&& !className.equals(null))
				return true;

		} //End try

		catch (Exception e) {
			if(e.getMessage().contains("Stale element reference"))
				return false;
			else 
				throw new Exception("Exception at ListView.isItemSelected : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function IsItemSelected

	/**
	 * isItemSelectedByIndex : To Check if item got selected after clicking or right clicking
	 * @param itemIdx - Index of the item 
	 * @return true if item is selected; false if item not selected
	 * @throws Exception
	 */
	public Boolean isItemSelectedByIndex(int itemIdx)throws Exception {

		try {

			this.listingRows = this.listingTable.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			String className = itemRows.get(itemIdx).getAttribute("class").toString();

			if (className.toUpperCase().contains("HIGHLIGHT"))
				return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isItemSelectedByIndex : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function isItemSelectedByIndex

	/**
	 * getSelectedListViewItem : Gets the name of the selected item in the list
	 * @param none
	 * @return Name of the selected item
	 * @throws Exception
	 */
	public String getSelectedListViewItem()throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			WebElement itemRow = this.listingRows.findElement(By.cssSelector("tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			return(itemRow.findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getSelectedListViewItem : "+ e.getMessage(), e);
		} //End catch

	} //End function getSelectedListViewItem

	/**
	 * getSelectedListViewItems : Gets the name of the selected items in the list
	 * @param none
	 * @return Name of the selected items
	 * @throws Exception
	 */
	public String getSelectedListViewItems()throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRow = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap row_highlight']>td>div[class='list_holder name_column']>span")); //Stores the web element with tr tag

			String selectedItems = "";

			for (int i = 0; i < itemRow.size(); i++)
			{
				selectedItems += itemRow.get(i).getText().trim();

				if (!(i == (itemRow.size()-1)))
					selectedItems += "\n";
			}
			return selectedItems;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getSelectedListViewItems : "+ e.getMessage(), e);
		} //End catch

	} //End function getSelectedListViewItems

	/**
	 * getAllItemNames : Gets the name of all the items in the list
	 * @param none
	 * @return Name of the selected items
	 * @throws Exception
	 */
	public String[] getAllItemNames()throws Exception {

		try {

			if (!this.isColumnExists(Caption.Column.ColumnName.Value))
				throw new Exception("Column " + Caption.Column.ColumnName.Value + ") does not exists.");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			String[] nameOfItems = new String[rowCt];

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++)//Loops through to identify the existence of item
				nameOfItems[itemIdx] = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText();

			return nameOfItems;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getAllItemNames : "+ e.getMessage(), e);
		} //End catch

	} //End function getSelectedListViewItem

	/**
	 * itemCount : Gets the number of items displayed in the list
	 * @param none
	 * @return Number of items in the list
	 * @throws Exception
	 */
	public int itemCount()throws Exception {

		try {

			Utils.fluentWait(driver);
			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			Utils.fluentWait(driver);
			return(itemRows.size()); //Number of rows with specified attribute

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return 0;
			else
				throw new Exception ("Exception in ListView.itemCount", e);
		} //End catch

	} //End function ItemCount

	/**
	 * getColumnValueByItemName : Gets the value of the item of the specified column 
	 * @param itemName - Name of the item
	 * @param columnName - Name of the column
	 * @return - Value of the item in the specified column
	 * @throws Exception
	 */
	public String getColumnValueByItemName(String itemName, String columnName)throws Exception {

		try {

			if (!this.isColumnExists(columnName)) {//Checks for column existence and inserts the column if not
				this.insertColumn(columnName);

				if (!this.isColumnExists(columnName)) //Checks for column existence
					throw new Exception("Column (" + columnName + ") does not exists.");
			}
			int ind = this.getColumnOrder(columnName);
			ind = ind + 1;
			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops through all the items and clicks the specified item
				if (itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase()))
					break;

			return itemRows.get(itemIdx).findElement(By.cssSelector("td:nth-child(" + ind + ")")).getText();

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return "";
			else
				throw new Exception("Exception at ListView.getColumnValueByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End function GetColumnValue

	/**
	 * getColumnValueByItemIndex : Gets the value of the item of the specified column 
	 * @param itemName - Index of the item
	 * @param columnName - Name of the column
	 * @return - Value of the item in the specified column
	 * @throws Exception
	 */
	public String getColumnValueByItemIndex(int itemIndex, String columnName)throws Exception {

		try {


			if (!this.isColumnExists(columnName)) {//Checks for column existence and inserts the column if not
				this.insertColumn(columnName);

				if (!this.isColumnExists(columnName)) //Checks for column existence
					throw new Exception("Column (" + columnName + ") does not exists.");
			}
			int ind = this.getColumnOrder(columnName);
			ind = ind + 1;
			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'], tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag

			if (itemIndex >= itemRows.size())
				throw new Exception("Item Index (" + itemIndex + ") is greater the number of items.");

			return itemRows.get(itemIndex).findElement(By.cssSelector("td:nth-child(" + ind + ")")).getText().replaceAll("\n", "").trim();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnValueByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnValueByItemIndex

	/**
	 * getColumnValues : Gets all the values specified column
	 * @param columnName - Name of the column
	 * @return - Array of items of specified column
	 * @throws Exception
	 */
	public List<String> getColumnValues(String columnName)throws Exception {

		try {

			if (this.itemCount() == 0)
				throw new Exception ("List is empy to get column values");

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'], " +
					"tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int colnIdx = this.getColumnOrder(columnName)+1;

			List<String> columnValue = new ArrayList<String>();
			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops through all the items and clicks the specified item
				columnValue.add(itemRows.get(itemIdx).findElement(By.cssSelector("td:nth-child(" + colnIdx + ")")).getText());

			return columnValue;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnValues : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnValues

	/**
	 * getIconURLByItemName : Gets the icon URL of the item
	 * @param itemName - Name of the item
	 * @return - Icon URL of the specified item
	 * @throws Exception
	 */
	public String getIconURLByItemName(String itemName)throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops through all the items and clicks the specified item
				if (itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText().replaceAll("&nbsp;", " ").trim().toUpperCase().equalsIgnoreCase(itemName.toUpperCase()))
					break;

			return itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>div[class='list_overlay']")).getAttribute("style").toString();

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return "";
			else
				throw new Exception("Exception at ListView.getIconURLByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End function getIconURLByItemName

	/**
	 * getIconURLByItemIndex : Gets the icon URL of the item
	 * @param itemIdx - Index of the item
	 * @return - Icon URL of the specified item
	 * @throws Exception
	 */
	public String getIconURLByItemIndex(int itemIdx)throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag
			return itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>div[class='list_overlay']")).getAttribute("style").toString();

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return "";
			else
				throw new Exception("Exception at ListView.getIconURLByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function getIconURLByItemIndex

	/**
	 * navigateThroughView : Navigates through the view as specified
	 * @param viewName - Name of the view. (1. Documents>>By Class) 
	 * @return - true if navigated; false if not navigated
	 * @throws Exception
	 */
	public Boolean navigateThroughView(String viewName)throws Exception {

		try {

			String[] items = viewName.split(">>");
			TaskPanel taskPanel = new TaskPanel(this.driver);

			if (items.length == 1 && taskPanel.isItemExists(items[0])) { //Navigates to view if it is taskpanel
				taskPanel.clickItem(items[0]);
				return true;
			}

			for(int counter = 0; counter<items.length; counter++) { //Navigates to the view by double clicking in list view

				int snooze = 0;

				while(snooze < 30 && !this.isItemExists(items[counter].trim())) {//Snooze until the view gets displayed
					Thread.sleep(500);
					snooze++;
				}

				Utils.fluentWait(this.driver);

				if (!this.isItemExists(items[counter].trim()))
					return false;

				this.doubleClickItem(items[counter]);
				Utils.fluentWait(this.driver);

			}

			//	Utils.fluentWait(this.driver);
			return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.navigateThroughView : "+ e.getMessage(), e);
		} //End catch

	} //End function NavigateToView

	/**
	 * isColumnExists : Checks for the existence of the column
	 * @param colnName - Name of the column
	 * @return - true if column exists; false if not exists
	 * @throws Exception
	 */
	public Boolean isColumnExists(String colnName)throws Exception {

		try {

			List <WebElement> totalColns = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span"));
			int totalColnCt = totalColns.size();

			JavascriptExecutor js = (JavascriptExecutor) driver;
			WebElement element = this.driver.findElement(By.className("listing-container"));
			js.executeScript("arguments[0].scrollLeft = arguments[1];",element,0);
			int scrollIdx = 400;

			// Tracking the number of attempts of getting the column visible in view so attempting can be stopped if tried too many times.
			int attempts = 0;

			for( int i=0; i<totalColnCt; i++) {   
				Utils.fluentWait(driver);
				if (totalColns.get(i).isDisplayed() && colnName.equalsIgnoreCase(totalColns.get(i).getText()))
					return true;
				else if (!totalColns.get(i).isDisplayed()) {
					js = (JavascriptExecutor) driver;
					element = this.driver.findElement(By.className("listing-container"));
					js.executeScript("arguments[0].scrollLeft = arguments[1];",element, scrollIdx);
					scrollIdx = scrollIdx + 400;
					i--;

					// Already attempted 10 times to get the column to view and was not successful. Stopping attempting.
					if(attempts > 10){
						throw new Exception("isColumnExists attempted to get " + colnName + " visible but it failed.");
					}

					++attempts;
				}

			} //End for

			/*	List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) 
				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase()))
					return true;*/

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isColumnExists : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function IsColumnExists

	/**
	 * getColumnCount : Gets the number of column in the list
	 * @param None
	 * @return Number of columns in the list
	 * @throws Exception
	 */
	public int getColumnCount()throws Exception {

		try {

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			return (columnHeaders.size()); //Returns the number of column in the list

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnCount : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnCount

	/**
	 * getVisibleColumns : Gets all the visible columns in the column header
	 * @param None
	 * @return - Array of visible columns
	 * @throws Exception
	 */
	public String[] getVisibleColumns()throws Exception {

		try {

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			String visibleColumns[] = new String[rowCt];

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) 
				visibleColumns[itemIdx] = columnHeaders.get(itemIdx).getText();

			return visibleColumns;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getVisibleColumns : "+ e.getMessage(), e);
		} //End catch

	} //End function getVisibleColumns

	/**
	 * getColumnIndex : Gets the index of the column
	 * @param colnName - Name of the column
	 * @return - Index of a column
	 * @throws Exception
	 */
	public int getColumnIndex(String colnName)throws Exception {

		try {

			List<WebElement>columnHeaders = driver.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx = 0; itemIdx<rowCt; itemIdx++) 
				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase())) 
					break;

			String columnIndex = columnHeaders.get(itemIdx).getAttribute("id").toString();
			columnIndex = columnIndex.split("_")[columnIndex.split("_").length-1];
			return(Integer.parseInt(columnIndex));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnIndex

	/**
	 * getColumnOrder : Gets the order index of the column
	 * @param colnName - Name of the column
	 * @return - Index of a column
	 * @throws Exception
	 */
	public int getColumnOrder(String colnName)throws Exception {

		try {

			List<WebElement>columnHeaders = driver.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int colnIdx = 0;

			for (colnIdx = 0; colnIdx<rowCt; colnIdx++) 
				if (columnHeaders.get(colnIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase())) 
					break;

			return(colnIdx);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnOrder : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnIndex

	/**
	 * rightClickColumn : Right Clicks on the specified column in the column header
	 * @param colnName - Name of the column
	 * @return None
	 * @throws Exception
	 */
	public void rightClickColumn(String colnName)throws Exception {

		try {

			this.listingHeader = driver.findElement(By.id("headerTable"));
			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) 
				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase())) 
					break;

			WebElement columnToSelect = columnHeaders.get(itemIdx);
			Actions action = new Actions(this.driver);
			try {
				action.moveToElement(columnToSelect, 5, columnToSelect.getSize().height/2).contextClick().build().perform();
			}
			catch (Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", columnToSelect);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickColumn : "+ e.getMessage(), e);
		} //End catch

	} //End function rightClickColumn

	/**
	 * rightClickColumn : Right Clicks in the column header
	 * @param - None
	 * @return - None
	 * @throws Exception
	 */
	public void rightClickColumn()throws Exception {

		try {

			WebElement columnHeader = this.driver.findElement(By.cssSelector("div[id='headerBackground']")); //Stores the web element with tr tag

			if(browser.equalsIgnoreCase("Safari"))
			{
				ActionEventUtils.click(driver, columnHeader);
				ActionEventUtils.rightClick(driver, columnHeader);
			}
			else
			{
				Actions action = new Actions(this.driver);
				try {
					action.contextClick(columnHeader).build().perform();
				}
				catch (Exception e1) {
					((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", columnHeader);
				}
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickColumn : "+ e.getMessage(), e);
		} //End catch

	} //End function rightClickColumn

	/**
	 * rightClickOnEmptyHeader : Right Clicks on the empty place in column header
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void rightClickOnEmptyHeader()throws Exception {

		try {

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			WebElement lastColn = columnHeaders.get(columnHeaders.size() - 1); //Gets last column web element
			WebElement divider = lastColn.findElement(By.xpath("following-sibling::div")); //Divider web element
			int xOffset = lastColn.getSize().width + divider.getSize().width + 50; //X offset to move mouse
			int yOffset = lastColn.getSize().height/2; //Y offset to move mouse
			Actions action = new Actions(this.driver);
			//	action.moveToElement(lastColn, xOffset, yOffset).contextClick().build().perform(); //Perform right click on empty header
			try {
				action.moveToElement(lastColn, xOffset, yOffset).contextClick().build().perform(); //Perform right click on empty header
			}
			catch (Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", divider);
			}


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickOnEmptyHeader : "+ e.getMessage(), e);
		} //End catch

	} //End function rightClickOnEmptyHeader

	/**
	 * insertColumn : Inserts the column
	 * @param colnName - Name of the column
	 * @return true if column inserted; false if not inserted
	 * @throws Exception
	 */
	public Boolean insertColumn(String colnName)throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			boolean columnInserted = false;

			if(this.isColumnExists(colnName)) 
				return true;

			String[] standardColns = {"Name", "Type", "Object Type", "ID", "Version", "Relationships", "Size", "Checked Out To", "Checkout Time", "Date Created", "Date Modified"};

			this.rightClickColumn();

			this.mouseOverContextMenu("Insert Column");

			//List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div[id='insertColumns']"));

			//List<WebElement> contextMenuItems = this.driver.findElements(By.xpath("//ul[@class='menu-ul innerbox']/li/div[contains(., '-')]"));
			List<WebElement> contextMenuItems = driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div"));
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx = 0;

			if (Arrays.asList(standardColns).contains(colnName)) {
				this.mouseOverContextMenu("Standard Columns");

				//this.clickContextMenuItem(colnName);
				List<WebElement> columns = driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li[class*='columnsettings']>div"));

				//List<WebElement> columns = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div[id='insertColumns']"));
				String column;
				for(int i=0;i<columns.size();i++){
					column = columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).getText();
					//	column = columns.get(i).getText();
					if(column.equalsIgnoreCase(colnName)){
						//columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click();
						ActionEventUtils.click(driver,columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")));
						break;
					}
				}
				Utils.fluentWait(this.driver);
			}
			else {
				for (itemIdx = 0; itemIdx < contextMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					String colnGroup = contextMenuItems.get(itemIdx).getText();
					//System.out.println(colnGroup);
					if (contextMenuItems.get(itemIdx).isDisplayed() && !colnGroup.toUpperCase().trim().equals("") && colnGroup.toUpperCase().trim().contains("-")) {
						String splitColnGroup[] = colnGroup.split("-");
						if(splitColnGroup[0].trim().compareToIgnoreCase(colnName) <= 0 && splitColnGroup[1].trim().compareToIgnoreCase(colnName) >= 0) {
							this.mouseOverContextMenu(contextMenuItems.get(itemIdx).getText());
							Utils.fluentWait(this.driver);
							//this.clickContextMenuItem(colnName);
							List<WebElement> columns = driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li[class*='columnsettings']>div"));
							//List<WebElement> columns = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div[id='insertColumns']"));
							String column;
							for(int i=0;i<columns.size();i++){
								column = columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).getText().trim();
								//System.out.println(column);
								if(column.equalsIgnoreCase(colnName)){
									//columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click();
									ActionEventUtils.click(driver, columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")));
									columnInserted = true;
									break;
								}
							}
							if (columnInserted)//Checks if column is inserted then will break the loop
								break;
						}
					}
				}

				if (itemIdx >= contextMenuItemCt)
					throw new Exception("ListView.insertColumn : Column (" + colnName + ") is not inserted successfully.");
			}


		} //End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.insertColumn : "+ e.getMessage(), e);
		} //End catch

		Actions action = new Actions(this.driver);
		action.sendKeys(Keys.ESCAPE);
		Log.event("ListView.insertColumn : Column (" + colnName + ") is inserted.", StopWatch.elapsedTime(startTime));
		return this.isColumnExists(colnName);

	} //End function InsertColumn

	/**
	 * removeColumnByUnCheking : Removes column by un-checking the column
	 * @param colnName - Name of the column
	 * @return true if column removed; false if not not
	 * @throws Exception
	 */
	public Boolean removeColumnByUnCheking(String colnName)throws Exception {

		try {

			if(!this.isColumnExists(colnName)) 
				throw new Exception("Column (" + colnName + ") does not exists in the view.");

			String[] standardColns = {"Name", "Type", "Object Type", "ID", "Version", "Relationships", "Size", "Checked Out To", "Checkout Time", "Date Created", "Date Modified"};

			this.rightClickColumn();
			Utils.fluentWait(this.driver);
			this.clickColumnHeaderContextMenu("Insert Column");
			Utils.fluentWait(this.driver);

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']"));
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx = 0;

			if (Arrays.asList(standardColns).contains(colnName)) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					if(contextMenuItems.get(itemIdx).getText().equals("Standard Columns")) {
						//contextMenuItems.get(itemIdx).click();
						ActionEventUtils.click(driver,contextMenuItems.get(itemIdx));
						Utils.fluentWait(this.driver);
						if(this.itemExistsInContextMenu(colnName)) {
							this.clickContextMenuItem(colnName);
							Utils.fluentWait(this.driver);
							break;
						}
					}
				}
			}
			else {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					String colnGroup = contextMenuItems.get(itemIdx).getText();

					if (!colnGroup.toUpperCase().trim().equals("") && colnGroup.toUpperCase().trim().contains("-")) {
						String splitColnGroup[] = colnGroup.split("-");
						if(splitColnGroup[0].trim().compareToIgnoreCase(colnName) <= 0 && splitColnGroup[1].trim().compareToIgnoreCase(colnName) >= 0) {
							//contextMenuItems.get(itemIdx).click();
							ActionEventUtils.click(driver,contextMenuItems.get(itemIdx));
							Utils.fluentWait(this.driver);
							this.clickContextMenuItem(colnName);
							Utils.fluentWait(this.driver);
							break;
						}
					}
				}	
			}


		} //End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.removeColumnByUnCheking : "+ e.getMessage(), e);
		} //End catch

		Actions action = new Actions(this.driver);
		action.sendKeys(Keys.ESCAPE);
		return this.isColumnExists(colnName);

	} //End function InsertColumn

	/**
	 * removeColumn : Removes the specified column
	 * @param colnName - Name of the column
	 * @return true if column inserted; false if not inserted
	 * @throws Exception
	 */
	public Boolean removeColumn(String colnName)throws Exception {

		try {

			if(!this.isColumnExists(colnName))
				throw new Exception("Column (" + colnName + ") does not exists.");

			this.rightClickColumn(colnName);
			//this.clickContextMenuItem(Caption.Column.RemoveThisColumn.Value); //Select Remove this Column
			List<WebElement> columns = driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li[class*='columnsettings']>div,ul[class='menu-ul innerbox']>li[class*='columnHeaderContextMenu']>div"));

			String column;
			for(int i=0;i<columns.size();i++){
				column = columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).getText();
				if(column.equalsIgnoreCase(Caption.Column.RemoveThisColumn.Value)){
					//columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click();
					ActionEventUtils.click(driver,columns.get(i).findElement(By.cssSelector("div[class='menuItemText']")));
					break;
				}
			}
			return !(this.isColumnExists(colnName)); //Checks if column is removed

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.removeColumn : "+ e.getMessage(), e);
		} //End catch

	} //End function removeColumn

	/** 
	 * getSortedColumn : To get the column which has sort indicator
	 * @param None
	 * @return Name of the column with sort image
	 * @throws Exception
	 */
	public String getSortedColumn()throws Exception {

		try {

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int itemIdx=0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++)
				if (columnHeaders.get(itemIdx).getAttribute("class") != null)
					if (columnHeaders.get(itemIdx).getAttribute("class").toString().contains("sortimage")) 
						return columnHeaders.get(itemIdx).getText();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getSortedColumn : "+ e.getMessage(), e);
		} //End catch

		return "";

	} //End function getSortedColumn

	/**
	 * clickRefresh : Clicks ListView header refresh icon
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public ListView clickRefresh()throws Exception {

		try {

			//this.listing.findElement(By.id("refreshListing")).click();
			ActionEventUtils.click(driver,this.listing.findElement(By.id("refreshListing")));
			Utils.fluentWait(this.driver);
			return new ListView(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickRefresh : "+ e.getMessage(), e);
		} //End catch

	} //End function clickRefresh

	/**
	 * clickShowMoreResults : Clicks ListView header refresh icon
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickShowMoreResults(String group)throws Exception {

		try {

			List<WebElement> headers = this.listing.findElements(By.cssSelector("div[class='group_header']")); //Refresh icon web element

			for(WebElement header : headers) {
				if(header.getText().contains(group)) {
					//header.findElement(By.linkText("Show more results")).click();
					ActionEventUtils.click(driver,header.findElement(By.linkText("Show more results")));
					return;
				}
			}

			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickShowMoreResults : "+ e.getMessage(), e);
		} //End catch

	} //End function clickShowMoreResults

	/**
	 * clickShowMoreResults : Clicks ListView header refresh icon
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public String getGroupHeader(int index)throws Exception {

		try {

			List<WebElement> headers = this.listing.findElements(By.cssSelector("div[class='group_header']")); //Refresh icon web element
			return headers.get(index).getText();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getGroupHeader : "+ e.getMessage(), e);
		} //End catch

	} //End function clickShowMoreResults

	/**
	 * isShowMoreResultsDisplayed : Clicks ListView header refresh icon
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public boolean isShowMoreResultsDisplayed(String group)throws Exception {

		try {
			boolean flag = false;
			List<WebElement> headers = this.listing.findElements(By.cssSelector("div[class='group_header']")); //Refresh icon web element

			for(WebElement header : headers) {
				if(header.getText().contains(group)) {
					flag = header.findElement(By.linkText("Show more results")).isDisplayed();
					break;
				}
			}

			return flag;

		} //End try

		catch (NoSuchElementException e) {
			return false;
		} //End catch


	} //End function isShowMoreResultsDisplayed

	/**
	 * rightClickRefreshIcon : Right Clicks on the refresh icon in the column header
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void rightClickRefreshIcon()throws Exception {

		try {

			WebElement refreshIcon = this.listing.findElement(By.id("refreshListing")); //Refresh icon web element
			Actions action = new Actions(this.driver);
			//	action.contextClick(refreshIcon).build().perform(); //Perform right click on refresh icon
			try {
				action.contextClick(refreshIcon).build().perform(); //Perform right click on refresh icon
			}
			catch (Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", refreshIcon);
			}
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickRefreshIcon : "+ e.getMessage(), e);
		} //End catch

	} //End function rightClickRefreshIcon

	/**
	 * groupCount : Gets the count of the group
	 * @param none
	 * @return count of the group
	 * @throws Exception
	 */
	public int groupCount() throws Exception {

		try {
			List<WebElement> groups = this.listingTable.findElements(By.cssSelector("table[id='mainTable'] tr[class='groupHeader']"));
			return (groups.size());
		} //End try

		catch(Exception e) {
			throw new Exception("Exception at ListView.rightClickRefreshIcon : "+ e.getMessage(), e);
		}
	}	//groupCount

	/**
	 * groupExists : Checks if group header exists in the list
	 * @param headerName - Name of the group header
	 * @return true if group header exists; false if not
	 * @throws Exception
	 */
	public boolean groupExists(String headerName) throws Exception {

		try {

			WebElement list = this.listingTable.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> groups = list.findElements(By.cssSelector("tr[class='groupHeader']"));

			for(int counter = 0; counter < groups.size(); counter++) {
				String text = groups.get(counter).findElement(By.cssSelector("div[class='group_header']")).getText();
				if(text.contains(headerName)) 
					return true;
			}

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at ListView.groupExists : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End groupExists

	/**
	 * getViewCaption : To get the caption of view from the list header
	 * @param none
	 * @return Name of the view caption
	 * @throws Exception
	 */
	public String getViewCaption()throws Exception {

		try {

			WebElement captionElement = this.listing.findElement(By.cssSelector("div[id='viewCaption']>span[class='captionText']"));
			return(captionElement.getText());

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.getViewCaption : "+ e.getMessage(), e);
		} //End catch

	} //End function getViewCaption

	/**
	 * isBackToViewButtonExists : To check if back to view button exists in the list view
	 * @param none
	 * @return true if back to view button exists; if not returns false
	 * @throws Exception
	 */
	public boolean isBackToViewButtonExists()throws Exception {

		try {

			if (this.listing.findElement(By.cssSelector("div[id='viewCaption']>div[id='backButton']")).isDisplayed())
				return true;

			return false;

		} //End try
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception("Exception at ListView.isBackToViewButtonExists : "+ e.getMessage(), e);
		} //End catch

	} //End function clickBackToViewButton

	/**
	 * clickBackToViewButton : To click back button in list header
	 * @param none
	 * @return none
	 * @throws Exception
	 */
	public void clickBackToViewButton()throws Exception {

		try {

			//this.listing.findElement(By.cssSelector("div[id='viewCaption']>div[id='backButton']")).click();
			ActionEventUtils.click(driver,this.listing.findElement(By.cssSelector("div[id='viewCaption']>div[id='backButton']")));
			Utils.fluentWait(this.driver);

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.clickBackToViewButton : "+ e.getMessage(), e);
		} //End catch

	} //End function clickBackToViewButton

	/**
	 * isContextMenuExists : To check if context menu gets displayed in the list view
	 * @return true if context menu displayed; false if context menu does not displayed
	 * @throws Exception
	 */
	public Boolean isContextMenuExists() throws Exception {

		try {

			WebElement contextMenuItems = this.driver.findElement(By.cssSelector("ul[class='contextMenu']")); //Context menu web element

			if (contextMenuItems.isDisplayed()) //returns true if context menu gets displayed
				return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isContextMenuExists : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End clickContextMenu

	/**
	 * contextMenuDisplayed : Checks if context menu is displayed
	 * @param none
	 * @return true if context menu is displayed; false if not displayed
	 * @throws Exception
	 */
	public Boolean contextMenuDisplayed()throws Exception {

		try {

			WebElement contextMenu = this.driver.findElement(By.cssSelector("body>ul[id='menuSubOperations']"));

			if (contextMenu.isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException"))
				return false;
			else				
				throw new Exception("Exception at ListView.contextMenuDisplayed : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function contextMenuDisplayed

	/**
	 * clickContextMenu : To click item in the context menu
	 * @param itemToClick - Item to select from context menu
	 * @return none
	 * @throws Exception
	 */
	public void clickContextMenuItem(String itemToClick) throws Exception {

		try {

			if (browser.equalsIgnoreCase("safari"))
				if (itemToClick.equalsIgnoreCase("Convert to Multi-file Docum..."))
					itemToClick = "Convert to Multi-file Document";
				else if (itemToClick.equalsIgnoreCase("Convert to Single-file Docum..."))
					itemToClick = "Convert to Single-file Document";

			List<WebElement> listItems = this.driver.findElements(By.cssSelector("ul[class='contextMenu']>li>div>div>span"));
			String text;
			int loopIndx;
			for(loopIndx = 0; loopIndx < listItems.size(); loopIndx++){
				text = listItems.get(loopIndx).getText();
				if(text.equalsIgnoreCase(itemToClick)){
					//listItems.get(i).click();
					ActionEventUtils.click(driver,listItems.get(loopIndx));
					Utils.fluentWait(driver);
					return;
				}
			}
			/*String[] items = itemToClick.split(">>");
			int itemCt = items.length;
			List<WebElement> listItems = this.driver.findElement(By.id("root-menu-div")).findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li[class*='newObject'],ul[class='contextMenu']>li>div>div>span"));
			//List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li,ul[class='contextMenu']>li>div>div>span"));
			String text;
            for(int i=0; i<listItems.size(); i++){
            	text = listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")).getText();
            	 if(text.equalsIgnoreCase(itemToClick)){
            		 listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click();     
            	 	break;
            	 }
            }*/

			//List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li,ul[class='contextMenu']>li>div>div>span"));
			//			//ul[@class='menu-ul innerbox']/li[contains(class, 'columnHeaderContextMenu') and normalize-space(.)='"+itemToClick+"'] | " +
			//			List<WebElement> contextMenuItems = this.driver.findElements(By.xpath("//ul[@class='menu-ul innerbox']/li[normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | //ul[@class='contextMenu']/li/div/div/span[normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | "
			//					+ "//li[contains(@class, 'updateColumnSettingsMenu')]/div/div/div[contains(@class, 'menuItemText') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | //ul[contains(@class, 'menu-ul innerbox')]/li/div[contains(@class, 'menu-item') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"]"));
			//			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			/*int itemIdx =0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  { //Loops to identify the instance of the item to be clicked
					if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
						JavascriptExecutor executor = (JavascriptExecutor)driver;
						executor.executeScript("arguments[0].click();", contextMenuItems.get(itemIdx));
						//contextMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
						Utils.fluentWait(this.driver);
						break;
					}
				}

				Utils.fluentWait(driver);*/

			Utils.fluentWait(driver);	

			if (loopIndx >= listItems.size()) //Checks for the existence of the item to click
				throw new Exception ("Item (" + itemToClick + ") does not exists or not enabled in the list.");


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickContextMenuItem : "+ e.getMessage(), e);
		} //End catch	

	} //End clickContextMenu

	/**
	 * clickContextMenu : To click item in the context menu
	 * @param itemToClick - Item to select from context menu
	 * @return none
	 * @throws Exception
	 */
	public Boolean isItemAvailableinContextmenu(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;
			//List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li,ul[class='contextMenu']>li>div>div>span"));
			//ul[@class='menu-ul innerbox']/li[contains(class, 'columnHeaderContextMenu') and normalize-space(.)='"+itemToClick+"'] | " +
			List<WebElement> contextMenuItems = this.driver.findElements(By.xpath("//ul[@class='menu-ul innerbox']/li[normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | //ul[@class='contextMenu']/li/div/div/span[normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | "
					+ "//li[contains(@class, 'updateColumnSettingsMenu')]/div/div/div[contains(@class, 'menuItemText') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | //ul[contains(@class, 'menu-ul innerbox')]/li/div[contains(@class, 'menu-item') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"]"));
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx =0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  { //Loops to identify the instance of the item to be clicked
					if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
						return true;
					}
				}

				Utils.fluentWait(driver);

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					return false;
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isItemAvailableinContextmenu : "+ e.getMessage(), e);
		} //End catch	

		return false;

	} //End clickContextMenu

	/**
	 * clickColumnHeaderContextMenu : To click item in the context menu of column header
	 * @param itemToClick - Item to select from context menu in column header
	 * @return none
	 * @throws Exception
	 */
	public void clickColumnHeaderContextMenu(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='columnHeaderContextMenu']," +
					"li[class*='updateColumnSettingsMenu']>div>div>div[class='menuItemText']," +
					"ul[class='menu-ul innerbox']>li>div[class='menu-item']"));

			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx =0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  { //Loops to identify the instance of the item to be clicked
					System.out.println(contextMenuItems.get(itemIdx).getText());
					if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
						ActionEventUtils.click(driver, contextMenuItems.get(itemIdx));
						//contextMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
						break;
					}
				}

				Utils.fluentWait(driver);

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					throw new Exception ("Item (" + items[i] + ") does not exists or not enabled in the list.");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickColumnHeaderContextMenu : "+ e.getMessage(), e);
		} //End catch	

	} //End clickColumnHeaderContextMenu

	/**
	 * mouseOverContextMenu : To click item in the context menu of column header
	 * @param itemToClick - Item to select from context menu in column header
	 * @return none
	 * @throws Exception
	 */
	public void mouseOverContextMenu(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;
			WebElement element = null;
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			List<WebElement> contextMenuItems = new ArrayList<WebElement>();
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx =0;

			for (int i=0; i<itemCt; i++) {
				contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='columnHeaderContextMenu']," +
						"li[class*='updateColumnSettingsMenu']>div>div>div[class='menuItemText'], div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div[class='menu-item']"));

				/* contextMenuItems = this.driver.findElements(By.xpath("//ul[@class='menu-ul innerbox']/li[contains(class, 'columnHeaderContextMenu') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | " +
                             "//li[contains(@class, 'updateColumnSettingsMenu')]/div/div/div[contains(@class, 'menuItemText') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"] | //ul[contains(@class, 'menu-ul innerbox')]/li/div[contains(@class, 'menu-item') and normalize-space(.)="+Utility.xPathStringParser(itemToClick)+"]"));
				 */
				contextMenuItemCt = contextMenuItems.size();
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  { //Loops to identify the instance of the item to be clicked
					System.out.println(contextMenuItems.get(itemIdx).getText());
					if (contextMenuItems.get(itemIdx).isDisplayed() && contextMenuItems.get(itemIdx).getText().trim().toUpperCase().equals(items[i].trim().toUpperCase())) {
						element = contextMenuItems.get(itemIdx);
						break;
					}
				}

				if (itemIdx >= contextMenuItemCt)
					throw new Exception("Item("+items[i].trim()+") does not exists or not enabled in the list.");

				ActionEventUtils.moveToElement(driver, element);

				if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Safari"))
					ActionEventUtils.click(driver, element);

				Utils.fluentWait(driver);

				Log.event("ListView.mouseOverContextMenu : Performed mouse over action over the item ("+itemToClick+").");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.mouseOverContextMenu : "+ e.getMessage(), e);
		} //End catch     

	} //End mouseOverContextMenu

	/**
	 * itemExistsInContextMenu : To check the existence of item in the context menu
	 * @param none
	 * @return true if context menu is displayed; false if not displayed
	 * @throws Exception
	 */
	public Boolean itemExistsInContextMenu(String itemToClick) throws Exception {

		Boolean isExists = false;

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div>div," +
					"ul[class='menu-ul innerbox']>li>div,ul[class='contextMenu']>li>div>div>span"));

			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx = 0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					if(contextMenuItems.get(itemIdx).isDisplayed())
						if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
							isExists = true;
							break;
						}
				}

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					return false;
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.itemExistsInContextMenu : "+ e.getMessage(), e);
		} //End catch	

		return isExists;

	} //End itemExistsInContextMenu

	/**
	 * itemExistsInColumnHeaderMenu : To check the existence of item in the column header menu
	 * @param none
	 * @return true if menu item is displayed; false if not displayed
	 * @throws Exception
	 */
	public Boolean itemExistsInColumnHeaderMenu(String itemToClick) throws Exception {

		Boolean isExists = false;

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='columnHeaderContextMenu'],li[class*='updateColumnSettingsMenu']>div>div>div[class='menuItemText']"));
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx =0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (contextMenuItems.get(itemIdx).isDisplayed())
						if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
							isExists = true;
							break;
						}

				Utils.fluentWait(driver);

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					return false;
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.itemExistsInColumnHeaderMenu : "+ e.getMessage(), e);
		} //End catch	

		return isExists;

	} //End itemExistsInContextMenu

	/**
	 * itemEnabledInContextMenu : To check the enabled status of item in the context menu
	 * @param itemToClick - Item to check for enable status
	 * @return true if context menu item is enabled; false if not enabled
	 * @throws Exception
	 */
	public Boolean itemEnabledInContextMenu(String itemToClick) throws Exception {

		//Variable Declaration
		Boolean isEnabled = false;

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='contextMenu']>li>div>div>span"));
			int contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx = 0;

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if(contextMenuItems.get(itemIdx).isDisplayed())
						if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
							if (!contextMenuItems.get(itemIdx).findElement(By.xpath("..")).getAttribute("class").toString().toUpperCase().contains("DIMMED"))
								isEnabled = true;
							break;
						}

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					return false;
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.itemEnabledInContextMenu : "+ e.getMessage(), e);
		} //End catch	

		return isEnabled;

	} //End itemEnabledInContextMenu

	/**
	 * clickItemInGeneralContextMenu : To check the enabled status of item in the context menu
	 * @param itemToClick - Item to check for enable status
	 * @return true if context menu item is enabled; false if not enabled
	 * @throws Exception
	 */
	public void clickItemInGeneralContextMenu(String itemToClick) throws Exception {

		try {

			//Variable Declaration
			int itemIdx; //Stores the index of the items used in for loop
			int contextMenuItemCt; //Stores the task panel item count
			String itemID = "";
			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[id='menuSubOperations']>li"));
			contextMenuItemCt = contextMenuItems.size(); //Gets the number of items in the new menu

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<contextMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (contextMenuItems.get(itemIdx).getText().toUpperCase().trim().contains(items[i].toUpperCase())) {
						itemID = contextMenuItems.get(itemIdx).getAttribute("id");
						break;
					}

				if (itemIdx >= contextMenuItemCt) //Checks for the existence of the item to click
					throw new Exception ("Item (" + items[i] + ") does not exists or not enabled in the list.");
			}

			JavascriptExecutor js = (JavascriptExecutor) this.driver; 
			js.executeScript("var contextMenu = document.getElementById('" + itemID + "');contextMenu.click();");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickItemInGeneralContextMenu : "+ e.getMessage(), e);
		} //End catch	

	} //End clickContextMenu


	/**
	 * isThumbnailsView : To check if list view is in thumbnails mode
	 * @param none
	 * @return true if it is thumbnails view; false if not
	 * @throws Exception
	 */
	public Boolean isThumbnailsView()throws Exception {

		try {

			this.listingTable.findElement(By.cssSelector("div[class='thumbDiv']"));
			return true;

		} //End try
		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException"))
				return false;
			else
				throw new Exception("Exception at ListView.isThumbnailsView : "+ e.getMessage(), e);
		} //End catch

	} //End function GetViewCaption

	/**
	 * getColumnWebElement : Gets the Web Element of the column
	 * @param colnName - Name of the column
	 * @return Web element of the column
	 * @throws Exception
	 */
	public WebElement getColumnWebElement(String colnName)throws Exception {

		try {

			if (!this.isColumnExists(colnName))
				throw new SkipException("Column (" + colnName + ") does not exists.");

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx = 0; itemIdx < rowCt; itemIdx++) 
				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase()))
					return(columnHeaders.get(itemIdx));

			return null;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnWebElement : "+ e.getMessage(), e);
		} //End catch

	} //End function getColumnWebElement

	/**
	 * clickColumn : To click the column in the header
	 * @param colnName - Name of the column
	 * @return true if it is clicked; false if not
	 * @throws Exception
	 */
	public Boolean clickColumn(String colnName)throws Exception {

		try {

			//this.getColumnWebElement(colnName).click();
			ActionEventUtils.click(driver,this.getColumnWebElement(colnName));
			return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickColumn : "+ e.getMessage(), e);
		} //End catch

	} //End function ClickColumn

	/**
	 * getColumnSortImage : To get the status of the column in sorting
	 * @param colnName - Name of the column
	 * @return ASC if it is in ascending order; DSC if it is in descending order
	 * @throws Exception
	 */
	public String getColumnSortImage(String colnName)throws Exception {

		//Variable Declaration
		String sort = ""; //Stores the last item in the bread crumb 

		try {

			if (!this.isColumnExists(colnName))
				throw new Exception("Column (" + colnName + ") does not exists.");

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int itemIdx=0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) 
				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(colnName.toUpperCase())) 
					break;

			if(itemIdx != rowCt) {
				if(columnHeaders.get(itemIdx).getAttribute("Class").toString().contains("sortimage-up"))
					sort = "ASC";
				else if(columnHeaders.get(itemIdx).getAttribute("Class").toString().contains("sortimage-down"))
					sort = "DSC";
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getColumnSortImage : "+ e.getMessage(), e);
		} //End catch

		return sort;

	} //End function getColumnSortImage

	/**
	 * getColumnSize : Gets the current size of the column
	 * @param colnName - Name of the column
	 * @return width of the column
	 * @throws Exception
	 */
	public int getColumnSize(String colnName)throws Exception {

		try {

			//return(Integer.parseInt(this.getColumnWebElement(colnName).getAttribute("style").split("width: ")[1].split("px;")[0]));
			String columnWidth = "";

			if (browser.equalsIgnoreCase("Edge"))
				columnWidth = this.getColumnWebElement(colnName).getAttribute("style").split("width: ")[1].split("px;")[0];
			else
				columnWidth = this.getColumnWebElement(colnName).getAttribute("style").split("width: ")[1].split("px;")[0];

			return(Integer.parseInt(columnWidth));

		} //End try

		catch (Exception e) {
			throw e;
		} //End catch

	} //End function columnResize

	/**
	 * resizeColumn : Resize the column
	 * @param colnName - Name of the column
	 * @param offset - Offest to resize the column
	 * @return width of the column
	 * @throws Exception
	 */
	public int resizeColumn(String colnName, int offset)throws Exception {

		try {

			WebElement columnElement = this.getColumnWebElement(colnName);
			WebElement resizer = columnElement.findElement(By.xpath("..")).findElement(By.cssSelector("div"));
			Actions action = new Actions(this.driver);
			action.dragAndDropBy(resizer, offset, 0).perform();
			//return(Integer.parseInt(columnElement.getAttribute("style").split("width: ")[1].split("px;")[0]));
			String columnWidth = "";

			if (browser.equalsIgnoreCase("Edge"))
				columnWidth = columnElement.getAttribute("style").split("width: ")[1].split("px;")[0];
			else
				columnWidth = columnElement.getAttribute("style").split("width: ")[2].split("px;")[0];

			return(Integer.parseInt(columnWidth));

		} //End try

		catch (Exception e) {
			throw e;
		} //End catch

	} //End function columnResize

	/**
	 * getColumnWidth : Gets the column width
	 * @param colnName - Name of the column
	 * @return width of the column
	 * @throws Exception
	 */
	public int getColumnWidth(String colnName)throws Exception {

		try {

			WebElement columnElement = this.getColumnWebElement(colnName);
			//return(Integer.parseInt(columnElement.getAttribute("style").split("width: ")[1].split("px;")[0]));
			return(Integer.parseInt(columnElement.getAttribute("style").split("width: ")[2].split("px;")[0]));

		} //End try

		catch (Exception e) {
			throw e;
		} //End catch

	} //End function getColumnWidth

	/**
	 * sortColumn : Sorts the column
	 * @param isAscending - true to perform in Ascending order; false if not
	 * @return None
	 * @throws Exception
	 */
	public void sortColumn(boolean isAscending) throws Exception {

		try {

			this.rightClickColumn(); //Right clicks column
			Utils.fluentWait(driver);

			if (isAscending) //To sort in Ascending
				this.clickColumnHeaderContextMenu(Caption.Column.SortAscending.Value);
			else //To Sort in Descending
				this.clickColumnHeaderContextMenu(Caption.Column.SortDescending.Value);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.sortColumn : "+ e.getMessage(), e);
		} //End catch

	} //sortColumn

	/**
	 * isItemExists : To check the existence of the item
	 * @param itemName - Name of the item
	 * @param columnName - Name of the column
	 * @return true if item exists; false if not
	 * @throws Exception
	 */
	public Boolean isItemExists(String itemName, String columnName)throws Exception {

		try {

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			if(this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td[contains(@class, 'column-'"+this.getColumnIndex(columnName)+"')]/div[@class='list_holder name_column']/span[text()='" + itemName + "']")).isDisplayed())
				return true;
			else
				return false;*/
			int index = this.getColumnOrder(columnName);
			index = index + 1;
			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) {

				if (itemRows.get(itemIdx).findElement(By.cssSelector("td:nth-child(" + index + ")")).getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) 
					return true;
			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at ListView.isItemExists : "+ e.getMessage(), e);
		} //End catch
		return false;
	} //End function IsItemExists

	/**
	 * clickItem : To click the item
	 * @param item - Name of the item
	 * @param columnName - Name of the column
	 * @return width of the column
	 * @throws Exception
	 */
	public Boolean clickItem(String itemName, String colName)throws Exception {

		Boolean isClicked = false; //Stores the last item in the bread crumb 

		try {


			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			//	this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td[contains(@class, 'column-'"+this.getColumnIndex(colName)+"')]/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]")).click();
			ActionEventUtils.click(driver,this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td[contains(@class, 'column-'"+this.getColumnIndex(colName)+"')]/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]")));

			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td[class='listing-column column-" + this.getColumnIndex(colName) +  "]>div"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					itemSpan.click();
					break;
				}

			} //End for
			 */			
			if (this.isItemSelected(itemName)) //	Checks if item is selected
				isClicked = true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickItem : "+ e.getMessage(), e);
		} //End catch

		return isClicked;

	} //End function ClickItem

	/**
	 * clickItem : To click the item
	 * @param item - Name of the item
	 * @param columnName - Name of the column
	 * @return width of the column
	 * @throws Exception
	 */
	public void clickItemOccurence(String itemName, String colName, int occ)throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;
			int count= 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td[class*='listing-column column-" + this.getColumnIndex(colName) +  "']>div"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					count++;
					if(count == occ) {
						//itemSpan.click();
						ActionEventUtils.click(driver,itemSpan);
						return;
					}
				}
			} //End for

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickItemOccurence : "+ e.getMessage(), e);
		} //End catch

	} //End function ClickItem

	/**
	 * getItemOccurence : To count the occurences of the item
	 * @param itemName - Name of the item
	 * @param columnName - Name of the column
	 * @return Number of time the object has occured
	 * @throws Exception
	 */
	public int getItemOccurence(String itemName, String colName)throws Exception {

		int count = 0; 

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td[class*='listing-column column-" + this.getColumnIndex(colName) +  "']>div"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) 
					count++;

			} //End for

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getItemOccurence : "+ e.getMessage(), e);
		} //End catch

		return count;

	} //End function ClickItem

	/**
	 * getItemIndexByItemName : To get index of an item
	 * @param itemName - Name of the item
	 * @param colName - Name of the column  
	 * @return Index of an item
	 * @throws Exception
	 */
	public int getItemIndexByItemName(String itemName, String colName)throws Exception {

		try {

			if (!this.isColumnExists(colName)) //Checks if item exists in the list
				this.insertColumn(colName);

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx=0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td[class*='listing-column column-" + this.getColumnIndex(colName) +  "']>div"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase()))
					return itemIdx;

			} //End for

			if (itemIdx >= rowCt) //Checks if item exists in the list
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getItemIndexByItemName : "+ e.getMessage(), e);
		} //End catch

		return -1;

	} //End function getItemIndexByItemName

	/**
	 * rightClickItem : To right click the item
	 * @param itemName - Name of the item
	 * @param columnName - Name of the column
	 * @return width of the column
	 * @throws Exception
	 */
	public Boolean rightClickItem(String itemName, String colName)throws Exception {

		//Variable Declaration
		Boolean isClicked = false; //Stores the last item in the bread crumb 

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			WebElement itemSpan = this.listingRows.findElement(By.xpath("tr[contains(@class,'listing-item tap')]/td[contains(@class, 'column-'"+this.getColumnIndex(colName)+"')]/div[@class='list_holder name_column']/span[text()=" + Utility.xPathStringParser(itemName) + "]"));

			Actions action = new Actions(this.driver);

			try {
				action.moveToElement(itemSpan, 10, itemSpan.getSize().height/2).contextClick().build().perform();
				isClicked = true;
			}
			catch (Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", itemSpan);
				isClicked = true;
			}
			/*this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']>tbody"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;
			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td[class*='column-" + this.getColumnIndex(colName) +  "']>div"));

				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					Actions action = new Actions(this.driver);
					action.moveToElement(itemSpan, 10, itemSpan.getSize().height/2).contextClick().build().perform();
					isClicked = true;
					break;
				}

			} //End for

			if (itemIdx >= rowCt)
				isClicked = false;*/

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.rightClickItem : "+ e.getMessage(), e);
		} //End catch

		return isClicked;

	} //End function ClickItem

	/**
	 * expandItemByName : To expands the item in the list view by item name
	 * @param itemName - Name of the item
	 * @return true if item is expanded; false if not
	 * @throws Exception
	 */
	public Boolean expandItemByName(String itemName)throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));

			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag
			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx=0;
			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops through all the items and clicks the specified item

				WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
				System.out.println("List Item : " + itemSpan.getText() + "\tTD Item : " + itemName + "\tStatus:" + itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase()));
				if (itemSpan.getText().toUpperCase().equalsIgnoreCase(itemName.toUpperCase())) {
					System.out.println("Inside if item exists");
					WebElement expandIcon = itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon'][src*='Collapsed.png']"));
					//expandIcon.click();
					ActionEventUtils.click(driver,expandIcon);
					Utils.fluentWait(this.driver);
					return true;
				}

			} //End for

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at ListView.expandItemByName : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End function expandItemByName

	/**
	 * expandItemByIndex : To expands the item in the list view by index
	 * @param itemName - Name of the item
	 * @return true if item is expanded; false if not
	 * @throws Exception
	 */
	public Boolean expandItemByIndex(int itemIdx)throws Exception {

		try {

			this.listingRows = this.listing.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class='listing-item tap'],tr[class='listing-item tap row_highlight']")); //Stores the web element with tr tag

			int rowCt = itemRows.size(); //Number of rows with specified attribute

			if (itemIdx >= rowCt)
				throw new Exception("Item with index (" + itemIdx + ") is greater than number of items (" + rowCt + ").");

			WebElement itemSpan = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span"));
			WebElement expandIcon = itemSpan.findElement(By.xpath("..")).findElement(By.cssSelector("img[class='arrow_icon'][src*='Collapsed.png']"));
			//	expandIcon.click();
			ActionEventUtils.click(driver,expandIcon);
			Utils.fluentWait(this.driver);
			return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at ListView.expandItemByIndex : "+ e.getMessage(), e);
		} //End catch

	} //End function expandItemByName

	/**
	 * objectCountInGroupHeader : Get the ObjectCount in the group header from list view
	 * @param objectType - Type of the object
	 * @return number of objects from first group header
	 * @throws Exception 
	 */
	public int objectCountInGroupHeader(String objectType) throws Exception {

		try {

			final long startTime = StopWatch.startTime();

			List<WebElement> groupHeaders = driver.findElements(By.cssSelector("div[class='group_header']"));
			int grpHeaderCt = groupHeaders.size();
			String objectHeader = "";

			for (int loopIdx=0; loopIdx<grpHeaderCt; loopIdx++)
				if (groupHeaders.get(loopIdx).getText().equalsIgnoreCase(objectType)) {
					objectHeader = groupHeaders.get(loopIdx).getText();
					break;
				}

			if (objectHeader.equals(""))
				throw new Exception ("Object type header (" + objectType + ") does not exists in the list.");

			String[] header = objectHeader.split("\\(");
			String objectName = header[0];
			String[] searchCount = header[1].split("\\)");

			Log.event("Search Count of " + objectName + ":" + searchCount[0], StopWatch.elapsedTime(startTime));
			return (Integer.parseInt(searchCount[0]));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.objectCountInGroupHeader : "+ e.getMessage(), e);
		} //End catch

	} //End objectCountInGroupHeader

	/**
	 * isGroupHeaderAvailable : To verify if search results has Documents/Objects as header
	 * @param none
	 * @return true if Objects or Documents available as header; if not returns false
	 * @throws Exception
	 */
	public boolean isGroupHeaderAvailable() throws Exception { 

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);

			List<WebElement> groupHeaders = driver.findElements(By.cssSelector("div[class='group_header']"));

			if (groupHeaders.contains("Documents") || groupHeaders.contains("Objects")) {
				Log.event("Group Header contains Documents or objects", StopWatch.elapsedTime(startTime));
				return true;
			}
		}

		catch(Exception e) {
			throw new Exception("Exception at ListView.isGroupHeaderAvailable : "+ e.getMessage(), e);
		}

		Log.event("Group Header does not contains Documents or objects", StopWatch.elapsedTime(startTime));
		return false;
	}

	/**
	 * isGroupHeaderAvailable : To verify if search results has Documents/Objects as header
	 * @param none
	 * @return true if Objects or Documents available as header; if not returns false
	 * @throws Exception
	 */
	public boolean isGroupHeaderAvailable(String itemName) throws Exception { 

		final long startTime = StopWatch.startTime();

		try {

			List<WebElement> groupHeaders = driver.findElements(By.cssSelector("div[class='group_header']"));
			int grpHeaderCt = groupHeaders.size();
			String objectHeader = "";

			for (int loopIdx=0; loopIdx<grpHeaderCt; loopIdx++)
				if (groupHeaders.get(loopIdx).getText().trim().toLowerCase().contains(itemName.trim().toLowerCase())) {
					objectHeader = groupHeaders.get(loopIdx).getText();
					return true;
				}

			if (objectHeader.equals(""))
				throw new Exception ("Object type header (" + itemName + ") does not exists in the list.");
		}

		catch(Exception e) {
			throw new Exception("Exception at ListView.isGroupHeaderAvailable : "+ e.getMessage(), e);
		}

		Log.event("Group Header does not contains "+ itemName + " or objects", StopWatch.elapsedTime(startTime));
		return false;
	}

	/**
	 * isDetailsDisplayMode : To verify if display mode is details or thumbnails
	 * @param none
	 * @return true if display mode is details; false if thumbnails
	 * @throws Exception
	 */
	public boolean isDetailsDisplayMode() throws Exception { 

		final long startTime = StopWatch.startTime();

		try {


			Utils.fluentWait(driver);

			List<WebElement> groupHeaders = driver.findElements(By.cssSelector("div[class='group_header']"));

			if (groupHeaders.contains("Documents") || groupHeaders.contains("Objects")) {
				Log.event("Group Header contains Documents or objects", StopWatch.elapsedTime(startTime));
				return true;
			}
		}

		catch(Exception e) {
			throw new Exception("Exception at ListView.isDetailsDisplayMode : "+ e.getMessage(), e);
		}

		Log.event("Group Header does not contains Documents or objects", StopWatch.elapsedTime(startTime));
		return false;
	}

	/**
	 * dragColumn : To drag column from one position to another position
	 * @param none
	 * @return true if display mode is details; false if thumbnails
	 * @throws Exception
	 */
	public boolean dragColumn(String fromColumn, String toColumn, Boolean isBefore) throws Exception { 

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isColumnExists(fromColumn))
				throw new Exception("Column (" + fromColumn + ") does not exists.");

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int fromColnIdx = this.getColumnIndex(fromColumn);
			WebElement frontColnElement = null;
			WebElement toColnElement = null;
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) { //Loops to identify the index of the web element

				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(fromColumn.toUpperCase()))
					frontColnElement = columnHeaders.get(itemIdx);

				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(toColumn.toUpperCase())) 
					toColnElement = columnHeaders.get(itemIdx);

				if (frontColnElement != null && toColnElement != null)
					break;
			}

			//Calculation to perform drag and drop position of from column
			int fromMidX = (frontColnElement.getLocation().x + frontColnElement.getSize().width)/2;
			int toMidX = (toColnElement.getLocation().x + toColnElement.getSize().width)/2;
			int toDragX = toMidX - fromMidX;

			//Calculating drop x-Coordinate
			if (toDragX < 0 && isBefore)
				toDragX = toDragX - 100;
			else if (toDragX < 0 && !isBefore)
				toDragX = toDragX + 100;
			else if (toDragX > 0 && isBefore)
				toDragX = toDragX - 100;
			else
				toDragX = toDragX + 100;

			//Performiong Drag & Drop operation
			Actions action = new Actions(driver);
			action.dragAndDropBy(frontColnElement, toDragX, 0).build().perform();
			Log.event("dragColumn : Drag operation is performed.", StopWatch.elapsedTime(startTime));

			//Verifies if Drag & Drop operation is successful
			if (toDragX < 0 && this.getColumnIndex(fromColumn) < fromColnIdx)
				return true;
			else if (toDragX > 0 && this.getColumnIndex(fromColumn) > fromColnIdx)
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at ListView.dragColumn : "+ e.getMessage(), e);
		} //End catch

	} //dragColumn

	/**
	 * dragColumn : To drag column from one position to another position
	 * @param none
	 * @return true if display mode is details; false if thumbnails
	 * @throws Exception
	 */
	public boolean dragColumn(String fromColumn, Boolean isLeftSide) throws Exception { 

		final long startTime = StopWatch.startTime();

		try {

			if (!this.isColumnExists(fromColumn))
				throw new Exception("Column (" + fromColumn + ") does not exists.");

			List<WebElement> columnHeaders = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span")); //Stores the web element with tr tag
			int rowCt = columnHeaders.size(); //Number of rows with specified attribute
			int fromColnIdx = this.getColumnIndex(fromColumn);
			WebElement frontColnElement = null;
			WebElement toColnElement = null;
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops to identify the index of the web element

				if (columnHeaders.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(fromColumn.toUpperCase())) {
					frontColnElement = columnHeaders.get(itemIdx);
					break;
				}

			if (isLeftSide && itemIdx >= 1)
				toColnElement = columnHeaders.get(itemIdx - 1);
			else if (!isLeftSide && itemIdx < rowCt-1)
				toColnElement = columnHeaders.get(itemIdx + 1);
			else
				throw new SkipException("Column (" + fromColumn + ") cannot be dragged to specified position.");

			System.out.println(toColnElement.getText());

			//Calculation to perform drag and drop position of from column
			int fromMidX = (frontColnElement.getLocation().x + frontColnElement.getSize().width/2);
			//int toMidX = toColnElement.getLocation().x;
			int toMidX = (toColnElement.getLocation().x + toColnElement.getSize().width/2);
			int toDragX = toMidX - fromMidX;

			//Calculating drop x-Coordinate
			if (toDragX < 0 && isLeftSide)
				toDragX = toDragX - 100;
			else if (toDragX < 0 && !isLeftSide)
				toDragX = toDragX + 100;
			else if (toDragX > 0 && isLeftSide)
				toDragX = toDragX - 100;
			else
				toDragX = toDragX + 100;

			//Performiong Drag & Drop operation
			Actions action = new Actions(driver);
			action.dragAndDropBy(frontColnElement, toDragX, 0).build().perform();
			Log.event("dragColumn : Drag operation is performed.", StopWatch.elapsedTime(startTime));

			//Verifies if Drag & Drop operation is successful
			if (toDragX < 0 && this.getColumnIndex(fromColumn) < fromColnIdx)
				return true;
			else if (toDragX > 0 && this.getColumnIndex(fromColumn) > fromColnIdx)
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at ListView.dragColumn : "+ e.getMessage(), e);
		} //End catch

	} //dragColumn

	/**
	 * isMetadataPropertiesEnabled : To verify if Properties in right menu is enabled
	 * @param none
	 * @return true if Properties is enabled; false if not
	 * @throws Exception
	 */
	public boolean isMetadataPropertiesEnabled() throws Exception { 

		try {

			if (this.itemCount() <=0) //Checks if items are available in the page
				throw new Exception("There are no item listed in the page.");

			if (!this.rightClickItemByIndex(0)) //Right clicks on the first item
				throw new Exception("Item is not right clicked.");

			if (this.itemEnabledInContextMenu(Caption.MenuItems.Properties.Value)) //Checks if properties menu is in enabled state
				return true;
		}

		catch(Exception e) {
			throw new Exception("Exception at ListView.isMetadataPropertiesEnabled : "+ e.getMessage(), e);
		}

		return false;
	}

	/*------------------------------Re-usable Functions----------------------------*/

	/** isHistoryViewOpened : To Check if History view is opened 
	 * @param driver
	 * @return true if History view is opened; if not false
	 * @throws Exception
	 */
	public static Boolean isHistoryViewOpened(WebDriver driver) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.getViewCaption().toUpperCase().equalsIgnoreCase("HISTORY"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isHistoryViewOpened : "+ e.getMessage(), e);
		} //End catch

	} //End isHistoryViewOpened

	/** isRelationshipsViewOpened : To Check if Relationships view is opened 
	 * @param driver
	 * @return true if Realtionships view is opened; if not false
	 * @throws Exception
	 */
	public static Boolean isRelationshipsViewOpened(WebDriver driver) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.getViewCaption().toUpperCase().contains("RELATIONSHIPS"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isRelationshipsViewOpened : "+ e.getMessage(), e);
		} //End catch

	} //End isRelationshipsViewOpened

	/** isSubObjectsViewOpened : To Check if Sub-Objects view is opened 
	 * @param driver
	 * @return true if Sub-objects view is opened; if not false
	 * @throws Exception
	 */
	public static Boolean isSubObjectsViewOpened(WebDriver driver) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.getViewCaption().toUpperCase().contains("SUBOBJECTS"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isSubObjectsViewOpened : "+ e.getMessage(), e);
		} //End catch

	} //End isSubObjectsViewOpened

	/** isMembersViewOpened : To Check if Show Members view is opened 
	 * @param driver
	 * @return true if Show members view is opened; if not false
	 * @throws Exception
	 */
	public static Boolean isMembersViewOpened(WebDriver driver) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.getViewCaption().toUpperCase().contains("COLLECTION MEMBERS"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isMembersViewOpened : "+ e.getMessage(), e);
		} //End catch

	} //End isMembersViewOpened

	/** isCheckedOutByItemName : To Check if object is checked out by the object name
	 * @param driver
	 * @return true if object is checked out; if not false
	 * @throws Exception
	 */
	public static Boolean isCheckedOutByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			//switch (ListView.isHistoryViewOpened(driver).toString().toUpperCase()) {

			//case "TRUE" : {
			if (listView.getIconURLByItemName(itemName).toUpperCase().contains("CHECKEDOUT"))
				return true;
			else
				return false;

			//} //End case TRUE
			/*
			case "FALSE" : {
				//	try {listView.removeColumn(Caption.Column.Coln_CheckedOutTo.Value);} catch (Exception e) {}
				if (!listView.insertColumn(Caption.Column.Coln_CheckedOutTo.Value)) //Checks and inserts Checked out To Column to the list
					throw new Exception("Column (Checked out To) column does not inserted successfully.");

				//listView = listView.clickRefresh(); //Refresh list view
				Utils.fluentWait(driver);

				if (!listView.getColumnValueByItemName(itemName, Caption.Column.Coln_CheckedOutTo.Value).equals(""))
					return true;
				else
					return false;

			}//End case FALSE

			default : 
				return false;

			} //End Switch
			 */
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isCheckedOutByItemName : "+ e.getMessage());
		} //End catch

	} //End isCheckedOutByName

	/** isCheckedOutByItemIndex : To Check if object is checked out by the object index
	 * @param driver
	 * @return true if object is checked out; if not false
	 * @throws Exception
	 */
	public static Boolean isCheckedOutByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {


			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			//switch (ListView.isHistoryViewOpened(driver).toString().toUpperCase()) {

			//case "TRUE" : {
			if (listView.getIconURLByItemIndex(itemIndex).toUpperCase().contains("CHECKEDOUT"))
				return true;
			else
				return false;

			//} //End case TRUE
			/*
			case "FALSE" : {
				try {listView.removeColumn(Caption.Column.Coln_CheckedOutTo.Value);} catch (Exception e) {}
				if (!listView.insertColumn(Caption.Column.Coln_CheckedOutTo.Value)) //Checks and inserts Checked out To Column to the list
					throw new Exception("Column (" + Caption.Column.Coln_CheckedOutTo.Value + ") does not inserted successfully.");

				//listView = listView.clickRefresh(); //Refresh list view

				if (!listView.getColumnValueByItemIndex(itemIndex, Caption.Column.Coln_CheckedOutTo.Value).equals(""))
					return true;
				else
					return false;

			}//End case FALSE

			default : 
				return false;

			} //End Switch
			 */
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isCheckedOutByItemIndex : "+ e.getMessage());
		} //End catch

	} //End isCheckedOutByItemIndex


	/** isSFDBasedOnObjectIcon : Checks if the object is a single file document based on its icon in the listing. 
	 * Single file documents have icons based on their filetype. The check is based on the icon URL in the style attribute.
	 * @param objectName - Name of the object to be checked
	 * @return true if SFD; false if not
	 * @throws Exception
	 */
	public Boolean isSFDBasedOnObjectIcon(String objectName) throws Exception{

		try{

			List<WebElement> itemRows = this.listingRows.findElements(By.cssSelector("tr[class*='listing-item tap']")); //Stores the web element with tr tag

			int rowCt = itemRows.size(); //Number of rows with specified attribute
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops through all the items and clicks the specified item
				if (itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']>span")).getText().toUpperCase().equalsIgnoreCase(objectName.toUpperCase()))
					break;

			if(itemIdx >= rowCt)
				throw new Exception("The specified object '" + objectName + "' was not found in listing.");

			String styleAttribute = itemRows.get(itemIdx).findElement(By.cssSelector("td>div[class='list_holder name_column']")).getAttribute("style").toString();

			//Single file objects have icon with their objects filetype. These single file object icons have the string "filetypes" in their URL
			if(styleAttribute.contains("/filetypes/") && styleAttribute.contains("background-image:") && styleAttribute.contains("url("))
				return true;
			else
				return false;
		}
		catch(Exception e){
			throw new Exception("Exception in ListView.isSFDBasedOnObjectIcon: " + e.getMessage());
		}

	}

	/** isSFDByItemName : To Check if object is Single file document by item name
	 * @param driver
	 * @return true if SFD; if not false
	 * @throws Exception
	 */
	public static Boolean isSFDByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			Utils.fluentWait(driver);

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new SkipException("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_SingleFile.Value)) //Checks and inserts Single file Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_SingleFile.Value + ") does not inserted successfully.");

			String singleFileStatus = listView.getColumnValueByItemName(itemName, Caption.Column.Coln_SingleFile.Value);

			try {listView.removeColumn(Caption.Column.Coln_SingleFile.Value);} catch (Exception e) {}

			//listView = listView.clickRefresh(); //Refresh list view

			if (singleFileStatus.toUpperCase().equals("YES"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isSFDByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End isSingleFileByItemName

	/** isSFDByItemIndex : To Check if object is Single file document by item index
	 * @param driver
	 * @return true if SFD; if not false
	 * @throws Exception
	 */
	public static Boolean isSFDByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			try {listView.removeColumn(Caption.Column.Coln_SingleFile.Value);} catch (Exception e) {}

			if (!listView.insertColumn(Caption.Column.Coln_SingleFile.Value)) //Checks and inserts Single file Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_SingleFile.Value + ") does not inserted successfully.");

			//listView = listView.clickRefresh(); //Refresh list view

			if (listView.getColumnValueByItemIndex(itemIndex, Caption.Column.Coln_SingleFile.Value).toUpperCase().equals("YES"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isSFDByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End isSingleFileByItemName

	/** getObjectTypeByItemName : To get the object type of the specified item
	 * @param driver
	 * @return Object type of an item
	 * @throws Exception
	 */
	public static String getObjectTypeByItem(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn("Object Type")) //Checks and inserts Single file Column to the list
				throw new Exception("Column (Object Type) column does not inserted successfully.");

			return(listView.getColumnValueByItemName(itemName, "Object Type"));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getObjectTypeByItem : "+ e.getMessage(), e);
		} //End catch

	} //End getObjectTypeByItemName

	/** getObjectTypeByItemIndex : To get the object type of the specified index
	 * @param driver
	 * @return Object type of an item
	 * @throws Exception
	 */
	public static String getObjectTypeByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			if (!listView.insertColumn("Object Type")) //Checks and inserts Single file Column to the list
				throw new Exception("Column (Object Type) column does not inserted successfully.");

			return(listView.getColumnValueByItemIndex(itemIndex, "Object Type"));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getObjectTypeByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End getObjectTypeByItemIndex

	/** getVersionByObjectName : To get the version of an object
	 * @param driver
	 * @param itemName Name of an object
	 * @return Version of an object
	 * @throws Exception
	 */
	public static int getVersionByObjectName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new Exception("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_Version.Value)) //Checks and inserts Single file Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_Version.Value + ") does not inserted successfully.");

			return(Integer.parseInt(listView.getColumnValueByItemName(itemName, Caption.Column.Coln_Version.Value)));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getVersionByObjectName : "+ e.getMessage(), e);
		} //End catch

	} //End getVersionByObjectName

	/** getVersionByObjectIndex :To get the version of an object with specified index
	 * @param driver
	 * @param itemIndex Index of an object
	 * @return Version of an object
	 * @throws Exception
	 */
	public static int getVersionByObjectIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			if (!listView.insertColumn(Caption.Column.Coln_Version.Value)) //Checks and inserts Single file Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_Version.Value + ") does not inserted successfully.");

			return(Integer.parseInt(listView.getColumnValueByItemIndex(itemIndex, Caption.Column.Coln_Version.Value)));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getVersionByObjectIndex : "+ e.getMessage(), e);
		} //End catch

	} //End getVersionByObjectIndex

	/** openMFDByItemName : To open MFD document by object Name
	 * @param driver Instance of the web driver
	 * @param itemName Name of the MFD document  
	 * @return true if MFD document is opened; false if not
	 * @throws Exception
	 */
	public static Boolean openMFDByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (ListView.isSFDByItemName(driver, itemName))
				throw new Exception("Object (" + itemName + ") is not an MFD document.");

			listView.doubleClickItem(itemName); //Double clicks the MFD document
			Utils.fluentWait(driver);

			if (listView.getViewCaption().equalsIgnoreCase("Files - " + itemName))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.openMFDByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End openMFDByItemName

	/** openMFDByItemIndex : To open MFD document by object index
	 * @param driver Instance of the web driver
	 * @param Index of the MFD document  
	 * @return true if MFD document is opened; false if not
	 * @throws Exception
	 */
	public static Boolean openMFDByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (ListView.isSFDByItemIndex(driver, itemIndex))
				throw new Exception("Object (" + itemIndex + ") is not an MFD document.");

			String itemName = listView.getItemNameByItemIndex(itemIndex);

			listView.doubleClickItemByIndex(itemIndex); //Double clicks the MFD document
			Utils.fluentWait(driver);

			if (listView.getViewCaption().equalsIgnoreCase("Files - " + itemName))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.openMFDByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End openMFDByItemIndex

	/** verifyCurrentView : To verify current view is opened correctly
	 * @param driver Instance of the web driver
	 * @param viewName Name of the view  
	 * @return true if view is opened; false if not
	 * @throws Exception
	 */
	public static Boolean isViewNavigated(WebDriver driver, String viewName) throws Exception {

		try {

			HomePage homePage = new HomePage(driver);
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String vaultName = xmlParameters.getParameter("VaultName");			

			if (viewName.equalsIgnoreCase(Caption.MenuItems.Home.Value) && homePage.menuBar.GetBreadCrumbItem().equalsIgnoreCase(vaultName) 
					&& driver.getCurrentUrl().toUpperCase().endsWith("VIEWS/"))
				return true;
			else if (viewName.equalsIgnoreCase("SEARCH") && driver.getCurrentUrl().toUpperCase().contains("TEMPSEARCH"))
				return true;
			else if (homePage.menuBar.GetBreadCrumbItem().toUpperCase().endsWith(viewName.toUpperCase()))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isViewNavigated : "+ e.getMessage(), e);
		} //End catch

	} //End verifyCurrentView

	/** getRandomObject : To verify current view is opened correctly
	 * @param driver Instance of the web driver
	 * @return Random object from the list in a view
	 * @throws Exception
	 */
	public static String getRandomObject(WebDriver driver) throws Exception {

		try {
			ListView listView = new ListView(driver);

			int min = 1;
			int max = listView.itemCount();

			if (max == 0)
				throw new SkipException("No objects present in the navigated view");

			Random rand = new Random();
			int randomNum = rand.nextInt((max - min) + 1) + min;

			if (randomNum == max)
				randomNum += -1;

			return (listView.getItemNameByItemIndex(randomNum));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getRandomObject : "+ e.getMessage(), e);
		} //End catch

	} //End getRandomObject

	/** getMarkedAsCompleteByItemName : To get the user who marked as completed by name of the assignment
	 * @param driver
	 * @param itemName - Assignment Name
	 * @return User Name who marked assignment as completed
	 * @throws Exception
	 */
	public static String getMarkedAsCompleteByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new SkipException("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_MarkedAsCompleteBy.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_MarkedAsCompleteBy.Value + ") column does not inserted successfully.");

			String completedUser = listView.getColumnValueByItemName(itemName, Caption.Column.Coln_MarkedAsCompleteBy.Value);
			try {listView.removeColumn(Caption.Column.Coln_MarkedAsCompleteBy.Value);} catch (Exception e) {}
			return(completedUser);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getMarkedAsCompleteByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End getMarkedAsCompleteByItemName

	/** getMarkedAsCompleteByItemIndex : To get the user who marked as completed by index of the assignment
	 * @param driver
	 * @param itemIndex - Index of the assignment
	 * @return User Name who marked assignment as completed
	 * @throws Exception
	 */
	public static String getMarkedAsCompleteByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			if (!listView.insertColumn(Caption.Column.Coln_MarkedAsCompleteBy.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_MarkedAsCompleteBy.Value + ") column does not inserted successfully.");

			String completedUser = listView.getColumnValueByItemIndex(itemIndex, Caption.Column.Coln_MarkedAsCompleteBy.Value);
			try {listView.removeColumn(Caption.Column.Coln_MarkedAsCompleteBy.Value);} catch (Exception e) {}
			return(completedUser);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getMarkedAsCompleteByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End getMarkedAsCompleteByItemIndex

	/** getRejectedByItemName : To get the user who marked as rejected assignment by name of the assignment
	 * @param driver
	 * @param itemName - Assignment Name
	 * @return User Name who rejected assignment
	 * @throws Exception
	 */
	public static String getRejectedByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (!listView.isItemExists(itemName))
				throw new SkipException("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_MarkedAsRejectedeBy.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_MarkedAsRejectedeBy.Value + ") column does not inserted successfully.");

			String rejectedUser = listView.getColumnValueByItemName(itemName, Caption.Column.Coln_MarkedAsRejectedeBy.Value);
			try {listView.removeColumn(Caption.Column.Coln_MarkedAsRejectedeBy.Value);} catch (Exception e) {}
			return(rejectedUser);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getRejectedByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End getRejectedByItemName

	/** getRejectedByItemIndex : To get the user who rejected the the assignment by index
	 * @param driver
	 * @param itemIndex - Index of the assignment
	 * @return User Name who rejected the assignment
	 * @throws Exception
	 */
	public static String getRejectedByItemIndex(WebDriver driver, int itemIndex) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (listView.itemCount() < itemIndex)
				throw new Exception("Item with index " + itemIndex + " does not exists.");

			if (!listView.insertColumn(Caption.Column.Coln_MarkedAsRejectedeBy.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (" + Caption.Column.Coln_MarkedAsRejectedeBy.Value + ") column does not inserted successfully.");

			String rejectedUser = listView.getColumnValueByItemIndex(itemIndex, Caption.Column.Coln_MarkedAsRejectedeBy.Value);
			try {listView.removeColumn(Caption.Column.Coln_MarkedAsRejectedeBy.Value);} catch (Exception e) {}
			return(rejectedUser);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getRejectedByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End getRejectedByItemIndex

	/** isApprovedByItemName : To verify if assignment is approved
	 * @param driver
	 * @param itemName - Assignment Name
	 * @return true if assignment is approved; false if not
	 * @throws Exception
	 */
	public static Boolean isApprovedByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			if (!ListView.getMarkedAsCompleteByItemName(driver, itemName).equalsIgnoreCase(""))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isApprovedByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End isApprovedByItemName

	/** isRejectedByItemName : To verify if assignment is rejected
	 * @param driver
	 * @param itemName - Assignment Name
	 * @return true if assignment is approved; false if not
	 * @throws Exception
	 */
	public static Boolean isRejectedByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			if (!ListView.getRejectedByItemName(driver, itemName).equalsIgnoreCase(""))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isRejectedByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End isRejectedByItemName

	/** getCommentsByItemName : To get the latest comments of the specified item
	 * @param driver
	 * @param itemName - Object Name
	 * @return Latest comment displayed in comments column
	 * @throws Exception
	 */
	public static String getCommentsByItemName(WebDriver driver, String itemName) throws Exception {

		try {

			ListView listView = new ListView (driver);

			//listView.clickRefresh();//Refreshes the view

			if (!listView.isItemExists(itemName))
				throw new SkipException("Item (" + itemName + ") does not exists in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_Comment.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (Comment) column does not inserted successfully.");

			return(listView.getColumnValueByItemName(itemName, Caption.Column.Coln_Comment.Value));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getCommentsByItemName : "+ e.getMessage(), e);
		} //End catch

	} //End getCommentsByItemName

	/** getCommentsByItemIndex : To get the latest comments at the specified index
	 * @param driver
	 * @param itemIdx - Index of an item
	 * @return Latest comment displayed in comments column
	 * @throws Exception
	 */
	public static String getCommentsByItemIndex(WebDriver driver, int itemIdx) throws Exception {

		try {

			ListView listView = new ListView (driver);

			if (itemIdx >= listView.itemCount())
				throw new Exception("Item Index (" + itemIdx + ") is greater the number of items in the list.");

			if (!listView.insertColumn(Caption.Column.Coln_Comment.Value)) //Checks and inserts Mark as Complete By Column to the list
				throw new Exception("Column (Comments) column does not inserted successfully.");

			return(listView.getColumnValueByItemIndex(itemIdx, Caption.Column.Coln_Comment.Value));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.getCommentsByItemIndex : "+ e.getMessage(), e);
		} //End catch

	} //End getCommentsByItemIndex


	/*--------------------------------Functions required for smoke test cases--------------------------------------*/
	public boolean isDataInListView(WebDriver driver) throws Exception
	{
		final long startTime = StopWatch.startTime();
		try{

			if(!driver.findElement(By.cssSelector("table[id='mainTable']")).isDisplayed())
				return false;
			System.out.println("hello  :"+driver.findElement(By.cssSelector("td[class='listing-column column-0']")).getText());
		}


		catch(Exception e)
		{
			e.printStackTrace();
		}
		Log.event("Successfully displayed the objects in List View.", StopWatch.elapsedTime(startTime));
		return true;

	}

	public void contextMenuList()
	{
		List<WebElement> contextMenuItems=driver.findElements(By.cssSelector("ul[id='menuSubOperations']>li"));

		for(WebElement item:contextMenuItems)
			System.out.println(item.getText());
	}

	public int groupHeaderCount() throws Exception
	{

		//Variable Declarations
		List<WebElement> groups = null;
		int count = 0;

		try {
			groups = listingTable.findElements(By.cssSelector("table[id='mainTable'] tr[class='groupHeader'],div[class='thumbnail_group_container']"));
			for(int i=0;i<groups.size();i++)
				System.out.println(groups.get(i).getText());
			count = groups.size();
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ListView.groupHeaderCount : "+ e.getMessage(), e);

		}
		return count;

	}	

	public Boolean scrollToVisibleColumn(String colnName) throws Exception {

		try {

			List <WebElement> totalColns = this.listingHeader.findElements(By.cssSelector("thead>tr>th[id='headerTable']>span"));
			int totalColnCt = totalColns.size();
			Utils.fluentWait(this.driver);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			WebElement element = this.driver.findElement(By.className("listing-container"));
			js.executeScript("arguments[0].scrollLeft = arguments[1];",element,0);
			int scrollIdx = 400;

			for( int i=0; i<totalColnCt; i++) {   

				if (totalColns.get(i).isDisplayed() && colnName.equalsIgnoreCase(totalColns.get(i).getText()))
					return true;
				else if (!totalColns.get(i).isDisplayed()) {
					js = (JavascriptExecutor) driver;
					element = this.driver.findElement(By.className("listing-container"));
					js.executeScript("arguments[0].scrollLeft = arguments[1];",element, scrollIdx);
					scrollIdx = scrollIdx + 400;
				}
			} //End for

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at ListView.scrollToVisibleColumn : "+ e.getMessage(), e);
		} //End catch

		return false;

	} //End scrollToVisibleColumn

	/**
	 * openListViewContextMenu : Right Clicks in the column header
	 * @param - None
	 * @return - None
	 * @throws Exception
	 */
	public void openListViewContextMenu()throws Exception {

		try {

			WebElement listView=driver.findElement(By.cssSelector("div[id='listingTable']"));
			Actions action = new Actions(this.driver);
			try {
				action.contextClick(listView).build().perform();
			}
			catch (Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);", listView);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.openListViewContextMenu : "+ e.getMessage(), e);
		} //End catch

	} //End function rightClickColumn
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public ListView clickViewCaptionToHomeView()throws Exception {

		try {

			//	this.listing.findElement(By.cssSelector("div[id='backButton']")).click();
			ActionEventUtils.click(driver,this.listing.findElement(By.cssSelector("div[id='backButton']")));
			Utils.fluentWait(driver);
			return new ListView(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.clickViewCaptionToHomeView : "+ e.getMessage(), e);
		} //End catch

	} //End function clickViewCaptionToHomeView

	/**
	 * isColumnValuesSorted: This function is to check the column values is sorted correctly in the list view
	 * @param ColumnValues - String list which contains the values to be checked
	 * @param dataType - Date/Integer [By default will comapre as string]
	 * @param sortOrder - ASC/DSC [Ascending/Descending]
	 * @return True/False based on the sort order
	 * @throws Exception 
	 */
	public boolean isColumnValuesSorted(List<String> columnValues, String dataType, String sortOrder) throws Exception
	{
		try{

			if (dataType.equalsIgnoreCase("Date"))
			{
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
				Date startDate, endDate;

				for (int count = 0; count < columnValues.size()-1; count++)
				{
					startDate = df.parse(columnValues.get(count));
					endDate = df.parse(columnValues.get(count+1));

					if (sortOrder.equalsIgnoreCase("DSC"))
					{
						if (startDate.compareTo(endDate) < 0)
							return false;
					}
					else
						if (startDate.compareTo(endDate) > 0)
							return false;
				}
			}
			else if(dataType.equalsIgnoreCase("Integer"))
			{
				for(int count = 0; count < columnValues.size()-1; count++)
					if (sortOrder.equalsIgnoreCase("DSC"))
					{
						if(Integer.parseInt(columnValues.get(count)) < Integer.parseInt(columnValues.get(count+1)))
							return false;
					}
					else
						if(Integer.parseInt(columnValues.get(count)) > Integer.parseInt(columnValues.get(count+1)))
							return false;
			}
			else
			{
				for(int count = 0; count < columnValues.size()-1; count++)
					if (sortOrder.equalsIgnoreCase("DSC"))
					{
						if(columnValues.get(count).compareToIgnoreCase(columnValues.get(count+1)) < 0)
							return false;
					}
					else
						if(columnValues.get(count).compareToIgnoreCase(columnValues.get(count+1)) > 0)
							return false;
			}			

			return true;

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.isColumnValuesSorted : "+ e.getMessage(), e);
		} //End catch
	}//End isColumnValuesSorted

	/**
	 * resetDisplaySettingsToDefaults: Function to reset the column settings to default
	 * @param type: resetToCommon/resetToProgram
	 */
	public void resetDisplaySettingsToDefaults(String type)throws Exception
	{
		try
		{
			this.rightClickColumn();
			this.clickColumnHeaderContextMenu("Reset Display Settings to Defaults");

			if(!MFilesDialog.exists(driver, "Reset Display Settings to Defaults"))
				throw new Exception("Reset Display Settings to Defaults dialog is not displayed.");

			MFilesDialog mfDialog = new MFilesDialog(driver, "Reset Display Settings to Defaults");//Instantiates the mfiles dialog
			mfDialog.clickButton(type);//Clicks the button in the M-Files dialog
			Utils.fluentWait(driver);

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at ListView.resetDisplaySettingsToDefaults : "+ e.getMessage(), e);
		} //End catch
	}

	/**
	 * isItemOfIndexHighlightedByText: Checks whether the item in the listview is highlighed by the search text
	 * @param highlightedText: Search text
	 * @return: Items which are all not highlighted in the list view 
	 * @throws Exception 
	 */
	public String isListviewItemsHighlightedByText(String highlightedText) throws Exception
	{
		try {

			String items = "", item = "";
			List<WebElement> listingRows = this.listing.findElements(By.cssSelector("table[id='mainTable'] tr[class*='listing-item tap']"));
			for(int i = 0; i < listingRows.size(); i++)
			{
				item = getItemNameByItemIndex(i);
				if(item.toLowerCase().contains(highlightedText.toLowerCase()))
					if(!listingRows.get(i).findElement(By.cssSelector("td span[class='highlight']")).getText().trim().equalsIgnoreCase(highlightedText))
						items +=  item + ";";
			}

			return items;//Returns the item which are all not highlighted in the list view

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ListView.isListviewItemsHighlightedByText: "+ e.getMessage(), e);
		} //End catch
	}//End of isListviewItemsHighlightedByText

} //End Class ListView
package MFClient.Wrappers;

import java.util.List;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

public class TreeView {

	//Variable Declaration
	WebDriver driver=null;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");

	/**
	 * PageFactory element initialization
	 */
	@FindBy(how=How.ID, using="tree")
	private WebElement treeview; //Stores the instance of the List

	@FindBy(how=How.CSS,using="div[class='taskpaneButton'][onclick*='COMMAND8']")
	private WebElement taskPaneBtn;

	/**
	 * TreeView : Constructor to instantiate TreeView class
	 * @param driver
	 * @throws Exception
	 */
	public TreeView(final WebDriver driver) throws Exception {
		this.driver = driver;
		PageFactory.initElements(this.driver, this);
	}

	/**
	 * isTaskPaneBtnDisplayed : Checks if Taskpane button is displayed
	 * @param driver
	 * @throws Exception
	 */
	public boolean isTaskPaneBtnDisplayed() throws Exception {

		try {

			if(taskPaneBtn.isDisplayed())
				return true;	

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at Treeview.isTaskPaneBtnDisplayed : "+e);
		} //End catch

		return false;
	} //End isTaskPaneBtnDisplayed

	/**
	 * clickTreeViewItem : This method is to click item in tree view panel
	 * @param itemToClick Item to click in tree view
	 * @return None
	 * @throws Exception
	 */
	public void clickTreeViewItem(String itemToClick) throws Exception {

		try {

			Utils.fluentWait(driver);

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			WebElement parentElement = this.treeview;

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element
				parentElement = this.treeview;
				List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li a")); //Identifies all the tree view elements that could be clicked
				int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
				int itemIdx = 0;

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim())) {
						Utils.fluentWait(driver);
						break;
					}
				if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
					throw new Exception("Item (" + items[i] + ") does not exists in the tree view.");

				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (i == itemCt-1) //Clicks the tree view item
				{
					Utils.fluentWait(driver);
					if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()", treeViewItems.get(itemIdx));
					else
						ActionEventUtils.click(driver, treeViewItems.get(itemIdx));
					//((JavascriptExecutor) driver).executeScript("arguments[0].click()",treeViewItems.get(itemIdx));
					//treeViewItems.get(itemIdx).click();	
				}
				else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()", parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));
					else
						ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));
				//((JavascriptExecutor) driver).executeScript("arguments[0].click()",parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));
				//parentElement.findElement(By.cssSelector("ins[class='jstree-icon']")).click();

				Utils.fluentWait(this.driver);

			} //End for

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.clickTreeViewItem : "+e);
		} //End catch

	}//End Function ClickTreeViewItem

	/**
	 * getChildItems : This method is to get the child items in the navigation pane
	 * @param itemToClick Item to click in tree view
	 * @return Child items of the selected item
	 * @throws Exception
	 */
	public String[] getChildItems(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;
			String[] childItems = new String[0];
			WebElement parentElement = null; 

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element
				Utils.fluentWait(driver);
				parentElement = this.treeview;
				List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li a")); //Identifies all the tree view elements that could be clicked
				int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
				int itemIdx = 0;

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim()))
						break;

				if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
					throw new Exception("Item (" + items[i] + ") does not exists in the tree view.");

				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (i == itemCt-1) {//Clicks the tree view item
					Utils.fluentWait(driver);
					ActionEventUtils.click(driver, treeViewItems.get(itemIdx));
				}
				else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));	

			} //End for

			Utils.fluentWait(this.driver);
			parentElement = this.treeview;
			List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li a")); //Identifies all the tree view elements that could be clicked
			int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[items.length-1].toUpperCase().trim()))
					break;

			parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

			if (parentElement.equals(null))
				throw new Exception("No items found.");

			int parentViewPath = Integer.parseInt(parentElement.getAttribute("aria-level").toString()) + 1;
			treeViewItems = parentElement.findElements(By.cssSelector("ul[role='group']>li[aria-level='" + parentViewPath +"'] a"));

			treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
			childItems = new String[treeViewItemCt];
			itemIdx = 0;

			for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
				childItems[itemIdx] = treeViewItems.get(itemIdx).getText().trim();

			return childItems;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.getChildItems : "+e);
		} //End catch

	}//End Function getChildItems

	/**
	 * getHomeTreeItems : This method gets all the home tree items
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public String[] getHomeTreeItems() throws Exception {

		try {

			WebElement vaultNode = this.treeview.findElement(By.cssSelector("ul[class*='jstree-no-dots']>li")); //Element of the root
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String vaultName = xmlParameters.getParameter("VaultName");	

			List<WebElement> childElements = vaultNode.findElements(By.cssSelector("ul>li a")); //Gets all the home tree items
			int childElementLen = childElements.size();
			String[] visibleElements = new String[childElementLen];

			for (int loopIdx=0; loopIdx<childElementLen; loopIdx++)//Stores all the name of the home tree items
				visibleElements[loopIdx] = childElements.get(loopIdx).getText().trim();

			visibleElements = ArrayUtils.removeElement(visibleElements, vaultName); 

			return visibleElements;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.getHomeTreeItems : "+e);
		} //End catch

	}//End Function getHomeTreeItems

	/**
	 * getRootNode : This method gets the name of the root node
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public String getRootNode() throws Exception {

		try {

			return (this.treeview.findElement(By.cssSelector("ul[class*='jstree-no-dots']>li>a")).getText().trim()); //Value of the root node

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.getRootNode : "+e);
		} //End catch

	}//End Function getRootNode

	/**
	 * clickExpandArrowIcon : This method is to click expand arrow icon of an item
	 * @param itemToClick Item to click in tree view
	 * @return None
	 * @throws Exception
	 */
	public void clickExpandArrowIcon(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			WebElement parentElement = this.treeview;

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element

				List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li>a")); //Identifies all the tree view elements that could be clicked
				int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
				int itemIdx = 0;

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim())) 
						break;

				if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
					throw new Exception("Item (" + items[i] + ") does not exists in the tree view.");

				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));
				//parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")).click();

				Utils.fluentWait(this.driver);

			} //End for

			Utils.fluentWait(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.clickExpandArrowIcon : "+e);
		} //End catch

	}//End Function clickExpandArrowIcon

	/**
	 * clickCollapseArrowIcon : This method is to click collapse arrow icon of an item
	 * @param itemToClick Item to click in tree view
	 * @return None
	 * @throws Exception
	 */
	public void clickCollapseArrowIcon(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			WebElement parentElement = this.treeview;

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element
				parentElement = this.treeview;
				List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li a")); //Identifies all the tree view elements that could be clicked
				int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
				int itemIdx = 0;

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim())) 
						break;

				if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
					throw new Exception("Item (" + items[i] + ") does not exists in the tree view.");

				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (i == itemCt-1 && parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-OPEN")) //Clicks the tree view item
					ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("i")));
				//parentElement.findElement(By.cssSelector("i")).click();				
				else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					ActionEventUtils.click(driver,parentElement.findElement(By.cssSelector("i")));
				//	parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")).click();

			} //End for

			Utils.fluentWait(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.clickCollapseArrowIcon : "+e);
		} //End catch

	}//End Function clickExpandArrowIcon

	/**
	 * getItemStatus : This method is to get the status of the item
	 * @param itemToClick Item to click in tree view
	 * @return Status as "CLOSED;OPEN;NO CHILD; NONE";
	 * @throws Exception
	 */
	public String getItemStatus(String itemToClick) throws Exception {

		try {

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			WebElement parentElement = this.treeview;

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element
				parentElement = this.treeview;
				List<WebElement> treeViewItems = parentElement.findElements(By.cssSelector("ul>li a")); //Identifies all the tree view elements that could be clicked
				int treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view
				int itemIdx = 0;

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim())) 
						break;

				if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
					throw new Exception("Item (" + items[i] + ") does not exists in the tree view.");

				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (i == itemCt-1) {//Clicks the tree view item
					if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED"))
						return "CLOSED";
					else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-OPEN"))
						return "OPEN";
					else
						return "NO CHILD";
				}
				else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("i[class*='jstree-icon']")));
				//parentElement.findElement(By.cssSelector("ins[class*='jstree-icon']")).click();

				Utils.fluentWait(this.driver);

			} //End for

			return "NONE";

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at Treeview.getItemStatus : "+e);
		} //End catch

	}//End Function getItemStatus

	/*--------------------------------Functions required for smoke test cases--------------------------------------*/
	public void clickVaultFolder(String vaultName) throws Exception
	{
		try{
			switch(vaultName.trim()){
			case "My Vault":
				WebElement myVaultFolder=driver.findElement(By.cssSelector("li[name='"+vaultName+"']>a"));
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", myVaultFolder);
				else
					ActionEventUtils.click(driver, myVaultFolder);
				//myVaultFolder.click();
				break;
			case "Sample Vault":
				WebElement sampleVaultFolder=driver.findElement(By.cssSelector("li[name='"+vaultName+"']>a"));
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", sampleVaultFolder);
				else
					ActionEventUtils.click(driver, sampleVaultFolder);
				//	sampleVaultFolder.click();
				break;
			}
			Log.message(vaultName+" Folder clicked.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.exception(new NoSuchElementException("'"+vaultName+"' not present."),driver);
		}
	}

	public void clickTaskPaneBtn() throws Exception {
		final long startTime = StopWatch.startTime();
		if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
			((JavascriptExecutor) driver).executeScript("arguments[0].click()", taskPaneBtn);
		else
			ActionEventUtils.click(driver, taskPaneBtn);
		//	taskPaneBtn.click();
		Log.event("TaskPane button clicked from TreeView.",StopWatch.elapsedTime(startTime));
	}
}

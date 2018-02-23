package MFClient.Wrappers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
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
import MFClient.Pages.LoginPage;


public class MenuBar {

	WebDriver driver=null;


	/********************************************************************
	 *	Page Factory Elements -MenuBar
	 **********************************************************************/

	@FindBy(how=How.CSS,using="div[id='menubar']")
	private WebElement menuBarPane;

	/**
	 * MenuBar : Constructor to instantiate MenuBar class
	 * @param driver
	 * @throws Exception
	 */
	public MenuBar(final WebDriver driver) throws Exception {
		this.driver=driver;
		PageFactory.initElements(this.driver, this);
	}

	/**
	 * isMenubarDisplayed : To Verify if menubar is displayed
	 * @param none
	 * @return true if menubar is displayed; if not false
	 * @throws Exception 
	 */
	public boolean isMenuInMenubarDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (this.menuBarPane.findElement(By.id("menubarbg")).isDisplayed()) { //Checks if menubar is displayed
				Log.event("Menu in MenuBar is displayed.",StopWatch.elapsedTime(startTime));
				return true;
			}

		} //End try
		catch(Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception("Exception at MenuBar.isMenuInMenubarDisplayed : "+ e.getMessage());
		} //End catch

		Log.event("Menu in MenuBar is not displayed.",StopWatch.elapsedTime(startTime));
		return false;

	} //End isMenubarDisplayed

	/**
	 * IsSettingsItemChecked : This method is to check if item is checked in settings menu
	 * @param itemToClick - Menu item which has to be checked for state
	 * @return true if selected / false if not selected
	 * @throws Exception 
	 */
	public boolean IsSettingsItemChecked(String itemToClick)throws Exception {

		//Variable Declaration
		//---------------------
		int itemIdx; //Stores the index of the items used in for loop
		int settingsMenuItemCt; //Stores the task panel item count
		String style="";

		try {

			String[] items = itemToClick.split("->");

			clickSettingsIcon(); //Clicks New Settings icon

			int itemCt = items.length;

			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));
			settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int count=0; count < itemCt; count++) {
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++) {  //Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[count].toUpperCase())) {
						if (count != itemCt-1)
							ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
						//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
						else {
							WebElement option = settingsMenuItems.get(itemIdx).findElement(By.cssSelector("div[class='menuItemIcon']"));
							style = option.getAttribute("style").toString();
						}
						break;
					}
				}

				if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
					throw new FileNotFoundException("Item (" + items[count] + ") does not exists or not enabled in the list.");
			}

			if (style.equals(""))
				return false;
			else
				return true;


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.IsSettingsItemChecked : "+ e.getMessage());
		} //End catch

	} //End function IsSettingsItemChecked

	/**
	 * clickNewMenuItem : Clicks the specified item under the new menu
	 * @param itemToClick - Menu item to be clicked
	 * @return none
	 * @throws Exception 
	 */
	public void clickNewMenuItem(String itemName) throws Exception {

		final long startTime = StopWatch.startTime();

		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String browser = xmlParameters.getParameter("driverType");

		try {
			WebElement newMenuIcon = this.menuBarPane.findElement(By.id("menuNew"));
			//ActionEventUtils.click(driver,newMenuIcon);
			if ( browser.equalsIgnoreCase("IE")|| browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()",newMenuIcon);
			else
				newMenuIcon.click();

			//newMenuIcon.click();
			Log.event("New Menu Icon clicked",StopWatch.elapsedTime(startTime));
			String[] items = itemName.split(">>");
			int itemCt = items.length;
			List<WebElement> listItems = this.driver.findElement(By.id("root-menu-div")).findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li[class*='newObject'],"
					+ "div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li[class*='NewAnnotation']"));
			//List<WebElement> contextMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li,ul[class='contextMenu']>li>div>div>span"));
			String text;
			for(int i=0; i<listItems.size(); i++){
				text = listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")).getText();
				if(text.equalsIgnoreCase(itemName)){
					//ActionEventUtils.click(driver, listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")));
					if ( browser.equalsIgnoreCase("IE")|| browser.equalsIgnoreCase("edge"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()",listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")));
					else
						listItems.get(i).findElement(By.cssSelector("div[class='menuItemText']")).click(); 

					Utils.fluentWait(driver);
					break;
				}
			}

			if (itemCt >= listItems.size()) //Checks for the existence of the item to click
				throw new Exception ("Item (" + itemName + ") does not exists or not enabled in the list.");

		}//End try
		catch(InvalidElementStateException e) { 
			throw new Exception("Unable to click 'New' option from Menubar.");

		}//End catch

	}//End clickNewMenuItem

	/**
	 * ClickSettingsItem : Clicks the specified item under the settings menu
	 * @param itemToClick - Menu item to be clicked
	 * @return none
	 * @throws Exception 
	 */
	public void ClickSettingsItem(String itemToClick)throws Exception {

		try {

			//Variable Declaration
			int itemIdx; //Stores the index of the items used in for loop
			int settingsMenuItemCt; //Stores the task panel item count

			String[] items = itemToClick.split("->");

			clickSettingsIcon(); //Clicks New Settings icon

			int itemCt = items.length;

			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));
			settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int i=0; i<itemCt; i++) {
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
						ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
						//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
						break;
					}

				if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
					throw new FileNotFoundException("Item (" + items[i] + ") does not exists or not enabled in the list.");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.ClickSettingsItem : "+ e.getMessage());
		} //End catch

	} //End function ClickSettingsItem

	/**
	 * ClickSettingsItem : Clicks the specified item under the settings menu
	 * @param itemToClick - Menu item to be clicked
	 * @return none
	 * @throws Exception 
	 */
	public void ClickUserInfo(String itemToClick)throws Exception {

		try {
			clickUserDetails();
			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='settings']"));
			int settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			int itemIdx = 0;
			for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (settingsMenuItems.get(itemIdx).getText().trim().equals(itemToClick)) {
					ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
					//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
					break;
				}

			if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
				throw new FileNotFoundException("Item (" + itemToClick + ") does not exists or not enabled in the list.");


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.ClickSettingsItem : "+ e.getMessage());
		} //End catch

	} //End function ClickSettingsItem


	/**
	 * ClickOperationsMenu : Clicks the specified item under the operations menu
	 * @param itemToClick - Menu item to be clicked
	 * @return none
	 * @throws Exception 
	 */
	public void ClickOperationsMenu(String itemToClick)throws Exception {

		try {

			if (driver == null)
				return ;

			//Variable Declaration
			WebElement menuSettings; //Stores the instance of Settings icon	
			int itemIdx; //Stores the index of the items used in for loop
			int settingsMenuItemCt; //Stores the task panel item count

			String[] items = itemToClick.split(">>");

			menuSettings = this.menuBarPane.findElement(By.id("menuSettings")); //Stores the instance of new menu bar
			ActionEventUtils.click(driver, menuSettings);
			//menuSettings.click(); //Clicks New Settings icon
			Utils.fluentWait(driver);
			int itemCt = items.length;

			/*for (int loopIdx=0; loopIdx<itemCt; loopIdx++) {

				try {
					//option = this.driver.findElement(By.xpath("//ul[@class='menu-ul innerbox']/li/div[@class='menu-item']/div/div[@class='menuItemText']"));

					option = this.driver.findElement(By.xpath("//ul[@class='menu-ul innerbox']/li/div[@class='menu-item']/div/div/span[normalize-space(.)='"+items[loopIdx]+"']"));
				}
				catch(Exception e1) {
					option = this.driver.findElement(By.xpath("//ul[@class='menu-ul innerbox']/li/div[@class='menu-item']/div/div[normalize-space(.)='"+items[loopIdx]+"']"));
				}

				if (loopIdx == itemCt-1) {						
					//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
					JavascriptExecutor executor = (JavascriptExecutor)driver;
					executor.executeScript("arguments[0].click();", option);
					Utils.fluentWait(driver);
				}
				else {
					ListView listView = new ListView(this.driver);
					listView.mouseOverContextMenu(option.getText());
					Utils.fluentWait(driver);
				}
			}*/

			List<WebElement> settingsMenuItems = this.driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div>span" +
					",div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div"));
			settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int loopIdx=0; loopIdx<itemCt; loopIdx++) {
				settingsMenuItems = this.driver.findElements(By.cssSelector("div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div>span" +
						",div.outerbox:not([style*=display]) ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div"));
				settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[loopIdx].toUpperCase())) {

						if (loopIdx == itemCt-1) {	
							ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
							//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
							/*	JavascriptExecutor executor = (JavascriptExecutor)driver;
							executor.executeScript("arguments[0].click();", settingsMenuItems.get(itemIdx));*/
							Utils.fluentWait(driver);
						}
						else {
							ListView listView = new ListView(this.driver);
							listView.mouseOverContextMenu(settingsMenuItems.get(itemIdx).getText());
							Utils.fluentWait(driver);
						}

						break;
					}
				}

				if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
					throw new FileNotFoundException("Item (" + items[loopIdx] + ") does not exists or not enabled in the list.");

			}
			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.ClickOperationsMenu : "+ e.getMessage());
		} //End catch

	} //End function ClickOperationsMenu

	/**
	 * setGroupObjectsbyObjectType : Enable/Disable the Group objects by object type
	 * @param flag - True to enable || False to disable
	 * @return none
	 * @throws Exception 
	 */
	public void setGroupObjectsbyObjectType(boolean flag)throws Exception {

		try {

			if (driver == null)
				return ;

			//Variable Declaration
			WebElement menuSettings; //Stores the instance of Settings icon	
			int itemIdx; //Stores the index of the items used in for loop
			int settingsMenuItemCt; //Stores the task panel item count

			String itemToClick = "Display Mode>>Group Objects by Object Type";
			String[] items = itemToClick.split(">>");
			String elemStyle = "";
			boolean reset = false;

			menuSettings = this.menuBarPane.findElement(By.id("menuSettings")); //Stores the instance of new menu bar
			ActionEventUtils.click(driver, menuSettings);
			Utils.fluentWait(driver);
			int itemCt = items.length;			

			List<WebElement> settingsMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div>span" +
					",ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div"));
			settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int loopIdx=0; loopIdx<itemCt; loopIdx++) {
				settingsMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div>span" +
						",ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div"));
				settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++)  {//Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[loopIdx].toUpperCase())) {
						if (loopIdx == itemCt-1) {	
							elemStyle = settingsMenuItems.get(itemIdx).findElement(By.xpath("..")).findElement(By.cssSelector("div[class*='menuItemIcon']")).getAttribute("style");
							if (elemStyle.toUpperCase().contains("CHECKED.PNG") && flag)
							{
								reset = true;
								break;
							}
							else if (!elemStyle.toUpperCase().contains("CHECKED.PNG") && !flag)
							{
								reset = true;
								break;
							}
							ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
						}
						else {
							ListView listView = new ListView(this.driver);
							listView.mouseOverContextMenu(settingsMenuItems.get(itemIdx).getText());
							Utils.fluentWait(driver);
						}

						break;
					}
				}

				if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
					throw new FileNotFoundException("Item (" + items[loopIdx] + ") does not exists or not enabled in the list.");

			}
			if (reset)
				ActionEventUtils.click(driver, menuSettings);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.setGroupObjectsbyObjectType : "+ e.getMessage());
		} //End catch

	} //End function setGroupObjectsbyObjectType

	/**
	 * GetOperationMenuItemClassName : Returns the class name of the specified menu item
	 * @param itemToCheck - Menu item to find the class
	 * @return className - Class name of the specified menu item
	 * @throws Exception 
	 */
	public String GetOperationMenuItemClassName(String itemToCheck)throws Exception {

		try {

			String className = "";

			List<WebElement> settingsMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='objectOperations']>div[class='menu-item']>div>div[class*=menuItemText]>span"));
			int settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx=0;

			for (itemIdx=0; itemIdx<settingsMenuItemCt-1; itemIdx++) {
				System.out.println(settingsMenuItems.get(itemIdx).getText());
				if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(itemToCheck.toUpperCase())) {
					className = settingsMenuItems.get(itemIdx).findElement(By.xpath("..")).getAttribute("class").toString();
					break;
				}
			}
			if (className.equals("")) //Checks for the existence of the item to click
				className = settingsMenuItems.get(itemIdx).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).getAttribute("class");

			return className;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.GetOperationMenuItemClassName : "+ e.getMessage());
		} //End catch

	}

	/**
	 * isBreadCrumbDisplayed : Checks if breadcrumb is displayed
	 * @param none
	 * @return true if breadcrumb displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isBreadCrumbDisplayed()throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement breadCrumbBar = this.menuBarPane.findElement(By.id("breadCrumb")); //Stores the instance of bread crumb bar

			if (breadCrumbBar.isDisplayed()){
				Log.event("Breadcrumb displayed.", StopWatch.elapsedTime(startTime));
				return true;
			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) { 
				Log.event("Breadcrumb is not Displayed.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else
				throw new Exception("Exception at MenuBar.isBreadCrumbDisplayed : "+ e.getMessage());
		} //End catch

		Log.event("Breadcrumb is not displayed.", StopWatch.elapsedTime(startTime));
		return false;

	} //End function isBreadCrumbDisplayed

	/**
	 * getBreadCrumbLastItem : Returns the last view item displayed in the bread crumb
	 * @param none
	 * @return lastItem - Last Text displayed in the bread crumb
	 * @throws Exception 
	 */
	public String getBreadCrumbLastItem()throws Exception {

		try {

			WebElement breadCrumbBar = this.menuBarPane.findElement(By.id("breadCrumb")); //Stores the instance of bread crumb bar
			List<WebElement> breadCrumbBarList = breadCrumbBar.findElements(By.cssSelector("li"));
			int brdCrumbBarCt = breadCrumbBarList.size(); //Gets the number of items in the breadcrumb items
			String item = breadCrumbBarList.get(brdCrumbBarCt - 1).getText().replaceAll("&nbsp;", " ").replaceAll("\n", "").replaceAll("\u00A0"," ").trim();

			return item;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.getBreadCrumbLastItem : "+ e.getMessage());
		} //End catch

	} //End function getBreadCrumbLastItem

	/**
	 * GetBreadCrumbItem : Returns the text displayed in the bread crumb
	 * @param none
	 * @return lastItem - Text displayed in the bread crumb
	 * @throws Exception 
	 */
	public String GetBreadCrumbItem()throws Exception {

		try {

			String lastItem = ""; //Stores the last item in the breadcrumb 

			WebElement breadCrumbBar = this.menuBarPane.findElement(By.id("breadCrumb")); //Stores the instance of bread crumb bar
			List<WebElement> breadCrumbBarList = breadCrumbBar.findElements(By.cssSelector("li"));

			int brdCrumbBarCt = breadCrumbBarList.size(); //Gets the number of items in the breadcrumb items
			int itemIdx = 0;

			//Loops and gets the item in the breadcrumb list
			for (itemIdx=0; itemIdx<brdCrumbBarCt; itemIdx++) {
				if (itemIdx==0)
					lastItem = breadCrumbBarList.get(itemIdx).getText().trim();
				else 
					lastItem = lastItem + ">" +breadCrumbBarList.get(itemIdx).getText().trim();
			}

			return lastItem.replaceAll("&nbsp;", " ").replaceAll("\n", "").replaceAll("\u00A0"," ").trim();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.GetBreadCrumbItem : "+ e.getMessage());
		} //End catch

	} //End function GetBreadCrumbLastItem

	/**
	 * clickBreadcrumbItem : Clicks the specified item in teh breadcrumb
	 * @param item to be clicked
	 * @return true if itme clicked else false
	 * @throws Exception 
	 */
	public Boolean clickBreadcrumbItem(String itemName)throws Exception {

		try {

			WebElement breadCrumbBar = this.menuBarPane.findElement(By.id("breadCrumb")); //Stores the instance of bread crumb bar
			List<WebElement> breadCrumbBarList = breadCrumbBar.findElements(By.cssSelector("ul li"));

			int brdCrumbBarCt = breadCrumbBarList.size(); //Gets the number of items in the breadcrumb items
			int itemIdx = 0;

			//Loops and gets the item in the breadcrumb list
			for (itemIdx=0; itemIdx<brdCrumbBarCt; itemIdx++) {
				if(breadCrumbBarList.get(itemIdx).getText().trim().equals(itemName)) {
					ActionEventUtils.click(driver, breadCrumbBarList.get(itemIdx).findElement(By.cssSelector("a")));
					//breadCrumbBarList.get(itemIdx).click();
					Utils.fluentWait(driver);
					return true;
				}
			}

			return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.clickBreadcrumbItem : "+ e.getMessage());
		} //End catch

	} //End function clickBreadcrumbItem

	/**
	 * clickBreadcrumbVaultIcon : Clicks vault icon in breadcrumb
	 * @param None
	 * @return None
	 * @throws Exception 
	 */
	public void clickBreadcrumbVaultIcon()throws Exception {

		try {

			WebElement breadCrumbBar = this.menuBarPane.findElement(By.id("breadCrumb")); //Stores the instance of bread crumb bar
			WebElement vaultIcon = breadCrumbBar.findElement(By.cssSelector("li>a[id='vaultIcon']")); //Web element vault icon
			ActionEventUtils.click(driver, vaultIcon);
			//	vaultIcon.click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.clickBreadcrumbVaultIcon : "+ e.getMessage());
		} //End catch

	} //End function clickBreadcrumbItem

	/**
	 * IsItemEnabledInOperationsMenu : Checks if menu item is enabled under operations menu
	 * @param itemToCheck - Menu item to be checked if enabled
	 * @return true if enabled / false if menu item is disabled
	 * @throws Exception 
	 */
	public Boolean IsItemEnabledInOperationsMenu(String itemToCheck)throws Exception {


		//Variable Declaration
		Boolean isEnabled = false;
		WebElement menuSettings = this.menuBarPane.findElement(By.id("menuSettings")); //Stores the instance of new menu bar

		try {

			String[] items = itemToCheck.split(">>");
			ActionEventUtils.click(driver, menuSettings);
			//menuSettings.click(); //Clicks New Settings icon

			int itemCt = items.length;
			int itemIdx = 0;

			List<WebElement> settingsMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li[class*='action']>div[class='menu-item']>div>div[class*=menuItemText],ul[class='menu-ul innerbox']>li[class*='action']>div[class='menu-item']>div>div[class*=menuItemText]>span"));
			int settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int i=0; i<itemCt-1; i++)
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++)//Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase()))
						ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
			//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu

			String className = "";

			for (itemIdx=0; itemIdx<settingsMenuItemCt-1; itemIdx++) {
				System.out.println(settingsMenuItems.get(itemIdx).getText());
				if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().startsWith(items[itemCt-1].toUpperCase())) {
					className = settingsMenuItems.get(itemIdx).getAttribute("class").toString();
					break;
				}
			}

			if(itemIdx == settingsMenuItemCt-1)
				throw new Exception("The given option " + itemToCheck + " was not listed in the menu");

			if (className.equals("")) //Checks for the existence of the item to click
				className = settingsMenuItems.get(itemIdx).findElement(By.xpath("..")).getAttribute("class");

			if (className.equals("")) //Checks for the existence of the item to click
				className = settingsMenuItems.get(itemIdx).findElement(By.xpath("..")).findElement(By.xpath("..")).findElement(By.xpath("..")).getAttribute("class");

			if (className.equals("")) {
				ActionEventUtils.click(driver, menuSettings);
				//menuSettings.click(); //Clicks New Settings icon
				//Checks for the existence of the item to click
				throw new FileNotFoundException("Item (" + items[itemCt-1] + ") does not exists or not enabled in the list.");

			}


			if (!className.toUpperCase().contains("DIMMED"))	
				isEnabled = true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.IsItemEnabledInOperationsMenu : "+ e.getMessage());
		} //End catch

		menuSettings.click(); //Clicks New Settings icon
		return isEnabled;

	} //End function IsItemEnabledInOperationsMenu



	public List<String> getEnabledOrDisabledItemsInOperationsMenu(Boolean enabledOptions) throws Exception{


		try{

			List<String> enabledItems = new ArrayList<String>();

			//Click the gear icon to open operations menu
			WebElement menuSettings = this.menuBarPane.findElement(By.id("menuSettings"));
			ActionEventUtils.click(driver, menuSettings);

			//This selector will help to select only menu items that are currently visible, so for example items in other similar menus will not be selected
			String menuDisplayedSelector = "div.outerbox:not([style*=display])";

			//The following selectors are required in order to get all different menu items in operations menu. 
			//Unfortunately there is no good common identifier for them so using 3 selectors here.
			String objectActionsOperationsMenuSelector = menuDisplayedSelector +  " ul[class='menu-ul innerbox']>li[class*=objectOperations]";
			String settingsOperationsMenuSelector = menuDisplayedSelector + " ul[class='menu-ul innerbox']>li[class*=settings]:not([class*=columnsettings])";
			String displayModeOperationsMenuSelector = menuDisplayedSelector + " ul[class='menu-ul innerbox']>li[class*=updateColumnSettingsMenu]";

			//All selectors combined to get all menu items in operations menu
			String combinedSelectorForAllOperationsMenuItems = objectActionsOperationsMenuSelector + "," + settingsOperationsMenuSelector + "," + displayModeOperationsMenuSelector;
			List<WebElement> allSettingsMenuItems = this.driver.findElements(By.cssSelector(combinedSelectorForAllOperationsMenuItems));

			//Collecting all enabled OR disabled menu items here, depending on which ones we are looking for.
			List<WebElement> specificSettingsMenuItems = new ArrayList<WebElement>();

			for(int j = 0; j < allSettingsMenuItems.size(); ++j){

				//Going deeper in element structure of the individual menu items. This element will know if it is enabled or not.
				WebElement menuItemCandidate = allSettingsMenuItems.get(j).findElement(By.cssSelector("div[class='menu-item']>div>div[class*=menuItemText]"));

				if(enabledOptions){

					//Here looking for enabled menu items

					if(!menuItemCandidate.getAttribute("class").contains("dimmed"))
						specificSettingsMenuItems.add(menuItemCandidate);
				}
				else{

					//Here looking for disabled menu items

					if(menuItemCandidate.getAttribute("class").contains("dimmed"))
						specificSettingsMenuItems.add(menuItemCandidate);
				}
			}

			//Now we have all the enabled OR disabled menu items here 
			for(int i=0; i < specificSettingsMenuItems.size(); i++){

				try{

					//Attempt to see if there is span element inside. That span element will contain the text value of the menu item
					WebElement spanElement = specificSettingsMenuItems.get(i).findElement(By.cssSelector("span:first-child"));
					enabledItems.add(spanElement.getText());
				}
				catch(Exception e){
					if(e instanceof org.openqa.selenium.NoSuchElementException){

						//Span element was not found, in that case the text value of the menu item should be in this div element.	
						String menuItemText = specificSettingsMenuItems.get(i).getText();
						enabledItems.add(menuItemText);
					}
					else
						throw new Exception("Expcetion in MenuBar.getEnabledItemsInOperationsMenu: ", e);

				}

			}
			//Close the operations menu
			ActionEventUtils.click(driver, menuSettings);
			return enabledItems;
		}
		catch(Exception e){
			throw new Exception("Exception in MenuBar.getEnabledItemsInOperationsMenu: ", e);

		}

	}

	/**
	 * IsOperationMenuItemExists : Checks if menu item exists under operations menu
	 * @param itemToClick - Menu item to be checked if exists
	 * @return true if exists / false if menu item does not exist
	 * @throws Exception 
	 */
	public Boolean IsOperationMenuItemExists(String itemToClick)throws Exception {

		//Variable Declaration
		Boolean isExists = false;

		try {

			//Variable Declaration
			WebElement menuSettings; //Stores the instance of Settings icon	
			int itemIdx; //Stores the index of the items used in for loop
			int settingsMenuItemCt; //Stores the task panel item count

			String[] items = itemToClick.split(">>");

			menuSettings = this.menuBarPane.findElement(By.id("menuSettings")); //Stores the instance of new menu bar
			ActionEventUtils.click(driver, menuSettings);
			//menuSettings.click(); //Clicks New Settings icon

			int itemCt = items.length;

			List<WebElement> settingsMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div>span" +
					",ul[class='menu-ul innerbox']>li>div[class='menu-item']>div>div"));
			settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu

			for (int i=0; i<itemCt; i++) 
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++) //Loops to identify the instance of the item to be clicked
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase())) {
						isExists = true;
						break;
					}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.IsOperationMenuItemExists : "+ e.getMessage());
		} //End catch

		return isExists;

	} //End function IsOperationMenuItemExists

	/**
	 * NewMenuItemExists : Checks if menu item exists under new menu
	 * @param itemToClick - Menu item to be checked if exists
	 * @return true if exists / false if menu item does not exist
	 * @throws Exception 
	 */
	public boolean NewMenuItemExists(String itemToClick)throws Exception {

		//Variable Declaration
		boolean existence = false;

		try {

			//Variable Declaration
			WebElement menuNew;	//Stores the instance of new menu icon
			int itemIdx; //Stores the index of the items used in for loop
			int newMenuItemCt; //Stores the task panel item count

			menuNew = this.menuBarPane.findElement(By.id("menuNew")); //Stores the instance of new menu bar
			menuNew.click(); //Clicks the New menu (+)

			List<WebElement> newMenuItems = this.driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li>div"));
			newMenuItemCt = newMenuItems.size(); //Gets the number of items in the new menu

			for (itemIdx=0; itemIdx<newMenuItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
				if (newMenuItems.get(itemIdx).getText().toUpperCase().trim().equals(itemToClick.toUpperCase())) 
					break;

			if (itemIdx == newMenuItemCt) //Checks for the existence of the item to click
				existence = false;
			else
				existence = true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.NewMenuItemExists : "+ e.getMessage());
		} //End catch

		return existence;

	} //End function NewMenuItemExists

	/**
	 * IsNewMenuDisplayed: This method is to check the existence of new menu
	 * @param none
	 * @return true if menu exists; false if menu does not exists
	 * @throws Exception
	 */
	public Boolean IsNewMenuDisplayed()throws Exception {

		//Variable Declaration
		Boolean isDisplayed = false;

		try {
			WebElement newMenuIcon=driver.findElement(By.cssSelector("li[id='menuNew']"));
			if (newMenuIcon.isDisplayed())
				isDisplayed = true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				isDisplayed = false;
			else
				throw new Exception("Exception at MenuBar.IsNewMenuDisplayed : "+ e.getMessage());
		} //End catch

		return isDisplayed;

	} //End function IsNewMenuDisplayed

	/**
	 * clickTreeViewIcon : Click Tree View Icon in the menu bar
	 * @throws Exception 
	 * @deprecated Functionality no more exists
	 */
	public void clickTreeViewIcon() throws Exception {

		try {
			final long startTime = StopWatch.startTime();
			WebElement treeViewIcon = driver.findElement(By.cssSelector("div[id='showhideTreeViewIcon']"));
			treeViewIcon.click();
			Log.event("TreeView button clicked from Menubar.",StopWatch.elapsedTime(startTime));
		}
		catch (Exception e) {
			throw new Exception("Exception at MenuBar.clickTreeViewIcon : "+ e.getMessage());
		}
	} //clickTreeViewIcon

	/**
	 * isTreeViewBtnDisplayed : Verifies if show or hide 'Tree view' button displayed
	 * @return true or false
	 * @deprecated Functionality no more exists
	 * @throws Exception 
	 */
	public boolean isTreeViewBtnDisplayed() throws Exception
	{
		boolean isDisplayed=false;

		try {	

			WebElement treeViewIcon = driver.findElement(By.cssSelector("div[id='showhideTreeViewIcon']"));
			if (treeViewIcon.isDisplayed())
				isDisplayed=true;
		}
		catch(Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")|e.getClass().toString().contains("NullPointerException"))
				return false;
			else 
				throw new Exception("Exception at MenuBar.isTreeViewBtnDisplayed : "+ e.getMessage());

		}

		return isDisplayed;
	}

	/**
	 * verifyLoggedInUser: Verify if User logged in to application
	 * @param userName - Name of the user
	 * @return true if logged with the specified user; if not false
	 * @throws Exception
	 */
	public boolean verifyLoggedInUser(String userName) throws Exception
	{

		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("span[id='userNameForDisplay']"));

			if (loggedInUser.getAttribute("title").trim().equalsIgnoreCase(userName.trim()))
				return true;

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at MenuBar.verifyLoggedInUser : "+ e.getMessage());
		} //End catch

		return false;

	} //End isLoggedIn

	/**
	 * getLoggedInUserName: Gets the logged in user name
	 * @return Logged in user name
	 * @throws Exception
	 */
	public String getLoggedInUserName() throws Exception
	{
		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("span[id='userNameForDisplay']"));
			return loggedInUser.getAttribute("title").trim();

		} //End try
		catch(Exception e) {
			if(e.getClass().toString().toLowerCase().contains("nosuchelementexception"))
				return "";
			else
				throw new Exception("Exception at MenuBar.verifyLoggedInUser : "+ e.getMessage());
		} //End catch

	} //End getLoggedInUserName

	/**
	 * getRegValueForDisplayMode: This method gets the registry value for display mode
	 * @param none
	 * @return Registry value for display mode
	 * @throws Exception
	 */
	public static String getRegValueForDisplayMode()throws Exception {

		try {

			String regPath = "Software\\Motive\\M-Files\\10.2.3920.84\\Server\\MFWA\\Data\\{3362A3B2-269B-4C22-9B93-EE4ABA98D5C2}\\ViewSettings\\20\\_searchfolder";
			String regItem = "ViewMode";
			return(Utility.readRegistry(regPath, regItem));

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MenuBar.getRegValueForDisplayMode : "+ e.getMessage());
		} //End catch


	} //End function getRegValueForDisplayMode

	/**
	 * clickLogOut: Clicks Log out from user display
	 * @param userName - Name of the user
	 * @return true if logged with the specified user; if not false
	 * @throws Exception
	 */
	public void clickLogOut() throws Exception
	{

		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("ul[id='user']>li span[id='userNameForDisplay']"));
			ActionEventUtils.click(driver, loggedInUser);
			//loggedInUser.click();

			WebElement logOutOption=driver.findElement(By.cssSelector("li[class*='LogOut']>div>div>div[class='menuItemText']"));
			ActionEventUtils.click(driver,logOutOption);
			//logOutOption.click();

			/*WebElement logOutElement = this.driver.findElement(By.cssSelector("li[class*='LogOut']"));
			logOutElement.click();*/

			/*	WebElement option = this.driver.findElement(By.xpath("//ul[@class='menu-ul innerbox']/li/div[@class='menu-item']/div/div/span[normalize-space(.)='Log Out']"));
			option.click();

			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));
			int settingsMenuItemCt = settingsMenuItems.size(); //Gets the number of items in the new menu
			int itemIdx = 0;
				for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++) { //Loops to identify the instance of the item to be clicked
					System.out.println(settingsMenuItems.get(itemIdx).getText());
					if (settingsMenuItems.get(itemIdx).getText().toUpperCase().trim().equals("LOG OUT")) {
						ActionEventUtils.click(driver, settingsMenuItems.get(itemIdx));
						//settingsMenuItems.get(itemIdx).click(); //Clicks the item in Settings menu
						break;
					}
				}

				if (itemIdx >= settingsMenuItemCt) //Checks for the existence of the item to click
					throw new FileNotFoundException("Item (Log out) does not exists or not enabled in the list.");*/


		} //End try
		catch(Exception e) {
			throw new Exception("Exception at MenuBar.clickLogOut : "+ e.getMessage());
		} //End catch

	} //End clickLogOut

	/**
	 * isItemEnabledInUserSettings : Mouse hover the 'Vaults' options in Menubar 
	 * @param none
	 * @return none
	 */
	public final boolean isItemEnabledInUserSettings(String itemToCheck) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("ul[id='user']>li span[id='userNameForDisplay']"));//Click the username in menubar
			ActionEventUtils.click(driver, loggedInUser);

			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));
			int settingsMenuItemCt = settingsMenuItems.size();
			int itemIdx = 0;

			for (itemIdx=0; itemIdx < settingsMenuItemCt; itemIdx++) //Loops to iterate through the items in the menu
				if(settingsMenuItems.get(itemIdx).isDisplayed())//Checks whether the element is displayed
					if (settingsMenuItems.get(itemIdx).getText().trim().equals(itemToCheck)) //Checks whether the expected item is exists
						return true;//Returns item is exist in the user settings

			Log.event("MenuBar.isItemEnabledInUserSettings : Checked whether the item '" + itemToCheck + "' is exists or not.", StopWatch.elapsedTime(startTime));

			return false;//Returns item is not exist in the user settings			

		}//End try

		catch(Exception e) {
			throw new Exception("Exception at MenuBar.isItemEnabledInUserSettings : "+ e.getMessage());
		}//End catch

	}//End of isItemEnabledInUserSettings


	/**
	 * selectMenuItemVault : Mouse hover the 'Vaults' options in Menubar 
	 * @param none
	 * @return none
	 */
	public final void selectMenuItemVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("ul[id='user']>li span[id='userNameForDisplay']"));//Click the username in menubar
			ActionEventUtils.click(driver, loggedInUser);

			List<WebElement> settingsMenuItems = driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));
			int settingsMenuItemCt = settingsMenuItems.size();
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<settingsMenuItemCt; itemIdx++) //Loops to identify the instance of the item to be clicked
				if (settingsMenuItems.get(itemIdx).getText().trim().equals("Vaults")) {
					Actions action = new Actions(driver);
					action.moveToElement(settingsMenuItems.get(itemIdx)).build().perform();//Perform the mouse hover action in Vaults option 
					break;
				}

			Log.event("MenuBar.selectMenuItemVault : Select the 'Vaults' command in menu bar.",StopWatch.elapsedTime(startTime));

		}//End try
		catch(Exception e) {
			throw new Exception("Exception at MenuBar.selectMenuItemVault : "+ e.getMessage());
		}//End catch
	}//selectMenuItemVault

	/**
	 * getVaultList : get the all vault name which listed in menubar
	 * 
	 * @return noOfVaults : Vault listed in the User name list
	 * @throws Exception 
	 */
	public final String[] getVaultList() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			List<WebElement> vaultName = driver.findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li>div>div[class='vaultitem']>div[class='menuItemText']"));
			int vaultList = vaultName.size();
			int itemIndx = 0;

			String [] noOfVaults = new String[vaultList];

			for (itemIndx=0; itemIndx<vaultList; itemIndx++)
				noOfVaults[itemIndx] = vaultName.get(itemIndx).getText().trim();

			Log.event("MenuBar.getVaultList : Get the all vaults are listed in task pane.",StopWatch.elapsedTime(startTime));

			return noOfVaults;
		}//End try
		catch(Exception e){
			throw new Exception("Exception at MenuBar.getVaultList : "+ e.getMessage());
		}//End catch

	}//End getVaultList

	/**
	 * selectVault : Select the specified vault from the vault list which displayed in Menubar
	 * @param vault
	 * @throws Exception
	 */
	public final void selectVault(String vault) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			List<WebElement> vaultName = driver.findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li>div>div[class='vaultitem']>div[class='menuItemText']"));
			int vaultList = vaultName.size();

			for(int itemIndx=0;itemIndx<vaultList;itemIndx++)
				if(vaultName.get(itemIndx).getText().equalsIgnoreCase(vault)){//Check if vault name is displayed as same
					vaultName.get(itemIndx).click();
					break;
				}//End if

			Log.event("MenuBar.selectVault : Select the specified vault from the menu bar.",StopWatch.elapsedTime(startTime));

		}//End try
		catch(Exception e){
			Log.exception(e);
		}//End catch
	}//End selectVault


	/**
	 * getVaultNameInBreadCrumb : Get the vault name which displayed in breadcrumb
	 * @return vaultName
	 * @throws Exception
	 */
	public final String getVaultNameInBreadCrumb() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			WebElement breadCrumb = driver.findElement(By.cssSelector("div[id='breadCrumb']"));
			WebElement vault = breadCrumb.findElement(By.cssSelector("ul[class='breadCrumbList']>li"));
			Utils.fluentWait(driver);

			String vaultName = vault.getText();
			Log.event("MenuBar.getVaultNameInBreadCrumb : Select the vault name which displayed in breadcrumb.",StopWatch.elapsedTime(startTime));
			return vaultName;

		}//End try
		catch(Exception e){
			throw new Exception("Exception at MenuBar.getVaultNameInBreadCrumb : "+ e.getMessage());
		}//End catch
	}//End 	getVaultListInBreadCrumb



	/**
	 * changePassword: Clicks Change Password link and enter new password
	 * @param oldPassword - Old Password
	 * @param newPassword - New Password
	 * @return None
	 * @throws Exception
	 */
	public void changePassword(String oldPassword, String newPassword) throws Exception
	{

		try {

			WebElement loggedInUser = this.menuBarPane.findElement(By.cssSelector("ul[id='user']>li span[id='userNameForDisplay']"));
			ActionEventUtils.click(driver, loggedInUser);

			WebElement changePwd=driver.findElement(By.cssSelector("li[class*='ChangePassword']>div>div>div[class='menuItemText']"));
			//changePwd.click();
			ActionEventUtils.click(driver,changePwd);

			MFilesDialog mfilesDialog = new MFilesDialog (driver);
			mfilesDialog.changePassword(oldPassword, newPassword);			

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at MenuBar.changePassword : "+ e.getMessage());
		} //End catch

	} //End changePassword


	/**
	 * istickMarkdisplayed : This function used to verify the tick mark is displayed or not in specified vault
	 * 
	 * @param vaultName
	 * @return
	 * @throws Exception
	 */
	public Boolean istickMarkdisplayed(String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement tickmark = driver.findElement(By.cssSelector(""));

			if(tickmark.isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at Menubar.istickMarkdisplayed : " +e);
		} //End catch

		finally {
			Log.event("Verified if property exists in MetadataCard dialog.", StopWatch.elapsedTime(startTime));
		}

	} //propertyExists




	/*--------------------------------Functions required for smoke test cases--------------------------------------*/

	/**
	 * selectFromOperationsMenu: This Method selects the operations menu item 
	 * @param itemToClick - Item to be clicked in the context menu(eg:Rename)
	 * @return none
	 * @throws Exception 
	 */
	public Boolean selectFromOperationsMenu(String itemToClick) throws Exception {
		final long startTime = StopWatch.startTime();
		//Click the 'Settings' options on menubar	
		clickSettingsIcon();

		try	{

			WebElement listItems=driver.findElement(By.cssSelector("li[class*='"+itemToClick+"']>div>div[id='menu_"+itemToClick+"']>div[class='menuItemText']"));
			listItems.click();
			Utils.fluentWait(driver);
			if(itemToClick.trim().equalsIgnoreCase("CheckOut") || itemToClick.trim().equalsIgnoreCase("CheckIn")) {
				//Wait until waitOverlay disappears
				new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
				.pollingEvery(125,TimeUnit.MILLISECONDS)
				.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));
			}

			Log.event("Successfully selected the Operations->"+listItems,StopWatch.elapsedTime(startTime));
			if(!listItems.isDisplayed())
				return true;
		}
		catch(InvalidElementStateException | NullPointerException | NoSuchElementException e) {
			if(e.getClass().toString().contains("NoSuchElementException")) {
				throw new Exception("Unable to find "+itemToClick+" from Operations menu, check if it is disabled.");
			}
			else
				return false;
			//e.printStackTrace();
		}
		return false;
	}

	/**
	 * clickSettingsMenuItems: This Method selects the settings menu item 
	 * @param itemToClick - Item to be clicked in the context menu
	 * @return none
	 * @throws Exception 
	 */
	public void clickSettingsMenuItems(String itemToClick) {
		final long startTime = StopWatch.startTime();
		try {
			//Click the 'Settings' options on menubar	
			clickSettingsIcon();
			String[] itemToSelect=itemToClick.split("->");
			List<WebElement> listItems=driver.findElements(By.cssSelector("ul[class='menu-ul innerbox']>li"));

			for (int j=0;j<itemToSelect.length;j++) {
				for (int i=0;i<listItems.size();i++) {
					System.out.println("ActualString :"+listItems.get(i).getText().toLowerCase().trim());
					System.out.println("InputString :"+itemToSelect[j].toLowerCase().trim());
					if (listItems.get(i).getText().toLowerCase().trim().equals(itemToSelect[j].toLowerCase().trim())) {
						JavascriptExecutor executor = (JavascriptExecutor)driver;
						executor.executeScript("arguments[0].click();", listItems.get(i));
						//listItems.get(i).click();
						Utils.fluentWait(this.driver);
						Log.event("Successfully selected the Settings->"+itemToClick,StopWatch.elapsedTime(startTime));

						break;
					}
				}
				if (j>=itemToSelect.length) {
					break;
				}
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * clickSettingsIcon: This Method Clicks the Settings icon 
	 * @param none
	 * @return none
	 * @throws Exception 
	 */
	public void clickSettingsIcon() throws Exception {
		final long startTime = StopWatch.startTime();
		try	{
			WebElement settingsIcon=driver.findElement(By.cssSelector("li[id='menuSettings']"));
			ActionEventUtils.click(driver, settingsIcon);
			//settingsIcon.click();
			Utils.fluentWait(this.driver);
			Log.event("'Settings' Menu is clicked from top right corner",StopWatch.elapsedTime(startTime));
		}
		catch(Exception e){
			Log.exception(new Exception("Settings menu not displayed."),driver);
			//e.printStackTrace();
		}
	}

	/**
	 * logOutFromMenuBar: Clicks the Logout link in the menubar 
	 * @param none
	 * @return Object of LoginPage Class
	 * @throws Exception 
	 */
	public LoginPage logOutFromMenuBar() throws Exception
	{
		try{

			//clicks UserInfo on top right corner
			clickUserDetails();
			Utils.fluentWait(driver);
			//Clicks the "LogOut' option from UserInfo
			WebElement logOutOption=driver.findElement(By.cssSelector("li[class*='LogOut']>div>div>div[class='menuItemText']"));
			ActionEventUtils.click(driver, logOutOption);
			//logOutOption.click();
			Utils.isLogOutPromptDisplayed(driver);

			//Utils.waitForPageLoad(driver);
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[id='logininfo']")));

		}
		catch(Exception e){
			Log.exception(new Exception("Error while Logging Out from User Info."),driver);
			//e.printStackTrace();
		}
		return new LoginPage(driver);
	}

	/**
	 * clickUserDetails: Clicks the User name in the menubar 
	 * @param none
	 * @return none
	 * @throws Exception 
	 */
	public void clickUserDetails() throws Exception
	{
		try{

			//clicks on userName on top right corner
			WebElement userInfo=driver.findElement(By.cssSelector("div[id='logininfo']>ul[id='user']>li"));
			ActionEventUtils.click(driver, userInfo);
			//userInfo.click();

		}
		catch(Exception e){
			throw new Exception("Exception at MenuBar.clickUserDetails: "+e.getMessage(), e);
		}
	}

	/**
	 * selectDisplayModeSettingOptions: Clicks the specified menu under Display Mode settings 
	 * @param option -  Display mode menu to be selected
	 * @return none
	 * @throws Exception 
	 */
	public void selectDisplayModeSettingOptions(String option) throws Exception
	{

		final long startTime=StopWatch.startTime();
		try {

			clickSettingsIcon();
			clickDisplayModeItems();
			//	option=option.replace(" ","").trim();
			WebElement actionItem=driver.findElement(By.cssSelector("li[class*='"+option+"']"));
			actionItem.findElement(By.cssSelector("div[class='menuItemText']"));

			Actions action=new Actions(driver);
			action.click(actionItem).build().perform();

			Log.event("Successfully selected :"+option+" Setting option",StopWatch.elapsedTime(startTime));	
		}
		catch(Exception e){
			Log.exception(new Exception("Could not select the 'DisplayMode' options."),driver);
		}
	}

	/**
	 * clickDisplayModeItems: Clicks the 'Display Mode' option 
	 * @param none
	 * @return none
	 * @throws Exception 
	 */
	public void clickDisplayModeItems() throws Exception
	{
		try{
			WebElement displayModeItem=driver.findElement(By.cssSelector("li[class*='updateColumnSettingsMenu']>div>div>div[class='menuItemText']"));
			Actions action=new Actions(driver);
			action.clickAndHold(displayModeItem).build().perform();

		}
		catch(NoSuchElementException e){
			Log.exception(new Exception("Unable to click 'DisplayMode'"),driver);
		}
	}

	public LoginPage logOutFromMenuBar1() throws Exception
	{
		try{

			//clicks UserInfo on top right corner
			clickUserDetails();
			//Clicks the "LogOut' option from UserInfo
			WebElement logOutOption=driver.findElement(By.cssSelector("li[class*='LogOut']>div>div>div[class='menuItemText']"));
			ActionEventUtils.click(driver, logOutOption);
			//logOutOption.click();

			Utils.isLogOutPromptDisplayed(driver);
		}
		catch(Exception e){
			Log.exception(new Exception("Error while Logging Out from User Info."),driver);
		}
		return new LoginPage(driver);
	}

	public void changePasswordFromMenuBar(String oldPwd,String newPassword) throws Exception
	{
		try{

			//clicks UserInfo on top right corner
			clickUserDetails();

			//Clicks the "ChangePassword' option from UserInfo
			WebElement changePwd=driver.findElement(By.cssSelector("li[class*='ChangePassword']>div>div>div[class='menuItemText']"));
			ActionEventUtils.click(driver, changePwd);
			//changePwd.click();


			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement((By.cssSelector("div[class='window ui-dialog-content ui-widget-content']")))));

			List<WebElement> passwordFields=driver.findElements(By.cssSelector("div[class='changePassword']>input[type='password']"));
			WebElement txtOldPassword=passwordFields.get(0);
			WebElement txtNewPassword=passwordFields.get(1);
			WebElement txtConfirmPassword=passwordFields.get(2);

			txtOldPassword.sendKeys(oldPwd);
			txtNewPassword.sendKeys(newPassword);
			txtConfirmPassword.sendKeys(newPassword);

			HomePage homePage=new HomePage(driver);
			homePage.clickOKButton();

			Utils.fluentWait(driver);
			//Confirm the password update dialog
			homePage.clickOKButton();
		}
		catch(Exception e){
			Log.exception(new Exception("Error while Changing Password from User Info."),driver);
		}
	}

} //End class MenuBar

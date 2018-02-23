package MFClient.Wrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import genericLibrary.ActionEventUtils;
import genericLibrary.FileDownloader;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class TaskPanel {

	final WebDriver driver;

	@FindBy(how=How.ID, using="taskpane")
	private WebElement taskPane; //Stores the instance of the List

	@FindBy(how=How.ID, using="rightPanel")
	private WebElement rightPanel; //Stores the instance of the List

	/**
	 * TaskPanel : Constructor to instantiate TaskPanel wrapper 
	 * @return none
	 * @author Aspire Systems Merlin-QA Automation
	 * @throws Exception 
	 */
	public TaskPanel(final WebDriver driver) throws Exception {
		try {

			this.driver = driver;
			PageFactory.initElements(this.driver, this);

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception TaskPanel.TaskPanel : ", e);
		} //End catch

	} //End TaskPanel


	public boolean isNewMenuItemExpand() throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			//Expand the 'New' menu item in task pane
			WebElement taskpaneNewHeader = this.taskPane.findElement(By.cssSelector("div[class='taskpaneHeader New']")); //Expand the 'New' menu item in task pane
			WebElement expandTaskpaneNewHeader = taskpaneNewHeader.findElement(By.cssSelector("div>span[class*='ExpandIcon']"));

			if(expandTaskpaneNewHeader.isDisplayed())
				return true;
			else 
				return false;

		}//End try
		catch (Exception e) {
			Log.event("Task pane 'New' header item is not expanded.", StopWatch.elapsedTime(startTime));
			return false;
		}//End catch 
	}//End expandNewItem



	/**
	 * clickItem : This method is to click the item in the Taskpanel
	 * @param itemToClick - Item to be clicked from Taskpanel
	 * @return none
	 * @author Aspire Systems Merlin-QA Automation
	 */
	public Boolean clickItem(String itemToClick) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*WebElement elementToClick = this.taskPane.findElement(By.xpath("//div[contains(@class, 'taskpaneItemText') and normalize-space(.)='" + itemToClick + "']"));
			elementToClick.click();
			Utils.fluentWait(driver);
			Log.event("TaskPanel.clickItem : " + itemToClick + " is clicked.", StopWatch.elapsedTime(startTime));
			return true;*/

			if(!this.isItemExists(itemToClick)){

				if(this.isNewMenuItemExpand()){//Expand the new menu item in task pane
					WebElement taskpaneNewHeader = this.taskPane.findElement(By.cssSelector("div[class='taskpaneHeader New']")); //Expand the 'New' menu item in task pane
					WebElement expandTaskpaneNewHeader = taskpaneNewHeader.findElement(By.cssSelector("div>span[class*='ExpandIcon']"));
					//expandTaskpaneNewHeader.click();
					ActionEventUtils.click(driver,expandTaskpaneNewHeader);
				}
			}
			//	List<WebElement> taskPaneItems = this.taskPane.findElements(By.cssSelector("div.taskpaneItemText,span.taskpaneItemText")); //Stores the instance of the Taskpane
			List<WebElement> taskPaneItems = this.taskPane.findElements(By.cssSelector("div.taskpaneItemText,span.taskpaneItemText")); //Stores the instance of the Taskpane
			int taskPaneItemCt = taskPaneItems.size(); //Gets the number of items in the task pane

			for (int itemIdx=0; itemIdx<taskPaneItemCt; itemIdx++) //Loops to identify the instance of the item to be clicked
				if (taskPaneItems.get(itemIdx).getText().toUpperCase().trim().equals(itemToClick.toUpperCase())) {
					ActionEventUtils.click(driver, taskPaneItems.get(itemIdx));
					//	taskPaneItems.get(itemIdx).click(); //Clicks the item in task pane
					Utils.fluentWait(this.driver);
					if(!itemToClick.equals(Caption.MenuItems.UndoCheckOut.Value))
						Utils.fluentWait(this.driver);

					Log.event("clickItem : " + itemToClick + " is clicked.", StopWatch.elapsedTime(startTime));
					return true;
				}

			Log.event("clickItem : " + itemToClick + " is not available to click.", StopWatch.elapsedTime(startTime));
			return false;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException"))  {
				Log.event("TaskPanel.clickItem : " + itemToClick + " is not availble to click.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else
				throw new Exception("Exception at TaskPanel.clickItem : "+ e.getMessage());
		} //End catch

	} //End function ClickItem

	/**
	 * isItemExists : This method is to check the existence of the item in the Taskpanel
	 * @param itemToCheck - Item to be checked for existence from Taskpanel
	 * @return true if item exists; false if item does not exists
	 * @author Aspire Systems Merlin-QA Automation
	 */
	public Boolean isItemExists(String itemToCheck) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*if (this.taskPane.findElement(By.xpath("//*[contains(@class, 'taskpaneItemText') and normalize-space(.)='" + itemToCheck + "']")).isDisplayed()) {
				Log.event("TaskPanel.isItemExists : " + itemToCheck + " exists.", StopWatch.elapsedTime(startTime));
				return true;
			}
			else {
				Log.event("TaskPanel.isItemExists : " + itemToCheck + " is not exists.", StopWatch.elapsedTime(startTime));
				return false;
			}*/



			List<WebElement> taskPaneItems = this.taskPane.findElements(By.cssSelector("div.taskpaneItemText,span.taskpaneItemText")); //Stores the instance of the Taskpane
			int taskPaneItemCt = taskPaneItems.size(); //Gets the number of items in the task pane
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<taskPaneItemCt; itemIdx++) { //Loops to identify the instance of the item to be clicked
				if (taskPaneItems.get(itemIdx).getText().toUpperCase().trim().equals(itemToCheck.toUpperCase())) {
					if(taskPaneItems.get(itemIdx).isDisplayed())
						return true;
				}
			}

			if (itemIdx >= taskPaneItemCt) //Checks for the existence of the item
				return false;

			return false;
		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) {
				Log.event("TaskPanel.isItemExists : " + itemToCheck + " is not exists.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else
				throw new Exception("Exception at TaskPanel.isItemExists : "+ e.getMessage());
		} //End catch


	} //End function IsItemExists

	/**
	 * isAppletEnabled : This method is to check if Applet is enabled
	 * @param none
	 * @return true if applet enabled; false if applet not enabled
	 * @author Aspire Systems Merlin-QA Automation
	 */	
	public Boolean isAppletEnabled() throws Exception {

		try {

			WebElement applet; 

			if (Utility.getBrowserName(this.driver).contains("internet explorer"))
				applet = this.rightPanel.findElement(By.cssSelector("div[id='applet']>object[id='appletObject']"));
			else
				applet = this.rightPanel.findElement(By.cssSelector("div[id='applet'] applet[id='appletObject']"));

			if (applet.isEnabled())
				return true;

			return false;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at TaskPanel.isAppletEnabled : "+ e.getMessage());
		} //End catch

		//	return false;

	} //End function IsAppletEnabled

	/**
	 * IsTaskPaneGoToDisplayed : This method is to check if task pane only with GoTo items is displayed
	 * @param none
	 * @return true if task pane GoTO displayed; false if task pane GoTO not displayed
	 * @author Aspire Systems Merlin-QA Automation
	 */
	public Boolean isTaskPaneGoToDisplayed() throws Exception {

		try {

			//WebElement taskPaneGoto = this.driver.findElement(By.cssSelector("div[id='page']>div[id='panel']>div[class='goTo_container ui-layout-south ui-layout-pane ui-layout-pane-south']>div[id='taskpaneGoTo']"));
			WebElement taskPaneGoto = this.driver.findElement(By.cssSelector("div[id=\"taskpaneGoTo\"]"));

			if (taskPaneGoto.isDisplayed())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at TaskPanel.isTaskPaneGoToDisplayed : "+ e.getMessage());
		} //End catch

	} //End function isTaskPaneGoToDisplayed

	/**
	 * isTaskPaneNewDisplayed : This method is to check if task pane with New items is displayed
	 * @param none
	 * @return none
	 * @author Aspire Systems Merlin-QA Automation
	 */
	public boolean isTaskPaneNewDisplayed() throws Exception  {

		try {

			WebElement newCommandOnTaskPane=driver.findElement(By.cssSelector("div[id='taskpaneNew'][class='taskpaneArea']"));

			if (newCommandOnTaskPane.isDisplayed()) 
				return true;

		} //End try

		catch(Exception e) 	{
			if(e.getClass().toString().contains("NoSuchElementException"))
				return false;
			else
				throw new Exception("Exception at TaskPanel.isTaskPaneNewDisplayed : "+ e.getMessage());
		} //End catch

		return false;

	} //isTaskPaneNewDisplayed

	/**
	 * getWorkflowStates : Gets the listed workflow in taskpane
	 * @param None
	 * @return Available workflow in the taskpane list
	 * @throws Exception
	 */
	public List<String> getWorkflowStates() throws Exception {

		final List <String> elementStates = new ArrayList <String>();

		try {

			List<WebElement> moveToStates= driver.findElements(By.cssSelector("div[id='taskpaneStates']:not([style='display: none;'])>div[class='childItems']>div[class*='changeState']>div[class='taskpaneItemText']"));

			for(WebElement state:moveToStates)
				elementStates.add(state.getText());

		} //End try
		catch(Exception e) 	{
			Log.exception(new Exception("Unable to find/display the Workflow states in taskpane."));
		} //End catch

		return elementStates;

	} //getWorkflowStates

	/**
	 * collapseItem : This method collapses the given menu
	 * @param itemName - name of the sub-category
	 * @return true if expanded / false if collapsed
	 */
	public boolean collapseItem(String itemName) throws Exception  {

		try {

			List<WebElement> taskPaneHeader = this.taskPane.findElements(By.cssSelector("div[id='taskpaneContent']>div>div[class*='taskpaneHeader']"));
			String iconStatus = "";

			for (int itemIdx = 0; itemIdx<taskPaneHeader.size(); itemIdx++) {

				if (taskPaneHeader.get(itemIdx).getText().trim().equalsIgnoreCase(itemName)) {

					if (taskPaneHeader.get(itemIdx).findElement(By.cssSelector("div[class='taskpaneItemText']>span")).getAttribute("class").toString().contains("taskpaneHeaderCollapseIcon")) {
						//	taskPaneHeader.get(itemIdx).click();
						ActionEventUtils.click(driver,taskPaneHeader.get(itemIdx));

						if (taskPaneHeader.get(itemIdx).findElement(By.cssSelector("div[class='taskpaneItemText']>span")).getAttribute("class").toString().contains("taskpaneHeaderExpandIcon"));
						return true;
					} //End if
					else
						return false;
				} //End if

			} //End for

			if (iconStatus.equals("")) //Checks if item exists in the list
				throw new Exception ("Takpanel header item (" + itemName + ") does not exists.");


		} //End try

		catch(Exception e) 	{
			throw new Exception("Exception at TaskPanel.collapseItem : "+ e.getMessage());
		} //End catch

		return false;

	} //collapseItem

	/**
	 * isHeaderExpanded : This method checks if the given category is in expanded state
	 * @param itemName - name of the sub-category
	 * @return true if expanded / false if collapsed
	 */
	public boolean isHeaderExpanded(String itemName) throws Exception  {

		try {

			List<WebElement> newCommandOnTaskPane = this.driver.findElements(By.cssSelector("div[id='taskpane']>div[id='taskpaneContent']>div[class='taskpaneArea']>div[class*='taskpaneHeader'],"+
					"div[id='taskpane']>div[id='taskpaneContent']>div>div[class='taskpaneArea']>div[class='taskpaneHeader'],div[id='taskpane']>div[id='taskpaneContent']>div[class*='taskpaneArea'][style*='display: block']>div[class*='taskpaneHeader']"));

			for(int count = 0; count < newCommandOnTaskPane.size(); count++){
				if(newCommandOnTaskPane.get(count).getText().trim().equalsIgnoreCase(itemName)){
					if(newCommandOnTaskPane.get(count).findElement(By.cssSelector("div[class='taskpaneItemText']>span")).getAttribute("class").toString().contains("taskpaneHeaderCollapseIcon"))
						return true;
					else
						return false;
				}

			}

			throw new Exception("The Given item " + itemName + " does not exist in the task pane.");

		} //End try

		catch(Exception e) 	{
			throw new Exception("Exception at TaskPanel.isHeaderExpanded : "+ e.getMessage());
		} //End catch

	} //getExpandedState

	/**
	 * isAppletEnabled : This method Clicks the item in the Taskpane
	 * @param driverType Type of driver
	 * @return true if applet is enabled; if not false
	 * @throws Exception
	 */
	public Boolean isAppletEnabled(String driverType) throws Exception {

		try {

			WebElement applet = null;

			if (driverType.equalsIgnoreCase("internet explorer"))
				applet = driver.findElement(By.cssSelector("div[id='applet']>object[id='appletObject']"));
			else
				applet = driver.findElement(By.cssSelector("div[id='applet']>applet[id='appletObject']"));

			if (applet.isEnabled())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw new Exception("Exception at TaskPanel.isAppletEnabled : "+ e.getMessage());
		} //End catch

		return false;

	} //End of IsAppletEnabled

	/**
	 * markApproveReject : This method is to Approve or reject from taskpanel
	 * @param itemToSelect Approve- To Approve or Mark Complete; Reject-To Reject
	 * @return None
	 * @throws Exception
	 */
	public void markApproveReject(String itemToSelect) throws Exception {

		try {

			switch (itemToSelect.toUpperCase()) {

			case "APPROVE" : { //Select Approve from taskpanel

				if (this.isItemExists(Caption.MenuItems.Approve.Value))
					this.clickItem(Caption.MenuItems.Approve.Value);
				else 
					this.clickItem(Caption.MenuItems.MarkComplete.Value);

				Utils.fluentWait(driver);					
				break;					

			} //End Case:Approve

			case "MARK APPROVED" : { //Select Approve from taskpanel
				if (this.isItemExists(Caption.MenuItems.Approve.Value))
					this.clickItem(Caption.MenuItems.Approve.Value);
				else 
					this.clickItem(Caption.MenuItems.MarkComplete.Value);

				Utils.fluentWait(driver);					
				break;					

			} //End Case:Approve

			case "MARK REJECT" : { //Select Approve from taskpanel
				if (this.isItemExists(Caption.MenuItems.Approve.Value))
					this.clickItem(Caption.MenuItems.Reject.Value);
				break;

			} //End Case:Approve

			case "REJECT" : {
				this.clickItem(Caption.MenuItems.Reject.Value);
				Utils.fluentWait(driver);					
				break;
			} //END Case:Reject

			} //End Swithc

			if (MFilesDialog.exists(this.driver)) { //Clicks Ok button in M-Files Dialog
				MFilesDialog mfilesDialog = new MFilesDialog (driver);
				mfilesDialog.clickOkButton();
				Utils.fluentWait(this.driver);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at TaskPanel.markApproveReject : "+ e.getMessage());
		} //End catch

	} //End of IsAppletEnabled

	/**
	 * getVaultList : get the all vault name which listed in taskpanel
	 * 
	 * @return noOfVaults : vault listed in after clicking 'vaults' the command in taskpane
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

			Log.event("TaskPanel.getVaultList : Get the all vaults are listed in task pane.",StopWatch.elapsedTime(startTime));

			return noOfVaults;
		}//End try
		catch(Exception e){
			throw new Exception("Exception at TaskPanel.getVaultList : "+ e.getMessage());
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
					//vaultName.get(itemIndx).click();
					ActionEventUtils.click(driver,vaultName.get(itemIndx));
					break;
				}//End if

			Log.event("MenuBar.selectVault : Select the specified vault from the menu bar.",StopWatch.elapsedTime(startTime));

		}//End try
		catch(Exception e){
			Log.exception(e);
		}//End catch
	}//End selectVault


	/**
	 * Click the OK button in Upload file dialog box
	 * @throws Exception
	 */
	public void clickOkOnUploadDialog() throws Exception
	{
		try{
			WebElement uploadOkBtn=driver.findElement(By.cssSelector("button[class*='window_ok'][style='display: inline-block;']"));
			ActionEventUtils.click(driver, uploadOkBtn);
			//uploadOkBtn.click();
			System.out.println("click the ok button");
		}
		catch(Exception e) {
			Log.exception(new Exception("Unable to click 'Ok' button on Upload Popup dialog."),driver);
		}

	}

	/**
	 * Description: Click 'Upload' button of Document Object
	 * File to be uploaded
	 * @param driver
	 * @throws Exception 
	 */
	public void selectFiletoUpload(String fileLocation) throws Exception{
		try{
			//			//WebElement fileupload = driver.findElement(By.cssSelector(""));
			//			WebElement fileupload = driver.findElement(By.cssSelector("div[class='upload']>button[id='btPickFile']"));
			//			ActionEventUtils.click(driver, fileupload);
			//			//fileupload.click();
			//			File file = new File(fileLocation);
			//			file.toURI();
			//			fileupload.sendKeys(file.getAbsolutePath());
			//			fileupload.sendKeys(fileLocation);
			//			Utils.fluentWait(driver);
			WebElement fileUpload1 =driver.findElement(By.cssSelector("button[id='btPickFile']"));
			ActionEventUtils.click(driver, fileUpload1);
			File file = new File(fileLocation);
			file.toURI();
			fileUpload1.sendKeys(file.getAbsolutePath());
			//fileUpload1.sendKeys(fileLocation);
			Utils.fluentWait(driver);
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to click 'Upload' button"),driver);
		}
	}






	/*--------------------------------Functions required for smoke test cases--------------------------------------*/

	/**
	 * 
	 * @param href
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public boolean downloadSingleFile(WebElement href, String fileName) throws Exception{

		try{

			FileDownloader downloadTestFile = new FileDownloader(driver);
			String downloadedFileAbsoluteLocation = downloadTestFile.downloadFile(href,"DownloadedFiles");

			if (new File(downloadedFileAbsoluteLocation).exists())
			{
				if (downloadTestFile.getHTTPStatusOfLastDownloadAttempt()==200)
					return true;
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}

	/**
	 * selectViewAndModifyItemFromTaskPane: Select object actions from 'view and modify' section of task pane
	 * @param item
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean selectViewAndModifyItemFromTaskPane(String item) throws Exception
	{
		try {

			WebElement viewAndModiyItem=driver.findElement(By.cssSelector("div[id='taskpane"+item+"']>div[class='taskpaneItemText']"));

			if(!item.contains("Replace"))
				viewAndModiyItem.getText().replace(" ","");
			else 
				viewAndModiyItem.getText();

			//Clicks an Item from 'View and Modify' section of taskpane
			ActionEventUtils.click(driver, viewAndModiyItem);
			//viewAndModiyItem.click();

			return true;
			//			}
		}catch(Exception e){
			Log.exception(new Exception("Unable to perform '"+item+"' from 'View and Modify' section."),driver);
		}
		return false;
	}

	/**
	 * selectObjectActionsFromTaskPane: "Select the 'Action' from TaskPane for the selected Object"
	 * @param action
	 * @throws Exception
	 */
	public void selectObjectActionsFromTaskPane(String action) throws Exception {
		try {
			WebElement taskPaneActions= driver.findElement(By.cssSelector("div[id='taskpane"+action+"']"));
			ActionEventUtils.click(driver, taskPaneActions);
			//	taskPaneActions.click();
			Utils.fluentWait(driver);
			Utils.isLogOutPromptDisplayed(driver);
		}
		catch(Exception e){
			Log.exception(new Exception("Unable to select the '"+action+"' actions from taskpane."), driver);
		}

	}

	/**
	 * isMoveToStateOptionsAvailable: Verify if 'MoveToState' options displayed in TaskPane
	 * @param option
	 * @return
	 * @throws Exception
	 */
	public Boolean isMoveToStateOptionsAvailable(String option) throws Exception {
		Boolean isDisplayed=false;
		try{
			WebElement moveToState= driver.findElement(By.cssSelector("div[id='taskpaneStates']:not([style='display: none;'])"));
			if(moveToState.isDisplayed()) {
				isDisplayed=true;
			}
		}
		catch(Exception e)
		{
			if(e.getClass().toString().contains("NoSuchElementException"))
				Log.message("'"+option+"' is Hidden in taskpane.");
			else
				Log.exception(new Exception("Unable to find/display the '"+option+"' Element in taskpane."));
		}
		return isDisplayed;
	}

	/**
	 * isObjectActionsDisplayedOnTaskPane: "Verify the display of selected Object 'Action' in TaskPane"
	 * @param action
	 * @throws Exception
	 */
	public Boolean isObjectActionsDisplayedOnTaskPane(String action) throws Exception
	{
		Boolean isDisplayed=false;
		WebElement taskPaneActions=null;
		try
		{
			if(!action.trim().equalsIgnoreCase("DownloadFile")){
				taskPaneActions= driver.findElement(By.cssSelector("div[id='taskpane"+action+"']:not([style*='display: none;'])"));
			}
			else {
				taskPaneActions= driver.findElement(By.cssSelector("a[id*='"+action+"']"));
			}
			if(taskPaneActions.isDisplayed()|taskPaneActions.getText().trim().contains(action.replace(" ",""))) {
				isDisplayed=true;
			}
		}
		catch(Exception e){
			if(e.getClass().toString().contains("NoSuchElementException"))
				Log.message("'"+action+"' is Hidden in taskpane.");
			else
				Log.exception(new Exception("Unable to find/display the '"+action+"' Element in taskpane."));
		}
		return isDisplayed;
	}

	/**
	 * expandItem : This method Expands the given menu
	 * @param itemName - name of the sub-category
	 * @return true if expanded / false if collapsed
	 */
	public boolean expandItem(String itemName) throws Exception  {

		try {

			List<WebElement> newCommandOnTaskPane=driver.findElements(By.cssSelector("div[id='taskpane']>div[id='taskpaneContent']>div[class='taskpaneArea']>div[class*='taskpaneHeader'],"+
					"div[id='taskpane']>div[id='taskpaneContent']>div>div[class='taskpaneArea']>div[class='taskpaneHeader'],div[id='taskpane']>div[id='taskpaneContent']>div[class*='taskpaneArea'][style*='display: block']>div[class*='taskpaneHeader']"));

			for(int count = 0; count < newCommandOnTaskPane.size(); count++){
				if(newCommandOnTaskPane.get(count).getText().trim().equalsIgnoreCase(itemName)){
					WebElement icon = newCommandOnTaskPane.get(count).findElement(By.cssSelector("div[class='taskpaneItemText']>span"));
					if(icon.getAttribute("class").toString().contains("taskpaneHeaderExpandIcon"))
						ActionEventUtils.click(driver, icon);
					//icon.click();

					if(newCommandOnTaskPane.get(count).findElement(By.cssSelector("div[class='taskpaneItemText']>span")).getAttribute("class").toString().contains("taskpaneHeaderCollapseIcon"))
						return true;
					else
						return false;
				}

			}

			throw new Exception("The Given item " + itemName + " does not exist in the task pane.");

		} //End try

		catch(Exception e) 	{
			throw new Exception("Exception at TaskPanel.expandItem : "+ e.getMessage());
		} //End catch

	} //expandItem








} //End Class TaskPanel

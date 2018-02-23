package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

public class MFilesDialog {

	//Variable Declaration
	public WebElement msgDialog;
	public final WebDriver driver;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");
	/**
	 * MFilesDialog : Constructor to instantiate MFiles dialog
	 * @param driver
	 * @return None
	 * @throws Exception
	 */
	public MFilesDialog(final WebDriver driver) throws Exception {

		try {

			this.driver = driver;
			//Utils.fluentWait(driver);
			int snooze = 0;
			for (snooze = 0; snooze < 5; snooze++)
			{
				try
				{
					new WebDriverWait(this.driver, 60).ignoring(NoSuchElementException.class)
					.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
							(this.driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])"))));

					break;
				}
				catch(Exception e0){}
			}

			if (snooze >= 5)
				throw new Exception ("M-Files dialog is not displayed After waited for 5 times.");

			//msgDialog = this.driver.findElement(By.cssSelector("div[role='dialog'][style*='display: block'],div[class*='ui-draggable'][style*='display: block']")); //Web element div of the error message
			msgDialog = this.driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])")); //Web element div of the error message
			//msgDialog = this.driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none'])")); //Web element div of the error message

			if (!msgDialog.isDisplayed())
				throw new Exception ("M-Files dialog is not displayed.");

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException"))
				throw new Exception ("M-Files dialog is not displayed. : "+e.getMessage(), e);
			else
				throw new Exception ("Exception in MFilesDialog.MFilesDialog : "+e.getMessage(), e);
		} //End catch

	} //End MFilesDialog

	/**
	 * MFilesDialog : Constructor to instantiate MFiles dialog
	 * @param driver
	 * @return None
	 * @throws Exception
	 */
	public MFilesDialog(final WebDriver driver, String caption) throws Exception {

		try {

			this.driver = driver;

			int snooze = 0;
			for (snooze = 0; snooze < 5; snooze++)
			{
				try
				{
					new WebDriverWait(this.driver, 60).ignoring(NoSuchElementException.class)
					.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
							(this.driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])"))));

					break;
				}
				catch(Exception e0){}
			}

			if (snooze >= 5)
				throw new Exception ("M-Files dialog is not displayed After waited for 5 times.");

			List<WebElement> dialogs = this.driver.findElements(By.cssSelector("div[role='dialog']:not([style*='display: none'])>div>span[class='ui-dialog-title']")); //Web element div of the error message 

			for(int count = 0; count < dialogs.size(); count++) {
				if(dialogs.get(count).getText().toUpperCase().contains(caption.toUpperCase())) {
					msgDialog = dialogs.get(count).findElement(By.xpath("..")).findElement(By.xpath(".."));
					break;
				}
			}

			if (!msgDialog.isDisplayed())
				throw new Exception ("MFilesDialog.MFilesDialog :- M-Files dialog is not displayed with title contains :'"+caption+"'");

		} //End try
		catch(NoSuchElementException e)
		{
			throw new Exception ("Exception at MFilesDialog.MFilesDialog : M-Files dialog is not displayed. : "+e.getMessage(), e);
		}
		catch (Exception e) {
			throw new Exception ("Exception in MFilesDialog.MFilesDialog : "+e.getMessage(), e);
		} //End catch

	} //End MFilesDialog

	/**
	 * getTitle : To get the title of the message dialog
	 * @param None
	 * @return Title displayed in the MFiles dialog
	 * @throws Exception
	 */
	public void setPermission(String perm) throws Exception {

		try {

			WebElement selectDocPermission=driver.findElement(By.cssSelector("select[class='prompt']"));
			new Select(selectDocPermission).selectByVisibleText(perm);
			/*Select docPermission=new Select(selectDocPermission);
			docPermission.selectByVisibleText(perm);*/
		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in MFilesDialog.setPermission : "+e.getMessage(), e);
		} //End catch

	} //End getTitle

	/**
	 * getTitle : To get the title of the message dialog
	 * @param None
	 * @return Title displayed in the MFiles dialog
	 * @throws Exception
	 */

	public String getTitle() throws Exception {

		try {

			WebElement titleSpan = msgDialog.findElement(By.cssSelector("div>span[class='ui-dialog-title'],div[class*='ui-header-titlebar']>span")); //Web element of the error message

			if(titleSpan.isDisplayed()){
				System.out.println(titleSpan.getAttribute("innerText"));
				return(titleSpan.getText().trim());
			}
			else
				return "";


			//	return(titleSpan.getAttribute("innerText").trim());
			//return(titleSpan.getText().trim()); //Gets the error message displayed
			/*WebElement titleSpan = msgDialog.findElement(By.cssSelector("div>span[class='ui-dialog-title']")); //Web element of the error message
			if(titleSpan.getText().isEmpty())
				titleSpan =driver.findElement(By.cssSelector("div[role='dialog'][style*='display: block'],div[class*='ui-draggable'][style*='display: block']")); //Web element div of the error message
			System.out.println("Title : " +titleSpan.getText());
			return(titleSpan.getText().trim()); //Gets the error message displayed
			 */			

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in MFilesDialog.getTitle : "+e.getMessage(), e);
		} //End catch

	} //End getTitle

	/**
	 * getMessage : To get the message of the message dialog
	 * @param None
	 * @return Message displayed in the MFiles dialog
	 * @throws Exception
	 */
	public String getMessage() throws Exception {

		try {

			WebElement msgRow =  null;

			try {
				msgRow = this.driver.findElement(By.cssSelector("div[class='message'],div[class*='ui-widget-content']>table[class='messages']>tbody>tr>td>div[class='message']," +
						"div[class*='ui-widget-content']>div[class='window_pages']>div[class='errorDialog']>div[class='shortErrorArea']," +
						"div[class*='ui-widget-content']>div[class*='promptWindow']>div[class*='promptText'],"+ "div[class='errorDialog']>div[class='shortErrorArea']")); //Web element of the error message
			}
			catch (Exception e1) {
				msgRow = this.driver.findElement(By.cssSelector("div[class*='ui-widget-content']>div[class*='promptWindow'] div[class*='promptText']"));
			}
			System.out.println(msgRow.getText());
			return(msgRow.getText()); //Gets the error message displayed

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in MFilesDialog.getMessage : "+e.getMessage(), e);
		} //End catch

	} //End getMessage

	/**
	 * isShowDetailLinkExists : To check the existence of Show Details link
	 * @param None
	 * @return returns true if link exists; if not returns false
	 * @throws Exception
	 */
	public Boolean isShowDetailLinkExists() throws Exception {

		try {

			WebElement msgRow = msgDialog.findElement(By.cssSelector("div[class*='ui-widget-content']>table[class='message']>tbody>tr>td>div[class='openLongErrorArea']," +
					"div[class*='ui-widget-content']>div[class='window_pages']>div[class='errorDialog']>div[class='openLongErrorArea']")); //Web element of the error message

			if (msgRow.isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception ("Exception in MFilesDialog.isShowDetailLinkExists : "+e.getMessage(), e);
		} //End catch

	} //End isShowDetailLinkExists

	/**
	 * ClickShowDetailLink : To Click the Show Details link
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickShowDetailLink() throws Exception {

		try {

			WebElement msgRow = msgDialog.findElement(By.cssSelector("div[class*='ui-widget-content']>table[class='message']>tbody>tr>td>div[class='openLongErrorArea']," +
					"div[class*='ui-widget-content']>div[class='window_pages']>div[class='errorDialog']>div[class='openLongErrorArea']")); //Web element of the error message

			ActionEventUtils.click(driver, msgRow);
			//msgRow.click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickShowDetailLink : "+e.getMessage(), e);
		} //End catch

	} //End ClickShowDetailLink

	/**
	 * GetDetailedMessage : To fetch the detailed message
	 * @param None
	 * @return message - text displayed in the 'Detailed Message' area
	 * @throws Exception
	 */
	public String getDetailedMessage() throws Exception {

		try {

			//Variable Declaration
			WebElement msgRow; //To store the web element of the error message

			msgRow = msgDialog.findElement(By.cssSelector("div[class*='ui-widget-content']>table[class='message']>tbody>tr>td>div[class='message']," +
					"div[class*='ui-widget-content']>div[class='window_pages']>div[class='errorDialog']>div[class='longErrorArea']")); //Web element of the error message

			return msgRow.getText(); //Gets the error message displayed

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getDetailedMessage : "+e.getMessage(), e);
		} //End catch

	} //End GetMessage

	/**
	 * Close : To Close the dialog 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void close() throws Exception {

		try {

			//Variable Declaration
			WebElement closeButton; //To store the web element of the error message

			closeButton = msgDialog.findElement(By.cssSelector("div>button[title='Close']")); //Web element of the error message
			ActionEventUtils.click(driver, closeButton);
			//closeButton.click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.close : " +e);
		} //End catch

	} //End GetMessage

	/**
	 * GetIcon : This method gets the icon displayed in the dialog 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public String getIcon() throws Exception {

		try {

			WebElement iconRow = msgDialog.findElement(By.cssSelector("img")); //Web element of the error message
			String iconName = iconRow.getAttribute("src");
			return iconName.split("/")[iconName.split("/").length-1].split("\\.")[0];

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getIcon : "+e.getMessage(), e);
		} //End catch

	} //End GetIcon

	/**
	 * ClickButton : Clicks the button of the specified class 
	 * @param btnClass - class of the button to be clicked
	 * @return true if the button is clicked / false if the button is not found
	 * @throws Exception
	 */
	public boolean clickButton(String btnClass) throws Exception  {

		try {

			WebElement btn;

			try { btn = msgDialog.findElement(By.cssSelector("div>table[class*='buttons']>tbody>tr>td>button[class*='" + btnClass + "']," +
					"div>div>button[class*='" + btnClass + "'],div>button[class*='" + btnClass + "'],div[class*='ForceUndoCheckout']>div[class='buttons']>button[class*='" + btnClass + "']")); } 
			catch (NoSuchElementException e) {
				btn = this.driver.findElement(By.cssSelector("div>table[class='buttons']>tbody>tr>td>button[class*='" + btnClass + "']," +
						"div>div>button[class*='" + btnClass + "'],div>button[class*='" + btnClass + "'],div[class*='ForceUndoCheckout']>div[class='buttons']>button[class*='" + btnClass + "']"));
			}

			if (browser.equalsIgnoreCase("ie"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", btn);
			else
				ActionEventUtils.click(driver, btn);

			return true;

			/*Thread.sleep(500);

			int snooze = 0; 

			while (MFilesDialog.exists(this.driver, title) && snooze < 3) {
				Thread.sleep(500);
				snooze ++;
			}

			if (MFilesDialog.exists(this.driver, title))
				return false;
			else
				return true;*/

		} //End try

		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickButton : "+e.getMessage(), e);
		} //End catch

	} //End clickButton

	/**
	 * getButtonElement : Gets the web element of the button
	 * @param btnClass - class of the button to be clicked
	 * @return Webelement of the button
	 * @throws Exception
	 */
	public WebElement getButtonElement(String btnClass) throws Exception  {

		try {

			return (msgDialog.findElement(By.cssSelector("div>table[class='buttons']>tbody>tr>td>button[class='" + btnClass + "']," +
					"div>div>button[class*='" + btnClass + "']")));

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return null;
			else
				throw new Exception("Exception at MFilesDialog.getButtonElement : "+e.getMessage(), e);
		} //End catch

	} //End ClickOkButton

	/**
	 * IsButtonExists : Check the existence of the button of the specified class 
	 * @param btnClass - class of the button to be checked for existence
	 * @return true if the button exists / false if the button is not found
	 * @throws Exception
	 */
	public boolean isButtonExists(String btnClass) throws Exception {

		try {

			WebElement button = msgDialog.findElement(By.cssSelector("div[class='" + btnClass + "']>button"));

			if (!button.isDisplayed())
				return false;

		} //End try

		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isButtonExists : "+e.getMessage(), e);
		} //End catch

		return true;

	} //End IsButtonExists

	/**
	 * ClickOkButton : Clicks the Ok button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickOkButton() throws Exception {

		try {
			if (!this.clickButton("ok"))
				throw new Exception ("Ok button is not clicked.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickOkButton : "+e.getMessage(), e);
		} //End catch

		finally{
			Log.event("M-FilesDialog.clickOkButton : OK button is clicked in the M-Files Dialog..");
		}

	} //End ClickOkButton

	/**
	 * getOkButtonPosition: This method gets the Ok Button X position, Y position, Width and Height
	 * @param None
	 * @return XPos, YPos, Width and Height as Hashmap
	 * @throws Exception
	 */
	public HashMap<String, Integer> getOkButtonPosition() throws Exception {

		try {

			WebElement okBtnElement = this.getButtonElement("ok");

			if (okBtnElement.equals(null)) 
				okBtnElement = this.getButtonElement("window_ok");

			if (okBtnElement.equals(null)) 
				okBtnElement = this.getButtonElement("ok ui-primary-blue");

			HashMap<String, Integer> position = new HashMap<>();
			position.put("XPos", okBtnElement.getLocation().x);
			position.put("YPos", okBtnElement.getLocation().y);
			position.put("Width", okBtnElement.getSize().width);
			position.put("Height", okBtnElement.getSize().height);
			return position;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getOkButtonPosition : "+e.getMessage(), e);
		} //End catch

	} //End getOkButtonPosition

	/**
	 * ClickCancelButton : Clicks the Cancel button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickCancelButton() throws Exception {

		try {

			if (!this.clickButton("cancel"))
				throw new Exception ("Cancel button is not clicked.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickCancelButton : "+e.getMessage(), e);
		} //End catch

		finally{
			Log.event("M-FilesDialog.clickCancelButton : Cancel button is clicked in the M-Files Dialog..");
		}

	} //End ClickCancelButton

	/**
	 * getCancelButtonPosition: This method gets the Cancel Button X position, Y position, Width and Height
	 * @param None
	 * @return XPos, YPos, Width and Height as Hashmap
	 * @throws Exception
	 */
	public HashMap<String, Integer> getCancelButtonPosition() throws Exception {

		try {

			WebElement okBtnElement = this.getButtonElement("cancel");

			if (okBtnElement.equals(null)) 
				okBtnElement = this.getButtonElement("window_cancel");

			if (okBtnElement.equals(null)) 
				okBtnElement = this.getButtonElement("cancel ui-default-gray");

			HashMap<String, Integer> position = new HashMap<>();
			position.put("XPos", okBtnElement.getLocation().x);
			position.put("YPos", okBtnElement.getLocation().y);
			position.put("Width", okBtnElement.getSize().width);
			position.put("Height", okBtnElement.getSize().height);
			return position;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getCancelButtonPosition : "+e.getMessage(), e);
		} //End catch

	} //End getCancelButtonPosition

	/**
	 * ClickCloseButton : Clicks the close button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickCloseButton() throws Exception {

		try {

			this.clickButton("ui-dialog-titlebar-close");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickCloseButton : "+e.getMessage(), e);
		} //End catch

	} //End ClickCloseButton

	/**
	 * ClickEscapeKey : Simulates Esc key press 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public Boolean clickEscapeKey() throws Exception {

		try {

			this.msgDialog.sendKeys(Keys.ESCAPE); //Clicks Esc key

			int snooze = 0; 

			while (MFilesDialog.exists(this.driver) && snooze < 50) {
				snooze ++;
				Thread.sleep(500);
			}

			if (!MFilesDialog.exists(this.driver))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickEscapeKey : "+e.getMessage(), e);
		} //End catch

	} //End ClickEscapeKey

	/**
	 * ClickCheckOutButton : Clicks the Check Out button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickCheckOutButton() throws Exception {

		try {

			this.clickButton("checkout");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickCheckOutButton : "+e.getMessage(), e);
		} //End catch

	} //End ClickCheckOutButton

	/**
	 * ClickShowButton : Clicks the Show button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickShowButton() throws Exception {

		try {

			this.clickButton("show");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickShowButton : "+e.getMessage(), e);
		} //End catch

	} //End ClickShowButton

	/**
	 * ClickCopyToClipboard : Clicks the Copy To Clipboard button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickCopyToClipboard() throws Exception {

		try {

			this.clickButton("copyToClipboardContainer");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickCopyToClipboard : "+e.getMessage(), e);
		} //End catch

	} //End ClickCopyToClipboard

	/**
	 * clickSharePublicLinkBtn : Clicks the Share public link button 
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickSharePublicLinkBtn() throws Exception {

		try {
			if (!this.clickButton("shareCommand"))
				throw new Exception ("Share public link button is not clicked.");

			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickSharePublicLinkBtn : "+e.getMessage(), e);
		} //End catch

	} //End ClickOkButton


	/**
	 * ClickButton : Click the create public link in shared by me dialog
	 * @return true if the button is clicked / false if the button is not found
	 * @throws Exception
	 */
	public boolean clickCreatePublicLink() throws Exception  {

		try {

			WebElement btn;

			btn = msgDialog.findElement(By.cssSelector("button[class*= 'shareCommand'")); 
			ActionEventUtils.click(driver, btn);
			return true;

			/*Thread.sleep(500);

			int snooze = 0; 

			while (MFilesDialog.exists(this.driver, title) && snooze < 3) {
				Thread.sleep(500);
				snooze ++;
			}

			if (MFilesDialog.exists(this.driver, title))
				return false;
			else
				return true;*/

		} //End try

		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickButton : "+e.getMessage(), e);
		} //End catch

	} //End clickButton

	/**
	 * IsCopyToClipboardBtnDisplayed : Checks the Copy To Clipboard button is displayed 
	 * @param None
	 * @return true if the button is displayed / false if the button is not displayed
	 * @throws Exception
	 */
	public Boolean isCopyToClipboardBtnDisplayed() throws Exception {

		try {

			return this.isButtonExists("copyToClipboardContainer");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isCopyToClipboardBtnDisplayed : "+e.getMessage(), e);
		} //End catch

	} //End ClickCopyToClipboard

	/**
	 * IsWorkflowDialogDisplayed : Checks if workflow dialog is displayed 
	 * @param None
	 * @return true if the  workflow dialog is displayed / false if the  workflow dialog is not displayed
	 * @throws Exception
	 */
	public Boolean isWorkflowDialogDisplayed() throws Exception {

		try {

			if (this.getTitle().toUpperCase().contains("WORKFLOW"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isWorkflowDialogDisplayed : "+e.getMessage(), e);
		} //End catch

	} //End IsWorkflowDialogDisplayed

	/**
	 * isCommentsDialogDisplayed : Checks if comments dialog is displayed 
	 * @param None
	 * @return true if the  workflow dialog is displayed / false if the  workflow dialog is not displayed
	 * @throws Exception
	 */
	public Boolean isCommentsDialogDisplayed() throws Exception {

		try {

			WebElement commentDlg = this.msgDialog.findElement(By.cssSelector("div[class='commentsDialog']"));

			if (this.getTitle().toUpperCase().contains("COMMENTS") || commentDlg.isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isCommentsDialogDisplayed : "+e.getMessage(), e);
		} //End catch

	} //End isCommentsDialogDisplayed

	/**
	 * SetWorkflow : Sets the specified workflow 
	 * @param workflowName - name of the workflow to be set
	 * @return none
	 * @throws Exception
	 */
	public void setWorkflow(String workflowName) throws Exception {

		try {

			if (!this.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			WebElement workflowSelect = msgDialog.findElement(By.cssSelector("div[class='workflowDialog']>div[id='workflowArea']>select[id='workflow']"));

			/*if(workflowName.equals("")) {
				Select select = new Select(workflowSelect);
				select.selectByVisibleText(workflowName);
				return;
			}*/

			if (browser.equalsIgnoreCase("Edge"))
				workflowSelect.sendKeys(workflowName);
			else
				new Select(workflowSelect).selectByVisibleText(workflowName);

			/*Select select = new Select(workflowSelect);
			select.selectByVisibleText(workflowName);

			List<WebElement> workflowOptions = msgDialog.findElements(By.cssSelector("div[class='workflowDialog']>div[id='workflowArea']>select[id='workflow']>option"));
			int workflowCt = workflowOptions.size();
			int loopIdx = 0;

			for (loopIdx=0; loopIdx<workflowCt; loopIdx++)
				if (workflowOptions.get(loopIdx).getText().toUpperCase().trim().equals(workflowName.toUpperCase().trim())) {
					workflowSelect.sendKeys(workflowName);
					workflowSelect.sendKeys(Keys.TAB);
					//workflowOptions.get(loopIdx).click();
					break;
				}

			if (loopIdx >= workflowCt)
				throw new Exception("Workflow (" + workflowName + ") does not exists in the list.");	*/		

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setWorkflow : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflow

	/**
	 * getWorkflowStates : Gets the available workflow state 
	 * @param None
	 * @return List of all available workflow state
	 * @throws Exception
	 */
	public List<String> getWorkflowStates() throws Exception {

		final List <String> elementStates = new ArrayList <String>();

		try {

			List <WebElement> options =driver.findElements(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']>option"));

			for(WebElement state: options) 
				if(!state.getText().trim().isEmpty() && !state.getAttribute("class").equalsIgnoreCase("disableOption"))
					elementStates.add(state.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getWorkflowStates : "+e.getMessage(), e);
		} //End catch

		return elementStates;

	} //End getWorkflowStates

	/**
	 * 
	 * @param tabName
	 * @return
	 * @throws Exception 
	 */
	public boolean isStateEnabled(String state) throws Exception {
		try{

			List <WebElement> options =driver.findElements(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']>option"));

			for(int count = 0; count < options.size(); count++) {
				if(options.get(count).getText().trim().equals(state)) {
					if(options.get(count).getAttribute("class").equals("disableOption"))
						return false;
					else
						return true;
				}
			}

			throw new Exception("The Given State was not available for the selected Workflow");

		}
		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.isStateEnabled : "+e.getMessage(), e);
		}
	}

	/**
	 * getWorkflowState : Gets the current workflow state 
	 * @param None
	 * @return current workflow state
	 * @throws Exception
	 */
	public String getWorkflow() throws Exception {

		try {

			WebElement state =driver.findElement(By.cssSelector("select[id='workflow']>option[selected='']"));
			return state.getText().trim();

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return "";
			else
				throw new Exception("Exception at MFilesDialog.getWorkflow : "+e.getMessage(), e);
		} //End catch

	} //End getWorkflowStates

	/**
	 * getWorkflows : Gets the available workflows 
	 * @param None
	 * @return List of all available workflows
	 * @throws Exception
	 */
	public List<String> getWorkflows() throws Exception {

		final List <String> elementStates = new ArrayList <String>();

		try {

			List <WebElement> options =driver.findElements(By.cssSelector("div[class='workflowDialog']>div[id='workflowArea']>select[id='workflow']>option"));

			for(WebElement state: options) 
				if(!state.getText().trim().isEmpty())
					elementStates.add(state.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getWorkflows : "+e.getMessage(), e);
		} //End catch

		return elementStates;

	} //End getWorkflowStates

	/**
	 * getWorkflowState : Gets the current workflow state 
	 * @param None
	 * @return current workflow state
	 * @throws Exception
	 */
	public String getWorkflowState() throws Exception {

		try {

			WebElement state =driver.findElement(By.cssSelector("select[id='state']>option[selected='']"));
			return state.getText().trim();

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return "";
			else
				throw new Exception("Exception at MFilesDialog.getWorkflowState : "+e.getMessage(), e);
		} //End catch

	} //End getWorkflowStates

	/**
	 * SetWorkflowState : Sets the specified workflow state
	 * @param stateName - name of the workflow state to be set
	 * @return none
	 * @throws Exception
	 */
	public void setWorkflowState(String stateName) throws Exception {

		try {

			/*if (!this.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			WebElement stateSelect = msgDialog.findElement(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']"));
			Select select = new Select(stateSelect);
			select.selectByVisibleText(stateName);

			List<WebElement> stateOptions = msgDialog.findElements(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']>option"));
			int workflowCt = stateOptions.size();
			int loopIdx = 0;

			for (loopIdx=0; loopIdx<workflowCt; loopIdx++) {
				System.out.println(stateOptions.get(loopIdx).getText());
				if (stateOptions.get(loopIdx).getText().toUpperCase().trim().equals(stateName.toUpperCase().trim())) {
					JavascriptExecutor executor = (JavascriptExecutor)driver;
					executor.executeScript("arguments[0].click();", stateOptions.get(loopIdx));
					//stateOptions.get(loopIdx).click();
					break;
				}
			}

			if (loopIdx >= workflowCt)
				throw new Exception("Workflow state (" + stateName + ") does not exists in the list.");	*/		


			if (!this.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			WebElement workflowSelect = msgDialog.findElement(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']"));
			if (browser.equalsIgnoreCase("Edge"))
				workflowSelect.sendKeys(stateName);
			else
				new Select(workflowSelect).selectByVisibleText(stateName);

			/*List<WebElement> workflowstate = new Select(workflowSelect).getOptions();
			workflowstate.get(1).click();
			 */
			/*Select select = new Select(workflowSelect);
			select.selectByVisibleText(workflowName);

			List<WebElement> workflowOptions = msgDialog.findElements(By.cssSelector("div[class='workflowDialog']>div[id='stateArea']>select[id='state']>option"));
			int workflowCt = workflowOptions.size();
			int loopIdx = 0;

			for (loopIdx=0; loopIdx<workflowCt; loopIdx++)
				if (workflowOptions.get(loopIdx).getText().toUpperCase().trim().equals(stateName.toUpperCase().trim())) {
					workflowSelect.sendKeys(stateName);
					workflowSelect.sendKeys(Keys.TAB);
					//workflowOptions.get(loopIdx).click();
					break;
				}

			if (loopIdx >= workflowCt)
				throw new Exception("Workflow state (" + stateName + ") does not exists in the list.");	*/

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setWorkflowState : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflowState

	/**
	 * SetWorkflowComments : Sets the specified comments in the workflow dialog
	 * @param comments - Comments to be set
	 * @return none
	 * @throws Exception
	 */
	public void setWorkflowComments(String comments) throws Exception {

		try {

			if (comments.equals(null))
				return;

			if (!this.isWorkflowDialogDisplayed())
				throw new Exception("Workflow dialog is not displayed.");

			WebElement workflowComment = msgDialog.findElement(By.cssSelector("div[class='workflowDialog']>div[id='commentArea']>textarea[id='comment']"));
			workflowComment.sendKeys(comments);			

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setWorkflowComments : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflowComments

	/**
	 * SetWorkflowComments : Sets the specified comments in the workflow dialog
	 * @param comments - Comments to be set
	 * @return none
	 * @throws Exception
	 */
	public String getCommentHistory(int version) throws Exception {

		try {

			if (!this.isCommentsDialogDisplayed())
				throw new Exception("Comments dialog is not displayed.");


			List<WebElement> workflowComment = msgDialog.findElements(By.cssSelector("div[id='commentHistory']>div"));
			if(workflowComment.size() == 0)
				return "";
			return workflowComment.get(version).findElement(By.cssSelector("div[class='commentText']")).getText();			

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getCommentHistory : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflowComments

	/**
	 * SetWorkflowComments : Sets the specified comments in the workflow dialog
	 * @param comments - Comments to be set
	 * @return none
	 * @throws Exception
	 */
	public void setComment(String comment) throws Exception {

		try {

			if (!this.isCommentsDialogDisplayed())
				throw new Exception("Comments dialog is not displayed.");

			WebElement commentArea = msgDialog.findElement(By.cssSelector("div[id='commentArea']>textarea"));
			commentArea.sendKeys(comment);			

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setComment : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflowComments

	/**
	 * isCopyToClipBoardLinkExists: This method is to check if Copy to Clipboard link exists
	 * @param None
	 * @return Boolean value <br/> <b>true</b> if Copy to Clipboard link exists <br/> <b>false</b> if Copy to Clipboard link does not exists
	 * @throws Exception
	 */
	public Boolean isCopyToClipBoardLinkExists() throws Exception {

		try {

			WebElement copyLink = this.msgDialog.findElement(By.className("clipBoard"));

			if (copyLink.isDisplayed())
				return true;
			else
				return false;


		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isCopyToClipBoardLinkExists : "+e.getMessage(), e);
		} //End catch

	} //End IsCopyToClipBoardLinkExists

	/**
	 * getCopyToClipboardPosition: This method gets the copy to clipboard X position, Y position, Width and Height
	 * @param None
	 * @return XPos, YPos, Width and Height as Hashmap
	 * @throws Exception
	 */
	public HashMap<String, Integer> getCopyToClipboardPosition() throws Exception {

		try {

			WebElement copyLink = this.msgDialog.findElement(By.className("clipBoard"));
			HashMap<String, Integer> position = new HashMap<>();
			position.put("XPos", copyLink.getLocation().x);
			position.put("YPos", copyLink.getLocation().y);
			position.put("Width", copyLink.getSize().width);
			position.put("Height", copyLink.getSize().height);
			return position;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getCopyToClipboardPosition : "+e.getMessage(), e);
		} //End catch

	} //End IsCopyToClipBoardLinkExists

	/**
	 * clickCopyToClipBoardLink : This method clicks Copy to Clipboard link
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void clickCopyToClipBoardLink() throws Exception {

		try {

			ActionEventUtils.click(driver, this.msgDialog.findElement(By.className("clipBoard")));
			//this.msgDialog.findElement(By.className("clipBoard")).click(); //Clicks Copy to Clipboard link

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickCopyToClipBoardLink : "+e.getMessage(), e);
		} //End catch

	} //End ClickCopyToClipBoardLink

	/**
	 * getHyperlink: This method gets the hyperlink text stored in the Get Hyperlink dialog
	 * @param None
	 * @return Hyperlink text
	 * @throws Exception
	 */
	public String getHyperlink() throws Exception {

		try {

			String hyperlinkText = this.msgDialog.findElement(By.cssSelector("input[class='prompt']")).getAttribute("value"); //Gets the text in the hyperlink text box
			return hyperlinkText;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getHyperlink : "+e.getMessage(), e);
		} //End catch

	} //End getHyperlink

	/**
	 * isGetHyperlinkDialogOpened: This method is to check get hyperlink dialog has opened
	 * @param None
	 * @return Hyperlink text
	 * @throws Exception
	 */
	public Boolean isGetMFilesWebURLDialogOpened() throws Exception {

		try {

			if (this.getTitle().toUpperCase().contains(Caption.MenuItems.GetMFilesWebURL.Value.toUpperCase())) //Checks if MFiles dialog has title Get Hyperlink as title
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isGetMFilesWebURLDialogOpened : "+e.getMessage(), e);
		} //End catch

	} //End getHyperlink

	/**
	 * isGetComboURLDialogOpened: This method is to check get hyperlink dialog has opened
	 * @param None
	 * @return Combo URL text
	 * @throws Exception
	 */
	public Boolean isGetComboURLDialogOpened() throws Exception {

		try {

			if (this.getTitle().toUpperCase().contains(Caption.MenuItems.GetHyperlink.Value.toUpperCase())) //Checks if MFiles dialog has title Get Hyperlink as title
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isGetComboURLDialogOpened : "+e.getMessage(), e);
		} //End catch

	} //End getHyperlink

	/**
	 * clickLinksOnComboURLDialog: This method is to click links in Get Combo URL dialog
	 * @param None
	 * @return Combo URL text
	 * @throws Exception
	 */
	public void clickLinksOnComboURLDialog(String itemToClick) throws Exception {

		try {

			List<WebElement> items = this.driver.findElements(By.cssSelector("div[id='ComboUrl']>a")); // Get the list of links in GetCoboURL dialog
			int itemsCount = items.size();
			for (int loopIdx=0; loopIdx<itemsCount; loopIdx++)
				if (items.get(loopIdx).getText().equalsIgnoreCase(itemToClick)) { // Check the expected link is displayed
					//	items.get(loopIdx).click(); // Itemtoclick is available and click on it.
					ActionEventUtils.click(driver, items.get(loopIdx));
					Utils.fluentWait(driver);
					break;
				}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickLinksOnComboURLDialog : "+e.getMessage(), e);
		} //End catch

	} //End getHyperlink

	/**
	 * setHyperLinkAction: This method is to set the Hyperlink action in the dialog
	 * @param action - Action to be performed (Actions can be <b>'Download file; Show Object; Show View'</b>)
	 * @return true if action got selected; false if not
	 * @throws Exception
	 */
	public Boolean setHyperLinkAction(String action) throws Exception {

		try {

			ConcurrentHashMap <String, String> hyperlinkAction = new ConcurrentHashMap <String, String>();
			hyperlinkAction.put("DOWNLOAD FILE", "selectedFile");
			hyperlinkAction.put("SHOW OBJECT", "selectedObject");
			hyperlinkAction.put("SHOW VIEW", "currentLink");

			WebElement actionRbtn = this.msgDialog.findElement(By.id(hyperlinkAction.get(action.toUpperCase())));

			if (!actionRbtn.isEnabled())
				throw new Exception("Action (" + action + ") is not enabled.");

			ActionEventUtils.click(driver, actionRbtn);
			//	actionRbtn.click();

			if (actionRbtn.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setHyperLinkAction : "+e.getMessage(), e);
		} //End catch

	} //End setHyperLinkAction

	/**
	 * isHyperLinkActionSelected: This method is to check if hyperlink action is selected
	 * @param action - Action to be performed (Actions can be <b>'Download file; Show Object; Show View'</b>)
	 * @return true if action is selected; false if not
	 * @throws Exception
	 */
	public Boolean isHyperLinkActionSelected(String action) throws Exception {

		try {

			ConcurrentHashMap <String, String> hyperlinkAction = new ConcurrentHashMap <String, String>();
			hyperlinkAction.put("DOWNLOAD FILE", "selectedFile");
			hyperlinkAction.put("SHOW OBJECT", "selectedObject");
			hyperlinkAction.put("SHOW VIEW", "currentLink");

			WebElement actionRbtn = this.msgDialog.findElement(By.id(hyperlinkAction.get(action.toUpperCase())));

			if (!actionRbtn.isEnabled())
				throw new Exception("Action (" + action + ") is not enabled.");

			if (actionRbtn.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isHyperLinkActionSelected : "+e.getMessage(), e);
		} //End catch

	} //End isHyperLinkActionSelected

	/**
	 * isHyperLinkActionEnabled: This method is to check if hyperlink action is enabled
	 * @param action - Action to be performed (Actions can be <b>'Download file; Show Object; Show View'</b>)
	 * @return true if action is selected; false if not
	 * @throws Exception
	 */
	public Boolean isHyperLinkActionEnabled(String action) throws Exception {

		try {

			ConcurrentHashMap <String, String> hyperlinkAction = new ConcurrentHashMap <String, String>();
			hyperlinkAction.put("DOWNLOAD FILE", "selectedFile");
			hyperlinkAction.put("SHOW OBJECT", "selectedObject");
			hyperlinkAction.put("SHOW VIEW", "currentLink");

			WebElement actionRbtn = this.msgDialog.findElement(By.id(hyperlinkAction.get(action.toUpperCase())));

			if (actionRbtn.isEnabled())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isHyperLinkActionEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isHyperLinkActionSelected

	/**
	 * setHyperLinkLayoutOptions: This method is to set hyperlink custom layout options
	 * @param customOption - Option to be selected (Options can be <b>'Default; Simple Listing'</b>)
	 * @return true if action got selected; false if not
	 * @throws Exception
	 */
	public Boolean setHyperLinkLayoutOption(String customOption) throws Exception {

		try {

			List<WebElement> layout = this.msgDialog.findElements(By.cssSelector("td[class='optionItemHolder']>div"));
			int layoutCt = layout.size();
			WebElement layoutRbtn = null;

			for (int loopIdx=0; loopIdx<layoutCt; loopIdx++)
				if (layout.get(loopIdx).getText().equalsIgnoreCase(customOption)) {
					layoutRbtn = layout.get(loopIdx).findElement(By.tagName("input"));
					break;
				}

			if (layoutRbtn.equals(null))
				throw new Exception("Layout Radio button (" + customOption + ") does not exists.");

			if (!layoutRbtn.isEnabled())
				throw new Exception("Custom layout option (" + customOption + ") is not enabled.");

			ActionEventUtils.click(driver, layoutRbtn);
			//layoutRbtn.click();

			if (layoutRbtn.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setHyperLinkLayoutOption : "+e.getMessage(), e);
		} //End catch

	} //End setHyperLinkLayoutOptions

	/**
	 * isHyperLinkLayoutOptionSelected: This method is to check if hyperlink layout option is selected
	 * @param customOption - Option to be selected (Options can be <b>'Default; Simple Listing'</b>)
	 * @return true if action got selected; false if not
	 * @throws Exception
	 */
	public Boolean isHyperLinkLayoutOptionSelected(String customOption) throws Exception {

		try {

			List<WebElement> layout = this.msgDialog.findElements(By.cssSelector("td[class='optionItemHolder']>div"));
			int layoutCt = layout.size();
			int loopIdx = 0;

			for (loopIdx=0; loopIdx<layoutCt; loopIdx++)
				if (layout.get(loopIdx).getText().equalsIgnoreCase(customOption)) {
					if (layout.get(loopIdx).findElement(By.tagName("input")).isSelected())
						return true;
					else
						return false;
				}

			if (loopIdx >= layoutCt)
				throw new Exception("Layout option (" + customOption + ") does not exists.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isHyperLinkLayoutOptionSelected : "+e.getMessage(), e);
		} //End catch

		return false;

	} //End setHyperLinkLayoutOptions

	/**
	 * setHyperLinkLayoutItem: This method is to set hyperlink custom layout items
	 * @param customItem - Item to be selected (Items can be <b>'Search area; Task area; Properties pane; Top Menu; Breadcrumb; Java applet; Metadata card'</b>)
	 * @return true if item got selected; false if not
	 * @throws Exception
	 */
	public Boolean setHyperLinkLayoutItem(String customItem) throws Exception {

		try {

			List<WebElement> layout = this.msgDialog.findElements(By.cssSelector("div[id='defaultlayoutitems']>div"));
			int layoutCt = layout.size();
			int loopIdx=0;
			WebElement layoutchkBox = null;

			for (loopIdx=0; loopIdx<layoutCt; loopIdx++) //Gets the web element of layout
				if (layout.get(loopIdx).getText().equalsIgnoreCase(customItem))
					layoutchkBox = layout.get(loopIdx).findElement(By.cssSelector("input"));

			if (layoutchkBox.equals(null)) //Checks for existence of layout
				throw new Exception("Custom layout option (" + customItem + ") does not exists.");

			if (!layoutchkBox.isEnabled()) //Checks if layout item is enabled
				throw new Exception("Custom layout option (" + customItem + ") is not enabled.");

			if (!layoutchkBox.isSelected()) //Selects if not selected
				ActionEventUtils.click(driver, layoutchkBox);
			//	layoutchkBox.click();

			if (layoutchkBox.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setHyperLinkLayoutItem : "+e.getMessage(), e);
		} //End catch

	} //End setHyperLinkLayoutItem

	/**
	 * setHyperLinkLayoutItem: This method is to set hyperlink custom layout items
	 * @param customItem - Item to be selected (Items can be <b>'Search area; Task area; Properties pane; Top Menu; Breadcrumb; Java applet; Metadata card'</b>)
	 * @param toUnselectOthers - true to unselect others; false not to unselect others
	 * @return true if item got selected; false if not
	 * @throws Exception
	 */
	public Boolean setHyperLinkLayoutItem(String customItem, Boolean toUnselectOthers) throws Exception {

		try {

			List<WebElement> layout = this.msgDialog.findElements(By.cssSelector("div[id='defaultlayoutitems']>div"));
			WebElement layoutchkBox = null;

			for (int i=0; i<layout.size(); i++) {

				if (layout.get(i).getText().equalsIgnoreCase(customItem))
					layoutchkBox = layout.get(i).findElement(By.cssSelector("input"));

				if (layout.get(i).findElement(By.cssSelector("input")).isSelected())
					ActionEventUtils.click(driver, layout.get(i).findElement(By.cssSelector("input")));
				//layout.get(i).findElement(By.cssSelector("input")).click();
			}

			if (layoutchkBox.equals(null))
				throw new Exception("Custom layout option (" + customItem + ") does not exists.");

			if (!layoutchkBox.isEnabled())
				throw new Exception("Custom layout option (" + customItem + ") is not enabled.");

			if (!layoutchkBox.isSelected())
				ActionEventUtils.click(driver, layoutchkBox);
			//	layoutchkBox.click();

			if (layoutchkBox.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setHyperLinkLayoutItem : "+e.getMessage(), e);
		} //End catch

	} //End setHyperLinkLayoutItem

	/**
	 * isHyperLinkLayoutItemSelected: This method is to check if hyperlink custom layout items are selected
	 * @param customItem - Item to be selected (Items can be <b>'Search area; Task area; Properties pane; Top Menu; Breadcrumb; Java applet; Metadata card'</b>)
	 * @return true if item got selected; false if not
	 * @throws Exception
	 */
	public Boolean isHyperLinkLayoutItemSelected(String customItem) throws Exception {

		try {

			List<WebElement> layout = this.msgDialog.findElements(By.cssSelector("div[id='defaultlayoutitems']>div"));
			int layoutCt = layout.size();
			int loopIdx=0;

			for (loopIdx=0; loopIdx<layoutCt; loopIdx++) //Checks if hyperlink action is selected
				if (layout.get(loopIdx).getText().equalsIgnoreCase(customItem)) {
					if (layout.get(loopIdx).findElement(By.cssSelector("input")).isSelected())
						return true;
					else
						return false;
				}

			if (loopIdx >= layoutCt) //Checks layout item exists
				throw new Exception("Layout (" + customItem + ") does not exists.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isHyperLinkLayoutItemSelected : "+e.getMessage(), e);
		} //End catch

		return false;

	} //End isHyperLinkLayoutItemSelected

	/**
	 * setHyperLinkEmbedAuthentication: This method is to set embed authentication details
	 * @param isEmbed - true to embed authentication details; false to not embed authentication details
	 * @return true if item got selected; false if not
	 * @throws Exception
	 */
	public Boolean setHyperLinkEmbedAuthentication(Boolean isEmbed) throws Exception {

		try {

			WebElement embedAuthDetails = this.msgDialog.findElement(By.id("auth"));

			if (!embedAuthDetails.isEnabled())
				throw new Exception("Embed authentication deatils is not enabled.");

			if (!embedAuthDetails.isSelected() && isEmbed)
				ActionEventUtils.click(driver, embedAuthDetails);
			//embedAuthDetails.click();
			else if (embedAuthDetails.isSelected() && !isEmbed)
				ActionEventUtils.click(driver, embedAuthDetails);
			//embedAuthDetails.click();

			if (embedAuthDetails.isSelected() && isEmbed)
				return true;
			else if (!embedAuthDetails.isSelected() && !isEmbed)
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setHyperLinkEmbedAuthentication : "+e.getMessage(), e);
		} //End catch

	} //End setHyperLinkEmbedAuthentication

	/**
	 * isHyperlinkAuthenticationEmbed: This method is to check the status of embed authentication details
	 * @param none
	 * @return true if item got selected; false if not
	 * @throws Exception
	 */
	public Boolean isHyperlinkAuthenticationEmbed() throws Exception {

		try {

			WebElement embedAuthDetails = this.msgDialog.findElement(By.id("auth"));

			if (!embedAuthDetails.isDisplayed())
				throw new Exception("Embed authentication details is not displayed.");

			if (embedAuthDetails.isSelected())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isHyperlinkAuthenticationEmbed : "+e.getMessage(), e);
		} //End catch

	} //End isHyperlinkAuthenticationEmbed

	/**
	 * confirmDelete: This methods clicks Yes in the confirm delete dialog
	 * @param none
	 * @return true if deletion is successful else false
	 * @throws Exception
	 */
	public Boolean confirmDelete() throws Exception {

		try {

			if (!MFilesDialog.exists(this.driver, "Confirm Delete"))
				return false;

			int snooze = 0;

			while (MFilesDialog.exists(this.driver, "Confirm Delete") && snooze < 5)
			{
				if (this.getTitle().contains("Confirm Delete")) 
					this.clickOkButton();

				snooze++;
			}

			Utils.fluentWait(this.driver);

			if (MFilesDialog.exists(driver, "Confirm Delete"))				
				throw new Exception(this.getMessage());
			else
				return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.confirmDelete : "+e.getMessage(), e);
		} //End catch

	} //End Exists

	/**
	 * confirmDelete: This methods clicks Yes in the confirm delete dialog
	 * @param none
	 * @return true if deletion is successful else false
	 * @throws Exception
	 */
	public Boolean confirmDeleteHistory() throws Exception {

		try {

			if (!MFilesDialog.exists(this.driver))
				return false;

			while (MFilesDialog.exists(this.driver)) 
				if (this.getTitle().contains("M-Files")) 
					this.clickOkButton();

			Utils.fluentWait(this.driver);

			if (MFilesDialog.exists(driver))				
				throw new Exception(this.getMessage());
			else
				return true;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.confirmDeleteHistory : "+e.getMessage(), e);
		} //End catch

	} //End Exists

	/**
	 * isRenameDialogOpened: This method is to check rename dialog has opened
	 * @param None
	 * @return true if rename dialog is opened; false if not
	 * @throws Exception
	 */
	public boolean isRenameDialogOpened() throws Exception {

		try {

			if (!MFilesDialog.exists(this.driver))
				return false;

			if (this.getTitle().toUpperCase().equalsIgnoreCase(Caption.MenuItems.Rename.Value.toUpperCase()))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isRenameDialogOpened : "+e.getMessage(), e);
		} //End catch

	} //End isRenameDialogOpened

	/**
	 * rename: This methods is to perform the rename operation
	 * @param newName - New name to be set to the object
	 * @param toRename - true to click the Ok button / false to click the Cancel Button
	 * @return none
	 * @throws Exception
	 */
	public void rename(String newName, Boolean toRename) throws Exception {

		try {

			if (!this.isRenameDialogOpened())
				throw new Exception ("Rename dialog does not exists.");

			WebElement txtBox = this.msgDialog.findElement(By.cssSelector("div>input[class='prompt']")); //Web element of the value box
			txtBox.click();
			txtBox.clear();
			try{
				txtBox.sendKeys(newName); //Enters the new name in the value box
			}
			catch(Exception e0){((JavascriptExecutor) driver).executeScript("arguments[0].value=arguments[1]", txtBox, newName);}

			if (toRename) //Clicks Ok or Cancel based on the user input
				this.clickOkButton();
			else
				this.clickCancelButton();

			Utils.fluentWait(this.driver);

			int snooze = 0;

			while (MFilesDialog.exists(this.driver) && snooze <5) {
				Thread.sleep(500);
				snooze++;
			}

			if (MFilesDialog.exists(this.driver)) 
				throw new Exception (this.getMessage());

			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.rename : "+e.getMessage(), e);
		} //End catch

	} //End rename

	/**
	 * rename: This methods is to perform the rename operation
	 * @param newName - New name to be set to the object
	 * @return none
	 * @throws Exception
	 */
	public void rename(String newName) throws Exception {

		try {

			if (!this.isRenameDialogOpened())
				throw new Exception ("Rename dialog does not exists.");

			WebElement txtBox = this.msgDialog.findElement(By.cssSelector("div>input[class='prompt']")); //Web element of the value box
			txtBox.clear();
			txtBox.sendKeys(newName); //Enters the new name in the value box
			this.clickOkButton();
			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.rename : "+e.getMessage(), e);
		} //End catch

	} //End rename

	/**
	 * getPublicLink: This methods gets the public link from text box
	 * @param None
	 * @return Public link
	 * @throws Exception
	 */
	public String getPublicLink() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement textBox = this.msgDialog.findElement(By.id("sharedlink")); //Web element of the value box
			String value = textBox.getAttribute("value");
			return value;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isSharePublicLinkDlgOpened : "+e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MFilesDialog.isSharePublicLinkDlgOpened.",StopWatch.elapsedTime(startTime));
		}
	}		



	/**
	 * clickStopSharing : Clicks the stop sharing in share by me dialog
	 * @param None
	 * @throws Exception
	 */
	public void clickStopSharing(String docName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {


			List <WebElement> sharedObjectsList =driver.findElements(By.cssSelector("table>tbody[id='sharedList']>tr"));

			for (int loopIdx=0; loopIdx<sharedObjectsList.size(); loopIdx++) //Checks if hyperlink action is selected
				if (sharedObjectsList.get(loopIdx).findElement(By.cssSelector("td[class='share-tbl-col-title'] span")).getText().trim().equalsIgnoreCase(docName.trim())) {
					sharedObjectsList.get(loopIdx).findElement(By.cssSelector("td div[class='share-icon-delete']")).click();	
					break;
				}


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.clickStopSharing : "+e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MFilesDialog.clickStopSharing.: Completed..",StopWatch.elapsedTime(startTime));
		}//End finally
	}	//End clickStopSharing	



	/**
	 * setTargetVersionInPublicLink: This methods sets the version to share through public link
	 * @param version "Latest" or "This" as parameters
	 * @return None
	 * @throws Exception
	 */
	public void setTargetVersionInPublicLink(String version) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!version.equalsIgnoreCase("LATEST") || !version.equalsIgnoreCase("THIS"))
				throw new Exception("Version can be either Latest or This");

			List<WebElement> versionRBtns = this.msgDialog.findElements(By.cssSelector("label[class='allowVersion']>input")); //Web element of the value box

			for (int loopIdx = 0; loopIdx<versionRBtns.size(); loopIdx++) 
				if (versionRBtns.get(loopIdx).getText().toUpperCase().contains(version.toUpperCase()))
					ActionEventUtils.click(driver, versionRBtns.get(loopIdx));
			//versionRBtns.get(loopIdx).click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setTargetVersionInPublicLink : " + e);
		} //End catch

		finally {
			Log.event("MFilesDialog.setTargetVersionInPublicLink.",StopWatch.elapsedTime(startTime));
		}
	}	//setTargetVersionInPublicLink

	/**
	 * setDateTimeInPublicLink: This methods sets the expiry date and time of public link
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public void setDateTimeInPublicLink(String dd_mmm_yyyy, String hh_mm) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement dateSelector = this.msgDialog.findElement(By.id("shareDate")); //Web element of the value box
			ActionEventUtils.click(driver, dateSelector);
			//dateSelector.click();

			DatePicker datePicker = new DatePicker(driver);

			if (!datePicker.DatePickerExists())
				throw new Exception("Date Picker does not exists.");

			datePicker.SetCalendar(dd_mmm_yyyy);

			WebElement timeSelector = this.msgDialog.findElement(By.id("shareTime")); //Web element of the value box
			ActionEventUtils.click(driver, timeSelector);
			//	timeSelector.click();

			String[] time = hh_mm.split(":");
			timeSelector.sendKeys(time[0] + Keys.ARROW_RIGHT + time[1]);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setDateTimeInPublicLink : " + e);
		} //End catch

		finally {
			Log.event("MFilesDialog.setDateTimeInPublicLink.",StopWatch.elapsedTime(startTime));
		}
	}	//setTargetVersionInPublicLink





	//Reusable Functions
	/**
	 * Exists: This method is to check the existence of the Message dialog
	 * @param driver
	 * @return true if message dialog exists else false
	 * @throws Exception
	 */
	public static Boolean exists(WebDriver driver) throws Exception {

		try {

			new WebDriverWait(driver, 120).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])"))));

			WebElement mDialog = driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])")); //Web element div of the error message

			if (mDialog.isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.exists : "+e.getMessage(), e);
		} //End catch

	} //End Exists

	/**
	 * Exists: This method is to check the existence of the Message dialog
	 * @param driver
	 * @return true if message dialog exists else false
	 * @throws Exception
	 */
	public static Boolean exists(WebDriver driver, String caption) throws Exception {

		try {
			WebElement mDialog = null;
			int count = 0;
			new WebDriverWait(driver, 120).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])"))));

			List<WebElement> dialogs = driver.findElements(By.cssSelector("div[role='dialog']:not([style*='display: none']):not([class*='inprogressDialog'])>div>span[class='ui-dialog-title']")); //Web element div of the error message 

			for(count = 0; count < dialogs.size(); count++) {
				if(dialogs.get(count).getText().contains(caption)) {
					mDialog = dialogs.get(count).findElement(By.xpath("..")).findElement(By.xpath(".."));
					break;
				}
			}

			if(count == dialogs.size())
				return false;
			if (mDialog.isDisplayed())
				return true;
			else
				return false;

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.exists : "+e.getMessage(), e);
		} //End catch

	} //End Exists

	/**
	 * closeMFilesDialog: This method is to close the Message dialog
	 * @param driver
	 * @return none
	 * @throws Exception
	 */
	public static void closeMFilesDialog(WebDriver driver) throws Exception {

		//Variable Declaration
		int snooze = 0;

		try {

			if (!MFilesDialog.exists(driver))
				return;

			while (MFilesDialog.exists(driver) && snooze < 10) {

				MFilesDialog message = new MFilesDialog(driver);
				message.close();
				try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
				snooze++;
			}

			if (MFilesDialog.exists(driver))
				throw new Exception("MFiles dialog is not closed after closing several dialogs.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.closeMFilesDialog : "+e.getMessage(), e);
		} //End catch

	} //End Exists

	/**
	 * isSharePublicLinkDlgOpened: This method is to check Share Public link dialog is opened dialog has opened
	 * @param None
	 * @return Hyperlink text
	 * @throws Exception
	 */
	public static MFilesDialog isSharePublicLinkDlgOpened(WebDriver driver) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!MFilesDialog.exists(driver))
				throw new Exception("Exception at MFilesDialog.isSharePublicLinkDlgOpened : M-Files dialog does not opened.");

			MFilesDialog mfilesDialog = new MFilesDialog(driver);

			if (mfilesDialog.getTitle().toUpperCase().contains(Caption.MenuItems.SharePublicLink.Value.toUpperCase())) //Checks if MFiles dialog has title Get Hyperlink as title
				return mfilesDialog;
			else
				throw new Exception("Exception at MFilesDialog.isSharePublicLinkDlgOpened : M-Files dialog with title " + Caption.MenuItems.SharePublicLink.Value + " does not opened.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isSharePublicLinkDlgOpened : " + e);
		} //End catch

		finally {
			Log.event("MFilesDialog.isSharePublicLinkDlgOpened.",StopWatch.elapsedTime(startTime));
		}

	} //End isSharePublicLinkDlgOpened

	/**
	 * changePassword: This methods is to change the password
	 * @param oldPassword - Old Password
	 * @param newPassword - New Password
	 * @return none
	 * @throws Exception
	 */
	public void changePassword(String oldPassword, String newPassword) throws Exception {

		try {

			List<WebElement> passwordFields=driver.findElements(By.cssSelector("div[class='changePassword']>input[type='password']"));
			passwordFields.get(0).clear();
			passwordFields.get(0).sendKeys(oldPassword);
			passwordFields.get(1).clear();
			passwordFields.get(1).sendKeys(newPassword);
			passwordFields.get(2).clear();
			passwordFields.get(2).sendKeys(newPassword);

			this.clickOkButton();
			Utils.fluentWait(driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.changePassword : "+e.getMessage(), e);
		} //End catch

	} //changePassword

	/**
	 * confirmUndoCheckOut: This methods is to confirm the undo checkout operation
	 * @param isConfirm - true to click the Ok button / false to click the Cancel Button
	 * @return none
	 * @throws Exception
	 */
	public void confirmUndoCheckOut(Boolean isConfirm) throws Exception {

		try {
			int snooze = 0;
			new MFilesDialog(driver);

			while(snooze < 2 && MFilesDialog.exists(this.driver)) {

				if (!this.getTitle().toUpperCase().contains("UNDO CHECKOUT"))
					throw new Exception("Title of Confirm Undo Checkout dialog is not opened." + this.getTitle());			

				if (isConfirm) //Clicks Ok or Cancel based on the user input
					this.clickOkButton();
				else
					this.clickCancelButton();

				Utils.fluentWait(this.driver);
			}

			Utils.fluentWait(this.driver);

			if (MFilesDialog.exists(this.driver))
				throw new Exception("Message dialog not closed after clicking Yes in the Confirm Undo Checkout dialog.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.confirmUndoCheckOut : "+e.getMessage(), e);
		} //End catch

	} //End confirmUndoCheckOut

	/**
	 * IsUndoCheckOutPromtDisplayed: This method is to check if Undo-Checkout prompt is displayed
	 * @param None
	 * @return true if undo-checkout prompt is displayed / false if not
	 * @throws Exception
	 */
	public Boolean IsUndoCheckOutPromtDisplayed() throws Exception {

		try {

			if (!MFilesDialog.exists(this.driver))
				throw new Exception("Confirm Undo Checkout dialog is not opened.");

			if (this.getTitle().toUpperCase().contains("UNDO CHECKOUT"))
				return true;
			else 
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.IsUndoCheckOutPromtDisplayed : "+e.getMessage(), e);
		} //End catch

	} //End confirmUndoCheckOut

	/**
	 * IsCheckOutPromtDisplayed: This method is to check if Checkout prompt is displayed
	 * @param None
	 * @return true if checkout prompt is displayed / false if it is not
	 * @throws Exception
	 */
	public Boolean isCheckOutPromtDisplayed() throws Exception {

		try {

			if (!MFilesDialog.exists(this.driver)) 
				throw new Exception("MFiles Dialog is not Displayed.");

			WebElement checkoutBtn = this.msgDialog.findElement(By.cssSelector("div[class*='ui-widget-content']>div>table[class='buttons']>tbody>tr>td>button[class*='checkout']"));

			if (checkoutBtn.isDisplayed())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {

			if (e.getClass().toString().contains("Message Dialog Not Displayed"))
				return false;
			else	
				throw new Exception("Exception at MFilesDialog.isCheckOutPromtDisplayed : "+e.getMessage(), e);
		} //End catch

	} //End isCheckOutPromtDisplayed

	/**
	 * changeWorkflow: This methods is to set all fields in the workflow dialog
	 * @param workflowName - Name of the workflow
	 * @param stateName - Name of the state
	 * @param comments - Comments to be set
	 * @return none
	 * @throws Exception
	 */
	public void changeWorkflow(String workflowName, String stateName, String comments) throws Exception {

		try {	

			this.setWorkflow(workflowName);
			Utils.fluentWait(this.driver);
			this.setWorkflowState(stateName);
			Utils.fluentWait(this.driver);
			this.setWorkflowComments(comments);
			Utils.fluentWait(this.driver);
			this.clickButton("window_ok");
			Utils.fluentWait(this.driver);

			if (MFilesDialog.exists(this.driver))
				throw new Exception ("Workflow dialog not closed after clicking Ok button in the dialog");

		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.changeWorkflow : "+e.getMessage(), e);
		} //End catch
	} //End changeWorkflow

	/**
	 * getTextFromComboURLDialog: This methods retrive text box from Get Combo URL dialog
	 * @param 
	 * @param 
	 * @param 
	 * @return none
	 * @throws Exception
	 */
	public String getTextFromComboURLDialog(String item) throws Exception {

		try {	

			if(item.equalsIgnoreCase("textbox"))
			{
				WebElement textValueinGetComboURL = driver.findElement(By.id("ComboUrl"));
				Utils.fluentWait(this.driver);
				System.out.println(textValueinGetComboURL.getText());
				return textValueinGetComboURL.getText().trim();
			}
			else if(item.equalsIgnoreCase("textarea"))
			{
				WebElement textValueinGetComboURL = driver.findElement(By.id("ComboUrlDesc"));
				Utils.fluentWait(this.driver);
				return textValueinGetComboURL.getAttribute("value").trim();	
			}
			else
				throw new Exception ("Invalid arguments");

		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getTextFromComboURLDialog : "+e.getMessage(), e);
		} //End catch
	} //End getTextFromComboURLDialog

	/**
	 * getURLFromComboURLDialog: This methods retrive link URL from Get Combo URL dialog
	 * @param 
	 * @param 
	 * @param 
	 * @return none
	 * @throws Exception
	 */
	public String getURLFromComboURLDialog(String item) throws Exception {

		try {	

			WebElement textValueinGetComboURL = driver.findElement(By.id("ComboUrlDesc"));
			String[] textareaValue = textValueinGetComboURL.getAttribute("value").trim().split("\n");
			int index;
			for(index=0;index<textareaValue.length;index++)
			{
				//	Log.message(textareaValue[index]);
				if(textareaValue[index].startsWith(item))
				{ 
					String linkURL = textareaValue[index].substring(item.length()+1).trim();
					return linkURL;
				}
			}
		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.getURLFromComboURLDialog : "+e.getMessage(), e);
		} //End catch
		return null;
	} //End getTextFromComboURLDialog

	/**
	 * isComboURLEditable: This methods retrive text box from Get Combo URL dialog
	 * @param 
	 * @param 
	 * @param 
	 * @return none
	 * @throws Exception
	 */
	public Boolean isComboURLEditable() throws Exception {

		try {	
			String test = "Testing";
			WebElement textValueinGetComboURL = driver.findElement(By.id("ComboUrl"));
			Utils.fluentWait(this.driver);
			WebElement textareaValueinGetComboURL = driver.findElement(By.id("ComboUrlDesc"));
			Utils.fluentWait(this.driver);

			try {
				textValueinGetComboURL.sendKeys(test);
				if(textValueinGetComboURL.getText().equalsIgnoreCase(test))
					return true;
			}
			catch (Exception e1) {
				try {
					textareaValueinGetComboURL.sendKeys(test);
					if(textareaValueinGetComboURL.getAttribute("value").trim().equalsIgnoreCase(test))
						return true;
					else
						return false;
				}
				catch (Exception e2) {
					return false;
				}
			} //End catch

		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.isComboURLEditable : "+e.getMessage(), e);
		} //End catch
		return false;

	} //End isComboURLEditable

	/*--------------------------------Functions required for smoke test cases--------------------------------------*/

	/**
	 * SetWorkflow : Sets the specified workflow 
	 * @param workflowName - name of the workflow to be set
	 * @return none
	 * @throws Exception
	 */
	public void setWorkflow(String workflowName,String driverType) throws Exception {

		try {

			WebElement workflowSelect = msgDialog.findElement(By.cssSelector("select[id='workflow']"));
			ActionEventUtils.click(driver, workflowSelect);
			//	workflowSelect.click();
			Select select = new Select(workflowSelect);
			List<WebElement> workflowItems=select.getOptions();
			int workflowCt = workflowItems.size();
			int loopIdx = 0;

			for (loopIdx=0; loopIdx<workflowCt; loopIdx++) {
				System.out.println(workflowItems.get(loopIdx).getText());
				if (workflowItems.get(loopIdx).getText().toUpperCase().trim().equalsIgnoreCase(workflowName)) {

					if (driverType.equalsIgnoreCase("chrome") || driverType.equalsIgnoreCase("IE")||driverType.equalsIgnoreCase("edge") || driverType.equalsIgnoreCase("Safari")){
						ActionEventUtils.click(driver, workflowItems.get(loopIdx));
						//	workflowItems.get(loopIdx).click();
						select.selectByVisibleText(workflowItems.get(loopIdx).getText().trim());
					}
					else if (driverType.equalsIgnoreCase("firefox")) {
						Actions action=new Actions(driver);
						action.moveToElement(workflowItems.get(loopIdx)).click().build().perform();
						workflowSelect.sendKeys(Keys.RETURN);
					}

					if(select.getFirstSelectedOption().getText().trim().equalsIgnoreCase(workflowName))
						break;

				}
				else if(workflowName.trim().isEmpty())
				{
					select.selectByIndex(-1);
				}
			}
			if (loopIdx >= workflowCt)
				throw new Exception("Workflow (" + workflowName + ") does not exists in the list.");		

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NullPointerException"))
				throw new Exception("'Workflow' name is empty");
			else
				throw new Exception("Exception at MFilesDialog.setWorkflow : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflow

	/**
	 * SetWorkflowState : Sets the specified workflow state
	 * @param stateName - name of the workflow state to be set
	 * @return none
	 * @throws Exception
	 */
	public void setWorkflowState(String stateName,String driverType) throws Exception {

		try {
			Thread.sleep(500);
			WebElement stateSelect = msgDialog.findElement(By.cssSelector("select[id='state'][style='display: inline-block;'],select[id='state']"));
			Select select = new Select(stateSelect);
			List<WebElement> stateOptions=select.getOptions();
			int workflowCt = stateOptions.size();
			int loopIdx;

			for (loopIdx=0; loopIdx<workflowCt; loopIdx++) {
				System.out.println(stateOptions.get(loopIdx).getText());
				if (stateOptions.get(loopIdx).getText().toUpperCase().trim().equals(stateName.toUpperCase().trim())) {
					ActionEventUtils.click(driver, stateOptions.get(loopIdx));
					//stateOptions.get(loopIdx).click();
					Thread.sleep(300);
					if(driverType.equalsIgnoreCase("chrome")||driverType.equalsIgnoreCase("IE")||driverType.equalsIgnoreCase("edge")) {
						select.selectByVisibleText(stateOptions.get(loopIdx).getText().trim());
					}
					else if(driverType.equalsIgnoreCase("firefox")){
						stateOptions.get(loopIdx).sendKeys(Keys.RETURN);
						Actions action=new Actions(driver);
						action.moveToElement(stateOptions.get(loopIdx)).click().build().perform();
					}
					if(select.getFirstSelectedOption().getText().trim().equalsIgnoreCase(stateName.toUpperCase().trim()))
						break;
				}
			}

			if (loopIdx >= workflowCt)
				throw new Exception("Workflow state (" + stateName + ") does not exists in the list.");	

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at MFilesDialog.setWorkflowState : "+e.getMessage(), e);
		} //End catch

	} //End SetWorkflowState

	/**
	 * eSign: Function is used to perform Electroinc signature operation
	 * @param userName
	 * @param password
	 * @throws Exception
	 */
	public String eSign(String userName, String password, String domainName, String userFullName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			setESignUserName(userName);//Sets the username in the Electronic signature MFiles Dialog
			setESignPassword(password);//Sets the password in the Electronic signature MFiles Dialog

			if (!domainName.equals(""))//Checks if domain name is not empty
				setESignDomain(domainName);//Sets the domain name in the Electronic signature MFiles Dialog

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date date = new Date();
			String timeStamp = dateFormat.format(date)+ " (UTC+00.00) "+userFullName+" ("+userName+")";

			clickButton("sign");//Clicks the Sign button in the Electronic signature MFiles Dialog
			Utils.fluentWait(driver);

			return timeStamp;			

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.eSign : "+e.getMessage(), e);
		} //End catch

		finally{
			Log.event("MFilesDialog.eSign : Electronic signature is completed.", StopWatch.elapsedTime(startTime));
		}

	}//End eSign

	/**
	 * setESignUserName : Sets Username in E-sign dialog
	 * @param userName - Username
	 * @return none
	 * @throws Exception 
	 */
	public void setESignUserName(String userName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement esignDialog = msgDialog.findElement(By.cssSelector("table[class='buttons credentials']>tbody"));
			WebElement txtUserName = esignDialog.findElement(By.cssSelector("tr>td>input[id='UserName']"));

			txtUserName.click();
			txtUserName.clear();
			txtUserName.sendKeys(userName); //Enters the UserName

			Log.event("MFilesDialog.setESignUserName : Username is entered.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.setESignUserName : "+e.getMessage(), e);
		} //End catch

	} //End setESignUserName

	/**
	 * setEsignPassword : Sets password in E-sign dialog
	 * @param password - Password of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setESignPassword(String password) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement esignDialog = msgDialog.findElement(By.cssSelector("table[class='buttons credentials']>tbody"));
			WebElement txtPassword = esignDialog.findElement(By.cssSelector("tr>td>input[id='Password']"));

			txtPassword.click();
			txtPassword.clear();
			txtPassword.sendKeys(password); //Enters the password

			Log.event("MFilesDialog.setESignPassword : Password is entered.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.setEsignPassword : "+e.getMessage(), e);
		} //End catch

	} //End setESignPassword

	/**
	 * setESignDomain : Sets Domain name in E-sign dialog
	 * @param domainName - Domain of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setESignDomain(String domainName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement esignDialog = msgDialog.findElement(By.cssSelector("table[class='buttons credentials']>tbody"));
			WebElement txtDomain = esignDialog.findElement(By.cssSelector("tr>td>input[id='Domain']"));

			txtDomain.click();
			txtDomain.clear();
			txtDomain.sendKeys(domainName); //Enters the password

			Log.event("MFilesDialog.setESignDomain : Domain name is entered.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.setESignDomain : "+e.getMessage(), e);
		} //End catch

	} //End setESignDomain

	/**
	 * selectESignReason: selects the ESign reason in the E-Sign dialog
	 * @param reason
	 * @throws Exception
	 */
	public void selectESignReason(String reason)throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			WebElement esignMessageArea = msgDialog.findElement(By.cssSelector("table[class='message']>tbody"));
			WebElement eSignReasonPrompt = esignMessageArea.findElement(By.cssSelector("tr>td>select[class*='eSignPromptInfos']"));
			if (browser.equalsIgnoreCase("Edge"))
				eSignReasonPrompt.sendKeys(reason);
			else
				new Select(eSignReasonPrompt).selectByVisibleText(reason.trim()); //Selects the Reason from the list of options

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.selectESignReason : "+e.getMessage(), e);
		} //End catch
		finally {
			Log.event("MFilesDialog.selectESignReason : Reason is selected in the E-Sign dialog.", StopWatch.elapsedTime(startTime));
		}//End Finally

	}//End selectESignReason

	/**
	 * getESignDialogMessage: Gets the E-Sign dialog message 
	 * @return : E-Sign dialog message box texts
	 * @throws Exception
	 */
	public String getESignDialogMessage() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement esignMessageArea = msgDialog.findElement(By.cssSelector("table[class='message']>tbody"));
			WebElement eSignMessage = esignMessageArea.findElement(By.cssSelector("tr>td>textarea[class*='textMeaning']"));
			String eSignMsgText = eSignMessage.getText().trim();

			if (browser.equalsIgnoreCase("edge"))
			{
				String actualeSignMsgText = "";
				String[] actualValues = eSignMessage.getAttribute("value").trim().split("\n");

				for (int i = 0; i < actualValues.length; i++)
				{
					if (!actualValues[i].trim().equals(""))
						actualeSignMsgText += actualValues[i].trim();

					if (i != (actualValues.length-1))
						actualeSignMsgText += "\n";
				}
				return actualeSignMsgText;
			}

			return eSignMsgText;

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.getESignDialogText : "+e.getMessage(), e);
		} //End catch
		finally {
			Log.event("MFilesDialog.getESignDialogText : E-Sign dialog box text is returned.", StopWatch.elapsedTime(startTime));
		}//End Finally

	}//End getESignDialogText

	/**
	 * isESignDialogExist: Waits and checks if the Electronic Signature M-Files dialog is displayed
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public static boolean isESignDialogExist(WebDriver driver)throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			boolean wait = true;
			int snooze = 0;

			while(wait && snooze < 10)
			{
				try
				{
					new WebDriverWait(driver, 150).ignoring(NoSuchElementException.class)
					.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
							(driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none'])>div[id='ElectronicSignatureMfilesUser']"))));

					wait = false;//Element is displayed
				}
				catch (Exception e0){Thread.sleep(500);}

				snooze++;
			}//Waits for the element

			if (exists(driver, Caption.MFilesDialog.ElectronicSignature.Value))//Checks if ESign dialog exists in the view
				return true;//Returns dialog exist in the view

			return false;//Returns dialog not exist in the view

		} //End try
		catch(NoSuchElementException e)
		{
			return false;
		}
		catch(Exception e) {
			throw new Exception("Exception at MFilesDialog.isESignDialogExist : "+e.getMessage(), e);
		} //End catch

		finally {
			Log.event("MFilesDialog.isESignDialogExist : Checked E-Sign dialog is exist or not in the view...", StopWatch.elapsedTime(startTime));
		}//End Finally

	}//End isESignDialogExist

	/**
	 * enterExternalRepositoryCredentials: Enters username and password to already opened external repository login dialog and clicks OK button.
	 * @param username - Username for external repository
	 * @param password - Password for external repository
	 * @throws Exception
	 */
	public void enterExternalRepositoryCredentials(String username, String password) throws Exception{

		try{

			WebElement usernameInput = msgDialog.findElement(By.cssSelector("[name=username]"));
			WebElement passwordInput = msgDialog.findElement(By.cssSelector("[name=password]"));

			usernameInput.sendKeys(username);
			passwordInput.sendKeys(password);

			this.clickOkButton();

			Utils.fluentWait(driver);
		}
		catch(Exception e){
			throw new Exception("Exception at MFilesDialog.enterExternalRepositoryCredentials: " + e.getMessage(), e);
		}

	}

	/**
	 * clickExternalRepositoryTableCell: Clicks a cell in the external repositories dialog. This effectively selects the row of the external repository.
	 * @param repositoryName - Displayname of the external repository to be clicked in the dialog.
	 * @throws Exception
	 */
	public void clickExternalRepositoryTableCell(String repositoryName) throws Exception{

		try{

			//This selector will select all cells in the dialog table
			List<WebElement> repositoryTableCells = msgDialog.findElements(By.cssSelector(".repository-tbl-col-title>div"));

			for(int i = 0; i < repositoryTableCells.size(); ++i){

				if(repositoryTableCells.get(i).getText().equals(repositoryName)){

					//Click the external repository cell in the table of external repositories
					ActionEventUtils.click(driver, repositoryTableCells.get(i));
					break;
				}
			}
		}
		catch(Exception e){
			throw new Exception("Exception at MFilesDialog.clickExternalRepositoryTableCell: " + e.getMessage(), e);
		}

	}

	/**
	 * loginToExternalRepository: Selects the external repository in the dialog and logs in by entering username and password. The dialog is 
	 * closed in the end and the external repository can be accessed immediately after this method.
	 * @param username - Username for external repository
	 * @param password - Password for external repository
	 * @param repositoryName - Displayname of the external repository
	 * @throws Exception
	 */
	public void loginToExternalRepository(String username, String password, String repositoryName) throws Exception{

		try{

			this.clickExternalRepositoryTableCell(repositoryName);

			WebElement loginButton = msgDialog.findElement(By.cssSelector("[class*=login]"));
			ActionEventUtils.click(driver, loginButton);

			MFilesDialog loginDialog = new MFilesDialog(driver, repositoryName);
			loginDialog.enterExternalRepositoryCredentials(username, password);

			this.clickCloseButton();
		}
		catch(Exception e){
			throw new Exception("Exception at MFilesDialog.loginToExternalRepository: " + e.getMessage(), e);
		}
	}

	/**
	 * logoutFromExternalRepository: Selects the external repository in the dialog and clicks logout button. The dialog is also closed.
	 * @param repositoryName - Displayname of the external repository
	 * @throws Exception
	 */
	public void logoutFromExternalRepository(String repositoryName) throws Exception{

		try{

			this.clickExternalRepositoryTableCell(repositoryName);

			WebElement logoutButton = msgDialog.findElement(By.cssSelector("[class*=logout]"));
			ActionEventUtils.click(driver, logoutButton);

			this.clickCloseButton();
		}
		catch(Exception e){
			throw new Exception("Exception at MFilesDialog.logoutFromExternalRepository: " + e.getMessage(), e);
		}
	}

	/**
	 * getLoggedInStatusOfExternalRepository: Reads the return value from the already opened external repositories dialog.
	 * @param repoName - Displayname of the external repository
	 * @return Returns string of what is displayed in the "User column" for the external repository in the dialog.
	 * Format: "Logged in as username" OR "Not logged in". Returns null if the external repository is not found in the dialog.
	 * @throws Exception
	 */
	public String getLoggedInStatusOfExternalRepository(String repoName) throws Exception {

		//Boolean repoFound = false;

		try{
			List<WebElement> extRepositoryTableRows = driver.findElements(By.cssSelector(".externalRepositoryList>tr"));

			for(int i = 0; i < extRepositoryTableRows.size(); ++i){

				WebElement currentRow = extRepositoryTableRows.get(i);

				//Get text of the first div element in the row. It is the repository name.
				String repoNameInElement = currentRow.findElement(By.cssSelector("td:first-child>div")).getText();

				if(repoNameInElement.equals(repoName)){

					//repoFound = true;

					//Get text of the second div element in the row. It is the string which displays login status of external repository.
					return currentRow.findElement(By.cssSelector("td:nth-child(2)>div")).getText();
				}
			}

			/*if(!repoFound)
				throw new Exception("Exception at MFilesDialog.logoutFromExternalRepository: External repository " + repoName + " not found in dialog.");
			 */
		}
		catch(Exception e){
			throw new Exception("Exception at MFilesDialog.getUsernameLoggedInToExternalRepository: " + e.getMessage(), e);	
		}

		//The external repository was not found in the dialog.
		return null;
	}

}
package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.LoginPage;

public class ConfigurationPanel {

	//Variable Declaration
	private final WebDriver driver;
	private WebElement configurationDiv;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");

	/**
	 * ConfigurationPanel : Constructor to instantiate Configuration Panel
	 * @param driver - Web eriver
	 * @return None
	 * @throws Exception 
	 */	
	public ConfigurationPanel(final WebDriver driver) throws Exception {
		try {

			this.driver = driver;
			new WebDriverWait(this.driver, 60).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(this.driver.findElement(By.cssSelector("div[id='divConfig']"))));

			this.configurationDiv = this.driver.findElement(By.cssSelector("div[id='divConfig']"));
		}
		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.ConfigurationPanel : "+e.getMessage(), e);
		}

	} //ConfigurationPanel


	/**
	 * setWindowsSSO : This method is to set the option for window sso user
	 * @param option - Any one of the option Disabled(default),Show on login page, use automatically
	 * @return None
	 * @throws Exception 
	 */	
	public void setWindowsSSO(String option) throws Exception {

		try {
			List<WebElement> labelToSelect = this.driver.findElement(By.id("configgeneral")).findElements(By.cssSelector("td[class='listelement windowsAuthentication']>span>label"));
			//List<WebElement> labelToSelect = this.driver.findElements(By.name("windowsAuth")); //Gets all the elements with the windows auth input
			int noOfElmnts = labelToSelect.size(); //Number of label elements
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) //Loops to identify the instance of the item to be clicked
			{	
				Log.message(labelToSelect.get(itemIdx).getText());
				if (labelToSelect.get(itemIdx).getText().toUpperCase().trim().equals(option.toUpperCase())) {

					WebElement windowSSORow = labelToSelect.get(itemIdx).findElement(By.xpath("..")); //Web element row of the window sso
					WebElement rBtnToSelect = windowSSORow.findElement(By.name("windowsAuth")); //Web Element radio button to select
					//rBtnToSelect.click(); //Selects the option of window sso
					ActionEventUtils.click(driver, rBtnToSelect);
					break;

				}
			}		
			if (itemIdx >= noOfElmnts) //Throws exception if the item to select does not exits
				throw new Exception("Item (" + option + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at ConfigurationPanel.setWindowsSSO : "+ e.getMessage(), e);
		} //End catch

	} //End setWindowsSSO

	/*-----------------------------------Configuration General Settings Page --------------------------------------------*/

	/**
	 * getPageName : This method gets the name of the page that is currently viewed
	 * @param None
	 * @return Name of the page
	 * @throws Exception 
	 */	
	public String getPageName() throws Exception {

		try {

			WebElement lastModElement = this.configurationDiv.findElement(By.cssSelector("div[class='configHeader']>span[id='configurationTitle']")); //Information of the page slected
			return (lastModElement.getText()); //Text in page information header

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getPageName : "+e.getMessage(), e);
		} //End catch

	} //End getPageName

	/**
	 * getLastModifiedDateTime : This method gets the last modified date and time
	 * @param None
	 * @return Date and Time in the format of yyyy-mm-dd hh:mm:ss
	 * @throws Exception 
	 */	
	public String getLastModifiedDateTime() throws Exception {

		try {


			WebElement lastModElement = this.configurationDiv.findElement(By.cssSelector("div[class='configHeader']>p[class='configFooter']>span[id='lastModified']")); //Last modified date and time web element
			return (lastModElement.getText()); //Text in the last modified date and time

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getLastModifiedDateTime : "+e.getMessage(), e);
		} //End catch

	} //End getLastModifiedDateTime


	/**
	 * saveSettings : This method is to clicks Save button in the General page
	 * @param None
	 * @return true if save is successful; false if not
	 * @throws Exception 
	 */	
	public String clickSavebtn() throws Exception {

		try {

			WebElement saveBtn = this.configurationDiv.findElement(By.cssSelector("div[id='actionControls']>input[id='saveSettings']")); //web element Save button
			saveBtn.click();					

			String currDateTime = "";

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			currDateTime = dateFormat.format(date);

			System.out.println(currDateTime);

			return currDateTime;

		}
		catch(Exception e) {
			throw new Exception("Exception at ConfigurationPanel.clickSavebtn  : Exception atError while confirming changes to Configuration settings. : "+e.getMessage(), e);
		}

	}






	/**
	 * saveSettings : This method is to clicks Save button in the General page
	 * @param None
	 * @return true if save is successful; false if not
	 * @throws Exception 
	 */	
	public Boolean saveSettings() throws Exception {

		try {

			WebElement saveBtn = this.configurationDiv.findElement(By.cssSelector("div[id='actionControls']>input[id='saveSettings']")); //web element Save button

			if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()",saveBtn);
			else
				ActionEventUtils.click(driver, saveBtn);

			//saveBtn.click(); //Clicks Save button
			Utils.fluentWait(this.driver);



			while (true) {

				//Checks if Settings saved successfully error message is displayed
				if (this.getMsgOnSaving().toUpperCase().trim().contains("SETTINGS SAVED SUCCESSFULLY")) {
					this.closeErrorDialog();
					return true;
				}
				else if (this.getMsgOnSaving().toUpperCase().trim().contains("REFERENCE NOT SET")) {
					this.closeErrorDialog(); //Close Error dialog
					saveBtn = this.configurationDiv.findElement(By.cssSelector("div[id='actionControls']>input[id='saveSettings']")); //web element Save button
					//saveBtn.click(); //Clicks Save button
					ActionEventUtils.click(driver, saveBtn);
				}
				else if (this.getMsgOnSaving().toUpperCase().trim().contains("PLEASE ENTER A VALID ID FOR THE DEFAULT VIEW.")) {
					this.closeErrorDialog(); //Close Error dialog
					this.setDefaultView("Home");
					saveBtn = this.configurationDiv.findElement(By.cssSelector("div[id='actionControls']>input[id='saveSettings']")); //web element Save button
					//saveBtn.click(); //Clicks Save button
					ActionEventUtils.click(driver, saveBtn);
				}
				else
					return false;
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.saveSettings : "+e.getMessage(), e);
		} //End catch

	} //End saveSettings

	/**
	 * getMsgOnSaving : This method gets the message displayed on saving the settings
	 * @param None
	 * @return Message displayed on saving error message
	 * @throws Exception 
	 */	
	public String getMsgOnSaving() throws Exception {

		try {

			MFilesDialog mfilesDialog = new MFilesDialog (driver);
			return (mfilesDialog.getMessage());

			/*new WebDriverWait(driver, 120).ignoring(NoSuchElementException.class)
				.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
						(driver.findElement(By.cssSelector("div[class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable']"))));

			WebElement errorDlg = driver.findElement(By.cssSelector("div[class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable']")); //Web element div of the error message
			WebElement errorMsgRow = errorDlg.findElement(By.cssSelector("div[class='window_pages']>div[class='errorDialog']>div[class='shortErrorArea']")); //Web element of the error message
			return (errorMsgRow.getText()); //Gets the error message displayed
			 */			
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getMsgOnSaving : "+e.getMessage(), e);
		} //End catch

	} //End getMsgOnSaving

	/**
	 * closeErrorDialog : This method clicks ok button in error message dialog
	 * @param None
	 * @return None
	 * @throws Exception 
	 */	
	public void closeErrorDialog() throws Exception {

		try {

			MFilesDialog mfilesDialog = new MFilesDialog(driver);
			mfilesDialog.clickOkButton();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.closeErrorDialog : "+e.getMessage(), e);
		} //End catch

	} //End closeErrorDialog

	/**
	 * resetSettings : This method clicks Rest button in the General page
	 * @param None
	 * @return None
	 * @throws Exception 
	 */	
	public void resetSettings() throws Exception {

		try {
			int snooze = 0;
			WebElement resetBtn = this.configurationDiv.findElement(By.cssSelector("div[id='actionControls']>input[id='reloadSettings']")); //Web element reset button
			//resetBtn.click(); //Clicks reset button
			while(snooze < 5 && !resetBtn.isDisplayed()) {
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", resetBtn);
				snooze++;
			}
			if (resetBtn.isDisplayed())
				ActionEventUtils.click(driver, resetBtn);
			else
				throw new Exception("Reset button is not displayed to click in the configuration webpage.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.resetSettings : "+e.getMessage(), e);
		} //End catch

	} //End resetSettings

	/**
	 * getAccessMessage : This method gets the displayed access message
	 * @param None
	 * @return Access message
	 * @throws Exception 
	 */	
	public String getAccessMessage() throws Exception {

		try {

			WebElement accessMessageDiv = driver.findElement(By.cssSelector("div[id='rightPanel']>div[id='accessMessage']")); //Information of the page slected

			if (accessMessageDiv.isDisplayed())
				return (accessMessageDiv.getText());

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException") || e.getClass().toString().contains("StaleElementReferenceException"))
				return null;
			else
				throw new Exception("Exception at ConfigurationPanel.getAccessMessage : "+e.getMessage(), e);
		} //End catch

		return null;

	} //End getAccessMessage

	/**
	 * getPort : This method is to get the port name in the general configuration page
	 * @param None
	 * @return Port Name
	 * @throws Exception 
	 */	
	public String getPort() throws Exception {

		try {

			WebElement portRow = this.getWebElement("Port:"); //Gets the row element of the prot
			return (portRow.findElement(By.cssSelector("td[class='fieldLabel']+td")).getText()); // Gets the text in the port

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getPort : "+e.getMessage(), e);
		} //End catch

	} //End GetPort

	/**
	 * getProtocol : This method gets the value in the protocol
	 * @param None
	 * @return Protocol string
	 * @throws Exception 
	 */	
	public String getProtocol() throws Exception {

		try {

			WebElement protocolRow = this.getWebElement("Protocol:"); //Protocol row Web element 
			return (protocolRow.findElement(By.cssSelector("td[class='fieldLabel']+td")).getText()); //Text of the protocol value

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getProtocol : "+e.getMessage(), e);
		} //End catch

	} //End GetProtocol

	/**
	 * setRestrictAccess : This method is to select/un-select restrict access
	 * @param isRestrict - true to select and false to un-select check box
	 * @return None
	 * @throws Exception 
	 */	
	public void setRestrictAccess(Boolean isRestrict) throws Exception {

		try {

			WebElement restrictAccessRow = this.getWebElement("Restrict access to configuration pages"); //Web Element of Restrict access to configuration pages
			WebElement checkBoxElement = restrictAccessRow.findElement(By.cssSelector("input[id='restrictAccess']")); //Web element of the check box

			if (!checkBoxElement.isSelected() && isRestrict) //Selects the restrict web access if to restrict and already not selected
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isRestrict)  //Un-Selects the restrict web access if not to restrict and already selected
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setRestrictAccess : "+e.getMessage(), e);
		} //End catch

	} //End setRestrictAccess

	/**
	 * getRestrictAccess : This method is to get the status of the restrict access checkbox
	 * @param None
	 * @return true if selected; false if not
	 * @throws Exception 
	 */
	public Boolean getRestrictAccess() throws Exception {

		try {

			WebElement restrictAccessRow = this.getWebElement("Restrict access to configuration pages"); //Web Element of Restrict access to configuration pages
			WebElement checkBoxElement = restrictAccessRow.findElement(By.cssSelector("input[id='restrictAccess']")); //Web element of the check box
			return (checkBoxElement.isSelected());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getRestrictAccess : "+e.getMessage(), e);
		} //End catch

	} //End GetRestrictAccess

	/**
	 * enableIPRange : This function will enabled the IP Range fields in the configuration webpage based on the parameter set
	 * @param set: True/false
	 * @return : True/False
	 * @throws Exception
	 */
	public boolean enableIPRange(boolean set) throws Exception

	{
		final long startTime = StopWatch.startTime();

		try {

			WebElement restrictAccessRow = this.driver.findElement(By.cssSelector("input[id='restrictAccess']")); //Web Element of Allowed IP Range

			if(!(restrictAccessRow.isSelected()) && set)
				ActionEventUtils.click(driver, restrictAccessRow);
			else if(restrictAccessRow.isSelected() && !set)
				ActionEventUtils.click(driver, restrictAccessRow);


			return restrictAccessRow.isSelected();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.enableIPRange : "+e.getMessage(), e);
		} //End catch
		finally
		{
			Log.event("ConfigurationPanel.enableIPRange: Completed..", StopWatch.elapsedTime(startTime));
		}

	}//End enableIPRange

	/**
	 * setAllowedIPRange : This method is to set the allowed IP Range
	 * @param ipRange - IP Range as String. (Eg : 192.168.1.0/24)
	 * @return None
	 * @throws Exception 
	 */	
	public void setAllowedIPRange(String ipRange) throws Exception {

		try {

			String[] ipRangeSplit = ipRange.split("//"); //Splits to string to set in the text box

			WebElement restrictAccessRow = this.getWebElement("Allowed IP range:"); //Web Element of Allowed IP Range
			WebElement ipRangeTxtBox = restrictAccessRow.findElement(By.cssSelector("td[class='fieldValue ipRange']>input[id='txtIpRange1']")); //Webelement of the text box
			ipRangeTxtBox.clear(); //Clears the old entry if any
			ipRangeTxtBox.sendKeys(ipRangeSplit[0]); //Enters the ip range
			ipRangeTxtBox = restrictAccessRow.findElement(By.cssSelector("td[class='fieldValue ipRange']>input[id='txtIpRange2']")); //Webelement of the text box
			ipRangeTxtBox.clear(); //Clears the old entry if any
			ipRangeTxtBox.sendKeys(ipRangeSplit[1]); //Enters the ip range

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAllowedIPRange : "+e.getMessage(), e);
		} //End catch

	} //End setAllowedIPRange

	/**
	 * getAllowedIPRange : This method is to get the value in the allowed IP Range
	 * @param None
	 * @return  IP Range as string (Eg : 192.168.1.0/24)
	 * @throws Exception 
	 */
	public String getAllowedIPRange() throws Exception {

		try {

			WebElement restrictAccessRow = this.getWebElement("Allowed IP range:"); //Web Element of Allowed IP Range
			WebElement ipRangeTxtBox = restrictAccessRow.findElement(By.cssSelector("td[class='fieldValue ipRange']>input[id='txtIpRange1']")); //Web element of the text box
			String ipRange = ipRangeTxtBox.getText(); //Gets the IP range
			ipRange = ipRange + "//";
			ipRangeTxtBox = restrictAccessRow.findElement(By.cssSelector("td[class='fieldValue ipRange']>input[id='txtIpRange2']")); //Web element of the text box
			ipRange = ipRange + ipRangeTxtBox.getText(); //Gets the IP range
			return ipRange;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAllowedIPRange : "+e.getMessage(), e);
		} //End catch

	} //End getAllowedIPRange

	/**
	 * setPageTitle : This method sets the page title
	 * @param pageTitle - Title of the page to be displayed
	 * @return None
	 * @throws Exception 
	 */	
	public void setPageTitle(String pageTitle) throws Exception {

		try {

			WebElement restrictAccessRow = this.getWebElement("Page title:"); //Web Element of Page title
			WebElement pageTitleTxtBox = restrictAccessRow.findElement(By.cssSelector("input[id='txtPageTitle']")); //Web element page title text box
			pageTitleTxtBox.clear(); //Clears the page title text box
			pageTitleTxtBox.sendKeys(pageTitle); //Enters the value in page title

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setPageTitle : "+e.getMessage(), e);
		} //End catch

	} //End setPageTitle

	/**
	 * getPageTitle : This method gets the value entered in the page title
	 * @param None
	 * @return Value of the page title in string
	 * @throws Exception 
	 */
	public String getPageTitle() throws Exception {

		try {

			WebElement restrictAccessRow = this.getWebElement("Page title:"); //Web Element of Page title
			WebElement pageTitleTxtBox = restrictAccessRow.findElement(By.cssSelector("input[id='txtPageTitle']")); //Web element page title text box
			return (pageTitleTxtBox.getAttribute("value")); //Gets the value in page title

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getPageTitle : "+e.getMessage(), e);
		} //End catch

	} //End getPageTitle

	/**
	 * setLanguage : This method is to select the language
	 * @param language - Language to select from combo box
	 * @return None
	 * @throws Exception 
	 */	
	public void setLanguage(String language) throws Exception {

		try {

			WebElement languageRow = this.getWebElement("Language:"); //Web Element of Language row
			List<WebElement> languageOptions = languageRow.findElements(By.cssSelector("select[id='selectLanguage']>option")); //Web Element options of the language list

			int optionCount = languageOptions.size(); //Number of available languages in the list
			int itemIdx=0;

			for (itemIdx=0; itemIdx<optionCount; itemIdx++)  //Loops to identify the web element of the language to be selected
				if (languageOptions.get(itemIdx).getText().toUpperCase().trim().equals(language.toUpperCase())) {
					//languageOptions.get(itemIdx).click(); //Selects the language from list
					ActionEventUtils.click(driver, languageOptions.get(itemIdx));
					return;
				}

			if (itemIdx >= optionCount) //Throws exception if language specified does not exists in the list
				throw new Exception("Language Option (" + language + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setLanguage : "+e.getMessage(), e);
		} //End catch

	} //End setLanguage

	/**
	 * getLanguage : This method is to get the selected language
	 * @param None
	 * @return Selected language as string
	 * @throws Exception 
	 */	
	public String getLanguage() throws Exception {

		try {

			WebElement languageRow = this.getWebElement("Language:"); //Web Element of Page title
			WebElement languageTxtBox = languageRow.findElement(By.cssSelector("select[id='selectLanguage']>option[selected]")); //Web Element options of the language list
			return (languageTxtBox.getText());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getLanguage : "+e.getMessage(), e);
		} //End catch

	} //End getLanguage



	/**
	 * getWindowsSSO : This method is to get the item that is selected for window sso
	 * @param None
	 * @return Returns the label in the format of string.
	 * @throws Exception 
	 */	
	public String getWindowsSSO() throws Exception {

		try {

			//			List<WebElement> rBtnToGetValue = this.driver.findElements(By.cssSelector("div[id='divConfig']>div[class='settings-container1']>div[id='configgeneral']>input[name='windowsAuth']")); //Gets all the elements with the windows auth input 
			List<WebElement> rBtnToGetValue = this.driver.findElements(By.name("windowsAuth")); //Gets all the elements with the windows auth input
			int noOfElmnts = rBtnToGetValue.size(); //Number of radio button in the page
			int itemIdx = 0;
			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++) //Loops to identify the selected radion button for window sso
				if (rBtnToGetValue.get(itemIdx).isSelected()) {

					WebElement windowSSORow = rBtnToGetValue.get(itemIdx).findElement(By.xpath("..")); //Web element row of the window sso
					return (windowSSORow.findElement(By.cssSelector("label")).getText()); //Lable of the selected radio button
				}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getWindowsSSO : "+e.getMessage(), e);
		} //End catch

		return "";

	} //End SetWindowsSSO

	/**
	 * setForceMFilesUserLogin : This method is to set force mfiles user login
	 * @param isForceLogin - true to force M-Files user login; false if not
	 * @return None
	 * @throws Exception 
	 */	
	public void setForceMFilesUserLogin(Boolean isForceLogin) throws Exception {

		try {

			WebElement forceMFilesUserRow = this.getWebElement("Force M-Files user login"); //Web Element of force M-Files user login row
			WebElement checkBoxElement = forceMFilesUserRow.findElement(By.cssSelector("input[id='chkForceMFilesUserLogin']")); //Web element of the check box
			WebElement forceMFilesUserRow1 = this.getWebElement("Windows SSO:"); //Web Element of force M-Files user login row
			WebElement rdobtnElement = forceMFilesUserRow1.findElement(By.cssSelector("input[id='DefaultLogin']")); //Web element of the check box

			if (!rdobtnElement.isSelected() && isForceLogin)
			{
				Utils.fluentWait(driver);
				//rdobtnElement.click();
				ActionEventUtils.click(driver, rdobtnElement);
				Utils.fluentWait(driver);
			}
			if (!checkBoxElement.isSelected() && isForceLogin) //Selects the Force M-Files user login
			{
				Utils.fluentWait(driver);
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
				Utils.fluentWait(driver);
			}
			else if (checkBoxElement.isSelected() && !isForceLogin)  //De-Selects the Force M-Files user login
			{
				Utils.fluentWait(driver);
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
				Utils.fluentWait(driver);
			}

			/*if (this.isAutoLoginUserNameEnabled())
				this.setAutoLogin(false);*/

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setForceMFilesUserLogin : "+e.getMessage(), e);
		} //End catch

	} //End setForceMFilesUserLogin

	/**
	 * getForceMFilesUserLogin : This method is to gets force mfiles user login
	 * @param None
	 * @return true if force M-Files user login; false if not
	 * @throws Exception 
	 */	
	public Boolean getForceMFilesUserLogin() throws Exception {

		try {

			WebElement forceMFilesUserRow = this.getWebElement("Force M-Files user login"); //Web Element of Restrict access to configuration pages
			WebElement checkBoxElement = forceMFilesUserRow.findElement(By.cssSelector("input[id='chkForceMFilesUserLogin']")); //Web element of the check box
			return (checkBoxElement.isSelected());

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getForceMFilesUserLogin : "+e.getMessage(), e);	
		} //End catch

	} //End GetForceMFilesUserLogin

	/**
	 * setDefaultAuthType : This method is to select default authentication type
	 * @param authenticationType - Windows user or M-Files user to select
	 * @return None
	 * @throws Exception 
	 */	
	public void setDefaultAuthType(String authenticationType) throws Exception {

		try {

			WebElement forceMFilesUserRow = this.getWebElement("Default authentication type:"); //Web Element of Set Default Authentication

			switch (authenticationType.toUpperCase()) {
			case "WINDOWS USER" : { //Selects if windows user
				this.setForceMFilesUserLogin(false);
				WebElement rBtnAuthentication = forceMFilesUserRow.findElement(By.cssSelector("input[id='winauth']"));
				//rBtnAuthentication.click();
				ActionEventUtils.click(driver, rBtnAuthentication);
				break;
			}
			case "M-FILES USER" : { //Selects if M-Files user
				WebElement rBtnAuthentication = forceMFilesUserRow.findElement(By.cssSelector("input[id='mfilesauth']"));
				//rBtnAuthentication.click();
				ActionEventUtils.click(driver, rBtnAuthentication);
				break;
			}
			default: { //Throws exception if input is of no windows or M-Files
				throw new Exception("Item (" + authenticationType + ") does not exists in the list.");
			}
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setDefaultAuthType : "+e.getMessage(), e);
		} //End catch

	} //End SetDefaultAuthType

	/**
	 * getDefaultAuthType : This method is to get default authentication type
	 * @param None
	 * @return Windows user or M-Files user
	 * @throws Exception 
	 */	
	public String getDefaultAuthType() throws Exception {

		try {

			WebElement forceMFilesUserRow = this.getWebElement("Default authentication type:"); //Web Element of Restrict access to configuration pages

			if (forceMFilesUserRow.findElement(By.cssSelector("input[id='winauth']")).isSelected()) //Returns windows user if it is selected
				return ("Windows user");
			else if (forceMFilesUserRow.findElement(By.cssSelector("input[id='mfilesauth']")).isSelected()) //Returns M-Files user if it is selected
				return ("M-Files user");
			else
				throw new Exception("Exception in Default Authentication type.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getDefaultAuthType : "+e.getMessage(), e);
		} //End catch

	} //End GetDefaultAuthType

	/**
	 * isDefaultAuthTypeEnabled : This method is to check if default authentication type is enabled
	 * @param authenticationType - Windows user or M-Files user to select
	 * @return true if authentication type is enabled; false if not
	 * @throws Exception 
	 */	
	public boolean isDefaultAuthTypeEnabled(String authenticationType) throws Exception {

		try {

			WebElement forceMFilesUserRow = this.getWebElement("Default authentication type:"); //Web Element of Set Default Authentication

			switch (authenticationType.toUpperCase()) {
			case "WINDOWS USER" : { //Selects if windows user
				if (forceMFilesUserRow.findElement(By.cssSelector("input[id='winauth']")).isEnabled())
					return true;
				else
					return false;
			}
			case "M-FILES USER" : { //Selects if M-Files user
				if (forceMFilesUserRow.findElement(By.cssSelector("input[id='mfilesauth']")).isEnabled())
					return true;
				else
					return false;
			}
			default: { //Throws exception if input is of no windows or M-Files
				throw new Exception("Item (" + authenticationType + ") does not exists in the list.");
			}
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isDefaultAuthTypeEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isDefaultAuthTypeEnabled

	/**
	 * SetAutoLogin : This method is to select or unselect the Auto-login 
	 * @param isAutoLogin - true to enable auto login; false if not
	 * @return None
	 * @throws Exception 
	 */	
	public void setAutoLogin(Boolean isAutoLogin) throws Exception {

		try {

			WebElement autoLoginChkBox = this.driver.findElement(By.cssSelector("input[id='autoLogin']")); //Gets all the elements with the windows auth input 

			if (!autoLoginChkBox.isSelected() && isAutoLogin) //Selects the auto login check box
				ActionEventUtils.click(driver, autoLoginChkBox);
			//autoLoginChkBox.click();
			else if (autoLoginChkBox.isSelected() && !isAutoLogin) //Un-Select the auto login check box
				ActionEventUtils.click(driver, autoLoginChkBox);
			//autoLoginChkBox.click();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAutoLogin : "+e.getMessage(), e);
		} //End catch

	} //End setAutoLogin

	/**
	 * getAutoLogin : This method is to select or unselect the Auto-login 
	 * @param None
	 * @return true to enable auto login; false if not
	 * @throws Exception 
	 */	
	public Boolean getAutoLogin() throws Exception {

		try {

			WebElement autoLoginChkBox = this.driver.findElement(By.cssSelector("input[id='autoLogin']")); 

			if (autoLoginChkBox.isSelected()) //Returns true if auto-login is selected
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAutoLogin : "+e.getMessage(), e);
		} //End catch

	} //End getAutoLogin

	/**
	 * setAutoLoginUserName : This method is to set the user name set in auto login
	 * @param userName - Sets the user name
	 * @return None
	 * @throws Exception
	 */
	public void setAutoLoginUserName(String userName) throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Username:"); //Web Element User name row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtUserName']")); //Text box web element user name
			int snooze = 0;			

			while (snooze < 5 && !autoLoginElement.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", autoLoginElement);
				snooze++;
			}

			if (autoLoginElement.isEnabled()) {//Enters the user name if the user name text box is enabled
				autoLoginElement.clear(); //Clears the text box
				autoLoginElement.sendKeys(userName);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAutoLoginUserName : "+e.getMessage(), e);
		} //End catch

	} //End setAutoLoginUserName

	/**
	 * getAutoLoginUserName : This method is to get the user name set in auto login
	 * @param None
	 * @return User Name
	 * @throws Exception
	 */
	public String getAutoLoginUserName() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Username:"); //Web Element of user name row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtUserName']")); //Web element of user name text box
			return (autoLoginElement.getText()); //Gets the value typed in the text box

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAutoLoginUserName : "+e.getMessage(), e);
		} //End catch

	} //End getAutoLoginUserName

	/**
	 * isAutoLoginUserNameEnabled : This method is to check if auto login user name set is enabled
	 * @param None
	 * @return true if user name text box is enabled; false if not
	 * @throws Exception
	 */
	public boolean isAutoLoginUserNameEnabled() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Username:"); //Web Element User name row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtUserName']")); //Text box web element user name

			if (autoLoginElement.isEnabled())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isAutoLoginUserNameEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isAutoLoginUserNameEnabled

	/**
	 * setAutoLoginPassword : This method is set the auto-login password
	 * @param password - Password
	 * @return None
	 * @throws Exception
	 */
	public void setAutoLoginPassword(String password) throws Exception {
		try {

			WebElement autoLoginElement = this.getWebElement("Password:"); //Web Element of Password row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtPassword']")); //Text box web element of password
			int snooze = 0;			

			while (snooze < 5 && !autoLoginElement.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", autoLoginElement);
				snooze++;
			}

			if (autoLoginElement.isEnabled()) {//Sets the password value if it is enabled
				autoLoginElement.clear();
				autoLoginElement.sendKeys(password);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAutoLoginPassword : "+e.getMessage(), e);
		} //End catch

	} //End setAutoLoginPassword

	/**
	 * getAutoLoginPassword : This method is gets the auto-login password
	 * @param None
	 * @return Password
	 * @throws Exception
	 */
	public String getAutoLoginPassword() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Password:"); //Web Element of Password row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtPassword']")); //Text box web element of password 
			return (autoLoginElement.getText()); //Gets the value stored in password text box

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAutoLoginPassword : "+e.getMessage(), e);
		} //End catch

	} //End getAutoLoginPassword

	/**
	 * isAutoLoginPasswordEnabled : This method is get the enabled status of auto login password
	 * @param None
	 * @return true if password text box is enabled; false if not
	 * @throws Exception
	 */
	public boolean isAutoLoginPasswordEnabled() throws Exception {
		try {

			WebElement autoLoginElement = this.getWebElement("Password:"); //Web Element of Password row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtPassword']")); //Text box web element of password

			if (autoLoginElement.isEnabled()) 
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isAutoLoginPasswordEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isAutoLoginPasswordEnabled

	/**
	 * setAutoLoginDomain : This method is set the domain in auto login
	 * @param domain - Sets the windows user domain
	 * @return None
	 * @throws Exception
	 */
	public void setAutoLoginDomain(String domain) throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Domain:"); //Web Element of Domain row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtDomain']")); //Text box web element of domain
			int snooze = 0;			

			while (snooze < 5 && !autoLoginElement.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", autoLoginElement);
				snooze++;
			}

			if (autoLoginElement.isEnabled()) {//Enters the name of the domain if it is enabled 
				autoLoginElement.clear();
				autoLoginElement.sendKeys(domain);
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAutoLoginDomain : "+e.getMessage(), e);
		} //End catch

	} //End setAutoLoginDomain

	/**
	 * getAutoLoginDomain : This method is to get the domain in auto login
	 * @param None
	 * @return Password
	 * @throws Exception
	 */
	public String getAutoLoginDomain() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Domain:"); //Web element of the domain row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtDomain']")); //Text box web element of the domain
			return (autoLoginElement.getText()); //Gets the text entered in the domain text box

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAutoLoginDomain : "+e.getMessage(), e);
		} //End catch

	} //End getAutoLoginDomain

	/**
	 * isAutoLoginDomainEnabled : This method is check the enabled status of domain text box
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public boolean isAutoLoginDomainEnabled() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Domain:"); //Web Element of Domain row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("input[id='txtDomain']")); //Text box web element of domain

			if (autoLoginElement.isEnabled())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isAutoLoginDomainEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isAutoLoginDomainEnabled

	/**
	 * setAutoLoginVault : This method is to select the vault in auto login option
	 * @param vault - Name of the vault
	 * @return None
	 * @throws Exception
	 */
	public void setAutoLoginVault(String vault) throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Document vault:"); //Web Element of vault row
			List<WebElement> vaultOptions = autoLoginElement.findElements(By.cssSelector("select[id='selectVaults']>option"));  //Options to select the vault
			Select select = new Select(autoLoginElement.findElement(By.cssSelector("select[id='selectVaults']")));
			
			int optionCount = vaultOptions.size(); //Number of available languages in the list
			int itemIdx =0;
			int snooze = 0;			

			while (snooze < 5 && !driver.findElement(By.cssSelector("select[id='selectVaults']")).isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", driver.findElement(By.cssSelector("select[id='selectVaults']")));
				snooze++;
			}

			for (itemIdx=0; itemIdx < optionCount; itemIdx++) //Loops to identify the web element of the vault
				if (vaultOptions.get(itemIdx).getText().toUpperCase().trim().equals(vault.toUpperCase()))
					break;

			if (itemIdx >= optionCount) //Checks for the existence of the vault to select
				throw new Exception("Document Vault (" + vault + ") does not exists in the list.");

			if (vaultOptions.get(itemIdx).isEnabled()) //Selects the vault if it is enabled
				if (browser.equalsIgnoreCase("edge"))
					driver.findElement(By.cssSelector("select[id='selectVaults']")).sendKeys(vault);
				else if (browser.equalsIgnoreCase("safari"))
					select.selectByIndex(itemIdx);
				else
					ActionEventUtils.click(driver, vaultOptions.get(itemIdx));
			else
				throw new Exception(vaultOptions.get(itemIdx).getText()+" is not enabled in the vault list");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAutoLoginVault : "+e.getMessage(), e);
		} //End catch

	} //End setAutoLoginVault

	/**
	 * getAutoLoginVault : This method is to get the selected vault in auto login
	 * @param None
	 * @return Vault Name
	 * @throws Exception
	 */
	public String getAutoLoginVault() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Document vault:"); //Web Element of document vault row
			autoLoginElement = autoLoginElement.findElement(By.cssSelector("select[id='selectVaults']")); //Select web element of the vault

			Select selectObj = new Select(autoLoginElement); //Instantiating Select class
			return(selectObj.getFirstSelectedOption().toString()); //Gets the selected option

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAutoLoginVault : "+e.getMessage(), e);
		} //End catch

	} //End getAutoLoginVault

	/**
	 * isAutoLoginVaultEnabled : This method is to check if auto login vault is enabled
	 * @param None
	 * @return true if vault field is enabled; false if not
	 * @throws Exception
	 */
	public boolean isAutoLoginVaultEnabled() throws Exception {

		try {

			WebElement autoLoginElement = this.getWebElement("Document vault:"); //Web Element of vault row
			WebElement vaultOption = autoLoginElement.findElement(By.cssSelector("select[id='selectVaults']"));  //Options to select the vault

			if (vaultOption.isEnabled())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isAutoLoginVaultEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isAutoLoginVaultEnabled

	/**
	 * setAllAutoLoginOptions : This method is set all the auto login options
	 * @param autoLoginOptions - Hashmap with keys IsAutoLogin-Yes or No; UserName-String; Password-String; Domain-String; Vault-String
	 * @return None
	 * @throws Exception
	 */
	public void setAllAutoLoginOptions(Map<String, String> autoLoginOptions) throws Exception {

		try {

			//Selects or unselects the Auto login checkbox
			if (autoLoginOptions.get("IsAutoLogin").toString().trim().toUpperCase().equals("YES"))
				this.setAutoLogin(true);
			else if (autoLoginOptions.get("IsAutoLogin").toString().toUpperCase().equals("NO"))
				this.setAutoLogin(false);

			if (this.getAutoLogin()) {
				this.setAutoLoginUserName(autoLoginOptions.get("UserName").toString()); //Sets Auto login user name
				this.setAutoLoginPassword(autoLoginOptions.get("Password").toString()); //Sets Auto login password
				this.setAutoLoginDomain(autoLoginOptions.get("Domain").toString()); //Sets Auto login Domain
				this.setAutoLoginVault(autoLoginOptions.get("Vault").toString()); //Sets Auto login vault
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setAllAutoLoginOptions : "+e.getMessage(), e);
		} //End catch

	} //End SetAllAutoLoginOptions

	/**
	 * setAllAutoLoginOptions : This method is to get all the values of the selected auto login options
	 * @param None
	 * @return Hashmap with keys IsAutoLogin-Yes or No; UserName-String; Password-String; Domain-String; Vault-String
	 * @throws Exception
	 */
	public Map<String, String> getAllAutoLoginOptions() throws Exception {

		//Variable Declaration
		Map<String, String> autoLoginOptions = new HashMap<String, String>();

		try {

			autoLoginOptions.put("IsAutoLogin",this.getAutoLogin().toString()); //Gets the status of autologin
			autoLoginOptions.put("UserName",this.getAutoLoginUserName()); //Gets the user name
			autoLoginOptions.put("Password",this.getAutoLoginPassword()); //Gets the password
			autoLoginOptions.put("Domain",this.getAutoLoginDomain()); //Gets the Domain
			autoLoginOptions.put("Vault",this.getAutoLoginVault()); //Gets the selected vault name

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getAllAutoLoginOptions : "+e.getMessage(), e);
		} //End catch

		return autoLoginOptions;

	} //End getAllAutoLoginOptions

	/**
	 * isGeneralSettingPageEmpty : This method is to check if General Settings Page is empty
	 * @param None
	 * @return true if page is empty; false if not
	 * @throws Exception
	 */
	public static Boolean isGeneralSettingPageEmpty(WebDriver driver) throws Exception {

		//Variable Declaration
		Boolean isEmpty = true;

		try {

			//Variable Declaration
			WebElement rightPanel; //To Store the check box web element of auto login

			rightPanel = driver.findElement(By.cssSelector("div[id='page']>div[id='rightPanel']>div[id='divConfig']"));

			if (rightPanel.findElement(By.cssSelector("div[class='configHeader']")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("configvault")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("configgeneral")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("configNavigation")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("configTaskPane")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("configControls")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

			if (rightPanel.findElement(By.id("actionControls")).isDisplayed()) //Returns true if auto-login is selected
				isEmpty = false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isGeneralSettingPageEmpty : "+e.getMessage(), e);
		} //End catch

		return isEmpty;

	} //End isGeneralSettingPageEmpty

	/*-----------------------------------Configuration Vault Settings page --------------------------------------------*/

	/**
	 * getVaultName: This method is to get the vault name in the configuration page
	 * @param None
	 * @return Name of the vault
	 * @throws Exception
	 */
	public String getVaultName() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement vaultNameRow = getWebElement(vaultTableRows, "Name:"); //Gets the row element of the vault
			return (vaultNameRow.findElement(By.cssSelector("td[class='fieldLabel']+td")).getText()); // Gets the text in the vaule name

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getVaultName : "+e.getMessage(), e);
		} //End catch

	} //End getVaultName

	/**
	 * getVaultUniqueID: This method is to get the unique ID of the vault
	 * @param None
	 * @return Unique id of the vault as string
	 * @throws Exception
	 */
	public String getVaultUniqueID() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement vaultIDRow = getWebElement(vaultTableRows, "Unique ID:"); //Gets the row element of the vault unique id
			return (vaultIDRow.findElement(By.cssSelector("td[class='fieldLabel']+td")).getText()); // Gets the text in the Vault unique id

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getVaultUniqueID : "+e.getMessage(), e);
		} //End catch

	} //End getVaultUniqueID

	/**
	 * setVaultAccess: This method is to select/un-select Allow vault access
	 * @param isAllow - true to allow access; false to disallow
	 * @return None
	 * @throws Exception
	 */
	public void setVaultAccess(Boolean isAllow) throws Exception {
		try {

			WebElement checkBoxElement = driver.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkVaultAccess']"));

			if (!checkBoxElement.isSelected() && isAllow) //Selects the allow access to this vault
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isAllow)  //Un-Selects the allow access to this vault
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setVaultAccess : "+e.getMessage(), e);
		} //End catch

	} //End setVaultAccess

	/**
	 * getVaultAccess: This method is to get the status of vault access check box
	 * @param None
	 * @return true if allowed; false if not
	 * @throws Exception
	 */
	public Boolean getVaultAccess() throws Exception {

		try {
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement = vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkVaultAccess']")); //Allow vault access check box element
			Utils.fluentWait(driver);
			return(checkBoxElement.isSelected()); //Gets the status of the check box element

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getVaultAccess : "+e.getMessage(), e);
		} //End catch

	} //End getVaultAccess

	/**
	 * setDefaultView: This method is to select the default home view of the web access
	 * @param defaultView - Default view to be selected
	 * @return None
	 * @throws Exception
	 */
	public void setDefaultView(String defaultView) throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement defaultViewRow = this.getWebElement(vaultTableRows, "Default view:"); //Web Element of default view row
			List<WebElement> defaultViewOptions = defaultViewRow.findElements(By.cssSelector("select[id='gotoItemSel']>optgroup>option")); //Web Element options of the default view list
			Select select = new Select(defaultViewRow.findElement(By.cssSelector("select[id='gotoItemSel']")));
			List<WebElement> options = select.getOptions();
			int optionCount = defaultViewOptions.size(); //Number of available default view options in the list
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<optionCount; itemIdx++) { //Loops to identify the web element of the language to be selected
				System.out.println(defaultViewOptions.get(itemIdx).getText());
				if (defaultViewOptions.get(itemIdx).getText().toUpperCase().trim().equals(defaultView.toUpperCase())) {
					Utils.fluentWait(driver);
					//defaultViewOptions.get(itemIdx).click(); //Selects the language from list
					if(browser.equalsIgnoreCase("safari"))
					{
						for (int i = 0;i < options.size(); i++)
							if(options.get(i).getText().trim().equalsIgnoreCase(defaultView))
							{
								select.selectByIndex(i);
								break;
							}				
					}
					else if(browser.equalsIgnoreCase("edge"))
						defaultViewRow.findElement(By.cssSelector("select[id='gotoItemSel']")).sendKeys(defaultView);
					else
						ActionEventUtils.click(driver, defaultViewOptions.get(itemIdx));
					//selectOption.sendKeys("defaultViewOptions.get(itemIdx).getText().trim");
					Utils.fluentWait(this.driver);
					return;
				}
			}
			if (itemIdx >= optionCount) {
				defaultViewOptions = defaultViewRow.findElements(By.cssSelector("select[id='gotoItemSel']>option")); //Web Element options of the default view list
				optionCount = defaultViewOptions.size(); //Number of available default view options in the list

				for (itemIdx=0; itemIdx<optionCount; itemIdx++)  //Loops to identify the web element of the default view to be selected
					if (defaultViewOptions.get(itemIdx).getText().toUpperCase().trim().contains("OTHER")) {
						//defaultViewOptions.get(itemIdx).click(); //Selects the default view from list
						if(browser.equalsIgnoreCase("edge"))
							defaultViewRow.findElement(By.cssSelector("select[id='gotoItemSel']")).sendKeys("Other [specify view ID]");
						else if(browser.equalsIgnoreCase("safari"))
							select.selectByValue("other");
						else
							ActionEventUtils.click(driver, defaultViewOptions.get(itemIdx));
						Utils.fluentWait(this.driver);
						break;
					}
			}

			if (itemIdx >= optionCount) //Throws exception if default view specified does not exists in the list
				throw new Exception("Default View Option (Other) does not exists in the list.");

			WebElement defaultViewIdTxtBox = defaultViewRow.findElement(By.cssSelector("td>div>input[id='txtDefaultViewID']"));

			if (defaultViewIdTxtBox.isDisplayed() && defaultViewIdTxtBox.isEnabled()) //Types the default view id
				defaultViewIdTxtBox.sendKeys(defaultView);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setDefaultView : "+e.getMessage(), e);
		} //End catch

	} //End setDefaultView


	/**
	 * isDefaultViewEnabled: This method is to check the default home view field is enabled or not
	 * @param defaultView - Default view to be selected
	 * @return True or false
	 * @throws Exception
	 */
	public boolean isDefaultViewEnabled() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement defaultViewRow = this.getWebElement(vaultTableRows, "Default view:"); //Web Element of default view row
			WebElement defaultViewField = defaultViewRow.findElement(By.cssSelector("select[id='gotoItemSel']")); //Web Element options of the default view list

			return defaultViewField.isEnabled();

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isDefaultViewEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isDefaultViewEnabled

	/**
	 * getDefaultView: To get the default home view of the web access
	 * @param None
	 * @return Default Home view
	 * @throws Exception
	 */
	public String getDefaultView() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement defaultViewRow = this.getWebElement(vaultTableRows, "Default view:"); //Web Element of default view row
			WebElement defaultViewSelect = defaultViewRow.findElement(By.cssSelector("select[id='gotoItemSel']")); //Web Element select of the default view list
			String defaultView;

			Select selectObj = new Select(defaultViewSelect); //Instantiating Select class
			defaultView = selectObj.getFirstSelectedOption().getText().toString(); //Gets the selected option

			WebElement defaultViewIdTxtBox = defaultViewRow.findElement(By.cssSelector("td>div>input[id='txtDefaultViewID']"));

			if (defaultViewIdTxtBox.isDisplayed() && defaultViewIdTxtBox.isEnabled()) //Gets the default view id
				defaultView = defaultViewIdTxtBox.getAttribute("value");

			return defaultView;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getDefaultView : "+e.getMessage(), e);
		} //End catch

	} //End GetDefaultView

	/**
	 * setLayout: This method is to select the layout of the web access
	 * @param layout - layout to select
	 * @return None
	 * @throws Exception
	 */
	public void setLayout(String layout) throws Exception {
		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement layoutRow = this.getWebElement(vaultTableRows, "Layout:"); //Web Element of default view row
			/*WebElement layoutDropdown = driver.findElement(By.cssSelector("select[id='ddlLayout']"));
            layoutDropdown.click();*/
			WebElement layoutSelect = layoutRow.findElement(By.cssSelector("select[id='ddlLayout']")); //Web Element options of the default view list


			/*int rowCnt = layoutSelect.size();
            int itemIndx = 0;


            for(itemIndx=0;itemIndx<rowCnt;itemIndx++) 
                  if (layoutSelect.get(itemIndx).getText().trim().equalsIgnoreCase(layout.trim())) {
                         Utils.fluentWait(driver);

                         ActionEventUtils.click(driver, layoutSelect.get(itemIndx));//layoutSelect.get(itemIndx).click();
                         break;
                  }

            if (itemIndx>=rowCnt)
                  throw new Exception ("Layout is does not exists in the list.");*/

			Select selectObj = new Select(layoutSelect); //Instantiating Select class
			selectObj.selectByVisibleText(layout); //Selects the layout from the list of options
			Utils.fluentWait(driver);


		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setLayout : "+e.getMessage(), e);
		} //End catch

	} //End SetLayout

	/**
	 * setLayout: This method is to select the layout of the web access
	 * @param layout - layout to select
	 * @return None
	 * @throws Exception
	 */
	public void setJavaApplet(String status) throws Exception {
		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement javaApplet = this.getWebElement(vaultTableRows, "Java applet:"); //Web Element of default view row

			switch(status.toUpperCase().trim()) {
			case "ENABLE" : {
				WebElement enableBtn = javaApplet.findElement(By.cssSelector("input[id='enableApplet']")); //Web Element radio button of Enable
				//enableBtn.click();
				ActionEventUtils.click(driver, enableBtn);
				break;
			}
			case "DISABLE" : {
				WebElement disableBtn = javaApplet.findElement(By.cssSelector("input[id='disableApplet']")); //Web Element radio button of Disable
				//disableBtn.click();
				ActionEventUtils.click(driver, disableBtn);
				break;
			}
			}//End switch

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setJavaApplet : "+e.getMessage(), e);
		} //End catch

	} //End SetLayout

	public boolean isJavaAppletEnabled() throws Exception {
		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement javaApplet = this.getWebElement(vaultTableRows, "Java applet:"); //Web Element of default view row
			WebElement javaEnable = javaApplet.findElement(By.cssSelector("input[id='enableApplet']"));

			if(javaEnable.isSelected())//Verify if java applet is enabled or not
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isJavaAppletEnabled : "+e.getMessage(), e);
		} //End catch

	} //End SetLayout





	/**
	 * getLayout: This method is to get the layout selected from the web access
	 * @param None
	 * @return Selected layout
	 * @throws Exception
	 */
	public String getLayout() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement layoutRow = this.getWebElement(vaultTableRows, "Layout:"); //Web Element of default view row
			WebElement layoutSelect = layoutRow.findElement(By.cssSelector("select[id='ddlLayout']")); //Web Element options of the default view list
			Select selectObj = new Select(layoutSelect); //Instantiating Select class
			return (selectObj.getFirstSelectedOption().getText().toString().trim()); //Selects the layout from the list of options

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getLayout : "+e.getMessage(), e);
		} //End catch

	} //End getLayout

	/**
	 * getallLayouts: This method is to get all the layouts from the configuration page layout field
	 * @param None
	 * @return 
	 * @return Layouts list
	 * @throws Exception
	 */
	public String[] getallLayouts() throws Exception {

		try {
			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement layoutRow = this.getWebElement(vaultTableRows, "Layout:"); //Web Element of default view row
			WebElement layoutSelect = layoutRow.findElement(By.cssSelector("select[id='ddlLayout']")); //Web Element options of the default view list
			//layoutSelect.click();
			ActionEventUtils.click(driver, layoutSelect);
			Select selectObj = new Select(layoutSelect);//Instantiating Select class
			selectObj.getOptions().get(0).getText();
			String[] layouts = new String[selectObj.getOptions().size()];	
			for(int i=0; i< selectObj.getOptions().size();i++){
				layouts[i] = selectObj.getOptions().get(i).getText().trim();
				System.out.println(layouts[i]);
			}



			return layouts; //Selects the layout from the list of options

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getallLayouts : "+e.getMessage(), e);
		} //End catch

	} //End getLayout

	/**
	 * setPreventNavigation: This method is to select/un-select prevent navigation outside the default view
	 * @param isPrevent - true to select and false to un-select check box
	 * @return None
	 * @throws Exception
	 */
	public void setPreventNavigation(Boolean isPrevent) throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement = vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkPreventDefaultView']"));

			if (!checkBoxElement.isSelected() && isPrevent) //Selects to prevent navigation outside default view
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isPrevent)  //Un-Selects to prevent navigation outside default view
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setPreventNavigation : "+e.getMessage(), e);
		} //End catch

	} //End setPreventNavigation

	/**
	 * getPreventNavigation: This method is to get the status of prevent navigation check box
	 * @param None
	 * @return true if selected; false if not
	 * @throws Exception
	 */
	public Boolean getPreventNavigation() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement = vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkPreventDefaultView']")); //Allow vault access check box element
			return (checkBoxElement.isSelected()); //Gets the status of the check box element

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getPreventNavigation : "+e.getMessage(), e);
		} //End catch

	} //End GetPreventNavigation

	/**
	 * isPreventNavigationEnabled: This method is check if prevent navigation outside view check box is in enabled state
	 * @param None
	 * @return true if enabled; false if not
	 * @throws Exception
	 */
	public boolean isPreventNavigationEnabled() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement = vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkPreventDefaultView']"));

			if (checkBoxElement.isEnabled()) //Selects to prevent navigation outside default view
				return true;
			else 
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isPreventNavigationEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isPreventNavigationEnabled

	/**
	 * setRetainLatestSearchCriteria: This method is to select/un-select retain latest search criteria
	 * @param isRetain - true to select and false to un-select check box
	 * @return None
	 * @throws Exception
	 */
	public void setRetainLatestSearchCriteria(Boolean isRetain) throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement =vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkRetainDefObject']"));

			if (!checkBoxElement.isSelected() && isRetain) //Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isRetain)  //Un-Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setRetainLatestSearchCriteria : "+e.getMessage(), e);
		} //End catch

	} //End setRetainLatestSearchCriteria

	/**
	 * getRetainLatestSearchCriteria: This method is to get the status of latest search criteria
	 * @param None
	 * @return true if selected; false if not
	 * @throws Exception
	 */
	public Boolean getRetainLatestSearchCriteria() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement checkBoxElement =vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkRetainDefObject']"));
			return (checkBoxElement.isSelected()); //Gets the status of the check box element

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getRetainLatestSearchCriteria : "+e.getMessage(), e);
		} //End catch

	} //End getRetainLatestSearchCriteria

	public void setFoceFollowingSelection(Boolean isRetain) throws Exception {

		/* --------------------------------------------------------------------
		 * Function Name	: SetRetainLatestSearchCriteria
		 * Description		: This method is to select/un-select retain latest search criteria
		 * @Param					: Boolean value true to select and false to un-select check box
		 * Output					: None
		 -----------------------------------------------------------------------*/

		try {

			//Variable Declaration
			WebElement checkBoxElement; //To Store the check box web element
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			checkBoxElement =vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkForceObjectCriteria']"));

			if (!checkBoxElement.isSelected() && isRetain) //Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isRetain)  //Un-Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setFoceFollowingSelection : "+e.getMessage(), e);
		} //End catch

	} //End SetRetainLatestSearchCriteria

	/**
	 * setDefaultSearchCriteria : Sets default searc criteria
	 * @param searchCriteria
	 * @throws Exception
	 */
	public void setDefaultSearchCriteria(String searchCriteria) throws Exception {

		/* --------------------------------------------------------------------
		 * Function Name	: SetDefaultSearchCriteria
		 * Description		: This method is to select default search criteria
		 * @Param					: String Search Criteria to select from the list
		 * Output					: None
	 -----------------------------------------------------------------------*/

		try {

			//Variable Declaration
			WebElement searchCriteriaSelect; //To store web element of select combo-box
			Select selectObj; //To Store the object of Select Class
			this.setRetainLatestSearchCriteria(false);
			this.setFoceFollowingSelection(true); //Un-Select Retain latest search settings check-box
			Utils.fluentWait(driver);
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			searchCriteriaSelect = vaultTable.findElement(By.cssSelector("select[id='ddlDefaultObjectCriteria']"));

			if (!searchCriteriaSelect.isDisplayed() || !searchCriteriaSelect.isEnabled())
				throw new Exception("Select Default Search Criteria is not enabled.");

			selectObj = new Select(searchCriteriaSelect); //Instantiating Select class
			selectObj.selectByVisibleText(searchCriteria.trim()); //Selects the searchCriteria from the list of options

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setDefaultSearchCriteria : "+e.getMessage(), e);
		} //End catch

	} //End SetDefaultSearchCriteria

	/**
	 * getDefaultSearchCriteria: This method is to get the search criteria selected from the web access
	 * @param None
	 * @return searchCriteria selected
	 * @throws Exception
	 */
	public String getDefaultSearchCriteria() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement searchCriteriaSelect =vaultTable.findElement(By.cssSelector("select[id='ddlDefaultObjectCriteria']")); //Web Element options of the search layout

			Select selectObj = new Select(searchCriteriaSelect); //Instantiating Select class
			return (selectObj.getFirstSelectedOption().getText().trim()); //Selects the layout from the list of options
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getDefaultSearchCriteria : "+e.getMessage(), e);
		} //End catch

	} //End getDefaultSearchCriteria

	/**
	 * setDefaultSearchCriteria: This method is to select default search criteria
	 * @param None
	 * @return true if enabled; false if not
	 * @throws Exception
	 */
	public boolean isDefaultSearchCriteriaEnabled() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement searchCriteriaSelect = vaultTable.findElement(By.cssSelector("select[id='ddlDefaultObjectCriteria']"));

			if (searchCriteriaSelect.isEnabled())
				return true;
			else
				return false;			

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.isDefaultSearchCriteriaEnabled : "+e.getMessage(), e);
		} //End catch

	} //End isDefaultSearchCriteriaEnabled

	/**
	 * setRetainLatestSearchSettings: This method is to select/un-select retain latest search settings
	 * @param isRetain - true to retain selection; false if not
	 * @return None
	 * @throws Exception
	 */
	public void setRetainLatestSearchSettings(Boolean isRetain) throws Exception {

		try {

			WebElement checkBoxElement = this.driver.findElement(By.id("chkRetainDefSearch"));	
			if (!checkBoxElement.isSelected() && isRetain) //Selects to retain latest search settings
			{	ActionEventUtils.click(driver, checkBoxElement);
			//checkBoxElement.click();
			Utils.fluentWait(driver);   
			}
			else if (checkBoxElement.isSelected() && !isRetain)  //Un-Selects to retain latest search settings
			{
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
				Utils.fluentWait(driver);   
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setRetainLatestSearchSettings : "+e.getMessage(), e);
		} //End catch

	} //End SetRetainLatestSearchSettings

	/**
	 * setRetainLatestSearchSettings: This method is to select/un-select retain latest search settings
	 * @param isRetain - true to retain selection; false if not
	 * @return None
	 * @throws Exception
	 */
	public void setRetainLatestSearchSettingsInSearchCriteria(Boolean isRetain) throws Exception {

		try {

			WebElement checkBoxElement = this.driver.findElement(By.id("chkRetainDefObject"));	
			if (!checkBoxElement.isSelected() && isRetain) //Selects to retain latest search settings
			{	//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
				Utils.fluentWait(driver);   
			}
			else if (checkBoxElement.isSelected() && !isRetain)  //Un-Selects to retain latest search settings
			{
				ActionEventUtils.click(driver, checkBoxElement);
				//checkBoxElement.click();
				Utils.fluentWait(driver);   
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setRetainLatestSearchSettingsInSearchCriteria : "+e.getMessage(), e);
		} //End catch

	} //End SetRetainLatestSearchSettings

	/**
	 * getRetainLatestSearchSettings: This method is to get the status of retain latest search settings
	 * @param None
	 * @return true if selected; false if not
	 * @throws Exception
	 */
	public Boolean getRetainLatestSearchSettings() throws Exception {

		try {

			//			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			//			WebElement checkBoxElement = vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkRetainDefSearch']"));
			WebElement checkBoxElement = this.driver.findElement(By.id("chkRetainDefSearch"));
			return(checkBoxElement.isEnabled());
			// return(checkBoxElement.isSelected()); //Gets the status of the check box element

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getRetainLatestSearchSettings : "+e.getMessage(), e);
		} //End catch

	} //End GetRetainLatestSearchSettings

	/**
	 * setDefaultSearchSettings: This method is to select default search settings
	 * @param searchSettings - Search settings to select from the list
	 * @return None
	 * @throws Exception
	 */
	public void setDefaultSearchSettings(String searchSettings) throws Exception {

		try {

			this.setRetainLatestSearchSettings(false); //Un-Select Retain latest search settings check-box
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement searchSettingsSelect = vaultTable.findElement(By.cssSelector("select[id='ddlSearchSetting']"));

			if (!searchSettingsSelect.isDisplayed() || !searchSettingsSelect.isEnabled())
				throw new Exception("Select Default Search settings is not enabled.");

			Select selectObj = new Select(searchSettingsSelect); //Instantiating Select class
			selectObj.selectByVisibleText(searchSettings.trim()); //Selects the searchSettings from the list of options

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setDefaultSearchSettings : "+e.getMessage(), e);
		} //End catch

	} //End setDefaultSearchSettings

	/**
	 * getDefaultSearchSettings: This method is to get the search settings selected
	 * @param None
	 * @return Default search setting selected
	 * @throws Exception
	 */
	public String getDefaultSearchSettings() throws Exception {

		try {

			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement searchSettingsSelect =vaultTable.findElement(By.cssSelector("select[id='ddlSearchSetting']")); //Web Element options of the search layout

			Select selectObj = new Select(searchSettingsSelect); //Instantiating Select class
			return (selectObj.getFirstSelectedOption().toString()); //Selects the searchSettings from the list of options

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getDefaultSearchSettings : "+e.getMessage(), e);
		} //End catch

	} //End getDefaultSearchSettings

	/**
	 * setTopMenu: This method is to select show or hide top menu
	 * @param status - Show or Hide
	 * @return None
	 * @throws Exception
	 */
	public void setTopMenu(String status) throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement topMenuRow = this.getWebElement(vaultTableRows, "Top menu:"); //Web Element of breadcrump row

			switch(status.toUpperCase().trim()) {
			case "SHOW" : {
				WebElement rBtnShowHide = topMenuRow.findElement(By.cssSelector("input[id='topMenuShow']")); //Web Element radio button of Show
				//rBtnShowHide.click();
				ActionEventUtils.click(driver, rBtnShowHide);
				break;
			}
			case "HIDE" : {
				WebElement rBtnShowHide = topMenuRow.findElement(By.cssSelector("input[id='topMenuHide']")); //Web Element radio button of Show
				//rBtnShowHide.click();
				ActionEventUtils.click(driver, rBtnShowHide);
				break;
			}
			default : throw new Exception("Item (" + status + ") does not exists.");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setTopMenu : "+e.getMessage(), e);
		} //End catch

	} //End SetTopMenu

	/**
	 * getTopMenu: This method is to return the status of top menu
	 * @param None
	 * @return status - Show or Hide
	 * @throws Exception
	 */
	public String getTopMenu() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement topMenuRow = this.getWebElement(vaultTableRows, "Top menu:"); //Web Element of breadcrump row
			WebElement rBtnShowHide = topMenuRow.findElement(By.cssSelector("input[id='topMenuShow']")); //Web Element radio button of Show

			if (rBtnShowHide.isSelected())
				return ("Show");
			else
				return ("Hide");	

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getTopMenu : "+e.getMessage(), e);
		} //End catch

	} //End  getTopMenu

	/**
	 * setBreadCrumb: This method is to select show or hide breadcrumb
	 * @param status - Show or Hide
	 * @return None
	 * @throws Exception
	 */
	public void setBreadCrumb(String status) throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement breadcrumbRow = this.getWebElement(vaultTableRows, "Breadcrumb:"); //Web Element of default view row

			switch(status.toUpperCase().trim()) {
			case "SHOW" : {
				WebElement rBtnShowHide = breadcrumbRow.findElement(By.cssSelector("input[id='breadCrumbShow']")); //Web Element radio button of Show
				//rBtnShowHide.click();
				ActionEventUtils.click(driver, rBtnShowHide);
				break;
			}
			case "HIDE" : {
				WebElement rBtnShowHide = breadcrumbRow.findElement(By.cssSelector("input[id='breadCrumbHide']")); //Web Element radio button of Show
				//rBtnShowHide.click();
				ActionEventUtils.click(driver, rBtnShowHide);
				break;
			}
			default : throw new Exception("Item (" + status + ") does not exists.");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setBreadCrumb : "+e.getMessage(), e);
		} //End catch

	} //End setBreadCrumb

	/**
	 * getBreadCrumb: This method is to return the status of breadcrumb
	 * @param None
	 * @return status - Show or Hide
	 * @throws Exception
	 */
	public String getBreadCrumb() throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			WebElement topMenuRow = this.getWebElement(vaultTableRows, "Breadcrumb:"); //Web Element of breadcrump row
			WebElement rBtnShowHide = topMenuRow.findElement(By.cssSelector("input[id='breadCrumbShow']")); //Web Element radio button of Show

			if (rBtnShowHide.isSelected())
				return("Show");
			else
				return("Hide");	

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getBreadCrumb : "+e.getMessage(), e);
		} //End catch

	} //End GetBreadCrumb

	/**
	 * setVaultCommands: This method is to select show or hide for the specified control
	 * @param commandName - Name of the vault command (Eg : Check Out)
	 * @param status - Show or Hide
	 * @return None
	 * @throws Exception
	 */
	public void setVaultCommands(String commandName, String status) throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("div[id*='config'][style*='display: block;']>table[class='fullLength']"));
			WebElement controlsRow = getWebElement(vaultTableRows, commandName + ":"); //Web Element of default view row			
			System.out.println(controlsRow.getText());
			List<WebElement> commandData = controlsRow.findElements(By.cssSelector("td")); //Finds web elements with td under the row
			int tdCount = commandData.size(); //Gets the size of the number of elements
			int itemIdx = 0;
			String tdClass = "";

			for (itemIdx=0; itemIdx<tdCount; itemIdx++) //Loops to get the class name of the required control
				if (commandData.get(itemIdx).getText().toUpperCase().contains(commandName.toUpperCase())) {
					tdClass = commandData.get(itemIdx).getAttribute("class");
					break;
				}

			switch(status.toUpperCase().trim()) { //Selects Show or Hide based on the user parameter
			case "SHOW" : {
				WebElement rBtnShowHide = controlsRow.findElement(By.cssSelector("td[class='" + tdClass + "']+td>span>input[value='true']")); //Web Element radio button of Show
				ActionEventUtils.click(driver, rBtnShowHide);
				//	rBtnShowHide.click();
				break;
			}
			case "HIDE" : {
				WebElement rBtnShowHide = controlsRow.findElement(By.cssSelector("td[class='" + tdClass + "']+td>span>input[value='false']")); //Web Element radio button of Show
				ActionEventUtils.click(driver, rBtnShowHide);
				//	rBtnShowHide.click();
				break;
			}
			default : throw new Exception("Item (" + status + ") does not exists.");
			}

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setVaultCommands : "+e.getMessage(), e);
		} //End catch

	} //End setVaultCommands

	/**
	 * setBreadCrumb: This method is to return the status of specified command
	 * @param commandName - Name of the vault command (Eg : Check Out)
	 * @return Show or Hide
	 * @throws Exception
	 */
	public String getVaultCommands(String commandName) throws Exception {

		try {

			List<WebElement> vaultTableRows=driver.findElements(By.cssSelector("table[class='fullLength']"));
			WebElement controlsRow = getWebElement(vaultTableRows, commandName + ":"); //Web Element of default view row			
			List<WebElement> commandData = controlsRow.findElements(By.cssSelector("td")); //Finds web elements with td under the row
			int tdCount = commandData.size(); //Gets the size of the number of elements
			int itemIdx = 0;
			String tdClass = "";

			for (itemIdx=0; itemIdx<tdCount; itemIdx++) {//Loops to get the class name of the required control
				if (commandData.get(itemIdx).getText().toUpperCase().contains(commandName.toUpperCase())) {
					//	System.out.println("settingsname :"+commandData.get(itemIdx).getText().toUpperCase());
					tdClass = commandData.get(itemIdx).getAttribute("class");
					break;
				}
			}

			WebElement rBtnShowHide = controlsRow.findElement(By.cssSelector("td[class='" + tdClass + "']+td>span>input[value='true']")); //Web Element radio button of Show
			String selectedStatus = "";

			if (rBtnShowHide.isSelected()) //Returns Show if Show is selected if not returns Hide
				selectedStatus = "Show";
			else 
				selectedStatus = "Hide";

			if (commandName.equalsIgnoreCase("Save column settings") && selectedStatus.equalsIgnoreCase("Show"))
				selectedStatus = "Allow";
			else if (commandName.equalsIgnoreCase("Save column settings") && selectedStatus.equalsIgnoreCase("Hide"))
				selectedStatus = "Disallow";

			return selectedStatus;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getVaultCommands : "+e.getMessage(), e);
		} //End catch

	} //End getVaultCommands

	/**
	 * getWebElement : This method is to get the parent web element when label is passed
	 * @param labelName Name of the lable to be selected from table
	 * @return WebElement of the label passed
	 * @throws Exception 
	 */	
	public WebElement getWebElement(String labelName) throws Exception {

		try {

			List<WebElement> configTable = this.driver.findElements(By.cssSelector("div[class='fieldSet']>div[style='display: block;']>table[class='fullLength']>tbody>tr")); //Stores all the web element that contains tr tag
			int noOfElmnts = configTable.size(); //Size of the row elements
			WebElement element;
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  {//Loops to identify the instance of the item to be clicked
				List<WebElement> tableRows = configTable.get(itemIdx).findElements(By.cssSelector("td")); //Element with the passed label
				int noOfRows = tableRows.size();
				int rowIdx = 0;
				for (rowIdx=0; rowIdx<noOfRows; rowIdx++) {
					element = tableRows.get(rowIdx);
					if (element.getText().toUpperCase().trim().equals(labelName.toUpperCase())) {//Returns the matching element
						element = element.findElement(By.xpath(".."));
						return element;
					}
				}
			}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item
				throw new Exception("Item (" + labelName + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getWebElement : "+e.getMessage(), e);
		} //End catch

		return null;

	} //End getWebElement

	/**
	 * getWebElement : This method is to get the parent web element when label is passed
	 * @param generalTable Rows and columns of a table web element
	 * @param labelName Name of the lable to be selected from table
	 * @return WebElement of the label passed
	 * @throws Exception 
	 */	
	public WebElement getWebElement(List<WebElement> table, String labelName) throws Exception {

		//Variable Declaration
		WebElement element = null; //Stores return value

		try {

			int noOfElmnts = table.size(); //Size of the row elements
			int itemIdx = 0;

			for (itemIdx=0; itemIdx<noOfElmnts; itemIdx++)  {//Loops to identify the instance of the item to be clicked
				List<WebElement> tableRows = table.get(itemIdx).findElements(By.cssSelector("td")); //Element with the passed label
				int noOfRows = tableRows.size();
				int rowIdx = 0;

				for (rowIdx=0; rowIdx<noOfRows; rowIdx++) {
					element = tableRows.get(rowIdx);
					System.out.println(element.getText());
					if (element.getText().toUpperCase().trim().equals(labelName.toUpperCase())) {//Returns the matching element
						element = element.findElement(By.xpath(".."));
						return element;
					}
				}
			}

			if (itemIdx >= noOfElmnts) //Checks for the existence of the item
				throw new Exception("Item (" + labelName + ") does not exists in the list.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getWebElement : "+e.getMessage(), e);
		} //End catch

		return element;

	} //End getWebElement

	/**
	 * Description: This method is to select/un-select retain latest search criteria
	 * @param isRetain
	 * @return
	 * @throws Exception
	 */
	public Boolean getForceFollowingSelection() throws Exception {

		try {

			//Variable Declaration
			WebElement checkBoxElement; //To Store the check box web element
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			checkBoxElement =vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkForceObjectCriteria']"));
			return (checkBoxElement.isSelected()); //Gets the status of the check box element
		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.getForceFollowingSelection : "+e.getMessage(), e);
		} //End catch

	} //End getForceFollowingSelection

	public void setForceFollowingSelection(Boolean isRetain) throws Exception {

		/* --------------------------------------------------------------------
		 * Function Name	: SetRetainLatestSearchCriteria
		 * Description		: This method is to select/un-select retain latest search criteria
		 * @Param					: Boolean value true to select and false to un-select check box
		 * Output					: None
		 -----------------------------------------------------------------------*/

		try {

			//Variable Declaration
			WebElement checkBoxElement; //To Store the check box web element
			WebElement vaultTable=driver.findElement(By.cssSelector("div[id='configvault'][style*='display: block;']>table[class='fullLength']"));
			checkBoxElement =vaultTable.findElement(By.cssSelector("tbody>tr>td>span[class='fieldValue']>input[id='chkForceObjectCriteria']"));

			if (!checkBoxElement.isSelected() && isRetain) //Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);
			else if (checkBoxElement.isSelected() && !isRetain)  //Un-Selects to retain latest search criteria
				//checkBoxElement.click();
				ActionEventUtils.click(driver, checkBoxElement);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setForceFollowingSelection : "+e.getMessage(), e);
		} //End catch
	}


	/**
	 * setValueSearchmaximumResult: This method is to set value in maximum search result option
	 * @param isAllow - true to allow access; false to disallow
	 * @return None
	 * @throws Exception
	 */
	public void setValueSearchmaximumResult(String maximum) throws Exception {
		try {

			WebElement maximumsearch = driver.findElement(By.cssSelector("div[id='divConfig']")).findElement(By.id("searchResultMaxLimit"));
			//maximumsearch.click();
			ActionEventUtils.click(driver, maximumsearch);
			maximumsearch.clear();
			maximumsearch.sendKeys(maximum);		

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.setValueSearchmaximumResult : "+e.getMessage(), e);
		} //End catch

	} //End setValueSearchmaximumResult


	/**
	 * resetVaultCommands: This method is to reset vault commands to the initial state
	 * @param CommandName - Name of the vault command (Eg : Check Out)
	 * @param prevStatus - Show or Hide
	 * @param testVault - Vault Name
	 * @return None
	 * @throws Exception
	 */
	public void resetVaultCommands(String CommandName, String prevStatus, String testVault) throws Exception {
		try{

			if(!Utility.logOut(driver))
				throw new Exception("Error while logging out from the default page.");
			Utils.fluentWait(driver);
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>" + Caption.ConfigSettings.Config_TaskArea.Value);
			configurationPage.configurationPanel.setVaultCommands(CommandName, prevStatus);
			Utils.fluentWait(driver);
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Error while saving the settings at finally.");
		}
		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.resetVaultCommands : "+e.getMessage(), e);
		} //End catch

	} //End resetVaultCommands

	/**
	 * resetLayout: This method is to reset layout to the initial state
	 * @param CommandName - Name of the vault command (Eg : Check Out)
	 * @param prevStatus - Show or Hide
	 * @param testVault - Vault Name
	 * @return None
	 * @throws Exception
	 */
	public void resetLayout(String prevLayout, String testVault) throws Exception {
		try{

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			configurationPage.treeView.clickTreeViewItem("Vault-specific settings>>" + testVault); // Clicks Vault item
			Utils.fluentWait(driver);
			configurationPage.configurationPanel.setLayout(prevLayout);
			Utils.fluentWait(driver);
			if (!configurationPage.configurationPanel.saveSettings())
				throw new Exception("Error while saving the settings at finally.");
		}
		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPanel.resetLayout : "+e.getMessage(), e);
		} //End catch

	} //End resetVaultCommands

} //End Class ConfigurationPanel

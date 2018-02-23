package MFClient.Pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

import MFClient.Wrappers.ConfigurationPanel;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.TreeView;

public class ConfigurationPage extends LoadableComponent <ConfigurationPage>{

	private boolean pageLoaded = false;
	private final WebDriver driver;
	public ConfigurationPanel configurationPanel;
	public TreeView treeView;
	XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public String browser = xmlParameters.getParameter("driverType");

	static String taskPaneItems="configTaskPane";
	static String configControls ="configControls";

	/********************************************************************
	 *	Page Factory Elements -LoginPage
	 **********************************************************************/

	@FindBy(how=How.CSS,using="list[id='general0'][class*='closed']>ins")
	private WebElement expandVaultSpecificSettingsTree;

	@FindBy(how=How.CSS,using="list[id='general0']>a")
	private WebElement vaultSpecificSettingsFolder;

	@FindBy(how=How.CSS,using="list[id='_root'][class*='closed']>ins")
	private WebElement expandGeneralSettingsTree;

	@FindBy(how=How.CSS,using="li[id='_root']>a")
	private WebElement generalSettingsFolder;

	@FindBy(how=How.CSS,using="li[name='My Vault'][class*='closed']>ins")
	private WebElement expandMyVaultFolder;

	@FindBy(how=How.CSS,using="li[name='My Vault']>a")
	private WebElement myVaultFolder;

	@FindBy(how=How.CSS,using="li[name='Sample Vault'][class*='closed']>ins")
	private WebElement expandSampleVaultFolder;

	@FindBy(how=How.CSS,using="li[name='Sample Vault']>a")
	private WebElement sampleVaultFolder;

	@FindBy(how=How.CSS,using="li[valueid='TaskPane'][parent='Sample Vault']>a")
	private WebElement sampleVaultTaskArea;

	@FindBy(how=How.CSS,using="div[id='configTaskPane']:not([style*='display: none;'])")
	private WebElement taskPaneSettings;

	@FindBy(how=How.CSS,using="div[id='configControls']:not([style*='display: none;'])")
	private WebElement controlSettings;

	@FindBy(how=How.CSS,using="form[class='loginForm']")
	private WebElement loginForm;

	/***Page Factory Methods
	 * @throws Exception ***/
	public ConfigurationPage(final WebDriver driver) throws Exception {

		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 2);
		PageFactory.initElements(finder, this);
		configurationPanel = new ConfigurationPanel(this.driver);
		treeView = new TreeView(this.driver);
	}

	final protected void isLoaded()
	{
		if (!(driver.getCurrentUrl().toLowerCase().contains("/Configuration.aspx"))) {

			if (!pageLoaded)
				Assert.fail();

			try {
				throw new Exception("Exception at ConfigurationPage.isLoaded : Expected page was a WebAccess Configuration page, but current page is not a Configuration page." + "Current Page is: " + driver.getCurrentUrl());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Verify whether is Configuration page
		}
	}

	final protected void load()
	{
		try {
			Utils.waitForPageLoad(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pageLoaded = true;
	}

	/**
	 * isLogOutExists : This method checks if log out link exists
	 * @param None
	 * @return true if log out link exists; false if not
	 * @throws Exception 
	 */		
	public Boolean isLogOutExists() throws Exception {

		try {

			WebElement logOutBtn = driver.findElement(By.cssSelector("div[class='logout']>div[class='logOutText']")); //Information of the page slected

			if (logOutBtn.isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException"))				
				return false;
			else
				throw new Exception("Exception at ConfigurationPage.isLogOutExists : "+e.getMessage(), e);
		} //End catch

		return false;

	} //End LogOut

	/**
	 * logOut : This method clicks log out button
	 * @param None
	 * @return true if log out is successful; false if not
	 * @throws Exception 
	 */	
	public boolean logOut() throws Exception {

		try {

			WebElement logOutBtn = driver.findElement(By.cssSelector("div[class='logout']>div[class='logOutText']")); //Information of the page slected
			ActionEventUtils.click(driver, logOutBtn);
			//logOutBtn.click(); //Clicks Logout button
			Utils.fluentWait(this.driver);

			String currentUrl = this.driver.getCurrentUrl();

			if (!currentUrl.toUpperCase().contains("CONFIGURATION") && currentUrl.toUpperCase().contains("LOGOUT=TRUE")) //Checks if 
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPage.logOut : "+e.getMessage(), e);
		} //End catch

	} //End logOut


	/*------------------------------Function required by Smoke/Configuration UI--------------------------------------*/

	/**
	 * <br>Description : Verify if SettingsTree displayed</br>
	 * @param driver
	 * @return
	 * @throws Exception 
	 */
	public boolean isSettingsTreeDisplayed() throws Exception
	{
		final long startTime = StopWatch.startTime();
		try
		{
			WebElement rootSettingsTree=driver.findElement(By.cssSelector("div[id='tree']"));
			if(rootSettingsTree.isDisplayed()) {
				Log.event("Settings Tree displayed.", StopWatch.elapsedTime(startTime));
				return true;
			}
		}
		catch(NoSuchElementException e)
		{
			throw new Exception("Exception at ConfigurationPage.isSettingsTreeDisplayed : Settings Tree view not displayed : "+e.getMessage(), e);
		}
		return false;
	}

	/**
	 * <br>Description: Expand the Document Vault</br>
	 * @param vaultName
	 * @throws Exception 
	 */
	public void expandVaultFolder(String vaultName) throws Exception
	{
		try {

			Utils.fluentWait(driver);

			String className = "jstree-node  jstree-closed";

			Boolean isClosed = getFolderElement(vaultName).findElement(By.xpath("..")).getAttribute("Class").toUpperCase().contains(className.toUpperCase());

			if (isClosed)
			{
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", getFolderElement(vaultName.trim()).findElement(By.xpath("..")).findElement(By.cssSelector("i[class*='jstree-icon']")));
				else
					ActionEventUtils.click(driver, getFolderElement(vaultName.trim()).findElement(By.xpath("..")).findElement(By.cssSelector("i[class*='jstree-icon']")));
				Log.event(vaultName+" Folder expanded.");
			}
			else
				Log.event(vaultName+" already in expanded state.");

			/*switch(vaultName.trim()){
			case "My Vault":
				expandMyVaultFolder.click();
				break;
			case "Sample Vault":
				expandSampleVaultFolder.click();
				break;
			}*/


		}
		catch(Exception e)
		{

			throw new Exception("Exception at Configurationpage.expandVaultFolder : "+e.getMessage(), e);

			/*if(getFolderElement(vaultName).findElement(By.xpath("..")).findElement(By.className("jstree-node  jstree-closed")).isDisplayed() && e.getClass().toString().contains("NoSuchElementException")) {
				Log.event(vaultName+" already Opened");
			}
			else {
				throw new NoSuchElementException("Unable to expand "+vaultName+" tree");
			}*/
		}
	}

	/**
	 * 
	 * @param settingName
	 * @throws Exception 
	 */
	public void expandSettingsTree(String settingName) throws Exception
	{
		try {
			if(settingName.trim().equalsIgnoreCase("Vault-specific settings")){
				//expandVaultSpecificSettingsTree.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", expandVaultSpecificSettingsTree);
				else
					ActionEventUtils.click(driver, expandVaultSpecificSettingsTree);
			}
			else if(settingName.trim().equalsIgnoreCase("General settings"))
				//expandGeneralSettingsTree.click();
				ActionEventUtils.click(driver, expandGeneralSettingsTree);
			Log.event(settingName+" Folder expanded.");
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.expandSettingsTree : Unable to expand "+settingName+" tree : "+e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param settingName
	 * @throws Exception
	 */
	public void clickSettingsFolder(String settingName) throws Exception
	{
		try {

			Utils.fluentWait(this.driver);
			int snooze = 0;

			if(settingName.trim().equalsIgnoreCase("Vault-specific settings")){
				//vaultSpecificSettingsFolder.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", getFolderElement("Vault-specific settings"));
				else
					ActionEventUtils.click(driver, getFolderElement("Vault-specific settings"));
			}
			else if(settingName.trim().equalsIgnoreCase("General settings")){
				//generalSettingsFolder.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", getFolderElement("General settings"));
				else
					ActionEventUtils.click(driver, getFolderElement("General settings"));
			}
			else if(settingName.trim().equalsIgnoreCase("General")){
				WebElement generalFolder=driver.findElement(By.cssSelector("div[id='tree']>ul>li[id='_root']>ul>li[id='_root_general']>a"));
				//generalFolder.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", generalFolder);
				else
					ActionEventUtils.click(driver, generalFolder);
				while(configurationPanel.getPageTitle().equals("") && snooze < 60)
				{
					Thread.sleep(500);
					snooze++;
					Log.event("ConfigurationPage.clickSettingsFolder : Waiting for the Configuration settings loading in the configuration webpage..");
				}
			}
			else if(settingName.trim().equalsIgnoreCase("TaskPane")) {
				WebElement taskPaneFolder = getFolderElement("Sample Vault").findElement(By.xpath("..")).findElement(By.cssSelector("ul>li[id*='TaskPane']>a"));
				//taskPaneFolder.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()",taskPaneFolder);
				else
					ActionEventUtils.click(driver, taskPaneFolder);
			}
			else if(settingName.trim().equalsIgnoreCase("Controls")) {
				WebElement controlsFolder = getFolderElement("Sample Vault").findElement(By.xpath("..")).findElement(By.cssSelector("ul>li[id*='Controls']>a"));
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", controlsFolder);
				else
					ActionEventUtils.click(driver, controlsFolder);
				/*JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", controlsFolder);*/
				//	controlsFolder.click();
			}
			Log.event(settingName+" Folder clicked.");

		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.clickSettingsFolder : "+settingName+" Folder not present : "+ e.getMessage(), e);

		}
	}

	/**
	 * <br>Description: Click the Document Vault</br>
	 * @param vaultName
	 */
	public void clickVaultFolder(String vaultName)
	{
		try {
			Utils.fluentWait(driver);
			int snooze = 0;

			if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", this.getFolderElement(vaultName));
			else
				ActionEventUtils.click(driver, this.getFolderElement(vaultName));

			while(!configurationPanel.getVaultAccess() && configurationPanel.getDefaultView().contains("Root") && configurationPanel.getBreadCrumb().equals("Hide") && configurationPanel.getTopMenu().equals("Hide") && snooze < 60)
			{
				Thread.sleep(500);
				snooze++;
				Log.event("ConfigurationPage.clickVaultFolder : Waiting for the Configuration settings loading in the configuration webpage..");
			}
			Utils.fluentWait(driver);
			Log.event(vaultName+" Folder clicked.");
		}
		catch(Exception e)
		{
			throw new NoSuchElementException("Exception at ConfigurationPage.clickVaultFolder : "+vaultName+" Folder not present : "+e.getMessage(), e);
		}
	}

	/**
	 * getFolderElement : This function is used to get the vault folder link in the configuration webpage
	 * @param vaultName
	 * @return Vault folder link webelement
	 * @throws Exception
	 */
	public WebElement getFolderElement(String vaultName) throws Exception{

		final long startTime = StopWatch.startTime();

		try
		{
			List<WebElement> vaults = driver.findElements(By.cssSelector("li[role='treeitem']>a"));

			for (int i=0; i < vaults.size(); i++){
				System.out.println(vaults.get(i).getText());
				if (vaults.get(i).getText().trim().equalsIgnoreCase(vaultName.trim()))
					return vaults.get(i);}

			throw new Exception("Vault not exist in the configuration webpage");
		}//End Try
		catch (Exception e) {
			throw new Exception ("Exception at ConfigurationPage.getFolderElement", e);
		} //End catch
		finally {
			Log.event("ConfigurationPage.getFolderElement : "+vaultName+" folder link returned", StopWatch.elapsedTime(startTime));
		}//End finally
	}//End getFolderElement

	/**
	 * <br>Description : Select 'TaskPane' options of SampleVault</br>
	 * @param driver
	 * @throws Exception
	 */
	public boolean clickSampleVaultTaskPane(WebDriver driver) throws Exception 
	{
		try
		{
			//sampleVaultTaskArea.click();
			if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", sampleVaultTaskArea);
			else
				ActionEventUtils.click(driver, sampleVaultTaskArea);

			if(taskPaneSettings.isDisplayed())
			{
				Log.event("SampleVault->Taskpane Settings displayed.");
				return true;
			}
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.clickSampleVaultTaskPane : SampleVault->Task Area is not Displayed : "+e.getMessage(), e);
		}

		return false;
	}

	/**
	 * <br>Description : 'Show' SampleVault Settings</br>
	 * @param driver
	 * @param option
	 * @param settingSection
	 * @throws Exception 
	 *//*
	public void chooseConfigurationVaultSettings(WebDriver driver,String option,String optionHeader,String status) throws Exception
	{
		int settingValue=0;
		List<WebElement> options=null;

		try {

			if(optionHeader.equalsIgnoreCase("taskPane")) {
				options=taskPaneSettings.findElements(By.cssSelector("div[id='"+taskPaneItems+"']>table[class='fullLength']>tbody>tr"));
			}
			else if(optionHeader.equalsIgnoreCase("controls")) {
				options=controlSettings.findElements(By.cssSelector("div[id='"+configControls+"']>table[class='fullLength']>tbody>tr"));
			}

			Utils.fluentWait(this.driver);
			for (int i=0;i<options.size();i++)
			{
				List<WebElement> fields=options.get(i).findElements(By.cssSelector("td"));
				for (int j=0;j<fields.size();j++)
				{
				//	System.out.println("J :"+j+","+fields.get(j).getText());
					if(fields.get(j).getText().contains(option))
					{
							if (status.toUpperCase().trim().equals("SHOW") || status.toUpperCase().trim().equals("ALLOW"))	{
								Utils.fluentWait(driver);
								JavascriptExecutor executor = (JavascriptExecutor)driver;
								executor.executeScript("arguments[0].click();", fields.get(j+1).findElement(By.cssSelector("input[id*='show']")));
							//	fields.get(j+1).findElement(By.cssSelector("input[id*='show']")).click();
								break;
							}
							else if (status.toUpperCase().trim().equals("HIDE") || status.toUpperCase().trim().equals("DISALLOW")) {
								Utils.fluentWait(driver);
								JavascriptExecutor executor = (JavascriptExecutor)driver;
								executor.executeScript("arguments[0].click();", fields.get(j+1).findElement(By.cssSelector("input[id*='hide']")));
							//	fields.get(j+1).findElement(By.cssSelector("input[id*='hide']")).click();
								break;
							}
							settingValue=j+1;
							break;
					}
				}

				if(settingValue!=0)
					break;
			}
		}
		catch(Exception e)
		{
			//throw new Exception("Could not 'show' Task Pane Settings."));
			e.printStackTrace();
		}
	}*/

	/**
	 * <br>Description : 'Show' SampleVault Settings</br>
	 * @param driver
	 * @param option
	 * @param settingSection
	 * @throws Exception 
	 */
	public void chooseConfigurationVaultSettings(WebDriver driver,String option,String optionHeader,String status) throws Exception
	{
		int settingValue=0;
		List<WebElement> options=null;
		option = option +":";
		try {

			if(optionHeader.equalsIgnoreCase("taskPane")) {
				options=taskPaneSettings.findElements(By.cssSelector("div[id='"+taskPaneItems+"']>table[class='fullLength']>tbody>tr"));
			}
			else if(optionHeader.equalsIgnoreCase("controls")) {
				options=controlSettings.findElements(By.cssSelector("div[id='"+configControls+"']>table[class='fullLength']>tbody>tr"));
			}

			Utils.fluentWait(this.driver);
			for (int i=0;i<options.size();i++)
			{
				List<WebElement> fields=options.get(i).findElements(By.cssSelector("td"));
				for (int j=0;j<fields.size();j++)
				{
					// System.out.println("J :"+j+","+fields.get(j).getText());
					if(fields.get(j).getText().contains(option))
					{ 
						if (status.toUpperCase().trim().equals("SHOW") || status.toUpperCase().trim().equals("ALLOW"))	{
							System.out.println("J :"+j+1 +","+fields.get(j+1).getText());
							//fields.get(j+1).findElement(By.cssSelector("input[id*='show']")).click();
							ActionEventUtils.click(driver, fields.get(j+1).findElement(By.cssSelector("input[id*='show']")));
							settingValue = j+1;
							break;
						}
						else if (status.toUpperCase().trim().equals("HIDE") || status.toUpperCase().trim().equals("DISALLOW")) {
							//fields.get(j+1).findElement(By.cssSelector("input[id*='hide']")).click();
							ActionEventUtils.click(driver, fields.get(j+1).findElement(By.cssSelector("input[id*='hide']")));
							settingValue = j+1;
							break;
						}
						settingValue=j+1;
						break;
					}
				}

				if(settingValue!=0)
					break;
			}
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.chooseGeneralSettings : Could not 'show' Task Pane Settings : "+ e.getMessage(), e);
		}
	}

	public void chooseGeneralSettings(String itemToClick) throws Exception{
		try {
			String[] items= itemToClick.split(">>");
			for (String item:items) {
				if(item.trim().equalsIgnoreCase("General")|item.trim().equalsIgnoreCase("General settings")) {
					clickSettingsFolder(item);
				}
				else if(item.trim().equalsIgnoreCase("My Vault")| item.trim().equalsIgnoreCase("Sample Vault")) {
					clickVaultFolder(item);
				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.chooseGeneralSettings : Unable to select 'General Settings' : "+ e.getMessage(), e);
		}
	}

	/**
	 *<br>Description : Verify if User 'Logged In' to application</br>
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isLoginPageDisplayed() throws Exception
	{
		try {
			//WebElement loginForm=driver.findElement(By.cssSelector("form[class='loginForm']"));
			if(!loginForm.isDisplayed())
				return false;
		}catch(Exception e){
			throw new Exception("Exception at ConfigurationPage.isLoginPageDisplayed :Login form not displayed : "+e.getMessage(), e);
		}

		return true;
	}

	/**
	 * <br>Description: Get the Error Message</br>
	 * @return error Messge
	 * @throws Exception 
	 */
	public String getErrorMessage() throws Exception {
		//Variable Declaration
		String errMsg = ""; 
		int snooze = 0;

		try {

			//Variable Declaration
			//WebElement errMsgDiv;

			while (errMsg.equals("") && snooze < 30) {
				WebElement errMsgDiv = driver.findElement(By.cssSelector("div[id='msg']"));
				errMsg = errMsgDiv.getText();
				Utils.fluentWait(this.driver);
				snooze++;
			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException")|e.getClass().toString().contains("NoSuchElementException")) {
				errMsg = "";
			}
			else { 
				throw new Exception("Exception at ConfigurationPage. : "+e.getMessage(), e);
			}
		} //End catch

		return errMsg;
	} //End function getErrorMessage

	/**
	 * <br>Description: click 'LogOut' button </br> 
	 * @throws Exception
	 */
	public void clickLogOut() throws Exception {
		try {

			WebElement logOutBtn = driver.findElement(By.cssSelector("div[class='logout']>div[class='logOutText']")); //Information of the page slected
			/*JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();",logOutBtn);*/
			ActionEventUtils.click(driver, logOutBtn);
			//logOutBtn.click(); //Clicks Logout button
			Utils.fluentWait(this.driver);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at ConfigurationPage. : "+e.getMessage(), e);
		} //End catch

	} //End LogOut

	/**
	 * <br> Description:Verify if General Settings Page is empty</br>
	 * @return true or false
	 * @throws Exception 
	 */
	public Boolean isGeneralSettingPageEmpty() throws Exception {
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
			throw new Exception("Exception at ConfigurationPage. : "+e.getMessage(), e);
		} //End catch

		return isEmpty;

	} //End GetAutoLogin

	/**
	 * <br>Description: This method is to click item in tree view panel</br>
	 * @param itemToClick
	 * @throws Exception
	 */
	public void clickTreeViewItem(String itemToClick) throws Exception {

		try {

			//Variable Declaration
			int itemIdx; //To store the index of the items used in for loop
			int treeViewItemCt; //To store the tree view item count
			WebElement parentElement; //To store the parent web element of the item to be clicked
			List<WebElement> treeViewItems; //To store all the web element that is to be clicked

			String[] items = itemToClick.split(">>");
			int itemCt = items.length;

			parentElement=driver.findElement(By.cssSelector("div[id='tree']"));
			//			parentElement = this.rootSettingsTree;

			for (int i=0; i<itemCt; i++) { //Loops till the last depth of the element

				treeViewItems = parentElement.findElements(By.cssSelector("ul>li>a")); //Identifies all the tree view elements that could be clicked
				treeViewItemCt = treeViewItems.size(); //Gets the number of items in the tree view

				for (itemIdx=0; itemIdx<treeViewItemCt; itemIdx++)  //Loops to identify the instance of the item to be clicked
				{
					System.out.println(treeViewItems.get(itemIdx).getText());
					if (treeViewItems.get(itemIdx).getText().toUpperCase().trim().equals(items[i].toUpperCase().trim())) {
						break;
					}

					if (itemIdx >= treeViewItemCt) //Checks for the existence of the item to click
						throw new Exception("Item (" + items[i] + ") does not exists in the list.");
				}
				parentElement = treeViewItems.get(itemIdx).findElement(By.xpath("..")); //Gets the parent element of the element that could be clicked

				if (i == itemCt-1) //Clicks the tree view item
					if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()", treeViewItems.get(itemIdx));
					else
						ActionEventUtils.click(driver, treeViewItems.get(itemIdx));
				else if (parentElement.getAttribute("class").toUpperCase().trim().contains("JSTREE-CLOSED")) //Expands the tree view item after checking its expansion status
					if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
						((JavascriptExecutor) driver).executeScript("arguments[0].click()", parentElement.findElement(By.cssSelector("ins[class='jstree-icon']")));
					else
						ActionEventUtils.click(driver, parentElement.findElement(By.cssSelector("ins[class='jstree-icon']")));


			} //End for

			int snooze  = 0;

			if(itemToClick.equals("General settings>>General"))
				while(configurationPanel.getPageTitle().equals("") && snooze < 60)
				{
					Thread.sleep(500);
					snooze++;
					Log.event("ConfigurationPage.clickTreeViewItem : Waiting for the Configuration settings loading in the configuration webpage..");
				}
			else if(itemToClick.contains("Vault-specific settings>>"))
				while(!configurationPanel.getVaultAccess() && configurationPanel.getDefaultView().contains("Root") && configurationPanel.getBreadCrumb().equals("Hide") && configurationPanel.getTopMenu().equals("Hide") && snooze < 60)
				{
					Thread.sleep(500);
					snooze++;
					Log.event("ConfigurationPage.clickTreeViewItem : Waiting for the Configuration settings loading in the configuration webpage..");
				}

		} //End try

		catch (Exception e) {
			if(e.getClass().toString().contains("NullPointerException")) 
				throw new Exception("Exception at ConfigurationPage.clickTreeViewItem : No setting specified in the TestData sheet : "+e.getMessage(), e);
			else 
				throw new Exception("Exception at ConfigurationPage.clickTreeViewItem : Unable to select the configuration setting Folder : "+e.getMessage(), e);
		} //End catch

	}//End Function ClickTreeViewItem

	/**
	 * <br>Description: Reset the changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 */
	public void clickResetButton() throws Exception
	{
		try
		{
			int snooze = 0;
			WebElement resetBtn=driver.findElement(By.cssSelector("div[id='actionControls']>input[id='reloadSettings']"));
			//Click Save button

			while(snooze < 5 && !resetBtn.isDisplayed()) {
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", resetBtn);
				snooze++;
			}

			if(resetBtn.isDisplayed())
			{

				//click 'Reset' button
				//resetBtn.click();
				if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", resetBtn);
				else
					ActionEventUtils.click(driver, resetBtn);

				Utils.fluentWait(this.driver);

			}
			else
				throw new Exception("Reset button is not displayed in the configuration webpage.");

		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.clickResetButton : Error while Resetting changes to Configuration settings."+e.getMessage(), e);
		}
	}
	/**
	 * <br>Description: Save changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 *//*
	public void clickSaveButton() throws Exception
	{
		try {
			WebElement saveBtn=driver.findElement(By.cssSelector("input[id='saveSettings']"));
			//Click Save button
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", saveBtn);
			//saveBtn.click();


		}
		catch(Exception e)
		{
			throw new Exception("Error while saving changes to Configuration settings.");
		}
	}*/

	/**
	 * <br>Description: Save changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 */
	public void saveSettings() throws Exception
	{
		try {


			Utils.fluentWait(driver);			
			WebElement saveBtn=driver.findElement(By.cssSelector("input[id='saveSettings']"));

			if (!saveBtn.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", saveBtn);
			}

			//Click Save button	
			ActionEventUtils.click(driver, saveBtn);
			Utils.fluentWait(driver);
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.clickSaveButton : Error while saving changes to Configuration settings. : "+e.getMessage(), e);
		}
	}

	/**
	 * <br>Description: Save changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 */
	public void clickSaveButton() throws Exception
	{
		try {

			Utils.fluentWait(driver);			
			WebElement saveBtn=driver.findElement(By.cssSelector("input[id='saveSettings']"));

			if (!saveBtn.isDisplayed())
			{
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].scrollIntoView();", saveBtn);
			}

			//Click Save button
			if(browser.equalsIgnoreCase("ie")||browser.equalsIgnoreCase("edge"))
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", saveBtn);
			else
				ActionEventUtils.click(driver, saveBtn);

			MFilesDialog mfilesdialog = new MFilesDialog(driver);

			if(!mfilesdialog.getMessage().equalsIgnoreCase("Settings saved successfully."))
				throw new Exception("Save message is not displayed as expected.");

			Utils.fluentWait(driver);
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.clickSaveButton : Error while saving changes to Configuration settings. : "+e.getMessage(), e);
		}
	}

	/**
	 * <br>Description: Confirm Changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 *//*
	public void clickOKBtnOnSaveDialog() throws Exception 
	{
		try {
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[class*='ui-draggable']>div>div>button[class*='window_ok']"))));

			//click OK button on Save dialog
			WebElement okBtn=driver.findElement(By.cssSelector("button[class*='window_ok']"));
//			JavascriptExecutor executor = (JavascriptExecutor)driver;
//			executor.executeScript("arguments[0].click();",okBtn);
			okBtn.click();
			Utils.fluentWait(this.driver);
		}
		catch(Exception e) {
			throw new Exception("Error while confirming changes to Configuration settings."));
		}

	}*/


	/**
	 * <br>Description get the 'Current timestamp'</br>
	 * @return
	 */
	public static String getTime() {

		String currDateTime;

		//Gets current date and time
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		currDateTime = dateFormat.format(date);
		return currDateTime;
	}


	/**
	 * <br>Description: Confirm Changes to Settings</br>
	 * @param driver
	 * @throws Exception
	 */
	public void clickOKBtnOnSaveDialog() throws Exception 

	{

		final long startTime = StopWatch.startTime();
		try {
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[class*='ui-draggable']>div>div>button[class*='window_ok']"))));

			//click OK button on Save dialog
			WebElement okBtn=driver.findElement(By.cssSelector("button[class*='window_ok']"));
			ActionEventUtils.click(driver, okBtn);
			Utils.fluentWait(driver);

			Log.event("ConfigurationPage.clickOKBtnOnSaveDialog : Clicked the ok button in the save dialog box.", StopWatch.elapsedTime(startTime));
		}
		catch(Exception e) {
			throw new Exception("Exception at ConfigurationPage.clickOKBtnOnSaveDialog : Exception atError while confirming changes to Configuration settings. : "+e.getMessage(), e);
		}

	}

	public Boolean getConfigSettingValue(String optionHeader,String option,String status) throws Exception {
		List<WebElement> options=null;
		Boolean settingValue=false;
		try {

			if(optionHeader.equalsIgnoreCase("taskPane")) {
				options=taskPaneSettings.findElements(By.cssSelector("div[id='"+taskPaneItems+"']>table[class='fullLength']>tbody>tr"));
			}
			else if(optionHeader.equalsIgnoreCase("controls")) {
				options=controlSettings.findElements(By.cssSelector("div[id='"+configControls+"']>table[class='fullLength']>tbody>tr"));
			}

			Utils.fluentWait(this.driver);
			for (int i=0;i<options.size();i++)
			{
				List<WebElement> fields=options.get(i).findElements(By.cssSelector("td"));
				for (int j=0;j<fields.size();j++)
				{
					if(fields.get(j).getText().contains(option))
					{
						if (status.toUpperCase().trim().equals("SHOW") || status.toUpperCase().trim().equals("ALLOW"))	{
							if(fields.get(j+1).findElement(By.cssSelector("input[id*='show']")).isSelected()) {
								settingValue=true;
								break;
							}

						}
						else if (status.toUpperCase().trim().equals("HIDE") || status.toUpperCase().trim().equals("DISALLOW")) {
							if(fields.get(j+1).findElement(By.cssSelector("input[id*='hide']")).isSelected()) {
								settingValue=true;
								break;
							}
						}
					}
				}

			}
		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage.getConfigSettingValue : Could not 'show' Task Pane Settings."+e.getMessage(), e);

		}
		return settingValue;
	}

	/**
	 * <br>Description: Check warning message dispalyed</br>
	 * @param driver
	 * @throws Exception
	 */
	public Boolean isWarningDialogDisplayed() throws Exception
	{
		try {
			WebElement warningdialog=driver.findElement(By.cssSelector("div[class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable ui-header-titlebar']")).findElement(By.cssSelector("div[class='window_pages']>img"));
			//Click Save button
			String image = warningdialog.getAttribute("src");
			if(image.contains("Warning"))
			{
				//  warningdialog.findElement(By.cssSelector("div[class='window_buttons']>button[class='window_ok ui-default-gray']")).click();
				//click OK button on Save dialog
				WebElement okBtn= warningdialog.findElement(By.cssSelector("button[class*='window_ok']"));
				ActionEventUtils.click(driver, okBtn);
				// Utils.fluentWait(driver);
				Thread.sleep(200);
				return true;
			}
			else
				return false;
		}
		catch(Exception e)
		{
			if (e.getClass().toString().contains("NoSuchElementException")) { 
				Log.event("Warning dialog is not Displayed in MFWA.");
				return false;
			}
			else				
				throw new Exception("Exception at ConfigurationPage.isWarningDialogDisplayed : Error while checking the warning dialog is display in configuration page : "+e.getMessage(), e);
		}
	}// End isWarningDialogDisplayed

	/**
	 * <br>Description: Enabled UTC date option</br>
	 * @param driver
	 * @throws Exception
	 */
	public void setUTCdate(boolean bool) throws Exception
	{
		try {
			Thread.sleep(1000);
			WebElement UTCcheckbox=driver.findElement(By.id("chkUseUtc"));
			//Click Save button
			if(!UTCcheckbox.isSelected())
			{
				//UTCcheckbox.click();
				ActionEventUtils.click(driver, UTCcheckbox);
				Thread.sleep(500);
			}

		}
		catch(Exception e)
		{
			throw new Exception("Exception at ConfigurationPage. : Error while checking the warning dialog is display in configuration page : "+e.getMessage(), e);
		}
	}// End setUTCdate

}
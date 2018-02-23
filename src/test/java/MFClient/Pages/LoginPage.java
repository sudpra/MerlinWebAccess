package MFClient.Pages;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

import MFClient.Wrappers.Utility;

public class LoginPage extends LoadableComponent <HomePage>{

	private boolean pageLoaded = false;
	private final WebDriver driver;
	/********************************************************************
	 *	Page Factory Elements -LoginPage
	 **********************************************************************/
	@FindBy(how=How.CSS, using="input[id='txtUsername']")
	private WebElement txtUserName;

	@FindBy(how=How.CSS, using="input[id='txtPassword']")
	//@FindBy(how=How.CSS, using="input[class='placeholder clone']")
	private WebElement txtPassword;

	@FindBy(how=How.CSS, using="input[value='Log In'][type='submit'],div[id='login']>input")
	private WebElement btnLogin;


	/********************************************************************
	 * Page Factory Methods
	 **********************************************************************/
	public LoginPage(final WebDriver driver) {
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 2);
		PageFactory.initElements(finder, this);
	}

	final protected void isLoaded(){
		if (!(driver.getCurrentUrl().toLowerCase().contains("/login.aspx") && driver.getTitle().contains("M-Files Web Access"))){
			if (!pageLoaded)
				Assert.fail();
			try {
				Log.fail("Expected page was a WebAccess Home page, but current page is not a Login page." + "Current Page is: " + driver.getCurrentUrl(), driver);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Verify whether is Home page
		}
	}

	final protected void load(){
		try {
			Utils.fluentWait(driver);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		pageLoaded = true;
	}

	/**
	 * loginToWebApplication : Logs in to WebApplication
	 * @param userName - Name of the user
	 * @param password - Password of the user
	 * @param documentVault - Test vault to select for login
	 * @return Instance of home page
	 * @throws Exception 
	 */
	public HomePage loginToWebApplication(String userName, String password, String documentVault) throws Exception {

		final long startTime = StopWatch.startTime();


		try {
			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToWebApplication : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}
			catch (Exception e1) {}
			setUserName(userName);//Sets the user name
			setPassword(password);//Sets the password
			Utils.fluentWait(driver);
			clickLoginBtn();//Clicks Login button
			Thread.sleep(2000);
			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(this.driver);} catch (Exception e1) {}
			Utils.fluentWait(driver, 750);
			selectVault(documentVault);//Select Vault to login
			Utils.fluentWait(this.driver, 750);
			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToWebApplication : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}
			catch (Exception e2) {}

			return new HomePage(driver);

		} //End try
		catch(Exception e){
			throw new Exception("Exception at LoginPage.loginToWebApplication : " + e);
		} //End catch
		finally{
			Log.event("Successfully logged in to login Page.",StopWatch.elapsedTime(startTime));
		}

	} //End loginToWebApplication

	/**
	 * setUserName : Sets user name in the in the login page
	 * @param userName - Name of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setUserName(String userName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			txtUserName.clear();
			txtUserName.sendKeys(userName); //Enters the value in user name text box

			Log.event("setUserName : User Name is entered.", StopWatch.elapsedTime(startTime));

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at LoginPage.setUserName : " + e);
		} //End catch

	} //setUserName

	/**
	 * setPassword : Sets password in the login page
	 * @param password - Password of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setPassword(String password) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (Utility.getBrowserName(this.driver).toLowerCase().contains("internet explorer 9")) 
				txtPassword = driver.findElement(By.cssSelector("input[class='placeholder clone']"));
			else
				txtPassword.clear();

			txtPassword.sendKeys(password); //Enters the password

			Log.event("setPassword : Password is entered.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.setPassword : " + e);
		} //End catch

	} //End setPassword

	/**
	 * getPassword : Gets password in the login page
	 * @param none
	 * @return password - Password of the user
	 * @throws Exception 
	 */
	public String getPassword() throws Exception {

		try {

			JavascriptExecutor js=(JavascriptExecutor) driver;
			return (String) js.executeScript("return document.getElementById('txtPassword').value");

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.getPassword : " + e);
		} //End catch

	} //End getPassword

	/**
	 * clickLoginBtn : Clicks Login button in the login page
	 * @param none
	 * @return none
	 * @throws Exception 
	 */
	public void clickLoginBtn() throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			ActionEventUtils.click(driver, btnLogin);
			//((JavascriptExecutor) driver).executeScript("arguments[0].click()", btnLogin);
			Log.event("clickLoginBtn : Login button is clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.clickLoginBtn : " + e);
		} //End catch

	} //clickLoginBtn

	/**
	 * selectVault : To select vault in the login page
	 * @param docVault - Name of the test vault
	 * @return None
	 * @throws Exception 
	 */
	public void selectVault(String docVault) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			new LoginPage(driver);//Re-Instantiates the login page
			int snooze = 0;
			while (snooze < 3 && !driver.getCurrentUrl().toLowerCase().contains("login.aspx") && !driver.getCurrentUrl().toLowerCase().contains("default.aspx"))
			{
				try
				{
					new LoginPage(driver);//Re-Instantiates the login page

					if(driver.findElement(By.cssSelector("input[value*='No']")).isDisplayed())
					{
						ActionEventUtils.click(driver, driver.findElement(By.cssSelector("input[value*='No']")));
						Log.warning("LoginPage.selectVault : Clicked No button in the remember login page.");
						break;
					}
				}
				catch(Exception e0){Thread.sleep(500);}
				snooze++;
			}
			snooze = 0;
			while (snooze < 6)
			{
				try {
					Alert alert = driver.switchTo().alert();
					Log.warning("LoginPage.selectVault : Alert dialog displayed with message : "+ alert.getText());
					alert.accept();
					Utils.fluentWait(this.driver);
					break;
				}
				catch (Exception e1) {
					Thread.sleep(500);
				}				
				snooze++;
			}
			Utils.fluentWait(driver);

			if (docVault.equalsIgnoreCase("")  || docVault.equalsIgnoreCase(null))
				return;

			int _timeout = 0;

			//Code to wait 30 seconds if default page or vault selection panel is not loaded
			while (!this.driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && 
					this.driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']")).size() == 0 && 
					_timeout < 60) {

				Thread.sleep(500);
				_timeout++;

			} //End while

			if ((!this.driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && 
					this.driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']")).size() == 0))
			{
				snooze = 0;
				while (snooze < 3 && !driver.getCurrentUrl().toLowerCase().contains("login.aspx") && !driver.getCurrentUrl().toLowerCase().contains("default.aspx"))
				{
					try
					{
						new LoginPage(driver);//Re-Instantiates the login page

						if(driver.findElement(By.cssSelector("input[value*='No']")).isDisplayed())
						{
							ActionEventUtils.click(driver, driver.findElement(By.cssSelector("input[value*='No']")));
							Log.warning("LoginPage.selectVault : Clicked No button in the remember login page.");
							break;
						}
					}
					catch(Exception e0){Thread.sleep(500);}
					snooze++;
				}
				if ((!this.driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && 
						this.driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']")).size() == 0))
					throw new Exception("Exception at Loginpage.selectVault : Current URL is not default.aspx or Vault selection panel is not displayed.");
			}

			if (this.driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && !this.driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				return;

			List<WebElement> vaultName = this.driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']")); //Fetch the list of vaults displayed

			if (vaultName.size() == 0)
				return;

			for(WebElement vault : vaultName){

				if (vault.getText().trim().equalsIgnoreCase(docVault.trim())){
					//vault.click();
					//((JavascriptExecutor) driver).executeScript("arguments[0].click()", vault);
					ActionEventUtils.click(driver, vault);
					Thread.sleep(2000);
					//Utils.fluentWait(this.driver);
					try { Alert alert = driver.switchTo().alert(); alert.dismiss();} catch (Exception e1) {}
					Log.event("selectVault : '" + docVault + "' link displayed and selected after providing valid user credentials.",StopWatch.elapsedTime(startTime));
					break;
				}
			}

			Utils.fluentWait(this.driver);

		} //End try

		catch(Exception e) {
			throw new Exception ("Exception at LoginPage.selectVault : " + e);
		} //End catch

	} //End selectVault


	/**
	 * selectVault : To select vault in the login page
	 * @param docVault - Name of the test vault
	 * @return None
	 * @throws Exception 
	 */
	public void selectWindowsUserVault(String docVault) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (docVault.equalsIgnoreCase("")  || docVault.equalsIgnoreCase(null))
				return;

			if (this.driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				return;

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='vaults']"))));

			List<WebElement> vaultName = this.driver.findElements(By.cssSelector("div[id='vaults']>a")); //Fetch the list of vaults displayed

			if (vaultName.size() == 0)
				return;

			for(WebElement vault : vaultName){
				System.out.println(vault.getText() + " : " + docVault);
				if (vault.getText().trim().equalsIgnoreCase(docVault.trim())){
					//vault.click();
					//	((JavascriptExecutor) driver).executeScript("arguments[0].click()", vault);
					ActionEventUtils.click(driver, vault);
					//Utils.fluentWait(this.driver);
					try { Alert alert = driver.switchTo().alert(); alert.dismiss();	} catch (Exception e1) {}
					Log.event("selectVault : '" + docVault + "' link displayed and selected after providing valid user credentials.",StopWatch.elapsedTime(startTime));
					break;
				}
			}

			Utils.fluentWait(this.driver);

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.selectWindowsUserVault : " + e);
		} //End catch

	} //End selectVault

	/**
	 * loginToConfigurationUI : To perform login operation in configuration page
	 * @param userName - Name of the user
	 * @param password - Password of the user
	 * @return Instance of Configuration Page
	 * @throws Exception 
	 */
	public ConfigurationPage loginToConfigurationUI(String userName, String password) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToConfigurationUI : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}
			catch (Exception e1) {}
			// Enter Login Credentials and click Login button
			setUserName(userName);
			setPassword(password);
			Utils.fluentWait(driver);
			clickLoginBtn();
			Utils.fluentWait(driver, 750);			
			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToConfigurationUI : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}catch (Exception e2) {}


		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.loginToConfigurationUI : "+ e);
		} //End catch

		Log.event("Successfully logged in to Configuration Page.",StopWatch.elapsedTime(startTime));
		return new ConfigurationPage(driver);

	} //End loginToConfigurationUI

	/**
	 * isLoginPageDisplayed : To check if login page is displayed
	 * @return true if login page is displayed; false if not
	 * @throws Exception 
	 */
	public boolean isLoginPageDisplayed() throws Exception {

		try {

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("form[class='loginForm']"))));

			WebElement loginForm=driver.findElement(By.cssSelector("form[class='loginForm']"));

			if(!loginForm.isDisplayed())
				return false;

		} //End try

		catch(Exception e) {
			Log.exception(new NoSuchElementException("Login form not displayed."));
		} //End catch

		return true;
	}

	/**
	 * isUserNameFieldDisplayed: Checks whether username field is displayed or not in the loginpage
	 * @return
	 * @throws Exception
	 */
	public Boolean isUserNameFieldDisplayed() throws Exception {

		//Variable Declaration
		Boolean isDisplayed = false;

		try {

			WebElement userNameField = driver.findElement(By.cssSelector("input[id='txtUsername']"));

			if (userNameField.isDisplayed())
				isDisplayed = true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException"))				
				isDisplayed = false;
			else
				throw new Exception("Exception at LoginPage.isUserNameFieldDisplayed : " + e);
		} //End catch

		return isDisplayed;

	} //isUserNameFieldDisplayed

	/**
	 * clickWindowLoginDisplayed : To click the wo
	 * @return true if login page is displayed; false if not
	 * @throws Exception 
	 */
	public Boolean isWindowLoginDisplayed() throws Exception {

		//Variable Declaration
		Boolean isDisplayed = false;

		try {

			WebElement loginWndDiv = driver.findElement(By.cssSelector("a[id='windowLoginPage']"));

			if (loginWndDiv.isDisplayed())
				isDisplayed = true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException"))				
				isDisplayed = false;
			else
				throw new Exception("Exception at LoginPage.isWindowLoginDisplayed : " + e);
		} //End catch

		return isDisplayed;

	} //isWindowLoginDisplayed


	/**
	 * isWindowLoginDisplayed : To check if login window displayed
	 * @return true if login page is displayed; false if not
	 * @throws Exception 
	 */
	public void clickWindowLoginlink() throws Exception {


		try {

			WebElement loginWndDiv = driver.findElement(By.cssSelector("div a[id='windowLoginPage']"));
			ActionEventUtils.click(driver, loginWndDiv);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at LoginPage.clickWindowLoginlink : " + e);
		}

	} //clickWindowLoginlink

	/**
	 * getErrorMessage : To get error message displayed after clicking login
	 * @return Error message
	 * @throws Exception 
	 */
	public String getErrorMessage() throws Exception {

		String errMsg = "";
		try {

			int snooze = 0;
			Utils.fluentWait(driver);
			while (errMsg.equals("") && snooze < 30) {
				WebElement errMsgDiv = driver.findElement(By.cssSelector("div[id='msg']"));
				errMsg = errMsgDiv.getText();
				Thread.sleep(500);
				snooze++;
			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException") || e.getMessage().toLowerCase().contains("unable to get element text")) {
				errMsg = "";
			}
			else { 
				throw new Exception("Exception at LoginPage.getErrorMessage : " + e.getMessage());
			}
		} //End catch

		return errMsg;

	} //End function getErrorMessage

	/**
	 * navigateToApplication : Logs into the application
	 * @param webSite
	 * @param userName
	 * @param password
	 * @param vaultName
	 * @return None
	 * @throws Exception 
	 */
	public void navigateToApplication(String webSite, String userName, String password, String vaultName) throws Exception {

		try {

			driver.get(webSite);
			Utils.fluentWait(driver);
			int snooze = 0;

			while (snooze < 10 && (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") || !driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") || !driver.getCurrentUrl().toUpperCase().contains("CONFIGURATON.ASPX.ASPX")))
			{
				Thread.sleep(500);
				snooze++;
			}

			if(webSite.contains("Configuration.aspx")| webSite.contains("target=config"))
				loginToConfigurationUI(userName,password);
			else 
				loginToWebApplication(userName,password,vaultName);

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at LoginPage.navigateToApplication : " + e);
		} //End catch

	} //End navigateToApplication

	/**
	 * launchDriverAndLogin : This method launches url and logs into application
	 * @param driver - Instance of the current web driver
	 * @param isPrerequsite - true if it is a pre-requsite step; false if not
	 * @return Instance of HomePage
	 * @throws Exception
	 */
	public static HomePage launchDriverAndLogin(WebDriver driver, Boolean isPrerequsite) throws Exception {

		try {

			/*if (isPrerequsite)
				Log.testCaseInfo();*/

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			String testVault = xmlParameters.getParameter("VaultName");

			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application

			if (!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX")) //Checks if it login is done successful
				throw new Exception ("Browser is not navigated to the default page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			if (isPrerequsite)
				Log.message("Pre-requsite : Browser is opened and logged into MFWA. ( User Name : " + userName + "; Vault : " + testVault + ")");//, driver);

			return homePage;

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at LoginPage.launchDriverAndLogin : " + e);
		} //End catch

	} //End function launchDriverAndLogin

	/**
	 * launchDriverAndLogin : This method launches url and logs into application
	 * @param driver - Instance of the current web driver
	 * @return Instance of HomePage
	 * @throws Exception
	 */
	public static HomePage launchDriverAndLogin(WebDriver driver, String userName, String password, String testVault) throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			int snooze = 0;

			driver.get(loginURL); //Launches with the loginURL
			Utils.fluentWait(driver);

			while (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && snooze < 60) {
				snooze++;
				Thread.sleep(500);
			}

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			snooze = 0;
			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password, testVault); //Logs into application

			while (!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX") && snooze < 60) {
				snooze++;
				Thread.sleep(500);
			}

			if (!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX")) //Checks if it login is done successful
				throw new Exception ("Browser is not navigated to the default page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			return homePage;

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at LoginPage.launchDriverAndLogin : " + e);
		} //End catch

	} //End function launchDriverAndLogin


	/**
	 * launchDriverAndLogin : This method launches url and logs into application
	 * @param driver - Instance of the current web driver
	 * @param isPrerequsite - true if it is a pre-requsite step; false if not
	 * @return Instance of HomePage
	 * @throws Exception
	 */
	public static HomePage loginIntoWebApplication(WebDriver driver, Boolean isPrerequsite) throws Exception {

		try {

			/*if (isPrerequsite)
				Log.testCaseInfo();*/

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String loginURL = xmlParameters.getParameter("webSite");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");
			String testVault = xmlParameters.getParameter("VaultName");

			driver.get(loginURL); //Launches with the URL

			if (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Login page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			HomePage homePage = loginPage.loginToWebApplication(userName, password); //Logs into application
			Utils.fluentWait(driver);

			if (isPrerequsite)
				Log.message("Pre-requsite : Browser is opened and logged into MFWA. ( User Name : " + userName + "; Vault : " + testVault + ")", driver);

			return homePage;

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at LoginPage.loginIntoWebApplication : " + e);
		} //End catch

	} //End function launchDriverAndLogin

	/**
	 * loginToWebApplication : Logs in to WebApplication
	 * @param userName - Name of the user
	 * @param password - Password of the user
	 * @param documentVault - Test vault to select for login
	 * @return Instance of home page
	 * @throws Exception 
	 */
	public HomePage loginToWebApplication(String userName, String password) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToWebApplication : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}
			catch (Exception e1) {}
			setUserName(userName);//Sets the user name
			setPassword(password);//Sets the password
			Utils.fluentWait(driver);
			clickLoginBtn();//Clicks Login button
			Utils.fluentWait(driver, 750);
			try {
				Alert alert = driver.switchTo().alert();
				Log.warning("LoginPage.loginToWebApplication : Alert dialog displayed with message : "+ alert.getText());
				alert.dismiss();
				Utils.fluentWait(this.driver);
			}
			catch (Exception e2) {}

			return new HomePage(driver);

		} //End try
		catch(Exception e){
			throw new Exception("Exception at LoginPage.loginToWebApplication : " + e);
		} //End catch
		finally{
			Log.event("Successfully logged in to login Page.",StopWatch.elapsedTime(startTime));
		}
	} //End loginToWebApplication


	/**
	 * loginToWebApplicationUsingSAMLorOAuth : Logs in to WebApplication using SAML or OAUTH
	 * @param userName - Name of the user
	 * @param password - Password of the user
	 * @return Instance of home page
	 * @throws Exception 
	 */
	public HomePage loginToWebApplicationUsingSAMLorOAuth(String userName, String password, String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			setSAMLorOAuthUserName(userName);
			setSAMLorOAuthPassword(password);
			clickSAMLorOAuthLoginBtn();
			selectVault(vaultName);//Selects the vault if vault list is displayed

			if(!driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				throw new Exception("LoginPage.loginToWebApplicationUsingSAMLorOAuth: Default page is not loaded after clicking the login button.[Current URL: " + driver.getCurrentUrl() + "]");

			return new HomePage(driver);

		} //End try
		catch(Exception e){
			throw new Exception("Exception at LoginPage.loginToWebApplicationUsingSAMLorOAuth : " + e);
		} //End catch
		finally{
			Log.event("Successfully logged in to Default Page using SAML/OAUTH.",StopWatch.elapsedTime(startTime));
		}
	} //End loginToWebApplicationUsingSAMLorOAuth

	/**
	 * setSAMLorOAuthUserName : Sets user name in the in the SAML or OAuth login page
	 * @param userName - Name of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setSAMLorOAuthUserName(String userName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			try
			{
				if(driver.findElement(By.cssSelector("div[id*='otherTileText']")).isDisplayed())
					ActionEventUtils.click(driver, driver.findElement(By.cssSelector("div[id*='otherTileText']")));

				Utils.fluentWait(driver);

			}
			catch(Exception e0){}

			if(userName.equals(null))
				userName = "";

			WebElement usernameField = driver.findElement(By.cssSelector("input[name*='loginfmt']"));
			usernameField.findElement(By.xpath("..")).click();
			usernameField.clear();
			usernameField.sendKeys(userName);

			WebElement nextBtn = driver.findElement(By.cssSelector("input[value*='Next']"));
			ActionEventUtils.click(driver, nextBtn);//Clicks the next button
			Utils.fluentWait(driver);

			Log.event("setSAMLorOAuthUserName : User Name is entered.", StopWatch.elapsedTime(startTime));

		} //End try
		catch(Exception e) {
			throw new Exception("Exception at LoginPage.setSAMLorOAuthUserName : " + e);
		} //End catch

	} //setSAMLorOAuthUserName

	/**
	 * setSAMLorOAuthPassword : Sets password in the SAML or OAuth login page
	 * @param password - Password of the user
	 * @return none
	 * @throws Exception 
	 */
	public void setSAMLorOAuthPassword(String password) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			if(password.equals(null))
				password = "";

			WebElement passwordField = driver.findElement(By.cssSelector("input[name*='passwd']"));
			passwordField.click();
			passwordField.clear();
			Utils.fluentWait(driver);
			passwordField.click();
			passwordField.sendKeys(password);
			Log.event("setSAMLorOAuthPassword : Password is entered.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.setSAMLorOAuthPassword : " + e);
		} //End catch

	} //End setSAMLorOAuthPassword

	/**
	 * clickSAMLorOAuthLoginBtn : Clicks Login button in the SAML or OAuth login page
	 * @param none
	 * @return none
	 * @throws Exception 
	 */
	public void clickSAMLorOAuthLoginBtn() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement loginBtn = driver.findElement(By.cssSelector("input[value*='Sign in']"));
			ActionEventUtils.click(driver, loginBtn);
			int snooze = 0;
			while (snooze < 6)
			{
				try {
					Alert alert = driver.switchTo().alert();
					Log.warning("LoginPage.clickSAMLorOAuthLoginBtn : Alert dialog displayed with message : "+ alert.getText());
					alert.accept();
					Utils.fluentWait(this.driver);
					break;
				}
				catch (Exception e1) {
					Thread.sleep(500);
				}				
				snooze++;
			}
			new LoginPage(driver);//Re-instantiates the login page
			snooze = 0;
			while (snooze < 3 && !driver.getCurrentUrl().toLowerCase().contains("login.aspx") && !driver.getCurrentUrl().toLowerCase().contains("default.aspx"))
			{
				try
				{
					if(driver.findElement(By.cssSelector("input[value*='No']")).isDisplayed())
					{
						ActionEventUtils.click(driver, driver.findElement(By.cssSelector("input[value*='No']")));
						Log.warning("LoginPage.clickSAMLorOAuthLoginBtn: Clicked No button in the remember login page.");
						break;
					}
				}
				catch(Exception e0){Thread.sleep(500);}
				snooze++;
			}
			snooze = 0;
			while (snooze < 3)
			{
				try {
					Alert alert = driver.switchTo().alert();
					Log.warning("LoginPage.clickSAMLorOAuthLoginBtn : Alert dialog displayed with message : "+ alert.getText());
					alert.accept();
					Utils.fluentWait(this.driver);
					break;
				}
				catch (Exception e1) {
					Thread.sleep(500);
				}				
				snooze++;
			}
			Utils.fluentWait(driver);
			Log.event("clickSAMLorOAuthLoginBtn : Login button is clicked.", StopWatch.elapsedTime(startTime));

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at LoginPage.clickSAMLorOAuthLoginBtn : " + e);
		} //End catch

	} //clickSAMLorOAuthLoginBtn

	/** getSAMLorOAuthLoginErrorMessage
	 * 
	 */
	public String getSAMLorOAuthLoginErrorMessage() throws Exception {

		try {
			String errMsg = "", errorMessage = "";
			WebElement errorArea = driver.findElement(By.cssSelector("div[id='usernameError'], div[id='passwordError']"));
			String[] message = null;
			if(errorArea.isDisplayed())
			{
				errMsg = errorArea.getText().trim();
				message = errMsg.split("\n");
				for(int i = 0;i < message.length; i++)
					if(!message[i].trim().equals(""))
					{
						errorMessage += message[i].trim();

						if (i != message.length-1)
							errorMessage += "\n";
					}
			}
			return errorMessage;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("StaleElementReferenceException") || e.getClass().toString().contains("NoSuchElementException") || e.getMessage().toLowerCase().contains("unable to get element text")) {
				return "";
			}
			else { 
				throw new Exception("Exception at LoginPage.getSAMLorOAuthLoginErrorMessage : " + e.getMessage());
			}
		} //End catch

	} //End function getSAMLorOAuthLoginErrorMessage

	/**
	 * isVaultListDisplayed : Checks whether vault list is displayed or not
	 * @return boolean : True/False
	 * @throws Exception 
	 */
	public boolean isVaultListDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			WebElement vaultList = driver.findElement(By.cssSelector("div[id='vaults']"));
			return (vaultList.isDisplayed());

		}//End try
		catch(Exception e){
			if (e.getMessage().contains("NoSuchElementException") || e.getMessage().contains("Unable to locate element") || e.getMessage().contains("Unable to find element"))
				return false;
			else
				throw new Exception("Exception at LoginPage.isVaultListDisplayed : " + e);
		}//End catch
		finally{
			Log.event("LoginPage.isVaultListDisplayed : Completed..",StopWatch.elapsedTime(startTime));
		}

	}//End getVaultList


	/**
	 * getVaultList : get the all vault name which listed after logged in
	 * 
	 * @return noOfVaults : vault listed in after clicking 'vaults' the command in taskpane
	 * @throws Exception 
	 */
	public final String[] getVaultList() throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='vaults']"))));

			List<WebElement> vaultName = driver.findElements(By.cssSelector("div[id='vaults']>div[class*='vault']"));
			int vaultList = vaultName.size();
			int itemIndx = 0;

			String [] noOfVaults = new String[vaultList];

			for (itemIndx=0; itemIndx<vaultList; itemIndx++)
				noOfVaults[itemIndx] = vaultName.get(itemIndx).getText().trim();

			Log.event("TaskPanel.getVaultList : Get the all vaults are listed in task pane.",StopWatch.elapsedTime(startTime));

			return noOfVaults;
		}//End try
		catch(Exception e){
			throw new Exception("Exception at LoginPage.getVaultList : " + e);
		}//End catch

	}//End getVaultList

	/**
	 * launchDriverAndLoginToConfig : This method launches url and logs into application
	 * @param driver - Instance of the current web driver
	 * @param isPrerequsite - true if it is a pre-requsite step; false if not
	 * @return Instance of ConfigurationPage
	 * @throws Exception
	 */
	public static ConfigurationPage launchDriverAndLoginToConfig(WebDriver driver, Boolean isPrerequsite) throws Exception {

		try {

			/*if (isPrerequsite)
				Log.testCaseInfo();*/

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String configURL = xmlParameters.getParameter("ConfigurationURL");
			String userName = xmlParameters.getParameter("UserName");
			String password = xmlParameters.getParameter("Password");

			driver.get(configURL); //Launches with the URL

			String currentURL = driver.getCurrentUrl();

			if (currentURL.toUpperCase().contains("CONFIGURATION.ASPX") && !currentURL.toUpperCase().contains("LOGIN.ASPX"))
				return new ConfigurationPage(driver);

			if (!currentURL.toUpperCase().contains("CONFIG")) //Checks if Login.aspx page is loaded
				throw new Exception ("Browser is not navigated to the Configuration page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			LoginPage loginPage = new LoginPage(driver); //Instantiates LoginPage class
			ConfigurationPage configPage = loginPage.loginToConfigurationUI(userName, password); //Logs into application
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().toUpperCase().contains("CONFIGURATION.ASPX")) //Checks if it login is done successful
				throw new Exception ("Browser is not navigated to the configuration page. Current URL : "+driver.getCurrentUrl().toUpperCase());

			if (isPrerequsite)
				Log.message("Pre-requsite : Browser is opened and logged into MFWA configuration page. ( User Name : " + userName + ";)");

			return configPage;

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at LoginPage.launchDriverAndLoginToConfig : " + e);
		} //End catch

	} //End function launchDriverAndLoginToConfig

	/**
	 * clickLoginLink
	 * @param loginType: Windows/SAML/OAuth
	 */
	public void clickLoginLink(String loginType) throws Exception {

		try {

			List<WebElement> links = driver.findElements(By.cssSelector("div[id='loginFooter'] div[id='loginOptions']>div>a"));
			int i = 0, snooze = 0;

			for(i = 0; i < links.size(); i++)
				if(links.get(i).getText().trim().toUpperCase().contains(loginType.toUpperCase()))
				{
					ActionEventUtils.click(driver, links.get(i));//Clicks the link in the login page
					while (snooze < 3)
					{
						try {
							Alert alert = driver.switchTo().alert();
							Log.warning("LoginPage.clickSAMLorOAuthLoginBtn : Alert dialog displayed with message : "+ alert.getText());
							alert.accept();
							Utils.fluentWait(this.driver);
							break;
						}
						catch (Exception e1) {
							Thread.sleep(500);
						}				
						snooze++;
					}
					Utils.fluentWait(driver);//Waits for the page load
					break;
				}

			if(i >= links.size())
				throw new Exception("LoginPage.clickLoginLink : Login type - '" + loginType + "' link is not exists in the login page.");

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at LoginPage.clickLoginLink : " + e.getMessage(), e);
		}

	}//End of clickLoginLink

	/**
	 * isSAMLorOAuthLinkDisplayed
	 * @param loginType: SAML/OAuth
	 */
	public boolean isSAMLorOAuthLinkDisplayed(String loginType) throws Exception {

		try {

			List<WebElement> links = driver.findElements(By.cssSelector("div[id='loginFooter'] div[id='loginOptions']>div>a"));
			int i = 0;

			for(i = 0; i < links.size(); i++)
				if(links.get(i).getText().trim().toUpperCase().contains(loginType.toUpperCase()))
					return true;//Returns link is exists in the login page

			return false;//Returns the link is not exists in the login page

		} //End try

		catch (Exception e) {
			throw new Exception("Exception at LoginPage.isSAMLorOAuthLinkDisplayed : " + e.getMessage(), e);
		}

	}//End of isSAMLorOAuthLinkDisplayed

} //End LoginPage

package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;
import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;

import org.openqa.selenium.os.WindowsUtils;

import java.io.*;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;
import org.openqa.selenium.JavascriptExecutor;

import com.aventstack.extentreports.ExtentTest;
import com.sun.jna.platform.win32.Advapi32Util;

import static com.sun.jna.platform.win32.WinReg.*;

public class Utility {

	/**
	 * backupVault: Variable is used to verify backup is Yes/No
	 */

	public static String backup = "", configUsers = "", HubandNodeConfig = "", installApp = "", OS = System.getProperty("os.name");
	static XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
	public static String browser = xmlParameters.getParameter("driverType");

	/**
	 * logOut : To Logout from web access client/Configuration
	 * @param driver - Instance of web driver
	 * @return true if log out is successful; false if not
	 * @throws Exception
	 */
	public static Boolean logOut(WebDriver driver) throws Exception {

		try {

			if (driver.getCurrentUrl().toUpperCase().contains("CONFIGURATION.ASPX")) {
				ConfigurationPage configurationPage = new ConfigurationPage(driver);
				return (configurationPage.logOut());
			}
			else
				return (Utility.logoutFromWebAccess(driver));		
		} 
		catch (Exception e) {
			throw new Exception ("Exception at Utility.logOut : "+e.getMessage(), e);
		}

	} //End function logOut

	/**
	 * logoutFromWebAccess : To Logout from web access
	 * @param driver - Instance of web driver
	 * @return true if log out is successful; false if not
	 * @throws Exception
	 */
	public static Boolean logoutFromWebAccess(WebDriver driver) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*TaskPanel tpanel = new TaskPanel(driver); //Instantiating TaskPanel wrapper class
			tpanel.clickItem("Log Out"); //Clicks Logout from the task panel*/

			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				return true;

			MenuBar menuBar = new MenuBar(driver); 
			Utils.fluentWait(driver);
			menuBar.clickLogOut(); //Clicks Log out from menu bar
			int snooze = 0;
			while (snooze < 3)
			{
				try {
					Alert alert = driver.switchTo().alert();
					Log.warning("Utility.logoutFromWebAccess : Alert dialog displayed with message : "+ alert.getText());
					if(alert.getText().toLowerCase().contains("are you sure you want to send this information?"))
						alert.accept();
					else
						alert.dismiss();
					Utils.fluentWait(driver);
					break;
				}
				catch (Exception e0) {
					Thread.sleep(500);
				}				
				snooze++;
			}

			Utils.fluentWait(driver);
			if (MFilesDialog.exists(driver)) { //Checks if MFiles dialog is displayed while logout
				MFilesDialog mfilesDialog = new MFilesDialog(driver);
				mfilesDialog.clickOkButton(); //Clicks Ok button in the Log out confirmation dialog
			}

			snooze = 0;

			while (!driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX") && snooze<20) {
				snooze++;
				Thread.sleep(500);
				Log.event("Utility.logoutFromWebAccess : Waiting for page load...", StopWatch.elapsedTime(startTime));
			}

			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX"))
				return true;

			return false;

		} 
		catch (Exception e) {
			throw new Exception ("Exception at Utility.logoutFromWebAccess : "+e.getMessage(), e);
		}

	} //End function logoutFromWebAccess

	/**
	 * resetToDefaultLayout : Resets to Default layout
	 * @param driver - Instance of web driver
	 * @return None
	 * @throws Exception
	 */
	public static void resetToDefaultLayout(WebDriver driver) throws Exception {

		if (driver == null)
			return;
		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String testVault = xmlParameters.getParameter("VaultName");

			/*if (driver.getCurrentUrl().toUpperCase().contains("DEFAULT.ASPX"))
				Utility.logOut(driver);*/
			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
			configurationPage.configurationPanel.saveSettings();
		} 
		catch (Exception e) {
			throw new Exception ("Exception at Utility.resetToDefaultLayout: "+e.getMessage(), e);
		}

	} //End function resetToDefaultLayout

	/**
	 * getCurrentDateTime : This method is to get current date and time
	 * @param None
	 * @return Current date and time in the string format
	 * @throws Exception
	 */
	public static String getCurrentDateTime() throws Exception {

		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			return(dateFormat.format(date));

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getCurrentDateTime : "+e.getMessage(), e);
		} //End catch	

	} //End getCurrentDateTime

	/**
	 * getObjectName : This method is creates name of the object with method & current time stamp
	 * @param None
	 * @return String with current method name and current time stamp
	 * @throws Exception
	 */
	public static String getObjectName(String methodName) throws Exception {

		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String dateTime = dateFormat.format(date);
			dateTime = dateTime.replace("/", "");
			dateTime = dateTime.replace(":", "");
			dateTime = dateTime.replace(" ", "-");
			String newMethodName = methodName.replace("Sprint", "") + "-" + dateTime;
			return(newMethodName);

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getObjectName : "+e.getMessage(), e);
		} //End catch	

	} //End getCurrentDateTime

	/**
	 * openSharedLinkPage : This method open public link URL in new page
	 * @param publicLink - Public link to be naviagated
	 * @return Instance of Webdriver
	 * @throws Exception
	 */
	public static WebDriver openSharedLinkPage(String publicLink) throws Exception {

		WebDriver driver = null;

		try {

			driver = WebDriverUtils.getDriver();
			Utils.fluentWait(driver);
			driver.get(publicLink);
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equals(publicLink))
				throw new Exception("Shared public link is not opened in new browser window.");

			return driver;

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.navigateToPage : "+e.getMessage(), e);
		} //End catch

	} //End function openSharedLinkPage

	/**
	 * openSharedLinkPage : This method open public link URL in new page
	 * @param publicLink - Public link to be naviagated
	 * @return Instance of Webdriver
	 * @throws Exception
	 */
	public static WebDriver openSharedLinkPage(String publicLink, WebDriver driver) throws Exception {

		try {

			driver.get(publicLink);
			Utils.fluentWait(driver);

			if (!driver.getCurrentUrl().equals(publicLink))
				throw new Exception("Shared public link is not opened in new browser window.");

			return driver;

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.navigateToPage : "+e.getMessage(), e);
		} //End catch

	} //End function openSharedLinkPage

	/**
	 * navigateToPage : This method navigates to the specified URL
	 * @param driver - Instance of the current web driver
	 * @param URL - URL to be navigated
	 * @return Instance of HomePage
	 * @throws Exception
	 */
	public static HomePage navigateToPage(WebDriver driver, String URL) throws Exception {

		try {

			driver.get(URL); //Launches with the URL

			if (browser.contains("firefox"))
				Thread.sleep(3000);

			Utils.fluentWait(driver);
			return (new HomePage(driver));		

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.navigateToPage : "+e.getMessage(), e);
		} //End catch

	} //End function navigateToPage

	/**
	 * navigateToPage : This method navigates to the specified URL
	 * @param driver - Instance of the current web driver
	 * @param URL - URL to be navigated
	 * @param userName - Name of the user to login
	 * @param password - password of the user
	 * @param vaultName - name of the vault to be navigated
	 * @return Instance of HomePage
	 * @throws Exception
	 */
	public static HomePage navigateToPage(WebDriver driver, String URL, String userName, String password, String vaultName) throws Exception {

		try {


			driver.get(URL); //Launches with the URL
			Thread.sleep(5000);
			Utils.fluentWait(driver);

			if (driver.getCurrentUrl().toUpperCase().contains("LOGIN.ASPX")) {
				LoginPage loginPage = new LoginPage(driver);
				return (loginPage.loginToWebApplication(userName, password, vaultName));
			}
			else 
				return (new HomePage(driver));		

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.navigateToPage : "+e.getMessage(), e);
		} //End catch

	} //End function navigateToPage

	/** 
	 * getBrowserName : This method is to get the name of the current executing browser
	 * @param driver
	 * @return Browser name with version
	 * @throws Exception
	 */
	public static String getBrowserName(WebDriver driver) throws Exception {

		//Variable Declaration
		String browserName = "";

		try {

			Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
			browserName = cap.getBrowserName();
			String browser_version = "";

			if (browserName.equalsIgnoreCase("INTERNET EXPLORER")) {
				String uAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");

				if (uAgent.contains("MSIE") && uAgent.contains("Windows")) 
					browser_version = uAgent.substring(uAgent.indexOf("MSIE") + 5, uAgent.indexOf("Windows") - 2);
				else if (uAgent.contains("Trident/7.0"))
					browser_version = "11.0";
				else
					browser_version = "0.0";

			}
			else
			{
				try{
					browser_version = cap.getCapability("browserVersion").toString();				
				}
				catch(Exception e0){browser_version = cap.getVersion();}
			}

			if (!browser_version.equals(""))
				browserName =  browserName + " " + browser_version.substring(0, browser_version.indexOf("."));

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getBrowserName : "+e.getMessage(), e);
		} //End catch	

		return browserName;

	} //End getBrowserName

	/** 
	 * getMethodName : This method is to get the name of the current executing method name
	 * @param driver
	 * @return Method Name
	 * @throws Exception
	 */
	public static String getMethodName() throws Exception {
		//Variable Declaration
		String methodName = "";
		try {

			methodName = Reporter.getCurrentTestResult().getMethod().getMethodName().toString().trim();
			return methodName;

		} //End try

		catch (Exception e) {
			throw new Exception("Exeception at Utility.getMethodName : " + e);
		} //End catch	

	} //End getMethodName

	/** 
	 * getMethodDescription : This method is to get the name of the current executing method name
	 * @param driver
	 * @return Method Name
	 * @throws Exception
	 */
	public static String getMethodDescription() throws Exception {
		//Variable Declaration
		String methodDescription = "";
		try {

			methodDescription = Reporter.getCurrentTestResult().getMethod().getDescription().toString().trim();
			return methodDescription;

		} //End try

		catch (Exception e) {
			throw new Exception("Exeception at Utility.getMethodDescription : " + e);
		} //End catch	

	} //End getMethodDescription

	/** 
	 * getClassName : This method is to get the name of the current executing method name
	 * @param driver
	 * @return Method Name
	 * @throws Exception
	 */
	public static String getClassName() throws Exception {
		//Variable Declaration
		String className = "";
		try {

			className = Reporter.getCurrentTestResult().getClass().getSimpleName().toString().trim();
			return className;

		} //End try

		catch (Exception e) {
			throw new Exception("Exeception at Utility.getClassName : " + e);
		} //End catch	

	} //End getMethodDescription

	/**
	 * getTextFromClipboard : This method gets the current text from clipboard
	 * @param None
	 * @return String stored in the clipboard
	 * @throws Exception
	 */
	public static String getTextFromClipboard() throws Exception {

		try {


			return(String) Toolkit.getDefaultToolkit()
					.getSystemClipboard().getData(DataFlavor.stringFlavor); 

		} 
		catch (Exception e) {
			throw new Exception ("Exception at Utility.getTextFromClipboard : "+e.getMessage(), e);
		}

	} //End function syncNavigation

	public static boolean selectTemplate(String temp, WebDriver driver) throws Exception {
		boolean value = false;
		try {
			if(temp == "" || temp == null)
				return value;

			WebElement selectObjTemplate = driver.findElement(By.cssSelector("iframe[src*='objecttemplateselector.html.aspx']"));
			driver.switchTo().frame(selectObjTemplate);

			driver.findElement(By.cssSelector("input[id='mf-template-filter']")).clear();
			driver.findElement(By.cssSelector("input[id='mf-template-filter']")).sendKeys(temp);
			Utils.fluentWait(driver);

			Utils.fluentWait(driver);
			List<WebElement> options = driver.findElements(By.cssSelector("div[class='mf-listing-content']>ul>li[class*='mf-file-item']:not([style*='display: none;'])"));

			for(int count = 0; count < options.size(); count++) {
				if(options.get(count).getAttribute("title").contains("(."+temp+")")) {
					ActionEventUtils.click(driver, options.get(count));
					//	options.get(count).click();
					value = true;
					break;
				}
			}

			if(!value) {
				for(int count = 0; count < options.size(); count++) {
					if(options.get(count).getAttribute("title").contains(temp)) {
						ActionEventUtils.click(driver, options.get(count));
						//options.get(count).click();
						value = true;
						break;
					}
				}
			}
			ActionEventUtils.click(driver, driver.findElement(By.cssSelector("div[id='mf-footer']>div[id='mf-buttons']>button[class*='mf-next-button']")));
			//driver.findElement(By.cssSelector("div[id='mf-footer']>div[id='mf-buttons']>button[class*='mf-next-button']")).click();
			Utils.fluentWait(driver);

			driver.switchTo().defaultContent();

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.selectTemplate : "+e.getMessage(), e);
		} //End catch
		return value;

	}

	public static boolean closeSelectTemplate(WebDriver driver) throws Exception {

		try {

			WebElement selectObjTemplate = driver.findElement(By.cssSelector("iframe[src*='objecttemplateselector.html.aspx']"));
			driver.switchTo().frame(selectObjTemplate);
			ActionEventUtils.click(driver, driver.findElement(By.cssSelector("button[class*='mf-cancel-button']")));
			//driver.findElement(By.cssSelector("button[class*='mf-cancel-button']")).click();
			driver.switchTo().defaultContent();
			Utils.fluentWait(driver);
			return true;


		}//End try
		catch (Exception e) {
			return false;
		} //End catch

	}

	public static boolean isSelectTemplateDisplayed(WebDriver driver) throws Exception {
		try {

			driver.findElement(By.cssSelector("iframe[src*='objecttemplateselector.html.aspx']"));
			return true;

		}//End try
		catch (Exception e) {
			return false;
		} //End catch

	}

	/**
	 * quitDriver : This method to quit the driver
	 * @param driver - Instance of the current web driver
	 * @return None
	 * @throws Exception
	 */
	public static void quitDriver(WebDriver driver) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (driver == null) 
				return;

			driver.switchTo().defaultContent();
			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
			try { Utility.logOut(driver); }  catch(Exception e0) {}
			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
			driver.quit();

			/*if(browser.equalsIgnoreCase("firefox")) {
				Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");
				Runtime.getRuntime().exec("taskkill /F /IM plugin-container.exe");
				Runtime.getRuntime().exec("taskkill /F /IM WerFault.exe");
			}
			else 
				driver.quit();*/

			try {
				/*if (browser.equalsIgnoreCase("ie")) {
					WindowsUtils.killByName("iexplore.exe");
					WindowsUtils.killByName("IEDriverServer.exe");
				}
				else */if(browser.equalsIgnoreCase("edge"))
				{
					WindowsUtils.killByName("MicrosoftEdgeCP.exe");
					WindowsUtils.killByName("MicrosoftEdge.exe");
					WindowsUtils.killByName("MicrosoftWebDriver.exe");
				}
			}
			catch (Exception e) { 	}

			String message = "Total time spent in fluent wait : " + Utils.snoozeTime + "</br>No of times fluent wait called : " + Utils.snoozeIdx;
			Log.event("Utility.quitDriver :" + message + "</br>Driver is quit.",StopWatch.elapsedTime(startTime));

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.quitDriver : "+e.getMessage(), e);
		} //End catch
		finally {
			if(driver != null)
				driver.quit();
		}//End finally

	} //End function quitDriver

	/**
	 * quitDriver : This method to quit the driver
	 * @param driver - Instance of the current web driver
	 * @param extentTest - Instance of the current extentTest
	 * @return None
	 * @throws Exception
	 */
	public static void quitDriver(WebDriver driver, ExtentTest extentTest) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (driver == null || extentTest == null) 
				return;

			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
			driver.switchTo().defaultContent();
			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
			try { Utility.logOut(driver); }  catch(Exception e0) {}
			try { Alert alert = driver.switchTo().alert(); alert.dismiss();	Utils.fluentWait(driver);} catch (Exception e0) {}
			driver.quit();

			/*if(browser.equalsIgnoreCase("firefox")) {
				Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");
				Runtime.getRuntime().exec("taskkill /F /IM plugin-container.exe");
				Runtime.getRuntime().exec("taskkill /F /IM WerFault.exe");
			}
			else 
				driver.quit();*/

			try {
				if (browser.equalsIgnoreCase("ie")) {
					WindowsUtils.killByName("iexplore.exe");
					WindowsUtils.killByName("IEDriverServer.exe");
				}
				else if(browser.equalsIgnoreCase("edge"))
				{
					WindowsUtils.killByName("MicrosoftEdgeCP.exe");
					WindowsUtils.killByName("MicrosoftEdge.exe");
					WindowsUtils.killByName("MicrosoftWebDriver.exe");
				}
			}
			catch (Exception e) { 	}

			String message = "Total time spent in fluent wait : " + Utils.snoozeTime + "</br>No of times fluent wait called : " + Utils.snoozeIdx;
			Log.event("Utility.quitDriver :" + message + "</br>Driver is quit.",StopWatch.elapsedTime(startTime));

		} //End try
		catch (Exception e) {
			throw new Exception("Exception at Utility.quitDriver : " + e);
		} //End catch
		finally {

			if(driver != null)
				driver.quit();
		}//End finally

	} //End function quitDriver

	/**
	 * refreshDriver : This method to refresh the driver
	 * @param driver - Instance of the current web driver
	 * @return None
	 * @throws Exception
	 */
	public static HomePage refreshDriver(WebDriver driver) throws Exception {

		try {

			driver.navigate().refresh();
			return (new HomePage(driver));

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.refreshDriver : "+e.getMessage(), e);
		} //End catch

	} //End function quitDriver

	/**
	 * readRegistry : This method is to read the value from registry
	 * @param regPath - Path of the registry command to read
	 * @param regName - Name of the registry command to read
	 * @return String value of the registry command
	 * @throws Exception
	 */
	public static String readRegistry(String regPath, String regName) throws Exception {

		try {

			String regValue = "";

			try {
				regValue = Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, regPath, regName);
			}
			catch (Exception e) { }

			if (regValue.equals(""))
				regValue = Integer.toString(Advapi32Util.registryGetIntValue(HKEY_LOCAL_MACHINE, regPath, regName));

			return regValue;

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.readRegistry : "+e.getMessage(), e);
		} //End catch

	} //End readRegistry

	/**
	 * compareObjects : This method is to compare two hashmap objects
	 * @param actual - The actual object to compare
	 * @param expected - The expected object to compare
	 * @return String value of difference between two objects
	 * @throws Exception
	 */
	public static String compareObjects(ConcurrentHashMap <String, String> actual, ConcurrentHashMap <String, String> expected) throws Exception {

		try {

			String diff = "";

			for (final String key : actual.keySet()) {
				if (!actual.get(key).equalsIgnoreCase(expected.get(key)))
					diff = diff + key + " : [D]" + actual.get(key) + "(actual)" + expected.get(key) + "(expected);\n";
			}

			return diff;

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.compareObjects : "+e.getMessage(), e);
		} //End catch

	} //End compareObjects

	/**
	 * compareObjects : This method is to compare two hashmap objects
	 * @param actual - The actual object to compare
	 * @param expected - The expected object to compare
	 * @param ignoreProps - Properties to be ignored
	 * @return String value of difference between two objects
	 * @throws Exception
	 */
	public static String compareObjects(ConcurrentHashMap <String, String> actual, ConcurrentHashMap <String, String> expected, String ignoreProps) throws Exception {

		try {

			String diff = "";

			for (final String key : actual.keySet()) {
				if(ignoreProps.contains(key))
					continue;
				if (!actual.get(key).equalsIgnoreCase(expected.get(key)))
					diff = diff + key + " : [D]" + actual.get(key) + "(actual)" + expected.get(key) + "(expected);\n";
			}

			return diff;

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.compareObjects : "+e.getMessage(), e);
		} //End catch

	} //End compareObjects

	/**
	 * configureHubAndNode: This method is used to launch the hub & node
	 * @throws Exception
	 */
	public static void configureHubAndNode() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			if (HubandNodeConfig.equals(""))
				HubandNodeConfig = test.getParameter("HubandNodeConfig");

			if (HubandNodeConfig.equalsIgnoreCase("YES") && !(test.getParameter("driverType").equalsIgnoreCase("safari"))) 
			{

				InetAddress ipAddr = InetAddress.getLocalHost();
				String localIPAddress = ipAddr.getHostAddress();

				Log.event("Utility.configureHubAndNode started to configure the Hub and Node in the machine:"+localIPAddress, StopWatch.elapsedTime(startTime));

				String deviceHost = test.getParameter("deviceHost");
				String devicePort = test.getParameter("devicePort");
				String driverType = test.getParameter("driverType");
				String hubURL = deviceHost+":"+devicePort;
				String driverPath = System.getProperty("user.dir")+ "\\Common\\drivers\\";
				String cmd = "";
				int maxInstance = 1;

				if (driverType.equalsIgnoreCase("Chrome") || driverType.equalsIgnoreCase("Firefox"))
					maxInstance = 5;

				if (driverType.equalsIgnoreCase("chrome"))
					driverPath += "chromedriver.exe";
				else if (driverType.equalsIgnoreCase("ie"))
					driverPath += "IEDriverServer.exe";

				if (driverType.equalsIgnoreCase("firefox"))
					cmd= "\""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\\Node-Firefox.bat\" \""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\" \""+hubURL+"\"";
				else
					cmd= "\""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\\Node.bat\" \""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\"  "+hubURL+" "+driverType+" "+maxInstance+" \""+driverPath+"\"";


				Utility.killExistingSeleniumprocess();//Calls the function to kill all the existing processes running on the machine

				Utility.killExistingBrowserDrivers();//Calls the function to kill all the existing browsers running on the machine

				//Launches the Hub if the devicehost is localhost
				//------------------------------------------------
				if (deviceHost.equalsIgnoreCase("localhost") || deviceHost.equalsIgnoreCase(localIPAddress)) {//Checks if Host is localhost or not
					Runtime.getRuntime().exec("\""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\\Hub.bat\" \""+System.getProperty("user.dir") + "\\Common\\Prerequsites\\HubAndNode\"  \""+devicePort+"\"");// Launches the Hub in the local machine
					Log.event("Utility.configureHubAndNode: Selenium Grid Hub launched in the localhost", StopWatch.elapsedTime(startTime));
				}

				//Launches the Node in the system
				//--------------------------------
				Runtime.getRuntime().exec(cmd);// Launches the node in the system
				Log.event("Utility.configureHubAndNode: Launched the Node in the machine:"+ localIPAddress, StopWatch.elapsedTime(startTime));

				int snooze = 0;
				while (snooze < 60 && !Utility.isHuborNodeStarted())
				{
					Thread.sleep(500);
					snooze++;
					Log.event("Utility.configureHubAndNode: Connecting Hub and Node...", StopWatch.elapsedTime(startTime));
				}

				//Checks if Hub and node are configured correctly
				//----------------------------------------------
				if (!Utility.isHuborNodeStarted())
					throw new SkipException("Utility.configureHubAndNode : Error while configure the Hub and node. (Hub/Node is not launched successfully)");

				Log.event("Utility.configureHubAndNode: Hub and Node connected successfully...", StopWatch.elapsedTime(startTime));

				HubandNodeConfig = "No";//Sets the nodeConfig to "NO" to prevent the configuration of Hub and node again

				Log.event("Utility.configureHubAndNode : Configured the Hub and Node in the machine:"+localIPAddress, StopWatch.elapsedTime(startTime));
			}

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.configureHubAndNode : "+e.getMessage(), e);
		} //End catch
	}//End configureHubAndNode

	/*
	 * killExistingSeleniumprocess: This function is used to kill the existing selenium processes in the environment
	 */
	public static void killExistingSeleniumprocess() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.killExistingSeleniumprocess: Started to kill the existing selenium server processes", StopWatch.elapsedTime(startTime));

			Process p = Runtime.getRuntime().exec("powershell.exe -version 2 \""+ System.getProperty("user.dir").replaceAll(" ", "` ") + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\SeleniumServerJavaProcessKill.PS1\"");//Kills all the existing Selenium server is launched in the systems

			p.getOutputStream().close();
			InputStream inputstream = p.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("Selenium server process") && snooze < 20) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Killing the Existing selenium server processes : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Existing Selenium server process killed successfully"))
				Log.event("Existing selenium server processes are killed successfully", StopWatch.elapsedTime(startTime));
			else if (line.contains("No Existing Selenium server process"))
				Log.event("No Existing selenium server processes to kill", StopWatch.elapsedTime(startTime));

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.killExistingSeleniumprocess : "+e.getMessage(), e);
		} //End catch

	}//End killExistingSeleniumprocess

	/*
	 * killExistingBrowserDrivers: This function is used to kill the existing browser drivers in the environment
	 */
	public static void killExistingBrowserDrivers() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			Utility.killExistingBrowser();//Kills the existing browsers launched browsers in the environment

			Log.event("Utility.killExistingBrowserDrivers: Started to kill the existing browser processes", StopWatch.elapsedTime(startTime));

			Process p = Runtime.getRuntime().exec("powershell.exe -version 2 \""+ System.getProperty("user.dir").replaceAll(" ", "` ") + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\BrowserDriverProcessKill.ps1\"");//Calls the powershell script to Kills all the existing browser processes is launched in the system

			p.getOutputStream().close();
			InputStream inputstream = p.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("Existing browser drivers closed successfully") && snooze < 20) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Killing the Existing browser driver processes : " + line, StopWatch.elapsedTime(startTime));       
			}

			Log.event("Existing browser driver processes are killed successfully", StopWatch.elapsedTime(startTime));

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.killExistingBrowserDrivers : "+e.getMessage(), e);
		} //End catch

	}//End killExistingBrowserDrivers

	/*
	 * killExistingBrowser: This function is used to kill the existing browsers in the environment
	 */
	public static void killExistingBrowser() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.killExistingBrowserDrivers: Started to kill the existing browser processes", StopWatch.elapsedTime(startTime));

			Process p = Runtime.getRuntime().exec("powershell.exe -version 2 \""+ System.getProperty("user.dir").replaceAll(" ", "` ") + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\BrowserProcessKill.ps1\"");//Calls the powershell script to Kills all the existing browsers launched in the system

			p.getOutputStream().close();
			InputStream inputstream = p.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("Existing browsers closed successfully") && snooze < 20) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Killing the Existing browser processes : " + line, StopWatch.elapsedTime(startTime));       
			}

			Log.event("Existing browser processes are killed successfully", StopWatch.elapsedTime(startTime));

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.killExistingBrowser : "+e.getMessage(), e);
		} //End catch

	}//End killExistingBrowser

	/**
	 * configureExtentXML : Update the field value in the xml
	 * @param xmlPath
	 * @param field
	 * @param value
	 * @throws Exception 
	 */
	public static void configureExtentXML(String xmlPath, String field, String value){

		final long startTime = StopWatch.startTime();

		try {

			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

			String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\GenerateExtentXML.PS1\" \"" + xmlPath.replaceAll(" " , "` ") + "\" \"" + field.replaceAll(" " , "` ") + "\" \"" + value.replaceAll(" " , "` ") +"\"";

			Process process = Runtime.getRuntime().exec(cmd);

			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 10 && !line.contains("XML Saved Successfully") && !line.contains("Exception")) {

				snooze++;
				Log.event("Utility.configureExtentXML : Updating the XML.. : "+line, StopWatch.elapsedTime(startTime));
				line = bufferedreader.readLine();
			}

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.configureExtentXML : Field("+ field +") is updated with the value("+ value +") in Extent Report Configuration File.", StopWatch.elapsedTime(startTime));


		} //End try
		catch (Exception e) {} //End catch

	} //End configureExtentXML

	/**
	 * installApplication : This method is to install M-Files Application through powershell script
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public static void installApplication() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			//Returns if isInstall in xml is selected as No
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isInstall = test.getParameter("isInstall");

			if (!installApp.equals("") || isInstall == null || isInstall.toUpperCase().equalsIgnoreCase("NO"))
				return;

			//Creating the required paramenters for installing build using powershell
			String productVersion = test.getParameter("productVersion");
			String buildPath = System.getProperty("user.dir") + "\\Common\\Prerequsites\\Powershell\\Build";
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\InstallBuild.ps1\"";

			//Copies build from project location to C:\Build path for easier installation
			File source = new File(buildPath);
			File dest = new File("C:\\Build");
			String[] myFiles;    
			if(dest.isDirectory()){
				myFiles = dest.list();
				for (int i=0; i < myFiles.length; i++) {
					File myFile = new File(dest, myFiles[i]);
					Log.event("Utility.installApplication : Started to delete the existing file("+ myFile.getName() +") in the C:/Build folder...", StopWatch.elapsedTime(startTime));
					myFile.delete();
				}
			}
			Log.event("Utility.installApplication : Existing files in the C:/Build folder is cleared...", StopWatch.elapsedTime(startTime));
			Log.event("Utility.installApplication : Started to copying the build from project location to local folder(C:/Build) for installation...", StopWatch.elapsedTime(startTime));
			FileUtils.copyDirectory(source, dest);
			Log.event("Utility.installApplication : Copied the execution build from project location to local folder(C:/Build)...", StopWatch.elapsedTime(startTime));
			buildPath = "C:\\Build";

			//Rename the installation file if the build is topic build
			//--------------------------------------------------------
			String build = "M-Files_x64_eng_";
			String[] product = productVersion.split("\\.");
			if(!product[product.length-1].equalsIgnoreCase("0"))
			{
				File renamedBuild = new File("C:\\Build\\" + build + productVersion.replaceAll("\\.", "_")+".msi");
				dest = new File("C:\\Build");
				myFiles = null;    
				if(dest.isDirectory()){
					myFiles = dest.list();
					for (int i=0; i < myFiles.length; i++) {
						File myFile = new File(dest, myFiles[i]);
						Log.event("Utility.installApplication : Started to delete the existing file("+ myFile.getName() +") in the C:/Build folder...", StopWatch.elapsedTime(startTime));
						if(myFile.getName().contains("M-Files"))
						{
							myFile.renameTo(renamedBuild);//Renames the file into the new name
							break;
						}
					}
				}
			}

			//Running powershell script for installation
			Log.event("Utility.installApplication : M-Files Build Installation is started to check the build compatability in the environment...", StopWatch.elapsedTime(startTime));
			String cmd="powershell.exe -version 2 " + powershellPath + " \"" + productVersion + "\" \"" + buildPath + "\"";
			Process process = Runtime.getRuntime().exec(cmd);

			//Reads output message from powershell script
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			//To decide if powershell script execution is completed successfully or with execution
			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("M-Files Build installation is in progress..." +  line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) //Checks if powershell execution had some exeception
				throw new Exception("Exception occured while installing the M-Files Application : " + line);

			if(process.isAlive()) //Destroys the powershell process if it is alive
				process.destroy();

			Utility.configureAdminUserAccount();//Configures system admin user details
			Utility.configureLicenseAndWebAccess(); //Configures License & web access


			Log.event("Utility.installApplication : M-Files Build Installation is done.", StopWatch.elapsedTime(startTime));

			installApp = "No";
			backup = "No";

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.installApplication :" + e, e);
		} //End catch

	} //End installApplication

	/**
	 * installVaultApplication : This method is to install the vault application in the test vault
	 * @param appName - The file name of the vault application
	 * @return None
	 * @throws Exception
	 */
	public static void installVaultApplication(String appName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isAppInstall = (test.getParameter("isAppInstall") != null) ? test.getParameter("isAppInstall") : "No";
			String vaultName = test.getParameter("VaultName");

			if (isAppInstall.equalsIgnoreCase("YES"))
			{
				if(appName.equals(null) || appName.equals(""))
					throw new Exception("Application name is empty or null. Please check the same in XML test.");

				//Creating the required paramenters for installing build using powershell
				String appPath = System.getProperty("user.dir") + "\\Common\\Prerequsites\\Powershell\\ExternalApplication\\"+appName;
				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
				String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\InstallVaultApplication.ps1\"";

				//Running powershell script for installation
				Log.event("Utility.installApplication : M-Files vault Application('" + appName + "') installation in the Vault: " + vaultName + " is started......", StopWatch.elapsedTime(startTime));
				String cmd="powershell.exe -version 2 " + powershellPath + " \"" + vaultName.replaceAll(" ", "` ") + "\" \"" + appPath.replaceAll(" ", "` ") + "\"";
				Process process = Runtime.getRuntime().exec(cmd);

				//Reads output message from powershell script
				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				//To decide if powershell script execution is completed successfully or with execution
				while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event("M-Files vault application('" + appName + "') installation is in progress..." +  line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) //Checks if powershell execution had some exeception
					throw new Exception("Exception occured while installing the M-Files Application('" + appName + "') in the vault('" + vaultName + "') : " + line);

				if(process.isAlive()) //Destroys the powershell process if it is alive
					process.destroy();

				Log.event("Utility.installVaultApplication : M-Files vault application('" + appName + "') installation is done for " + vaultName , StopWatch.elapsedTime(startTime));
			}
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.installVaultApplication :" + e.getMessage(), e);
		} //End catch

	} //End installVaultApplication

	/**
	 * setExternalRepository : This method is to set the external repository of the test class
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public static void setExternalRepository(String repoName, String testClassName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isAppInstall = (test.getParameter("isAppInstall") != null) ? test.getParameter("isAppInstall") : "No";

			if (isAppInstall.equalsIgnoreCase("YES"))
			{

				//Creating the required paramenters for installing build using powershell
				String UserDir= System.getProperty("user.dir");
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				String systemName = localMachine.getHostName();
				String srcFolderPath = "\\\\" + systemName + "\\" + UserDir.replace(":", "$").replace("\\", "\\\\") + "\\Common\\Prerequsites\\ExternalRepositories\\"+repoName;
				String sharedFolderPath = "\\\\" + systemName + "\\" + UserDir.replace(":", "$").replace("\\", "\\\\") + "\\ExternalRepository\\"+testClassName;
				File source = new File(srcFolderPath);
				File dest = new File(sharedFolderPath);

				//Copies the files to the External Repository location
				//----------------------------------------------------
				FileUtils.copyDirectory(source, dest);

				Log.event("Utility.setExternalRepository : .", StopWatch.elapsedTime(startTime));
			}
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.setExternalRepository :" + e, e);
		} //End catch

	} //End setExternalRepository

	/**
	 * clearExternalRepository : This method is to clear the external repository set for the testclass
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public static void clearExternalRepository(String testClassName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isAppInstall = (test.getParameter("isAppInstall") != null) ? test.getParameter("isAppInstall") : "No";

			if (isAppInstall.equalsIgnoreCase("YES"))
			{
				Log.event("Utility.clearExternalRepository : Started to clear the external repository of test class('" + testClassName + "')...", StopWatch.elapsedTime(startTime));

				//Creating the required paramenters for installing build using powershell
				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				String systemName = localMachine.getHostName();
				String sharedFolderPath = "\\\\" + systemName + "\\" + UserDir.replace(":", "$") + "\\ExternalRepository\\"+testClassName;
				File source = new File(sharedFolderPath);

				//Delete the directory
				//---------------------
				FileUtils.deleteDirectory(source);

				Log.event("Utility.clearExternalRepository :Cleared the external repository of test class('" + testClassName + "')...", StopWatch.elapsedTime(startTime));;
			}
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.clearExternalRepository :" + e, e);
		} //End catch

	} //End clearExternalRepository

	/**
	 * setFileShareNamedValue : This method is to set the named value for installed file share application
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public static void setFileShareNamedValue(String testClassName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isAppInstall = (test.getParameter("isAppInstall") != null) ? test.getParameter("isAppInstall") : "No";
			String vaultName = test.getParameter("VaultName");
			String displayName = test.getParameter("ExternalRepositoryDisplayName");
			String indexerAndCommonUsername = test.getParameter("ExternalRepositoryIndexerAndCommonUsername");
			String indexerAndCommonUserPassword = test.getParameter("ExternalRepositoryIndexerAndCommonUserPassword");
			String authType = test.getParameter("ExternalRepositoryAuthenticationType");

			if (isAppInstall.equalsIgnoreCase("YES"))
			{
				//Creating the required paramenters for installing build using powershell
				String UserDir= System.getProperty("user.dir");
				//java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				//String systemName = localMachine.getHostName();
				//String sharedFolderPath = "\\\\" + systemName + "\\" + UserDir.replace(":", "$") + "\\ExternalRepository\\"+testClassName;
				String sharedFolderPath = UserDir.replace("\\", "\\\\") + "\\\\ExternalRepository\\\\"+testClassName;

				String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\ConfigureNetworkFolderConnectorToVault.ps1\"";

				Log.event("Utility.setFileShareNamedValue : Started to set the Named Value for file share application in the vault('" + vaultName + "')...", StopWatch.elapsedTime(startTime));

				//Running powershell script for installation
				//String cmd="powershell.exe -version 2 " + powershellPath.replaceAll(" " , "` ") + " \"" + vaultName.replaceAll(" " , "` ") + "\" \"" + sharedFolderPath.replaceAll(" " , "` ") + "\" \"" + authType.replaceAll(" " , "` ") + "\" \"" + displayName.replaceAll(" " , "` ") + "\" \"" + indexerAndCommonUsername.replaceAll(" " , "` ") + "\" \"" + indexerAndCommonUserPassword.replaceAll(" " , "` ") + "\"";

				//The script does not seem compatible with powershell v2
				String cmd="powershell.exe " + powershellPath.replaceAll(" " , "` ") + " \"" + vaultName.replaceAll(" " , "` ") + "\" \"" + sharedFolderPath.replaceAll(" " , "` ") + "\" \"" + authType.replaceAll(" " , "` ") + "\" \"" + displayName.replaceAll(" " , "` ") + "\" \"" + indexerAndCommonUsername.replaceAll(" " , "` ") + "\" \"" + indexerAndCommonUserPassword.replaceAll(" " , "` ") + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				//Reads output message from powershell script
				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				//To decide if powershell script execution is completed successfully or with execution
				while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					System.out.println(line);
					Log.event("Setting the named values for file share application is in progress..." +  line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) //Checks if powershell execution had some exeception
					//throw new Exception("Exception occured while setting the Named values : " + line);

					if(process.isAlive()) //Destroys the powershell process if it is alive
						process.destroy();

				Log.event("Utility.setFileShareNamedValue : Named values set for the File Share application in the vault('" + vaultName + "').", StopWatch.elapsedTime(startTime));

				//Waiting 60 seconds for indexing the repository and other possible automatic setups
				System.out.println("Waiting for 60 seconds so that external repository indexing is done and users and user groups are synced.");
				Thread.sleep(60000);
				System.out.println("60 seconds wait is over.");

				//Perform Re-Build search operation on the vault
				/*System.out.println("Search re-build operation starts...");
				Utility.reBuildSearch();//Performs the search rebuild operation
				System.out.println("Search re-build operation completed...");*/
			}
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.setFileShareNamedValue :" + e.getMessage(), e);
		} //End catch

	} //End setFileShareNamedValue

	public static void setFullControlToExternalRepoWindowsACLPermission(String testClassName) throws Exception{

		try{

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			Boolean setFullControl = false;

			String enabledParameter = test.getParameter("enableFullControlToExternalRepositoryFolder");

			if(enabledParameter != null && enabledParameter.equalsIgnoreCase("Yes"))
				setFullControl = true;

			if(!setFullControl){
				System.out.println("Not setting full control to the external repository of '" + testClassName + "' test class. Setting full control ACL was not enabled in XML parameter 'EnableFullControlToExternalRepositoryFolder'");
				return;
			}

			ArrayList<String> extRepoUsers = new ArrayList<String>();

			String indexerUser = test.getParameter("ExternalRepositoryIndexerAndCommonUsername");
			String testUser = test.getParameter("ExternalRepositoryTestUserUsername");

			if(indexerUser != null)
				extRepoUsers.add(indexerUser);

			if(testUser != null && !testUser.equals(indexerUser)){
				//Adding testUser to user list only if it differs from the indexer user
				extRepoUsers.add(testUser);
			}

			if(extRepoUsers.isEmpty()){
				System.out.println("No external repository users were defined for '" + testClassName + "' test class. Cannot set full control to any test users");
				return;
			}

			for(int i = 0; i < extRepoUsers.size(); ++i){

				String UserDir = System.getProperty("user.dir");

				String fullFilePathToExtRepoFolder = UserDir + "\\ExternalRepository\\" + testClassName;
				String userName = extRepoUsers.get(i);
				String permission = "FullControl";
				String allowOrDeny = "Allow";

				String[] scriptParameters = new String[]{fullFilePathToExtRepoFolder, userName, permission, allowOrDeny};

				String scriptName = "SetWindowsACLPermission.ps1";

				runPowershellScript(scriptName, scriptParameters);
			}
		}
		catch(Exception e){
			throw new Exception("Exception at Utility.setFullControlToExternalRepoWindowsACLPermission :" + e.getMessage(), e);
		}

	}

	public static void setWindowsACLPermission(String testClassName, String workBook) throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			Boolean setPermissionsEnabled = false;

			String setPermissionsParameter = test.getParameter("enableSetupACLPermissions");

			if(setPermissionsParameter != null && setPermissionsParameter.equalsIgnoreCase("Yes"))
				setPermissionsEnabled = true;

			if(!setPermissionsEnabled){
				System.out.println("Not setting custom ACL permissions to individual files and folders in the external repository of '" + testClassName + "' test class. Setup was not enabled in parameter 'enableSetupACLPermissions'");
				return;
			}


			String UserDir = System.getProperty("user.dir");

			File dir = new File(".");

			String strBasePath = dir.getCanonicalPath();
			String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
					+ workBook;

			FileInputStream file = new FileInputStream(new File(inputFile));

			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet("FilePermissions");

			//Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();

			if(rowIterator.hasNext()) {

				//Skipping the first title row
				Row row = rowIterator.next();

				while(rowIterator.hasNext()) {

					row = rowIterator.next();

					List<String> values = new ArrayList<String>();

					//For each row, iterate through each columns
					Iterator<Cell> cellIterator = row.cellIterator();
					while(cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						values.add(cell.getStringCellValue());
					}

					//String relativeFilePath = "sub1\\sub1_1\\Unmanaged1.pdf";
					String relativeFilePath = values.get(1);
					String fullFilePath = UserDir + "\\ExternalRepository\\" + testClassName + "\\" + relativeFilePath;

					String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\SetWindowsACLPermission.ps1\"";

					String username = test.getParameter("ExternalRepositoryTestUserUsername");

					//String permission = "Write";
					String permission = values.get(2);

					//String allowOrDeny = "Deny";
					String allowOrDeny = values.get(3);

					String cmd="powershell.exe " + powershellPath.replaceAll(" " , "` ") + " \"" + fullFilePath.replaceAll(" " , "` ") + "\" \"" + username.replaceAll(" " , "` ") + "\" \"" + permission.replaceAll(" " , "` ") + "\" \"" + allowOrDeny.replaceAll(" " , "` ") + "\"";

					System.out.println("Setting Windows ACL with details: '" + username +  " " + permission + " " + allowOrDeny + " " + relativeFilePath + "'" );

					Process process = Runtime.getRuntime().exec(cmd);

					//Reads output message from powershell script
					process.getOutputStream().close();
					InputStream inputstream = process.getInputStream();
					InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
					BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
					String line = "";

					int snooze = 0;

					//To decide if powershell script execution is completed successfully or with execution
					while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
						snooze++;
						line = bufferedreader.readLine();
						Log.event("Setting Windows ACL permission in progress.." +  line, StopWatch.elapsedTime(startTime));       
					}

					if (line.contains("Exception")) //Checks if powershell execution had some exeception
						//throw new Exception("Exception occured while setting the Named values : " + line);

						if(process.isAlive()) //Destroys the powershell process if it is alive
							process.destroy();

				}
			}

		}
		catch(Exception e){
			throw new Exception("Exception at Utility.setWindowsACLPermission :" + e.getMessage(), e);
		}

	}


	public static void setupPromoteObjects(String workBook) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String promoteEnabled = test.getParameter("enableSetupPromoteObjects");

			if(promoteEnabled == null || promoteEnabled.equalsIgnoreCase("No")){
				System.out.println("Utility.setupPromoteObjects not enabled in xml.");
				return;
			}

			String UserDir = System.getProperty("user.dir");

			File dir = new File(".");

			String strBasePath = dir.getCanonicalPath();
			String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
					+ workBook;

			FileInputStream file = new FileInputStream(new File(inputFile));

			HSSFWorkbook workbook = new HSSFWorkbook(file);
			HSSFSheet sheet = workbook.getSheet("SetupPromoteObjects");

			//Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();

			if(rowIterator.hasNext()) {

				//Skipping the first title row
				Row row = rowIterator.next();

				while(rowIterator.hasNext()) {

					row = rowIterator.next();

					List<String> values = new ArrayList<String>();

					//For each row, iterate through each columns
					Iterator<Cell> cellIterator = row.cellIterator();
					while(cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						values.add(cell.getStringCellValue());
					}

					String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\PromoteObject.ps1\"";

					String vaultName = test.getParameter("VaultName");
					String repoUsername = test.getParameter("ExternalRepositoryIndexerAndCommonUsername");
					String repoPassword = test.getParameter("ExternalRepositoryIndexerAndCommonUserPassword");

					String mfilesUsername = test.getParameter("SetupPromotionUserUsername");
					String mfilesPassword = test.getParameter("SetupPromotionUserPassword");


					String objTitle = values.get(1);

					String classID = values.get(2);

					String objFilePath = values.get(3);

					String isFolder = values.get(4);

					String lookupPropertyID = values.get(5);
					String lookupPropertyValueID = values.get(6);

					String cmd="powershell.exe " + powershellPath.replaceAll(" " , "` ") + " \"" + vaultName.replaceAll(" " , "` ") + "\" \"" + mfilesUsername.replaceAll(" " , "` ") + "\" \"" + mfilesPassword.replaceAll(" " , "` ") + "\" \"" + repoUsername.replaceAll(" " , "` ") + "\" \"" + repoPassword.replaceAll(" " , "` ") + "\" \"" + objTitle.replaceAll(" " , "` ") + "\" \"" + objFilePath.replaceAll(" " , "` ") + "\" \"" + isFolder.replaceAll(" " , "` ") + "\" " + classID + " " + lookupPropertyID + " " + lookupPropertyValueID;

					System.out.println("Utility.setupPromoteObjects: Promoting object '" + objFilePath + "'");

					Process process = Runtime.getRuntime().exec(cmd);

					//Reads output message from powershell script
					process.getOutputStream().close();
					InputStream inputstream = process.getInputStream();
					InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
					BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
					String line = "";

					int snooze = 0;

					//To decide if powershell script execution is completed successfully or with execution
					while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
						snooze++;
						line = bufferedreader.readLine();
						//System.out.println("Script output:" + line);
						Log.event("Promoting object in progress.." +  line, StopWatch.elapsedTime(startTime));       
					}

					if (line.contains("Exception")) //Checks if powershell execution had some exeception
						//throw new Exception("Exception occured while setting the Named values : " + line);

						if(process.isAlive()) //Destroys the powershell process if it is alive
							process.destroy();

				}
			}
		}
		catch(Exception e){
			throw new Exception("Exception at Utility.setupPromoteObjects :" + e.getMessage(), e);
		}
	}

	/**
	 * reBuildSearch: This method is used to perform search re-build operation using M-Files as
	 * @throws Exception
	 */
	public static void reBuildSearch() throws Exception{

		try{

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String vaultName = test.getParameter("VaultName");

			String[] scriptParameters = new String[]{vaultName};

			String scriptName = "RebuildSearch.ps1";

			runPowershellScript(scriptName, scriptParameters);
		}
		catch(Exception e){
			throw new Exception("Exception at Utility.reBuildSearch :" + e.getMessage(), e);
		}
	}

	public static void configureMetadataProvider(String metadataProviderName, String configurationFile) throws Exception{

		try{
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String vaultName = test.getParameter("VaultName");


			File dir = new File(".");
			String strBasePath = dir.getCanonicalPath();
			String configFilePath = strBasePath + File.separator + "Common" + File.separator + "Prerequsites" + File.separator + "Powershell" + File.separator + "ExternalApplication" +
					File.separator + "Configurations" + File.separator + configurationFile;

			String[] scriptParameters = new String[]{vaultName, metadataProviderName, configFilePath};

			String scriptName = "ConfigureMetadataProviderToVault.ps1";

			runPowershellScript(scriptName, scriptParameters);
		}
		catch(Exception e){
			throw new Exception("Exception at Utility.configureMetadataProvider :" + e.getMessage(), e);
		}
	}

	public static void runPowershellScript(String scriptName, String[] scriptParameters) throws Exception{

		final long startTime = StopWatch.startTime();

		try{

			String UserDir = System.getProperty("user.dir");

			String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\"+ scriptName +"\"";

			//TODO: What if we have to use powershell version 2??? Maybe some parameter to this method?
			String cmd = "powershell.exe " + powershellPath.replaceAll(" " , "` ");

			for(int i = 0; i < scriptParameters.length; ++i){
				cmd += " \"";
				cmd += scriptParameters[i].replaceAll(" " , "` ");
				cmd += "\"";
			}

			Process process = Runtime.getRuntime().exec(cmd);

			//Reads output message from powershell script
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			//To decide if powershell script execution is completed successfully or with execution
			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				//System.out.println("Script output:" + line);
				Log.event("Script in progress: " +  line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) //Checks if powershell execution had some exeception
				throw new Exception("Exception occured while running the script : " + line);

			if(process.isAlive()) //Destroys the powershell process if it is alive
				process.destroy();
		}
		catch(Exception e){
			throw new Exception("Exception at Utility.runPowershellScript :" + e.getMessage(), e);
		}
	}

	//To share the folder
	public static void shareFolder() throws Exception{

		try{

			String drivePath = System.getProperty("user.dir") + "\\Common\\Share";
			String yourCommand = "net share sharefolder= " + drivePath + "/GRANT:merlinqa,FULL";
			Process p = Runtime.getRuntime().exec(yourCommand);
			p.getOutputStream().close();

		}			
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * configureLicenseAndWebAccess : This method is to configure license & web access to default site through powershell script
	 * @param None
	 * @return None
	 * @throws Exception
	 */
	public static void configureLicenseAndWebAccess() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			//Creating the required paramenters for installing build using powershell
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String licensePath = UserDir + "\\Common\\Prerequsites\\License\\License.txt";

			String powershellPath = "\"" + UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\License_WebConfigure.ps1\"";

			Log.event("Utility.configureLicenseAndWebAccess : Started to configure the License and web access in the MFServer...", StopWatch.elapsedTime(startTime));

			//Running powershell script for installation
			String cmd="powershell.exe -version 2 " + powershellPath + " \"" + licensePath + "\"";
			Process process = Runtime.getRuntime().exec(cmd);

			//Reads output message from powershell script
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			//To decide if powershell script execution is completed successfully or with execution
			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Configuring License & Webaccess is in progress..." +  line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) //Checks if powershell execution had some exeception
				throw new Exception("Exception occured while installing the M-Files Application : " + line);

			if(process.isAlive()) //Destroys the powershell process if it is alive
				process.destroy();

			Log.event("Utility.configureLicenseAndWebAccess : License is installed and web access is configured to default site.", StopWatch.elapsedTime(startTime));

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.configureLicenseAndWebAccess :" + e, e);
		} //End catch

	} //End configureLicenseAndWebAccess

	/**
	 * backupTestVault : This method is to backup the test vault
	 * @return None
	 * @throws Exception
	 */
	public static void backupTestVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			if (backup.equals(""))
				backup = test.getParameter("isBackup");

			String backupVault = test.getParameter("BackupVault");

			String productVersion = test.getParameter("productVersion");

			if (backupVault == null || backupVault.isEmpty())
				backupVault = "Sample` Vault";
			else
				backupVault = backupVault.replaceAll(" ", "` ");

			if (backup.equalsIgnoreCase("YES")) 
			{	
				if (productVersion.contains("11.2."))
					productVersion = "11.2";
				else if (productVersion.contains("12.0."))
					productVersion = "12.0";
				else if (productVersion.contains("11.3"))
					productVersion = "11.3";
				else
					throw new Exception("Please check the mentioned productversion("+productVersion+") in the xml. Mentioned product vault branch is not available in the vault directory");

				Log.event("Utility.backupTestVault : " + backupVault.replaceAll("` ", " ") + " backup is started.", StopWatch.elapsedTime(startTime));

				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\BackupVaults.PS1\" \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\VaultBackup\\" + productVersion + "\\" + backupVault + ".mfb\" \""+backupVault+"\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(backupVault.replaceAll("` ", " ") + " Backup Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while backup a vault : " + line);

				if(process.isAlive())
					process.destroy();

				backup = "No";

				/* Map<String, String> map = new HashMap<String, String>();
				map.put("isBackup", "No");
				test.setParameters(map);*/

				Log.event("Utility.backupTestVault : "+ backupVault.replaceAll("` ", " ") + " backup is done.", StopWatch.elapsedTime(startTime));
			}

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.backupTestVault : "+e.getMessage(), e);
		} //End catch

	} //End backupTestVault

	/**
	 * getVaultGUID : This method is to get the GUID of the document vault through Powershell script using M-Files API
	 * @return None
	 * @throws Exception
	 */
	public static String getVaultGUID(String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.getVaultGUID : Started to get the " + vaultName.replaceAll("` ", " ") + "GUID.", StopWatch.elapsedTime(startTime));

			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

			String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\GetVaultGUID.PS1\" \""+vaultName.replaceAll(" " , "` ")+"\"";

			Process process = Runtime.getRuntime().exec(cmd);

			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;
			String GUID = "";

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				if (line.contains(vaultName+" GUID:")){
					GUID = line.split(":")[1].trim();
					break;
				}
				Log.event("Getting " + vaultName + " GUID is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while getting vault GUID : " + line);

			if(process.isAlive())
				process.destroy();

			return GUID;//Retunrs the vault GUID

		} //End try
		catch (Exception e) {
			return "";
		} //End catch
		finally
		{
			Log.event("Utility.getVaultGUID : "+vaultName + " GUID is got.", StopWatch.elapsedTime(startTime));
		}//End Finally

	} //End getVaultGUID

	/**
	 * AttachOrRestoreTestVault : This method is to AttachOrRestore the test vault
	 * @return None
	 * @throws Exception
	 */
	public static void AttachOrRestoreTestVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String productVersion = test.getParameter("productVersion");
			String mainDataFolderStoragePath = test.getParameter("mainDataFolderStoragePath");
			String mainDataFolderUsagePath = test.getParameter("mainDataFolderUsagePath");

			String sqlBackupStorageFolderPath = test.getParameter("sqlBackupStorageFolderPath");
			String sqlBackupTempFolder = test.getParameter("sqlBackupTempFolder");
			String databaseType = test.getParameter("Database");

			if(databaseType == null)
				databaseType = "fb";

			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String vaultName = test.getParameter("VaultName").replaceAll(" " , "` ");

			String productVersionShort = "";

			if (productVersion.contains("11.2."))
				productVersionShort = "11.2";
			else if (productVersion.contains("12.0."))
				productVersionShort = "12.0";
			else if (productVersion.contains("11.3"))
				productVersionShort = "11.3";
			else
				throw new Exception("Please check the mentioned productversion("+productVersionShort+") in the xml. Mentioned product vault branch is not available in the vault directory");

			Log.event("Utility.AttachOrRestoreTestVault :" + test.getParameter("VaultName") + " restore is started.", StopWatch.elapsedTime(startTime));

			String restoreVault = test.getParameter("RestoreVault");

			if(restoreVault == null || restoreVault.isEmpty())
				restoreVault = "Sample` Vault";
			else
				restoreVault = restoreVault.replaceAll(" ", "` ");

			//Performs the attach/restore operation using powershell
			//------------------------------------------------------
			vaultName = "\"" + vaultName + ":"+restoreVault + ":" + databaseType + "\"";
			String powershellScript = "\""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AttachOrRestoreVaults.ps1\"";
			String backupPath = "\""+ UserDir + "\\Common\\Prerequsites\\Powershell\\VaultBackup\\" + productVersionShort + "\"";			

			String cmd="powershell.exe -version 2 " + powershellScript + " "+vaultName + " " + backupPath + " \"" + mainDataFolderStoragePath + "\" \"" + mainDataFolderUsagePath +"\" \"" + sqlBackupStorageFolderPath + "\" \"" + sqlBackupTempFolder + "\" \"" + productVersion + "\"";

			//String cmd="powershell.exe " + powershellScript + " "+vaultName + " " + backupPath + " \"" + mainDataFolderStoragePath + "\" \"" + mainDataFolderUsagePath +"\" \"" + sqlBackupStorageFolderPath + "\" \"" + sqlBackupTempFolder + "\"";


			Process process = Runtime.getRuntime().exec(cmd);

			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 20 && !line.toLowerCase().contains("completed") && !line.toLowerCase().contains("exception")) {
				snooze++;
				line = bufferedreader.readLine();
				System.out.println(line);
				Log.event(test.getParameter("VaultName") + " Attach/Restore Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.toLowerCase().contains("exception")) 
				throw new Exception("Exception occured while Attach/Restore a vault : " + line);

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.AttachOrRestoreTestVault :" + test.getParameter("VaultName") +" is Attached/Restored.", StopWatch.elapsedTime(startTime));

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.AttachOrRestoreTestVault :"+e.getMessage(), e);
		} //End catch

	} //End AttachOrRestoreTestVault

	/**
	 * restoreTestVault : This method is to restore the test vault
	 * @return None
	 * @throws Exception
	 */
	public static void restoreTestVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isRestore = (test.getParameter("isRestore") != null) ? test.getParameter("isRestore") : "No";
			String migrateToSQL = (test.getParameter("migrateToSQL") != null) ? test.getParameter("migrateToSQL") : "No";
			String isAttach = (test.getParameter("isAttach") != null) ? test.getParameter("isAttach") : "No";
			String productVersion = test.getParameter("productVersion");
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String vaultName = test.getParameter("VaultName").replaceAll(" " , "` ");

			if (isRestore.equalsIgnoreCase("YES")) 
			{
				if (!test.getParameter("HubandNodeConfig").equalsIgnoreCase("Yes"))
					if (!Utility.isHuborNodeStarted())
						throw new SkipException("Hub is not started or down.");

				if (productVersion.contains("11.2."))
					productVersion = "11.2";
				else if (productVersion.contains("12.0."))
					productVersion = "12.0";
				else if (productVersion.contains("11.3"))
					productVersion = "11.3";
				else
					throw new Exception("Please check the mentioned productversion("+productVersion+") in the xml. Mentioned product vault branch is not available in the vault directory");

				Log.event("Utility.restoreTestVault :" + test.getParameter("VaultName") + " restore is started.", StopWatch.elapsedTime(startTime));

				String restoreVault = test.getParameter("RestoreVault");

				if(restoreVault == null || restoreVault.isEmpty())
					restoreVault = "Sample` Vault";
				else
					restoreVault = restoreVault.replaceAll(" " , "` ");

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\RestoreVaults.PS1\" \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\VaultBackup\\" + productVersion + "\\"+ restoreVault +".mfb\" \"" + vaultName + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName.replaceAll("` " , " ") + " Restore Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while restore a vault('" + vaultName.replaceAll("` " , " ") + "') : " + line);

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.restoreTestVault :" + vaultName.replaceAll("` " , " ") +" is restored.", StopWatch.elapsedTime(startTime));

				if(migrateToSQL.equalsIgnoreCase("Yes"))
					Utility.migrateVaultToSqlServer();
			}
			else if(isRestore.equalsIgnoreCase("NO") && isAttach.equalsIgnoreCase("YES"))
				Utility.AttachOrRestoreTestVault();

		} //End try
		catch (Exception e) {
			if (e instanceof SkipException)
				throw new SkipException(e.getMessage());
			else
				throw new Exception ("Exception at Utility.restoreTestVault :"+e.getMessage(), e);
		} //End catch

	} //End restoreTestVault


	/**
	 * migrateVaultToSqlServer : This method is to migrate the test vault to sql server
	 * @return None
	 * @throws Exception
	 */
	public static void migrateVaultToSqlServer() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String migrateToSQL = (test.getParameter("migrateToSQL") != null) ? test.getParameter("migrateToSQL") : "No";
			String sqlServerName = test.getParameter("sqlServerName").replaceAll(" " , "` ");
			String sqlServerUserName = test.getParameter("sqlServerUserName").replaceAll(" " , "` ");
			String sqlServerPassword = test.getParameter("sqlServerPassword").replaceAll(" " , "` ");
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String vaultName = test.getParameter("VaultName").replaceAll(" " , "` ");

			if (migrateToSQL.equalsIgnoreCase("YES")) 
			{
				Log.event("Utility.migrateVaultToSqlServer :" + test.getParameter("VaultName") + " migration to sql server is started.", StopWatch.elapsedTime(startTime));

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\MigrateVaultToSQL.ps1\" \""+ vaultName + "\" \"" + sqlServerUserName + "\" \""+ sqlServerPassword +"\" \"" + sqlServerName + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName.replaceAll("` " , " ") + " Migrate Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while migrating a vault('" + vaultName.replaceAll("` " , " ") + "') : " + line);

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.migrateVaultToSqlServer :" + vaultName.replaceAll("` " , " ") +" is restored.", StopWatch.elapsedTime(startTime));

			}//End if
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.migrateVaultToSqlServer :"+e.getMessage(), e);
		} //End catch

	} //End migrateVaultToSqlServer

	/**
	 * migrateVaultToSqlServer : This method is to migrate the test vault to sql server
	 * @return None
	 * @throws Exception
	 */
	public static void migrateVaultToSqlServer(String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String migrateToSQL = (test.getParameter("migrateToSQL") != null) ? test.getParameter("migrateToSQL") : "No";
			String sqlServerName = test.getParameter("sqlServerName").replaceAll(" " , "` ");
			String sqlServerUserName = test.getParameter("sqlServerUserName").replaceAll(" " , "` ");
			String sqlServerPassword = test.getParameter("sqlServerPassword").replaceAll(" " , "` ");
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

			if (migrateToSQL.equalsIgnoreCase("YES")) 
			{
				Log.event("Utility.migrateVaultToSqlServer :" + test.getParameter("VaultName") + " migration to sql server is started.", StopWatch.elapsedTime(startTime));

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\MigrateVaultToSQL.ps1\" \""+ vaultName.replaceAll(" " , "` ") + "\" \"" + sqlServerUserName + "\" \""+ sqlServerPassword +"\" \"" + sqlServerName + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName.replaceAll("` " , " ") + " Migrate Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while migrating a vault('" + vaultName.replaceAll("` " , " ") + "') : " + line);

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.migrateVaultToSqlServer :" + vaultName.replaceAll("` " , " ") +" is restored.", StopWatch.elapsedTime(startTime));

			}//End if
		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.migrateVaultToSqlServer :"+e.getMessage(), e);
		} //End catch

	} //End migrateVaultToSqlServer


	/**
	 * restoreTestVault : This method is to restore the test vault
	 * @param vaultName: vaultName to be restored
	 * @return None
	 * @throws Exception
	 */
	public static void restoreTestVault(String vaultName, String restoreVault) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isRestore = test.getParameter("isRestore");
			String productVersion = test.getParameter("productVersion");
			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");
			String migrateToSQL = (test.getParameter("migrateToSQL") != null) ? test.getParameter("migrateToSQL") : "No";

			if (isRestore.equalsIgnoreCase("YES")||isRestore.equalsIgnoreCase("Teamcity")) 
			{
				if (!test.getParameter("HubandNodeConfig").equalsIgnoreCase("Yes"))
					if (!Utility.isHuborNodeStarted())
						throw new SkipException("Hub is not started or down.");

				if (productVersion.contains("11.2."))
					productVersion = "11.2";
				else if (productVersion.contains("12.0."))
					productVersion = "12.0";
				else if (productVersion.contains("11.3."))
					productVersion = "11.3";
				else
					throw new Exception("Please check the mentioned productversion("+productVersion+") in the xml. Mentioned product vault branch is not available in the vault directory");

				Log.event("Utility.restoreTestVault :" + vaultName + " restore is started.", StopWatch.elapsedTime(startTime));

				if(restoreVault == null || restoreVault.isEmpty())
					restoreVault = "Sample` Vault";
				else
					restoreVault = restoreVault.replaceAll(" " , "` ");

				vaultName = vaultName.replaceAll(" " , "` ");

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\RestoreVaults.PS1\" \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\VaultBackup\\" + productVersion +"\\"+ restoreVault +".mfb\" \"" + vaultName + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName.replaceAll("` " , " ") + " Restore Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while restore a vault : " + vaultName.replaceAll("` " , " ") + " - " + line);

				if(process.isAlive())
					process.destroy();

				if(migrateToSQL.equalsIgnoreCase("Yes"))
					Utility.migrateVaultToSqlServer(vaultName.replaceAll("` " , " "));

				Log.event("Utility.restoreTestVault :" + vaultName.replaceAll("` " , " ") +" is restored.", StopWatch.elapsedTime(startTime));
			}
		} //End try
		catch (Exception e) {
			if (e instanceof SkipException)
				throw new SkipException(e.getMessage());
			else
				throw new Exception ("Exception at Utility.restoreTestVault :"+e.getMessage(), e);
		} //End catch

	} //End restoreTestVault

	/**
	 * getServerVaults : This method is to available vaults name in the server
	 * @return Available vault names
	 * @throws Exception
	 */
	public static String getServerVaults() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

			Log.event("Utility.getServerVaults : Started to getting the available server vaults..", StopWatch.elapsedTime(startTime));

			String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\GetAllServerVaults.ps1\"";

			Process process = Runtime.getRuntime().exec(cmd);

			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";
			String vaults = "";

			int snooze = 0;

			while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();

				if(line.contains("Vaults :"))
					vaults = line.replaceAll("Vaults :", "");

				Log.event(line + "...", StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while getting the available vaults in the server" + line);

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.getServerVaults : Got the available vaults in the server...", StopWatch.elapsedTime(startTime));

			return vaults;//Returns the available server vaults

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.getServerVaults :"+e.getMessage(), e);
		} //End catch

	} //End getServerVaults

	/**
	 * takeVaultOfflineAndBringOnline : This method is to take Vault to Offline And then bring vault to Online the test vault
	 * @param vaultName: Vault to be taken to offline/online
	 * @param action: OfflineAndOnline (or) Offline (or) Online
	 * @return None
	 * @throws Exception
	 */
	public static void takeVaultOfflineAndBringOnline(String vaultName, String action) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isOfflineAndOnline = (test.getParameter("isOfflineAndOnline") != null) ? test.getParameter("isOfflineAndOnline") : "No";

			if (isOfflineAndOnline.equalsIgnoreCase("YES"))
			{
				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

				Log.event("Utility.takeVaultOfflineAndOnline :" + vaultName.replaceAll("` " , " ") + " " + action + " is started.", StopWatch.elapsedTime(startTime));

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\TakeVaultOfflineAndOnline.ps1\" \"" + vaultName.replaceAll(" " , "` ") + "\" \""+ action +"\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName.replaceAll("` " , " ") + " is moving to offline/online... : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while taking a vault('" + vaultName.replaceAll("` " , " ") + "') " + action + " : " + line);

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.takeVaultOfflineAndOnline :" + vaultName.replaceAll("` " , " ") +" is moved to " + action + ".", StopWatch.elapsedTime(startTime));
			}			

		} //End try
		catch (Exception e) {
			if (e instanceof SkipException)
				throw new SkipException(e.getMessage());
			else
				throw new Exception ("Exception at Utility.takeVaultOfflineAndOnline :"+e.getMessage(), e);
		} //End catch

	} //End takeVaultOfflineAndOnline

	/**
	 * destroyTestVault : This method is to destroy the test vault
	 * @return None
	 * @throws Exception
	 */
	public static void destroyTestVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isDestroy = test.getParameter("isDestroy");
			String isDetach = test.getParameter("isDetach");

			if(isDetach == null)
				isDetach = "NO";

			if (isDestroy.equalsIgnoreCase("YES") || isDetach.equalsIgnoreCase("YES")) {
				Log.event("Utility.destroyVault : " + test.getParameter("VaultName") +" destory is started.", StopWatch.elapsedTime(startTime));

				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

				String scriptName = "";

				//Vault will also be destroyed if both isDetach and isDestroy have value "Yes"
				if(isDestroy.equalsIgnoreCase("YES"))
					scriptName = "DestroyVault.PS1";
				else if(isDetach.equalsIgnoreCase("YES"))
					scriptName = "DetachVault.PS1";

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\" + scriptName + "\" \""+ test.getParameter("VaultName").replaceAll(" ", "` ") + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(test.getParameter("VaultName") + " Destroy Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while destroy a vault('" + test.getParameter("VaultName").replaceAll(" ", "` ") + "') : " + line);

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.destroyTestVault : "+ test.getParameter("VaultName") +" destroy is done.", StopWatch.elapsedTime(startTime));
			}
		} //End try
		catch (Exception e) { throw new Exception ("Exception at Utility.destroyTestVault :"+e.getMessage(), e); } //End catch

	} //End destroyTestVault	

	/**
	 * destroyTestVault : This method is to destroy the test vault
	 * @param vaultName: vault to be destroyed
	 * @return None
	 * @throws Exception
	 */
	public static void destroyTestVault(String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isDestroy = test.getParameter("isDestroy");
			if (isDestroy.equalsIgnoreCase("YES")) {

				Log.event("Utility.destroyVault : " + test.getParameter("VaultName") +" destory is started.", StopWatch.elapsedTime(startTime));

				String UserDir= System.getProperty("user.dir").replaceAll(" " , "` ");

				String cmd="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\DestroyVault.PS1\" \""+ vaultName.replaceAll(" ", "` ") + "\"";

				Process process = Runtime.getRuntime().exec(cmd);

				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event(vaultName + " Destroy Operation is in progress.. : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while destroy a vault('" + test.getParameter("VaultName").replaceAll(" ", "` ") + "') : " + line);

				if(process.isAlive())
					process.destroy();


			}
		} //End try
		catch (Exception e) { throw new Exception ("Exception at Utility.destroyTestVault : "+e.getMessage(), e); } //End catch
		finally{Log.event("Utility.destroyTestVault : "+ vaultName +" destroy is done.", StopWatch.elapsedTime(startTime));}

	} //End destroyTestVault	

	/**
	 * configureAdminUserAccount : This method is to configure the Admin user account in server
	 * @return None
	 * @throws Exception
	 */
	public static void configureAdminUserAccount() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			String UserName = "", FullName = "", AccountType="windows", LicenseType = "named", Password = "none", DomainName = "", Admin = "admin";

			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");

			String params = "";
			String cmd = "", cmd1 = "";

			cmd = "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\GetUserAccountDetails.ps1\"";

			Log.event("Utility.configureAdminUserAccount : Started to get the current logged in user account from the environment... ", StopWatch.elapsedTime(startTime));

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				if (line.contains("UserName:")){
					UserName = line.split(":")[1].trim();
					FullName = UserName.toUpperCase();
				}
				else if (line.contains("DomainName:"))
					DomainName = line.split(":")[1].trim();

				Log.event("Utility.configureAdminUserAccount is getting the useraccount from the machine : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while getting the user account details from the machine : " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.configureAdminUserAccount : Currently logged in user account, username("+ UserName +") and domain name("+DomainName+") is got from the environment...", StopWatch.elapsedTime(startTime));

			params = UserName.replaceAll(" ", "` ") + "\" \"" + FullName.replaceAll(" ", "` ")
					+ "\" \""+ AccountType.replaceAll(" ", "` ") +"\" \"" + LicenseType.replaceAll(" ", "` ") 
					+ "\" \"" + Password.replaceAll(" ", "` ") + "\" \"" + DomainName.replaceAll(" ", "` ")
					+ "\" \"" + Admin.replaceAll(" ", "` ") ;

			cmd1 = "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInServer.ps1\" \"" + params +"\"";

			Log.event("Utility.configureAdminUserAccount is started to Configure the user " + UserName +" in Server", StopWatch.elapsedTime(startTime));

			process = Runtime.getRuntime().exec(cmd1);
			process.getOutputStream().close();
			inputstream = process.getInputStream();
			inputstreamreader = new InputStreamReader(inputstream);
			bufferedreader = new BufferedReader(inputstreamreader);
			line = "";

			snooze = 0;

			while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.configureAdminUserAccount is Configuring the user " + UserName +" in Server : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while Configuring the user " + UserName +" in Server :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.configureAdminUserAccount is Configured the user " + UserName +" in Server", StopWatch.elapsedTime(startTime));

		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.configureAdminUserAccount : "+ e, e); }//End Catch
		finally{Log.event("Utility.configureAdminUserAccount: Configured the current logged in useraccount in Server...", StopWatch.elapsedTime(startTime));}

	}//End of configureAdminUserAccount

	/**
	 * configureUsers : This method is to configure the users in vault and server
	 * @return None
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String configureUsers(String workBook) throws Exception {

		final long startTime = StopWatch.startTime();
		String userValues = "";

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String UserConfig = test.getParameter("UserConfig");

			if (UserConfig.equalsIgnoreCase("YES")) {
				Log.event("Utility.configureUsers is started to Configure the users in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

				File dir = new File(".");
				int rowIndex = 0;
				String strBasePath = dir.getCanonicalPath();
				String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
						+ workBook;

				FileInputStream file = new FileInputStream(new File(inputFile));
				List<String> values = new ArrayList<String>();


				HSSFWorkbook workbook = new HSSFWorkbook(file);
				HSSFSheet sheet = workbook.getSheet("Users");

				//Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				String cmd, cmd1, params, UserDir="";
				if(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					while(rowIterator.hasNext()) {
						row = rowIterator.next();
						//For each row, iterate through each columns
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							values.add(cell.getStringCellValue());
						}

						if(rowIndex == 0) 
							userValues = values.get(0)+","+ values.get(1)+","+values.get(2);

						Log.event("Utility.configureUsers is started to Configure the user " + values.get(0) +" in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));


						UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
						params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(1).replaceAll(" ", "` ")
								+ "\" \""+ values.get(2).replaceAll(" ", "` ") +"\" \"" + values.get(3).replaceAll(" ", "` ") 
								+ "\" \"" + values.get(4).replaceAll(" ", "` ") + "\" \"" + values.get(5).replaceAll(" ", "` ")
								+ "\" \"" + values.get(6).replaceAll(" ", "` ") ;

						cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInServer.ps1\" \"" + params +"\"";

						Process process = Runtime.getRuntime().exec(cmd);
						process.getOutputStream().close();
						InputStream inputstream = process.getInputStream();
						InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
						BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
						String line = "";

						int snooze = 0;

						while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.configureUsers is Configuring the user " + values.get(0) +" in Server : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while Configuring the user " + values.get(0) +" in Server :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.configureUsers is Configured the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						if (values.get(2).equalsIgnoreCase("mfiles"))
							params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(7).replaceAll(" ", "` ")
							+ "\" \""+ values.get(8).replaceAll(" ", "` ") +"\" \""+ test.getParameter("VaultName").replaceAll(" ", "` ");
						else
							params = values.get(5)+"\\"+values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(7).replaceAll(" ", "` ")
							+ "\" \""+ values.get(8).replaceAll(" ", "` ") +"\" \""+ test.getParameter("VaultName").replaceAll(" ", "` ");

						cmd1="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInVault.ps1\" \"" + params +"\"";

						process = Runtime.getRuntime().exec(cmd1);

						process.getOutputStream().close();
						inputstream = process.getInputStream();
						inputstreamreader = new InputStreamReader(inputstream);
						bufferedreader = new BufferedReader(inputstreamreader);

						line = "";

						snooze = 0;

						while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.configureUsers is Configuring the user " + values.get(0) +" in Vault('" + test.getParameter("VaultName") + "') : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while Configuring the user " + values.get(0) +" in Vault('" + test.getParameter("VaultName") + "') :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.configureUsers is Configured the user " + values.get(0) +" in Vault('" + test.getParameter("VaultName") + "') for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						values = new ArrayList<String>();
						rowIndex++;
					}
				}
				file.close();


				String isAttach = test.getParameter("isAttach");

				if(isAttach == null)
					isAttach = "";

				//Checking if the vault of the test class has been attached/restored by Teamcity build step. Such test classes will
				//be executed in parallel and may cause CPU spikes in the beginning. Thus, these test classes will wait a bit before proceeding
				//to test execution after the users have been configured.
				if(isAttach.equalsIgnoreCase("Teamcity")){

					//Waiting a random time between 60 and 149 seconds.
					Random rand = new Random();
					int x = rand.nextInt(90) + 60;
					System.out.println( workBook.replaceAll(".xls", "") + " is waiting for " + x + " seconds after creating test users.");
					Thread.sleep(x*1000);
				}
			}
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.configureUsers : "+ e, e); }//End Catch

		return userValues;

	}//End of configureUsers

	/**
	 * configureUsers : This method is to configure the users in mentioned vault and server based on the Mentioned Users sheet
	 * @param:workBook
	 * @param:workSheet
	 * @param:vaultName
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String configureUsers(String workBook, String workSheet, String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();
		String userValues = "";

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String UserConfig = test.getParameter("UserConfig");

			if (UserConfig.equalsIgnoreCase("YES")) {
				Log.event("Utility.configureUsers is started to Configure the users in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

				File dir = new File(".");
				int rowIndex = 0;
				String strBasePath = dir.getCanonicalPath();
				String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
						+ workBook;

				FileInputStream file = new FileInputStream(new File(inputFile));
				List<String> values = new ArrayList<String>();


				HSSFWorkbook workbook = new HSSFWorkbook(file);
				HSSFSheet sheet = workbook.getSheet(workSheet);

				//Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				String cmd, cmd1, params, UserDir="";
				if(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					while(rowIterator.hasNext()) {
						row = rowIterator.next();
						//For each row, iterate through each columns
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							values.add(cell.getStringCellValue());
						}

						if(rowIndex == 0) 
							userValues = values.get(0)+","+ values.get(1)+","+values.get(2);

						Log.event("Utility.configureUsers is started to Configure the user " + values.get(0) +" in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));


						UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
						params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(1).replaceAll(" ", "` ")
								+ "\" \""+ values.get(2).replaceAll(" ", "` ") +"\" \"" + values.get(3).replaceAll(" ", "` ") 
								+ "\" \"" + values.get(4).replaceAll(" ", "` ") + "\" \"" + values.get(5).replaceAll(" ", "` ")
								+ "\" \"" + values.get(6).replaceAll(" ", "` ") ;

						cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInServer.ps1\" \"" + params +"\"";

						Process process = Runtime.getRuntime().exec(cmd);
						process.getOutputStream().close();
						InputStream inputstream = process.getInputStream();
						InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
						BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
						String line = "";

						int snooze = 0;

						while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.configureUsers is Configuring the user " + values.get(0) +" in Server : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while Configuring the user " + values.get(0) +" in Server :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.configureUsers is Configured the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						if (values.get(2).equalsIgnoreCase("mfiles"))
							params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(7).replaceAll(" ", "` ")
							+ "\" \""+ values.get(8).replaceAll(" ", "` ") +"\" \""+ vaultName.replaceAll(" ", "` ");
						else
							params = values.get(5)+"\\"+values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(7).replaceAll(" ", "` ")
							+ "\" \""+ values.get(8).replaceAll(" ", "` ") +"\" \""+ vaultName.replaceAll(" ", "` ");

						cmd1="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInVault.ps1\" \"" + params +"\"";

						process = Runtime.getRuntime().exec(cmd1);

						process.getOutputStream().close();
						inputstream = process.getInputStream();
						inputstreamreader = new InputStreamReader(inputstream);
						bufferedreader = new BufferedReader(inputstreamreader);

						line = "";

						snooze = 0;

						while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.configureUsers is Configuring the user " + values.get(0) +" in Vault('" + vaultName + "') : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while Configuring the user " + values.get(0) +" in Vault('" + vaultName + "') :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.configureUsers is Configured the user " + values.get(0) +" in Vault('" + vaultName + "') for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						values = new ArrayList<String>();
						rowIndex++;
					}
				}
				file.close();
			}
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.configureUsers : "+ e.getMessage(), e); }//End Catch

		return userValues;

	}//End of configureUsers

	/**
	 * configureUsers : This method is to configure the users in vault and server
	 * @return None
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static void configureUsers(String vaultName, String userName, String  fullName, String  accountType, String  licenseType, String  password, String  domainName, String admin, String vaultControl, String userType) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String UserConfig = test.getParameter("UserConfig");

			if (UserConfig.equalsIgnoreCase("YES")) {

				Log.event("Utility.configureUsers is started to Configure the user " + userName +" in Server and vault...", StopWatch.elapsedTime(startTime));


				String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
				String params = userName.replaceAll(" ", "` ") + "\" \"" + fullName.replaceAll(" ", "` ")
						+ "\" \""+ accountType.replaceAll(" ", "` ") +"\" \"" + licenseType.replaceAll(" ", "` ") 
						+ "\" \"" + password.replaceAll(" ", "` ") + "\" \"" + domainName.replaceAll(" ", "` ")
						+ "\" \"" + admin.replaceAll(" ", "` ") ;

				String cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInServer.ps1\" \"" + params +"\"";

				Process process = Runtime.getRuntime().exec(cmd);
				process.getOutputStream().close();
				InputStream inputstream = process.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				String line = "";

				int snooze = 0;

				while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event("Utility.configureUsers is Configuring the user " + userName +" in Server : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while Configuring the user " + userName +" in Server :\n " + line);

				bufferedreader.close();

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.configureUsers is Configured the user " + userName +" in Server.", StopWatch.elapsedTime(startTime));

				if (accountType.equalsIgnoreCase("mfiles"))
					params = userName.replaceAll(" ", "` ") + "\" \"" + vaultControl.replaceAll(" ", "` ")
					+ "\" \""+ userType.replaceAll(" ", "` ") +"\" \""+ vaultName.replaceAll(" ", "` ");
				else
					params = domainName+"\\"+userName.replaceAll(" ", "` ") + "\" \"" + vaultControl.replaceAll(" ", "` ")
					+ "\" \""+ userType.replaceAll(" ", "` ") +"\" \""+ vaultName.replaceAll(" ", "` ");

				String cmd1="powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\AddUserInVault.ps1\" \"" + params +"\"";

				process = Runtime.getRuntime().exec(cmd1);

				process.getOutputStream().close();
				inputstream = process.getInputStream();
				inputstreamreader = new InputStreamReader(inputstream);
				bufferedreader = new BufferedReader(inputstreamreader);

				line = "";

				snooze = 0;

				while (snooze < 20 && !line.contains("Execution Completed") && !line.contains("Exception")) {
					snooze++;
					line = bufferedreader.readLine();
					Log.event("Utility.configureUsers is Configuring the user " + userName +" in Vault('" + vaultName + "') : " + line, StopWatch.elapsedTime(startTime));       
				}

				if (line.contains("Exception")) 
					throw new Exception("Exception occured while Configuring the user " + userName +" in Vault('" + vaultName + "') :\n " + line);

				bufferedreader.close();

				if(process.isAlive())
					process.destroy();

				Log.event("Utility.configureUsers is Configured the user " + userName +" in Vault('" + vaultName + "').", StopWatch.elapsedTime(startTime));
			}
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.configureUsers : "+ e, e); }//End Catch	

	}//End of configureUsers


	/**
	 * removeUserAccountInVault : This method is to remove the user in vault
	 * @return None
	 * @throws Exception
	 */
	public static void removeUserAccountInVault(String userName, String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.removeUserAccountInVault is started to remove the user " + userName +" in vault...", StopWatch.elapsedTime(startTime));


			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
			String params = userName.replaceAll(" ", "` ") + "\" \"" + vaultName.replaceAll(" ", "` ");

			String cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\RemoveUserInVault.ps1\" \"" + params +"\"";

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.removeUserAccountInVault is removing the user " + userName +" in vault : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while removing the user " + userName +" from vault :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.removeUserAccountInVault is removed the user " + userName +" from Vault('" + vaultName + "').", StopWatch.elapsedTime(startTime));

		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.removeUserAccountInVault : "+ e.getMessage(), e); }//End Catch	

	}//End of removeUserAccountInVault

	/**
	 * removeLoginAccountInServer : This method is used to remove the users in server
	 * @param userName : Username to be deleted
	 * @param accountType : windows/mfiles
	 * @param domainName : Domain name of the user
	 * @return None
	 * @throws Exception
	 */
	public static String removeLoginAccountInServer(String userName, String accountType, String domainName) throws Exception {

		final long startTime = StopWatch.startTime();
		String userValues = "";

		try {

			Log.event("Utility.removeLoginAccountInServer is started to destroy the user " + userName +" in Server", StopWatch.elapsedTime(startTime));

			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
			String params = userName.replaceAll(" ", "` ") + "\" \"" + accountType.replaceAll(" ", "` ") + "\" \"" + domainName.replaceAll(" ", "` ");

			String cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\DestroyUserInServer.ps1\" \"" + params +"\"";

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.removeLoginAccountInServer is removing the user " + userName +" in Server : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while removing the user " + userName +" in Server :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.removeLoginAccountInServer is removed the user " + userName +" in Server", StopWatch.elapsedTime(startTime));
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.removeLoginAccountInServer : "+e.getMessage(), e); }//End Catch

		return userValues;

	}//End of removeLoginAccountInServer


	/**
	 * enableORdisableUserAccountInVault : This method is to enable/disable the user in vault
	 * @return None
	 * @throws Exception
	 */
	public static void enableORdisableUserAccountInVault(String userName, String enable, String vaultName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.enableORdisableUserAccountInVault is started to enable/disable the user " + userName +" in vault...", StopWatch.elapsedTime(startTime));


			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
			String params = userName.replaceAll(" ", "` ")+ "\" \"" + enable.replaceAll(" ", "` ") + "\" \"" + vaultName.replaceAll(" ", "` ");
			String powershell = "powershell.exe";

			String cmd = powershell + " \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\EnableOrDisableUserInVault.ps1\" \"" + params +"\"";

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.enableORdisableUserAccountInVault is enabling/disabling the user " + userName +" in vault : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while enabling/disabling the user " + userName +" in vault :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.enableORdisableUserAccountInVault is enabling/disabling the user " + userName +" in Vault('" + vaultName + "').", StopWatch.elapsedTime(startTime));

		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.enableORdisableUserAccountInVault : "+ e.getMessage(), e); }//End Catch	

	}//End of enableORdisableUserAccountInVault


	/**
	 * enableORdisableLoginAccountInServer : This method is to enable/disable the user in server
	 * @return None
	 * @throws Exception
	 */
	public static void enableORdisableLoginAccountInServer(String userName, String enable) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.enableORdisableLoginAccountInServer is started to enable/disable the user " + userName +" in server...", StopWatch.elapsedTime(startTime));


			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
			String params = userName.replaceAll(" ", "` ") + "\" \"" + enable.replaceAll(" ", "` ");
			String powershell = "powershell.exe";

			String cmd = powershell + " \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\EnableOrDisableUserInServer.ps1\" \"" + params +"\"";

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.enableORdisableLoginAccountInServer is enabling/disabling the user " + userName +" in server : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while enabling/disabling the user " + userName +" in vault :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.enableORdisableLoginAccountInServer is enabling/disabling the user " + userName +" in server.", StopWatch.elapsedTime(startTime));

		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.enableORdisableLoginAccountInServer : "+ e.getMessage(), e); }//End Catch	

	}//End of enableORdisableLoginAccountInServer


	/**
	 * markLoginAccountAsAdmin : This method is to mark the login account as system admin or not in server
	 * @return None
	 * @throws Exception
	 */
	public static void markLoginAccountAsAdmin(String userName, String admin) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Log.event("Utility.markLoginAccountAsAdmin is started to enable/disable the user " + userName +" in vault...", StopWatch.elapsedTime(startTime));

			String UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
			String params = userName.replaceAll(" ", "` ") + "\" \"" + admin.replaceAll(" ", "` ");
			String powershell = "powershell.exe";

			String cmd = powershell + " \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\MarkUserAsAdminInServer.ps1\" \"" + params +"\"";

			Process process = Runtime.getRuntime().exec(cmd);
			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event("Utility.markLoginAccountAsAdmin is marking the user as system admin/non-admin " + userName +" in server : " + line, StopWatch.elapsedTime(startTime));       
			}

			if (line.contains("Exception")) 
				throw new Exception("Exception occured while marking the user as system admin/non-admin " + userName +" in server :\n " + line);

			bufferedreader.close();

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.markLoginAccountAsAdmin is marking the user as system admin/non-admin " + userName +" in server.", StopWatch.elapsedTime(startTime));

		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.markLoginAccountAsAdmin : "+ e.getMessage(), e); }//End Catch	

	}//End of enableORdisableUserAccountInVault

	/**
	 * destroyUsers : This method is used to destroy the users in server
	 * @return None
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String destroyUsers(String workBook) throws Exception {

		final long startTime = StopWatch.startTime();
		String userValues = "";

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isDestroy = test.getParameter("isDestroy");

			if (isDestroy.equalsIgnoreCase("YES")) {
				Log.event("Utility.destroyUsers is started to destroy the users in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

				File dir = new File(".");
				int rowIndex = 0;
				String strBasePath = dir.getCanonicalPath();
				String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
						+ workBook;

				FileInputStream file = new FileInputStream(new File(inputFile));
				List<String> values = new ArrayList<String>();


				HSSFWorkbook workbook = new HSSFWorkbook(file);
				HSSFSheet sheet = workbook.getSheet("Users");

				//Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				String cmd, params, UserDir="";
				if(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					while(rowIterator.hasNext()) {
						row = rowIterator.next();
						//For each row, iterate through each columns
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							values.add(cell.getStringCellValue());
						}

						if(rowIndex == 0) 
							userValues = values.get(0)+","+ values.get(1)+","+values.get(2);

						Log.event("Utility.destroyUsers is started to destroy the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));


						UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
						params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(2).replaceAll(" ", "` ") + "\" \"" + values.get(5).replaceAll(" ", "` ");

						cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\DestroyUserInServer.ps1\" \"" + params +"\"";

						Process process = Runtime.getRuntime().exec(cmd);
						process.getOutputStream().close();
						InputStream inputstream = process.getInputStream();
						InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
						BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
						String line = "";

						int snooze = 0;

						while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.destroyUsers is destroying the user " + values.get(0) +" in Server : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while destroying the user " + values.get(0) +" in Server :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.destroyUsers is removed the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						values = new ArrayList<String>();
						rowIndex++;
					}
				}
				file.close();
			}
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.destroyUsers : "+e.getMessage(), e); }//End Catch

		return userValues;

	}//End of destroyUsers

	/**
	 * destroyUsers : This method is used to destroy the users in server
	 * @return None
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String destroyUsers(String workBook, String workSheet) throws Exception {

		final long startTime = StopWatch.startTime();
		String userValues = "";

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isDestroy = test.getParameter("isDestroy");

			if (isDestroy.equalsIgnoreCase("YES")) {
				Log.event("Utility.destroyUsers is started to destroy the users in Server and vault for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

				File dir = new File(".");
				int rowIndex = 0;
				String strBasePath = dir.getCanonicalPath();
				String inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
						+ workBook;

				FileInputStream file = new FileInputStream(new File(inputFile));
				List<String> values = new ArrayList<String>();


				HSSFWorkbook workbook = new HSSFWorkbook(file);
				HSSFSheet sheet = workbook.getSheet(workSheet);

				//Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				String cmd, params, UserDir="";
				if(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					while(rowIterator.hasNext()) {
						row = rowIterator.next();
						//For each row, iterate through each columns
						Iterator<Cell> cellIterator = row.cellIterator();
						while(cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							values.add(cell.getStringCellValue());
						}

						if(rowIndex == 0) 
							userValues = values.get(0)+","+ values.get(1)+","+values.get(2);

						Log.event("Utility.destroyUsers is started to destroy the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));


						UserDir = System.getProperty("user.dir").replaceAll(" ", "` ");
						params = values.get(0).replaceAll(" ", "` ") + "\" \"" + values.get(2).replaceAll(" ", "` ") + "\" \"" + values.get(5).replaceAll(" ", "` ");

						cmd= "powershell.exe -version 2 \""+ UserDir + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\DestroyUserInServer.ps1\" \"" + params +"\"";

						Process process = Runtime.getRuntime().exec(cmd);
						process.getOutputStream().close();
						InputStream inputstream = process.getInputStream();
						InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
						BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
						String line = "";

						int snooze = 0;

						while (snooze < 60 && !line.contains("Execution Completed") && !line.contains("Exception")) {
							snooze++;
							line = bufferedreader.readLine();
							Log.event("Utility.destroyUsers is destroying the user " + values.get(0) +" in Server : " + line, StopWatch.elapsedTime(startTime));       
						}

						if (line.contains("Exception")) 
							throw new Exception("Exception occured while destroying the user " + values.get(0) +" in Server :\n " + line);

						bufferedreader.close();

						if(process.isAlive())
							process.destroy();

						Log.event("Utility.destroyUsers is removed the user " + values.get(0) +" in Server for the " + workBook.replaceAll(".xls", "") + " test plan", StopWatch.elapsedTime(startTime));

						values = new ArrayList<String>();
						rowIndex++;
					}
				}
				file.close();
			}
		}//End Try
		catch(Exception e) { throw new Exception ("Exception at Utility.destroyUsers : "+e.getMessage(), e); }//End Catch

		return userValues;

	}//End of destroyUsers

	/*
	 * setEmbedAuthenticationToken: This function is used to set the EmbedAuthenticationToken key value

	public static void setEmbedAuthenticationToken() throws Exception{

		final long startTime = StopWatch.startTime();
		WebDriver driver = WebDriverUtils.getDriver();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			Log.event("Utility.setEmbedAuthenticationToken: Started to set the EmbedAutheticationToken Value for the vault "+ test.getParameter("VaultName"), StopWatch.elapsedTime(startTime));

			//Step-1: Launch configuration page

			driver.get(test.getParameter("ConfigurationURL"));

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(test.getParameter("UserName"),test.getParameter("Password"));
			ConfigurationPage configPage = new ConfigurationPage(driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-4: click 'VaultName' folder in Web Access Configuration
			configPage.clickVaultFolder(test.getParameter("VaultName")); //Selects General in the tree view of configuration page;

			//Step-5: getVaultGUID
			String vaultID = configPage.configurationPanel.getVaultUniqueID().trim();

			String productVersion = test.getParameter("productVersion").trim();

			String[] site = test.getParameter("webSite").replaceAll("http://", "").split("/");

			String webSite = "1";

			if (site.length > 2)
				webSite = webSite.trim()+ ":" + site[1].trim();

			String tokenValue = WindowsUtils.readStringRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\" + productVersion + "\\Server\\MFWA\\Sites\\"+ webSite +"\\Vaults\\" + vaultID + "\\Configurations\\EmbedAuthenticationToken");

			System.out.println("EmbedAuthenticationToken Value:" + tokenValue);

			WindowsUtils.writeStringRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\" + productVersion + "\\Server\\MFWA\\Sites\\"+ webSite +"\\Vaults\\" + vaultID + "\\Configurations\\EmbedAuthenticationToken", "True");

			tokenValue = WindowsUtils.readStringRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\" + productVersion + "\\Server\\MFWA\\Sites\\"+ webSite +"\\Vaults\\" + vaultID + "\\Configurations\\EmbedAuthenticationToken");

			System.out.println("EmbedAuthenticationToken Value:" + tokenValue);

			Log.event("Utility.setEmbedAuthenticationToken: Set the EmbedAutheticationToken Value to True for the vault "+ test.getParameter("VaultName"), StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.setEmbedAuthenticationToken : "+e.getMessage(), e); }//End Catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}*/

	/*
	 * isExternalViewExists: This function is used to check if specified view name is exist or not in the home view
	 */
	public static boolean isExternalViewExists(String viewName) throws Exception{

		final long startTime = StopWatch.startTime();
		WebDriver driver = null;

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			Log.event("Utility.isExternalViewExists: Started to set the check the external view('" + viewName + "') is exist in the home view for the vault "+ test.getParameter("VaultName"), StopWatch.elapsedTime(startTime));

			driver = WebDriverUtils.getDriver();//Gets the driver

			HomePage homePage = LoginPage.launchDriverAndLogin(driver, false);

			if (!homePage.listView.isItemExists(viewName))
				return false;

			return true;//Returns external view is exist in the home view

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.isExternalViewExists : "+e.getMessage(), e); }//End Catch

		finally {
			try {Utility.quitDriver(driver);}catch(Exception e0){}
			Log.event("Utility.isExternalViewExists: Checked external view '"+ viewName + "' is exist or not in the home view.", StopWatch.elapsedTime(startTime));
		} //End finally
	}

	/*
	 * setAnnotations: This function is used to set the annotations key value
	 */
	public static void setAnnotations() throws Exception{

		final long startTime = StopWatch.startTime();
		WebDriver driver = null;

		try {

			driver = WebDriverUtils.getDriver();

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			Log.event("Utility.setAnnotations: Started to set the annotations Value for the vault "+ test.getParameter("VaultName"), StopWatch.elapsedTime(startTime));

			//Step-1: Launch configuration page

			driver.get(test.getParameter("ConfigurationURL"));

			//Step-2 : Login with system admin credentials
			LoginPage loginPage=new LoginPage(driver);
			loginPage.loginToConfigurationUI(test.getParameter("UserName"),test.getParameter("Password"));
			ConfigurationPage configPage = new ConfigurationPage(driver);

			//Verify if Login failed
			if (!configPage.getErrorMessage().isEmpty())
				throw new Exception("Unable to login to Configuration Page, with Error :"+configPage.getErrorMessage());

			//Step-4: click 'VaultName' folder in Web Access Configuration
			configPage.clickVaultFolder(test.getParameter("VaultName")); //Selects General in the tree view of configuration page;

			//Step-5: getVaultGUID
			String vaultID = configPage.configurationPanel.getVaultUniqueID().trim();

			String productVersion = test.getParameter("productVersion").trim();

			String[] site = test.getParameter("webSite").replaceAll("http://", "").split("/");

			String webSite = "1";

			if (site.length > 2)
				webSite = webSite.trim()+ ":" + site[1].trim();

			String cmd = "REG ADD HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\" + productVersion + "\\Server\\MFWA\\Sites\\"+ webSite +"\\Vaults\\" + vaultID + "\\Configurations\\PDFViewer /v Type /d PdfTron /f";
			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("The operation completed") && snooze < 5) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event(line +"..", StopWatch.elapsedTime(startTime));       
			}

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.Annotations : Set the Annotations Value to PdfTron for the vault "+ test.getParameter("VaultName"), StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.setAnnotations : "+e.getMessage(), e); }//End Catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	}

	/**
	 * 
	 * setShowChangePasswordRegistry: This function is used to set the ShowChangePassword Registry key value
	 * @param set = 1 or 0
	 *  
	 */
	public static void setShowChangePasswordRegistry(int set) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String productVersion = test.getParameter("productVersion").trim();

			String[] site = test.getParameter("webSite").replaceAll("http://", "").split("/");

			String webSite = "1";

			if (site.length > 2)
				webSite = webSite.trim()+ ":" + site[1].trim();

			String cmd = "REG ADD HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\" + productVersion + "\\Server\\MFWA\\Sites\\"+ webSite +" /v ShowChangePassword /t REG_DWORD /d " + set + " /f";

			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("The operation completed") && snooze < 5) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event(line +"..", StopWatch.elapsedTime(startTime));       
			}

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.setShowChangePasswordRegistry : Set the ShowChangePassword Value to '"+ set +"'", StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.setShowChangePasswordRegistry : "+e.getMessage(), e); }//End Catch
	}//End of setShowChangePasswordRegistry


	/**
	 * 
	 * setRegistryValue: This function is used to set the registryValue in the registryPath
	 * @param registryPath : Path of the registry key
	 * @param registryKey : Key should be added/updated
	 * @param registryType : Type of the Key [For e.g.: REG_WORD]
	 * @param registryValue : Value of the registry key
	 *  
	 */
	public static void setRegistryValue(String registryPath, String registryKey, String registryType, String registryValue) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String productVersion = test.getParameter("productVersion").trim();

			String[] site = test.getParameter("webSite").replaceAll("http://", "").split("/");

			String webSite = "1";

			if (site.length > 2)
				webSite = webSite.trim()+ ":" + site[1].trim();

			if(registryPath.contains("PRODUCTVERSION"))
				registryPath = registryPath.replaceAll("PRODUCTVERSION", productVersion);

			if(registryPath.contains("WEBSITE"))
				registryPath = registryPath.replaceAll("WEBSITE", webSite);

			if(registryValue == null || registryValue.equals(""))
				registryValue = "\"\"";

			String cmd = "REG ADD "+ registryPath +" /v "+ registryKey +" /t " + registryType + " /d " + registryValue + " /f";

			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("The operation completed") && snooze < 5) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event(line +"..", StopWatch.elapsedTime(startTime));       
			}

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.setRegistryValue : Set the Registry Value to set the registry key value.", StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.setRegistryValue : "+e.getMessage(), e); }//End Catch
	}//End of setRegistryValue

	/**
	 * 
	 * removeRegistryKey: This function is used to remove the registryKey in the registryPath
	 * @param keyName : Key should be removed
	 *   
	 */
	public static void removeRegistryKey(String keyName) throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();

			String productVersion = test.getParameter("productVersion").trim();

			String[] site = test.getParameter("webSite").replaceAll("http://", "").split("/");

			String webSite = "1";

			if (site.length > 2)
				webSite = webSite.trim()+ ":" + site[1].trim();

			if(keyName.contains("PRODUCTVERSION"))
				keyName = keyName.replaceAll("PRODUCTVERSION", productVersion);

			if(keyName.contains("WEBSITE"))
				keyName = keyName.replaceAll("WEBSITE", webSite);

			String cmd = "REG DELETE "+ keyName +" /f";

			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("The operation completed") && snooze < 5) {
				snooze++;
				if(line != "")
					break;
				line = bufferedreader.readLine();
				if(line == null)
					break;
				Log.event(line +"..", StopWatch.elapsedTime(startTime));       
			}

			if(process.isAlive())
				process.destroy();

			resetIIS();//Restarts the IIS server

			Log.event("Utility.removeRegistryKey : Removed the Registry key and restarted the iis server.", StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) {
			throw new Exception ("Exception at Utility.removeRegistryKey : "+e.getMessage(), e);
		}//End Catch
	}//End of removeRegistryKey

	/**
	 * 
	 * resetIIS: This function is used to restart the IIS server
	 *  
	 */
	public static void resetIIS() throws Exception{

		final long startTime = StopWatch.startTime();

		try {

			String cmd = "iisreset";

			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (!line.contains("restarted") && snooze < 5) {
				snooze++;
				line = bufferedreader.readLine();
				Log.event(line +"..", StopWatch.elapsedTime(startTime));       
			}

			if(process.isAlive())
				process.destroy();

			Log.event("Utility.resetIIS : Restarts the IIS server completed..", StopWatch.elapsedTime(startTime));

		}//End Try

		catch(Exception e) { throw new Exception ("Exception at Utility.resetIIS : "+e.getMessage(), e); }//End Catch
	}//End of resetIIS


	/**
	 * destroyAndRestoreTestVault : This method is to destroy and restore the test vault
	 * @return None
	 * @throws Exception
	 */
	public static void destroyAndRestoreTestVault() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String isRestore = test.getParameter("isRestore");

			if (isRestore.equalsIgnoreCase("YES")) { //Checks if restore operation has to be performed.
				Log.event("Utility.destroyAndRestoreTestVault : Sample vault destroy and restore is started.", StopWatch.elapsedTime(startTime));

				Process process = Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\Common\\Prerequsites\\Console.exe restore \"" + test.getParameter("VaultName") + "\""); //Runs the destroy restore console application

				if(process.waitFor() != 0) //Waits till destroy & restore operation gets completed.
					throw new Exception("Sample vault restore is not successful.");

				Log.event("Utility.destroyAndRestoreTestVault : Sample vault is destroyed and restored.", StopWatch.elapsedTime(startTime));
			}		
			else
				Log.event("Utility.destroyAndRestoreTestVault : Destroy and Restore operation of sample vault is not selected.", StopWatch.elapsedTime(startTime));

		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.destroyAndRestoreTestVault : "+e.getMessage(), e);
		} //End catch

	} //End destroyAndRestoreTestVault	

	/**
	 * disableSaveSearch : This method disables saving the search history
	 * @return None
	 * @throws Exception
	 */
	public static void disableSaveSearch() throws Exception {

		final long startTime = StopWatch.startTime();
		WebDriver driver = WebDriverUtils.getDriver();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String testVault = xmlParameters.getParameter("VaultName");

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault + ">>Controls");
			configurationPage.configurationPanel.setVaultCommands("Save search terms", "Hide");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.event("Utility.disableSaveSearch : Destroy and Restore operation of sample vault is not selected.", StopWatch.elapsedTime(startTime));


		} //End try
		catch (Exception e) {
			throw new Exception ("Exception at Utility.disableSaveSearch : "+e.getMessage(), e);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		}

	} //End destroyAndRestoreTestVault	

	/**
	 * isDefaultLayout : To Verify if default layout gets displayed
	 * @param homePage - Instance of home page
	 * @return empty string if default layout is displayed; information if any of layouts are missing
	 * @throws Exception
	 */
	public static String isDefaultLayout(HomePage homePage)throws Exception {

		try {

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (!homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is not available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + "Metadatacard is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			return unAvailableLayouts;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.isDefaultLayout : "+e.getMessage(), e);
		} //End catch

	} //End isDefaultLayout

	/**
	 * isSimpleListingLayout : To Verify if default layout gets displayed
	 * @param homePage - Instance of home page
	 * @return empty string if simple listing layout is displayed; information if any of layouts are missing
	 * @throws Exception
	 */
	public static String isSimpleListingLayout(HomePage homePage)throws Exception {

		try {

			String unAvailableLayouts = "";

			if (!homePage.isSearchbarPresent()) //Checks if Search bar present
				unAvailableLayouts = "Search Bar is not available;";

			if (homePage.isTaskPaneDisplayed()) //Checks if Task Pane present
				unAvailableLayouts = unAvailableLayouts + "Task Pane is available;";

			if (!homePage.previewPane.isTabExists(Caption.PreviewPane.MetadataTab.Value))
				unAvailableLayouts = unAvailableLayouts + ", Metadata card is not available;";

			if (!homePage.menuBar.isMenuInMenubarDisplayed()) //Checks if Menu Bar present
				unAvailableLayouts = unAvailableLayouts + "Menu Bar is not available;";

			if (!homePage.menuBar.isBreadCrumbDisplayed()) //Checks if Breadcrumb present
				unAvailableLayouts = unAvailableLayouts + "Breadcrumb is not available;";

			if (homePage.taskPanel.isAppletEnabled())
				unAvailableLayouts = unAvailableLayouts + "Java Applet is available;";

			return unAvailableLayouts;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.isSimpleListingLayout : "+e.getMessage(), e);
		} //End catch

	} //End isSimpleListingLayout

	public static String xPathStringParser(String value)
	{
		String parsedString = value;
		if (!value.contains("\"")) {
			parsedString = "\"" + value + "\"";
			return parsedString;
		}

		if (!value.contains("'")) {
			parsedString = "'" + value + "'";
			return parsedString;
		}

		return parsedString;
	}

	/**
	 * getNewTabURL: This method is open the newly opened tab and return the URL value
	 * @param None
	 * @return Combo URL text
	 * @throws Exception
	 */
	public static String getNewTabURL(WebDriver driver, int tabIndex) throws Exception {

		try {

			List<String> browserTabs = new ArrayList<String> (driver.getWindowHandles()); //get window handlers as list
			Thread.sleep(5000);
			driver.switchTo().window(browserTabs.get(tabIndex)); //switch to new tab
			Utils.fluentWait(driver);
			String tabURL = driver.getCurrentUrl(); //check is it correct page opened or not (e.g. check page's title)

			driver.switchTo().defaultContent();
			Utils.fluentWait(driver);

			return tabURL;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getNewTabURL : "+e.getMessage(), e);
		} //End catch

	} //End getNewTabURL

	/**
	 * getNewTabURL: This method is open the newly opened tab and return the URL value
	 * @param None
	 * @return Combo URL text
	 * @throws Exception
	 */
	public static String getNewTabURL(WebDriver driver, int tabIndex, boolean toClose) throws Exception {

		try {

			List<String> browserTabs = new ArrayList<String> (driver.getWindowHandles()); //get window handlers as list
			driver.switchTo().window(browserTabs.get(tabIndex)); //switch to new tab
			Utils.fluentWait(driver);
			String tabURL = driver.getCurrentUrl(); //check is it correct page opened or not (e.g. check page's title)

			if (toClose) { //Closes the current tab url
				driver.close();
				Utils.fluentWait(driver);
			}

			driver.switchTo().defaultContent();
			Utils.fluentWait(driver);

			return tabURL;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getNewTabURL : "+e.getMessage(), e);
		} //End catch

	} //End getNewTabURL

	/** getRandomNumber : To get the random number from the specified limit
	 * @param start Starting number
	 * @param end Ending number
	 * @return Random number from the specfied limit
	 * @throws Exception
	 */
	public static int getRandomNumber(int min, int max) throws Exception {

		try {

			//Random rand = new Random();
			return (min + (int)(Math.random() * ((max - min) + 1)));
			//return(rand.nextInt((max - min) + 1) + min);

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.getRandomNumber : "+e.getMessage(), e);
		} //End catch

	} //End getRandomObject

	/**isAlertPresent : To verify if browser alert is displayed in the driver or not
	 * * @param driver: Current webdriver
	 * @return boolean value
	 */	
	public static boolean isAlertPresent(WebDriver driver) 
	{ 
		try 
		{ 
			driver.switchTo().alert(); 
			return true; 
		}   // try 
		catch (NoAlertPresentException Ex) 
		{ 
			return false; 
		}   // catch 
	}   // isAlertPresent()

	/**
	 * tabExists: This method is used to check the tab is exist in the current webdriver
	 * @param driver: Active driver
	 * @param tabToCheck: New tab URL/Title
	 * @param opt: Used to Specify the tabToCheck param is URL or title
	 * 
	 */
	public static boolean tabExists(WebDriver driver, String tabToCheck, String opt) throws Exception 
	{
		final long startTime = StopWatch.startTime();

		try{

			WebDriver assingedWebDriver = driver;
			boolean windowFound = false;
			ArrayList <String> multipleTabs = new ArrayList <String>(assingedWebDriver.getWindowHandles());
			Log.event("Utility.tabExists : Started to check whether the Tab is exists or not...", StopWatch.elapsedTime(startTime));
			Thread.sleep(5000);
			for (int i = 0; i < multipleTabs.size(); i++) {//Iterates through the each tab avaialble in the currrent driver
				assingedWebDriver.switchTo().window(multipleTabs.get(i));//Navigates to the corresponding tab
				if (opt.equalsIgnoreCase("title")) {//Checks if tabToCheck is title of the new tab
					if (assingedWebDriver.getTitle().equals(tabToCheck)) {//Checks whether the new tab title is as expected
						windowFound = true;//New tab exists
						break;
					}
				}
				else if (opt.equalsIgnoreCase("url")) {//Checks if tabToCheck is URL of the new tab
					if (assingedWebDriver.getCurrentUrl().contains(tabToCheck)) {//Checks whether the new tab URL is as expected
						windowFound = true;//New tab exists
						break;
					}
				}//End IF
			}//End for

			return windowFound;//Retuns the tab is exist or not in the webdriver

		}//try

		catch (Exception e) {
			return false;
		} //End catch
		finally {
			Log.event("Utility.tabExists : Tab exists or not is completed.", StopWatch.elapsedTime(startTime));
		} //End Finally

	}//tabExists

	/**
	 * switchToTab: This method is used to navigate to the tab in the current webdriver
	 * @param driver: Active driver
	 * @param tabToNavigate: Tab URL/Title
	 * @param opt: Used to Specify the tabToCheck param is URL or title
	 * 
	 */
	public static WebDriver switchToTab(WebDriver driver, String tabToNavigate, String opt) throws Exception 
	{
		try{
			if (driver == null) return null;

			WebDriver assingedWebDriver = driver;
			boolean windowFound = false;
			if(Utility.tabExists(assingedWebDriver, tabToNavigate, opt)){

				ArrayList <String> multipleTabs = new ArrayList <String>(assingedWebDriver.getWindowHandles());

				for (int i = 0; i < multipleTabs.size(); i++) {//Iterates through the each tab avaialble in the currrent driver
					assingedWebDriver.switchTo().window(multipleTabs.get(i));//Navigates to the corresponding tab
					if (opt.equalsIgnoreCase("title")) {//Checks if tabToNavigate is title of the tab to navigate
						if (assingedWebDriver.getTitle().contains(tabToNavigate)) {//Checks whether the new tab title is as expected
							windowFound = true;//Navigated to tab
							break;
						}
					}
					else if (opt.equalsIgnoreCase("url")) {//Checks if tabToNavigate is URL of the tab to navigate
						if (assingedWebDriver.getCurrentUrl().contains(tabToNavigate)) {//Checks whether the new tab URL is as expected
							windowFound = true;//Navigated to tab
							break;
						}
					}//End IF
				}//End for
			}
			if (windowFound)
				return assingedWebDriver;//Retuns the current driver after navigate to the specified tab
			else 
				return driver;

		}//try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.switchToTab : "+e.getMessage(), e);
		} //End catch

	}//switchToTab

	/**
	 * closeTab: This method is used to close the tab in the current webdriver
	 * @param driver: Active driver
	 * @param tabToNavigate: Tab URL/Title
	 * @param opt: Used to Specify the tabToCheck param is URL or title
	 * 
	 */
	public static boolean closeTab(WebDriver driver, String tabToNavigate, String opt) throws Exception 
	{
		try{
			if (driver == null) return true;

			WebDriver assingedWebDriver = driver;
			if(Utility.tabExists(assingedWebDriver, tabToNavigate, opt)){

				ArrayList <String> multipleTabs = new ArrayList <String>(assingedWebDriver.getWindowHandles());

				for (int i = 0; i < multipleTabs.size(); i++) {//Iterates through the each tab avaialble in the currrent driver
					assingedWebDriver.switchTo().window(multipleTabs.get(i));//Navigates to the corresponding tab
					if (opt.equalsIgnoreCase("title")) {//Checks if tabToNavigate is title of the tab to navigate
						if (assingedWebDriver.getTitle().contains(tabToNavigate)) {//Checks whether the new tab title is as expected
							try
							{
								assingedWebDriver.switchTo().window(multipleTabs.get(i)).close();//Closes the tab
							}
							catch(Exception e0)
							{
								if(!browser.equalsIgnoreCase("edge"))//https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/4804746/
									throw e0;
							}
							break;
						}
					}
					else if (opt.equalsIgnoreCase("url")) {//Checks if tabToNavigate is URL of the tab to navigate
						if (assingedWebDriver.getCurrentUrl().contains(tabToNavigate)) {//Checks whether the new tab URL is as expected
							try
							{
								assingedWebDriver.switchTo().window(multipleTabs.get(i)).close();//Closes the tab
							}
							catch(Exception e0)
							{
								if(!browser.equalsIgnoreCase("edge"))//https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/4804746/
									throw e0;
							}
							break;
						}
					}//End IF
				}//End for
			}

			return !Utility.tabExists(assingedWebDriver, tabToNavigate, opt);

		}//try

		catch (Exception e) {
			throw new Exception ("Exception at Utility.switchToTab : "+e.getMessage(), e);
		} //End catch

	}//switchToTab

	/**isHuborNodeStarted: This function is used to check if the hub is started or not
	 * @throws Exception 
	 * @retunrs true:if hub is on; false: if not
	 * 
	 */
	public static boolean isHuborNodeStarted() throws Exception
	{
		WebDriver driver = WebDriverUtils.getDriver();
		try 
		{
			Log.event("Hub is Up and running!!!");
			return true;
		}//End try
		catch (Exception e) {
			return false;
		} //End catch
		finally {
			driver.quit();
		}//End Finally
	}//End isHuborNodeStarted

	/**
	 * enterFileUploadPath: This method is to enter File upload path using application created by AutoIt
	 * @param path Path of the file to be specified
	 * @throws Exception 
	 * @retunrs None
	 * 
	 */
	public static void enterFileUploadPath(String path) throws Exception	{

		final long startTime = StopWatch.startTime();

		try  {

			String uploadAppPath = System.getProperty("user.dir")+ "\\Common\\Prerequsites\\AutoIt\\FileUpload.exe";
			Runtime.getRuntime().exec(uploadAppPath + " " + path);

		}//End try

		catch (Exception e) {
			throw new Exception("Exception at Utility.enterFileUploadPath : " + e);
		} //End catch

		Log.event("Utility.enterFileUploadPath : Path is entered in File upload dialog.", StopWatch.elapsedTime(startTime));

	}//End enterFileUploadPath

	/**
	 * configureSAMLorOAuthRegistrySettings
	 */
	public static void configureSAMLorOAuthRegistrySettings() throws Exception
	{
		try
		{
			removeRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Motive\\M-Files\\PRODUCTVERSION\\Server\\MFServer\\Authentication");//Removes the registry settings

			updateSAMLorOAuthRegistryFileWithProductVersion();//Update the registry file with the productversion

			String cmd = "REGEDIT /s \"C:\\Build\\Registry\\Oauth-Saml-latest.reg\"";

			Process process = Runtime.getRuntime().exec(cmd); //Runs the command line
			Thread.sleep(2000);//Waits for the command execution

			if(process.isAlive())//Checks if process is alive
				process.destroy();//Destroy the process

			resetIIS();//Resets the iis after setting the registry value

			Log.event("Utility.configureSAMLorOAuthRegistrySettings: SAMLorOAuth regsitry settings is completed.");

		}//End of try
		catch(Exception e)
		{
			throw new Exception("Exception at Utility.configureSAMLorOAuthRegistrySettings: "+e.getMessage(), e);
		}
	}//End of configureSAMLorOAuthRegistrySettings


	/**
	 * checkSAMLorOAuthIsConfigured
	 * @param loginType: SAML/OAuth
	 * @return Returns whether the link(SAML/OAuth) is displayed or not in the login page
	 */
	public static boolean checkSAMLorOAuthIsConfigured(String loginType) throws Exception
	{
		WebDriver driver = null; 

		try
		{
			driver = WebDriverUtils.getDriver();//Gets the driver
			driver.get(Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("webSite").toString());//Launches the URL
			LoginPage loginPage = new LoginPage(driver);//Instantiates the login page
			return loginPage.isSAMLorOAuthLinkDisplayed(loginType);//Returns if the link is displayed or not in the login page

		}//End of try
		catch(Exception e)
		{
			throw new Exception("Exception at Utility.checkSAMLorOAuthIsConfigured: "+e.getMessage(), e);
		}
		finally
		{
			if(driver != null)
				driver.quit();//Quits the driver
		}
	}//End of configureSAMLorOAuthRegistrySettings

	/**
	 * updateSAMLorOAuthRegistryFileWithProductVersion
	 */
	public static void updateSAMLorOAuthRegistryFileWithProductVersion() throws Exception{

		final long startTime = StopWatch.startTime();

		try
		{

			String UserDir= System.getProperty("user.dir");//Gets the project location
			String file = UserDir+"\\Common\\Prerequsites\\Powershell\\Saml-OAuthRegistrySettings";
			String productVersion = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("productVersion").toString();//Gets the product version

			//Copies registry file from project location to C:\Build\REgistry path
			//--------------------------------------------------------------------
			File source = new File(file);
			File dest = new File("C:\\Build\\Registry");
			String[] myFiles;    
			if(dest.isDirectory()){
				myFiles = dest.list();
				for (int i=0; i < myFiles.length; i++) {
					File myFile = new File(dest, myFiles[i]);
					Log.event("Utility.updateSAMLorOAuthRegistryFileWithProductVersion : Started to delete the existing file("+ myFile.getName() +") in the C:/Build/Registry folder...", StopWatch.elapsedTime(startTime));
					myFile.delete();
				}
			}
			Log.event("Utility.updateSAMLorOAuthRegistryFileWithProductVersion : Existing files in the C:/Build/Registry folder is cleared...", StopWatch.elapsedTime(startTime));
			Log.event("Utility.updateSAMLorOAuthRegistryFileWithProductVersion : Started to copying the registry file from project location to local folder(C:/Build/Registry)...", StopWatch.elapsedTime(startTime));
			FileUtils.copyDirectory(source, dest);
			Log.event("Utility.updateSAMLorOAuthRegistryFileWithProductVersion : Copied the registry file from project location to local folder(C:/Build/Registry)...", StopWatch.elapsedTime(startTime));
			file = "C:\\Build\\Registry\\Oauth-Saml-latest.reg";

			String cmd="powershell.exe -version 2 \""+ UserDir.replaceAll(" " , "` ") + "\\Common\\Prerequsites\\Powershell\\PowershellScripts\\UpdateRegistryFile.PS1\" \"" + file + "\" \"" + productVersion.replaceAll(" " , "` ") + "\"";

			Process process = Runtime.getRuntime().exec(cmd);

			process.getOutputStream().close();
			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";

			int snooze = 0;

			while (snooze < 10 && !line.contains("Updated Successfully") && !line.contains("Exception")) {

				snooze++;
				Log.event("Utility.updateSAMLorOAuthRegistryFileWithProductVersion : Updating the REG.. : "+line);
				line = bufferedreader.readLine();
			}

			if(process.isAlive())
				process.destroy();

		}
		catch(Exception e)
		{
			throw new Exception("Exception at Utility.updateSAMLorOAuthRegistryFileWithProductVersion: "+e.getMessage(), e);
		}
	}//End of updateSAMLorOAuthRegistryFileWithProductVersion

} //End Class Utility
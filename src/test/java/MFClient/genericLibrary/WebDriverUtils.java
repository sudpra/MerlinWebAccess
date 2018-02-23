package genericLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariOptions;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;

import genericLibrary.StopWatch;

public class WebDriverUtils {
	static public String pageUrl;
	static public String pageTitle;
	static public String driverHost;
	static public String driverPort;
	static public String browserVersion;
	static URL hubURL;
	static AtomicInteger browserType = new AtomicInteger(0);

	/**
	 * This Function will get the information which is specified in parameter
	 * and return as a string value
	 * 
	 * @param informationToGet
	 *            : which information have to get either Url or Title, have to
	 *            specify as a string
	 * @param driver
	 * @return
	 */
	public static void getPageInfo(WebDriver driver) {

		pageUrl = (driver.getCurrentUrl());
		pageTitle = (driver.getTitle());
	}// New Page Info.

	/**
	 * This Function will switch and gives control over new window/tab opens.
	 * 
	 * @param driver
	 */
	public static void switchToNewWindow(WebDriver driver) {

		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
		}

	}// Switch to new window

	/**
	 * This Function will close the Page whose URL or Title match with parameter
	 * value PageInfo.
	 * 
	 * @param pageInfo
	 *            : URL or Title which will verify page to be close
	 * @param driver
	 */
	public static void closePage(String pageInfo, WebDriver driver) {

		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
			if (driver.getCurrentUrl().equalsIgnoreCase(pageInfo) || driver.getTitle().equalsIgnoreCase(pageInfo)) {
				driver.close();
				break;
			}
		}

	}// Close Specific Page

	/**
	 * Verifies wheather the given text is present in the whole page
	 * 
	 * @param driver
	 * @param TextToCheck
	 * @return
	 */
	public static Boolean VerifyTextPresentinWholePage(WebDriver driver, String textToVerify) {

		Boolean verificationStatus = false;

		try {
			WebElement elementPresence = driver.findElement(By.xpath("//*[contains(.,'" + textToVerify + "')]"));
			verificationStatus = elementPresence != null;
			if (verificationStatus)
				verificationStatus = false;
		}
		catch (Exception e) {
			verificationStatus = true;
		}
		return verificationStatus;
	}

	/**
	 * Checks whether particular element present or not
	 * 
	 * @param className
	 *            - Use it as a key to locate an element
	 * @return
	 */
	public static Boolean isElementPresentUsingClassName(WebDriver driver, String className) {
		try {
			driver.findElement(By.className(className));
			return true;
		}
		catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static WebDriver getDriver(String driverType, int windowSize) throws Exception {

		boolean reuse = false;
		String reuseSession = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("ReuseBrowserSession");
		reuse = reuseSession != null && reuseSession.equalsIgnoreCase("true") ? true : false;
		return getDriver(reuse,"", driverType);
	}

	/**
	 * 
	 * @param windowsize
	 * @return
	 * @throws Exception 
	 */
	public static WebDriver getDriver(String windowsize) throws Exception {

		boolean reuse = false;
		String reuseSession = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("ReuseBrowserSession");
		reuse = reuseSession.equalsIgnoreCase("true") ? true : false;
		return getDriver(reuse, windowsize);

	}

	/**
	 * getDriver : Launches driver and returns the instance of the driver
	 * @param None
	 * @return Instance of the driver
	 * @throws Exception 
	 */
	public static WebDriver getDriver() throws Exception {
		try {
			boolean reuse = false;
			String reuseSession = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("ReuseBrowserSession");
			reuse = reuseSession != null && reuseSession.equalsIgnoreCase("true") ? true : false;
			return getDriver(reuse, "");

		} //End try
		catch (Exception e) {
			if (e.getMessage().contains("Hub is not started or down.") || e.getMessage().contains("Node is not started or down."))
				throw new SkipException("SkipException at WebDriverUtils.getDriver : "+e.getMessage());
			else
				throw e;
		} //End catch

	} //End getDriver

	/**
	 * getDriver : Launches driver and returns the instance of the driver
	 * @param reuseSession
	 * @param windowSize
	 * @return Instance of the driver
	 * @throws Exception 
	 */
	public static WebDriver getDriver(boolean reuseSession, String windowSize) throws Exception {

		WebDriver driver = null;
		URL hubURL = null;
		final long startTime = StopWatch.startTime();

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String driverHost = test.getParameter("deviceHost");
			String driverPort = test.getParameter("devicePort");
			String driverType = test.getParameter("driverType");
			String driverVersion = test.getParameter("driverVersion");
			String productVersion = test.getParameter("driverVersion");
			String driverPath = System.getProperty("user.dir") + "\\Common\\Drivers\\";
			String description = Reporter.getCurrentTestResult().getMethod().getDescription();
			String className = Reporter.getCurrentTestResult().getClass().getSimpleName().toString().trim();
			String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
			if (test.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + test.getParameter("productVersion").trim() + " - " + test.getParameter("driverType").toUpperCase().trim() + test.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + test.getParameter("productVersion").trim() + " - " + test.getParameter("driverType").toUpperCase().trim();

			if(Reporter.getCurrentTestResult().getMethod().isTest())
				Log.testCaseInfo(description + "[" + driverType.toUpperCase() + "]", className + " - " +  methodName, className, productVersion);

			hubURL = new URL("http://" + driverHost + ":" + driverPort + "/wd/hub");

			switch (driverType.toUpperCase()) {

			case "CHROME" : {

				ChromeOptions opt = new ChromeOptions(); // Set the capabilities for set the user agent
				DesiredCapabilities capabilities = DesiredCapabilities.chrome();
				opt.addArguments("--ignore-certificate-errors");
				opt.addArguments("--disable-bundled-ppapi-flash");
				opt.addArguments("--always-authorize-plugins=true");
				opt.addArguments("--disable-extensions");
				opt.addArguments("--lang=en-US");
				opt.addArguments("disable-infobars");
				opt.addArguments("--start-maximized");

				if (!windowSize.isEmpty())
					opt.addArguments("--window-size=" + windowSize);

				System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
				capabilities.setCapability(ChromeOptions.CAPABILITY, opt);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);

				if (reuseSession)
					driver = ReusableRemoteWebDriver.getDriver(hubURL, capabilities);
				else
					driver = new RemoteWebDriver(hubURL, capabilities);

				break;

			} //End case : Chrome

			case "IE" : {

				System.setProperty("webdriver.ie.driver", driverPath + "IEDriverServer.exe");
				DesiredCapabilities caps = DesiredCapabilities.internetExplorer();

				caps.setVersion(driverVersion);
				caps.setCapability("enablePersistentHover", false);
				caps.setCapability("ignoreZoomSetting", true);	
				caps.setCapability("Enable Protected Mode", true);					
				//caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				caps.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				caps.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				caps.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,"dismiss");
				driver = new RemoteWebDriver(hubURL,caps);
				break;

			} //End case : IE

			case "SAFARI" : {

				/*	if (!isSupportedPlatform())
						throw new Exception("Platform is not either Windows are Mac.");*/


				/*SafariOptions options = new SafariOptions();
				options.setUseCleanSession(true);*/

				DesiredCapabilities caps = DesiredCapabilities.safari();
				/*caps.setPlatform(Platform.MAC);
				caps.setCapability(SafariOptions.CAPABILITY, true);
				caps.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				caps.setCapability(CapabilityType.PROXY,"proxy.aspiresys.com:3128");
				caps.setCapability("unexpectedAlertBehaviour", "ignore");
				caps.setCapability("setUseCleanSession", true);*/
				caps.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
				driver = new RemoteWebDriver(hubURL,caps);
				driver.manage().deleteAllCookies();//delete all cookies
				break;

			} //End case : Safari


			case "OPERA" : {

				ChromeOptions opt = new ChromeOptions(); // Set the capabilities for set the user agent
				DesiredCapabilities capabilities = DesiredCapabilities.chrome();
				opt.addArguments("--ignore-certificate-errors");
				opt.addArguments("--disable-bundled-ppapi-flash");
				opt.addArguments("--always-authorize-plugins=true");
				opt.addArguments("disable-infobars");

				if (!windowSize.isEmpty())
					opt.addArguments("--window-size=" + windowSize);

				System.setProperty("webdriver.chrome.driver", driverPath + "operadriver.exe");
				capabilities.setCapability(ChromeOptions.CAPABILITY, opt);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);

				if (reuseSession)
					driver = ReusableRemoteWebDriver.getDriver(hubURL, capabilities);
				else
					driver = new RemoteWebDriver(hubURL, capabilities);

				if (windowSize.isEmpty())
					driver.manage().window().maximize();

				break;
			}

			case "EDGE" : {	

				DesiredCapabilities capabilities = DesiredCapabilities.edge();
				capabilities.setPlatform(Platform.WIN10);
				driver = new RemoteWebDriver(hubURL, capabilities);	
				break;

			} //End case : EDGE

			case "IPHONE" : {
				driver = new RemoteWebDriver(hubURL, DesiredCapabilities.iphone());				
				break;
			} //End case : IPhone


			default : {

				synchronized (WebDriverUtils.class) {

					DesiredCapabilities capabilities = DesiredCapabilities.firefox();
					/*FirefoxProfile fp = new FirefoxProfile();
					fp.setEnableNativeEvents(true);
					fp.setPreference("app.update.auto", false);
					fp.setPreference("app.update.enabled", false);
					fp.setPreference("app.update.silent", false);
					fp.setPreference("dom.ipc.plugins.java.enabled", true ); 
					fp.setPreference("plugin.state.java", 2);
					fp.setPreference("plugin.state.npdeployjava", 2);
					capabilities.setCapability(CapabilityType.PROXY,"proxy.aspiresys.com:3128");
					capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
					capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
					capabilities.setCapability("unexpectedAlertBehaviour", "ignore");
					capabilities.setCapability(FirefoxDriver.PROFILE, fp);
					capabilities.setCapability("marionette", true);*/
					driver = new RemoteWebDriver(hubURL, capabilities);

				}

				driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
				break;
			} //End default : firefox

			} //End switch

			if (driver.equals(null))
				throw new Exception("Driver did not intialize...\n Please check if hub is running / configuration settings are corect.");

			if (driverType.equalsIgnoreCase("ie"))
				if (windowSize.isEmpty())
					driver.manage().window().maximize();

				else {
					Point initialWindowPosition = new Point(0, 0);
					driver.manage().window().setPosition(initialWindowPosition);
				}
			else{
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("top.window.moveTo(0,0); top.window.resizeTo(screen.availWidth,screen.availHeight);");
			}

			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			Log.event("WebDriverUtils.getDriver : Driver is launched.", StopWatch.elapsedTime(startTime));
			Log.addTestRunMachineInfo(driver);

		}
		catch (UnreachableBrowserException e) {
			throw new SkipException("WebDriverUtils.getDriver : Hub is not started or down.: "+e.getMessage(), e);
		}
		catch (WebDriverException e) {
			if (e.getMessage().toLowerCase().contains("error forwarding the new session") || e.getMessage().toLowerCase().contains("sessionnotcreatedexception") || e.getMessage().toLowerCase().contains("unable to create new remote session"))
				throw new SkipException("WebDriverUtils.getDriver : Node is not started or down.: "+e.getMessage(), e);
			else
				throw new Exception("Exception encountered in getDriver Method : "+e.getMessage().toString(), e);
		}
		catch (Exception e) {
			throw new Exception("Exception encountered in getDriver Method : " + e.getMessage().toString(), e);
		}

		return driver;

	} //End getDriver

	public static WebDriver getDriver(boolean reuseSession, String windowSize, String driverType) throws Exception {

		WebDriver driver = null;
		URL hubURL = null;

		try {

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String driverHost = test.getParameter("deviceHost");
			String driverVersion = test.getParameter("driverVersion");
			String driverPort = test.getParameter("devicePort");

			hubURL = new URL("http://" + driverHost + ":" + driverPort + "/wd/hub");

			if (driverType.equalsIgnoreCase("iPhone")) {

				driver = new RemoteWebDriver(hubURL, DesiredCapabilities.iphone());

			}
			if (driverType.equalsIgnoreCase("chrome")) {

				// Set the capabilities for set the user agent
				ChromeOptions opt = new ChromeOptions();
				DesiredCapabilities capabilities = DesiredCapabilities.chrome();

				opt.addArguments("--ignore-certificate-errors");
				opt.addArguments("--disable-bundled-ppapi-flash");
				opt.addArguments("--disable-extensions");
				opt.addArguments("--start-maximized");

				if (!windowSize.isEmpty())
					opt.addArguments("--window-size=" + windowSize);

				//				Proxy proxy = new Proxy();
				//				proxy.setHttpProxy("proxy.aspiresys.com:3128");
				//				capabilities.setCapability("proxy", proxy);
				System.setProperty("webdriver.chrome.driver", "D:\\chromedriver_win32\\chromedriver.exe");
				capabilities.setCapability(ChromeOptions.CAPABILITY, opt);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				if (reuseSession)
					driver = ReusableRemoteWebDriver.getDriver(hubURL, capabilities);
				else
					driver = new RemoteWebDriver(hubURL, capabilities);


			}
			if (driverType.equalsIgnoreCase("IE")) {

				/*System.setProperty("webdriver.ie.driver", "D:\\IEDriverServer_Win32_2.44.0\\IEDriverServer.exe");

//				InternetExplorerDriverService service = new InternetExplorerDriverService.Builder().usingAnyFreePort().withLogFile(new File("c:\\lib\\iedriver1.log")).withLogLevel(InternetExplorerDriverLogLevel.TRACE).build();
//				service.start();
				DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
				caps.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
				caps.setCapability("enablePersistentHover", false);
				caps.setCapability("ignoreZoomSetting", true);	
				caps.setCapability("Enable Protected Mode", true);
				caps.setCapability("enablePersistentHover", false);
				caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				caps.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				caps.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
						"dismiss");
				caps.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
//				Proxy proxy = new Proxy();
//				proxy.setProxyType(ProxyType.MANUAL);
//				proxy.setSslProxy("trustAllSSLCertificates");
//				caps.setCapability(CapabilityType.PROXY,proxy);

		        driver = new RemoteWebDriver(hubURL,caps);*/

				System.setProperty("webdriver.ie.driver", "D:\\IEDriverServer_Win32_2.44.0\\IEDriverServer.exe");

				//				InternetExplorerDriverService service = new InternetExplorerDriverService.Builder().usingAnyFreePort().withLogFile(new File("c:\\lib\\iedriver1.log")).withLogLevel(InternetExplorerDriverLogLevel.TRACE).build();
				//				service.start();
				DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
				caps.setCapability("ignoreZoomSetting", true);	
				caps.setCapability("Enable Protected Mode", true);
				caps.setCapability("ie.ensureCleanSession", true);
				caps.setCapability("enablePersistentHover", false);
				caps.setCapability("ie.forceCreateProcessApi", false); 
				caps.setVersion(driverVersion);
				caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				caps.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				caps.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				//caps.setCapability(CapabilityType.PAGE_LOADING_STRATEGY, true);
				caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
						"dismiss");
				caps.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				driver = new RemoteWebDriver(hubURL,caps);

			}
			/*if (driverType.equalsIgnoreCase("Opera")) {
				System.setProperty("webdriver.opera.driver", "E:\\operadriver1.5\\operadriver-1.5.exe");
				DesiredCapabilities capabilities = DesiredCapabilities.opera();
				OperaProfile profile = new OperaProfile();  // prepared profile
				profile.preferences().set("User Prefs", "Ignore Unrequested Popups", false);
				capabilities.setCapability("opera.profile", profile);
				capabilities.setCapability("Enable Protected Mode", false);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS,true);
				capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,"accept");
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.PLATFORM,"WINDOWS");

				driver = new RemoteWebDriver(hubURL,capabilities);
			}*/
			if (driverType.equalsIgnoreCase("Safari")) {

				isSupportedPlatform();
				DesiredCapabilities caps = DesiredCapabilities.safari();
				caps.setPlatform(Platform.MAC);
				caps.setCapability(SafariOptions.CAPABILITY, true);
				caps.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				//caps.setCapability(CapabilityType.PROXY,"proxy.aspiresys.com:3128");
				caps.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
				/*caps.setCapability("unexpectedAlertBehaviour", "ignore");
				caps.setCapability("setUseCleanSession", true);*/
				driver = new RemoteWebDriver(hubURL,caps);
				driver.manage().deleteAllCookies();//delete all cookies

			}

			if (driverType.equalsIgnoreCase("zap")) {

				System.setProperty("webdriver.firefox.bin","C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");

				Proxy zapFFProxy = new Proxy();
				zapFFProxy.setHttpProxy("localhost:8095")
				.setFtpProxy("localhost:8095")
				.setSslProxy("localhost:8095");
				zapFFProxy.setSocksProxy("localhost:8095");
				zapFFProxy.setNoProxy("127.0.0.1");

				DesiredCapabilities cap = DesiredCapabilities.firefox();
				FirefoxProfile fp = new FirefoxProfile();
				//fp.setEnableNativeEvents(true);
				fp.setPreference("app.update.auto", false);
				fp.setPreference("app.update.enabled", false);
				fp.setPreference("app.update.silent", false);
				fp.setPreference("plugin.state.java", 1);

				cap.setCapability(CapabilityType.PROXY, zapFFProxy);
				cap.setCapability(FirefoxDriver.PROFILE, fp);
				cap.setCapability("marionette", false);

				driver = new RemoteWebDriver(hubURL, cap);
			}


			if (driverType.equalsIgnoreCase("firefox")) {

				synchronized (WebDriverUtils.class) {
					DesiredCapabilities capabilities = DesiredCapabilities.firefox();
					/*FirefoxProfile fp = new FirefoxProfile();
					//fp.setEnableNativeEvents(true);
					fp.setPreference("app.update.auto", false);
					fp.setPreference("app.update.enabled", false);
					fp.setPreference("app.update.silent", false);
					fp.setPreference("plugin.state.java", 2);
					fp.setPreference("plugin.state.npdeployjava", 2);	
					fp.setPreference("extensions.blocklist.enabled", false);
					capabilities.setCapability(CapabilityType.PROXY,"proxy.aspiresys.com:3128");
					capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
					capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
					capabilities.setCapability("unexpectedAlertBehaviour", "ignore");
					capabilities.setCapability(FirefoxDriver.PROFILE, fp);
					capabilities.setCapability("marionette", true);*/
					driver = new RemoteWebDriver(hubURL, capabilities);
				}

				driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
			}

			if (driverType.equalsIgnoreCase("edge"))
				driver = new RemoteWebDriver(hubURL, DesiredCapabilities.edge());

			Assert.assertNotNull(driver, "Driver did not intialize...\n Please check if hub is running / configuration settings are corect.");

			if (driverType.equalsIgnoreCase("ie")) 
				if (windowSize.isEmpty())
					driver.manage().window().maximize();
				else {
					Point initialWindowPosition = new Point(0, 0);
					driver.manage().window().setPosition(initialWindowPosition);
				}
			else{
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("top.window.moveTo(0,0); top.window.resizeTo(screen.availWidth,screen.availHeight);");
			}


			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);

		}
		catch (UnreachableBrowserException e) {
			throw new SkipException("WebDriverUtils.getDriver : Hub is not started or down.: "+e.getMessage(), e);
		}
		catch (WebDriverException e) {
			if (e.getMessage().toLowerCase().contains("error forwarding the new session") || e.getMessage().toLowerCase().contains("sessionnotcreatedexception") || e.getMessage().toLowerCase().contains("unable to create new remote session"))
				throw new SkipException("WebDriverUtils.getDriver : Node is not started or down.: "+e.getMessage(), e);
			else
				throw new Exception("Exception encountered in getDriver Method : "+e.getMessage().toString(), e);
		}
		catch (Exception e) {
			throw new Exception("Exception encountered in getDriver Method : " + e.getMessage().toString(), e);
		}

		return driver;

	}

	/**
	 * isSupportedPlatform : To check if platform is windows are safari
	 * @param None
	 * @return true if windows are mac is current platform; if not false
	 */
	private static boolean isSupportedPlatform() {

		try {
			Platform current = Platform.getCurrent();
			return Platform.MAC.is(current) || Platform.WINDOWS.is(current) || Platform.VISTA.is(current);
		} //End try
		catch (Exception e) {
			throw e;
		} //End catch

	} //End isSupportedPlatform

	/**
	 * 
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public final static String getTestSessionNodeIP(final WebDriver driver) throws Exception {

		XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		final String driverHost = test.getParameter("deviceHost");
		final String driverPort = test.getParameter("devicePort");
		HttpHost host = new HttpHost(driverHost, Integer.parseInt(driverPort));
		HttpClient client = HttpClientBuilder.create().build();
		URL testSessionApi = new URL("http://" + driverHost + ":" + driverPort + "/grid/api/testsession?session=" + ((RemoteWebDriver) driver).getSessionId());
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", testSessionApi.toExternalForm());
		HttpResponse response = client.execute(host, r);
		JsonObject object =extractObject(response);
		JsonElement nodeP = object.get("proxyId");
		String nodeIP = nodeP.getAsString().toLowerCase();
		nodeIP = nodeIP.replace("http://", "");
		nodeIP = nodeIP.replaceAll(":[0-9]{1,5}", "").trim();
		return nodeIP;

	}


	private static JsonObject extractObject(HttpResponse resp) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer s = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		rd.close();
		JsonParser parser = new JsonParser();
		JsonObject objToReturn = (JsonObject)parser.parse(s.toString());
		return objToReturn;
	}

	/**
	 * 
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public final static String getHubSession(final WebDriver driver) throws Exception {

		XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		final String driverHost = test.getParameter("deviceHost");
		final String driverPort = test.getParameter("devicePort");
		HttpHost host = new HttpHost(driverHost, Integer.parseInt(driverPort));
		HttpClient client = HttpClientBuilder.create().build();
		URL testSessionApi = new URL("http://" + driverHost + ":" + driverPort + "/grid/api/testsession?session=" + ((RemoteWebDriver) driver).getSessionId());
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", testSessionApi.toExternalForm());
		HttpResponse response = client.execute(host, r);

		JsonObject object =extractObject(response);
		JsonElement nodeP = object.get("proxyId");
		String nodeIP = nodeP.getAsString().toLowerCase();
		nodeIP = nodeIP.replace("http://", "");
		nodeIP = nodeIP.replaceAll(":[0-9]{1,5}", "").trim();
		return nodeIP;

	}

	/**
	 * 
	 */
	public static void freeUpAllWebDrivers() {
		ReusableRemoteWebDriver.freeUpDrivers();
	}
	/**
	 * 	
	 * @param driver
	 */
	public static void markUnResuable(WebDriver driver) {
		ReusableRemoteWebDriver.markUnResuable(driver);
	}

}
/**
 * 
 * @author saraswathi.bantanaha
 *
 */

class ReusableRemoteWebDriver extends RemoteWebDriver {

	protected static Map <WebDriver, String> driverStatusPool = Collections.synchronizedMap(new HashMap <WebDriver, String>());
	protected static Map <WebDriver, DesiredCapabilities> driverCapabilityPool = Collections.synchronizedMap(new HashMap <WebDriver, DesiredCapabilities>());

	public ReusableRemoteWebDriver() {
		super();
	}

	public ReusableRemoteWebDriver(URL url, DesiredCapabilities capabilities) {
		super(url, capabilities);
	}

	private static String browser(DesiredCapabilities capabilities) {
		return capabilities.getCapability(CapabilityType.BROWSER_NAME).toString();
	}

	private static String chromeOptions(DesiredCapabilities capabilities) {

		ChromeOptions opt = (ChromeOptions) capabilities.getCapability(ChromeOptions.CAPABILITY);
		String options = "";

		try {
			options = opt.toString();
		}
		catch (Exception e) {
		}

		return options;

	}
	/**
	 * 
	 * @param url
	 * @param requiredCapabilities
	 * @return
	 */
	public static WebDriver getDriver(URL url, DesiredCapabilities requiredCapabilities) {

		boolean freeSessionFound = false;

		if (!driverStatusPool.containsValue("free")) {
			WebDriver newDriver = new ReusableRemoteWebDriver(url, requiredCapabilities);
			driverStatusPool.put(newDriver, "busy");
			driverCapabilityPool.put(newDriver, requiredCapabilities);
			return newDriver;
		}

		for (WebDriver driver : driverStatusPool.keySet()) {

			if (driverStatusPool.get(driver).equalsIgnoreCase("free")) {

				RemoteWebDriver rd = (RemoteWebDriver) driver;
				DesiredCapabilities remoteCapabilities = (DesiredCapabilities) rd.getCapabilities();

				if (browser(requiredCapabilities).equalsIgnoreCase(browser(remoteCapabilities))) {

					if (browser(requiredCapabilities).equalsIgnoreCase("chrome") && chromeOptions(requiredCapabilities).equalsIgnoreCase(chromeOptions(driverCapabilityPool.get(driver))))
						freeSessionFound = true;

				}

			}

			if (freeSessionFound) {
				try {
					driverStatusPool.put(driver, "busy");
					driver.manage().deleteAllCookies();
					driver.get("about:blank");
					return driver;
				}
				catch (WebDriverException e) {
					driverStatusPool.put(driver, "normalquit");
					driver.quit();
				}
			}

		}

		WebDriver newDriver = new ReusableRemoteWebDriver(url, requiredCapabilities);
		driverStatusPool.put(newDriver, "busy");
		driverCapabilityPool.put(newDriver, requiredCapabilities);
		return newDriver;

	}
	/**
	 * 
	 */
	@Override
	public void quit() {

		final WebDriver driver = (WebDriver) this;

		if (driverStatusPool.get(driver).equalsIgnoreCase("normalquit")) {

			driverCapabilityPool.remove(driver);
			driverStatusPool.remove(driver);
			try {
				super.quit();
			}
			catch (WebDriverException e) {
			}
			return;

		}

		driverStatusPool.put(driver, "free");

		Thread delayedQuit = new Thread() {

			@Override
			public void run() {

				try {

					for (int i = 0; i < 40; i++) {

						Thread.sleep(250);

						if (!driverStatusPool.get(driver).equalsIgnoreCase("free"))
							return;
					}

				}
				catch (InterruptedException | NullPointerException e) {
				}

				try {
					if (driverStatusPool.get(driver).equalsIgnoreCase("free")) {
						driverStatusPool.put(driver, "normalquit");
						try {
							driver.quit();
						}
						catch (WebDriverException e) {
						}
					}
				}
				catch (NullPointerException e) {
				}

			}

		};

		delayedQuit.start();

	}

	/**
	 * 
	 * @param driver
	 */
	public static void markUnResuable(WebDriver driver) {
		driverStatusPool.put(driver, "normalquit");
	}

	/**
	 * 
	 */
	public static void freeUpDrivers() {

		Object drivers[] = driverStatusPool.keySet().toArray();

		for (WebDriver driver : driverStatusPool.keySet())
			driverStatusPool.put(driver, "normalquit");

		for (int i = 0; i < drivers.length; i++) {

			try {
				((WebDriver) drivers[i]).quit();
			}
			catch (Exception e) {

			}

		}

	}
}
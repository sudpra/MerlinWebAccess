package genericLibrary;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.WebDriver;
import MFClient.Wrappers.Utility;


public class TestMethodWebDriverManager {
	
	// Map data structure which is used to manage WebDriver objects associated with test methods' names.
	// The key is test method name and value is a Stack data structure that contains all WebDrivers started for the test method.
	private ConcurrentHashMap <String, Stack<WebDriver>> webDriversByMethodName = null;
	
	// Map data structure which is used to manage ExtentTest objects associated with test methods' names.
	// The key is test method name and value is the ExtentTest object used in the test method.
	//private ConcurrentHashMap <String, ExtentTest> extentTestsByMethodName = null;
	
	public TestMethodWebDriverManager(){
		
		webDriversByMethodName = new ConcurrentHashMap <String, Stack<WebDriver>>();
		//extentTestsByMethodName = new ConcurrentHashMap <String, ExtentTest>();
	}
	
	/**
	 * createWebDriverForTestMethod : Creates a new WebDriver object and associates it with the test method.
	 * @param methodName - Name of the test method as a String that is used to associate the WebDriver with that test method.
	 * @return The newly created WebDriver object.
	 */
	public WebDriver createWebDriverForTestMethod(String methodName) throws Exception {
		
		try {
			
			WebDriver driver = WebDriverUtils.getDriver();
	
			Stack<WebDriver> webDriversStack = webDriversByMethodName.get(methodName);
			
			// Check if there is not yet a WebDriver stack for this test method.
			if(webDriversStack == null) {
				webDriversStack = new Stack<WebDriver>();	
			}
			
			// Push the new WebDriver on top of the stack and associate the stack with the method's name.
			webDriversStack.push(driver);
			webDriversByMethodName.put(methodName, webDriversStack);
			
			// The WebDriver is now associated with the test method and is ready for use.
			return driver;
		}
		catch(Exception e) {
			throw e;
		}
		
	}
	
	/**
	 * startTesting : Creates a WebDriver object and associates it with the test method.
	 * @param methodName - Name of the test method.
	 * @return New WebDriver object associated with the test method.
	 */
	public WebDriver startTesting(String methodName) throws Exception {
		
		try {
			
			//extentTestsByMethodName.put(methodName, extentTest);
			
			return this.createWebDriverForTestMethod(methodName);
		}
		catch(Exception e){
			throw e;
		}
		
	}
	
	/**
	 * quitTestMethodWebDrivers : Quits all WebDrivers associated with the test method.
	 * @param methodName - Name of the test method.
	 */
	public void quitTestMethodWebDrivers(String methodName) {
		
		Stack<WebDriver> drivers = webDriversByMethodName.get(methodName);
		
		//ExtentTest extentTest = extentTestsByMethodName.get(methodName);
		
		// There is no driver stack associated to this test method. Return without doing anything
		if(drivers == null)
			return;
		
		// Quit all drivers until none remain
		while(!drivers.isEmpty()) {
			
			WebDriver driver = drivers.pop();
			
			try {
			
				if(driver != null ) //&& extentTest != null) {
				{
					Utility.quitDriver(driver);
				}
			}
			catch(Exception e) {
				if(driver != null) {
					driver.quit();
				}
			}
		}
	}
	
	/**
	 * addWebDriverForTestMethod : Takes a WebDriver object and associates it with the test method.
	 * @param methodName - Name of the test method as a String that is used to associate the WebDriver with that test method.
	 * @param driver - The WebDriver object to be associated with the test method.
	 */
	public void addWebDriverForTestMethod(String methodName, WebDriver driver) throws Exception {
		
		webDriversByMethodName.get(methodName).push(driver);
		
	}
	
	
	public WebDriver getWebDriverOfTestMethodByIndex(String methodName, int index) {
		
		Stack<WebDriver> drivers = webDriversByMethodName.get(methodName);
		
		return drivers.get(index);
		
	}
	
	/**
	 * getLatestWebDriverOfTestMethod : Get the WebDriver that was started latest that is associated with the test method
	 * @param methodName - Name of the test method
	 * @return Latest WebDriver object associated with the test method
	 */
	public WebDriver getLatestWebDriverOfTestMethod(String methodName) {
		
		Stack<WebDriver> drivers = webDriversByMethodName.get(methodName);
		
		return drivers.peek();
		
	}
	
	/**
	 * getExtentTestOfTtestMethod : Get the ExtentTest that is associated with the test method
	 * @param methodName - Name of the test method
	 * @return ExtentTest object associated with the test method
	 */
	/*
	 public ExtentTest getExtentTestOfTtestMethod(String methodName) {
			
			return extentTestsByMethodName.get(methodName);
			
	}
	*/
	
	/*
	public void handleTimeoutFailure(String methodName, ITestResult result) {
		
		if(result.getStatus() == ITestResult.FAILURE) {

			if( result.getThrowable() instanceof org.testng.internal.thread.ThreadTimeoutException) {
				Exception e = (Exception)result.getThrowable();
				ExtentTest extentTest = extentTestsByMethodName.get(methodName);
				WebDriver driver = webDriversByMethodName.get(methodName).peek();
				try {
					Log.exception(e, driver);
				}
				catch(Exception e2){}
			}
		
		}
	}
	*/
}
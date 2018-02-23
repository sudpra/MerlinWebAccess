package genericLibrary;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.KlovReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * 
 * ExtentReports Generator (Works with @Listeners(EmailReport.class))
 *
 */
public class ExtentReporter {

	private static ExtentReports extentReport = null;
	private static String klovReport = (Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("klovReport") != null) ? Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("klovReport") : "No";
	private static String extentXReport = (Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("extentXReport") != null) ? Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("extentXReport") : "No";
	private static HashMap<Integer, ExtentTest> tests = new HashMap<Integer, ExtentTest>();
	private static boolean isReportClosed = false;

	/**
	 * To form a unique test name in the format	
	 * "PackageName.ClassName#MethodName"
	 * 
	 * @param iTestResult
	 * @return String - test name
	 */
	private static String getTestName(ITestResult iTestResult) {
		try{
			String testClassName = iTestResult.getTestClass().getName().replace("MFClient.Tests.", "").replace(".", ":");
			String testMethodName = iTestResult.getMethod().getMethodName().toString().trim();
			return testClassName +" - "+ testMethodName;
		}
		catch(Exception e){
			return ((iTestResult.getTestClass().getName().trim()+" - "+iTestResult.getMethod().getMethodName()).trim());
		}
	}

	/**
	 * To convert milliseconds to Date object
	 * 
	 * @param millis
	 * @return Date
	 */
	private static Date getTime(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

	/**
	 * To set run status for interrupted tests
	 * 
	 * @param iTestResult
	 * @param extentTest
	 */
	private static void setInterruptedTestStatus(ITestResult iTestResult, ExtentTest extentTest) {
		if(!(extentTest.getStatus().equals(Status.FATAL) || extentTest.getStatus().equals(Status.PASS))) {
			return;
		}
		switch (iTestResult.getStatus()) {
		case 2:
			if (iTestResult.getThrowable() == null)
				extentTest.log(Status.FAIL, "<font color=\"red\">Test Failed</font>");
			else
				extentTest.fail(iTestResult.getThrowable());//log(Status.FAIL, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(iTestResult.getThrowable()) + "</div>");
			break;
		case 3:
			if (iTestResult.getThrowable() == null)
				extentTest.log(Status.SKIP, "<font color=\"orange\">Test Skipped</font>");
			else
				extentTest.skip(iTestResult.getThrowable());//log(Status.SKIP, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(iTestResult.getThrowable()) + "</div>");
			break;
		}
	}

	/**
	 * Returns an ExtentReports instance if already exists. Creates new and
	 * returns otherwise.
	 * 
	 * @param iTestResult
	 * @return {@link ExtentReports} - Extent report instance
	 */
	private static synchronized ExtentReports getReportInstance(ITestResult iTestResult) {

		if (extentReport == null) {

			String reportFilePath = new File(iTestResult.getTestContext().getOutputDirectory()).getParent() + File.separator + "AutomationExtentReport.html";
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String reportName = "M-Files VCurrent Automation Report - " + xmlParameters.getParameter("productVersion") + " - " + xmlParameters.getParameter("driverType").toUpperCase();
			String hostname = "Unknown";
			String extentXMongoDBConnectionName = (xmlParameters.getParameter("extentXMongoDBConnectionName") != null) ? xmlParameters.getParameter("klovMongoDBConnectionName") : "";
			String extentXProjectName = (xmlParameters.getParameter("extentXProjectName") != null) ? xmlParameters.getParameter("extentXProjectName") : "M-Files - VCurrent Automation";
			String extentXURL = (xmlParameters.getParameter("extentXURL") != null) ? xmlParameters.getParameter("extentXURL") : "";
			String klovMongoDBConnectionName = (xmlParameters.getParameter("klovMongoDBConnectionName") != null) ? xmlParameters.getParameter("klovMongoDBConnectionName") : "";
			int klovMongoDBConnectionPort = (xmlParameters.getParameter("klovMongoDBConnectionPort") != null) ? Integer.parseInt(xmlParameters.getParameter("klovMongoDBConnectionPort")) : 27017;
			String klovProjectName = (xmlParameters.getParameter("klovProjectName") != null) ? xmlParameters.getParameter("klovProjectName") : "M-Files - VCurrent Automation";
			String klovURL = (xmlParameters.getParameter("klovURL") != null) ? xmlParameters.getParameter("klovURL") : "";
			KlovReporter klov = null;
			ExtentXReporter extentX = null;

			try
			{
				InetAddress addr;
				addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			}
			catch (UnknownHostException ex)
			{
				System.out.println("Hostname can not be resolved");
			}

			ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(reportFilePath);
			htmlReporter.config().setChartVisibilityOnOpen(true);
			htmlReporter.config().setDocumentTitle(reportName);
			htmlReporter.config().setReportName(reportName);
			htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
			htmlReporter.config().setTheme(Theme.STANDARD);		
			List<Status> statusHierarchy = Arrays.asList(
					Status.FATAL,
					Status.FAIL,
					Status.ERROR,
					Status.SKIP,
					Status.PASS,
					Status.WARNING,
					Status.DEBUG,
					Status.INFO
					);

			extentReport = new ExtentReports();
			extentReport.setSystemInfo("Host Name", hostname);
			extentReport.setSystemInfo("OS", System.getProperty("os.name"));
			extentReport.setSystemInfo("User Name", System.getProperty("user.name"));
			extentReport.setSystemInfo("Java Version", System.getProperty("java.version"));
			extentReport.config().statusConfigurator().setStatusHierarchy(statusHierarchy);

			if (extentXReport.equalsIgnoreCase("Yes"))
			{
				try
				{

					if (extentXMongoDBConnectionName.equals("") || extentXURL.equals(""))
						throw new Exception("ExtentX Connection parameters('extentXMongoDBConnectionName' & 'extentXURL') not defined in the testNgXML.");

					extentX = new ExtentXReporter(extentXMongoDBConnectionName);
					// project name
					extentX.config().setProjectName(extentXProjectName);

					// report or build name
					extentX.config().setReportName(reportName);

					// server URL
					// ! must provide this to be able to upload snapshots
					// Note: this is the address to the ExtentX server, not the Mongo database
					extentX.config().setServerUrl(extentXURL);
				}
				catch(Exception e)
				{
					System.out.println("Exception occurred while instantiating the ExtentX Report : "+ e.getMessage());
					extentX = null;
				}

			}

			if (klovReport.equalsIgnoreCase("Yes")) {

				try
				{
					if (klovMongoDBConnectionName.equals("") || klovURL.equals(""))
						throw new Exception("KLOV Connection parameters('klovMongoDBConnectionName' & 'klovURL') not defined in the testNgXML.");

					klov = new KlovReporter();
					// specify mongoDb connection
					klov.initMongoDbConnection(klovMongoDBConnectionName, klovMongoDBConnectionPort);
					// specify project
					// ! you must specify a project, other a "Default project will be used"
					klov.setProjectName(klovProjectName);
					// you must specify a reportName otherwise a default timestamp will be used
					klov.setReportName(reportName);
					// URL of the KLOV server
					// you must specify the served URL to ensure all your runtime media is uploaded
					// to the server
					klov.setKlovUrl(klovURL);
				}
				catch(Exception e)
				{
					System.out.println("Exception occurred while instantiating the KLOV Report : "+ e.getMessage());
					klov = null;
				}
			}

			if(klov != null && extentX != null)
				extentReport.attachReporter(htmlReporter, klov, extentX);
			else if(klov != null)
				extentReport.attachReporter(htmlReporter, klov);
			else if(extentX != null)
				extentReport.attachReporter(htmlReporter, extentX);
			else
				extentReport.attachReporter(htmlReporter);

		}
		return extentReport;
	}

	/**
	 * To start and return a new extent test instance with given test case
	 * description. Returns the test instance if the test has already been
	 * started
	 * 
	 * @param description
	 *            - test case description
	 * @return {@link ExtentTest} - ExtentTest Instance
	 */
	private static ExtentTest startTest(String description) {
		ExtentTest test = null;
		ITestResult iTestResult = Reporter.getCurrentTestResult();
		String testName = iTestResult != null ? getTestName(iTestResult) : Thread.currentThread().getName();
		Integer hashCode = iTestResult != null ? iTestResult.hashCode() : Thread.currentThread().hashCode();
		if (tests.containsKey(hashCode)) {
			test = tests.get(hashCode);
			if (description != null && !description.isEmpty()) {
				//test.setDescription(description);
			}
		} else {
			if (iTestResult == null || !iTestResult.getMethod().isTest()) {
				//test = new ExtentTest(testName, description);
			} else {

				XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
				String author = "M-Files";

				if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
					author = "M-Files - " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
				else
					author = "M-Files - " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

				test = getReportInstance(iTestResult).createTest(testName, description).assignAuthor(author).assignCategory(testName.split("-")[0].trim());
				tests.put(hashCode, test);				
			}
		}
		return test;
	}

	/**
	 * Returns the test instance if the test has already been started. Else
	 * creates a new test with empty description
	 * 
	 * @return {@link ExtentTest} - ExtentTest Instance
	 */
	private static ExtentTest getTest() {
		return startTest("");
	}

	/**
	 * To start a test with given test case info
	 * 
	 * @param testCaseInfo
	 */
	public static void testCaseInfo(String testCaseInfo) {
		startTest("<strong><font size = \"4\" color = \"#000080\">" + testCaseInfo + "</font></strong>");
	}

	/**
	 * To add the machine info in the test case
	 * 
	 * @param testCaseInfo
	 */
	public static void testMachineInfo(String testMachineInfo) {
		if(getTest() != null)
			getTest().log(Status.INFO, "<font size = \"2\" color = \"#2E4053\">" + testMachineInfo + "</font>");
	}

	/**
	 * To log the given message to the reporter at INFO level
	 * 
	 * @param message
	 */
	public static void info(String message) {
		if (getTest() != null)
			getTest().info(message);//log(Status.INFO, message);
	}

	/**
	 * To log the given message to the reporter at INFO level
	 * 
	 * @param message
	 */
	public static void warning(String message) {
		if (getTest() != null)
			getTest().warning(message);//log(Status.WARNING, message);
	}

	/**
	 * To log the given message to the reporter at INFO level with Screenshot
	 * 
	 * @param message: Description
	 * @param screenshotPath: ScreenshotPath
	 * @throws IOException 
	 */
	public static void info(String message, String screenshotPath) throws IOException {
		if (getTest() != null)
			getTest().info(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());//log(Status.INFO, message + getTest().addScreenCaptureFromPath(screenshotPath, "Test"));
	}

	/**
	 * To log the given message to the reporter at DEBUG level
	 * 
	 * @param event
	 */
	public static void debug(String event) {
		if (getTest() != null)
			getTest().debug(event);//log(Status.FATAL, event);
	}

	/**
	 * To log the given message to the reporter at PASS level
	 * 
	 * @param passMessage
	 */
	public static void pass(String passMessage) {
		if (getTest() != null)
			getTest().pass("<font color=\"green\">" + passMessage + "</font>");//log(Status.PASS, "<font color=\"green\">" + passMessage + "</font>");
	}

	/**
	 * To log the given message to the reporter at PASS level
	 * 
	 * @param passMessage
	 * @throws IOException 
	 */
	public static void pass(String passMessage, String screenshotPath) throws IOException {
		if (getTest() != null)
			getTest().pass("<font color=\"green\">" + passMessage + "</font>", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());//log(Status.PASS, "<font color=\"green\">" + passMessage + "</font>" + getTest().addScreenCaptureFromPath(screenshotPath));
	}

	/**
	 * To log the given message to the reporter at FAIL level
	 * 
	 * @param failMessage
	 */
	public static void fail(String failMessage) {
		if (getTest() != null)
			getTest().fail("<font color=\"red\">" + failMessage + "</font>");//log(Status.FAIL, "<font color=\"red\">" + failMessage + "</font>");
	}

	/**
	 * To log the given message to the reporter at Error level
	 * 
	 * @param errorMessage
	 */
	public static void error(String errorMessage) {
		if (getTest() != null)
			getTest().error("<font color=\"red\">" + errorMessage + "</font>");
	}

	/**
	 * To log the given message to the reporter at FAIL level
	 * 
	 * @param failMessage
	 * @throws IOException 
	 */
	public static void fail(String failMessage, String screenshotPath) throws IOException {
		if (getTest() != null)
			getTest().fail("<font color=\"red\">" + failMessage + "</font>", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());//log(Status.FAIL, "<font color=\"red\">" + failMessage + "</font>" + getTest().addScreenCaptureFromPath(screenshotPath));
	}

	/**
	 * To log the given message to the reporter at Error level
	 * 
	 * @param errorMessage
	 * @throws IOException 
	 */
	public static void error(String errorMessage, String screenshotPath) throws IOException {
		if (getTest() != null)
			getTest().error("<font color=\"red\">" + errorMessage + "</font>", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
	}

	/**
	 * To log the given message to the reporter at SKIP level
	 * 
	 * @param message
	 */
	public static void skip(String message) {
		if (getTest() != null)
			getTest().skip("<font color=\"orange\">" + message + "</font>");//log(Status.SKIP, "<font color=\"orange\">" + message + "</font>");
	}

	/**
	 * To log the given message to the reporter at SKIP level
	 * 
	 * @param message
	 * @throws IOException 
	 */
	public static void skip(String message, String screenshotPath) throws IOException {
		if (getTest() != null)
			getTest().skip("<font color=\"orange\">" + message + "</font>", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());//log(Status.SKIP, "<font color=\"orange\">" + message + "</font>" + getTest().addScreenCaptureFromPath(screenshotPath));
	}

	/**
	 * To print the stack trace of the given error/exception
	 * 
	 * @param t
	 */
	public static void logStackTrace(Throwable t) {
		if (t instanceof SkipException) {
			if (getTest() != null)
				getTest().skip(t);//log(Status.SKIP, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(t) + "</div>");
		} else {
			if (getTest() != null)
				getTest().fail(t);//log(Status.FAIL, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(t) + "</div>");
		}
	}

	/**
	 * To add attributes to a extent test instance
	 * 
	 * @param attribs
	 */
	public static void addAttribute(String... attribs) {
		if (getTest() != null)
			getTest().assignAuthor(attribs);
	}

	/**
	 * To end an extent test instance
	 */
	public static void endTest() {
		//getReportInstance(Reporter.getCurrentTestResult()).endTest(getTest());
		getReportInstance(Reporter.getCurrentTestResult()).flush();
	}

	/**
	 * To change the test run status to SKIP (to be used with retry analyzer)
	 * 
	 * @param result
	 */
	public static void setTestStatusAsSkip(ITestResult result) {
		try {
			/*ExtentTest test = tests.get(result.hashCode());
			test.getTest().getLogList().forEach(log -> {
				if (log.getStatus() == Status.ERROR || log.getStatus() == Status.FAIL || log.getStatus() == Status.FATAL) {
					log.setStatus(Status.SKIP);
				}
			});
			test.getTest().setStatus(Status.SKIP);*/
		} catch (Exception e) {
			logStackTrace(e);
		}
	}

	/**
	 * To flush and close the report instance
	 * 
	 * @param allTestCaseResults
	 * @param outdir
	 */
	public static void closeReport(List<ITestResult> allTestCaseResults, String outdir) {
		if (isReportClosed) {
			return;
		}
		if (extentReport == null && allTestCaseResults.size() > 0) {
			getReportInstance(allTestCaseResults.get(0));
		} else if (extentReport == null && allTestCaseResults.size() == 0) {
			/*report = new ExtentReports(outdir + File.separator + "ExtentReport.html", true);
			if (configFile.exists()) {
				report.loadConfig(configFile);
			}
			report.endTest(report.startTest("Empty TestNG Suite", "To run tests, please add '@Test' annotation to your test methods"));*/
			ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(outdir + File.separator + "ExtentReport.html");
			extentReport = new ExtentReports();
			extentReport.attachReporter(htmlReporter);
			extentReport.flush();
		}
		if (extentReport != null) {
			String testName = null;
			ExtentTest extentTest = null;
			Integer hashCode = 0;
			for (ITestResult iTestResult : allTestCaseResults) {
				testName = getTestName(iTestResult);
				hashCode = iTestResult.hashCode();
				if (!tests.containsKey(hashCode)) {
					extentTest = extentReport.createTest(testName, iTestResult.getMethod().getDescription() == null ? "" : iTestResult.getMethod().getDescription());
					extentTest.getModel().setStartTime(getTime(iTestResult.getStartMillis()));
					extentTest.assignCategory(iTestResult.getMethod().getGroups());
					List<String> output = Reporter.getOutput(iTestResult);
					for (String step : output) {
						if (step.contains("test-message")) {
							extentTest.log(Status.INFO, step);
						} else {
							extentTest.log(Status.FATAL, step);
						}
					}
					setInterruptedTestStatus(iTestResult, extentTest);
					extentTest.getModel().setEndTime(getTime(iTestResult.getEndMillis()));
					tests.put(hashCode, extentTest);
				} else {
					extentTest = tests.get(hashCode);
					if (extentTest.getModel().getEndTime() == null) {
						setInterruptedTestStatus(iTestResult, extentTest);
						extentTest.getModel().setEndTime(getTime(iTestResult.getEndMillis()));
					}
				}
			}
			/*for (ExtentTest eTest : tests.values()) {
				report.flush();
			}*/
			extentReport.flush();
			isReportClosed = true;
			//report.close();
		}
	}
}
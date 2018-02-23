package genericLibrary;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import genericLibrary.Log;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

public class Utils {

	public static int snoozeTime = 0;
	public static int snoozeIdx = 0;

	/**
	 * 
	 * @param driver
	 * @throws Exception 
	 */
	public static void waitForPageLoad(WebDriver driver) throws Exception 
	{

		final long startTime = StopWatch.startTime();
		driver.manage().timeouts().implicitlyWait(0,TimeUnit.MILLISECONDS);

		try {
			JavascriptExecutor js=(JavascriptExecutor) driver;
			int snooze = 0;

			while (true && snooze < 100) 		{
				if (js.executeScript("return document.readyState").toString().equalsIgnoreCase("COMPLETE"))
					break;
				Thread.sleep(100);
				snooze++;
			}//While(true)

			Thread.sleep(2000);
			new WebDriverWait(driver,60).pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(
					By.cssSelector("div[id='dvBlockPage'][class='blockPage'],img[src*='inprogress.gif']")));
			Thread.sleep(1000);
			//			try {
			//				if (driver.findElement(By.cssSelector("div[class*='ui-draggable']>div>div[class='window_buttons']>button[class='window_ok']")).isDisplayed()) {
			//					driver.findElement(By.cssSelector("div[class*='ui-draggable']>div>div[class='window_buttons']>button[class='window_ok']")).click();
			//				}
			//			}
			//			catch(Exception e) {
			//				if(e.getClass().toString().contains("NoSuchElementException"))
			//					Log.message("Successfully logged in without Error dialog.");
			//				else
			//					Log.exception(new Exception("Problems in Page Loading..."), driver);
			//			}
			Log.message("Successfully logged in without Error dialog.");
		}
		catch(Exception e){
			if(!e.getClass().toString().contains("NoSuchElementException"))
				Log.exception(new Exception("Problems in Page Loading..."), driver);
			else
				return;
		}

		final By spinner = By.cssSelector("div[id='dvBlockPage'][class='blockpage']>img[src*='load.gif'][style='display: inline;'],img[src*='inprogress.gif']");
		final FluentWait <WebDriver> wait = new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class, StaleElementReferenceException.class).pollingEvery(250, TimeUnit.MILLISECONDS);

		wait.until(new ExpectedCondition <Boolean>() {

			final public Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(spinner);
				for (WebElement element : spinners) { if (element.isDisplayed()) return false; }
				return true;
			}
		});

		driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
		Log.event("Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
	}

	/**
	 * fluentWait: This method is to wait until the current progress gets completed
	 * @param driver
	 * @return None
	 * @throws Exception
	 */
	public static void fluentWait(WebDriver driver) throws Exception {

		try
		{
			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String defaultTimeOut = "500";
			String timeoutIndex = (xmlParameters.getParameter("timeoutIndex") != null) ? xmlParameters.getParameter("timeoutIndex") : defaultTimeOut;
			Utils.fluentWait(driver, Integer.parseInt(timeoutIndex));
		}
		catch (Exception e) {
			throw new Exception(e);
		} // End catch

	} // End fluentWait

	/**
	 * fluentWait: This method is to wait until the current progress gets completed
	 * @param driver
	 * @param timeoutIdx
	 * @return None
	 * @throws Exception
	 */
	public static void fluentWait(WebDriver driver, int timeoutIdx) throws Exception {

		final long startTime = StopWatch.startTime();
		snoozeIdx ++;
		//driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);

		try {

			int snooze = 0;

			// Wait until page or frame loads
			JavascriptExecutor js = (JavascriptExecutor) driver;

			while (true && snooze < timeoutIdx)  {//Waits for page load

				try {

					if (js.executeScript("return document.readyState").toString().equalsIgnoreCase("COMPLETE") && !driver.findElement(By.cssSelector("div[id='start']")).isDisplayed())
						break;
					else
						Thread.sleep(100);
				}

				catch (WebDriverException e) {
					break;
				}

				snooze++;
			}

			snooze = 0;

			while (true && snooze < timeoutIdx) { //Waits for Active jQuery

				try {

					try{
						Alert alert = driver.switchTo().alert();

						if(alert != null)
							throw new Exception("Unexpected alert open in fluentWait: " + alert.getText());

					}
					catch(NoAlertPresentException e1){}

					String jQueryRes = js.executeScript("return jQuery.active").toString();		

					if (jQueryRes.equalsIgnoreCase("0")) 
						break;
					else 
						Thread.sleep(100);
				}
				catch (WebDriverException e) {
					break;
				}

				snooze++;

			} //End while for Active jQuery


			if(snooze >= timeoutIdx)
				throw new Exception("jQuery calls did not finish in reasonable time");

			snooze = 0;

			while (true && snooze < timeoutIdx)  {//Waits for page load

				try {

					if (js.executeScript("window.onload=function(){};return true;").toString().equalsIgnoreCase("true"))
						break;
					else
						Thread.sleep(100);
				}

				catch (WebDriverException e) {
					break;
				}

				snooze++;
			}

			String waitCssSelector = "div[style*='waiting_overlay.png'],img[src*='inprogress.gif'],div[id='inProgressDialog'],"
					+ "div[id='dvBlockPage'][class='blockpage']>img[src*='load.gif'][style='display: inline;']"; //Wait until the operation completes

			if (Utils.isMFilesDialogExists(driver) && driver.findElements(By.cssSelector("div[style*='waiting_overlay.png']")).size() > 0)
				return;

			snooze = 0;

			while (true && snooze < timeoutIdx) { //Waits for spinners
				try {
					List<WebElement> waitElements = driver.findElements(By.cssSelector(waitCssSelector));


					if (waitElements.size() == 0 || !waitElements.get(0).isDisplayed())
						break;
					else 
						Thread.sleep(100);	
				}

				catch (WebDriverException e) {
					break;
				}

				snooze++;

			} //End while for spinners

			if(snooze >= timeoutIdx)
				throw new Exception("Spinner element did not disappear in reasonable time");

		} // End try
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException") || e.getClass().toString().contains("StaleElementReferenceException"))
				return;
			else
				throw new Exception("Exception at Utils.fluentWait : "+ e);
		} // End catch

		finally {

			long elapsedTime = StopWatch.elapsedTime(startTime);
			//Log.event("Utils.fluentWait : Fluent wait operation completed", elapsedTime);
			snoozeTime = snoozeTime + (int) elapsedTime;

			//driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		}

	} // End fluentWait


	/**
	 * waitForElement: This method is to wait until element gets displayed
	 * @param driver
	 * @param pElement
	 * @return None
	 * @throws Exception
	 */
	public static Boolean waitForElement(WebDriver driver, WebElement pElement) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			int _TimeToWait = 10;

			for (int i = 0; i < _TimeToWait; i++) {

				try {
					if (pElement.isDisplayed()) return true;
				}
				catch (Exception e1) {    }
			}

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at Utils.waitForElement : ", e);
		} //End catch

		finally {
			Log.event("Utils.fluentWait : Fluent wait operation completed", StopWatch.elapsedTime(startTime));
		}

		return false;

	} //End waitForElement

	/**
	 * isWaitingOverlayExists: This method is to check if waiting overlay icon exists
	 * @param driver
	 * @return None
	 * @throws Exception
	 */
	public static Boolean isWaitingOverlayExists(WebDriver driver) throws Exception {

		try {

			WebElement	waitOverlay = driver.findElement(By.cssSelector("div[style*='waiting_overlay.png']"));

			if (waitOverlay.isDisplayed())
				return true;

		} //End try
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw e;
		} //End catch

		return false;
	} //End isWaitingOverlayExists

	/**
	 * isMFilesDialogExists: This method is to check the existence of the Message dialog
	 * @param driver
	 * @return true if message dialog exists else false
	 * @throws Exception
	 */
	public static Boolean isMFilesDialogExists(WebDriver driver) throws Exception {

		try {

			WebElement mDialog = driver.findElement(By.cssSelector("div[role='dialog']:not([style*='display: none'])")); //Web element div of the error message

			if (mDialog.isDisplayed())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException") || e.getClass().toString().contains("StaleElementReferenceException")) 
				return false;
			else	
				throw e;
		} //End catch

	} //End Exists

	/**
	 * isInProgressOverLayExists: This method is to check if In Progress icon overlay icon exists
	 * @param driver
	 * @return None
	 * @throws Exception
	 */
	public static Boolean isInProgressOverLayExists(WebDriver driver) throws Exception {

		try {

			WebElement	inProgressOverLay = driver.findElement(By.cssSelector("img[src*='inprogress.gif'],div[id='inProgressDialog']"));

			if (inProgressOverLay.isDisplayed())
				return true;

		} //End try
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw e;
		} //End catch

		return false;
	} //End isInProgressOverLayExists

	/**
	 * 
	 * @param driver
	 * @param element
	 */
	public void rightClickElement(WebDriver driver,WebElement element)
	{
		Actions action= new Actions(driver); 
		action.contextClick(element);

	}

	public static String getClipboardContents()
	{
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);

		boolean hasTransferableText =
				(contents != null) &&
				contents.isDataFlavorSupported(DataFlavor.stringFlavor);

		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException | IOException ex){
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;

	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static void isLogOutPromptDisplayed(WebDriver driver) throws Exception
	{
		try{
			Thread.sleep(1000);
			WebElement logOutPrompt=driver.findElement(By.cssSelector("div[class*='ui-draggable']:not([style*='display: none;'])>div[class*='promptWindow']>table[class='buttons']>tbody>tr>td>button[class*='ok']"));
			logOutPrompt.click();
			Utils.fluentWait(driver);
		}
		catch(Exception e){
			if(!e.getClass().toString().contains("NoSuchElementException")) {
				Log.exception(new Exception("Cound not found/click 'LogOut prompt' element."),driver);
			}
			else {
				Log.message("LogOut Prompt not Displayed.");
			}
		}

	}
	public void fetchRegistryValues()
	{
		//		        /* show */
		//		        var wsh = new ActiveXObject("WScript.Shell");
		//		        var key = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\my-script";
		//		        wsh.RegWrite (key, sWorkingPath + "my-script.hta", "REG_SZ");
	}
	public static String getCurrentDateTime() {

		/* --------------------------------------------------------------------
		 * Function Name	: getCurrentDateTime
		 * Description		: This method is to get current date and time
		 * @Param					: None
		 * @Return				: Current date and time in the string format
		 -----------------------------------------------------------------------*/

		//Variable Declaration
		String dateTime = "";

		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			dateTime = dateFormat.format(date);

		} //End try

		catch (Exception e) {
			throw e;
		} //End catch	

		return dateTime;

	} //End getCurrentDateTime

}

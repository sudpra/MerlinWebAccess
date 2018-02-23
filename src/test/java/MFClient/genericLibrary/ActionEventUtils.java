package genericLibrary;


import java.awt.Robot;
import java.awt.event.KeyEvent;

import genericLibrary.Log;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

public class ActionEventUtils {

	/**
	 * pressCTRLKey: Press CTRL + Key combination
	 * @param driver
	 * @param key to press with CTRL key
	 * @return None
	 * @throws Exception
	 */
	public static void pressCTRLKey(WebDriver driver, String key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			if (browser.equalsIgnoreCase("IE")) {
				Robot robot=new Robot();
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_I);
				robot.keyRelease(KeyEvent.VK_I);
				robot.keyRelease(KeyEvent.VK_CONTROL);

			}
			else {
				Actions action = new Actions(driver);
				action.keyDown(Keys.CONTROL).sendKeys(key).perform();
				Utils.fluentWait(driver);
			}



		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressCTRLKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressCTRLKey : CTRL + Key press combination operation completed", elapsedTime);		
		}

	} // End pressCTRLKey

	/**
	 * pressCTRLKey: Press CTRL + Key combination
	 * @param driver
	 * @param key to press with CTRL key
	 * @return None
	 * @throws Exception
	 */
	public static void pressCTRLKey(WebDriver driver, Keys key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*Robot robot=new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_CONTROL);*/

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressCTRLKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressCTRLKey : CTRL + Key press combination operation completed", elapsedTime);		
		}

	} // End pressCTRLKey

	/**
	 * pressSHIFTKey: Press SHIFT + Key combination
	 * @param driver
	 * @param key to press with SHIFT key
	 * @return None
	 * @throws Exception
	 */
	public static void pressSHIFTKey(WebDriver driver, String key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.keyDown(Keys.SHIFT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressSHIFTKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressSHIFTKey : SHIFT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressSHIFTKey

	/**
	 * pressSHIFTKey: Press SHIFT + Key combination
	 * @param driver
	 * @param key to press with SHIFT key
	 * @return None
	 * @throws Exception
	 */
	public static void pressSHIFTKey(WebDriver driver, Keys key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.keyDown(Keys.SHIFT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressSHIFTKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressSHIFTKey : SHIFT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressSHIFTKey

	/**
	 * pressALTKey: Press ALT + Key combination
	 * @param driver
	 * @param key to press with ALT key
	 * @return None
	 * @throws Exception
	 */
	public static void pressALTKey(WebDriver driver, String key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.keyDown(Keys.ALT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressALTKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressALTKey : ALT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressSHIFTKey

	/**
	 * pressALTKey: Press ALT + Key combination
	 * @param driver
	 * @param key to press with ALT key
	 * @return None
	 * @throws Exception
	 */
	public static void pressALTKey(WebDriver driver, Keys key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.keyDown(Keys.ALT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressALTKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressALTKey : ALT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressSHIFTKey

	/**
	 * pressCtrlShiftLKey: Press CTRL + SHIFT + Key combination
	 * @param driver
	 * @param key to press with CTRL + SHIFT
	 * @return None
	 * @throws Exception
	 */
	public static void pressCtrlShiftLKey(WebDriver driver, String key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*Robot robot=new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_CONTROL);*/

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressCtrlShiftLKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressCtrlShiftLKey : CTRL + SHIFT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressCTRLKey

	/**
	 * pressCtrlShiftLKey: Press CTRL + SHIFT + Key combination
	 * @param driver
	 * @param key to press with CTRL + SHIFT key
	 * @return None
	 * @throws Exception
	 */
	public static void pressCtrlShiftLKey(WebDriver driver, Keys key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			/*Robot robot=new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_CONTROL);*/

			Actions action = new Actions(driver);
			action.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressCtrlShiftLKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressCtrlShiftLKey : CTRL + SHIFT + Key press combination operation completed", elapsedTime);		
		}

	} // End pressCTRLKey

	/**
	 * pressKey: Press keys in keyboard using key as Keys
	 * @param driver
	 * @param key to press
	 * @return None
	 * @throws Exception
	 */
	public static void pressKey(WebDriver driver, Keys key) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.sendKeys(key).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressKey : Key press operation completed", elapsedTime);		
		}

	} // End pressKey

	/**
	 * pressKey: Press keys in keyboard using key as string
	 * @param driver
	 * @param keys to press
	 * @return None
	 * @throws Exception
	 */
	public static void pressKey(WebDriver driver, String keys) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Actions action = new Actions(driver);
			action.sendKeys(keys).perform();
			Utils.fluentWait(driver);


		} // End try
		catch (Exception e) {
			throw new Exception("Exception at KeyEventUtils.pressKey : " + e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressKey : Key press operation completed", elapsedTime);		
		}
	}
	/**
	 * Click : This function is used for browser specific click function
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void click(WebDriver driver,WebElement element) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");

			if (browser.equalsIgnoreCase("edge") || browser.equalsIgnoreCase("safari"))
			{
				((JavascriptExecutor) driver).executeScript("confirm = function(message){return true;};");
				((JavascriptExecutor) driver).executeScript("alert = function(message){return true;};");
				((JavascriptExecutor) driver).executeScript("prompt = function(message){return true;}");
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
			}
			else
				element.click();

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at ActionEventUtils.click: Unable to click the element : " + e, e);
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.click : Click completed.", elapsedTime);		
		}

	}//End Click function

	/**
	 * moveToElemAndRightClick : This function is used for rightClick function
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void moveToElemAndRightClick(WebDriver driver, WebElement element) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");
			Utils.fluentWait(driver);

			if (browser.equalsIgnoreCase("Safari")){
				String javaScript = "var evt = arguments[0].ownerDocument.createEvent(\"HTMLEvents\");"
						+ "evt.initEvent('contextmenu', true, true);"
						+ "if (document.createEventObject) {  return arguments[0].fireEvent('oncontextmenu', evt)}"
						+ "else{return !arguments[0].dispatchEvent(evt); }";
				((JavascriptExecutor) driver).executeScript(javaScript, element);
			}	
			else{
				Actions action=new Actions(driver);
				action.moveToElement(element, 10, element.getSize().height/2).contextClick().build().perform();
			}

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at ActionEventUtils.moveToElemAndRightClick : Unable to Rightclick the element : " + e, e);
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.moveToElemAndRightClick : Rightclick completed.", elapsedTime);		
		}

	}//End moveToElemAndRightClick function

	/**
	 * rightClick : This function is used for rightClick function
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void rightClick(WebDriver driver, WebElement element) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");
			Utils.fluentWait(driver);

			if (browser.equalsIgnoreCase("Safari")){
				String javaScript = "var evt = arguments[0].ownerDocument.createEvent(\"HTMLEvents\");"
						+ "evt.initEvent('contextmenu', true, true);"
						+ "if (document.createEventObject) {  return arguments[0].fireEvent('oncontextmenu', evt)}"
						+ "else{return !arguments[0].dispatchEvent(evt); }";
				((JavascriptExecutor) driver).executeScript(javaScript, element);
			}	
			else{
				Actions action=new Actions(driver);
				action.contextClick(element).build().perform();
			}

		}//End try
		catch (Exception e) {
			throw new Exception("Exception at Unable to Rightclick the element : "+e,e);
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.rightClick : Rightclick completed.", elapsedTime);		
		}

	}//End moveToElemAndRightClick function

	/**
	 * moveToElement : This function is used for moveToElement
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void moveToElement(WebDriver driver, WebElement element) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");
			Utils.fluentWait(driver);

			if ( browser.equalsIgnoreCase("Safari") || browser.equalsIgnoreCase("Edge")){
				String javaScript = "var evObj = document.createEvent('MouseEvents');" 
						+ "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" 
						+ "arguments[0].dispatchEvent(evObj);";
				((JavascriptExecutor) driver).executeScript(javaScript, element);
			}
			else {
				Actions action=new Actions(driver);
				action.moveToElement(element).perform();
			}

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at ActionEventUtils.moveToElement : "+ e);			
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.moveToElement : Moved to Element.", elapsedTime);		
		}

	}//End moveToElement function


	/**
	 * moveToElementAndClick : This function is used for moveToElement and click the element
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void moveToElementByOffsetAndClick(WebDriver driver, WebElement element, int x, int y) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");
			Utils.fluentWait(driver);

			if (browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Safari")){
				String javaScript = "var rect = arguments[0].getBoundingClientRect();"
						+ "document.elementFromPoint(rect.top, rect.bottom-2).click();";
				((JavascriptExecutor) driver).executeScript(javaScript, element);
			}
			else {
				Actions action=new Actions(driver);
				action.moveToElement(element).moveByOffset(x, y).click().perform();
			}

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at ActionEventUtils.moveToElementByOffsetAndClick : "+ e);		
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.moveToElementByOffsetAndClick : Moved to Element and clicked the element.", elapsedTime);		
		}

	}//End moveToElementAndClick function

	/**
	 * moveToElementAndClick : This function is used for moveToElement and click the element
	 * 
	 * @param driver
	 * @param element
	 * @throws Exception
	 */
	public static void moveToElementAndClick(WebDriver driver, WebElement element) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			String browser = xmlParameters.getParameter("driverType");
			Utils.fluentWait(driver);

			if ( browser.equalsIgnoreCase("Edge") || browser.equalsIgnoreCase("Firefox")){
				Actions action= new Actions(driver);
				action.moveToElement(element).click(element).build().perform();
			}
			else
				ActionEventUtils.click(driver, element);

		}//End try
		catch (Exception e) {
			throw new Exception ("Exception at ActionEventUtils.moveToElementAndClick : "+ e);			
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("ActionEventUtils.moveToElementAndClick : Moved to Element and clicked the element.", elapsedTime);		
		}

	}//End moveToElementAndClick function


	/*var myEvt = document.createEvent('MouseEvents');
	myEvt.initMouseEvent(
	   'click'          // event type
	   ,true           // can bubble?
	   ,true           // cancelable?
	   ,window      // the event's abstract view (should always be window)
	   ,1              // mouse click count (or event "detail")
	   ,100           // event's screen x coordinate
	   ,200           // event's screen y coordinate
	   ,100           // event's client x coordinate
	   ,200           // event's client y coordinate
	   ,false         // whether or not CTRL was pressed during event
	   ,false         // whether or not ALT was pressed during event
	   ,false         // whether or not SHIFT was pressed during event
	   ,false         // whether or not the meta key was pressed during event
	   ,1             // indicates which button (if any) caused the mouse event (1 = primary button)
	   ,null          // relatedTarget (only applicable for mouseover/mouseout events)
	);*/

}

package genericLibrary;

import genericLibrary.Log;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class KeyEventUtils {
	
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
				throw new Exception("Exception at KeyEventUtils.pressCtrlShiftLKey : "+ e, e);
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
	 * pressTAB: Press TAB
	 * @param driver
	 * * @return None
	 * @throws Exception
	 */
	public static void pressTAB(WebDriver driver) throws Exception {

		final long startTime = StopWatch.startTime();

		try {
			
			/*Robot robot=new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_I);
			robot.keyRelease(KeyEvent.VK_CONTROL);*/
			
			Actions action = new Actions(driver);
			action.sendKeys(Keys.TAB).perform();
			Utils.fluentWait(driver);
			

		} // End try
		catch (Exception e) {
				throw new Exception("Exception at KeyEventUtils.pressTAB : "+e, e);
		} // End catch

		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("KeyEventUtils.pressTAB : TAB press operation completed", elapsedTime);		
		}

	} // End pressCTRLKey
  
}

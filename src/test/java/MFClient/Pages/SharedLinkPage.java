package MFClient.Pages;

import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

public class SharedLinkPage extends LoadableComponent <SharedLinkPage>{
	
	private boolean pageLoaded = false;
	private final WebDriver driver;
	
	/**
	 * Page Factory elements
	 */
	@FindBy(how=How.ID,using="valignmid3")
	private WebElement divWhole;
		
	/**
	 * SharedLinkPage : SharedLinkPage constructor
	 * @param driver Webdriver
	 * @throws Exception
	 */
	public SharedLinkPage(final WebDriver driver) throws Exception {
		
		final long startTime = StopWatch.startTime();
		
		try {
			this.driver = driver;
			ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 2);
			PageFactory.initElements(finder, this);
		} //End try
		
		catch(Exception e) {
			throw new Exception ("Exception at SharedLinkPage constructor : " + e);
		} //End catch
		finally {
    	Log.event("AddServerPage Constructor", StopWatch.elapsedTime(startTime));
    } //End finally
		
	} //End SharedLinkPage
	
	final protected void isLoaded() {
		try {
			
			if (!(driver.getCurrentUrl().toLowerCase().contains("/SharedLinks.aspx"))) {
	
				if (!pageLoaded)
					Assert.fail();
				
				Log.fail("Expected page was a WebAccess Configuration page, but current page is not a Configuration page." + "Current Page is: " + driver.getCurrentUrl(), driver); // Verify whether is Configuration page
			
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	} //End isLoaded
	
	final protected void load() {
		
		try {
			Utils.waitForPageLoad(driver);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		pageLoaded = true;
	}
	
	/**
	 * getSharedDocumentName: Gets the document Name
	 * @param None
	 * @return Name of the document
	 * @throws Exception
	 */
	public String getSharedDocumentName() throws Exception {
    	
		final long startTime = StopWatch.startTime();
    	
    try {
    	
    	String docName = this.driver.findElement(By.cssSelector("p")).getText().trim();
	    return docName;
	    	    
    }//End try
		
    catch (Exception e) {
			throw new Exception("Exception at AddServerPage.connectToServer : " + e);
		} //End catch
		
    finally {
			Log.event("SharedLinkPage.getSharedDocumentName.", StopWatch.elapsedTime(startTime));
		} //End finally
    
  } //getSharedDocumentName
	
} //End SharedLinkPage
package MFClient.Wrappers;


import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.StopWatch;
import genericLibrary.Utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PreviewPane {

	//Variable Declaration
	WebDriver driver=null;

	/**
	 * PageFactory element initialization
	 */
	@FindBy(how=How.ID, using="previewPane")
	private WebElement previewPanel; //Stores the instance of the preview pane

	@FindBy(how=How.ID, using="rightPaneTabs")
	private WebElement rightPane; //Stores the instance of the preview pane

	/**
	 * PreviewPane : Constructor to instantiate Right pane class
	 * @param driver
	 * @throws Exception
	 */
	public PreviewPane(final WebDriver driver) throws Exception {
		try {

			this.driver = driver;
			PageFactory.initElements(this.driver, this);

		} //End try		
		catch (Exception e) {
			throw new Exception("Exception at PreviewPane.PreviewPane :"+e.getMessage(), e);
		} //End catch

	} //End PreviewPane

	/**
	 * isTabExists : To verify the existence of tab in the preview pane
	 * @param tabName - Name of the tab to check for existence
	 * @return true if item exists; false if not
	 * @throws Exception
	 */
	public Boolean isTabExists(String tabName)throws Exception {

		try {

			List<WebElement> tabElement = this.rightPane.findElements(By.cssSelector("ul[id='draggingTest']>li>a")); //Stores the web element with tr tag
			int rowCt = tabElement.size(); //Number of tabs with specified attribute

			for (int itemIdx=0; itemIdx<rowCt; itemIdx++) //Loops through to identify the existence of item
				if(tabElement.get(itemIdx).isDisplayed())//Checks whether that element is displayed in the view
					if (tabElement.get(itemIdx).getText().toUpperCase().equalsIgnoreCase(tabName.toUpperCase())) 
						return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				throw new Exception ("Exception in PreviewPane.isTabExists : "+e.getMessage(), e);
		} //End catch

		return false;

	} //End isTabExists


	/**
	 * isPreviewPaneHidden : To check if preview pane is hidden
	 * @param None
	 * @return true if preview pane is hidden; false if not 
	 * @throws Exception
	 */
	public Boolean isPreviewPaneHidden()throws Exception {

		try {

			WebElement rightPaneIcon = this.rightPane.findElement(By.cssSelector("li>span[id*='hideshowRightPane']")); //Stores the web element with tr tag

			if (rightPaneIcon.getAttribute("style").toString().toUpperCase().contains("SHOW_METADATA.PNG"))
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in PreviewPane.isPreviewPaneHidden :"+e.getMessage(), e);
		} //End catch

	} //End togglePreviewPane

	/**
	 * togglePreviewPane : To hide or show preview pane
	 * @param isHidden - true to hide; false to show
	 * @return true if operation successful as per param; false if not 
	 * @throws Exception
	 */
	public Boolean togglePreviewPane(Boolean isHidden)throws Exception {

		try {

			WebElement rightPaneIcon = this.rightPane.findElement(By.cssSelector("li[class='rightpaneIcon']>span[id='hideshowRightPaneIcon']")); //Stores the web element with tr tag

			if (this.isPreviewPaneHidden() && !isHidden)
				ActionEventUtils.click(driver, rightPaneIcon);
			//rightPaneIcon.click();
			else if (!this.isPreviewPaneHidden() && isHidden)
				ActionEventUtils.click(driver, rightPaneIcon);
			//rightPaneIcon.click();

			if (isHidden && this.isPreviewPaneHidden())
				return true;
			else if (!isHidden && !this.isPreviewPaneHidden())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			throw new Exception ("Exception in PreviewPane.togglePreviewPane : "+e.getMessage(), e);
		} //End catch

	} //End togglePreviewPane

	/**
	 * clickPreviewPaneTabs : To Click on preivew pane tabs
	 * @param tabName - Name of the tab
	 * @return true if operation successful as per param; false if not 
	 * @throws Exception
	 */
	public Boolean clickPreviewPaneTabs(String tabName) throws Exception {

		try {

			List<WebElement> previewPaneTabs=driver.findElements(By.cssSelector("div[id='rightPaneTabs']>ul[id='draggingTest']>li"));

			for(WebElement tab:previewPaneTabs) {
				if(tab.getText().trim().equalsIgnoreCase(tabName)) {
					// tab.findElement(By.cssSelector("a")).click();
					ActionEventUtils.click(driver,tab.findElement(By.cssSelector("a")));
					return true;
				} //End if

			} //End for
		} //End try
		catch(Exception e) {
			if (e.getMessage().contains("NoSuchElementException") || e.getMessage().contains("Unable to locate element") || e.getMessage().contains("Unable to find element"))
				return false;
			else
				throw new Exception("Exception at PreviewPane.clickPreviewPaneTabs :"+e.getMessage(), e);
		} //End catch
		return false;
	}//clickPreviewPaneTabs

	public void rightClickPreviewTab() throws Exception{  

		try{

			WebElement rightpane = this.driver.findElement(By.cssSelector("div[id='rightPaneTabs']"));
			Utils.fluentWait(driver);

			WebElement rightpaneMetadata = rightpane.findElement(By.cssSelector("ul[id='draggingTest']>li[id='metadataTab']"));
			Actions action = new Actions(this.driver);
			try {
				action.moveToElement(rightpaneMetadata).contextClick().build().perform();
			}
			catch(Exception e1) {
				((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('contextmenu',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 2,null); arguments[0].dispatchEvent(evt);",rightpaneMetadata);
			}

		}
		catch (Exception e) {
			throw new Exception("Exception at PreviewPane.rightClickPreviewTab :"+e.getMessage(), e);
		}//End catch
	}

	/**
	 * Verify the loading symbol is displayed in preview pane
	 * @return true if loading symbol is displayed ; false if not displayed
	 * @throws Exception 
	 */
	public boolean isLoadingSymbolDisplayed() throws Exception{

		try{

			//WebDriverWait wait = new WebDriverWait(driver, 10);
			//WebElement loadingSymbol = driver.findElement(By.cssSelector("div[id='loadingPDF'][style='display: block;']"));
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.name("loadingSymbol")));
			WebElement pdfObjectiFrame = driver.findElement(By.cssSelector("div[id='preview']>div[id='pdfObject']>iframe"));
			driver.switchTo().frame(pdfObjectiFrame);
			WebElement progressBar = driver.findElement(By.cssSelector("div[id='pdf-progress-bar']"));

			if(progressBar.isDisplayed())
				return true;
			else
			{
				driver.switchTo().defaultContent();
				return (this.isContentDisplayed());
			}

		}//End try
		catch(Exception e){
			if (e.getMessage().contains("NoSuchElementException") || e.getMessage().contains("Unable to locate element") || e.getMessage().contains("Unable to find element"))
			{
				driver.switchTo().defaultContent();
				return (this.isContentDisplayed());
			}
			else
				throw new Exception("Exception at PreviewPane.isLoadingSymbolDisplayed :"+e.getMessage(), e);
		}//End catch
		finally{
			driver.switchTo().defaultContent();//Switches to the default content
		}
	}//End isLoadingSymbolDisplayed


	/**
	 * Verify the Content is displayed in preview pane for the corresponding Files
	 * @return true if loading symbol is displayed ; false if not displayed
	 * @throws Exception 
	 */
	public boolean isContentDisplayed() throws Exception{

		final long startTime = StopWatch.startTime();

		try{
			Log.event("PreviewPane.isContentDisplayed: Started to Check if content is displayed or not...", StopWatch.elapsedTime(startTime));

			WebElement pdfObjectiFrame = driver.findElement(By.cssSelector("div[id='preview']>div[id='pdfObject']>iframe"));
			driver.switchTo().frame(pdfObjectiFrame);

			new WebDriverWait(this.driver, 360).ignoring(NoSuchElementException.class)
			.pollingEvery(2050,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf
					(driver.findElement(By.cssSelector("div[id='DocumentViewer']"))));

			Log.event("PreviewPane.isContentDisplayed: Waited for visibility of content...", StopWatch.elapsedTime(startTime));

			WebElement content = driver.findElement(By.cssSelector("div[id='ui-display'] div[id='DocumentViewer']"));
			if(content.isDisplayed())
				return true;
			else
				return false;

		}//End try
		catch(Exception e){
			if (e.getMessage().contains("NoSuchElementException") || e.getMessage().contains("Unable to locate element") || e.getMessage().contains("Unable to find element"))
				return false;
			else
				throw new Exception("Exception at PreviewPane.isContentDisplayed :"+e.getMessage(), e);
		}//End catch
		finally{
			driver.switchTo().defaultContent();//Switches to the default content
			Log.event("PreviewPane.isContentDisplayed: Checked if content is displayed or not...", StopWatch.elapsedTime(startTime));
		}
	}//End isContentDisplayed


	public boolean isPreviewTabObjectNotDisplayed() throws Exception {

		try{
			//WebElement content = driver.findElement(By.cssSelector("div>object[id='pdfObject']>embed[src*='view']"));
			WebElement content = driver.findElement(By.cssSelector("div>object[id='pdfObject']>embed[src*='about:blank']"));
			if(content.isDisplayed())
				return true;
			else
				return false;
		}//End try
		catch(Exception e){
			if (e.getMessage().contains("NoSuchElementException") || e.getMessage().contains("Unable to locate element") || e.getMessage().contains("Unable to find element"))
				return false;
			else
				throw new Exception("Exception at PreviewPane.isPreviewTabObjectNotDisplayed :"+e.getMessage(), e);
		}//End catch
	}//End isPreviewTabWhiteScreenDisplayed



	/**
	 * Click the Preview tab in home page
	 * @throws Exception
	 */
	public void clickMetadataTab() throws Exception{

		try{
			WebElement metadataTab = driver.findElement(By.cssSelector("ul li[id='metadataTab']>a"));
			ActionEventUtils.click(driver, metadataTab);
			//metadataTab.click();
			Utils.fluentWait(driver);
		}//End try
		catch(Exception e){
			throw new Exception("Exception at PreviewPane.clickMetadataTab : Unable to click the Metadata tab :"+e.getMessage(), e);
		}//End catch
	}

	/**
	 * Click the Preview tab in home page
	 * @throws Exception
	 */
	public void clickPreviewTab() throws Exception{

		try{

			/*List<WebElement> rightTabs = this.rightPane.findElements(By.cssSelector("ul[id='draggingTest']>li"));

			for (int i=0; i<rightTabs.size(); i++)
				System.out.println(rightTabs.get(i).getText());
			 */
			WebElement previewtab = driver.findElement(By.cssSelector("ul>li[id='previewTab']>a"));
			ActionEventUtils.click(driver, previewtab);
			//previewtab.click();
			Utils.fluentWait(driver);
		}//End try
		catch(Exception e){
			throw new Exception("Exception at PreviewPane.clickPreviewTab : Unable to click the Preview tab :"+e.getMessage(), e);
		}//End catch
	}


	/**
	 * rightClickPrevpaneMetadataTab : This function is used to right click the metadata tab in preview pane
	 * 
	 * @throws Exception
	 */

	public void popoutRightPaneMetadataTab() throws Exception {

		final long startTime = StopWatch.startTime();
		try {
			WebElement previewPaneTabs=driver.findElement(By.cssSelector("div[id='rightPaneTabs']"));
			WebElement metadataTab =  previewPaneTabs.findElement(By.cssSelector("li[id='metadataTab']>a"));

			ActionEventUtils.rightClick(driver, metadataTab);

			List<WebElement> popout = driver.findElements(By.cssSelector("li[class*='metadataContextMenu']"));//get the element in the list

			for (int index=0;index<popout.size();index++) {
				if(popout.get(index).isDisplayed())
					if (popout.get(index).getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").replaceAll("\n", "").trim().equalsIgnoreCase("Pop Out the Metadata Card"))
					{
						ActionEventUtils.click(driver,  popout.get(index));
						break;
					}
				//  popout.get(index).click();
			}

			Utils.fluentWait(driver);

		}//End try
		catch(Exception e){
			throw new Exception("Exception at PreviewPane.popoutRightPaneMetadataTab :"+e.getMessage(), e);
		}//End catch
		finally {
			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("Right click the metadata tab in preview pane", elapsedTime);
		}//End finally
	}//End rightClickPrevpaneMetadataTab


	public void rightpanePopoutMetadataTab() throws Exception {
		final long startTime = StopWatch.startTime();

		try {

			WebElement popoutmetadata = driver.findElement(By.cssSelector("div[id='root-menu-div']>div>ul[class='menu-ul innerbox']"));
			System.out.println(popoutmetadata.getText());
			WebElement popout = popoutmetadata.findElement(By.linkText("li>div>div[class*='menuItemText']"));
			System.out.println(popout.getText());
			ActionEventUtils.click(driver, popout);
			//  popout.click();

		}
		catch(Exception e){
			throw new Exception("Exception at PreviewPane.rightpanePopoutMetadataTab :"+e.getMessage(), e);
		}//End catch

		finally {

			long elapsedTime = StopWatch.elapsedTime(startTime);
			Log.event("Right click the metadata tab", elapsedTime);
		}//End finally

	}//rightpanePopoutMetadataTab

	/**
	 * getPreviewDocumentPageCount: Gets the total page count from the previewed pdf document. The value is read from the
	 * number provided by PDFTron, not by actually counting the page elements.
	 * @return Number of total pages in previewed pdf document as a String
	 * @throws Exception
	 */
	public String getPreviewDocumentPageCount() throws Exception{


		try{
			WebElement pdfObjectiFrame = driver.findElement(By.cssSelector("div[id='preview']>div[id='pdfObject']>iframe"));
			driver.switchTo().frame(pdfObjectiFrame);


			WebElement previewProgressBar = driver.findElement(By.cssSelector("div[id='pdf-progress-bar']"));
			int counter = 0;

			//Wait for progress bar to disappear.
			while(previewProgressBar.isDisplayed() && counter < 20){
				Thread.sleep(500);
				counter++;
			}

			Utils.fluentWait(driver);

			String totalPagesString = driver.findElement(By.cssSelector("div[id='totalPages']")).getText();

			// Remove "/" character from the beginning
			totalPagesString = totalPagesString.substring(1, totalPagesString.length());

			return totalPagesString;
		}
		catch(Exception e){
			throw new Exception("Exception at PreviewPane.getPreviewDocumentPageCount:"+e.getMessage(), e);
		}
		finally{
			driver.switchTo().defaultContent();
		}
	}

}//End Preview pane

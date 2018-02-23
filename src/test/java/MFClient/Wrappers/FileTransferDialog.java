package MFClient.Wrappers;

import genericLibrary.ActionEventUtils;
import genericLibrary.Log;
import genericLibrary.Utils;



import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class FileTransferDialog {
	//Variable Declaration
		public WebElement FiletransferDialog;
		public final WebDriver driver;
			
		/**
		 * FileTransferDialog : Constructor to instantiate MFiles dialog
		 * @param driver
		 * @return None
		 * @throws Exception
		 */
		public FileTransferDialog(final WebDriver driver) throws Exception {
		
			try {
				
				Utils.fluentWait(driver);
				this.driver = driver;
				this.FiletransferDialog = this.driver.findElement(By.cssSelector("div[id='fileTransferPrompt']"));
				
			} //End try
			
			catch (Exception e) {
				if (e.getClass().toString().contains("NoSuchElementException"))
					throw new Exception ("Exception in FiletransferDialog.FiletransferDialog : File transfer dialog is not displayed : "+e.getMessage(), e);
				else
					throw new Exception ("Exception in FiletransferDialog.FiletransferDialog : "+e.getMessage(), e);
			} //End catch
				
		} //End MFilesDialog
		
		
		
		/**
		 * isFileTransferdialogdisplayed : To Verify the existence of preview pane.
		 * @param none
		 * @return true if list view displayed; false if list view is not displayed
		 * @throws Exception
		 */
		public Boolean isFileTransferDialogDisplayed()throws Exception {
		
			try {
				
				if (this.FiletransferDialog.isDisplayed())
					return true;
				else
					return false;
				
			} //End try
			
			catch (Exception e) {
				if (e.getClass().toString().contains("NoSuchElementException")) 
					return false;
				else				
					throw new Exception("Exception at FileTransferDialog.isFileTransferDialogDisplayed : "+e.getMessage(), e);
			} //End catch
		
		} //End isFileTransferdialogdisplayed



		/**
		 * isCrossIconDisplayed : To Verify the existence of preview pane.
		 * @param none
		 * @return true if list view displayed; false if list view is not displayed
		 * @throws Exception
		 */
		public boolean isCrossIconDisplayed() throws Exception {
			
			try{
				List<WebElement> obj = this.FiletransferDialog.findElements(By.cssSelector("button[class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close']"));
			 if(obj.size()==0)
			 {
				 Log.message("Cross icon is not available in Filetransfer dialog");
				 return true;
			 }
			}
			catch (Exception e)
			{
				if (e.getClass().toString().contains("NoSuchElementException"))
					throw new Exception ("Exception at FileTransferDialog.isCrossIconDisplayed : File transfer dialog is not displayed :"+e.getMessage(), e);
				else
					throw new Exception("Exception at FileTransferDialog.isCrossIconDisplayed : "+e.getMessage(), e);
			}
			return false;
		}
		 

		/**
		 * clickOnDialog : To click on the MFiles dialog
		 * @param None
		 * @return Title displayed in the MFiles dialog
		 * @throws Exception
		 */
		public void clickOnDialog() throws Exception {
		
			try {
				ActionEventUtils.click(driver, FiletransferDialog);
				//FiletransferDialog.click();
			
			} //End try
			
			catch (Exception e) {
				throw new Exception("Exception at FileTransferDialog. : "+e.getMessage(), e);
			} //End catch
			
		} //End getTitle
		
		
		/**
		 * dragFileTransferDialog : Try to drag the file transfer dialog
		 * @param None
		 * @return Title displayed in the MFiles dialog
		 * @throws Exception
		 */
		public boolean dragFileTransferDialog() throws Exception 
		{
			 WebElement sourceElement=driver.findElement(By.id("fileTransferPrompt"));  
			 WebElement destinationElement=driver.findElement(By.id("previewPane"));  
			
			try
			{
			 if (sourceElement.isDisplayed() && destinationElement.isDisplayed())
			 {
				Actions builder = new Actions(driver);  // Configure the Action  
				org.openqa.selenium.interactions.Action dragAndDrop = builder.clickAndHold(sourceElement)  
				    .moveToElement(destinationElement)  
				    .release(destinationElement)  
				    .build();  // Get the action  
				 dragAndDrop.perform(); // Execute the Action  
				
				// Check the File transfer dialog is displayed after performing drag & drop action
				 if(!this.isFileTransferDialogDisplayed())
					 return false;
			  } 
			  else 
			  {
				System.out.println("Element was not displayed to drag");
			  }
			
			 } //End try
			catch (StaleElementReferenceException e)
			{
				System.out.println("Element with " + sourceElement + "or" + destinationElement + "is not attached to the page document "
						+ e.getStackTrace());
				
				return false;
		   	} 
			catch (NoSuchElementException e) {
				System.out.println("Element " + sourceElement + "or" + destinationElement + " was not found in DOM "+ e.getStackTrace());
				return false;
			}
			catch (Exception e) {
				throw new Exception("Exception at FileTransferDialog.dragFileTransferDialog : "+e.getMessage(), e);
			} //End catch
			
			return false;
		} //End getTitle
		
		/**
		 * getProgressText : To get the title of the item in file transfer dialog
		 * @param None
		 * @return Title displayed in the MFiles dialog
		 * @throws Exception
		 */
		public String getProgressText(String itemName) throws Exception {
		
			try {
				
				return this.FiletransferDialog.findElement(By.cssSelector("div[id='"+itemName.toLowerCase()+"ProgressContainer']")).getText().replaceAll("&nbsp;", " ").replaceAll("\u00A0"," ").trim();
				
			} //End try
			
			catch (Exception e) {
				throw new Exception("Exception at FileTransferDialog.getProgressText : "+e.getMessage(), e);
			} //End catch
		
		} //End getTitle
		
}

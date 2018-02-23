package MFClient.Wrappers;

import genericLibrary.Utils;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PropertiesPane {
	
	//Variable Declaration
	final WebDriver driver;
	//WebElement propPanel; //Stores the instance of the Properties pane
	
	
	/**
	 * <br>Description		: This method is to instantiate the PropertiesPane.</br>
	 * @param driver
	 * @throws Exception 
	 */
	public PropertiesPane(final WebDriver driver) throws Exception {

		this.driver = driver;
		Utils.fluentWait(driver);
//		try {
//		
////			propPanel = driver.findElement(By.cssSelector("div[id='browseBottom']>div[id='propertiesPane']"));
//			
//			} //End try
//			
//			catch (Exception e) {
//				throw e;
//			} //End catch
		
	} //End constructor ListView
	
	public Boolean isPropertyPaneExists()throws Exception {
		
	/* --------------------------------------------------------------------
	 * Function Name	: Exists
	 * Description		: This method is to check the existence of PropertiesPane.
	 * Input					: None
	 * Output					: true if exists false if not exists
	 -----------------------------------------------------------------------*/
	
		//Variable Declaration
		Boolean isDisplayed = false; //Stores the last item in the bread crumb 
		
		try {
			
			WebElement propPanel=driver.findElement(By.cssSelector("div[id='propertiesPane'][style*='display: block']"));
			if (propPanel.isDisplayed()) {
				isDisplayed = true;
			}
			
		} //End try
		
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) {
				isDisplayed = false;
			}
			else {				
				throw e;
			}
		} //End catch
	
		return isDisplayed;
		
	} //End function Exists
	
	public Boolean isPropertyExists(String propertyName)throws Exception {
		
	/* --------------------------------------------------------------------
	 * Function Name	: PropertyExists
	 * Description		: This method is to check the existence of properties.
	 * Input					: Name of the property (eg : Workflow)
	 * Output					: true if exists false if not exists
	 -----------------------------------------------------------------------*/
	
		//Variable Declaration
		Boolean isExists = false; //Stores the last item in the bread crumb 
		
		try {
			
			//Variable Declaration
			List<WebElement> propertyDiv;
			int itemIdx; //Stores the index of the items used in for loop
			int propertyCt; //Stores the number of property exists
			
			propertyDiv = driver.findElements(By.cssSelector("div[id='propertiesPaneMargin']>div>div>table>tbody>tr>td[class='dataname']"));
			propertyCt = propertyDiv.size(); //Number of rows with specified attribute
			
			for (itemIdx=0; itemIdx<propertyCt; itemIdx++) { //Loops through all the items and checks the existance of the item
				if (propertyDiv.get(itemIdx).getText().toUpperCase().trim().equalsIgnoreCase(propertyName.toUpperCase() + ":")) {
					isExists = true;
					break;
				}
				if (itemIdx >= propertyCt) {
					throw new Exception ("Property (" + propertyName + ") does not exists in the list.");
				}
			} //End for	
		} //End try
		
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				isExists = false;
			else				
				throw e;
		} //End catch
	
		return isExists;
		
	} //End function PropertyExists
	
	public String getPropertyValue(String propertyName)throws Exception {
		
	/* --------------------------------------------------------------------
	 * Function Name	: GetPropertyValue
	 * Description		: This method is to check the existence of properties.
	 * Input					: Name of the property (eg : Workflow)
	 * Output					: Value stored to the specified property
	 -----------------------------------------------------------------------*/
	
		//Variable Declaration
		String value = ""; //Stores the last item in the bread crumb 
		
		try {
			
			//Variable Declaration
			List<WebElement> propertyDiv;
			WebElement propertyValueRow;
			int itemIdx;
			int propertyCt;
		//	WebElement propPanel=driver.findElement(By.cssSelector("div[id='browseBottom']>div[id='propertiesPane']:not([style*='display : none'])"));
			propertyDiv = driver.findElements(By.cssSelector("div[id='propertiesPaneMargin']>div>div>table>tbody>tr>td[class='dataname']"));
			
			propertyCt = propertyDiv.size(); //Number of rows with specified attribute
			
			for (itemIdx=0; itemIdx<propertyCt; itemIdx++) {//Loops through all the items and checks the existance of the item
				if (propertyDiv.get(itemIdx).getText().toUpperCase().trim().equalsIgnoreCase(propertyName.toUpperCase() + ":"))
					break;
							
			if (itemIdx >= propertyCt) {//Checks if property exists in the list
				throw new Exception ("Property (" + propertyName + ") does not exists in the list.");
			}
			
			
			propertyValueRow =  propertyDiv.get(itemIdx).findElement(By.xpath("..")).findElement(By.cssSelector("td[class='dataname']+td"));
			value = propertyValueRow.getText();
			}
		
		} //End try
		
		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException"))
				return "";
			else
				throw e;
		} //End catch
	
		return value;
		
	} //End function GetPropertyValue
	
	public Boolean PropertyValueIsLink(String propertyName)throws Exception {
	
		//Variable Declaration
		Boolean isLink = false;
		
		try {
			
			//Variable Declaration
			List<WebElement> propertyDiv;
			WebElement propertyValueRow;
			int itemIdx;
			int propertyCt;
			
			//WebElement propPanel=driver.findElement(By.cssSelector("div[id='propertiesPane']"));
			propertyDiv = driver.findElements(By.cssSelector("div[id='propertiesPaneMargin']>div>div>table>tbody>tr>td[class='dataname']"));
			
			propertyCt = propertyDiv.size(); //Number of rows with specified attribute
			
			for (itemIdx=0; itemIdx<propertyCt; itemIdx++) {
				//Loops through all the items and checks the existance of the item
				System.out.println(propertyDiv.get(itemIdx).getText());
				if (propertyDiv.get(itemIdx).getText().toUpperCase().trim().equalsIgnoreCase(propertyName.toUpperCase() + ":")) {
					break;
				}
			
			if (itemIdx >= propertyCt) //Checks if property exists in the list
				throw new Exception ("Property (" + propertyName + ") does not exists in the list.");
			}		
			propertyValueRow =  propertyDiv.get(itemIdx).findElement(By.xpath("..")).findElement(By.cssSelector("td[class='dataname']+td"));
			
			if (propertyValueRow.getAttribute("class").toString().equalsIgnoreCase("PROPERTIESPANEBUTTON")) {
				isLink = true;
		//	}
		}
					
		} //End try
		
		catch (Exception e) {
			throw e;
		} //End catch
	
		return isLink;
		
	} //End function PropertyValueIsLink


	
	
} //End class PropertiesPane
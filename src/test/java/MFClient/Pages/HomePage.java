package MFClient.Pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import genericLibrary.*;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;

import MFClient.Wrappers.Caption;
import MFClient.Wrappers.ListView;
import MFClient.Wrappers.MFilesDialog;
import MFClient.Wrappers.MFilesObjectList;
import MFClient.Wrappers.MenuBar;
import MFClient.Wrappers.MetadataCard;
import MFClient.Wrappers.PreviewPane;
import MFClient.Wrappers.SearchPanel;
import MFClient.Wrappers.TaskPanel;
import MFClient.Wrappers.TreeView;

public final class HomePage extends LoadableComponent <HomePage> {

	private boolean pageLoaded = false;
	private final WebDriver driver;
	public ListView listView;
	public SearchPanel searchPanel;
	public TaskPanel taskPanel;
	public MenuBar menuBar;
	public PreviewPane previewPane;
	public TreeView treeView;


	/****************************************************************
	 * CSS Selectors - HomePage
	 ****************************************************************/

	final String CSS_GOTOHOLDER_ITEMS="div[id='taskpaneGoTo']>div[class='childItems']";
	final String CSS_LOGOUT="div#taskpaneLogOut>div[class='taskpaneItemText'], div[onclick*='LogOut']";
	final String CSS_NEWLINK="div[id='taskpaneNew']>div[class='childItems']:not([style*='display: none;'])";
	final String CSS_MENUNEWLINK="div[id='menu-div outerbox']:not([style*='display: none;'])";
	final String CSS_VIEWANDMODIFY="div[class='taskpaneViewAndModify']:not([style*='display: none;'])";

	@FindBy (how = How.CSS, using = CSS_NEWLINK+ " " + "div[class*='params:[0]']>div[class='taskpaneItemText']") 					//New Document link page factory web element					
	private WebElement lnkNewDocument;

	@FindBy (how = How.CSS, using = CSS_NEWLINK+ " " + "div[class*='params:[136]']>div[class='taskpaneItemText']") 					//New Customer link page factory web element					
	private WebElement lnkNewCustomer;	

	@FindBy (how = How.CSS, using = CSS_NEWLINK+ " " + "div[class*='params:[10]']>div[class='taskpaneItemText']") 					//New Assignment link page factory web element					
	private WebElement lnkNewAssignment;	

	@FindBy (how = How.CSS, using = CSS_NEWLINK+ " " + "div[class*='params:[101]']>div[class='taskpaneItemText']")  					//New Project link page factory web element					
	private WebElement lnkNewProject;	

	@FindBy(how=How.CSS,using="div[id='refreshListing']")
	private WebElement refreshBtn;

	@FindBy(how=How.CSS, using="li[id='menuNew']")
	private WebElement newMenuIcon;

	@FindBy(how=How.CSS,using="div[id='menubar']>div[id='menubarbg']")
	private WebElement topMenuBar;

	@FindBy(how=How.CSS,using="div[id='showhideTreeViewIcon'][style*='hide_metadata']")
	private WebElement showTreeView;

	@FindBy(how=How.CSS,using="div[id='showhideTreeViewIcon'][style*='show_metadata']")
	private WebElement hideTreeView;

	@FindBy(how=How.CSS,using="div[id^='taskpaneGoTo'][onclick*='/']>div[class='taskpaneItemText']")
	private WebElement taskPaneHome;

	@FindBy(how=How.CSS,using="div[id^='taskpaneGoTo'][onclick*='/V5']>div[class='taskpaneItemText']")
	private WebElement taskPaneCheckedOutToMe;

	@FindBy(how=How.CSS,using="div[id^='taskpaneGoTo'][onclick*='/V9']>div[class='taskpaneItemText']")
	private WebElement taskPaneAssignedToMe;

	@FindBy(how=How.CSS,using="div[id^='taskpaneGoTo'][onclick*='/V14']>div[class='taskpaneItemText']")
	private WebElement taskPaneRecentlyAccessedByMe;

	@FindBy(how=How.CSS,using="div[id^='taskpaneGoTo'][onclick*='/V15']>div[class='taskpaneItemText']")
	private WebElement taskPaneFavorites;

	/*@FindBy(how=How.CSS,using="input[id='searchString'],input[id='searchIn_searchString']")
	private WebElement searchString;*/

	@FindBy(how=How.CSS,using="input[id='quickSearch_Top_input'],input[id='searchString'],input[id='searchIn_searchString']")
	private WebElement searchString;

	@FindBy(how=How.CSS,using="ul[id='breadCrumb_ul']")
	private WebElement breadCrumbViewLabel;

	@FindBy(how=How.CSS,using="li[class$='first'][onClick*='/']")
	private WebElement breadCrumbHome;

	@FindBy(how=How.CSS, using="div[id='viewCaption'][style*='display: block;']")
	private WebElement viewTabHeader;

	@FindBy(how=How.CSS, using="div[class='errorDialog']>div[class='shortErrorArea'],div[class='message']")
	private WebElement popUpDialogMsg;

	@FindBy(how=How.CSS,using="button[class='window_ok']")
	private WebElement okBtn;

	@FindBy(how=How.CSS,using="button[class='cancel']")
	private WebElement cancelBtn;

	@FindBy(how=How.CSS,using="button[class='ok']")
	private WebElement yesBtn;

	@FindBy(how=How.CSS, using="ul[id='user']")
	private WebElement loggedInUser;

	@FindBy(how=How.CSS,using=CSS_LOGOUT)
	private WebElement taskPaneLogOut;

	public HomePage(final WebDriver driver) throws Exception {

		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 2);
		PageFactory.initElements(finder, this);
		listView = new ListView(this.driver);
		searchPanel = new SearchPanel(this.driver);
		taskPanel = new TaskPanel(this.driver);
		menuBar = new MenuBar(this.driver);
		previewPane = new PreviewPane(this.driver);
		treeView = new TreeView(this.driver);

	}

	final protected void isLoaded(){
		if (!(driver.getCurrentUrl().toLowerCase().contains("/Default.aspx") && driver.getCurrentUrl().toLowerCase().contains("/views/"))) {

			if (!pageLoaded)
				Assert.fail();

			try {
				Log.fail("Expected page was a WebAccess Home page, but current page is not a Home page." + "Current Page is: " + driver.getCurrentUrl(), driver);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Verify whether is Home page
		}
	}

	final protected void load(){

		try {
			Utils.waitForPageLoad(driver);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		int maxWait = 30;

		while (maxWait > 0) {

			String URL = driver.getCurrentUrl();
			maxWait--;

			if ((URL.contains("/Default.aspx") || URL.contains("/views/")))
				break;
		}

		pageLoaded = true;
	}

	/**
	 * isSearchbarPresent: Verify if 'SearchBar' exists
	 * @param none
	 * @return true if search bar present; if not false
	 * @throws Exception 
	 */
	public boolean isSearchbarPresent() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(this.driver);

			if (this.driver.findElement(By.cssSelector("div[id='searchBasic']")).isDisplayed()) {
				Log.event("Search bar displayed.", StopWatch.elapsedTime(startTime));
				return true;
			}
		}

		catch(Exception e) 	{
			if (e.getClass().toString().contains("NoSuchElementException")) { 
				Log.event("Search Bar is not Displayed in HomePage.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else
				throw e;
		}

		Log.event("Search bar is not displayed.", StopWatch.elapsedTime(startTime));

		return false;

	} //End try

	/**
	 * isTaskPaneDisplayed : This method is to if taskpanel is displayed
	 * @param none
	 * @return true if task pane displayed; false if task pane not displayed
	 * @throws Exception
	 */
	public Boolean isTaskPaneDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement taskPanel= this.driver.findElement(By.cssSelector("div[id='taskpane']"));

			if (taskPanel.isEnabled() && taskPanel.isDisplayed()) {
				Log.event("Task Pane Displayed in HomePage.", StopWatch.elapsedTime(startTime));
				return true;			
			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) { 
				Log.event("Task Pane is not Displayed in HomePage.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else				
				throw e;
		} //End catch

		Log.event("Task Pane is not Displayed in HomePage.", StopWatch.elapsedTime(startTime));
		return false;

	} //End function isTaskPaneDisplayed

	/**
	 * isMenubarDisplayed : To Verify if menubar is displayed
	 * @param none
	 * @return true if menubar is displayed; if not false
	 * @throws Exception 
	 */
	public boolean isMenubarDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (this.driver.findElement(By.id("menubar")).isDisplayed()) { //Checks if menubar is displayed
				Log.event("MenuBar is Displayed.",StopWatch.elapsedTime(startTime));
				return true;
			}

		} //End try
		catch(Exception e) {
			throw e;
		} //End catch

		Log.event("MenuBar is not Displayed.",StopWatch.elapsedTime(startTime));
		return false;

	} //End isMenubarDisplayed

	/**
	 * isListViewDisplayed : To Verify the existence of list view.
	 * @param none
	 * @return true if list view displayed; false if list view is not displayed
	 * @throws Exception
	 */
	public Boolean isListViewDisplayed()throws Exception {

		try {

			if (this.driver.findElement(By.cssSelector("div[id='listingTable']>table[id='mainTable']")).isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw e;
		} //End catch

		return false;

	} //End isListViewDisplayed

	/**
	 * isTreeViewDisplayed : To Verify the existence of tree view.
	 * @param none
	 * @return true if tree view displayed; false if not
	 * @throws Exception
	 */
	public Boolean isTreeViewDisplayed()throws Exception {

		try {

			if (this.driver.findElement(By.id("tree")).isDisplayed())
				return true;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw e;
		} //End catch

		return false;

	} //End isTreeViewDisplayed

	/**
	 * isPreviewPaneDisplayed : To Verify the existence of preview pane.
	 * @param none
	 * @return true if list view displayed; false if list view is not displayed
	 * @throws Exception
	 */
	public Boolean isPreviewPaneDisplayed()throws Exception {

		try {

			if (this.driver.findElement(By.cssSelector("div[id='rightPaneTabs']")).isDisplayed())
				return true;
			else
				return false;

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else				
				throw e;
		} //End catch

	} //End isPreviewPaneDisplayed

	/**
	 * isRightPaneDisplayed :  To Verify if Rightpane is displayed
	 * @return true if rightpane displayed; if not false
	 * @throws Exception
	 */
	public Boolean isRightPaneDisplayed() throws Exception {

		try {

			Utils.fluentWait(driver);

			WebElement rightPaneMetaCard=driver.findElement(By.cssSelector("div[id*='metaCard'][style*='display: block;'],span[id='hideshowRightPaneIcon']"));

			if(rightPaneMetaCard.isDisplayed()) {
				return true;
			}
		}
		catch(Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException")) 
				return false;
			else
				Log.exception(new Exception("Cound not found 'Rightpane Metacard' element."),driver);
		}
		return false;
	}

	/**
	 * isNavigationPaneDisplayed :  To verify if navigation pane is displayed
	 * @return true if navigation pane is displayed;  false if not
	 * @throws Exception
	 */
	public Boolean isNavigationPaneDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			WebElement navigationPane=driver.findElement(By.cssSelector("div[id='panel']:not([style*='display: none;'])>div[id='tree']"));

			if(navigationPane.isDisplayed()) {
				Log.event("Navigation Pane displayed.", StopWatch.elapsedTime(startTime));
				return true;
			}
		}
		catch(Exception e){
			if(e.getClass().toString().contains("NoSuchElementException")) {
				return false;
			}
			else
				Log.exception(new Exception("Error while in displaying Navigation pane."),driver);
		}
		return false;
	}

	/**
	 * isBreadcrumbDisplayed: verify if Breadcrumb displayed
	 * @return true or false
	 * @throws Exception
	 */
	public Boolean isBreadcrumbDisplayed() throws Exception{
		try{

			if(breadCrumbHome.isDisplayed())
				return true;
		}
		catch(Exception e)	{

			if(e.getClass().toString().contains("NoSuchElementException")) {
				return false;
			}
			else
				Log.exception(new Exception("Some error while finding 'Breadcrumb' on HomePage."), driver);

		}
		return false;

	}

	/**
	 * Verify the Preview tab is selected
	 * @return true if Preview tab is selected; false if not selected
	 */
	public boolean isPreviewTabSelected(){

		try{
			WebElement previewPane = driver.findElement(By.cssSelector("div[id='preview'][aria-hidden='false']"));
			if(previewPane.isDisplayed()){
				return true;
			}
			else
				return false;
		}//End try
		catch(Exception e){
			throw e;
		}//End catch
	}

	/**
	 * getLoggedinUserName: To Verify the existence of preview pane.
	 * @param none
	 * @return true if list view displayed; false if list view is not displayed
	 * @throws Exception
	 * */
	public String getLoggedinUserName()throws Exception {
		try {
			return (this.driver.findElement(By.cssSelector("ul[id='user']")).getText());
			//>li>a>span[id='userNameForDisplay']
		} //End try
		catch (Exception e) {
			throw e;
		} //End catch
	} //End getLoggedinUserName

	/**
	 * GroupObjectsByObjectType: To Verify if Display mode is Group objects by Object type
	 * @param none
	 * @return true if list view displayed; false if list view is not displayed
	 * @throws Exception
	 * */
	public static boolean GroupObjectsByObjectType(WebDriver driver)throws Exception {
		try {
			MenuBar menuBar = new MenuBar(driver);
			menuBar.ClickOperationsMenu("Display Mode>>Group Objects by Object Type");
			return true;
		} //End try
		catch (Exception e) {
			throw e;
		} //End catch
	} //End getLoggedinUserName

	/**
	 * downloadObjectFromTaskPane: 'Download Object' from taskpane link
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public boolean downloadObjectFromTaskPane(String file) throws Exception {

		TaskPanel taskPane=new TaskPanel(driver);

		try {

			WebElement downloadLink=driver.findElement(By.cssSelector("a[id='taskpaneDownloadFilesafari']"));
			ActionEventUtils.click(driver, downloadLink);
			//	downloadLink.click();//click 'Download link


			if (taskPane.downloadSingleFile(downloadLink,file))//if file downloads successfully
				return true;

		}

		catch(Exception e) {
			throw new Exception("Exception at HomePage.downloadObjectFromTaskPane :  " + e);
		}
		return false;
	}

	/**
	 * deleteObjectInView: Selects and deletes specified object from list
	 * @param objectName  Name of the object to be deleted
	 * @return true if object deletion is successful
	 * @throws Exception
	 */
	public boolean deleteObjectInView(String objectName) throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			if (!this.listView.clickItem(objectName)) //Selects the object
				throw new Exception("Document (" + objectName + ") is not selected.");

			this.menuBar.ClickOperationsMenu(Caption.MenuItems.Delete.Value); //Selects Delete from operations menu

			Utils.fluentWait(driver);

			MFilesDialog mfilesDialog = new MFilesDialog(this.driver);
			mfilesDialog.confirmDelete();

			if (!this.listView.isItemExists(objectName))
				return true;
			else
				return false;

		} //End try

		catch(Exception e) {
			throw new Exception("Exception at HomePage.deleteObjectInView :  " + e);
		} //End catch

		finally {
			Log.event("Hompage.deleteObjectInView.", StopWatch.elapsedTime(startTime));
		} //End finally

	} //End deleteObjectInView

	/*--------------------------------Functions required for smoke test cases--------------------------------------*/


	/**
	 * Description: Verify if User loggedin to application
	 * @param userName
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isLoggedIn(String userName) throws Exception
	{
		String userInfo=null;
		try{

			loggedInUser.isDisplayed();//verify if LoggedIn user name displayed
			if(loggedInUser.getText().contains("\\")) {
				String[] user=loggedInUser.getText().split("\\\\");
				userInfo=user[1];

			}
			else {
				userInfo=loggedInUser.getText();
			}
			if (userInfo.trim().equalsIgnoreCase(userName.trim()))
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//Log.exception(new Exception("Error while Login to MFiles-Web Access."), driver);
		}
		return false;
	}

	/**
	 * Clicking Document link from 'New' Task Pane
	 * @throws Exception 
	 */
	public void clickTaskPaneNewLink(String newObjectLink) throws Exception
	{
		try {
			Utils.fluentWait(driver);
			new WebDriverWait(driver, 120).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='taskpaneNew']>div[class='childItems']"))));

			switch(newObjectLink)
			{
			case "Document":{
				lnkNewDocument.click();//clicks 'document' link
				break;
			}
			case "Customer":
				lnkNewCustomer.click();
				break;
			case "Assignment":
				lnkNewAssignment.click();
				break;
			case "Project":
				lnkNewProject.click();
				break;
			}

		} 
		catch (StaleElementReferenceException e) {
			Log.exception(new Exception("Unable to click 'New' object link from taskpane."),driver);
		}
	}

	/**
	 * Description: Verify if correct title displayed on the Metadatacard
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean verifyMetadataCardDisplay(String objName) throws Exception 
	{
		try
		{
			Thread.sleep(2500);
			//			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class)
			//											.pollingEvery(250,TimeUnit.MILLISECONDS)
			//											.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[class*='ui-dialog-content ui-widget-content'][style*='display: block'],div[class*='ui-draggable'][style*='display: block']"))));

			String dialogTitle=driver.findElement(By.cssSelector("span[class='ui-dialog-title']")).getText();//reads metadatacard dialog title
			if (dialogTitle.contains(objName))
				return true;
		}
		catch(NoSuchElementException e)
		{
			Log.exception(new Exception("Unable to launch Metadatacard dialog."),driver);
		}
		return false;	
	}

	/**
	 * Description: Search a Term/File 
	 * @param driver
	 * @param fileName
	 * @return true or false
	 * @throws Exception 
	 */
	public String searchAFileName(String fileName) throws Exception
	{
		String enteredText;
		try{

			Utils.fluentWait(driver);

			try {
				searchString.isDisplayed();
				searchString.clear();
				//Enter search string
				searchString.sendKeys(fileName);
			}
			catch (NoSuchElementException | StaleElementReferenceException e ){
				Log.exception(new Exception("Search Input Box is not enabled to enter Text"));
			}

			enteredText=searchString.getAttribute("value");
			//check if search string is empty
			if (enteredText.isEmpty())
				throw new Exception("Could not enter Search Key as "+fileName);

			//Click Search Button
			clickSearchBtn(driver);

		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new Exception("Could not find '"+fileName+"' object in Search Results list view.");
		}
		return enteredText;
	}

	/**
	 * Description : Get the ObjectCount in the search results
	 * @return Object Count in Search Results of @Type 'Integer'
	 */	
	public int objectCountInSearchResults()
	{
		int docCount=0;
		String doumentHeader=driver.findElement(By.cssSelector("div[class='group_header']")).getText();

		String[] header=doumentHeader.split("\\(");
		//			String objectName=header[0];
		String[] searchCount=header[1].split("\\)");

		docCount=Integer.parseInt(searchCount[0]);

		return docCount;
	}

	/**
	 * Description : Verify Search successful
	 * @param driver
	 * @param tableHeader
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isSearchResults(WebDriver driver,String tableHeader) throws Exception
	{
		int objCount=0;
		try{

			Utils.fluentWait(driver);
			//Hide the Right pane tabs
			if(!isRightPaneHidden())
				throw new Exception("Could not hide the right pane tabs");

			WebElement listViewHeader=driver.findElement(By.cssSelector("div[class='group_header']"));
			if (listViewHeader.isDisplayed())
			{
				String doumentHeader=listViewHeader.getText();

				String[] header=doumentHeader.split("\\(");
				String objectName=header[0];
				String[] searchCount=header[1].split("\\)");

				if(searchCount[0].contains("+")) {
					int i=searchCount[0].length();
					i=searchCount[0].length()-1;
					objCount=Integer.parseInt(searchCount[0].substring(0,i));
				}
				else
					objCount=Integer.parseInt(searchCount[0]);

				if ((objectName.contains("Documents") || objectName.contains("Objects")) && objCount>0) {
					return true;
				}
			}
		}

		catch(NoSuchElementException | ArrayIndexOutOfBoundsException e)
		{
			Log.exception(new Exception("No Search Results Found with this Search term."),driver);
		}
		return false;
	}

	/**
	 * Description: Verify if 'SearchBar' exists
	 * @param driver
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isSearchbarPresent(WebDriver driver) throws Exception
	{
		try
		{
			driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
			Utils.fluentWait(driver);
			if(driver.findElement(By.cssSelector("div[id='searchBasic']")).isDisplayed()) 
				return true;
		}
		catch(NoSuchElementException e)
		{
			Log.exception(new Exception("Search bar not displayed"),driver);
		}
		return false;
	}

	/**
	 * Description: click 'Search' button 
	 * @param driver
	 * @throws Exception 
	 */
	public void clickSearchBtn(WebDriver driver) throws Exception
	{

		try{
			WebElement searchBtn=driver.findElement(By.cssSelector("input[id='searchButton']"));
			//		WebElement searchBtn=driver.findElement(By.cssSelector("input[id='btnSearch']"));
			searchBtn.click();
			Utils.fluentWait(driver);
		}
		catch(NoSuchElementException e){
			throw new Exception("Could not click 'Search' button.");
		}
	}

	/**
	 * Description: Verify object count from search 'column header' 
	 * @param searchText
	 * @throws Exception 
	 */
	public void enterSearchText(String searchText) throws Exception
	{
		try{
			Utils.fluentWait(driver);
			searchString.isDisplayed();
			searchString.sendKeys(searchText);
			Thread.sleep(200);
		}
		catch(NoSuchElementException | StaleElementReferenceException e)
		{
			throw new Exception("Search Input Box is not enabled to enter Text.");
		}


	}

	/**
	 * Description: Verify Search Term/File exists in Search Results view 
	 * @param driver
	 * @param fileName
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isDataInListView(WebDriver driver,String fileName,String columnName) throws Exception
	{
		boolean isAvailable=false;
		try{

			//hide the right panel tabs
			if(!isRightPaneHidden())
				throw new Exception("Unable to Hide right tab panes");

			WebElement tableElement=driver.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> rows= tableElement.findElements(By.cssSelector("tr[id*='listingTable_row_listing']"));

			int colValue=getListViewColumnIndex(columnName);

			for(int i=0;i<rows.size();)
			{
				List<WebElement> cells=rows.get(i).findElements(By.cssSelector("td[class*='listing-column column-"+colValue+"']"));
				for(int j=0;j<cells.size();)
				{
					if (cells.get(j).getText().contains(fileName) || cells.get(j).getText().contains("Objects")) {
						isAvailable=true;
						break;
					}
					j++;
				}

				if (isAvailable)
					break;
				i++;

			}

		}
		catch(Exception e)
		{
			throw new Exception("Could not find Search Term in ListView.");
		}
		return isAvailable;

	}

	public String isDataInListView(String fileName,String columnName) throws Exception
	{
		String objValue=null;
		try{

			//hide the right panel tabs
			if(!isRightPaneHidden())
				throw new Exception("Unable to Hide right tab panes");

			WebElement tableElement=driver.findElement(By.cssSelector("table[id='mainTable']"));
			List<WebElement> rows= tableElement.findElements(By.cssSelector("tr[id*='listingTable_row_listing']"));
			int colValue=getListViewColumnIndex(columnName);

			for(int i=0;i<rows.size();) {
				List<WebElement> cells=rows.get(i).findElements(By.cssSelector("td[class*='listing-column column-"+colValue+"']"));
				for(int j=0;j<cells.size();) {
					if (cells.get(j).getText().contains(fileName) || cells.get(j).getText().contains("Objects")) {
						objValue=cells.get(j).getText();
						break;
					}
					j++;
				}
				if (objValue.trim().contains(fileName))
					break;
				i++;

			}

		}

		catch(Exception e)
		{
			throw new Exception("Could not find Search Term in ListView.");
		}
		return objValue;

	}

	/**
	 * Description : Click New Menu option from HomePage
	 * @param driver
	 * @throws Exception 
	 */
	public void newMenuOption(WebDriver driver) throws Exception
	{

		try{
			Utils.fluentWait(driver);
			newMenuIcon.click();
			new WebDriverWait(driver, 60).ignoring(NoSuchElementException.class).pollingEvery(250,TimeUnit.MILLISECONDS).until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='menubarbg']"))));
		}
		catch(InvalidElementStateException e)
		{ 
			throw new Exception("Unable to click 'New' option from Menubar.");

		}


	}

	/**
	 * Description : Select new 'Object' from Menubar from HomePage
	 * @param driver
	 * @param object
	 * @throws Exception 
	 */
	public void clickNewMenuItem(WebDriver driver, MFilesObjectList object) throws Exception
	{
		//		driver.manage().timeouts().implicitlyWait(250, TimeUnit.MILLISECONDS);
		try{

			WebElement newMenuItem= driver.findElement(By.cssSelector("div.menu-item > #menu_new_"+object.getValue()+"> div.menuItemText,li[class*='"+object.getValue()+"']>div[class='menuItemText']"));
			Log.message(newMenuItem.getText());
			newMenuItem.click();
		}
		catch(NoSuchElementException | InvalidElementStateException e)
		{
			throw new Exception("Could not click 'New' Menu options");

		}

	}

	/**
	 * Description : Open context menu from Listview
	 * @param driver
	 * @param actionToPerform
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean openContextMenuDialog(WebDriver driver) throws Exception{
		try{

			WebElement actionItem=driver.findElement(By.cssSelector("td[class^='listing-column column-0']"));
			//Open the context menu of the list view item		
			ActionEventUtils.rightClick(driver, actionItem);
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("ul[id='menuSubOperations'][style*='display: block;']"))));
			if (driver.findElement(By.cssSelector("ul[id='menuSubOperations'][style*='display: block;']")).isDisplayed()){
				return true;
			}
		}
		catch(Exception e){
			//e.printStackTrace();
			if(e.getClass().toString().contains("NoSuchElementException")) {
				return false;
			}
			else {
				throw new Exception("Unable to display ContextMenu Options.");
			}
		}
		return false;
	}

	/**
	 * 
	 * @param fileName
	 * @return true or false
	 * @throws Exception
	 */
	public boolean selectObjectFromListView(String fileName,int colValue) throws Exception
	{
		try{

			List<WebElement> listViewItems= driver.findElements(By.cssSelector("table[id='mainTable']>tbody>tr[class='listing-item tap']>td"));

			for(int i=0;i<listViewItems.size();i++)
			{
				if(listViewItems.get(i).getText().trim().contains(fileName.trim())) {
					listViewItems.get(i);
					return true;
				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Unable to select the "+fileName+" Object from List view.");
			//	e.printStackTrace();

		}
		return false;
	}

	/**
	 * 
	 * @param fileName
	 * @return true or false
	 * @throws Exception
	 */
	public boolean doubleClickObjectFromListView(String fileName) throws Exception
	{
		try{

			List<WebElement> listViewItems= driver.findElements(By.cssSelector("table[id='mainTable']>tbody>tr[class='listing-item tap']>td"));

			for(int i=0;i<listViewItems.size();i++)
			{
				if(listViewItems.get(i).getText().trim().contains(fileName.trim())) {

					((JavascriptExecutor) driver).executeScript("var evt = document.createEvent('MouseEvents'); "
							+ "evt.initMouseEvent('dblclick', true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null); "
							+ "arguments[0].dispatchEvent(evt);", listViewItems.get(i));
					/*Actions action=new Actions(driver);
					action.doubleClick(listViewItems.get(i)).build().perform();*/

					return true;
				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Unable to select the "+fileName+" Object from List view.");

		}
		return false;
	}

	/**
	 * 
	 * @param newValue
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String updateObjectName(String newValue)
	{
		Date date=new Date();
		newValue=newValue.concat(String.valueOf(date.getDate()).concat(String.valueOf(date.getTime())));
		return newValue;
	}
	/**
	 * Description : 'Rename' Object using context menu options
	 * @param driver
	 * @param newValue
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean renameObjectsFromListView(String newValue,String oldValue,String menuOption) throws Exception {
		MenuBar menuBarItems;
		try{

			newValue=updateObjectName(newValue);

			//Select 'Rename' Option from context menu
			if (menuOption.trim().equalsIgnoreCase("contextMenu"))	{
				selectContextMenuItemFromListView("Rename");
			}
			else if (menuOption.trim().equalsIgnoreCase("operationsMenu"))	{
				menuBarItems=new MenuBar(driver);
				//Select the Operations menu options
				menuBarItems.selectFromOperationsMenu("Rename");
			}

			//Rename the object
			driver.findElement(By.cssSelector("input[class='prompt']")).sendKeys(newValue.substring(0,11));
			//Click 'Yes' Button
			clickYesButton();

			Thread.sleep(150);
			//Wait until waitOverlay disappears
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));

			//CHeck if object is Renamed
			String newName=isDataInListView(newValue.substring(0,11),"Name");

			if (!oldValue.equalsIgnoreCase(newName)) {
				return true;
			}
		}
		catch(Exception e){
			throw new Exception("Unable to perform 'Rename' Operation.");
		}
		return false;
	}

	/**
	 * Description : 'Delete' Object using context menu options
	 * @param driver
	 * @return true or false
	 * @throws Exception 
	 */

	public boolean deleteObjectFromListView(String menuOption) throws Exception
	{
		MenuBar menuBarItems;
		int beforeDelete,afterDelete;
		try{

			beforeDelete=objectCountInSearchResults();

			if (menuOption.trim().equalsIgnoreCase("contextMenu")) {
				//Select 'Delete' Option from context menu
				selectContextMenuItemFromListView("Delete");
			}
			else if (menuOption.trim().equalsIgnoreCase("operationsMenu"))	{
				menuBarItems=new MenuBar(driver);
				//Select the Operations menu options
				menuBarItems.selectFromOperationsMenu("Delete");
			}

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[class*='Confirm']"))));

			if (!driver.findElement(By.cssSelector("div[class='message']")).getText().contains("delete the selected object"))
				throw new NoSuchElementException("Delete popUp dialog is not displayed");

			//Click 'Yes' button
			clickYesButton();

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class*='Confirm']")));

			Utils.fluentWait(driver);
			if (beforeDelete!=1 || beforeDelete>1) {
				afterDelete=objectCountInSearchResults();

				if (afterDelete<beforeDelete){
					return true;
				}
			}
			else {
				return true;
			}

		}
		catch(Exception e) {
			throw new Exception("Unable to perform 'Delete' Operation.");
		}
		return false;
	}

	/**
	 * 
	 * @param status
	 * @param colIndex
	 * @return
	 * @throws Exception
	 */
	public String getListViewColumnValue(String status,int colIndex) throws Exception
	{
		String columnValue=null;
		try{

			//			beforeCheckOutTo=driver.findElement(By.cssSelector("td[class^='listing-column column-"+colIndex+"']")).getText();
			columnValue=driver.findElement(By.cssSelector("table[id='mainTable']>tr[id^='listingTable_row_listing']>td:nth-child("+(colIndex+1)+")")).getText();
		}
		catch(Exception e) {
			throw new Exception("Error reading the "+status+" Column Values");
		}
		return columnValue;
	}

	/**
	 * 
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public String getListViewColumnValue(String status) throws Exception
	{
		String beforeCheckOutTo=null;
		try {


			int columnIndex=getListViewColumnIndex(status);
			beforeCheckOutTo=driver.findElement(By.cssSelector("td[class='listing-column column-"+columnIndex+"']")).getText();

		}
		catch(Exception e)
		{
			throw new Exception("Could not read the "+status+" Column Values");
		}
		return beforeCheckOutTo;
	}

	/**
	 * Description : 'CheckOut' Object using context menu options
	 * @param driver
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean checkOutObjectFromContextMenu() throws Exception
	{
		try{
			//Select 'CheckOut' Option from context menu
			selectContextMenuItemFromListView("CheckOut");

			//Verify the CheckOut column
			if (!getListViewColumnValue("Checked Out To").isEmpty())
				return true;
		}
		catch(NoSuchElementException e){
			Log.exception(new Exception("Error while 'CheckingOut'"),driver);
		}
		return false;
	}

	/**
	 * Description : Hide the 'Right Pane' to read the List view columns
	 * @return
	 * @throws Exception
	 */
	public Boolean isRightPaneHidden() throws Exception 
	{
		Boolean isHidden=false;
		try {
			driver.findElement(By.cssSelector("li[id='hideshowRightPane']>span[style*='hide_metadata.png']")).click();
			if(driver.findElement(By.cssSelector("li[id='hideshowRightPane']>span[style*='show_metadata.png']")).isDisplayed())
				isHidden=true;
		}
		catch (Exception e) {
			if(e.getClass().toString().contains("NoSuchElementException"))
				isHidden=true;
			else
				Log.exception(new Exception("Unable to hide the right pane."),driver);
		}
		return isHidden;
	}


	/*	*//**
	 * Description : Check if object is 'Checkedout'
	 * @return true or false
	 * @throws Exception
	 *//*
	public boolean isObjectCheckedOut(String colName,String userName) throws Exception
	{
		try{	

			//hide the right pane to fetch the list view columns
			if(!isRightPaneHidden())
				throw new Exception("Unable to Hide right tab panes");
			//get the columnIndex
			int colIndex=getListViewColumnIndex(colName);

			//verify CheckOut column value and see if it is checkedOut 
			String colValue=getListViewColumnValue(colName,colIndex);

			if (!colValue.isEmpty() && colValue.trim().equalsIgnoreCase(userName.trim())){
				try {
						WebElement objectIcon=driver.findElement(By.cssSelector("div[class='list_overlay'][style*='checkedoutotheroverlay.png']"));
						if(objectIcon.isDisplayed())
						{
							  //Read List view column Headers
					        if (!readListViewHeaderNames(driver,"Version"))
					        {	        
					        	//Insert New Column to List view
					        	clickAndInsertListViewColumns(driver, "Insert Column->Standard Columns->Version");
					        	//Verify if newly inserted column exists
					        	if (!readListViewHeaderNames(driver,"Version"))
					        		throw new Exception("Column 'Version' not added.");
					        }
							openContextMenuDialog(driver);
							if(!undoCheckOutObject("contextMenu")){
								Log.exception(new Exception("Unable to cancel CheckOut done in another session."),driver);
							}
						}

						return true;


					}
					catch(Exception e)
					{
						if(e.getClass().toString().contains("NoSuchElementException"))
							return true;
						else
							throw e;
					}
			}
			else if (!colValue.isEmpty()&& !colValue.trim().equalsIgnoreCase(userName.trim())){
//				Log.exception(new SkipException("Object is checkedOut to Other User."),driver);
				return false;
			}
			else if (colValue.isEmpty())
				return false;
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Some error while verifying if object is CheckedOut"),driver);
		}

		return false;

	}*/

	/**
	 * Description : Check if object is 'Checkedout'
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isObjectCheckedOut(String colName,String userName) throws Exception
	{

		try{	
			Thread.sleep(1000);
			//hide the right pane to fetch the list view columns
			if(!isRightPaneHidden())
				throw new Exception("Unable to Hide right tab panes");
			//get the columnIndex
			int colIndex=getListViewColumnIndex(colName);

			//verify CheckOut column value and see if it is checkedOut 
			String colValue=getListViewColumnValue(colName,colIndex);
			//			String colValue=getListViewColumnValue(colName);

			if (!colValue.isEmpty() && colValue.trim().equalsIgnoreCase(userName.trim())){
				try {

					try {
						WebElement objectCheckedOutToCurrent=driver.findElement(By.cssSelector("div[class='list_overlay'][style*='CheckedOutCurrent.png']"));
						if(objectCheckedOutToCurrent.isDisplayed())
							return true;
					}
					catch(Exception e) {
						if(e.getClass().toString().contains("NoSuchElementException"))
							Log.message("not checkedOut to Current user");
						else
							throw e;
					}

					try {
						WebElement objectCheckedOutToOther=driver.findElement(By.cssSelector("div[class='list_overlay'][style*='checkedoutotheroverlay.png']"));
						if(objectCheckedOutToOther.isDisplayed()) {
							//Read List view column Headers
							if (!readListViewHeaderNames(driver,"Version"))
							{	        
								//Insert New Column to List view
								clickAndInsertListViewColumns(driver, "Insert Column->Standard Columns->Version");
								//Verify if newly inserted column exists
								if (!readListViewHeaderNames(driver,"Version"))
									throw new Exception("Column 'Version' not added.");
							}
							openContextMenuDialog(driver);
							if(!undoCheckOutObject("contextMenu")){
								Log.exception(new Exception("Unable to cancel CheckOut done in another session."),driver);
							}
						}
						return true;
					}
					catch(Exception e) {
						if(e.getClass().toString().contains("NoSuchElementException"))
							Log.message("not checkedOut to Other user");
						else
							throw e;
					}

				}
				catch(Exception e)
				{
					if(e.getClass().toString().contains("NoSuchElementException"))
						return true;
					else
						throw e;
				}
			}
			if (!colValue.isEmpty()&& !colValue.trim().equalsIgnoreCase(userName.trim())){
				//				Log.exception(new SkipException("Object is checkedOut to Other User."),driver);
				return false;
			}
			else if (colValue.isEmpty())
				return false;
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Some error while verifying if object is CheckedOut"),driver);
		}

		return false;

	}

	/**
	 * Verify if object checkedOut
	 * @param file
	 * @return
	 */
	public Boolean isCheckedOutOverLayDisplayed()
	{
		Boolean isCheckedOut=false;
		try
		{
			List<WebElement> tableRows=driver.findElements(By.cssSelector("table[id='mainTable']>tbody>tr[id*='listingTable_row_listing']"));
			for(WebElement row:tableRows)
			{
				WebElement item=row.findElement(By.cssSelector("td>div[class='list_overlay'][style*='CheckedOutCurrent.png']"));
				if (item.isDisplayed())
				{
					return isCheckedOut;
				}

			}
		}	
		catch(Exception e)
		{
			if(e.getClass().toString().contains("NoSuchElementException"))
				return isCheckedOut;
		}
		return isCheckedOut;
	}
	/**
	 * Description: Get the Row index of an object from list view
	 * @param file
	 * @return rowNo
	 */
	public int getTableRowOfObject(String file)
	{
		int rowNo=0;
		try
		{
			List<WebElement> tableRows=driver.findElements(By.cssSelector("table[id='mainTable']>tbody>tr[id*='listingTable_row_listing']"));
			for(WebElement row:tableRows)
			{
				if (row.getText().contains(file))
				{
					rowNo=tableRows.indexOf(row);
					break;
				}

			}
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return rowNo;
	}
	/**
	 * Description : 'CheckIn' Object using context menu options
	 * @param driver
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean checkInObjectFromContextMenu() throws Exception
	{
		try{
			//Select 'CheckIn' Option from context menu
			selectContextMenuItemFromListView("CheckIn");
			//Verify if Object CheckedOut
			if (getListViewColumnValue("Checked Out To").isEmpty())
				return true;
		} 
		catch(Exception e){
			throw new Exception("Error while 'CheckIn'");
		}
		return false;
	}
	/**
	 * Description : 'CheckIn with Comments' using context menu options
	 * @param driver
	 * @param comments
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean checkInObjectWithCommentsFromContextMenu(String comments) throws Exception
	{
		try{
			//Select 'CheckInWithComments' Option from context menu
			selectContextMenuItemFromListView("CheckInWithComments");
			//Enter Comments
			enterCommentsToObject(comments);

			if (!getListViewColumnValue("Checked Out To").isEmpty()&&getListViewColumnValue("Comments").isEmpty()){
				return false;
			}
		}
		catch(Exception e){
			Log.exception(new Exception("Error while 'CheckIn With Comments'"),driver);
		}
		return true;
	}
	/**
	 * Description : Enter Comments for selected Object
	 * @param comments
	 * @throws Exception
	 */
	public void enterCommentsToObject(String comments) throws Exception
	{
		try{
			Thread.sleep(500);
			WebElement commentField=driver.findElement(By.cssSelector("div[id='commentArea']>textarea"));
			commentField.clear();
			commentField.sendKeys(comments);
			clickOKButton();
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[id='commentArea']>textarea")));
		}
		catch(Exception e)
		{
			throw new Exception("Could not enter comments for the Object");
		}
	}

	/**
	 * Description : 'UndoCheckOut' Object using context menu options
	 * @param driver
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean undoCheckOutObject(String section) throws Exception
	{
		try{

			Thread.sleep(500);
			int colIndex=getListViewColumnIndex("Version");
			MenuBar menuBar=new MenuBar(driver);
			TaskPanel taskPane=new TaskPanel(driver);
			String beforeUndoCheckOut=getListViewColumnValue("Version",colIndex);
			int beforeUndoVersion,afterUndoVersion;
			if (!beforeUndoCheckOut.isEmpty())
			{
				beforeUndoVersion=Integer.parseInt(beforeUndoCheckOut.trim());

				//Select 'UndoCheckOut' Option from context menu
				if (section.trim().equalsIgnoreCase("contextMenu"))
					selectContextMenuItemFromListView("UndoCheckout");
				//Select 'UndoCheckOut' Option from operations menu
				else if (section.trim().equalsIgnoreCase("operationsMenu"))
					menuBar.selectFromOperationsMenu("UndoCheckout");
				//Select 'UndoCheckOut' Option from 'taskPane menu
				else if (section.trim().equalsIgnoreCase("taskPaneMenu"))
					taskPane.clickItem("UndoCheckout");

				Thread.sleep(500);
				//Click Yes button
				clickYesButton();

				//Wait for invisibility of 'wait overlay' 
				new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
				.pollingEvery(125,TimeUnit.MILLISECONDS)
				.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));

				afterUndoVersion=Integer.parseInt(getListViewColumnValue("Version",colIndex).trim());
				if (beforeUndoVersion>afterUndoVersion)
					return true;
			}

		}
		catch(Exception e) {
			Log.exception(new Exception("Error while 'UndoCheckout'"),driver);

		}
		return false;
	}

	/**
	 * Description: Replace File 
	 * @param fileLocation
	 * @param fileName
	 * @throws Exception
	 */
	public Boolean replaceFile(String fileLocation,String fileExtension,String versionIndex) throws Exception
	{
		try
		{

			MetadataCard metadatacard=new MetadataCard(driver);
			metadatacard.clickUploadBtn(fileLocation);
			metadatacard.clickOkOnUploadDialog();

			//Wait for invisibility of 'wait overlay' 
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));

			//get the columnIndex
			int colIndex=getListViewColumnIndex("Name");
			//verify Name column value and see if it is checkedOut 
			String newName=getListViewColumnValue("Name",colIndex);

			//get the columnIndex
			int versionColIndex=getListViewColumnIndex("Version");
			//verify Version column value and see if it is checkedOut 
			String versionValue=getListViewColumnValue("Version",versionColIndex);

			if(newName.contains(fileExtension) || (!versionValue.equals(versionIndex)))
				return true;
		}
		catch(Exception e)
		{
			Log.exception(new Exception("Some problems while Replacing file"));

		}
		return false;
	}
	/**
	 * Select the 'option' from the object 'context menu'
	 * @param action
	 * @throws Exception
	 */
	public void selectContextMenuItemFromListView(String action) throws Exception
	{
		try{
			//Select the required option from the 'context menu' 
			JavascriptExecutor js = (JavascriptExecutor) driver; 
			js.executeScript("var contextMenu = document.getElementById('"+action+"');contextMenu.click();");

		}
		catch(Exception e)
		{
			throw new Exception("Error while performing '"+action+"'");
		}

	}

	/**
	 * Verify if 'UndoCheckOut' operation is done
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isObjectCheckOutUndone() throws Exception
	{
		int beforeUndoVersion,afterUndoVersion;
		try{
			//fetch the object version before operation
			String beforeUndoCheckOut=getListViewColumnValue("Version");
			if (!beforeUndoCheckOut.isEmpty())
			{
				beforeUndoVersion=Integer.parseInt(beforeUndoCheckOut.trim());

				//Perform 'UndoCheckout' on object
				JavascriptExecutor js = (JavascriptExecutor) driver; 
				js.executeScript("var contextMenu = document.getElementById('UndoCheckout');contextMenu.click();");


				new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
				.pollingEvery(125,TimeUnit.MILLISECONDS)
				.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[class='message']"))));

				//Click Yes button
				clickYesButton();

				//Wait for invisibility of 'wait overlay' 
				new WebDriverWait(driver,60).ignoring(NoSuchElementException.class) 
				.pollingEvery(125,TimeUnit.MILLISECONDS)
				.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[class='list_overlay'][style*='waiting_overlay.png']")));


				//fetch the object version after operation
				String afterUndoCheckOut=getListViewColumnValue("Version");
				afterUndoVersion=Integer.parseInt(afterUndoCheckOut.trim());

				//Verify if object version is same as before after 'Undo'  
				if (beforeUndoVersion>afterUndoVersion)
					return true;
			}

		}
		catch(Exception e)
		{
			throw new Exception("Error while 'UndoCheckout'");

		}

		return false;
	}
	/**
	 * Description : 'Refresh' list view items
	 * @param driver
	 * @throws Exception 
	 */
	public void clickRefresh() throws Exception
	{
		try
		{
			//Click 'Refresh' button to refresh objects list
			refreshBtn.click();
			//Wait for invisibility of the loading image
			Utils.fluentWait(driver);
		}
		catch(NoSuchElementException e)
		{
			throw new Exception("Refresh Button not found.");
		}

	}

	/**
	 * Description : verify LoggedIn 'UserType'
	 * @param driver
	 * @param userType
	 * @return true or false
	 * @throws Exception
	 */
	public boolean verifyUserType(WebDriver driver,String userType) throws Exception
	{
		if (userType.equalsIgnoreCase("External"))
		{
			try
			{
				//Verify the display of 'New' Object Creation items on task pane using 'readOnly' credentials
				if (driver.findElement(By.cssSelector("div[id='taskpaneNew']>div[class='childItems']>div[class='taskpaneButton']")).isDisplayed())
					throw new Exception("'New' Object creation items are displayed in Task pane for External user.");

			}
			catch(NoSuchElementException e)
			{
				return true;
			}
		}

		else if (userType.equalsIgnoreCase("ReadOnly"))
		{
			//Click 'Document' link from taskpane
			//clickDocumentLink();
			clickTaskPaneNewLink("Document");//clicks 'Document' link

			try
			{
				//Verify the display of 'Error message' when attepting to create new object using 'readOnly' credentials
				String ErrMsg="Access denied. Your license type does not allow this operation.";

				//if (driver.findElement(By.cssSelector("div[class='errorDialog']>div[class='shortErrorArea']")).getText().equalsIgnoreCase(ErrMsg));
				if (popUpDialogMsg.getText().equalsIgnoreCase(ErrMsg))
					return true;
			}
			catch(Exception e)
			{
				throw new Exception(" ReadOnly' User is allowed to create New Object type.");

			}

		}
		return false;
	}
	/**
	 * Description : return Listview column Index value
	 * @param driver
	 * @param contextMenuItem
	 * @return Column Index
	 * @throws Exception 
	 */
	public int getListViewColumnIndex(String headerValue) throws Exception
	{
		int columnIndex=0;
		try{

			List<WebElement> tableHeaders=driver.findElements(By.cssSelector("table[id='headerTable']>thead>tr>th[id='headerTable']"));

			for(int j=0;j<tableHeaders.size();j++)
			{
				if (tableHeaders.get(j).getText().equalsIgnoreCase(headerValue))
				{
					tableHeaders.get(j);
					columnIndex=j;
					break;
				}

			}

		}
		catch(Exception e)
		{
			throw new Exception("Could not read the ListView Column header 'Index'.");
		}
		return columnIndex;
	}

	/**
	 * Description : return Listview column Name
	 * @param driver
	 * @param contextMenuItem
	 * @return Column Index
	 * @throws Exception 
	 */
	public boolean readListViewHeaderNames(WebDriver driver,String contextMenuItem) throws Exception
	{
		try{
			Thread.sleep(500);
			WebElement tableHeaders=driver.findElement(By.cssSelector("table[id='headerTable']>thead>tr"));
			String[] columnName=tableHeaders.getText().trim().split("\n");			   
			for(String column:columnName) {
				if (column.trim().toUpperCase().equalsIgnoreCase(contextMenuItem.trim().toUpperCase())) {
					return true;
				}
			}

		}
		catch(Exception e)
		{
			Log.exception(new Exception("New column "+contextMenuItem+" is not displayed."),driver);
		}
		return false;
	}

	/**
	 * 
	 * @param driver
	 * @return
	 * @throws Exception 
	 */
	public List<String> getGotoItemList(WebDriver driver) throws Exception
	{
		final List <String> items = new ArrayList <String>();

		try {
			List<WebElement> taskPaneGotoItems=driver.findElements(By.cssSelector("div[id='taskpaneGoTo']>div[class='childItems']>div"));
			if(taskPaneGotoItems.size()<1)
				throw new NullPointerException("Goto items are not displayed in the Taskpane.");
			for (final WebElement item : taskPaneGotoItems)
				items.add(item.getText());
		}
		catch (NoSuchElementException |InvalidElementStateException e) {
			throw new Exception("'GotoItem' list is not displayed in TaskPane.");
		}

		return items;
	}

	/**
	 * 	
	 * @param driver
	 * @param item
	 * @return
	 * @throws Exception 
	 */
	public boolean isGoToItemExists(WebDriver driver,String item) throws Exception
	{
		try
		{
			List<String> itemList=getGotoItemList(driver);
			if (itemList.size()>0)
			{
				for(String name:itemList)
				{
					if (name.toLowerCase().trim().contains(item.toLowerCase().trim())){
						return true;
					}
				}
			}

		}

		catch(Exception e)
		{
			if(e.getClass().toString().contains("NoSuchElementException") || e.getClass().toString().contains("NullPointerException"))
				throw new SkipException("No Items exists in 'Goto' Section of TaskPane.");
			else
				throw e;
		}

		return false;
	}

	/**
	 * 
	 * @param driver
	 * @param columnLevels
	 * @throws Exception 
	 */
	public void clickAndInsertListViewColumns(WebDriver driver,String columnLevels) throws Exception
	{

		try{

			WebElement actionItem=driver.findElement(By.cssSelector("table[id='headerTable']>thead>tr>th[id='headerTable']>span"));

			ActionEventUtils.rightClick(driver, actionItem);

			if (columnLevels.isEmpty())
				throw new Exception("Column name is Null, please specify.");

			String[] levels=columnLevels.split("->");

			int level=1;
			for(String levelname:levels)
			{
				selectHeaderPopUpMenuItem(driver,levelname,level);
				if (level>levels.length)
				{
					break;
				}
				level++;
			}

		}
		catch(Exception e){
			e.printStackTrace();
			//throw new Exception("Could not add new columns.");

		}

	}
	/**
	 * 
	 * @param driver
	 * @param columnLevels
	 * @throws Exception
	 */
	public void clickAndRemoveListViewColumns(WebDriver driver,String columnName) throws Exception
	{

		try{

			List<WebElement> tableHeaders=driver.findElements(By.cssSelector("table[id='headerTable']>thead>tr>th[id='headerTable']"));

			for(int j=0;j<tableHeaders.size();j++)
			{
				if (tableHeaders.get(j).getText().contains(columnName))
				{
					ActionEventUtils.rightClick(driver, tableHeaders.get(j));
					List<WebElement> listItems=driver.findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li"));
					for(int i=0;i<listItems.size();i++) {
						if(listItems.get(i).getText().equalsIgnoreCase("Remove this column")) {
							ActionEventUtils.click(driver, listItems.get(i));
							break;
						}
					}
					break;
				}

			}

		}//try
		catch(Exception e){
			throw new Exception("Could not add new columns.");
		}

	}

	/**
	 * 
	 * @param driver
	 * @param menuText
	 * @return
	 * @throws Exception 
	 */
	public void selectHeaderPopUpMenuItem(WebDriver driver,String menuText,int level) throws Exception
	{

		try{
			List<WebElement> listItems=driver.findElements(By.cssSelector("div[class='menu-div outerbox']>ul[class='menu-ul innerbox']>li"));

			for(int i=0;i<listItems.size();i++)
			{		
				if (listItems.get(i).getText().trim().equalsIgnoreCase(menuText.trim()))
				{
					ActionEventUtils.click(driver, listItems.get(i));
					break;

				}
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not insert Columns");
		}
	}


	/**
	 * @throws Exception 
	 * 
	 */
	public boolean hideTreeView() throws Exception
	{

		try{
			hideTreeView.isDisplayed();
			hideTreeView.click();
			Utils.fluentWait(driver);
		}
		catch(NoSuchElementException | InvalidElementStateException e){
			throw new SkipException("HideTreeView icon not displayed.");
		}

		if (driver.findElement(By.cssSelector("div[id='panel'][style*='display: none;']")).isDisplayed())
		{
			return true;
		}
		return false;

	}

	/**
	 * @throws Exception 
	 * 
	 */
	public boolean showTreeView() throws Exception
	{

		try{
			//check if 'Tree' view icon displayed
			if (!showTreeView.isDisplayed())
				Log.exception(new Exception("showTreeView icon not displayed."));
			//Click 'Tree' view icon
			showTreeView.click();

			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125, TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("div[id='panel'][style*='display: block;']"))));

			if (driver.findElement(By.cssSelector("div[id='panel'][style*='display: block;']")).isDisplayed()){
				return true;
			}
		}
		catch(Exception e)
		{
			Log.exception(new NoSuchElementException("showTreeView icon not displayed."),driver);
		}
		return false;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean navigateToHome() throws Exception
	{
		try{
			//click 'Home' on task pane
			taskPaneHome.click();

			//Verify the 'BreadCrumb' title
			if (breadCrumbHome.isDisplayed())
			{
				return true;
			}
		}catch(NoSuchElementException e){
			Log.exception(new Exception("Unable to Navigate to HomePage."),driver);
		}
		return false;
	}


	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean navigateToGotoViews(String breadcrumbTitle) throws Exception
	{
		try{
			switch(breadcrumbTitle){
			case "Favorites": taskPaneFavorites.click();//click 'Favorites' from taskpane
			break;
			case "Checked Out to Me": taskPaneCheckedOutToMe.click();//click 'CheckedOutToMe' from taskpane
			break;
			case "Assigned to Me": taskPaneAssignedToMe.click();//click 'AssignedToMe' from taskpane
			break;
			case "Recently Accessed by Me": taskPaneRecentlyAccessedByMe.click();//click 'RecentlyAccessedByMe' from taskpane
			break;
			}
			Thread.sleep(200);
			Utils.fluentWait(driver);
			//Fetch the 'Breadcrumb title
			if (breadCrumbViewLabel.getText().trim().contains(breadcrumbTitle.trim())){
				return true;	
			}
		}catch(NoSuchElementException | StaleElementReferenceException e){
			throw new Exception("Breadcrumb Label displayed as :"+breadCrumbViewLabel.getText());
		}
		return false;
	}


	/**
	 * Description :Verify the View Page Header
	 * @param headerName
	 * @return
	 * @throws Exception 
	 */
	public boolean isViewPageDisplayed(String headerName) throws Exception
	{
		String listViewHeader=null;
		try{
			//Verify the title of the List view
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(250,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(viewTabHeader));
			viewTabHeader.isDisplayed();
			if (viewTabHeader.getText().contains("-")) {
				String[] headerTitle=viewTabHeader.getText().trim().split("-");
				listViewHeader=headerTitle[0];
			}
			else
				listViewHeader=viewTabHeader.getText();

			if (listViewHeader.trim().equalsIgnoreCase(headerName.trim()))
			{
				return true;
			}
		}
		catch(NoSuchElementException e)
		{
			Log.exception(new NoSuchElementException("Expected Page header :'"+headerName+"' , But Page Header displayed as :'"+listViewHeader+"'"),driver);
		}
		return false;
	}
	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Exception
	 */

	public boolean isConfirmationDialogDisplayed(String msg) throws Exception
	{
		try
		{
			//Verify if confirmation dialog displayed
			popUpDialogMsg.isDisplayed();
			if (popUpDialogMsg.getText().trim().contains(msg.trim()))
			{
				clickOKButton();//click OK button
				return true;
			}
		}
		catch(NoSuchElementException e)
		{
			throw new Exception("Could not display Popup dialog.");
		}
		return false;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void clickOKButton() throws Exception
	{
		try{
			//Click OK button
			WebElement okBtn=driver.findElement(By.cssSelector("button[class='window_ok']"));
			okBtn.click();
		}
		catch(Exception e)
		{
			throw new Exception("Could not Click 'OK' button.");
		}
	}
	/**
	 * Description: Click Cancel button
	 * @throws Exception
	 */
	public void clickCancelButton() throws Exception
	{
		try{
			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			.pollingEvery(125,TimeUnit.MILLISECONDS)
			.until(ExpectedConditions.visibilityOf(cancelBtn));

			//click 'cancel' button
			cancelBtn.click();
		}catch(NoSuchElementException e)
		{
			throw new Exception("Could not Click 'Cancel' button.");
		}
	}
	/**
	 * 
	 * @throws Exception
	 */
	public void clickYesButton() throws Exception
	{
		try{
			//			new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
			//			.pollingEvery(125,TimeUnit.MILLISECONDS)
			//			.until(ExpectedConditions.visibilityOf(yesBtn));

			Thread.sleep(500);
			//click 'Yes' button on confirmation dialog
			yesBtn.click();
		}
		catch(NoSuchElementException e)
		{
			throw new Exception("Could not Click 'YES' button.");
		}
	}

	/**
	 * 
	 * @param option
	 * @param objectName
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public boolean removeFromFavorites(String objectName,String columnName) throws Exception
	{
		try{
			clickYesButton();

			clickOKButton();
			Thread.sleep(500);
			//click Refresh icon on homepage
			clickRefresh();
			//verify if object found in list view
			if (!isDataInListView(driver,objectName,columnName)){
				return true;
			}
		}catch(NoSuchElementException e)
		{
			throw new Exception("Could not Remove object from 'Favorites'");
		}
		return false;
	}

	/**
	 * Dscription: Verify if 'SingleFile' converted to 'MFD'
	 * @param file
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isSingleFileConvertedToMultiFile(String file) throws Exception
	{
		try{


			int colIndex=getListViewColumnIndex("Single File");
			String isSingle=getListViewColumnValue("Single File",colIndex);
			if (isSingle.equalsIgnoreCase("No"))
			{
				return true;
			}
		}catch(Exception e){
			throw new Exception("Error while converting to 'Multi File Document'");
		}
		return false;
	}

	/**
	 * Description: Verify if MFD converted to 'Single file'
	 * @param file
	 * @return true or false
	 * @throws Exception
	 */
	public boolean isMultiFileConvertedToSingle(String file) throws Exception
	{
		try{

			String isSingle=getListViewColumnValue("Single File");
			if (!isSingle.isEmpty()&isSingle.equalsIgnoreCase("Yes"))
			{
				return true;
			}
		}
		catch(Exception e)
		{
			throw new Exception("Error while converting to 'Single File'");
		}

		return false;
	}

	/**
	 * Description : gets the object hyperlink
	 * @return object hyperlink
	 * @throws Exception
	 */
	public String getObjectHyperLink() throws Exception {
		String linkValue=null;
		try {
			//copyToClipboard.click();
			Thread.sleep(500);
			linkValue=driver.findElement(By.cssSelector("input[class='prompt']")).getAttribute("value");
			clickCancelButton();
		}
		catch(Exception e) {
			Log.exception(new Exception("Could not find 'copyToClipboar' link."),driver);
		}
		if (linkValue.isEmpty())
		{
			Log.exception(new Exception("Hyperlink copied as 'Null'"),driver);
		}

		return linkValue;
	}

	/**
	 * Description : close the Hyperlink PopupDialog
	 * 
	 */
	public void closeHyperLinkPopUpDialog()
	{
		WebElement closeDialog=driver.findElement(By.cssSelector("button[class$='ui-dialog-titlebar-close']"));
		closeDialog.click();
		new WebDriverWait(driver,60).ignoring(NoSuchElementException.class)
		.pollingEvery(125,TimeUnit.MILLISECONDS)
		.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("ui-draggable")));
	}

	/**
	 * Description : Logout using 'task pane' link
	 * @return LoginPage
	 * @throws Exception
	 */
	public LoginPage clickLogOut() throws Exception
	{
		try {
			taskPaneLogOut.click();	
			Thread.sleep(500);
			Utils.isLogOutPromptDisplayed(driver);
			Utils.waitForPageLoad(driver);

		}
		catch(NoSuchElementException e)	{
			Log.exception(new Exception("Could not find/click 'LogOut' link from taskpane."), driver);
		}

		return new LoginPage(driver);
	}

	/**
	 * <br>Description: Get the Breadcrumb text</br>
	 * @return Breadcrumb label
	 * @throws Exception
	 */
	public String getBreadcrumbText() throws Exception{
		String breadCrumbText=null;
		try{

			breadCrumbText=breadCrumbViewLabel.getText();
		}
		catch(NoSuchElementException e)	{
			Log.exception(new Exception("Could not get 'Breadcrumb Label' on HomePage."), driver);
		}
		return breadCrumbText;

	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Boolean isCheckOutPromptDisplayed() throws Exception {
		try{
			WebElement logOutPrompt=driver.findElement(By.cssSelector("div[class*='ui-draggable']>div[class='promptWindow ui-dialog-content ui-widget-content']>table[class='buttons']>tbody>tr>td>button[class='checkout']"));
			if(logOutPrompt.isDisplayed()) {
				return true;
			}
		}
		catch(Exception e) {
			if(!e.getClass().toString().contains("NoSuchElementException")) 
				Log.exception(new Exception("Cound not found/click 'LogOut prompt' element."),driver);
			else 
				return false;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public Boolean isTopMenuExists() {
		try{
			topMenuBar.isDisplayed();
			return true;
		}
		catch(Exception e){
			if(e.getClass().toString().contains("NoSuchElementException")|e.getClass().toString().contains("StaleElementException"))
				return false;
			else
				throw e;
		}
	}

	/**
	 * isWorkflowdialogDisplayed : This method is to if taskpanel is displayed
	 * @param none
	 * @return true if task pane displayed; false if task pane not displayed
	 * @throws Exception
	 */
	public Boolean isWorkflowdialogDisplayed() throws Exception {

		final long startTime = StopWatch.startTime();

		try {

			Utils.fluentWait(driver);

			//WebElement workflowdialog= this.driver.findElement(By.cssSelector("div[class='ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable ui-header-titlebar']"));
			WebElement workflowdialog= this.driver.findElement(By.cssSelector("div[class='ui-dialog-titlebar ui-corner-all ui-widget-header ui-helper-clearfix ui-draggable-handle']"));

			String dialogHeaderText = workflowdialog.findElement(By.cssSelector("span[class*='ui-dialog-title']")).getText();

			if (dialogHeaderText.toUpperCase().contains("WORKFLOW")) 
			{
				Log.event("Workflow dialog is Displayed in MFWA.", StopWatch.elapsedTime(startTime));
				return true;			
			}
			else
			{
				Log.event("Workflow dialog is not Displayed in HomePage.", StopWatch.elapsedTime(startTime));
				return false;

			}

		} //End try

		catch (Exception e) {
			if (e.getClass().toString().contains("NoSuchElementException")) { 
				Log.event("Workflow dialog is not Displayed in HomePage.", StopWatch.elapsedTime(startTime));
				return false;
			}
			else				
				throw e;
		} //End catch

	} //End isSearchbarPresent	

}//End class HomePage



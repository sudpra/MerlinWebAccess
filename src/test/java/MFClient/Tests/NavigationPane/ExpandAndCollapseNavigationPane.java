package MFClient.Tests.NavigationPane;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.WebDriverUtils;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import MFClient.Pages.ConfigurationPage;
import MFClient.Pages.HomePage;
import MFClient.Pages.LoginPage;
import MFClient.Wrappers.Caption;
import MFClient.Wrappers.Utility;

@Listeners(EmailReport.class)
public class ExpandAndCollapseNavigationPane {

	public String xlTestDataWorkBook = null;
	public String loginURL = null;
	public String configURL = null;
	public String userName = null;
	public String password = null;
	public String testVault = null;
	public String className = null;
	public String productVersion = null;
	public WebDriver driver = null;
	public String driverType = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass (alwaysRun = true)
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");
			driverType = xmlParameters.getParameter("driverType");

			className = this.getClass().getSimpleName().toString().trim();
			if (xmlParameters.getParameter("driverType").equalsIgnoreCase("IE"))
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim() + xmlParameters.getParameter("driverVersion").trim();
			else
				productVersion = "M-Files " + xmlParameters.getParameter("productVersion").trim() + " - " + xmlParameters.getParameter("driverType").toUpperCase().trim();

			Utility.restoreTestVault();
			Utility.configureUsers(xlTestDataWorkBook);

		} //End try

		catch(Exception e) {
			if (e instanceof SkipException) 
				throw new SkipException(e.getMessage());
			else if (e.getClass().toString().contains("NullPointerException")) 
				throw new Exception ("Test data sheet does not exists.");
			else
				throw e;
		} //End catch

	} //End init

	/**
	 * quitDriver: Quits the driver after the method
	 * @throws Exception
	 */
	@AfterMethod (alwaysRun = true)
	public void quitDriver() throws Exception{

		try {

			if (driver != null)//Checks if driver is not equals to null
				driver.quit();//Quits the driver

			Log.endTestCase();//Ends the test case

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}//End quitDriver

	/**
	 * cleanApp : At after class method to destroy the vault used in the class
	 */
	@AfterClass (alwaysRun = true)
	public void cleanApp() throws Exception{

		try {
			Utility.destroyUsers(xlTestDataWorkBook);
			Utility.destroyTestVault();

		}//End try

		catch(Exception e){
			throw e;
		}//End Catch
	}

	/**
	 * 56.1.2.1 : Clicking an item in tree view should expand the item in Default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56","ExpandAndCollapseNavigationPane"}, 
			description = "Clicking an item in tree view should expand the item in Default layout")
	public void SprintTest56_1_2_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Single click the item
			//------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_2_1

	/**
	 * 56.1.2.2 : Clicking an item in tree view should expand the item in Default layout with navigation pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56", "ExpandAndCollapseNavigationPane"}, 
			description = "Clicking an item in tree view should expand the item in Default layout with navigation pane")
	public void SprintTest56_1_2_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout with navigation pane' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Single click the item
			//------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("4. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			catch(Exception e1){
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_2_2

	/**
	 * 56.1.2.3 : Clicking an item in tree view should expand the item in No Java Applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56","ExpandAndCollapseNavigationPane"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet layout")
	public void SprintTest56_1_2_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to ' No Java Applet Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Single click the item
			//------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_2_3

	/**
	 * 56.1.2.4 : Clicking an item in tree view should expand the item in No Java Applet no task are layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet no task area layout")
	public void SprintTest56_1_2_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to ' No Java Applet Layout no task area' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Single click the item
			//------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_2_4

	/**
	 * 56.1.2.5 : Clicking an item in tree view should expand the item in No Java Applet no task area but show GoTo shortcuts layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet no task area but show GoTo shortcuts layout")
	public void SprintTest56_1_2_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to ' No Java Applet Layout no task area but show GoTo shortcuts' layout
			//-----------------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Single click the item
			//------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_2_5

	/**
	 * 56.1.3.1 : Clicking left expand arrow icon for collapsed item should expand the item in Default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking left expand arrow icon for collapsed item should expand the item in Default layout")
	public void SprintTest56_1_3_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Click Expand icon for the item
			//---------------------------------------
			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("CLOSED"))
				throw new Exception("Item is not collapsed state");

			homePage.treeView.clickExpandArrowIcon(dataPool.get("ItemToClick"));

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane with default layout.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane with default layout. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_3_1

	/**
	 * 56.1.3.2 : Clicking left expand arrow icon for collapsed item should expand the item in Default layout with navigation pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking left expand arrow icon for collapsed item should expand the item in Default layout with navigation pane")
	public void SprintTest56_1_3_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default layout with navigation pane' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Click Expand icon for the item
			//---------------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("CLOSED"))
				throw new Exception("Item is not collapsed state");

			homePage.treeView.clickExpandArrowIcon(dataPool.get("ItemToClick"));

			Log.message("4. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			try
			{

				Utility.resetToDefaultLayout(driver);
			}
			catch(Exception e1){
				Log.exception(e1, driver);
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_3_2

	/**
	 * 56.1.3.3 : Clicking left expand arrow icon for collapsed item should expand the item in No Java applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking left expand arrow icon for collapsed item should expand the item in No Java applet layout")
	public void SprintTest56_1_3_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet' layout
			//-------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");
			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Click Expand icon for the item
			//---------------------------------------
			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("CLOSED"))
				throw new Exception("Item is not collapsed state");

			homePage.treeView.clickExpandArrowIcon(dataPool.get("ItemToClick"));

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_3_3

	/**
	 * 56.1.3.4 : Clicking left expand arrow icon for collapsed item should expand the item in No Java applet and no task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking left expand arrow icon for collapsed item should expand the item in No Java applet and no task area layout")
	public void SprintTest56_1_3_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet and no task area' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Click Expand icon for the item
			//---------------------------------------
			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("CLOSED"))
				throw new Exception("Item is not collapsed state");

			homePage.treeView.clickExpandArrowIcon(dataPool.get("ItemToClick"));

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_3_4

	/**
	 * 56.1.3.5 : Clicking left expand arrow icon for collapsed item should expand the item in No Java applet and no task area (but show "Go To" shortcuts) layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking left expand arrow icon for collapsed item should expand the item in No Java applet and no task area (but show 'Go To' shortcuts) layout")
	public void SprintTest56_1_3_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet and no task area (but show "Go To" shortcuts)' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Click Expand icon for the item
			//---------------------------------------
			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("CLOSED"))
				throw new Exception("Item is not collapsed state");

			homePage.treeView.clickExpandArrowIcon(dataPool.get("ItemToClick"));

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_3_5

	/**
	 * 56.1.4.1 : Clicking the expanded item should not collapse the item in Default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in Default layout")
	public void SprintTest56_1_4_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item
			//-------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is expanded.");

			//Step-6 : Single click the item again
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("6. Item (" + dataPool.get("ItemToClick") + ") is clicked again.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the expanded item did not get collapsed in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the expanded item got collapsed in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_4_1

	/**
	 * 56.1.4.2 : Clicking the expanded item should not collapse the item in Default layout with navigation pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in Default layout with navigation pane")
	public void SprintTest56_1_4_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default layout with navigation pane' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Expand the item
			//-------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("4. Item (" + dataPool.get("ItemToClick") + ") is expanded.");

			//Step-5 : Single click the item again
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked again.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the expanded item did not get collapsed in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the expanded item got collapsed in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest56_1_4_2

	/**
	 * 56.1.4.3 : Clicking the expanded item should not collapse the item in No Java applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in No Java applet layout")
	public void SprintTest56_1_4_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet' layout
			//-------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item
			//-------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is expanded.");

			//Step-6 : Single click the item again
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("6. Item (" + dataPool.get("ItemToClick") + ") is clicked again.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the expanded item did not get collapsed in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the expanded item got collapsed in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest56_1_4_3

	/**
	 * 56.1.4.4 : Clicking the expanded item should not collapse the item in No Java applet and no task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in No Java applet and no task area layout")
	public void SprintTest56_1_4_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet and no task area' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");


			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item
			//-------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is expanded.");

			//Step-6 : Single click the item again
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("6. Item (" + dataPool.get("ItemToClick") + ") is clicked again.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the expanded item did not get collapsed in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the expanded item got collapsed in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest56_1_4_4

	/**
	 * 56.1.4.5 : Clicking the expanded item should not collapse the item in No Java applet and no task area (but show 'Go To' shortcuts) layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"part1","Sprint56"}, 
			description = "Clicking an item in tree view should expand the item in No Java applet and no task area (but show 'Go To' shortcuts) layout")
	public void SprintTest56_1_4_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java applet and no task area (but show "Go To" shortcuts)' layout
			//-----------------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item
			//-------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is expanded.");

			//Step-6 : Single click the item again
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("6. Item (" + dataPool.get("ItemToClick") + ") is clicked again.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the expanded item did not get collapsed in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the expanded item got collapsed in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest56_1_4_5

	/**
	 * 56.1.9.1 : Clicking left collapse arrow icon for expanded item should collapse the item in default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Clicking left collapse arrow icon for expanded item should collapse the item in default layout.")
	public void SprintTest56_1_9_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item in the tree view
			//-----------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick")); //Clicks the item that gets expanded.

			if (homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("NO CHILD"))
				throw new SkipException("Invalid test data. Item (" + dataPool.get("ItemToClick") + ") does not have child to expand or collapse");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("OPEN"))
				throw new Exception("Item (" + dataPool.get("ItemToClick") + ") is not in expanded state.");

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Step-6 : Click collapse icon for the expanded item in the tree view.
			//-------------------------------------------------------------------
			homePage.treeView.clickCollapseArrowIcon(dataPool.get("ItemToClick")); //Clicks Collapse arrow icon

			Log.message("6. Collapse icon is selected for the item.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("CLOSED"))
				Log.pass("Test case Passed. On clicking collapse icon for the expanded item in navigation pane has collapsed the item.");
			else
				Log.fail("Test case Failed. On clicking collapse icon for the expanded item in navigation pane has not collapsed the item.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_9_1

	/**
	 * 56.1.9.2 : Clicking left collapse arrow icon for expanded item should collapse the item in default layout with navigation pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Clicking left collapse arrow icon for expanded item should collapse the item in default layout with navigation pane.")
	public void SprintTest56_1_9_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout with navigation pane' layout
			//-----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Expand the item in the tree view
			//-----------------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick")); //Clicks the item that gets expanded.

			if (homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("NO CHILD"))
				throw new SkipException("Invalid test data. Item (" + dataPool.get("ItemToClick") + ") does not have child to expand or collapse");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("OPEN"))
				throw new Exception("Item (" + dataPool.get("ItemToClick") + ") is not in expanded state.");

			Log.message("4. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Step-5 : Click collapse icon for the expanded item in the tree view.
			//-------------------------------------------------------------------
			homePage.treeView.clickCollapseArrowIcon(dataPool.get("ItemToClick")); //Clicks Collapse arrow icon

			Log.message("5. Collapse icon is selected for the item.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("CLOSED"))
				Log.pass("Test case Passed. On clicking collapse icon for the expanded item in navigation pane has collapsed the item.");
			else
				Log.fail("Test case Failed. On clicking collapse icon for the expanded item in navigation pane has not collapsed the item.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_9_2

	/**
	 * 56.1.9.3 : Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet layout.")
	public void SprintTest56_1_9_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item in the tree view
			//-----------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick")); //Clicks the item that gets expanded.

			if (homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("NO CHILD"))
				throw new SkipException("Invalid test data. Item (" + dataPool.get("ItemToClick") + ") does not have child to expand or collapse");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("OPEN"))
				throw new Exception("Item (" + dataPool.get("ItemToClick") + ") is not in expanded state.");

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Step-6 : Click collapse icon for the expanded item in the tree view.
			//-------------------------------------------------------------------
			homePage.treeView.clickCollapseArrowIcon(dataPool.get("ItemToClick")); //Clicks Collapse arrow icon

			Log.message("6. Collapse icon is selected for the item.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("CLOSED"))
				Log.pass("Test case Passed. On clicking collapse icon for the expanded item in navigation pane has collapsed the item.");
			else
				Log.fail("Test case Failed. On clicking collapse icon for the expanded item in navigation pane has not collapsed the item.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_9_3

	/**
	 * 56.1.9.4 : Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet No task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet No Task Area layout.")
	public void SprintTest56_1_9_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet No Task Area Layout' layout
			//---------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");


			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item in the tree view
			//-----------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick")); //Clicks the item that gets expanded.

			if (homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("NO CHILD"))
				throw new SkipException("Invalid test data. Item (" + dataPool.get("ItemToClick") + ") does not have child to expand or collapse");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("OPEN"))
				throw new Exception("Item (" + dataPool.get("ItemToClick") + ") is not in expanded state.");

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Step-6 : Click collapse icon for the expanded item in the tree view.
			//-------------------------------------------------------------------
			homePage.treeView.clickCollapseArrowIcon(dataPool.get("ItemToClick")); //Clicks Collapse arrow icon

			Log.message("6. Collapse icon is selected for the item.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("CLOSED"))
				Log.pass("Test case Passed. On clicking collapse icon for the expanded item in navigation pane has collapsed the item.");
			else
				Log.fail("Test case Failed. On clicking collapse icon for the expanded item in navigation pane has not collapsed the item.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_9_4

	/**
	 * 56.1.9.5 : Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet No task area but Show GoTo shortcuts layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Clicking left collapse arrow icon for expanded item should collapse the item in No Java Applet No Task Area but Show GoTo shortcuts layout.")
	public void SprintTest56_1_9_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet No Task Area Layout' layout
			//---------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : Expand the item in the tree view
			//-----------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick")); //Clicks the item that gets expanded.

			if (homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("NO CHILD"))
				throw new SkipException("Invalid test data. Item (" + dataPool.get("ItemToClick") + ") does not have child to expand or collapse");

			if (!homePage.treeView.getItemStatus(dataPool.get("ItemToClick")).equalsIgnoreCase("OPEN"))
				throw new Exception("Item (" + dataPool.get("ItemToClick") + ") is not in expanded state.");

			Log.message("5. Expanded arrow icon of item (" + dataPool.get("ItemToClick") + ") is clicked.");

			//Step-6 : Click collapse icon for the expanded item in the tree view.
			//-------------------------------------------------------------------
			homePage.treeView.clickCollapseArrowIcon(dataPool.get("ItemToClick")); //Clicks Collapse arrow icon

			Log.message("6. Collapse icon is selected for the item.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("CLOSED"))
				Log.pass("Test case Passed. On clicking collapse icon for the expanded item in navigation pane has collapsed the item.");
			else
				Log.fail("Test case Failed. On clicking collapse icon for the expanded item in navigation pane has not collapsed the item.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_9_5

	/**
	 * 56.1.12.1 : Continuous click an item in tree view in default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Continuous click an item in tree view in default layout")
	public void SprintTest56_1_12_1(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_Default.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : click the item continuously
			//-------------------------------------
			Random rand = new Random();
			int randomNum = rand.nextInt((10 - 3) + 1) + 3;

			for (int i=0; i<randomNum; i++)
				homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked " + randomNum + " times.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_12_1

	/**
	 * 56.1.12.2 : Continuous click an item in tree view in default with navigation pane layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Continuous click an item in tree view in default with navigation pane layout")
	public void SprintTest56_1_12_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation Pane Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-5 : click the item continuously
			//-------------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Random rand = new Random();
			int randomNum = rand.nextInt((10 - 3) + 1) + 3;

			for (int i=0; i<randomNum; i++)
				homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked " + randomNum + " times.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e0){
					Log.exception(e0, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest56_1_12_2

	/**
	 * 56.1.12.3 : Continuous click an item in tree view in No Java Applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Continuous click an item in tree view in No Java Applet layout")
	public void SprintTest56_1_12_3(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);//Disabled the java applet in configuration page

			if(configurationPage.configurationPanel.isJavaAppletEnabled())//Verify if java applet is disabled or not
				throw new Exception("Java applet is enabled.");

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings in configuration page
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. Java applet is disabled and Configuration settings are saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : click the item continuously
			//-------------------------------------
			Random rand = new Random();
			int randomNum = rand.nextInt((10 - 3) + 1) + 3;

			for (int i=0; i<randomNum; i++)
				homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked " + randomNum + " times.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_12_3

	/**
	 * 56.1.12.4 : Continuous click an item in tree view in No Java Applet, No Task Area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Continuous click an item in tree view in No Java Applet, No Task Area layout")
	public void SprintTest56_1_12_4(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_NoTaskArea.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet and no task area layout is selected and saved.");


			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : click the item continuously
			//-------------------------------------
			Random rand = new Random();
			int randomNum = rand.nextInt((10 - 3) + 1) + 3;

			for (int i=0; i<randomNum; i++)
				homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked " + randomNum + " times.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_12_4

	/**
	 * 56.1.12.5 : Continuous click an item in tree view in No Java Applet, No Task Area but Show GoTo shortcuts layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Continuous click an item in tree view in No Java Applet, No Task Area but Show GoTo shortcuts layout")
	public void SprintTest56_1_12_5(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null;

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area Layout but show GoTo shortcuts' layout
			//----------------------------------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setJavaApplet(Caption.ConfigSettings.Config_Disable.Value);

			if(configurationPage.configurationPanel.isJavaAppletEnabled())
				throw new Exception("Java applet is enabled");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_TaskAreaWithShowGoTo.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. No java applet with task area with go to layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Enable navigation pane
			//-------------------------------
			homePage.menuBar.ClickOperationsMenu(Caption.MenuItems.DisplayMode.Value + ">>" + Caption.MenuItems.NavigationPane.Value); //Enables Navigation Pane from operations menu

			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Log.message("4. Navigation pane is enabled from display mode.");

			//Step-5 : click the item continuously
			//-------------------------------------
			Random rand = new Random();
			int randomNum = rand.nextInt((10 - 3) + 1) + 3;

			for (int i=0; i<randomNum; i++)
				homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") is clicked " + randomNum + " times.");

			//Verification : To Verify if item has expanded on clicking the item
			//-------------------------------------------------------------------
			String status = homePage.treeView.getItemStatus(dataPool.get("ItemToClick"));

			if (status.equalsIgnoreCase("OPEN") || status.equalsIgnoreCase("NO CHILD"))
				Log.pass("Test case Passed. On Clicking the item got expanded in navigation pane.");
			else
				Log.fail("Test case Failed. On Clicking the item does not got expanded in navigation pane. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_12_5

	/**
	 * SprintTest1725 : Verify if breadcrumb and top menu are hidden user are able to view tree view
	 */
	@Test(groups = {"Sprint56"}, description = "Verify if breadcrumb and top menu are hidden user are able to view tree view")
	public void SprintTest1725() throws Exception {

		driver = null;
		ConfigurationPage configurationPage = null;

		try {



			driver = WebDriverUtils.getDriver();

			configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation Pane Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			configurationPage.configurationPanel.setBreadCrumb(Caption.ConfigSettings.Config_Hide.Value);//Set the breadcrumb as 'Hide'
			configurationPage.configurationPanel.setTopMenu(Caption.ConfigSettings.Config_Hide.Value);//Set the breadcrumb as 'Hide'

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("2. Bread crumb and top menu is disabled & saved the configuration settings.");

			//Step-2 : Log out from configuration page
			//---------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			if(!homePage.menuBar.isBreadCrumbDisplayed() && homePage.menuBar.IsNewMenuDisplayed())
				throw new Exception("Bread crumb & operations menu is displayed as wrongly");

			//Verification : Verify if tree view is displayed or not
			//------------------------------------------------------
			if (homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				Log.pass("Test Case Passed. Tree view is displayed successfully when hiding the breadcrumb & top menu.", driver);	
			else
				Log.fail("Test Case Failed.Tree view is not displayed when hiding the breadcrumb & top menu", driver);

		}//End try

		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				try
				{
					Utility.logOut(driver);
					configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
					configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
					configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
					configurationPage.configurationPanel.setBreadCrumb(Caption.ConfigSettings.Config_Show.Value);//Set the breadcrumb as 'Hide'
					configurationPage.configurationPanel.setTopMenu(Caption.ConfigSettings.Config_Show.Value);//Set the breadcrumb as 'Hide'
					configurationPage.configurationPanel.saveSettings();
				}
				catch(Exception e1){
					Log.exception(e1, driver);
				}
			}
			Utility.quitDriver(driver);

		} //End finally

	} //End SprintTest1725


} //End Class NavigationPane
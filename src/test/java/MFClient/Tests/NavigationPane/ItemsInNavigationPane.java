package MFClient.Tests.NavigationPane;

import genericLibrary.DataProviderUtils;
import genericLibrary.EmailReport;
import genericLibrary.Log;
import genericLibrary.Utils;
import genericLibrary.WebDriverUtils;

import java.util.Arrays;
import java.util.HashMap;
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
public class ItemsInNavigationPane {

	public static String xlTestDataWorkBook = null;
	public static String loginURL = null;
	public static String configURL = null;
	public static String userName = null;
	public static String password = null;
	public static String testVault = null;
	public static String className = null;
	public static String productVersion = null;
	public static WebDriver driver = null;
	public static String driverType = null;

	/**
	 * init : Before Class method to perform initial operations.
	 */
	@BeforeClass
	public void init() throws Exception {

		try {

			XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			xlTestDataWorkBook = xmlParameters.getParameter("TestData");
			loginURL = xmlParameters.getParameter("webSite");
			configURL = xmlParameters.getParameter("ConfigurationURL");
			userName = xmlParameters.getParameter("UserName");
			password = xmlParameters.getParameter("Password");
			testVault = xmlParameters.getParameter("VaultName");
			className = this.getClass().getSimpleName().toString().trim();
			driverType = xmlParameters.getParameter("driverType");
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
	 * 56.1.1A.1 : Items in Home page should be available in Navigation pane in Default layout
	 */
	@Test(groups = {"Sprint56"}, description = "Items in Home page should be available in Navigation pane in default layout.")
	public void SprintTest56_1_1A_1() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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

			//Step-5 : Obtain the default items from Home list view and navigation pane
			//-------------------------------------------------------------------------
			String[] homeViewItems = homePage.listView.getAllItemNames(); //Gets the items from home view
			String[] homeNavigationPaneItems = homePage.treeView.getHomeTreeItems(); //Gets the items from navigation pane
			Arrays.sort(homeViewItems);
			Arrays.sort(homeNavigationPaneItems);

			Log.message("5. Default items from home list view and navigation pane is obtained.");

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (Arrays.equals(homeViewItems, homeNavigationPaneItems)) //Verifies if two arrays are same
				Log.pass("Test case Passed. Items in Home page are available in navigation pane.");
			else {
				String addlInfo = "Item displayed in Home View : " + homeViewItems.toString() + "\n Items displayed in Navigation Pane : " + homeNavigationPaneItems.toString(); 
				Log.fail("Test case Failed. Items that are available in home view is not availble in navigation pane. " + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1A_1

	/**
	 * 56.1.1A.2 : Items in Home page should be available in Navigation pane in Default with navigation pane layout
	 */
	@Test(groups = {"Sprint56"}, description = "Items in Home page should be available in Navigation pane in default with navigation pane layout.")
	public void SprintTest56_1_1A_2() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with navigation pane Layout' layout
			//----------------------------------------------------------------------
			configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view

			if (!configurationPage.configurationPanel.getPageName().equalsIgnoreCase(testVault))
				throw new Exception("Configuration is not navigated to vault specific settings page.");

			configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_DefaultAndNavigation.Value);

			if (!configurationPage.configurationPanel.saveSettings()) //Save the settings
				throw new Exception("Configuration settings are not saved after changing its layout.");

			Log.message("1. " + Caption.ConfigSettings.Config_DefaultAndNavigation.Value + " layout is selected and saved.");

			//Step-2 : Log out from configuration page
			//----------------------------------------
			if (!configurationPage.logOut())
				throw new Exception("Log out from configuration page is not successful.");

			Log.message("2. Logged out from configuration page.");

			//Step-3 : Login to MFiles web access
			//-----------------------------------
			HomePage homePage = LoginPage.launchDriverAndLogin(driver, true); //Launched driver and logged in

			Log.message("3. Logged into MFiles Web access client.");

			//Step-4 : Obtain the default items from Home list view and navigation pane
			//-------------------------------------------------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			String[] homeViewItems = homePage.listView.getAllItemNames(); //Gets the items from home view
			String[] homeNavigationPaneItems = homePage.treeView.getHomeTreeItems(); //Gets the items from navigation pane
			Arrays.sort(homeViewItems);
			Arrays.sort(homeNavigationPaneItems);

			Log.message("4. Default items from home list view and navigation pane is obtained.");

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (Arrays.equals(homeViewItems, homeNavigationPaneItems)) //Verifies if two arrays are same
				Log.pass("Test case Passed. Items in Home page are available in navigation pane.");
			else {
				String addlInfo = "Item displayed in Home View : " + homeViewItems.toString() + "\n Items displayed in Navigation Pane : " + homeNavigationPaneItems.toString(); 
				Log.fail("Test case Failed. Items that are available in home view is not availble in navigation pane. " + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1A_2

	/**
	 * 56.1.1A.3 : Items in Home page should be available in Navigation pane in No Java Applet layout
	 */
	@Test(groups = {"Sprint56"}, description = "Items in Home page should be available in Navigation pane in No Java Applet layout.")
	public void SprintTest56_1_1A_3() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//----------------------------------------------------------------------
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

			//Step-5 : Obtain the default items from Home list view and navigation pane
			//-------------------------------------------------------------------------
			String[] homeViewItems = homePage.listView.getAllItemNames(); //Gets the items from home view
			String[] homeNavigationPaneItems = homePage.treeView.getHomeTreeItems(); //Gets the items from navigation pane
			Arrays.sort(homeViewItems);
			Arrays.sort(homeNavigationPaneItems);

			Log.message("5. Default items from home list view and navigation pane is obtained.");

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (Arrays.equals(homeViewItems, homeNavigationPaneItems)) //Verifies if two arrays are same
				Log.pass("Test case Passed. Items in Home page are available in navigation pane.");
			else {
				String addlInfo = "Item displayed in Home View : " + homeViewItems.toString() + "\n Items displayed in Navigation Pane : " + homeNavigationPaneItems.toString(); 
				Log.fail("Test case Failed. Items that are available in home view is not availble in navigation pane. " + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1A_3

	/**
	 * 56.1.1A.4 : Items in Home page should be available in Navigation pane in No Java Applet, No Task Area layout
	 */
	@Test(groups = {"Sprint56"}, description = "Items in Home page should be available in Navigation pane in No Java Applet, No Task Area  layout.")
	public void SprintTest56_1_1A_4() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area  Layout' layout
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

			//Step-5 : Obtain the default items from Home list view and navigation pane
			//-------------------------------------------------------------------------
			String[] homeViewItems = homePage.listView.getAllItemNames(); //Gets the items from home view
			String[] homeNavigationPaneItems = homePage.treeView.getHomeTreeItems(); //Gets the items from navigation pane
			Arrays.sort(homeViewItems);
			Arrays.sort(homeNavigationPaneItems);

			Log.message("5. Default items from home list view and navigation pane is obtained.");

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (Arrays.equals(homeViewItems, homeNavigationPaneItems)) //Verifies if two arrays are same
				Log.pass("Test case Passed. Items in Home page are available in navigation pane.");
			else {
				String addlInfo = "Item displayed in Home View : " + homeViewItems.toString() + "\n Items displayed in Navigation Pane : " + homeNavigationPaneItems.toString(); 
				Log.fail("Test case Failed. Items that are available in home view is not availble in navigation pane. " + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1A_4

	/**
	 * 56.1.1A.5 : Items in Home page should be available in Navigation pane in No Java Applet, No Task Area but show GoTo shortcuts layout
	 */
	@Test(groups = {"Sprint56"}, description = "Items in Home page should be available in Navigation pane in No Java Applet, No Task Area but show GoTo shortcuts layout.")
	public void SprintTest56_1_1A_5() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet, No Task Area but show GoTo shortcuts Layout' layout
			//----------------------------------------------------------------------
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

			//Step-5 : Obtain the default items from Home list view and navigation pane
			//-------------------------------------------------------------------------
			String[] homeViewItems = homePage.listView.getAllItemNames(); //Gets the items from home view
			String[] homeNavigationPaneItems = homePage.treeView.getHomeTreeItems(); //Gets the items from navigation pane
			Arrays.sort(homeViewItems);
			Arrays.sort(homeNavigationPaneItems);

			Log.message("5. Default items from home list view and navigation pane is obtained.");

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (Arrays.equals(homeViewItems, homeNavigationPaneItems)) //Verifies if two arrays are same
				Log.pass("Test case Passed. Items in Home page are available in navigation pane.");
			else {
				String addlInfo = "Item displayed in Home View : " + homeViewItems.toString() + "\n Items displayed in Navigation Pane : " + homeNavigationPaneItems.toString(); 
				Log.fail("Test case Failed. Items that are available in home view is not availble in navigation pane. " + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1A_5

	/**
	 * 56.1.1B.1 : Vault name should be the root of the navigation pane in default layout
	 */
	@Test(groups = {"Sprint56"},	description = "Vault name should be the root of the navigation pane in default layout.")
	public void SprintTest56_1_1B_1() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

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

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (homePage.treeView.getRootNode().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Vault (" + testVault + ") is the root node of the tree view.");
			else
				Log.fail("Test case Failed. Vault (" + testVault + ") is not the root node of the tree view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1B_1

	/**
	 * 56.1.1B.2 : Vault name should be the root of the navigation pane in default with Navigation pane layout
	 */
	@Test(groups = {"Sprint56"},	description = "Vault name should be the root of the navigation pane in default with navigation pane layout.")
	public void SprintTest56_1_1B_2() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default Layout with navigation pane' layout
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

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			if (homePage.treeView.getRootNode().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Vault (" + testVault + ") is the root node of the tree view.");
			else
				Log.fail("Test case Failed. Vault (" + testVault + ") is not the root node of the tree view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1B_2

	/**
	 * 56.1.1B.3 : Vault name should be the root of the navigation pane in No Java Applet layout
	 */
	@Test(groups = {"Sprint56"},	description = "Vault name should be the root of the navigation pane in No Java Applet layout.")
	public void SprintTest56_1_1B_3() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout' layout
			//--------------------------------------------------------
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

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (homePage.treeView.getRootNode().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Vault (" + testVault + ") is the root node of the tree view.");
			else
				Log.fail("Test case Failed. Vault (" + testVault + ") is not the root node of the tree view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1B_3

	/**
	 * 56.1.1B.4 : Vault name should be the root of the navigation pane in No Java Applet; No Task Area layout
	 */
	@Test(groups = {"Sprint56"},	description = "Vault name should be the root of the navigation pane in No Java Applet; No Task Area layout.")
	public void SprintTest56_1_1B_4() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout; No Task area' layout
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

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (homePage.treeView.getRootNode().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Vault (" + testVault + ") is the root node of the tree view.");
			else
				Log.fail("Test case Failed. Vault (" + testVault + ") is not the root node of the tree view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1B_4

	/**
	 * 56.1.1B.5 : Vault name should be the root of the navigation pane in No Java Applet; No Task Area but show GoTo shortcuts layout
	 */
	@Test(groups = {"Sprint56"},	description = "Vault name should be the root of the navigation pane in No Java Applet; No Task Area but show GoTo shortcuts layout.")
	public void SprintTest56_1_1B_5() throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'No Java Applet Layout; No Task area but show GoTo shortcuts' layout
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

			//Verification : To Verify if all the default items are available in navigation pane
			//----------------------------------------------------------------------------------			
			if (homePage.treeView.getRootNode().equalsIgnoreCase(testVault))
				Log.pass("Test case Passed. Vault (" + testVault + ") is the root node of the tree view.");
			else
				Log.fail("Test case Failed. Vault (" + testVault + ") is not the root node of the tree view.", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_1B_5

	/**
	 * 56.1.5.1 : Navigated view should be available in breadcrumb in default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56","Bug"}, 
			description = "Clicking an item in tree view should expand the item in default layout")
	public void SprintTest56_1_5_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select the item to navigate
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Verification : To Verify if selected item gets displayed in breadcrumb
			//----------------------------------------------------------------------
			String expItem = dataPool.get("ItemToClick").split(">>")[dataPool.get("ItemToClick").split(">>").length - 1];

			if (homePage.menuBar.getBreadCrumbLastItem().equalsIgnoreCase(expItem))
				Log.pass("Test case Passed. Selected item in navigation pane is displayed in breadcrumb.");
			else
				Log.fail("Test case Failed. Selected item in navigation pane is not displayed in breadcrumb. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_5_1

	/**
	 * 56.1.5.2 : Navigated view should be available in breadcrumb in Default layout with navigation pane
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56","Bug"}, 
			description = "Clicking an item in tree view should expand the item in Default layout with navigation pane")
	public void SprintTest56_1_5_2(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Verification : To Verify if selected item gets displayed in breadcrumb
			//----------------------------------------------------------------------
			String expItem = dataPool.get("ItemToClick").split(">>")[dataPool.get("ItemToClick").split(">>").length - 1];

			if (homePage.menuBar.getBreadCrumbLastItem().equalsIgnoreCase(expItem))
				Log.pass("Test case Passed. Selected item in navigation pane is displayed in breadcrumb.");
			else
				Log.fail("Test case Failed. Selected item in navigation pane is not displayed in breadcrumb. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_5_2

	/**
	 * 56.1.5.3 : Navigated view should be available in breadcrumb in No Java Applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56","Bug"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet layout")
	public void SprintTest56_1_5_3(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select the item to navigate
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Verification : To Verify if selected item gets displayed in breadcrumb
			//----------------------------------------------------------------------
			String expItem = dataPool.get("ItemToClick").split(">>")[dataPool.get("ItemToClick").split(">>").length - 1];

			if (homePage.menuBar.getBreadCrumbLastItem().equalsIgnoreCase(expItem))
				Log.pass("Test case Passed. Selected item in navigation pane is displayed in breadcrumb.");
			else
				Log.fail("Test case Failed. Selected item in navigation pane is not displayed in breadcrumb. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_5_3

	/**
	 * 56.1.5.4 : Navigated view should be available in breadcrumb in No Java Applet no task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56","Bug"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet no task area layout")
	public void SprintTest56_1_5_4(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select the item to navigate
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Verification : To Verify if selected item gets displayed in breadcrumb
			//----------------------------------------------------------------------
			String expItem = dataPool.get("ItemToClick").split(">>")[dataPool.get("ItemToClick").split(">>").length - 1];

			if (homePage.menuBar.getBreadCrumbLastItem().equalsIgnoreCase(expItem))
				Log.pass("Test case Passed. Selected item in navigation pane is displayed in breadcrumb.");
			else
				Log.fail("Test case Failed. Selected item in navigation pane is not displayed in breadcrumb. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_5_4

	/**
	 * 56.1.5.5 : Navigated view should be available in breadcrumb in No Java Applet no task area but show GoTo shortcuts layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56","Bug"}, 
			description = "Clicking an item in tree view should expand the item in No Java Applet no task area but show GoTo shortcuts layout")
	public void SprintTest56_1_5_5(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-4 : Select the item to navigate
			//------------------------------------
			homePage.treeView.clickTreeViewItem(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Verification : To Verify if selected item gets displayed in breadcrumb
			//----------------------------------------------------------------------
			String expItem = dataPool.get("ItemToClick").split(">>")[dataPool.get("ItemToClick").split(">>").length - 1];

			if (homePage.menuBar.getBreadCrumbLastItem().equalsIgnoreCase(expItem))
				Log.pass("Test case Passed. Selected item in navigation pane is displayed in breadcrumb.");
			else
				Log.fail("Test case Failed. Selected item in navigation pane is not displayed in breadcrumb. ", driver);			
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_5_5

	/**
	 * 56.1.7.1 : Items in tree view and listing view should be same in Default layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Items in tree view and listing view should be same in Default layout")
	public void SprintTest56_1_7_1(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select the item to navigate
			//------------------------------------
			String[] treeviewItems = homePage.treeView.getChildItems(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Step-6 Obtain the items from list view
			//--------------------------------------
			String[] listViewItems = homePage.listView.getAllItemNames();
			Arrays.sort(treeviewItems); 
			Arrays.sort(listViewItems);

			Log.message("6. Items from listview are obtained.");

			//Verification : To Verify if items are same in treeview and listview
			//----------------------------------------------------------------------
			if (Arrays.equals(treeviewItems, listViewItems))
				Log.pass("Test case Passed. After selecting item in treeview, items are same in both treeview and listview.");
			else {
				String addlInfo = "Items in list view : " + listViewItems + "\nItems in Tree view : " + treeviewItems + ".";
				Log.fail("Test case Failed. After selecting item in treeview, items are not same in both treeview and listview. Refer additional information" + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_7_1

	/**
	 * 56.1.7.2 : Items in tree view and listing view should be same in Default with Navigation pane layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Items in tree view and listing view should be same in Default with Navigation pane layout")
	public void SprintTest56_1_7_2(HashMap<String,String> dataValues, String driverType) throws Exception {

		driver = null; 

		try {



			driver = WebDriverUtils.getDriver();

			ConcurrentHashMap <String, String> dataPool = new ConcurrentHashMap <String, String>(dataValues);

			ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);

			//Step-1 : Change layout to 'Default with Navigation pane Layout' layout
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

			//Step-4 : Select the item to navigate
			//------------------------------------
			if (!homePage.isTreeViewDisplayed()) //Checks if Navigation Pane is displayed
				throw new Exception("Navigation pane is not displayed after enabling it from display mode.");

			Utils.fluentWait(driver);
			String[] treeviewItems = homePage.treeView.getChildItems(dataPool.get("ItemToClick"));

			Log.message("4. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Step-5 Obtain the items from list view
			//--------------------------------------
			String[] listViewItems = homePage.listView.getAllItemNames();
			Arrays.sort(treeviewItems); 
			Arrays.sort(listViewItems);

			Log.message("5. Items from listview are obtained.");

			//Verification : To Verify if items are same in treeview and listview
			//----------------------------------------------------------------------
			if (Arrays.equals(treeviewItems, listViewItems))
				Log.pass("Test case Passed. After selecting item in treeview, items are same in both treeview and listview.");
			else {
				String addlInfo = "Items in list view : " + listViewItems + "\nItems in Tree view : " + treeviewItems + ".";
				Log.fail("Test case Failed. After selecting item in treeview, items are not same in both treeview and listview. Refer additional information" + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_7_2

	/**
	 * 56.1.7.3 : Items in tree view and listing view should be same in No Java Applet layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Items in tree view and listing view should be same in No Java Applet layout")
	public void SprintTest56_1_7_3(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select the item to navigate
			//------------------------------------
			String[] treeviewItems = homePage.treeView.getChildItems(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Step-6 Obtain the items from list view
			//--------------------------------------
			String[] listViewItems = homePage.listView.getAllItemNames();
			Arrays.sort(treeviewItems); 
			Arrays.sort(listViewItems);

			Log.message("6. Items from listview are obtained.");

			//Verification : To Verify if items are same in treeview and listview
			//----------------------------------------------------------------------
			if (Arrays.equals(treeviewItems, listViewItems))
				Log.pass("Test case Passed. After selecting item in treeview, items are same in both treeview and listview.");
			else {
				String addlInfo = "Items in list view : " + listViewItems + "\nItems in Tree view : " + treeviewItems + ".";
				Log.fail("Test case Failed. After selecting item in treeview, items are not same in both treeview and listview. Refer additional information" + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_7_3

	/**
	 * 56.1.7.4 : Items in tree view and listing view should be same in No Java Applet no task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Items in tree view and listing view should be same in No Java Applet no task area layout")
	public void SprintTest56_1_7_4(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select the item to navigate
			//------------------------------------
			String[] treeviewItems = homePage.treeView.getChildItems(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Step-6 Obtain the items from list view
			//--------------------------------------
			String[] listViewItems = homePage.listView.getAllItemNames();
			Arrays.sort(treeviewItems); 
			Arrays.sort(listViewItems);

			Log.message("6. Items from listview are obtained.");

			//Verification : To Verify if items are same in treeview and listview
			//----------------------------------------------------------------------
			if (Arrays.equals(treeviewItems, listViewItems))
				Log.pass("Test case Passed. After selecting item in treeview, items are same in both treeview and listview.");
			else {
				String addlInfo = "Items in list view : " + listViewItems + "\nItems in Tree view : " + treeviewItems + ".";
				Log.fail("Test case Failed. After selecting item in treeview, items are not same in both treeview and listview. Refer additional information" + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_7_4

	/**
	 * 56.1.7.5 : Items in tree view and listing view should be same in No Java Applet no task area layout
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "excelDataReader", groups = {"Sprint56"}, 
			description = "Items in tree view and listing view should be same in No Java Applet no task area layout")
	public void SprintTest56_1_7_5(HashMap<String,String> dataValues, String driverType) throws Exception {

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

			//Step-5 : Select the item to navigate
			//------------------------------------
			String[] treeviewItems = homePage.treeView.getChildItems(dataPool.get("ItemToClick"));

			Log.message("5. Item (" + dataPool.get("ItemToClick") + ") from Navigation pane is selected.");

			//Step-6 Obtain the items from list view
			//--------------------------------------
			String[] listViewItems = homePage.listView.getAllItemNames();
			Arrays.sort(treeviewItems); 
			Arrays.sort(listViewItems);

			Log.message("6. Items from listview are obtained.");

			//Verification : To Verify if items are same in treeview and listview
			//----------------------------------------------------------------------
			if (Arrays.equals(treeviewItems, listViewItems))
				Log.pass("Test case Passed. After selecting item in treeview, items are same in both treeview and listview.");
			else {
				String addlInfo = "Items in list view : " + listViewItems + "\nItems in Tree view : " + treeviewItems + ".";
				Log.fail("Test case Failed. After selecting item in treeview, items are not same in both treeview and listview. Refer additional information" + addlInfo, driver);
			}
		}
		catch (Exception e) {
			Log.exception(e, driver);
		} //End catch

		finally {
			if (driver != null)
			{
				Utility.logOut(driver);
				ConfigurationPage configurationPage = LoginPage.launchDriverAndLoginToConfig(driver, false);
				configurationPage.treeView.clickTreeViewItem(Caption.ConfigSettings.VaultSpecificSettings.Value + ">>" + testVault); //Selects test vault from the tree view
				configurationPage.configurationPanel.setLayout(Caption.ConfigSettings.Config_Default.Value);	
				configurationPage.configurationPanel.saveSettings();
			}
			Utility.quitDriver(driver);
		} //End finally

	} //End SprintTest56_1_7_5



} //End Class NavigationPane
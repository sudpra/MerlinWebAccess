package genericLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

public class DataProviderUtils {

	static List<String> drivers;

	/**
	 * excelDataReader : Reads data from text file using dataprovider
	 * @param context Current testng XML context
	 * @return iterator of testdata and driver
	 * @throws Exception
	 */
	@DataProvider
	public static Iterator <Object[]> fileDataProvider(ITestContext context) throws IOException {

		String strBasePath = null;
		String inputFile = null;
		File dir1 = new File(".");
		strBasePath = dir1.getCanonicalPath();
		drivers = Arrays.asList(context.getCurrentXmlTest().getParameter("driverType").split(","));

		inputFile = strBasePath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ context.getCurrentXmlTest().getParameter("dataProviderPath");

		// Get a list of String file content (line items) from the test file.
		List <String> testData = getFileContentList(inputFile);

		// We will be returning an iterator of Object arrays so create that first.
		List <Object[]> dataToBeReturned = new ArrayList <Object[]>();

		// Populate our List of Object arrays with the file content.
		for (String userData : testData) {
			for (String currentDriver : drivers) {	
				dataToBeReturned.add(new Object[] { userData,currentDriver });
			}
		}

		// return the iterator - testng will initialize the test class and calls the
		// test method with each of the content of this iterator.
		return dataToBeReturned.iterator();//Set of TestData for specific Test

	}

	/**
	 * excelDataReader : Reads data from excel using dataprovider
	 * @param testName Name of the test method
	 * @param context Current testng XML context
	 * @return iterator of testdata and driver
	 * @throws Exception
	 */
	@DataProvider
	public static Iterator <Object[]> excelDataReader(Method testName, ITestContext context) throws Exception {

		String sheetName = "";

		try {

			List<String> webdrivers = Arrays.asList(context.getCurrentXmlTest().getParameter("driverType").split(","));
			//String workBookName = testName.getDeclaringClass().getDeclaredField("xlTestDataWorkBook").get(null).toString();
			String workBookName = context.getCurrentXmlTest().getParameter("TestData");
			sheetName = testName.getName();

			TestDataExtractor testData = new TestDataExtractor();
			testData.setWorkBookName(workBookName);
			testData.setWorkSheet(sheetName);

			List <HashMap <String, String>> inputDataValues = testData.readAllData(); //Get a list of String file content (line items) from the test file.
			List <Object[]> dataToBeReturned = new ArrayList <Object[]>(); // We will be returning an iterator of Object arrays so create that first.

			for(int i=0; i<inputDataValues.size(); i++) // Populate our List of Object arrays with the test data file content.
				for (String currentDriver : webdrivers) //Iterate for each driver type					
					dataToBeReturned.add(new Object[] { inputDataValues.get(i), currentDriver.trim() });

			return dataToBeReturned.iterator(); // return the iterator - testng will initialize the test class and calls the test method with each of the content of this iterator.

		} //End try
		catch (Exception e) {
			if (e.getClass().toString().contains("NullPointerException")) 
				throw new SkipException("Test data sheet '" + sheetName + "' does not exists.");
			else
				throw e;
		} //End catch

	} //End excelDataReader

	/**
	 * getFileContentList : Utility method to get the file content in UTF8
	 * @param filenamePath Path of the file
	 * @return lines in the file name path
	 */
	private static List <String> getFileContentList(String filenamePath) {
		List <String> lines = new ArrayList <String>();
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filenamePath), "UTF8"));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				lines.add(strLine);
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return lines;//returns the testdata file contents
	}

} //End class DataProviderUtils

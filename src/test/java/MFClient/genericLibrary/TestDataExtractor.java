package genericLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFBorderFormatting;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.testng.Assert;

public class TestDataExtractor {

	private String workBookName;
	private String workSheet;
	private String testCaseId;
	private HashMap <String, String> data;
	private int currentRowPtr = 0;

	public TestDataExtractor() {
	}

	public TestDataExtractor(String xlWorkBook, String xlWorkSheet) {
		this.workBookName = xlWorkBook;
		this.workSheet = xlWorkSheet;
	}

	public TestDataExtractor(String xlWorkBook, String xlWorkSheet, String tcID) {
		this.workBookName = xlWorkBook;
		this.workSheet = xlWorkSheet;
		this.testCaseId = tcID;
	}

	public String getWorkBookName() {
		return workBookName;
	}

	public void setWorkBookName(String workBookName) {
		this.workBookName = workBookName;
	}

	public String getWorkSheet() {
		return workSheet;
	}

	public void setWorkSheet(String workSheet) {
		this.workSheet = workSheet;
	}

	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String get(String key) {

		if (data.isEmpty())
			readData();
		return data.get(key);

	}

	private Hashtable <String, Integer> excelHeaders = new Hashtable <String, Integer>();
	private Hashtable <String, Integer> excelrRowColumnCount = new Hashtable <String, Integer>();

	/**
	 * @Function:readFromExcel
	 * @Description:Fetch Data from Excel
	 * @return
	 */
	public HashMap <String, String> readData() {

		HashMap <String, String> testData = new HashMap <String, String>();
		genericLibrary.ReadFromExcel readTestData = new genericLibrary.ReadFromExcel();
		boolean isDataFound = false;
		testCaseId = testCaseId != null ? testCaseId.trim() : "";
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;

		try {

			sheet = readTestData.initiateExcelConnection(workSheet, workBookName); // to initiate a connection to an excel sheet
			excelrRowColumnCount = readTestData.findRowColumnCount(sheet, excelrRowColumnCount); // find number of rows and columns
			excelHeaders = readTestData.readExcelHeaders(sheet, excelHeaders, excelrRowColumnCount); // to find excel header fields

			for (int r = 0; r < excelrRowColumnCount.get("RowCount"); r++) {

				row = sheet.getRow(r);
				if (row == null)
					continue;

				for (int c = 0; c < excelrRowColumnCount.get("ColumnCount"); c++) {

					if (row.getCell(excelHeaders.get("TestID")) == null)
						break;

					cell = row.getCell(excelHeaders.get("TestID"));

					if (!readTestData.convertHSSFCellToString(cell).toString().equalsIgnoreCase(testCaseId))
						continue;

					isDataFound = true;

					for (String key : excelHeaders.keySet()) {
						testData.put(key, readTestData.convertHSSFCellToString(row.getCell(excelHeaders.get(key))));
					}

					break;

				}

				if (isDataFound)
					break;

			}

			if (!isDataFound)
				Assert.fail("\nTest Data not found in test data sheet for Test Case Id  : " + testCaseId);

		}
		catch (RuntimeException e) {
			Assert.fail("Error During Execution; Execution Failed More details " + e);
			e.printStackTrace();
		}

		data = testData;
		return testData;
	}

	public List <HashMap <String, String>> readAllData() {

		List <HashMap <String, String>> dataList = new ArrayList <HashMap <String, String>>();
		genericLibrary.ReadFromExcel readTestData = new genericLibrary.ReadFromExcel();
		HSSFSheet sheet = null;
		HSSFRow row = null;

		try {

			sheet = readTestData.initiateExcelConnection(workSheet, workBookName); // to initiate a connection to an excel sheet
			excelrRowColumnCount = readTestData.findRowColumnCount(sheet, excelrRowColumnCount); // find number of rows and columns
			excelHeaders = readTestData.readExcelHeaders(sheet, excelHeaders, excelrRowColumnCount); // to find excel header fields

			for (int r = 1; r < excelrRowColumnCount.get("RowCount"); r++) {

				row = sheet.getRow(r);
				if (row == null)
					continue;
				HashMap <String, String> testData = new HashMap <String, String>();
				for (int c = 0; c < excelrRowColumnCount.get("ColumnCount"); c++) {

					for (String key : excelHeaders.keySet()) {
						testData.put(key, readTestData.convertHSSFCellToString(row.getCell(excelHeaders.get(key))));
					}

				}

				dataList.add(testData);
			}
		}
		catch (RuntimeException e) {
			throw e;
		}

		return dataList;
	}

	@SuppressWarnings("finally")
	public Boolean EOD() {

		//Variable Declaration
		Boolean isEOD = true;
		ReadFromExcel readTestData = new ReadFromExcel();
		HSSFSheet sheet = null;

		try {

			sheet = readTestData.initiateExcelConnection(workSheet, workBookName); // to initiate a connection to an excel sheet
			excelrRowColumnCount = readTestData.findRowColumnCount(sheet, excelrRowColumnCount); // find number of rows and columns

			if (excelrRowColumnCount.get("RowCount") == this.currentRowPtr + 1)
				isEOD = true;
			else
				isEOD = false;

		} //End try

		catch (RuntimeException e) {

		} //End catch

		finally {
			return isEOD;
		} //End finally

	} //End EOD

	/**
	 * @Function:readFromExcel
	 * @Description:Fetch Data from Excel
	 * @return
	 */
	public HashMap <String, String> readTestData() {

		HashMap <String, String> testData = new HashMap <String, String>();
		genericLibrary.ReadFromExcel readTestData = new genericLibrary.ReadFromExcel();
		boolean isDataFound = false;
		testCaseId = testCaseId != null ? testCaseId.trim() : "";
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;

		try {

			sheet = readTestData.initiateExcelConnection(workSheet, workBookName); // to initiate a connection to an excel sheet
			excelrRowColumnCount = readTestData.findRowColumnCount(sheet, excelrRowColumnCount); // find number of rows and columns
			excelHeaders = readTestData.readExcelHeaders(sheet, excelHeaders, excelrRowColumnCount); // to find excel header fields

			for (int r = 0; r < excelrRowColumnCount.get("RowCount"); r++) {

				row = sheet.getRow(r);
				if (row == null)
					continue;

				for (int c = 0; c < excelrRowColumnCount.get("ColumnCount"); c++) {

					if (row.getCell(excelHeaders.get("TestID")) == null)
						break;

					cell = row.getCell(excelHeaders.get("TestID"));

					if (!readTestData.convertHSSFCellToString(cell).toString().equalsIgnoreCase(testCaseId))
						continue;

					isDataFound = true;

					for (String key : excelHeaders.keySet()) {
						testData.put(key, readTestData.convertHSSFCellToString(row.getCell(excelHeaders.get(key))));
					}

					break;

				}

				if (isDataFound)
					break;

			}

			if (!isDataFound)
				Assert.fail("\nTest Data not found in test data sheet for Test Case Id  : " + testCaseId);

		}
		catch (RuntimeException e) {
			Assert.fail("Error During Execution; Execution Failed More details " + e);
			e.printStackTrace();
		}

		data = testData;
		return testData;
	}

	/**
	 * writeData : Writes data to excel sheet
	 * @param outdir Output directory to store the sheet
	 * @param writeData Data to be writte in excel sheet
	 * @return None
	 */
	public void writeData(String outdir, String writeData) {

		try {

			FileOutputStream fileOut = new FileOutputStream(outdir + File.separator + "BugReportSheet.xls");
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet();
			writeData = writeData.replace("|", "\n");
			String[] rows = writeData.split("\n");
			int rowCt = rows.length;

			worksheet.setDisplayGridlines(false);
			worksheet.autoSizeColumn(2);

			HSSFRow rowHeader = worksheet.createRow(0);
			HSSFCell cellA1 = rowHeader.createCell(0);
			cellA1.setCellValue("Test Plan");


			HSSFCell cellB1 = rowHeader.createCell(1);
			cellB1.setCellValue("Test case ID");

			HSSFCell cellC1 = rowHeader.createCell(2);
			cellC1.setCellValue("Environment");

			HSSFCell cellD1 = rowHeader.createCell(3);
			cellD1.setCellValue("Automation Results");			

			HSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFillBackgroundColor(HSSFColor.GREY_80_PERCENT.index);
			cellStyle.setBorderBottom(HSSFBorderFormatting.BORDER_THIN);
			cellStyle.setBorderTop(HSSFBorderFormatting.BORDER_THIN);
			cellStyle.setBorderLeft(HSSFBorderFormatting.BORDER_THIN);
			cellStyle.setBorderRight(HSSFBorderFormatting.BORDER_THIN);
			cellStyle.setWrapText(true);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			HSSFFont font= workbook.createFont();
			font.setFontHeightInPoints((short)12);
			font.setFontName("Calibri");
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setItalic(false);
			cellStyle.setFont(font);
			cellA1.setCellStyle(cellStyle);
			cellB1.setCellStyle(cellStyle);
			cellC1.setCellStyle(cellStyle);
			cellD1.setCellStyle(cellStyle);

			HSSFCellStyle cellStyle2 = workbook.createCellStyle();
			cellStyle2.cloneStyleFrom(cellStyle);
			cellStyle2.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			cellStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

			HSSFFont font2 = workbook.createFont();
			font2.setFontHeightInPoints((short)10);
			font2.setFontName("Calibri");
			font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			font2.setItalic(false);
			cellStyle2.setFont(font2);

			for (int i=0; i<rowCt; i++) {

				String[] colnVal = rows[i].split("<>");

				HSSFRow rowIndex = worksheet.createRow(i + 1);
				HSSFCell cellAIndex = rowIndex.createCell(0);
				cellAIndex.setCellValue(colnVal[0]);
				cellAIndex.setCellStyle(cellStyle2);

				HSSFCell cellBIndex = rowIndex.createCell(1);
				cellBIndex.setCellValue(colnVal[1]);
				cellBIndex.setCellStyle(cellStyle2);

				HSSFCell cellCIndex = rowIndex.createCell(2);
				cellCIndex.setCellValue("");
				cellCIndex.setCellStyle(cellStyle2);

				HSSFCell cellDIndex = rowIndex.createCell(3);
				cellDIndex.setCellValue(colnVal[2]);
				cellDIndex.setCellStyle(cellStyle2);

			}

			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}

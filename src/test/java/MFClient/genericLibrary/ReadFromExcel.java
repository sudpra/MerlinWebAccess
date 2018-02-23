package genericLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public class ReadFromExcel {
	/**
	 * @Function initiateExcelConnection
	 * @Description function to establish an initial connection with a work sheet
	 * @param workSheetName
	 *            (String)
	 * @param sectionName
	 *            (String)
	 * @param workBookName
	 *            (String)
	 * @return HSSFSheet (Work sheet)
	 * @Created
	 */
	public HSSFSheet initiateExcelConnection(String workSheet, String workBookName) {

		HSSFSheet sheet = null;

		try {
			
			String file = ".\\src\\test\\resources\\" + workBookName;
			file = file.replace("\\", File.separator);
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			sheet = wb.getSheet(workSheet);

		}

		catch (RuntimeException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		return sheet;
	}

	/**
	 * @Function findRowColumnCount
	 * @Description function to establish an initial connection with a work sheet
	 * @param HSSFSheet
	 * @param rowColumnCount
	 *            (Hashtable)
	 * @return Hashtable (returns row count and column count)
	 * @Created
	 */
	public Hashtable <String, Integer> findRowColumnCount(HSSFSheet sheet, Hashtable <String, Integer> rowColumnCount) {

		HSSFRow row = null;
		String temp = null;

		int rows = sheet.getPhysicalNumberOfRows();
		int cols = 0;
		int tmp = 0;
		int counter = 0;

		for (int i = 0; i < 10 || i < rows; i++) {

			row = sheet.getRow(i);

			if (row == null)
				continue;

			temp = convertHSSFCellToString(row.getCell(0));

			if (!temp.equals(""))
				counter++;

			tmp = sheet.getRow(i).getPhysicalNumberOfCells();
			if (tmp > cols)
				cols = tmp;

		}

		rowColumnCount.put("RowCount", counter);
		rowColumnCount.put("ColumnCount", cols);
		return rowColumnCount;

	}

	/**
	 * @Function readExcelHeaders
	 * @Description function to establish an initial connection with a work sheet
	 * @param HSSFSheet
	 * @param excelHeaders
	 *            (Hashtable)
	 * @param rowColumnCount
	 *            (Hashtable)
	 * @return Hashtable (Having Header column values)
	 * @Created
	 */
	public Hashtable <String, Integer> readExcelHeaders(HSSFSheet sheet, Hashtable <String, Integer> excelHeaders, Hashtable <String, Integer> rowColumnCount) {

		HSSFRow row = null;
		HSSFCell cell = null;

		for (int r = 0; r < rowColumnCount.get("RowCount"); r++) {

			row = sheet.getRow(r);

			if (row == null)
				continue;

			for (int c = 0; c < rowColumnCount.get("ColumnCount"); c++) {

				cell = row.getCell(c);
				if (cell != null)
					excelHeaders.put(cell.toString(), c);
			}

			break;
		}

		return excelHeaders;
	}

	/**
	 * @Function convertHSSFCellToString
	 * @Description function will convert the HSSFCell type value to its equivalent string value
	 * @param cell
	 *            : HSSFCell value
	 * @return String
	 * @Created
	 */
	public String convertHSSFCellToString(HSSFCell cell) {

		String cellValue = "";

		if (cell != null)
			cellValue = cell.toString().trim();

		return cellValue;

	}

	public String evaluateAndReturnCellValue(HSSFSheet sheet, String cellRange) {

		String val = "";
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		CellReference ref = new CellReference(cellRange);
		HSSFRow row = sheet.getRow(ref.getRow());

		if (row == null)
			return val;

		HSSFCell cell = row.getCell((int) ref.getCol());
		CellValue cellValue = evaluator.evaluate(cell);
		return cellValue.getStringValue();

	}

	public void setCellValue(HSSFSheet sheet, String cellRange, String value) {

		CellReference ref = new CellReference(cellRange);
		HSSFRow row = sheet.getRow(ref.getRow());
		HSSFCell cell = row.getCell((int) ref.getCol());
		cell.setCellValue(value);

	}

}

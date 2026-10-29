package com.automation.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelUtility {
    private static final String UI_RESOURCE = "/testdata/UiTestData.xlsx";
    private static final String SHEET_NAME = "TestData";
    private static final DataFormatter FORMATTER = new DataFormatter();

    private ExcelUtility() {
    }

    public static String getValue(String rowKey, String columnName) {
        return getValue(UI_RESOURCE, rowKey, columnName);
    }

    public static String getValue(String resource, String rowKey, String columnName) {
        String value = readValue(resource, rowKey, columnName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Excel test data '" + rowKey + "' has no value for column '" + columnName + "'.");
        }
        return value;
    }

    public static String getOptionalValue(String rowKey, String columnName) {
        return getOptionalValue(UI_RESOURCE, rowKey, columnName);
    }

    public static String getOptionalValue(String resource, String rowKey, String columnName) {
        return readValue(resource, rowKey, columnName);
    }

    private static String readValue(String resource, String rowKey, String columnName) {
        try (InputStream stream = ExcelUtility.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Excel test data workbook was not found: " + resource);
            }
            try (Workbook workbook = WorkbookFactory.create(stream)) {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    throw new IllegalStateException("Excel workbook is missing the '" + SHEET_NAME + "' sheet.");
                }
                Row headers = sheet.getRow(0);
                int columnIndex = findColumn(headers, columnName);
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null && rowKey.equals(cellValue(row.getCell(0)))) {
                        return cellValue(row.getCell(columnIndex));
                    }
                }
                throw new IllegalArgumentException("Excel test data row '" + rowKey + "' was not found.");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Excel test data workbook: " + resource, exception);
        }
    }

    private static int findColumn(Row headers, String columnName) {
        if (headers == null) {
            throw new IllegalStateException("Excel test data sheet has no header row.");
        }
        for (int column = 0; column < headers.getLastCellNum(); column++) {
            if (columnName.equalsIgnoreCase(cellValue(headers.getCell(column)))) {
                return column;
            }
        }
        throw new IllegalArgumentException("Excel test data column '" + columnName + "' was not found.");
    }

    private static String cellValue(org.apache.poi.ss.usermodel.Cell cell) {
        return cell == null ? "" : FORMATTER.formatCellValue(cell).trim();
    }
}

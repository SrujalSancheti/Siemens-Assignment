package com.automation.api.util;

import com.automation.util.ExcelUtility;

public final class ApiExcelUtility {
    private static final String RESOURCE = "/testdata/ApiTestData.xlsx";

    private ApiExcelUtility() {
    }

    public static String getValue(String rowKey, String columnName) {
        return ExcelUtility.getValue(RESOURCE, rowKey, columnName);
    }

    public static String getOptionalValue(String rowKey, String columnName) {
        return ExcelUtility.getOptionalValue(RESOURCE, rowKey, columnName);
    }
}

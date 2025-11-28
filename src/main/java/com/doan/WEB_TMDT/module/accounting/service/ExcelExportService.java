package com.doan.WEB_TMDT.module.accounting.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] exportFinancialReport(List<Map<String, Object>> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo tài chính");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Mã đơn/Kỳ", "Ngày", "Doanh thu", "VAT", "Giá vốn", 
                "Phí vận chuyển", "Phí cổng TT", "Lợi nhuận gộp", 
                "Thuế TNDN", "Lợi nhuận ròng", "Thực nhận"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data
            int rowNum = 1;
            for (Map<String, Object> record : data) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, getStringValue(record, "orderId", "period"), null);
                createCell(row, 1, getStringValue(record, "date", ""), null);
                createCell(row, 2, getNumberValue(record, "revenue"), numberStyle);
                createCell(row, 3, getNumberValue(record, "vat"), numberStyle);
                createCell(row, 4, getNumberValue(record, "costOfGoods"), numberStyle);
                createCell(row, 5, getNumberValue(record, "shippingCost"), numberStyle);
                createCell(row, 6, getNumberValue(record, "paymentGatewayCost"), numberStyle);
                createCell(row, 7, getNumberValue(record, "grossProfit"), numberStyle);
                createCell(row, 8, getNumberValue(record, "corporateTax"), numberStyle);
                createCell(row, 9, getNumberValue(record, "netProfit"), numberStyle);
                createCell(row, 10, getNumberValue(record, "actualReceived"), numberStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
    
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
    
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return "";
    }
    
    private Number getNumberValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return (Number) value;
        }
        return 0;
    }
}

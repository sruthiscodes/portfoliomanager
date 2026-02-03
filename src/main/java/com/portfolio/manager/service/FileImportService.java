package com.portfolio.manager.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.dto.ImportResultDTO;
import com.portfolio.manager.model.AssetType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileImportService {

    private final AssetService assetService;

    public ImportResultDTO importFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is required");
        }

        if (filename.toLowerCase().endsWith(".xlsx")) {
            return importExcel(file.getInputStream());
        } else if (filename.toLowerCase().endsWith(".csv")) {
            return importCSV(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload .xlsx or .csv file");
        }
    }

    private ImportResultDTO importExcel(InputStream inputStream) throws IOException {
        ImportResultDTO result = ImportResultDTO.builder()
                .totalRows(0)
                .successCount(0)
                .failureCount(0)
                .errors(new ArrayList<>())
                .importedAssets(new ArrayList<>())
                .build();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1; // Start from 1 since we skipped header
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    AssetDTO assetDTO = parseExcelRow(row, rowNum);
                    AssetDTO saved = assetService.createAsset(assetDTO);
                    result.getImportedAssets().add(saved);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    log.info("Imported asset from row {}: {}", rowNum, saved.getSymbol());
                } catch (Exception e) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String error = String.format("Row %d: %s", rowNum, e.getMessage());
                    result.getErrors().add(error);
                    log.warn("Failed to import row {}: {}", rowNum, e.getMessage());
                }
            }
        }

        return result;
    }

    private ImportResultDTO importCSV(InputStream inputStream) throws IOException {
        ImportResultDTO result = ImportResultDTO.builder()
                .totalRows(0)
                .successCount(0)
                .failureCount(0)
                .errors(new ArrayList<>())
                .importedAssets(new ArrayList<>())
                .build();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            int rowNum = 1; // Start from 1 since header is row 0
            for (CSVRecord record : csvParser) {
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    AssetDTO assetDTO = parseCSVRecord(record, rowNum);
                    AssetDTO saved = assetService.createAsset(assetDTO);
                    result.getImportedAssets().add(saved);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    log.info("Imported asset from row {}: {}", rowNum, saved.getSymbol());
                } catch (Exception e) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String error = String.format("Row %d: %s", rowNum, e.getMessage());
                    result.getErrors().add(error);
                    log.warn("Failed to import row {}: {}", rowNum, e.getMessage());
                }
            }
        }

        return result;
    }

    private AssetDTO parseExcelRow(Row row, int rowNum) {
        try {
            String symbol = getCellValue(row.getCell(0));
            String name = getCellValue(row.getCell(1));
            String assetTypeStr = getCellValue(row.getCell(2));
            String quantityStr = getCellValue(row.getCell(3));
            String avgBuyPriceStr = getCellValue(row.getCell(4));
            String currentPriceStr = getCellValue(row.getCell(5));

            return buildAssetDTO(symbol, name, assetTypeStr, quantityStr, avgBuyPriceStr, currentPriceStr, rowNum);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid data format: " + e.getMessage());
        }
    }

    private AssetDTO parseCSVRecord(CSVRecord record, int rowNum) {
        try {
            String symbol = record.get(0).trim();
            String name = record.get(1).trim();
            String assetTypeStr = record.get(2).trim();
            String quantityStr = record.get(3).trim();
            String avgBuyPriceStr = record.get(4).trim();
            String currentPriceStr = record.size() > 5 ? record.get(5).trim() : null;

            return buildAssetDTO(symbol, name, assetTypeStr, quantityStr, avgBuyPriceStr, currentPriceStr, rowNum);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid data format: " + e.getMessage());
        }
    }

    private AssetDTO buildAssetDTO(String symbol, String name, String assetTypeStr, 
                                   String quantityStr, String avgBuyPriceStr, 
                                   String currentPriceStr, int rowNum) {
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (assetTypeStr == null || assetTypeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset type is required");
        }

        AssetType assetType;
        try {
            assetType = AssetType.valueOf(assetTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid asset type: " + assetTypeStr + 
                ". Valid types are: STOCK, BOND, ETF, CRYPTO, CASH");
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(quantityStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid quantity: " + quantityStr);
        }

        BigDecimal avgBuyPrice;
        try {
            avgBuyPrice = new BigDecimal(avgBuyPriceStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid average buy price: " + avgBuyPriceStr);
        }

        BigDecimal currentPrice = null;
        if (currentPriceStr != null && !currentPriceStr.trim().isEmpty()) {
            try {
                currentPrice = new BigDecimal(currentPriceStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid current price: " + currentPriceStr);
            }
        }

        return AssetDTO.builder()
                .symbol(symbol.trim())
                .name(name.trim())
                .assetType(assetType)
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .currentPrice(currentPrice)
                .build();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                // If it's a whole number, return without decimal
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return null;
        }
    }
}

package com.portfolio.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.manager.dto.AssetDTO;
import com.portfolio.manager.dto.ImportResultDTO;
import com.portfolio.manager.model.AssetType;

@ExtendWith(MockitoExtension.class)
class FileImportServiceTest {

    @Mock
    private AssetService assetService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileImportService fileImportService;

    @Test
    void importExcelFile_success() throws IOException {
        // Create Excel file with test data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Portfolio");
            
            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Symbol");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Quantity");
            header.createCell(4).setCellValue("Avg Buy Price");
            header.createCell(5).setCellValue("Current Price");
            
            // Data row
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("AAPL");
            data.createCell(1).setCellValue("Apple Inc");
            data.createCell(2).setCellValue("STOCK");
            data.createCell(3).setCellValue(10.5);
            data.createCell(4).setCellValue(150.25);
            data.createCell(5).setCellValue(175.50);
            
            workbook.write(baos);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        when(multipartFile.getOriginalFilename()).thenReturn("portfolio.xlsx");
        when(multipartFile.getInputStream()).thenReturn(bais);

        AssetDTO mockSavedAsset = AssetDTO.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.5"))
                .avgBuyPrice(new BigDecimal("150.25"))
                .currentPrice(new BigDecimal("175.50"))
                .build();

        when(assetService.createAsset(any(AssetDTO.class))).thenReturn(mockSavedAsset);

        // Execute
        ImportResultDTO result = fileImportService.importFile(multipartFile);

        // Verify
        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImportedAssets()).hasSize(1);
        assertThat(result.getImportedAssets().get(0).getSymbol()).isEqualTo("AAPL");
        
        verify(assetService, times(1)).createAsset(any(AssetDTO.class));
    }

    @Test
    void importCSVFile_success() throws IOException {
        // Create CSV file with test data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new java.io.OutputStreamWriter(baos), CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("Symbol", "Name", "Type", "Quantity", "Avg Buy Price", "Current Price");
            csvPrinter.printRecord("GOOGL", "Alphabet Inc", "STOCK", "5.25", "2800.50", "2950.75");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        when(multipartFile.getOriginalFilename()).thenReturn("portfolio.csv");
        when(multipartFile.getInputStream()).thenReturn(bais);

        AssetDTO mockSavedAsset = AssetDTO.builder()
                .id(1L)
                .symbol("GOOGL")
                .name("Alphabet Inc")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("5.25"))
                .avgBuyPrice(new BigDecimal("2800.50"))
                .currentPrice(new BigDecimal("2950.75"))
                .build();

        when(assetService.createAsset(any(AssetDTO.class))).thenReturn(mockSavedAsset);

        // Execute
        ImportResultDTO result = fileImportService.importFile(multipartFile);

        // Verify
        assertThat(result.getTotalRows()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImportedAssets()).hasSize(1);
        
        verify(assetService, times(1)).createAsset(any(AssetDTO.class));
    }

    @Test
    void importFile_withPartialFailures() throws IOException {
        // Create Excel file with valid and invalid data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Portfolio");
            
            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Symbol");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Quantity");
            header.createCell(4).setCellValue("Avg Buy Price");
            header.createCell(5).setCellValue("Current Price");
            
            // Valid data row
            Row data1 = sheet.createRow(1);
            data1.createCell(0).setCellValue("AAPL");
            data1.createCell(1).setCellValue("Apple Inc");
            data1.createCell(2).setCellValue("STOCK");
            data1.createCell(3).setCellValue(10.5);
            data1.createCell(4).setCellValue(150.25);
            data1.createCell(5).setCellValue(175.50);
            
            // Invalid data row (missing symbol)
            Row data2 = sheet.createRow(2);
            data2.createCell(0).setCellValue("");
            data2.createCell(1).setCellValue("Test");
            data2.createCell(2).setCellValue("STOCK");
            data2.createCell(3).setCellValue(5);
            data2.createCell(4).setCellValue(100);
            
            workbook.write(baos);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        when(multipartFile.getOriginalFilename()).thenReturn("portfolio.xlsx");
        when(multipartFile.getInputStream()).thenReturn(bais);

        AssetDTO mockSavedAsset = AssetDTO.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc")
                .assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10.5"))
                .avgBuyPrice(new BigDecimal("150.25"))
                .currentPrice(new BigDecimal("175.50"))
                .build();

        when(assetService.createAsset(any(AssetDTO.class))).thenReturn(mockSavedAsset);

        // Execute
        ImportResultDTO result = fileImportService.importFile(multipartFile);

        // Verify
        assertThat(result.getTotalRows()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Row 3").contains("Symbol is required");
        assertThat(result.getImportedAssets()).hasSize(1);
    }

    @Test
    void importFile_unsupportedFormat() {
        when(multipartFile.getOriginalFilename()).thenReturn("portfolio.pdf");

        assertThatThrownBy(() -> fileImportService.importFile(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file format");
    }

    @Test
    void importFile_nullFilename() {
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        assertThatThrownBy(() -> fileImportService.importFile(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File name is required");
    }
}

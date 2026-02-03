# Portfolio Import Feature

## Overview

The Portfolio Manager application now supports importing your existing portfolio data from Excel (.xlsx) or CSV files. This feature enables seamless migration from spreadsheet-based portfolio tracking to the Portfolio Manager application.

## File Format

### Required Columns

Your Excel or CSV file must contain the following columns in this exact order:

| Column # | Column Name      | Description                                      | Required | Format         | Example         |
|----------|------------------|--------------------------------------------------|----------|----------------|-----------------|
| 1        | Symbol           | Asset ticker symbol                              | Yes      | Text           | AAPL            |
| 2        | Name             | Full name of the asset                           | Yes      | Text           | Apple Inc       |
| 3        | Type             | Asset type                                       | Yes      | Text (enum)    | STOCK           |
| 4        | Quantity         | Number of units owned                            | Yes      | Decimal        | 100.5           |
| 5        | Avg Buy Price    | Average purchase price per unit                  | Yes      | Decimal        | 150.25          |
| 6        | Current Price    | Current market price per unit (optional)         | No       | Decimal        | 175.50          |

### Asset Types

The following asset types are supported (case-insensitive):
- **STOCK** - Individual stocks
- **BOND** - Bonds
- **ETF** - Exchange-Traded Funds
- **CRYPTO** - Cryptocurrencies
- **CASH** - Cash holdings

### Data Validation Rules

1. **Symbol**: Cannot be empty or blank
2. **Name**: Cannot be empty or blank
3. **Type**: Must be one of the supported asset types (STOCK, BOND, ETF, CRYPTO, CASH)
4. **Quantity**: Must be a positive decimal number greater than zero
5. **Avg Buy Price**: Must be a positive decimal number greater than zero
6. **Current Price**: Optional; if provided, must be zero or greater

## Template Files

Sample template files are provided in the `docs/templates/` directory:
- **portfolio_template.xlsx** - Excel template with sample data
- **portfolio_template.csv** - CSV template with sample data

You can download these templates, replace the sample data with your portfolio information, and import the file into the application.

## How to Import

### Using the API

Send a POST request to `/api/assets/import` with a multipart file upload:

```bash
curl -X POST http://localhost:8080/api/assets/import \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/portfolio.xlsx"
```

Or for CSV:

```bash
curl -X POST http://localhost:8080/api/assets/import \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/portfolio.csv"
```

### Response Format

The import endpoint returns a detailed result in JSON format:

```json
{
  "totalRows": 10,
  "successCount": 9,
  "failureCount": 1,
  "errors": [
    "Row 5: Invalid asset type: BOND123. Valid types are: STOCK, BOND, ETF, CRYPTO, CASH"
  ],
  "importedAssets": [
    {
      "id": 1,
      "symbol": "AAPL",
      "name": "Apple Inc",
      "assetType": "STOCK",
      "quantity": 100.5,
      "avgBuyPrice": 150.25,
      "currentPrice": 175.50,
      "currentValue": 17637.75,
      "investedValue": 15100.13
    },
    ...
  ]
}
```

### Response Fields

- **totalRows**: Total number of data rows processed (excluding header)
- **successCount**: Number of assets successfully imported
- **failureCount**: Number of rows that failed to import
- **errors**: Array of error messages for failed rows, including row number and reason
- **importedAssets**: Array of successfully imported assets with their assigned IDs

## Excel File Example

Here's how your Excel file should look:

| Symbol | Name                  | Type   | Quantity | Avg Buy Price | Current Price |
|--------|-----------------------|--------|----------|---------------|---------------|
| AAPL   | Apple Inc             | STOCK  | 100.5    | 150.25        | 175.50        |
| GOOGL  | Alphabet Inc          | STOCK  | 50.25    | 2800.00       | 2950.75       |
| MSFT   | Microsoft Corporation | STOCK  | 75.0     | 250.50        | 340.25        |
| BTC    | Bitcoin               | CRYPTO | 2.5      | 45000.00      | 62000.00      |
| VTI    | Vanguard Total Market | ETF    | 200.0    | 220.00        | 245.30        |

## CSV File Example

```csv
Symbol,Name,Type,Quantity,Avg Buy Price,Current Price
AAPL,Apple Inc,STOCK,100.5,150.25,175.50
GOOGL,Alphabet Inc,STOCK,50.25,2800.00,2950.75
MSFT,Microsoft Corporation,STOCK,75.0,250.50,340.25
BTC,Bitcoin,CRYPTO,2.5,45000.00,62000.00
VTI,Vanguard Total Market ETF,ETF,200.0,220.00,245.30
```

## Error Handling

The import process handles errors gracefully:

1. **Partial Success**: If some rows fail validation, the successful rows are still imported
2. **Row-Level Errors**: Each error message includes the row number and specific error
3. **File Format Errors**: Unsupported file formats are rejected with a clear error message
4. **Data Validation**: Invalid data (missing required fields, negative values, invalid types) is caught and reported

### Common Errors

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "Symbol is required" | Symbol column is empty | Provide a valid ticker symbol |
| "Invalid asset type: XYZ" | Unrecognized asset type | Use one of: STOCK, BOND, ETF, CRYPTO, CASH |
| "Invalid quantity: abc" | Non-numeric quantity | Provide a valid decimal number |
| "Quantity must be greater than zero" | Zero or negative quantity | Provide a positive quantity |
| "Unsupported file format" | Wrong file extension | Use .xlsx or .csv files only |

## Tips for Successful Import

1. **Use Templates**: Start with the provided template files to ensure correct format
2. **Check Data Types**: Ensure numeric fields (Quantity, Avg Buy Price, Current Price) contain valid numbers
3. **Verify Asset Types**: Double-check that all Type values match the supported types exactly
4. **Remove Empty Rows**: Delete any empty rows from your spreadsheet before importing
5. **Current Price is Optional**: You can leave the Current Price column empty if you don't have current market prices
6. **Review Errors**: If any rows fail, check the error messages to identify and fix the issues

## Limitations

- **File Size**: For optimal performance, keep Excel files under 1000 rows. For larger portfolios, consider splitting into multiple files or using CSV format which has better memory efficiency
- **Excel Format**: Only .xlsx format is supported (not older .xls format)
- **Character Encoding**: CSV files should use UTF-8 encoding
- **Header Row**: Must be present as the first row in both Excel and CSV files
- **Maximum Upload Size**: Depends on server configuration (default Spring Boot multipart file size limit is 1MB, can be increased in application.yml)

## Migration Workflow

1. **Prepare Your Data**: Organize your portfolio data in Excel or CSV format using the provided templates
2. **Validate Your Data**: Ensure all required fields are filled and data types are correct
3. **Import**: Upload your file using the import API endpoint
4. **Review Results**: Check the import response for any errors
5. **Fix Errors (if any)**: Address any validation errors and re-import failed rows
6. **Verify**: Use the `/api/assets` endpoint to confirm all assets were imported correctly

## Support

If you encounter issues during import:
1. Check the error messages in the import response
2. Verify your file format matches the templates
3. Ensure all required fields have valid data
4. Check server logs for detailed error information

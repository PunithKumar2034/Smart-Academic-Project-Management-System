package com.pms.services;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;

public class BulkUploadService {

    public static class MappedData {
        public List<String> headers;
        public List<Map<String, String>> rows;
        public Map<String, String> columnMapping; // Canonical Field -> Original Header
        public List<String> missingFields;
        public List<String> sheets; // For Excel
    }

    // AI Mapping Logic: Matches messy headers to canonical database fields
    private static final Map<String, List<String>> CANONICAL_MAP = new HashMap<>() {{
        put("name", Arrays.asList("name", "full name", "fullname", "student", "teacher", "faculty", "username", "display name", "user"));
        put("email", Arrays.asList("email", "mail", "e-mail", "address", "user email", "contact"));
        put("role", Arrays.asList("role", "type", "position", "category", "user type", "status"));
    }};

    public static MappedData previewFile(File file, String sheetName) throws Exception {
        MappedData data = new MappedData();
        data.rows = new ArrayList<>();
        data.headers = new ArrayList<>();

        if (file.getName().endsWith(".csv")) {
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                String[] headers = reader.readNext();
                if (headers != null) {
                    data.headers = Arrays.asList(headers);
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        Map<String, String> row = new HashMap<>();
                        for (int i = 0; i < headers.length && i < nextLine.length; i++) {
                            row.put(headers[i], nextLine[i]);
                        }
                        data.rows.add(row);
                    }
                }
            }
        } else if (file.getName().endsWith(".xlsx")) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                List<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    sheetNames.add(workbook.getSheetName(i));
                }
                data.sheets = sheetNames;

                Sheet sheet = (sheetName == null) ? workbook.getSheetAt(0) : workbook.getSheet(sheetName);
                Iterator<Row> rowIterator = sheet.iterator();
                
                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    for (Cell cell : headerRow) {
                        data.headers.add(cell.toString());
                    }

                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Map<String, String> rowData = new HashMap<>();
                        for (int i = 0; i < data.headers.size(); i++) {
                            Cell cell = row.getCell(i);
                            rowData.put(data.headers.get(i), cell == null ? "" : cell.toString());
                        }
                        data.rows.add(rowData);
                    }
                }
            }
        }

        performSmartMapping(data);
        validateData(data);
        
        return data;
    }

    private static void performSmartMapping(MappedData data) {
        data.columnMapping = new HashMap<>();
        data.missingFields = new ArrayList<>();

        for (String canonical : CANONICAL_MAP.keySet()) {
            String match = findBestMatch(canonical, data.headers);
            if (match != null) {
                data.columnMapping.put(canonical, match);
            } else {
                data.missingFields.add(canonical);
            }
        }
    }

    private static String findBestMatch(String canonical, List<String> headers) {
        List<String> keywords = CANONICAL_MAP.get(canonical);
        for (String header : headers) {
            String cleanHeader = header.toLowerCase().trim();
            if (keywords.contains(cleanHeader)) return header;
            for (String kw : keywords) {
                if (cleanHeader.contains(kw) || kw.contains(cleanHeader)) return header;
            }
        }
        return null;
    }

    private static void validateData(MappedData data) {
        // Here we could add logic to find "gaps" and detect duplicates
        // For now, it's handled in the UI/Controller
    }
}

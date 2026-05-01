package com.biyahero.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CsvImportService {

    // Initialize the DAOs you need
    private final com.biyahero.dao.VanDAO vanDAO = new com.biyahero.dao.impl.VanDAOImpl();
    private final com.biyahero.dao.DriverDAO driverDAO = new com.biyahero.dao.impl.DriverDAOImpl();
    private final com.biyahero.dao.StopDAO stopDAO = new com.biyahero.dao.impl.StopDAOImpl();

    public ImportService.ImportResult importFromCSV(String filePath, String entityType) {
        int rowsImported = 0;
        int rowsSkipped = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length == 0 || line.trim().isEmpty()) continue;

                try {
                    switch (entityType.toUpperCase()) {
                        case "VANS":
                            if (data.length >= 3) {
                                com.biyahero.model.Van van = new com.biyahero.model.Van(
                                        data[0].trim(),
                                        data[1].trim(),
                                        Integer.parseInt(data[2].trim()),
                                        "Active"
                                );
                                vanDAO.addVan(van);
                                rowsImported++;
                            }
                            break;

                        case "DRIVERS":
                            if (data.length >= 3) {
                                com.biyahero.model.Driver driver = new com.biyahero.model.Driver(
                                        data[0].trim(),
                                        data[1].trim(),
                                        data[2].trim(),
                                        "Active"
                                );
                                driverDAO.addDriver(driver);
                                rowsImported++;
                            }
                            break;

                        case "STOPS":
                            if (data.length >= 2) {

                                com.biyahero.model.Stop stop = new com.biyahero.model.Stop(
                                        data[0].trim(),
                                        data[1].trim()
                                );

                                stopDAO.saveStop(stop);

                                rowsImported++;
                            }
                            break;

                        default:
                            return new ImportService.ImportResult(false, "Unknown import entity: " + entityType, 0);
                    }
                } catch (Exception e) {
                    rowsSkipped++;
                }
            }

            String msg = String.format("CSV Import finished! Added: %d | Skipped (Duplicates/Errors): %d",
                    rowsImported, rowsSkipped);
            return new ImportService.ImportResult(true, msg, rowsImported);

        } catch (Exception e) {
            return new ImportService.ImportResult(false, "Cannot read CSV file: " + e.getMessage(), -1);
        }
    }
}
package com.biyahero.service;

import com.biyahero.model.TripReport;
import com.biyahero.util.DBUtil;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // ── CSV ───────────────────────────────────────────────────────────────────

    public void exportTripReportsToCSV(List<TripReport> reports, String filePath)
            throws IOException {

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            pw.println("Trip ID,Driver,Route,Departure,Arrival," +
                       "Booked Seats,Total Capacity,Occupancy %,Total Revenue (PHP)");

            for (TripReport r : reports) {
                pw.printf("%s,%s,%s,%s,%s,%d,%d,%.1f,%.2f%n",
                        escapeCsv(r.getFormattedTripId()),
                        escapeCsv(r.getDriverName()),
                        escapeCsv(r.getRouteName()),
                        r.getDepartureTime() != null
                                ? r.getDepartureTime().format(DT_FMT) : "",
                        r.getArrivalDt() != null
                                ? r.getArrivalDt().format(DT_FMT) : "",
                        r.getBookedSeats(),
                        r.getTotalCapacity(),
                        r.getTotalCapacity() > 0
                                ? (r.getBookedSeats() * 100.0 / r.getTotalCapacity()) : 0.0,
                        r.getTotalRevenue()
                );
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ── JSON ──────────────────────────────────────────────────────────────────

    public void exportTripReportsToJSON(List<TripReport> reports, String filePath)
            throws IOException {

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            pw.println("{");
            pw.println("  \"exportedAt\": \"" + LocalDateTime.now().format(DT_FMT) + "\",");
            pw.println("  \"totalTrips\": " + reports.size() + ",");
            pw.println("  \"trips\": [");

            for (int i = 0; i < reports.size(); i++) {
                TripReport r = reports.get(i);
                double occupancyPct = r.getTotalCapacity() > 0
                        ? (r.getBookedSeats() * 100.0 / r.getTotalCapacity()) : 0.0;

                pw.println("    {");
                pw.println("      \"tripId\": \""    + esc(r.getFormattedTripId()) + "\",");
                pw.println("      \"driver\": \""    + esc(r.getDriverName())      + "\",");
                pw.println("      \"route\": \""     + esc(r.getRouteName())       + "\",");
                pw.println("      \"departure\": \"" +
                           (r.getDepartureTime() != null
                                   ? r.getDepartureTime().format(DT_FMT) : "") + "\",");
                pw.println("      \"arrival\": \"" +
                           (r.getArrivalDt() != null
                                   ? r.getArrivalDt().format(DT_FMT) : "") + "\",");
                pw.println("      \"bookedSeats\": "    + r.getBookedSeats()    + ",");
                pw.println("      \"totalCapacity\": "  + r.getTotalCapacity()  + ",");
                pw.printf ("      \"occupancyPercent\": %.1f,%n", occupancyPct);
                pw.printf ("      \"totalRevenuePHP\": %.2f%n",   r.getTotalRevenue());
                pw.print  ("    }");
                pw.println(i < reports.size() - 1 ? "," : "");
            }

            pw.println("  ]");
            pw.println("}");
        }
    }

    private String esc(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ── SQL Full Database Backup ───────────────────────────────────────────────

    // Exports all 8 tables as INSERT statements in FK-safe order:
    //   stop → route → van → driver → passenger → trip → routestop → booking
    // The output is a complete backup that ImportService can fully restore.
    public void exportFullDatabaseToSQL(String filePath) throws IOException {

        // Tables in insertion order — parents before children
        String[] tables = {
            "stop", "route", "van", "driver", "passenger", "trip", "routestop", "booking"
        };

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8));
             Connection conn = DBUtil.getConnection();
             Statement  stmt = conn.createStatement()) {

            pw.println("-- BiyaHero DB — Full Database Backup");
            pw.println("-- Generated: " + LocalDateTime.now().format(DT_FMT));
            pw.println("-- Database:  biyahero_db");
            pw.println();
            pw.println("USE biyahero_db;");
            pw.println("SET FOREIGN_KEY_CHECKS = 0;");
            pw.println();

            for (String table : tables) {
                pw.println("-- Table: " + table);

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    // Build column list once
                    StringBuilder colList = new StringBuilder("(");
                    for (int c = 1; c <= colCount; c++) {
                        colList.append("`").append(meta.getColumnName(c)).append("`");
                        if (c < colCount) colList.append(", ");
                    }
                    colList.append(")");

                    int rowCount = 0;
                    while (rs.next()) {
                        StringBuilder values = new StringBuilder("(");
                        for (int c = 1; c <= colCount; c++) {
                            String val = rs.getString(c);
                            if (val == null) {
                                values.append("NULL");
                            } else {
                                // Escape single quotes inside values
                                values.append("'").append(val.replace("'", "\\'")).append("'");
                            }
                            if (c < colCount) values.append(", ");
                        }
                        values.append(")");

                        pw.printf("INSERT INTO `%s` %s VALUES %s;%n",
                                table, colList, values);
                        rowCount++;
                    }

                    if (rowCount == 0) {
                        pw.println("-- (no rows)");
                    }
                } catch (Exception e) {
                    pw.println("-- ERROR reading table " + table + ": " + e.getMessage());
                }

                pw.println();
            }

            pw.println("SET FOREIGN_KEY_CHECKS = 1;");
            pw.println("-- End of backup");

        } catch (Exception e) {
            throw new IOException("SQL export failed: " + e.getMessage(), e);
        }
    }

    // ── PDF  (iText 7) ────────────────────────────────────────────────────────

    private static final DeviceRgb COLOR_HEADER_BG  = new DeviceRgb(0x2B, 0x4E, 0xC8);
    private static final DeviceRgb COLOR_ROW_ALT    = new DeviceRgb(0xF0, 0xF4, 0xFF);
    private static final DeviceRgb COLOR_SUMMARY_BG = new DeviceRgb(0x1E, 0x3A, 0x9A);

    public void exportTripReportsToPDF(List<TripReport> reports,
                                       String filePath,
                                       String rangeLabel)
            throws IOException {

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf)) {

            doc.setMargins(36, 36, 36, 36);

            doc.add(new Paragraph("BiyaHero: Trip Data Report")
                    .setFontSize(18).setBold()
                    .setFontColor(new DeviceRgb(0x2B, 0x4E, 0xC8))
                    .setTextAlignment(TextAlignment.LEFT));

            String subtitle = (rangeLabel != null && !rangeLabel.isBlank())
                    ? rangeLabel : "All Completed Trips";
            doc.add(new Paragraph(subtitle)
                    .setFontSize(10).setFontColor(ColorConstants.GRAY).setMarginBottom(4));

            doc.add(new Paragraph("Exported: " + LocalDateTime.now().format(DISPLAY_FMT))
                    .setFontSize(9).setFontColor(ColorConstants.GRAY).setMarginBottom(16));

            float[] colWidths = {1f, 2f, 2.5f, 2f, 1.8f, 1.8f};
            Table table = new Table(UnitValue.createPercentArray(colWidths))
                    .useAllAvailableWidth();

            addHeaderCell(table, "Trip ID");
            addHeaderCell(table, "Driver");
            addHeaderCell(table, "Route");
            addHeaderCell(table, "Departure");
            addHeaderCell(table, "Occupancy");
            addHeaderCell(table, "Revenue (PHP)");

            double totalRevenue  = 0;
            int    totalBooked   = 0;
            int    totalCapacity = 0;

            for (int i = 0; i < reports.size(); i++) {
                TripReport r   = reports.get(i);
                boolean altRow = (i % 2 == 1);

                addDataCell(table, r.getFormattedTripId(), altRow, TextAlignment.LEFT);
                addDataCell(table, r.getDriverName(),      altRow, TextAlignment.LEFT);
                addDataCell(table, r.getRouteName(),        altRow, TextAlignment.LEFT);
                addDataCell(table,
                        r.getDepartureTime() != null
                                ? r.getDepartureTime().format(DISPLAY_FMT) : "—",
                        altRow, TextAlignment.LEFT);
                addDataCell(table, r.getOccupancyRate(),   altRow, TextAlignment.CENTER);
                addDataCell(table,
                        String.format("%.2f", r.getTotalRevenue()),
                        altRow, TextAlignment.RIGHT);

                totalRevenue  += r.getTotalRevenue();
                totalBooked   += r.getBookedSeats();
                totalCapacity += r.getTotalCapacity();
            }

            doc.add(table);

            double avgOccupancy = totalCapacity > 0
                    ? (totalBooked * 100.0 / totalCapacity) : 0;

            doc.add(new Paragraph().setMarginTop(8));

            Table summary = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
                    .useAllAvailableWidth();
            addSummaryCell(summary, "Total Trips",   String.valueOf(reports.size()));
            addSummaryCell(summary, "Avg Occupancy", String.format("%.1f%%", avgOccupancy));
            addSummaryCell(summary, "Total Revenue", String.format("PHP %.2f", totalRevenue));
            doc.add(summary);

            doc.add(new Paragraph(
                    "Generated by BiyaHero Dispatcher System  •  " +
                    "This report covers completed trips only.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));
        }
    }

    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(
                new Cell().add(new Paragraph(text)
                        .setBold().setFontSize(9).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(COLOR_HEADER_BG).setPadding(6));
    }

    private void addDataCell(Table table, String text,
                             boolean altRow, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text == null ? "" : text)
                        .setFontSize(9).setTextAlignment(align))
                .setPadding(5)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(
                        new DeviceRgb(0xDD, 0xE3, 0xF0), 0.5f));
        if (altRow) cell.setBackgroundColor(COLOR_ROW_ALT);
        table.addCell(cell);
    }

    private void addSummaryCell(Table table, String label, String value) {
        table.addCell(
                new Cell().add(
                        new Paragraph(label + "\n" + value)
                                .setFontSize(10).setBold()
                                .setFontColor(ColorConstants.WHITE)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(COLOR_SUMMARY_BG).setPadding(8));
    }
}
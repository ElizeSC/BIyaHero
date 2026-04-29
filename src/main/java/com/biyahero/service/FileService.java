package com.biyahero.service;

import com.biyahero.model.TripReport;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // CSV
    //@param reports  the data rows to export
    //@param filePath absolute path of the output .csv file
    //@throws IOException if the file cannot be written
    public void exportTripReportsToCSV(List<TripReport> reports, String filePath)
            throws IOException {

        // force UTF-8 encoding
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            // header row
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

    // Wraps a field in quotes and escapes internal quotes (RFC 4180).
    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
    
    //JSON
    //@param reports  the data rows to export
    //@param filePath absolute path of the output .json file
    //@throws IOException if the file cannot be written
    public void exportTripReportsToJSON(List<TripReport> reports, String filePath)
            throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println("{");
            pw.println("  \"exportedAt\": \"" +
                       LocalDateTime.now().format(DT_FMT) + "\",");
            pw.println("  \"totalTrips\": " + reports.size() + ",");
            pw.println("  \"trips\": [");

            for (int i = 0; i < reports.size(); i++) {
                TripReport r = reports.get(i);
                double occupancyPct = r.getTotalCapacity() > 0
                        ? (r.getBookedSeats() * 100.0 / r.getTotalCapacity()) : 0.0;

                pw.println("    {");
                pw.println("      \"tripId\": \"" + esc(r.getFormattedTripId()) + "\",");
                pw.println("      \"driver\": \"" + esc(r.getDriverName()) + "\",");
                pw.println("      \"route\": \"" + esc(r.getRouteName()) + "\",");
                pw.println("      \"departure\": \"" +
                           (r.getDepartureTime() != null
                                   ? r.getDepartureTime().format(DT_FMT) : "") + "\",");
                pw.println("      \"arrival\": \"" +
                           (r.getArrivalDt() != null
                                   ? r.getArrivalDt().format(DT_FMT) : "") + "\",");
                pw.println("      \"bookedSeats\": " + r.getBookedSeats() + ",");
                pw.println("      \"totalCapacity\": " + r.getTotalCapacity() + ",");
                pw.printf ("      \"occupancyPercent\": %.1f,%n", occupancyPct);
                pw.printf ("      \"totalRevenuePHP\": %.2f%n", r.getTotalRevenue());
                pw.print  ("    }");
                pw.println(i < reports.size() - 1 ? "," : "");
            }

            pw.println("  ]");
            pw.println("}");
        }
    }

    // Escapes backslashes and double-quotes for JSON string values.
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // SQL Backup
    //exports a list of TripReports as SQL INSERT statements targeting the {@code trip} table in {@code biyahero_db}.
    //Only columns available from the report DTO are written. 
    // booking-level revenue is noted in a comment per row rather than stored in the trip table (which doesnt have a revenue column).
    // @param reports  the data rows to export
    // @param filePath absolute path of the output .sql file
    // @throws IOException if the file cannot be written
    public void exportTripReportsToSQL(List<TripReport> reports, String filePath)
            throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            pw.println("-- BiyaHero DB — Trip Data Backup");
            pw.println("-- Generated: " + LocalDateTime.now().format(DT_FMT));
            pw.println("-- Database:  biyahero_db");
            pw.println();
            pw.println("USE biyahero_db;");
            pw.println();
            pw.println("-- NOTE: Re-run only on a fresh / empty trip table.");
            pw.println("--       Foreign key constraints (van_id, driver_id, route_id)");
            pw.println("--       must already be satisfied before inserting these rows.");
            pw.println();

            for (TripReport r : reports) {
                String departure = r.getDepartureTime() != null
                        ? "'" + r.getDepartureTime().format(DT_FMT) + "'" : "NULL";
                String arrival = r.getArrivalDt() != null
                        ? "'" + r.getArrivalDt().format(DT_FMT) + "'" : "NULL";

                // Revenue annotation (informational. not a trip table column)
                pw.printf("-- Driver: %s | Route: %s | Revenue: PHP %.2f | Occupancy: %s%n",
                        r.getDriverName(), r.getRouteName(),
                        r.getTotalRevenue(), r.getOccupancyRate());

                // we only know trip_id, departure, arrival, and status from the DTO.
                // van_id, driver_id, route_id must be resolved via the live DB.
                pw.printf(
                        "INSERT INTO trip (trip_id, departure_dt, arrival_dt, trip_status) " +
                        "VALUES (%d, %s, %s, 'Completed');%n",
                        r.getTripId(), departure, arrival
                );
                pw.println();
            }
        }
    }


    // PDF  (iText 7)
    // brand colors
    private static final DeviceRgb COLOR_HEADER_BG  = new DeviceRgb(0x2B, 0x4E, 0xC8); // blue
    private static final DeviceRgb COLOR_ROW_ALT    = new DeviceRgb(0xF0, 0xF4, 0xFF); // light blue-grey
    private static final DeviceRgb COLOR_SUMMARY_BG = new DeviceRgb(0x1E, 0x3A, 0x9A); // dark blue

    // @param reports   the data rows to export
    //@param filePath  absolute path of the output .pdf file
    //@param rangeLabel optional label shown as subtitle (e.g. "Jan 01 – Dec 31, 2026"), pass null or empty string to omit
    // @throws IOException if the file cannot be written
    public void exportTripReportsToPDF(List<TripReport> reports,
                                       String filePath,
                                       String rangeLabel)
            throws IOException {

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf)) {

            doc.setMargins(36, 36, 36, 36);

            // title block
            doc.add(new Paragraph("BiyaHero: Trip Data Report")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(new DeviceRgb(0x2B, 0x4E, 0xC8))
                    .setTextAlignment(TextAlignment.LEFT));

            String subtitle = (rangeLabel != null && !rangeLabel.isBlank())
                    ? rangeLabel
                    : "All Completed Trips";
            doc.add(new Paragraph(subtitle)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(4));

            doc.add(new Paragraph("Exported: " + LocalDateTime.now().format(DISPLAY_FMT))
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(16));

            // Table 
            // Column widths (relative): TripID | Driver | Route | Departure | Occupancy | Revenue
            float[] colWidths = {1f, 2f, 2.5f, 2f, 1.8f, 1.8f};
            Table table = new Table(UnitValue.createPercentArray(colWidths))
                    .useAllAvailableWidth();

            addHeaderCell(table, "Trip ID");
            addHeaderCell(table, "Driver");
            addHeaderCell(table, "Route");
            addHeaderCell(table, "Departure");
            addHeaderCell(table, "Occupancy");
            addHeaderCell(table, "Revenue (PHP)");

            double totalRevenue   = 0;
            int    totalBooked    = 0;
            int    totalCapacity  = 0;

            for (int i = 0; i < reports.size(); i++) {
                TripReport r   = reports.get(i);
                boolean altRow = (i % 2 == 1);

                addDataCell(table, r.getFormattedTripId(),  altRow, TextAlignment.LEFT);
                addDataCell(table, r.getDriverName(),        altRow, TextAlignment.LEFT);
                addDataCell(table, r.getRouteName(),         altRow, TextAlignment.LEFT);
                addDataCell(table,
                        r.getDepartureTime() != null
                                ? r.getDepartureTime().format(DISPLAY_FMT) : "—",
                        altRow, TextAlignment.LEFT);
                addDataCell(table, r.getOccupancyRate(),    altRow, TextAlignment.CENTER);
                addDataCell(table,
                        String.format("%.2f", r.getTotalRevenue()),
                        altRow, TextAlignment.RIGHT);

                totalRevenue  += r.getTotalRevenue();
                totalBooked   += r.getBookedSeats();
                totalCapacity += r.getTotalCapacity();
            }

            doc.add(table);

            // Summary row
            double avgOccupancy = totalCapacity > 0
                    ? (totalBooked * 100.0 / totalCapacity) : 0;

            doc.add(new Paragraph()
                    .setMarginTop(8));

            Table summary = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
                    .useAllAvailableWidth();

            addSummaryCell(summary, "Total Trips",        String.valueOf(reports.size()));
            addSummaryCell(summary, "Avg Occupancy",      String.format("%.1f%%", avgOccupancy));
            addSummaryCell(summary, "Total Revenue",      String.format("PHP %.2f", totalRevenue));

            doc.add(summary);

            // Footer note
            doc.add(new Paragraph(
                    "Generated by BiyaHero Dispatcher System  •  " +
                    "This report covers completed trips only.")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));
        }
    }

    // PDF cell helpers 

    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(
                new Cell().add(new Paragraph(text)
                        .setBold()
                        .setFontSize(9)
                        .setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(COLOR_HEADER_BG)
                        .setPadding(6)
        );
    }

    private void addDataCell(Table table, String text,
                             boolean altRow, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text == null ? "" : text)
                        .setFontSize(9)
                        .setTextAlignment(align))
                .setPadding(5)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(
                        new DeviceRgb(0xDD, 0xE3, 0xF0), 0.5f));
        if (altRow) {
            cell.setBackgroundColor(COLOR_ROW_ALT);
        }
        table.addCell(cell);
    }

    private void addSummaryCell(Table table, String label, String value) {
        table.addCell(
                new Cell().add(
                        new Paragraph(label + "\n" + value)
                                .setFontSize(10)
                                .setBold()
                                .setFontColor(ColorConstants.WHITE)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(COLOR_SUMMARY_BG)
                        .setPadding(8)
        );
    }
}
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // ── Manifest row POJO ─────────────────────────────────────────────────────

    public static class ManifestRow {
        public final String passengerName;
        public final int    seatNumber;
        public final String pickupStop;
        public final String dropoffStop;
        public final double farePaid;
        public final String bookingStatus;

        public ManifestRow(String passengerName, int seatNumber,
                           String pickupStop, String dropoffStop,
                           double farePaid, String bookingStatus) {
            this.passengerName = passengerName;
            this.seatNumber    = seatNumber;
            this.pickupStop    = pickupStop;
            this.dropoffStop   = dropoffStop;
            this.farePaid      = farePaid;
            this.bookingStatus = bookingStatus;
        }
    }

    // Queries booking + passenger + stop for a given trip_id.
    private List<ManifestRow> fetchManifest(int tripId) throws Exception {
        List<ManifestRow> rows = new ArrayList<>();
        String sql =
            "SELECT p.name, b.seat_number, " +
            "       ps.stop_name AS pickup, ds.stop_name AS dropoff, " +
            "       b.fare_paid, b.booking_status " +
            "FROM booking b " +
            "JOIN passenger p ON b.passenger_id = p.passenger_id " +
            "JOIN stop ps     ON b.pickup_stop  = ps.stop_id " +
            "JOIN stop ds     ON b.dropoff_stop = ds.stop_id " +
            "WHERE b.trip_id = ? " +
            "ORDER BY b.seat_number";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ManifestRow(
                            rs.getString("name"),
                            rs.getInt("seat_number"),
                            rs.getString("pickup"),
                            rs.getString("dropoff"),
                            rs.getDouble("fare_paid"),
                            rs.getString("booking_status")
                    ));
                }
            }
        }
        return rows;
    }

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

    // ── CSV Manifest ──────────────────────────────────────────────────────────

    public void exportManifestToCSV(TripReport trip, String filePath) throws Exception {
        List<ManifestRow> manifest = fetchManifest(trip.getTripId());

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            // Trip header info
            pw.println("PASSENGER MANIFEST");
            pw.println("Trip ID," + escapeCsv(trip.getFormattedTripId()));
            pw.println("Driver,"  + escapeCsv(trip.getDriverName()));
            pw.println("Route,"   + escapeCsv(trip.getRouteName()));
            pw.println("Departure," + (trip.getDepartureTime() != null
                    ? trip.getDepartureTime().format(DT_FMT) : ""));
            pw.println("Total Passengers," + manifest.size());
            pw.println();

            // Passenger list
            pw.println("Seat No.,Passenger Name,Pickup,Dropoff,Fare Paid (PHP),Status");
            for (ManifestRow row : manifest) {
                pw.printf("%d,%s,%s,%s,%.2f,%s%n",
                        row.seatNumber,
                        escapeCsv(row.passengerName),
                        escapeCsv(row.pickupStop),
                        escapeCsv(row.dropoffStop),
                        row.farePaid,
                        escapeCsv(row.bookingStatus)
                );
            }

            if (manifest.isEmpty()) {
                pw.println("(No passengers booked for this trip)");
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

    // ── JSON Manifest ─────────────────────────────────────────────────────────

    public void exportManifestToJSON(TripReport trip, String filePath) throws Exception {
        List<ManifestRow> manifest = fetchManifest(trip.getTripId());

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {

            pw.println("{");
            pw.println("  \"manifest\": {");
            pw.println("    \"tripId\": \""    + esc(trip.getFormattedTripId()) + "\",");
            pw.println("    \"driver\": \""    + esc(trip.getDriverName())      + "\",");
            pw.println("    \"route\": \""     + esc(trip.getRouteName())       + "\",");
            pw.println("    \"departure\": \"" + (trip.getDepartureTime() != null
                    ? trip.getDepartureTime().format(DT_FMT) : "") + "\",");
            pw.println("    \"totalPassengers\": " + manifest.size());
            pw.println("  },");
            pw.println("  \"passengers\": [");

            for (int i = 0; i < manifest.size(); i++) {
                ManifestRow row = manifest.get(i);
                pw.println("    {");
                pw.println("      \"seatNumber\": "      + row.seatNumber                      + ",");
                pw.println("      \"name\": \""          + esc(row.passengerName)              + "\",");
                pw.println("      \"pickup\": \""        + esc(row.pickupStop)                 + "\",");
                pw.println("      \"dropoff\": \""       + esc(row.dropoffStop)                + "\",");
                pw.printf ("      \"farePaidPHP\": %.2f,%n", row.farePaid);
                pw.println("      \"status\": \""        + esc(row.bookingStatus)              + "\"");
                pw.print  ("    }");
                pw.println(i < manifest.size() - 1 ? "," : "");
            }

            pw.println("  ]");
            pw.println("}");
        }
    }

    private String esc(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final Set<String> ALLOWED_TABLES = Set.of(
        "stop", "route", "van", "driver", "passenger", "trip", "routestop", "booking"
    );

    public void exportFullDatabaseToSQL(String filePath) throws IOException {

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

            for (String table : ALLOWED_TABLES) {
                if (!ALLOWED_TABLES.contains(table))
                    throw new IllegalArgumentException("Invalid table: " + table);

                pw.println("-- Table: " + table);

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

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
                                values.append("'").append(val.replace("'", "\\'")).append("'");
                            }
                            if (c < colCount) values.append(", ");
                        }
                        values.append(")");

                        pw.printf("INSERT INTO `%s` %s VALUES %s;%n",
                                table, colList, values);
                        rowCount++;
                    }

                    if (rowCount == 0) pw.println("-- (no rows)");

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

            float[] colWidths = {1f, 2f, 2.5f, 2f, 2f, 1.8f, 1.8f};
            Table table = new Table(UnitValue.createPercentArray(colWidths))
                    .useAllAvailableWidth();

            addHeaderCell(table, "Trip ID");
            addHeaderCell(table, "Driver");
            addHeaderCell(table, "Route");
            addHeaderCell(table, "Departure");
            addHeaderCell(table, "Arrival");
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
                addDataCell(table,
                        r.getArrivalDt() != null
                                ? r.getArrivalDt().format(DISPLAY_FMT) : "—",
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

    // ── PDF Manifest ──────────────────────────────────────────────────────────

    public void exportManifestToPDF(TripReport trip, String filePath) throws Exception {
        List<ManifestRow> manifest = fetchManifest(trip.getTripId());

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf)) {

            doc.setMargins(36, 36, 36, 36);

            // Title
            doc.add(new Paragraph("BiyaHero: Passenger Manifest")
                    .setFontSize(18).setBold()
                    .setFontColor(new DeviceRgb(0x2B, 0x4E, 0xC8)));

            // Trip info block
            doc.add(new Paragraph(
                    "Trip: " + trip.getFormattedTripId() +
                    "   |   Driver: " + trip.getDriverName() +
                    "   |   Route: " + trip.getRouteName())
                    .setFontSize(10).setFontColor(ColorConstants.GRAY).setMarginBottom(2));

            doc.add(new Paragraph(
                    "Departure: " + (trip.getDepartureTime() != null
                        ? trip.getDepartureTime().format(DISPLAY_FMT) : "—") +
                    "   |   Arrival: " + (trip.getArrivalDt() != null    
                        ? trip.getArrivalDt().format(DISPLAY_FMT) : "—") +
                    "   |   Total Passengers: " + manifest.size())
                    .setFontSize(10).setFontColor(ColorConstants.GRAY).setMarginBottom(2));

            doc.add(new Paragraph("Exported: " + LocalDateTime.now().format(DISPLAY_FMT))
                    .setFontSize(9).setFontColor(ColorConstants.GRAY).setMarginBottom(16));

            if (manifest.isEmpty()) {
                doc.add(new Paragraph("No passengers booked for this trip.")
                        .setFontSize(10).setFontColor(ColorConstants.GRAY));
            } else {
                // Passenger table: Seat | Name | Pickup | Dropoff | Fare | Status
                float[] colWidths = {0.6f, 2.5f, 2f, 2f, 1.2f, 1.2f};
                Table table = new Table(UnitValue.createPercentArray(colWidths))
                        .useAllAvailableWidth();

                addHeaderCell(table, "Seat");
                addHeaderCell(table, "Passenger Name");
                addHeaderCell(table, "Pickup");
                addHeaderCell(table, "Dropoff");
                addHeaderCell(table, "Fare (PHP)");
                addHeaderCell(table, "Status");

                double totalFare = 0;
                for (int i = 0; i < manifest.size(); i++) {
                    ManifestRow row = manifest.get(i);
                    boolean alt = (i % 2 == 1);

                    addDataCell(table, String.valueOf(row.seatNumber), alt, TextAlignment.CENTER);
                    addDataCell(table, row.passengerName,              alt, TextAlignment.LEFT);
                    addDataCell(table, row.pickupStop,                 alt, TextAlignment.LEFT);
                    addDataCell(table, row.dropoffStop,                alt, TextAlignment.LEFT);
                    addDataCell(table, String.format("%.2f", row.farePaid), alt, TextAlignment.RIGHT);
                    addDataCell(table, row.bookingStatus,              alt, TextAlignment.CENTER);

                    totalFare += row.farePaid;
                }

                doc.add(table);

                // Summary footer
                doc.add(new Paragraph().setMarginTop(8));
                Table summary = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                        .useAllAvailableWidth();
                addSummaryCell(summary, "Total Passengers", String.valueOf(manifest.size()));
                addSummaryCell(summary, "Total Fare Collected", String.format("PHP %.2f", totalFare));
                doc.add(summary);
            }

            doc.add(new Paragraph(
                    "Generated by BiyaHero Dispatcher System  •  Confidential")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));
        }
    }

    // ── PDF cell helpers ──────────────────────────────────────────────────────

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
package com.kalasetu.fileio;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.model.AuditEvent;
import com.kalasetu.domain.model.CraftProduct;
import com.kalasetu.domain.model.Order;
import com.kalasetu.domain.model.OrderItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * File Read/Write module handling CSV Catalog import/export, Order Invoice Receipts, and Audit Logs.
 */
public class CatalogFileService {
    private static final String CSV_HEADER = "sku,title,craft_type,origin_village,gi_certified,price_inr,stock_qty";
    private final Path baseDir;

    public CatalogFileService(Path baseDir) throws IOException {
        this.baseDir = baseDir.toAbsolutePath().normalize();
        Files.createDirectories(this.baseDir.resolve("exports"));
        Files.createDirectories(this.baseDir.resolve("receipts"));
        Files.createDirectories(this.baseDir.resolve("logs"));
    }

    public Path resolveSafe(String subdir, String filename) throws InvalidInputException {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidInputException("filename", "Invalid or unsafe filename");
        }
        Path resolved = baseDir.resolve(subdir).resolve(filename).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new InvalidInputException("filename", "Path traversal attempt blocked");
        }
        return resolved;
    }

    public Path exportCatalogCsv(List<CraftProduct> products, String filename) throws IOException, InvalidInputException {
        Path out = resolveSafe("exports", filename);
        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (CraftProduct p : products) {
                writer.write(String.join(",",
                        escapeCsv(p.getSku()),
                        escapeCsv(p.getTitle()),
                        escapeCsv(p.getCraftType()),
                        escapeCsv(p.getOriginVillage()),
                        String.valueOf(p.isGiCertified()),
                        p.getPriceInr().toPlainString(),
                        String.valueOf(p.getStockQty())
                ));
                writer.newLine();
            }
        }
        return out;
    }

    public String exportCatalogCsvAsString(List<CraftProduct> products) {
        StringBuilder sb = new StringBuilder(CSV_HEADER).append("\n");
        for (CraftProduct p : products) {
            sb.append(escapeCsv(p.getSku())).append(',')
              .append(escapeCsv(p.getTitle())).append(',')
              .append(escapeCsv(p.getCraftType())).append(',')
              .append(escapeCsv(p.getOriginVillage())).append(',')
              .append(p.isGiCertified()).append(',')
              .append(p.getPriceInr().toPlainString()).append(',')
              .append(p.getStockQty()).append('\n');
        }
        return sb.toString();
    }

    public List<CraftProduct> importCatalogCsv(Path csvFile) throws IOException, InvalidInputException {
        List<CraftProduct> imported = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new InvalidInputException("csvHeader", "Expected header: " + CSV_HEADER);
            }
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                List<String> cols = parseCsvLine(line);
                if (cols.size() != 7) {
                    throw new InvalidInputException("csvRow" + row, "Expected 7 columns, found " + cols.size());
                }
                CraftProduct p = new CraftProduct();
                p.setSku(cols.get(0));
                p.setTitle(cols.get(1));
                p.setCraftType(cols.get(2));
                p.setOriginVillage(cols.get(3));
                p.setGiCertified(Boolean.parseBoolean(cols.get(4)));
                p.setPriceInr(new BigDecimal(cols.get(5)));
                p.setStockQty(Integer.parseInt(cols.get(6)));
                p.setDescription("Imported artisan craft via CSV");
                p.setActive(true);
                imported.add(p);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidInputException("csvNumeric", "Invalid price or stock number in CSV", nfe);
        }
        return imported;
    }

    public Path writeOrderReceipt(Order order) throws IOException, InvalidInputException {
        String safeOrderNo = order.getOrderNumber().replaceAll("[^A-Za-z0-9_-]", "");
        Path out = resolveSafe("receipts", safeOrderNo + ".txt");
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("====================================================\n");
            w.write("     WEAVO — FROM THE LOOM TO YOUR LIVING ROOM      \n");
            w.write("          ORDER INVOICE (TEST MODE RECEIPT)          \n");
            w.write("====================================================\n");
            w.write("Order Number : " + order.getOrderNumber() + "\n");
            w.write("Order Status : " + order.getOrderStatus() + "\n");
            w.write("Payment Mode : " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n");
            w.write("Txn Ref      : " + order.getTransactionRef() + "\n");
            w.write("Ship To      : " + order.getShippingAddress() + "\n");
            w.write("DPDP Consent : " + order.getDpdpConsentVersion() + "\n");
            w.write("----------------------------------------------------\n");
            for (OrderItem item : order.getItems()) {
                w.write(String.format("- %s x%d @ INR %s = INR %s (Artisan Payout: INR %s)%n",
                        item.getProductTitle() != null ? item.getProductTitle() : item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPriceInr().toPlainString(),
                        item.getLineTotalInr().toPlainString(),
                        item.getArtisanPayoutInr().toPlainString()));
            }
            w.write("----------------------------------------------------\n");
            w.write("Shipping Fee : INR " + order.getShippingFeeInr().toPlainString() + "\n");
            w.write("Platform Fee : INR " + order.getPlatformCommissionInr().toPlainString() + "\n");
            w.write("Grand Total  : INR " + order.getTotalAmountInr().toPlainString() + "\n");
            w.write("====================================================\n");
        }
        return out;
    }

    public String readOrderReceipt(String orderNumber) throws IOException, InvalidInputException {
        String safeOrderNo = orderNumber.replaceAll("[^A-Za-z0-9_-]", "");
        Path p = resolveSafe("receipts", safeOrderNo + ".txt");
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    public synchronized void appendAuditLog(AuditEvent event) throws IOException {
        Path logFile = baseDir.resolve("logs").resolve("weavo-audit.log");
        String line = String.format("%s | %s | %s | %s | actor=%s%n",
                event.getCreatedAt() != null ? event.getCreatedAt() : OffsetDateTime.now(),
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getActorUserId());
        Files.writeString(logFile, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<String> readRecentAuditLines(int maxLines) throws IOException {
        Path logFile = baseDir.resolve("logs").resolve("weavo-audit.log");
        if (!Files.exists(logFile)) return List.of();
        List<String> all = Files.readAllLines(logFile, StandardCharsets.UTF_8);
        int start = Math.max(0, all.size() - maxLines);
        List<String> tail = new ArrayList<>(all.subList(start, all.size()));
        Collections.reverse(tail);
        return tail;
    }

    private static String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }
}

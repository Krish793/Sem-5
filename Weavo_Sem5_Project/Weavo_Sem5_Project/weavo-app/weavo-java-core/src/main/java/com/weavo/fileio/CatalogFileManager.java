package com.weavo.fileio;

import com.weavo.exception.InvalidInputException;
import com.weavo.model.CraftProduct;
import com.weavo.model.Order;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File Read / Write Service demonstrating:
 * 1. BufferedReader and FileReader (reading catalog CSV files line-by-line)
 * 2. BufferedWriter and FileWriter (writing official tax invoices and transaction logs)
 * 3. File existence checks, directory management, and stream exception handling.
 */
public class CatalogFileManager {
    private final File dataDir;

    public CatalogFileManager(String baseDirPath) {
        this.dataDir = new File(baseDirPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * File Write Demonstration:
     * Writes an authentic tax invoice receipt to disk using BufferedWriter.
     */
    public File writeOrderInvoice(Order order, String customerName) throws IOException {
        File invoiceFile = new File(dataDir, "INVOICE_" + order.getOrderNumber() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(invoiceFile))) {
            writer.write("=================================================================\n");
            writer.write("           WEAVO ARTISAN MARKETPLACE - OFFICIAL TAX INVOICE       \n");
            writer.write("               From the loom to your living room                 \n");
            writer.write("=================================================================\n");
            writer.write(String.format("Order Number    : %s\n", order.getOrderNumber()));
            writer.write(String.format("Customer Name   : %s (ID: %s)\n", customerName, order.getCustomerId()));
            writer.write(String.format("Payment Status  : %s\n", order.getPaymentStatus()));
            writer.write(String.format("Transaction Ref : %s\n", order.getTransactionRef()));
            writer.write("-----------------------------------------------------------------\n");
            writer.write("ITEMS PURCHASED:\n");
            for (Order.OrderItem item : order.items()) {
                writer.write(String.format(" - %-32s x%-2d @ ₹%-8.2f = ₹%.2f\n", 
                        item.getTitle(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
            }
            writer.write("-----------------------------------------------------------------\n");
            writer.write(String.format("TOTAL PAYABLE AMOUNT   : ₹%.2f\n", order.getTotalAmountInr()));
            writer.write(String.format("DIRECT ARTISAN SHARE   : ₹%.2f (92%% Direct Jan Dhan Payout)\n", order.getArtisanPayoutInr()));
            writer.write(String.format("WEAVO PLATFORM/CLOUD   : ₹%.2f (8%% Infrastructure Fee)\n", order.getPlatformFeeInr()));
            writer.write("=================================================================\n");
            writer.write("DPDP Act 2023 Compliant · 100% Certified Gujarat Handloom\n");
            writer.flush();
        }
        return invoiceFile;
    }

    /**
     * File Write Demonstration:
     * Exports catalog data to CSV using BufferedWriter.
     */
    public File exportCatalogToCsv(List<CraftProduct> products, String filename) throws IOException {
        File csvFile = new File(dataDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("id,sku,title,craft_type,origin_village,gi_certified,price_inr,stock_qty\n");
            for (CraftProduct p : products) {
                writer.write(String.format("%s,%s,\"%s\",\"%s\",\"%s\",%b,%.2f,%d\n",
                        p.getId(), p.getSku(), p.getTitle(), p.getCraftType(), p.getOriginVillage(),
                        p.isGiCertified(), p.getPriceInr(), p.getStockQty()));
            }
            writer.flush();
        }
        return csvFile;
    }

    /**
     * File Read Demonstration:
     * Reads a CSV catalog file line-by-line using BufferedReader and FileReader.
     */
    public List<CraftProduct> readCatalogFromCsv(File csvFile) throws IOException, InvalidInputException {
        List<CraftProduct> products = new ArrayList<>();
        if (!csvFile.exists()) {
            throw new FileNotFoundException("Catalog file not found: " + csvFile.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // skip CSV header
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                // Simple CSV split (handling standard fields)
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 8) {
                    String id = parts[0].trim();
                    String sku = parts[1].trim();
                    String title = parts[2].replace("\"", "").trim();
                    String craftType = parts[3].replace("\"", "").trim();
                    String village = parts[4].replace("\"", "").trim();
                    boolean gi = Boolean.parseBoolean(parts[5].trim());
                    double price = Double.parseDouble(parts[6].trim());
                    int stock = Integer.parseInt(parts[7].trim());

                    products.add(new CraftProduct(id, sku, title, craftType, village, gi, price, stock));
                }
            }
        }
        return products;
    }

    /**
     * File Read Demonstration:
     * Reads text content of an invoice file back to console/string using BufferedReader.
     */
    public String readInvoiceContent(File invoiceFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(invoiceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}

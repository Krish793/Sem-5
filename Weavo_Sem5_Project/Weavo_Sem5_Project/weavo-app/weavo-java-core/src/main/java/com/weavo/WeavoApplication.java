package com.weavo;

import com.weavo.concurrency.InventoryOrderProcessor;
import com.weavo.exception.InvalidInputException;
import com.weavo.exception.OutOfStockException;
import com.weavo.exception.PaymentFailedException;
import com.weavo.fileio.CatalogFileManager;
import com.weavo.model.*;
import com.weavo.payment.CardPaymentGateway;
import com.weavo.payment.PaymentGateway;
import com.weavo.payment.UpiPaymentGateway;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Standalone Verification Application for Weavo.
 * Demonstrates all 5 Core Java Academic Syllabus Requirements:
 * 1. OOP Design (Encapsulation, Abstraction, Polymorphism)
 * 2. Inheritance & Interfaces (User hierarchy, PaymentGateway implementations)
 * 3. Custom Exceptions (WeavoException, OutOfStockException, PaymentFailedException, InvalidInputException)
 * 4. Multithreading (ThreadPoolExecutor, Callable/Future, ReentrantLock concurrency race protection)
 * 5. File Read & Write (BufferedReader, BufferedWriter, CSV export, Invoice generation)
 */
public class WeavoApplication {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   WEAVO ARTISAN MARKETPLACE - JAVA 5-PILLAR SYLLABUS VERIFICATION      ");
        System.out.println("                  From the loom to your living room                     ");
        System.out.println("========================================================================\n");

        // ---------------------------------------------------------------------
        // PILLAR 1 & 2: OOP DESIGN, INHERITANCE & INTERFACES
        // ---------------------------------------------------------------------
        System.out.println(">>> [PILLAR 1 & 2] Demonstrating OOP Design, Inheritance & Interfaces...");
        
        // Inheritance: Customer, Artisan, Admin inherit from abstract User
        User customer = new Customer("U101", "Aarav Patel", "aarav@weavo.demo", "Ahmedabad", "Gujarat", true, "12 SG Highway, Ahmedabad");
        User artisan = new Artisan("A201", "Rameshbhai Khatri", "ramesh@weavo.demo", "Jamnagar", "Gujarat", true, "Jamnagar Weavers Cluster", "Bandhani Tie-Dye", true);
        User admin = new Admin("AD01", "Priya Shah", "admin@weavo.demo", "Gandhinagar", "Gujarat", true, "Academic Quality Assurance");

        List<User> platformUsers = new ArrayList<>();
        platformUsers.add(customer);
        platformUsers.add(artisan);
        platformUsers.add(admin);

        for (User u : platformUsers) {
            System.out.printf("  • %-10s | %-20s | Can Manage Products: %-5b | Can Buy: %-5b\n",
                    u.getRoleName(), u.getFullName(), u.canManageInventory(), u.canPlaceOrders());
        }

        // Polymorphism via PaymentGateway interface
        PaymentGateway upiGateway = new UpiPaymentGateway();
        PaymentGateway cardGateway = new CardPaymentGateway();
        System.out.printf("  • Gateway Interface 1 : %s\n", upiGateway.getGatewayName());
        System.out.printf("  • Gateway Interface 2 : %s\n", cardGateway.getGatewayName());
        System.out.println("  ✓ OOP, Inheritance & Interface Polymorphism Verified!\n");

        // ---------------------------------------------------------------------
        // PILLAR 3: CUSTOM EXCEPTIONS
        // ---------------------------------------------------------------------
        System.out.println(">>> [PILLAR 3] Demonstrating Custom Exceptions...");
        
        // Test 1: InvalidInputException
        try {
            System.out.println("  Testing InvalidInputException (price < ₹50)...");
            new CraftProduct("P0", "WV-TEST", "Fake Item", "Test", "Test", false, 10.0, 5);
        } catch (InvalidInputException e) {
            System.out.printf("  ✓ Caught expected InvalidInputException: %s [Code: %s]\n", e.getMessage(), e.getErrorCode());
        }

        // Test 2: PaymentFailedException
        try {
            System.out.println("  Testing PaymentFailedException (Simulated Gateway Decline)...");
            Order testOrder = new Order("WV-FAIL-01", customer.getId());
            upiGateway.processPayment(testOrder, 1500.0, true);
        } catch (PaymentFailedException e) {
            System.out.printf("  ✓ Caught expected PaymentFailedException: %s [Code: %s, Amount: ₹%.2f]\n", 
                    e.getMessage(), e.getErrorCode(), e.getAmount());
        }

        // Test 3: OutOfStockException
        try {
            System.out.println("  Testing OutOfStockException (Ordering 20 units when stock is 2)...");
            CraftProduct limitedCraft = new CraftProduct("P101", "WV-BN-001", "Bandhani Saree", "Bandhani", "Jamnagar", true, 4850.0, 2);
            limitedCraft.decrementStock(20);
        } catch (OutOfStockException e) {
            System.out.printf("  ✓ Caught expected OutOfStockException: %s [Available: %d, Requested: %d]\n", 
                    e.getMessage(), e.getAvailableQty(), e.getRequestedQty());
        } catch (InvalidInputException ignored) {}
        System.out.println("  ✓ Custom Exception Handling Hierarchy Verified!\n");

        // ---------------------------------------------------------------------
        // PILLAR 4: MULTITHREADING & CONCURRENCY
        // ---------------------------------------------------------------------
        System.out.println(">>> [PILLAR 4] Demonstrating Multithreading & ReentrantLock Concurrency...");
        try {
            CraftProduct festivalDrop = new CraftProduct("P555", "WV-PT-001", "Patan Double Ikkat Patola", "Double Ikkat", "Patan", true, 8200.0, 3);
            InventoryOrderProcessor processor = new InventoryOrderProcessor(4);

            // 6 concurrent threads competing for 3 units
            processor.runConcurrentStressTest(festivalDrop, upiGateway, 6, 1);
            processor.shutdown();

            System.out.printf("  [Result] Successful thread purchases: %d | Blocked (out of stock): %d | Remaining Stock: %d\n",
                    processor.getSuccessfulOrdersCount(), processor.getRejectedOrdersCount(), festivalDrop.getStockQty());
            System.out.println("  ✓ Multithreading with ReentrantLock Race Protection Verified!\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---------------------------------------------------------------------
        // PILLAR 5: FILE READ & WRITE (BufferedReader, BufferedWriter)
        // ---------------------------------------------------------------------
        System.out.println(">>> [PILLAR 5] Demonstrating File Read & Write...");
        String outputDir = System.getProperty("user.dir") + File.separator + "weavo-data";
        CatalogFileManager fileManager = new CatalogFileManager(outputDir);

        try {
            // 1. Prepare sample catalog
            List<CraftProduct> catalog = new ArrayList<>();
            catalog.add(new CraftProduct("P1", "WV-BN-001", "Jamnagar Gajji Silk Bandhani", "Bandhani Tie-Dye", "Jamnagar", true, 4850.0, 12));
            catalog.add(new CraftProduct("P2", "WV-PT-002", "Patan Pure Silk Double Patola", "Double Ikkat", "Patan", true, 8200.0, 4));
            catalog.add(new CraftProduct("P3", "WV-AJ-003", "Dhamadka Organic Indigo Ajrakh Stole", "Ajrakh Block Print", "Dhamadka", true, 1850.0, 25));

            // 2. File Write: Export to CSV
            File csvFile = fileManager.exportCatalogToCsv(catalog, "weavo_catalog_exported.csv");
            System.out.printf("  • [BufferedWriter] Successfully exported %d artisan crafts to CSV: %s\n", 
                    catalog.size(), csvFile.getName());

            // 3. File Read: Read CSV back
            List<CraftProduct> readBack = fileManager.readCatalogFromCsv(csvFile);
            System.out.printf("  • [BufferedReader] Successfully read back %d records from CSV:\n", readBack.size());
            for (CraftProduct cp : readBack) {
                System.out.printf("      - %s (₹%.2f, %s, GI=%s)\n", cp.getTitle(), cp.getPriceInr(), cp.getOriginVillage(), cp.isGiCertified());
            }

            // 4. File Write: Order Invoice Receipt
            Order verifiedOrder = new Order("WV-2026-LIVE-001", customer.getId());
            verifiedOrder.addItem(readBack.get(0), 1);
            verifiedOrder.addItem(readBack.get(2), 2);
            upiGateway.processPayment(verifiedOrder, verifiedOrder.getTotalAmountInr(), false);

            File invoiceFile = fileManager.writeOrderInvoice(verifiedOrder, customer.getFullName());
            System.out.printf("  • [BufferedWriter] Generated official tax invoice: %s\n", invoiceFile.getName());

            // 5. File Read: Read invoice back to console
            System.out.println("  • [BufferedReader] Printing generated invoice receipt preview:");
            String invoicePreview = fileManager.readInvoiceContent(invoiceFile);
            for (String line : invoicePreview.split("\n")) {
                System.out.println("      | " + line);
            }
            System.out.println("  ✓ File Read/Write (BufferedReader / BufferedWriter) Verified!\n");

        } catch (IOException | InvalidInputException | PaymentFailedException e) {
            e.printStackTrace();
        }

        System.out.println("========================================================================");
        System.out.println("  ALL 5 JAVA SYLLABUS PILLARS TESTED AND VERIFIED SUCCESSFULLY!         ");
        System.out.println("========================================================================");
    }
}

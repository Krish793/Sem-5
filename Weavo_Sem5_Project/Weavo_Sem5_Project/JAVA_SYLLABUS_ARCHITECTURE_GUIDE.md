# Weavo Artisan Marketplace — Java Academic Syllabus Architecture Guide

**Institution:** JG University — School of Engineering (Information Technology)  
**Subject:** Advanced Java / Enterprise Software Engineering  
**Application:** Weavo — Local Artisan Marketplace (*From the loom to your living room*)  
**Team Members:**
- Eklavya Desai (242355) — Backend Architecture & Concurrency
- Faizankhan Pathan (242432) — E-Commerce Strategy & Catalog
- Krish Padsala (242397) — Full-Stack Lead & Cloud Deployment
- Fenil Vora (242473) — Software Testing & QA Suite
- Nihar Sangani (242448) — Startup Policies & Compliance

---

## Executive Summary: 5-Pillar Java Syllabus Implementation

| Syllabus Pillar | Java Package / Classes | Key Concepts Demonstrated |
| :--- | :--- | :--- |
| **1. OOP Design** | `com.weavo.model.*`, `com.kalasetu.domain.model.*` | Encapsulation, Abstraction, Data Hiding, State Management |
| **2. Inheritance & Interfaces** | `User` hierarchy, `PaymentGateway`, `PaymentMethod` | Abstract Classes, Interface Polymorphism, Subtyping |
| **3. Custom Exceptions** | `com.weavo.exception.*`, `com.kalasetu.domain.exception.*` | Exception Hierarchy, Checked/Unchecked Exceptions, Error Codes |
| **4. Multithreading** | `com.weavo.concurrency.*`, `com.kalasetu.concurrency.*` | `ExecutorService`, `ThreadPoolExecutor`, `ReentrantLock`, Thread Safety |
| **5. File Read / Write** | `com.weavo.fileio.*`, `com.kalasetu.fileio.*` | `BufferedReader`, `BufferedWriter`, CSV Parsing, Tax Invoices |

---

## 1. OOP Design (Object-Oriented Programming)

### Principles Applied
1. **Encapsulation**:
   - All domain entity variables (`id`, `sku`, `title`, `priceInr`, `stockQty`, `email`) are declared `private`.
   - Access and mutations occur strictly through validated getters and setters.
   - Example in `CraftProduct.java`:
     ```java
     public class CraftProduct {
         private String sku;
         private double priceInr;
         private int stockQty;

         public synchronized void decrementStock(int quantity) throws OutOfStockException {
             if (this.stockQty < quantity) {
                 throw new OutOfStockException(this.id, quantity, this.stockQty);
             }
             this.stockQty -= quantity;
         }
     }
     ```
2. **Abstraction**:
   - Abstract base class `User.java` encapsulates common user state and defines contract methods:
     - `public abstract String getRoleName();`
     - `public abstract boolean canManageInventory();`
     - `public abstract boolean canPlaceOrders();`

---

## 2. Inheritance & Interfaces

### Inheritance Class Hierarchy
```
                 ┌───────────────────────────┐
                 │    abstract class User    │
                 │ (Encapsulated Base State) │
                 └─────────────┬─────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         ▼                     ▼                     ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  class Customer  │  │  class Artisan   │  │   class Admin    │
│  (Buyer Logic)   │  │ (92% Karigar Cut)│  │ (Audit & Gov)    │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

- **Subclass Specialization**:
  - `Customer` adds `shippingAddress` and `loyaltyPoints`.
  - `Artisan` adds `craftClusterVillage`, `craftSpecialty`, `giCertified`, and `calculateArtisanEarnings()` guaranteeing the 92% direct payout.
  - `Admin` adds governance privileges.

### Interface Polymorphism
```java
public interface PaymentGateway {
    String getGatewayName();
    String processPayment(Order order, double amount, boolean simulateFailure) throws PaymentFailedException;
    boolean verifyTransaction(String transactionRef);
}
```
- **Implementations**:
  - `UpiPaymentGateway`: Simulates NPCI BHIM UPI tokenized payments (`UPI-TXN-...`).
  - `CardPaymentGateway`: Simulates RuPay / Visa 3D-Secure tokenized payments (`CARD-TXN-...`).
  - `WalletPaymentGateway`: Simulates digital wallet and netbanking escrow.

---

## 3. Custom Exceptions

Weavo defines a strict custom exception hierarchy rooted at `WeavoException`:

```
                    ┌─────────────────────────┐
                    │ class WeavoException    │
                    │   (Base Exception)      │
                    └────────────┬────────────┘
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     ▼                           ▼                           ▼
┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
│ OutOfStockException   │ │PaymentFailedException │ │ InvalidInputException │
│ (Race condition block)│ │ (Gateway decline)     │ │ (Bad price/qty check) │
└───────────────────────┘ └───────────────────────┘ └───────────────────────┘
```

### Code Example:
```java
// Thrown when an artisan craft runs out during concurrent checkout:
public class OutOfStockException extends WeavoException {
    private final String productId;
    private final int requestedQty;
    private final int availableQty;

    public OutOfStockException(String productId, int requestedQty, int availableQty) {
        super(String.format("Product '%s' is out of stock. Requested: %d, Available: %d", 
                productId, requestedQty, availableQty), "ERR_OUT_OF_STOCK");
        this.productId = productId;
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
    }
}
```

---

## 4. Multithreading & Concurrency

### Problem Solved
During Diwali and festival craft flash sales, hundreds of customers attempt to order the same limited, one-of-a-kind Patan Patola or Bandhani Saree simultaneously. Without thread synchronization, **race conditions** cause overselling and negative inventory.

### Concurrency Implementation
- **Technology**: `java.util.concurrent.ExecutorService`, `ThreadPoolExecutor`, `Callable<Boolean>`, `Future<Boolean>`, and `java.util.concurrent.locks.ReentrantLock`.
- **Race Protection Logic**:
  ```java
  public class InventoryOrderProcessor {
      private final ReentrantLock inventoryLock = new ReentrantLock();

      public boolean processSingleOrder(Order order, CraftProduct product, int quantity, 
                                        PaymentGateway gateway, boolean simulateFail) 
                                        throws OutOfStockException, PaymentFailedException {
          inventoryLock.lock(); // Thread synchronization
          try {
              if (product.getStockQty() < quantity) {
                  throw new OutOfStockException(product.getId(), quantity, product.getStockQty());
              }
              product.decrementStock(quantity);
              order.addItem(product, quantity);
              gateway.processPayment(order, order.getTotalAmountInr(), simulateFail);
              return true;
          } finally {
              inventoryLock.unlock(); // Always release lock in finally block
          }
      }
  }
  ```
- **Stress-Test Simulation**: The system launches multiple concurrent threads. If stock is 3 and 6 threads compete, exactly 3 succeed and 3 are safely rejected with `OutOfStockException`. Zero negative stock.

---

## 5. File Read & Write (File I/O)

### File Write Demonstration:
Using `java.io.BufferedWriter` and `java.io.FileWriter` to write:
1. **Official Tax Invoices (`INVOICE_WV-*.txt`)**:
   - Generates formatted legal invoices containing product details, customer ID, DPDP 2023 compliance tag, and the 92% karigar direct payout calculation.
2. **Catalog CSV Exports (`weavo_catalog_exported.csv`)**:
   - Exports the entire artisan database to CSV format.

### File Read Demonstration:
Using `java.io.BufferedReader` and `java.io.FileReader`:
1. **Catalog CSV Import**:
   - Reads catalog records line-by-line, parses CSV tokens, checks GI certification, and constructs `CraftProduct` objects in memory.
2. **Invoice Preview Stream**:
   - Reads invoice files back from disk line-by-line to stream to the terminal or web receipt viewer.

```java
// Example BufferedReader File Read
try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
    String line;
    boolean isFirstLine = true;
    while ((line = reader.readLine()) != null) {
        if (isFirstLine) { isFirstLine = false; continue; } // Skip header
        String[] parts = line.split(",");
        // Construct and validate CraftProduct
    }
}

// Example BufferedWriter File Write
try (BufferedWriter writer = new BufferedWriter(new FileWriter(invoiceFile))) {
    writer.write("=================================================================\n");
    writer.write("           WEAVO ARTISAN MARKETPLACE - OFFICIAL TAX INVOICE       \n");
    writer.write(String.format("TOTAL AMOUNT : ₹%.2f (92%% Direct Artisan Share: ₹%.2f)\n", total, payout));
    writer.flush();
}
```

---

## Source Code Directory Reference

```
weavo-app/
├── weavo-java-core/                   # Standalone Core Java Showcase (Zero External Deps)
│   ├── pom.xml
│   └── src/main/java/com/weavo/
│       ├── WeavoApplication.java      # Main runnable demonstration
│       ├── model/                     # OOP, Encapsulation & Inheritance
│       │   ├── User.java              # Abstract Base User
│       │   ├── Customer.java          # Inherits User
│       │   ├── Artisan.java           # Inherits User
│       │   ├── Admin.java             # Inherits User
│       │   ├── CraftProduct.java      # Product Entity
│       │   └── Order.java             # Order Entity
│       ├── payment/                   # Interfaces & Polymorphism
│       │   ├── PaymentGateway.java    # Interface
│       │   ├── UpiPaymentGateway.java # Implements PaymentGateway
│       │   └── CardPaymentGateway.java# Implements PaymentGateway
│       ├── exception/                 # Custom Exception Hierarchy
│       │   ├── WeavoException.java    # Base Custom Exception
│       │   ├── OutOfStockException.java
│       │   ├── PaymentFailedException.java
│       │   └── InvalidInputException.java
│       ├── concurrency/               # Multithreading & ReentrantLock
│       │   └── InventoryOrderProcessor.java
│       └── fileio/                    # File Read & Write (BufferedReader / BufferedWriter)
│           └── CatalogFileManager.java
│
├── kalasetu-domain/                   # Multi-Module Maven: Domain & Exceptions
├── kalasetu-payment/                  # Multi-Module Maven: Interfaces & Payment
├── kalasetu-concurrency/              # Multi-Module Maven: Multithreading Engine
├── kalasetu-fileio/                   # Multi-Module Maven: File Read/Write Service
├── kalasetu-persistence/             # Multi-Module Maven: Supabase REST & InMemory Repo
└── kalasetu-web/                     # Multi-Module Maven: Spring Boot 3 Controllers & UI
```

---

## Verification & Viva Defense Readiness

1. **How does Weavo use Inheritance?**
   - `User` is an abstract class with encapsulated common attributes (`id`, `fullName`, `email`, `city`). `Customer`, `Artisan`, and `Admin` extend `User` and provide role-specific implementations of abstract methods like `canManageInventory()`.
2. **How does Weavo demonstrate Polymorphism?**
   - The `PaymentGateway` interface defines `processPayment()`. At runtime, the checkout service invokes `processPayment()` without knowing whether it is dealing with `UpiPaymentGateway` or `CardPaymentGateway`.
3. **What custom exceptions exist and why?**
   - `OutOfStockException`, `PaymentFailedException`, and `InvalidInputException` extend `WeavoException`. They encapsulate domain-specific error codes (`ERR_OUT_OF_STOCK`, `ERR_PAYMENT_FAILED`) and metadata (available quantity, failed amount).
4. **How are race conditions handled in multithreading?**
   - `InventoryOrderProcessor` uses `ReentrantLock` around the inventory validation and decrement logic. When multiple threads execute concurrently, the lock guarantees that inventory checking and decrementing happen atomically.
5. **How does Weavo handle file input and output?**
   - `CatalogFileManager` uses `BufferedReader` for high-performance buffered line reading of CSV catalogs, and `BufferedWriter` with try-with-resources for writing official tax receipts and catalog exports.

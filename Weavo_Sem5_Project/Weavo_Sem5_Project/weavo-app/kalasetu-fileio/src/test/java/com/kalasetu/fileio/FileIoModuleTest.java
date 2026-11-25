package com.kalasetu.fileio;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.model.AuditEvent;
import com.kalasetu.domain.model.CraftProduct;
import com.kalasetu.domain.model.Order;
import com.kalasetu.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileIoModuleTest {

    @TempDir
    Path tempDir;

    @Test
    void testCsvExportAndImportRoundTrip() throws Exception {
        CatalogFileService service = new CatalogFileService(tempDir);

        CraftProduct p = new CraftProduct();
        p.setSku("WV-TEST-1");
        p.setTitle("Kutch Rogan Art Wall Panel, Framed");
        p.setCraftType("Rogan Painting");
        p.setOriginVillage("Nirona, Kutch");
        p.setGiCertified(true);
        p.setPriceInr(new BigDecimal("3200.00"));
        p.setStockQty(5);

        Path exported = service.exportCatalogCsv(List.of(p), "catalog.csv");
        assertTrue(Files.exists(exported));

        List<CraftProduct> imported = service.importCatalogCsv(exported);
        assertEquals(1, imported.size());
        assertEquals("WV-TEST-1", imported.get(0).getSku());
        assertEquals("Kutch Rogan Art Wall Panel, Framed", imported.get(0).getTitle());
        assertEquals(new BigDecimal("3200.00"), imported.get(0).getPriceInr());
    }

    @Test
    void testOrderReceiptAndAuditLogAndTraversalBlock() throws Exception {
        CatalogFileService service = new CatalogFileService(tempDir);

        assertThrows(InvalidInputException.class, () -> service.resolveSafe("exports", "../../etc/passwd"));

        Order o = new Order();
        o.setOrderNumber("WV-2026-9999");
        o.setOrderStatus("CONFIRMED");
        o.setPaymentMethod("UPI_TEST");
        o.setPaymentStatus("PAID_TEST");
        o.setTransactionRef("UPI-TEST-1234");
        o.setShippingAddress("Ahmedabad, Gujarat");
        o.setShippingFeeInr(BigDecimal.ZERO);
        o.setPlatformCommissionInr(new BigDecimal("388.00"));
        o.setTotalAmountInr(new BigDecimal("4850.00"));

        OrderItem item = new OrderItem();
        item.setProductTitle("Jamnagar Bandhani Saree");
        item.setQuantity(1);
        item.setUnitPriceInr(new BigDecimal("4850.00"));
        item.setLineTotalInr(new BigDecimal("4850.00"));
        item.setArtisanPayoutInr(new BigDecimal("4462.00"));
        o.setItems(List.of(item));

        Path receipt = service.writeOrderReceipt(o);
        assertTrue(Files.exists(receipt));
        String content = service.readOrderReceipt("WV-2026-9999");
        assertTrue(content.contains("Jamnagar Bandhani Saree"));

        AuditEvent ev = new AuditEvent();
        ev.setActorUserId(UUID.randomUUID());
        ev.setEventType("ORDER_PLACED");
        ev.setEntityType("ORDER");
        ev.setEntityId("WV-2026-9999");
        service.appendAuditLog(ev);
        assertEquals(1, service.readRecentAuditLines(10).size());
    }
}

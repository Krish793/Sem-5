package com.kalasetu.persistence;

import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.model.CraftProduct;
import com.kalasetu.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceModuleTest {

    @Test
    void testInMemoryRepositoryCatalogSearchAndAtomicStock() throws OutOfStockException {
        InMemoryMarketplaceRepository repo = new InMemoryMarketplaceRepository();
        assertEquals("IN_MEMORY_FALLBACK", repo.getBackendMode());

        List<CraftProduct> products = repo.findActiveProducts();
        assertTrue(products.size() >= 2);

        List<CraftProduct> patola = repo.searchProducts("Patola", null, true);
        assertEquals(1, patola.size());
        UUID pid = patola.get(0).getId();
        int initialStock = patola.get(0).getStockQty();

        int remaining = repo.decrementStockAtomic(pid, 2);
        assertEquals(initialStock - 2, remaining);

        assertThrows(OutOfStockException.class, () -> repo.decrementStockAtomic(pid, 100));

        Optional<User> demoUser = repo.findUserByEmail("aarav.buyer@weavo.demo");
        assertTrue(demoUser.isPresent());
    }
}

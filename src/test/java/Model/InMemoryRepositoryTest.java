package Model;

import Repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    private InMemoryRepository repo;

    // Runs before EVERY @Test method, giving each test a clean, empty repository.
    // This guarantees tests can't leak data into each other or depend on run order.
    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository();
    }

    // ---------- save() ----------

    @Test
    void save_thenFindById_returnsSameProduct() {
        Product p = new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10);
        repo.save(p);

        Product found = repo.findById(1L);

        assertNotNull(found);
        assertEquals("Widget", found.getName());
        assertEquals("Tools", found.getCategory());
        assertEquals("Hand Tools", found.getSubCategory());
        assertEquals(9.99, found.getPrice());
        assertEquals(10, found.getQuantity());
    }

    @Test
    void save_withDuplicateId_overwritesExistingProduct() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));
        repo.save(new Product(1L, "Widget V2", "Tools", "Hand Tools", 12.99, 20));

        Product found = repo.findById(1L);

        assertEquals("Widget V2", found.getName());
        assertEquals(20, found.getQuantity());
        assertEquals(1, repo.findAll().size()); // still only one product, not two
    }

    // ---------- findAll() ----------

    @Test
    void findAll_onEmptyRepository_returnsEmptyList() {
        List<Product> all = repo.findAll();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void findAll_returnsAllSavedProducts() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));
        repo.save(new Product(2L, "Gadget", "Electronics", "Accessories", 19.99, 5));

        List<Product> all = repo.findAll();

        assertEquals(2, all.size());
    }

    // ---------- findById() ----------

    @Test
    void findById_withUnknownId_returnsNull() {
        assertNull(repo.findById(99L));
    }

    // ---------- deleteById() ----------

    @Test
    void deleteById_removesProduct() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.deleteById(1L);

        assertNull(repo.findById(1L));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteById_withUnknownId_doesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999L));
    }

    @Test
    void deleteById_doesNotAffectOtherProducts() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));
        repo.save(new Product(2L, "Gadget", "Electronics", "Accessories", 19.99, 5));

        repo.deleteById(1L);

        assertNull(repo.findById(1L));
        assertNotNull(repo.findById(2L));
        assertEquals(1, repo.findAll().size());
    }

    // ---------- addQuantity() ----------

    @Test
    void addQuantity_increasesStock() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.addQuantity(1L, 5);

        assertEquals(15, repo.findById(1L).getQuantity());
    }

    @Test
    void addQuantity_withUnknownId_doesNotThrow() {
        assertDoesNotThrow(() -> repo.addQuantity(999L, 5));
    }

    @Test
    void addQuantity_withZero_leavesQuantityUnchanged() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.addQuantity(1L, 0);

        assertEquals(10, repo.findById(1L).getQuantity());
    }

    // ---------- removeQuantity() ----------

    @Test
    void removeQuantity_decreasesStock() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.removeQuantity(1L, 5);

        assertEquals(5, repo.findById(1L).getQuantity());
    }

    @Test
    void removeQuantity_withUnknownId_doesNotThrow() {
        assertDoesNotThrow(() -> repo.removeQuantity(999L, 5));
    }

    @Test
    void removeQuantity_moreThanAvailable_doesNotGoNegative() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.removeQuantity(1L, 100);

        // Quantity should be unchanged (blocked), never negative
        assertEquals(10, repo.findById(1L).getQuantity());
    }

    @Test
    void removeQuantity_exactAmountAvailable_bringsStockToZero() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.removeQuantity(1L, 10);

        assertEquals(0, repo.findById(1L).getQuantity());
    }
}
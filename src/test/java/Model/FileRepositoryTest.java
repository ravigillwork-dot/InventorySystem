package Model;

import Repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileRepositoryTest {
    @TempDir
    Path tempDir;
    private FileRepository repo;

    @BeforeEach
    void setUp(){
        Path testfile = tempDir.resolve("filerepotest.json");
        repo = new FileRepository(testfile.toString());
    }

    @Test
    void save_thenFindById_returnSameProduct(){
        Product p = new Product(3L, "Ravi Mouse", "Peripheral", "Mouse", 29.99 , 3);
        repo.save(p);

        Product found = repo.findById(3L);

        assertNotNull(found);
        assertEquals("Ravi Mouse", found.getName());
        assertEquals(3, found.getQuantity());
    }

    @Test
    void findById_withUnknownId_returnsNull(){
        assertNull(repo.findById(99L));
    }

    @Test
    void findAllOnEmptyFile_returnsNull(){
        List<Product> all = repo.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void findAll_returnAllSaved(){
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));
        repo.save(new Product(2L, "Gadget", "Electronics", "Accessories", 19.99, 5));

        List<Product> all = repo.findAll();

        assertEquals(2, all.size());

        Product a = repo.findById(1L);
        Product b = repo.findById(2L);

        assertEquals("Widget", a.getName());
        assertEquals(9.99,a.getPrice());
        assertEquals("Accessories", b.getSubCategory());
        assertEquals("Electronics", b.getCategory());
    }
    @Test
    void deleteById_removesProduct() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));
        repo.deleteById(1L);
        assertNull(repo.findById(1L));
    }
    @Test
    void deleteById_withUnknownId_doesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999L));
    }
    @Test
    void addQuantity_increasesStock() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.addQuantity(1L, 5);

        assertEquals(15, repo.findById(1L).getQuantity());
    }

    @Test
    void addQuantity_withNegativeAmount_isRejected() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.addQuantity(1L, -5);

        assertEquals(10, repo.findById(1L).getQuantity());
    }

    @Test
    void removeQuantity_decreasesStock() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.removeQuantity(1L, 5);

        assertEquals(5, repo.findById(1L).getQuantity());
    }

    @Test
    void removeQuantity_moreThanAvailable_doesNotGoNegative() {
        repo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        repo.removeQuantity(1L, 100);

        assertEquals(10, repo.findById(1L).getQuantity());
    }

    @Test
    void dataPersists_acrossSeperateRepositoryInstances(){
        Path testfile = tempDir.resolve("persisttest.json");
        FileRepository firstrepo = new FileRepository(testfile.toString());
        firstrepo.save(new Product(1L, "Widget", "Tools", "Hand Tools", 9.99, 10));

        FileRepository secondrepo = new FileRepository(testfile.toString());
        Product check = secondrepo.findById(1L);

        assertNotNull(check);
        assertEquals(9.99, check.getPrice());
    }

}

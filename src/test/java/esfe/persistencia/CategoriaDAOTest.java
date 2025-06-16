package esfe.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach para limpieza
import esfe.dominio.Categoria;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List; // Importar List
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaDAOTest {
    private CategoriaDAO categoriaDAO;
    private Categoria testCategoria; // Objeto para el usuario de prueba

    @BeforeEach
    void setUp() {
        categoriaDAO = new CategoriaDAO();
        // Generar una categoría de prueba única para cada test
        Random rand = new Random();
        int uniqueNum = rand.nextInt(100000);
        testCategoria = new Categoria(0, "CategoriaTest" + uniqueNum, "DescripcionTest" + uniqueNum);
    }

    @AfterEach
    void tearDown() {
        // Limpiar la categoría de prueba después de cada test si fue creada y tiene un ID
        cleanUpTestCategoria(testCategoria);
    }

    // --- Métodos Auxiliares para Pruebas (Refinados) ---

    /**
     * Crea una categoría en la base de datos y realiza aserciones para verificar su creación.
     * @param categoria La categoría a crear.
     * @return La categoría creada con su ID.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Categoria createAndAssert(Categoria categoria) throws SQLException {
        Categoria res = categoriaDAO.create(categoria);
        assertNotNull(res, "La categoría creada no debería ser nula.");
        assertTrue(res.getCategoriaID() > 0, "El ID de la categoría creada debería ser mayor que 0.");
        assertEquals(categoria.getNombre(), res.getNombre(), "El nombre de la categoría creada debe coincidir.");
        assertEquals(categoria.getDescripcion(), res.getDescripcion(), "La descripción de la categoría creada debe coincidir.");
        return res;
    }

    /**
     * Actualiza una categoría en la base de datos y realiza aserciones para verificar la actualización.
     * @param categoriaToUpdate La categoría a actualizar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void updateAndAssert(Categoria categoriaToUpdate) throws SQLException {
        categoriaToUpdate.setNombre(categoriaToUpdate.getNombre() + "_editado");
        categoriaToUpdate.setDescripcion(categoriaToUpdate.getDescripcion() + "_editado");

        boolean updated = categoriaDAO.update(categoriaToUpdate);
        assertTrue(updated, "La actualización de la categoría debería ser exitosa.");

        // Verificar los cambios recuperando la categoría de la DB
        Categoria fetchedCategoria = categoriaDAO.getById(categoriaToUpdate.getCategoriaID());
        assertNotNull(fetchedCategoria, "La categoría actualizada no debería ser nula al recuperarla.");
        assertEquals(categoriaToUpdate.getNombre(), fetchedCategoria.getNombre(), "El nombre de la categoría debería haber sido actualizado.");
        assertEquals(categoriaToUpdate.getDescripcion(), fetchedCategoria.getDescripcion(), "La descripción de la categoría debería haber sido actualizada.");
    }

    /**
     * Obtiene una categoría por ID y realiza aserciones para verificar su consistencia.
     * @param expectedCategoria La categoría esperada.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void getByIdAndAssert(Categoria expectedCategoria) throws SQLException {
        Categoria foundCategoria = categoriaDAO.getById(expectedCategoria.getCategoriaID());
        assertNotNull(foundCategoria, "No se encontró la categoría por ID: " + expectedCategoria.getCategoriaID());
        assertEquals(expectedCategoria.getCategoriaID(), foundCategoria.getCategoriaID(), "El ID de la categoría obtenida debe ser igual al esperado.");
        assertEquals(expectedCategoria.getNombre(), foundCategoria.getNombre(), "El nombre de la categoría obtenida debe ser igual al esperado.");
        assertEquals(expectedCategoria.getDescripcion(), foundCategoria.getDescripcion(), "La descripción de la categoría obtenida debe ser igual a la esperada.");
    }

    /**
     * Busca categorías por nombre y realiza aserciones sobre los resultados.
     * @param query La cadena de búsqueda.
     * @param expectedCategoriaInResults La categoría que se espera encontrar en los resultados.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void searchAndAssert(String query, Categoria expectedCategoriaInResults) throws SQLException {
        // CORREGIDO: Usar List<Categoria> para el resultado de categoriaDAO.search()
        List<Categoria> results = categoriaDAO.search(query);
        assertFalse(results.isEmpty(), "La búsqueda debería encontrar al menos un resultado para '" + query + "'.");

        boolean foundSpecificCategoria = false;
        for (Categoria cat : results) {
            if (cat.getCategoriaID() == expectedCategoriaInResults.getCategoriaID()) {
                foundSpecificCategoria = true;
                break; // Salir del bucle si encontramos la categoría específica
            }
        }
        assertTrue(foundSpecificCategoria, "La categoría de prueba '" + expectedCategoriaInResults.getNombre() + "' no fue encontrada en los resultados de la búsqueda.");
    }

    /**
     * Elimina una categoría de la base de datos y realiza aserciones para verificar la eliminación.
     * @param categoriaToDelete La categoría a eliminar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void deleteAndAssert(Categoria categoriaToDelete) throws SQLException {
        boolean deleted = categoriaDAO.delete(categoriaToDelete.getCategoriaID());
        assertTrue(deleted, "La eliminación de la categoría debería ser exitosa.");

        Categoria fetchedCategoriaAfterDelete = categoriaDAO.getById(categoriaToDelete.getCategoriaID());
        assertNull(fetchedCategoriaAfterDelete, "La categoría debería ser nula después de la eliminación.");
    }

    /**
     * Método auxiliar para limpiar la base de datos de categorías de prueba.
     * Asegura que los datos creados durante el test se eliminen.
     * @param categoria La categoría a limpiar.
     */
    private void cleanUpTestCategoria(Categoria categoria) {
        try {
            // Solo intentar eliminar si la categoría no es nula y tiene un ID válido
            if (categoria != null && categoria.getCategoriaID() > 0) {
                categoriaDAO.delete(categoria.getCategoriaID());
            }
        } catch (SQLException e) {
            System.err.println("Error al limpiar categoría de prueba con ID " + (categoria != null ? categoria.getCategoriaID() : "null") + ": " + e.getMessage());
        }
    }

    // ---------------------- TEST CASES (@Test methods) ----------------------

    @Test
    void testCreateCategoria() throws SQLException {
        // La categoría de prueba se crea en setUp()
        Categoria createdCategoria = createAndAssert(testCategoria);
        testCategoria.setCategoriaID(createdCategoria.getCategoriaID()); // Guardar el ID generado para limpieza en tearDown
    }

    @Test
    void testUpdateCategoria() throws SQLException {
        // Crear una categoría para actualizar
        Categoria createdCategoria = createAndAssert(testCategoria);
        testCategoria.setCategoriaID(createdCategoria.getCategoriaID()); // Guardar ID para tearDown

        updateAndAssert(createdCategoria);
    }

    @Test
    void testGetByIdCategoria() throws SQLException {
        // Crear una categoría para obtener por ID
        Categoria createdCategoria = createAndAssert(testCategoria);
        testCategoria.setCategoriaID(createdCategoria.getCategoriaID()); // Guardar ID para tearDown

        getByIdAndAssert(createdCategoria);
    }

    @Test
    void testSearchCategoriaByName() throws SQLException {
        // Crear categorías específicas para la prueba de búsqueda
        Random rand = new Random();
        int uniqueNum1 = rand.nextInt(100000) + 100000; // Para asegurar la unicidad entre tests
        int uniqueNum2 = rand.nextInt(100000) + 200000;

        Categoria searchCat1 = new Categoria(0, "CategoriaBusqueda" + uniqueNum1, "Desc1");
        Categoria searchCat2 = new Categoria(0, "BusquedaEspecifica" + uniqueNum2, "Desc2");

        Categoria createdSearchCat1 = createAndAssert(searchCat1);
        Categoria createdSearchCat2 = createAndAssert(searchCat2);

        try {
            // Probar búsqueda parcial
            searchAndAssert("Busqueda", createdSearchCat1);
            searchAndAssert("Busqueda", createdSearchCat2);

            // Probar búsqueda exacta (que también es una búsqueda parcial)
            searchAndAssert(createdSearchCat1.getNombre(), createdSearchCat1);

            // Probar búsqueda de un nombre que no existe
            List<Categoria> noResults = categoriaDAO.search("NoExiste" + rand.nextInt(1000));
            assertTrue(noResults.isEmpty(), "No debería encontrar resultados para una búsqueda inexistente.");

        } finally {
            // Asegurarse de limpiar estas categorías específicas del test
            cleanUpTestCategoria(createdSearchCat1);
            cleanUpTestCategoria(createdSearchCat2);
        }
    }

    @Test
    void testDeleteCategoria() throws SQLException {
        // Crear una categoría para eliminar
        Categoria createdCategoria = createAndAssert(testCategoria);
        testCategoria.setCategoriaID(createdCategoria.getCategoriaID()); // Guardar ID para tearDown

        deleteAndAssert(createdCategoria);
        // cleanUpTestCategoria(testCategoria) en tearDown ya no es estrictamente necesario aquí
        // porque deleteAndAssert ya verificó la eliminación, pero se mantiene para robustez general.
    }

    @Test
    void testGetAllCategorias() throws SQLException {
        // Crear algunas categorías para asegurar que getAllCategorias funcione
        Random rand = new Random();
        Categoria cat1 = createAndAssert(new Categoria(0, "AllCat1" + rand.nextInt(1000), "DescA"));
        Categoria cat2 = createAndAssert(new Categoria(0, "AllCat2" + rand.nextInt(1000), "DescB"));

        try {
            List<Categoria> allCategorias = categoriaDAO.getAllCategorias(); // Asume que este método existe
            assertNotNull(allCategorias, "La lista de todas las categorías no debería ser nula.");
            assertTrue(allCategorias.size() >= 2, "Debería haber al menos 2 categorías después de la creación.");

            // Verificar que las categorías creadas estén en la lista
            assertTrue(allCategorias.stream().anyMatch(c -> c.getCategoriaID() == cat1.getCategoriaID()), "Debería contener la categoría 1.");
            assertTrue(allCategorias.stream().anyMatch(c -> c.getCategoriaID() == cat2.getCategoriaID()), "Debería contener la categoría 2.");
        } finally {
            cleanUpTestCategoria(cat1);
            cleanUpTestCategoria(cat2);
        }
    }
}
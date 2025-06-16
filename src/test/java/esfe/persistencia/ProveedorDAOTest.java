package esfe.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach para limpieza
import esfe.dominio.Proveedor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List; // Importar List
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5

class ProveedorDAOTest {
    private ProveedorDAO proveedorDAO; // Instancia de ProveedorDAO
    private Proveedor testProveedor; // Objeto para el proveedor de prueba

    @BeforeEach
    void setUp() {
        proveedorDAO = new ProveedorDAO();
        // Generar un proveedor de prueba único para cada test
        Random rand = new Random();
        int uniqueNum = rand.nextInt(100000);
        testProveedor = new Proveedor(0, "ProveedorTest" + uniqueNum, "Tel" + uniqueNum, "email" + uniqueNum + "@test.com", "Dir" + uniqueNum);
    }

    @AfterEach
    void tearDown() {
        // Limpiar el proveedor de prueba después de cada test si fue creado y tiene un ID
        cleanUpTestProveedor(testProveedor);
    }

    // --- Métodos Auxiliares para Pruebas (Refinados) ---

    /**
     * Crea un proveedor en la base de datos y realiza aserciones para verificar su creación.
     * @param proveedor La instancia de Proveedor a crear.
     * @return La instancia de Proveedor creada con su ID generado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Proveedor createAndAssert(Proveedor proveedor) throws SQLException {
        Proveedor res = proveedorDAO.create(proveedor);
        assertNotNull(res, "El proveedor creado no debería ser nulo.");
        assertTrue(res.getProveedorID() > 0, "El ID del proveedor creado debería ser mayor que 0.");
        assertEquals(proveedor.getNombre(), res.getNombre(), "El nombre del proveedor creado debe coincidir.");
        assertEquals(proveedor.getTelefono(), res.getTelefono(), "El teléfono del proveedor creado debe coincidir.");
        assertEquals(proveedor.getEmail(), res.getEmail(), "El email del proveedor creado debe coincidir.");
        assertEquals(proveedor.getDireccion(), res.getDireccion(), "La dirección del proveedor creado debe coincidir.");
        return res;
    }

    /**
     * Actualiza un proveedor en la base de datos y realiza aserciones para verificar la actualización.
     * @param proveedorToUpdate La instancia de Proveedor a actualizar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void updateAndAssert(Proveedor proveedorToUpdate) throws SQLException {
        proveedorToUpdate.setNombre(proveedorToUpdate.getNombre() + "_editado");
        proveedorToUpdate.setTelefono("NuevoTel" + new Random().nextInt(1000));
        proveedorToUpdate.setEmail("editado_" + proveedorToUpdate.getEmail());
        proveedorToUpdate.setDireccion(proveedorToUpdate.getDireccion() + "_editado");

        boolean updated = proveedorDAO.update(proveedorToUpdate);
        assertTrue(updated, "La actualización del proveedor debería ser exitosa.");

        // Verificar los cambios recuperando el proveedor de la DB
        Proveedor fetchedProveedor = proveedorDAO.getById(proveedorToUpdate.getProveedorID());
        assertNotNull(fetchedProveedor, "El proveedor actualizado no debería ser nulo al recuperarlo.");
        assertEquals(proveedorToUpdate.getNombre(), fetchedProveedor.getNombre(), "El nombre del proveedor debería haber sido actualizado.");
        assertEquals(proveedorToUpdate.getTelefono(), fetchedProveedor.getTelefono(), "El teléfono del proveedor debería haber sido actualizado.");
        assertEquals(proveedorToUpdate.getEmail(), fetchedProveedor.getEmail(), "El email del proveedor debería haber sido actualizado.");
        assertEquals(proveedorToUpdate.getDireccion(), fetchedProveedor.getDireccion(), "La dirección del proveedor debería haber sido actualizada.");
    }

    /**
     * Obtiene un proveedor por ID de la base de datos y realiza aserciones de consistencia.
     * @param expectedProveedor La instancia de Proveedor esperada.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void getByIdAndAssert(Proveedor expectedProveedor) throws SQLException {
        Proveedor foundProveedor = proveedorDAO.getById(expectedProveedor.getProveedorID());
        assertNotNull(foundProveedor, "No se encontró el proveedor por ID: " + expectedProveedor.getProveedorID());
        assertEquals(expectedProveedor.getProveedorID(), foundProveedor.getProveedorID(), "El ID del proveedor obtenido debe ser igual al esperado.");
        assertEquals(expectedProveedor.getNombre(), foundProveedor.getNombre(), "El nombre del proveedor obtenido debe ser igual al esperado.");
        assertEquals(expectedProveedor.getTelefono(), foundProveedor.getTelefono(), "El teléfono del proveedor obtenido debe ser igual al esperado.");
        assertEquals(expectedProveedor.getEmail(), foundProveedor.getEmail(), "El email del proveedor obtenido debe ser igual al esperado.");
        assertEquals(expectedProveedor.getDireccion(), foundProveedor.getDireccion(), "La dirección del proveedor obtenido debe ser igual a la esperada.");
    }

    /**
     * Busca proveedores por nombre en la base de datos y realiza aserciones sobre los resultados.
     * @param query La cadena de búsqueda.
     * @param expectedProveedorInResults La instancia de Proveedor que se espera encontrar en los resultados.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void searchAndAssert(String query, Proveedor expectedProveedorInResults) throws SQLException {
        // CORREGIDO: Usar List<Proveedor> para el resultado de proveedorDAO.search()
        List<Proveedor> results = proveedorDAO.search(query);
        assertFalse(results.isEmpty(), "La búsqueda debería encontrar al menos un resultado para '" + query + "'.");

        boolean foundSpecificProveedor = false;
        for (Proveedor p : results) {
            if (p.getProveedorID() == expectedProveedorInResults.getProveedorID()) {
                foundSpecificProveedor = true;
                break; // Salir del bucle si encontramos el proveedor específico
            }
        }
        assertTrue(foundSpecificProveedor, "El proveedor de prueba '" + expectedProveedorInResults.getNombre() + "' no fue encontrado en los resultados de la búsqueda.");
    }

    /**
     * Elimina un proveedor de la base de datos y realiza aserciones para verificar la eliminación.
     * @param proveedorToDelete La instancia de Proveedor a eliminar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void deleteAndAssert(Proveedor proveedorToDelete) throws SQLException {
        boolean deleted = proveedorDAO.delete(proveedorToDelete.getProveedorID());
        assertTrue(deleted, "La eliminación del proveedor debería ser exitosa.");

        Proveedor fetchedProveedorAfterDelete = proveedorDAO.getById(proveedorToDelete.getProveedorID());
        assertNull(fetchedProveedorAfterDelete, "El proveedor debería ser nulo después de la eliminación.");
    }

    /**
     * Método auxiliar para limpiar la base de datos de proveedores de prueba.
     * Asegura que los datos creados durante el test se eliminen.
     * @param proveedor La instancia de Proveedor a limpiar.
     */
    private void cleanUpTestProveedor(Proveedor proveedor) {
        try {
            // Solo intentar eliminar si el proveedor no es nulo y tiene un ID válido
            if (proveedor != null && proveedor.getProveedorID() > 0) {
                proveedorDAO.delete(proveedor.getProveedorID());
            }
        } catch (SQLException e) {
            System.err.println("Error al limpiar proveedor de prueba con ID " + (proveedor != null ? proveedor.getProveedorID() : "null") + ": " + e.getMessage());
        }
    }

    // ---------------------- TEST CASES (@Test methods) ----------------------

    @Test
    void testCreateProveedor() throws SQLException {
        // El proveedor de prueba se crea en setUp()
        Proveedor createdProveedor = createAndAssert(testProveedor);
        testProveedor.setProveedorID(createdProveedor.getProveedorID()); // Guardar el ID generado para limpieza en tearDown
    }

    @Test
    void testUpdateProveedor() throws SQLException {
        // Crear un proveedor para actualizar
        Proveedor createdProveedor = createAndAssert(testProveedor);
        testProveedor.setProveedorID(createdProveedor.getProveedorID()); // Guardar ID para tearDown

        updateAndAssert(createdProveedor);
    }

    @Test
    void testGetByIdProveedor() throws SQLException {
        // Crear un proveedor para obtener por ID
        Proveedor createdProveedor = createAndAssert(testProveedor);
        testProveedor.setProveedorID(createdProveedor.getProveedorID()); // Guardar ID para tearDown

        getByIdAndAssert(createdProveedor);
    }

    @Test
    void testSearchProveedorByName() throws SQLException {
        // Crear proveedores específicos para la prueba de búsqueda
        Random rand = new Random();
        int uniqueNum1 = rand.nextInt(100000) + 100000;
        int uniqueNum2 = rand.nextInt(100000) + 200000;

        Proveedor searchProv1 = new Proveedor(0, "ProveedorBusqueda" + uniqueNum1, "TelS1", "s1@test.com", "DirS1");
        Proveedor searchProv2 = new Proveedor(0, "BusquedaEspecifica" + uniqueNum2, "TelS2", "s2@test.com", "DirS2");

        Proveedor createdSearchProv1 = createAndAssert(searchProv1);
        Proveedor createdSearchProv2 = createAndAssert(searchProv2);

        try {
            // Probar búsqueda parcial
            searchAndAssert("Busqueda", createdSearchProv1);
            searchAndAssert("Busqueda", createdSearchProv2);

            // Probar búsqueda exacta (que también es una búsqueda parcial)
            searchAndAssert(createdSearchProv1.getNombre(), createdSearchProv1);

            // Probar búsqueda de un nombre que no existe
            List<Proveedor> noResults = proveedorDAO.search("NoExisteProveedor" + rand.nextInt(1000));
            assertTrue(noResults.isEmpty(), "No debería encontrar resultados para una búsqueda inexistente.");

        } finally {
            // Asegurarse de limpiar estos proveedores específicos del test
            cleanUpTestProveedor(createdSearchProv1);
            cleanUpTestProveedor(createdSearchProv2);
        }
    }

    @Test
    void testDeleteProveedor() throws SQLException {
        // Crear un proveedor para eliminar
        Proveedor createdProveedor = createAndAssert(testProveedor);
        testProveedor.setProveedorID(createdProveedor.getProveedorID()); // Guardar ID para tearDown

        deleteAndAssert(createdProveedor);
        // cleanUpTestProveedor(testProveedor) en tearDown ya no es estrictamente necesario aquí
        // porque deleteAndAssert ya verificó la eliminación, pero se mantiene para robustez general.
    }

    @Test
    void testGetAllProveedores() throws SQLException {
        // Crear algunos proveedores para asegurar que getAllProveedores funcione
        Random rand = new Random();
        Proveedor prov1 = createAndAssert(new Proveedor(0, "AllProv1" + rand.nextInt(1000), "TelA", "a@test.com", "DirA"));
        Proveedor prov2 = createAndAssert(new Proveedor(0, "AllProv2" + rand.nextInt(1000), "TelB", "b@test.com", "DirB"));

        try {
            List<Proveedor> allProveedores = proveedorDAO.getAllProveedores(); // Asume que este método existe en ProveedorDAO
            assertNotNull(allProveedores, "La lista de todos los proveedores no debería ser nula.");
            assertTrue(allProveedores.size() >= 2, "Debería haber al menos 2 proveedores después de la creación.");

            // Verificar que los proveedores creados estén en la lista
            assertTrue(allProveedores.stream().anyMatch(p -> p.getProveedorID() == prov1.getProveedorID()), "Debería contener el proveedor 1.");
            assertTrue(allProveedores.stream().anyMatch(p -> p.getProveedorID() == prov2.getProveedorID()), "Debería contener el proveedor 2.");
        } finally {
            cleanUpTestProveedor(prov1);
            cleanUpTestProveedor(prov2);
        }
    }
}
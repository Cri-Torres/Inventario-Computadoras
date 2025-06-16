package esfe.persistencia;

import esfe.dominio.Computadora;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach para limpieza
import org.junit.jupiter.api.DisplayName; // Opcional: para nombres más descriptivos en los tests

import java.sql.SQLException;
import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.ArrayList;
import java.util.List; // Importar List
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ComputadoraDAOTest {
    private ComputadoraDAO computadoraDAO;
    private ArrayList<Integer> idsCreadosParaLimpieza; // Para almacenar IDs y limpiarlos después

    @BeforeEach
    void setUp() {
        computadoraDAO = new ComputadoraDAO();
        idsCreadosParaLimpieza = new ArrayList<>(); // Inicializar la lista de IDs a limpiar
        // NOTA: Asegúrate de que las tablas Categorias y Proveedores tengan datos (ej. ID=1)
        // para que las claves foráneas en la creación de Computadora no fallen.
        // Si no existen, estos tests pueden fallar por restricciones de integridad referencial.
    }

    @AfterEach
    void tearDown() {
        // Limpieza: Intentar eliminar todas las computadoras creadas durante el test
        for (Integer id : idsCreadosParaLimpieza) {
            try {
                if (computadoraDAO.getById(id) != null) { // Solo intentar eliminar si aún existe
                    computadoraDAO.delete(id);
                    System.out.println("Limpiado: Computadora con ID " + id + " eliminada.");
                }
            } catch (SQLException e) {
                System.err.println("Error al limpiar computadora con ID " + id + ": " + e.getMessage());
                // No lanzamos la excepción para que otros tests puedan continuar la limpieza
            }
        }
    }


    /**
     * Método auxiliar para crear y persistir una computadora para los tests.
     * Genera un número de serie único y asigna valores válidos.
     * Almacena el ID para limpieza posterior.
     * @return La computadora creada y persistida con su ID y fecha de compra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Computadora createAndAssertComputadora() throws SQLException {
        Random rand = new Random();
        // Asegura que el número de serie sea único con un timestamp y un número aleatorio
        String numeroSerie = "TEST-SN-" + System.currentTimeMillis() + "-" + rand.nextInt(100000);

        // Usamos el constructor sin ID ni fecha de compra, ya que estos se manejan automáticamente
        // Asegúrate de que CategoriaID y ProveedorID existan en tu base de datos de prueba para evitar fallos de FK.
        Computadora computadoraNueva = new Computadora(
                1, // CategoriaID: Asume que existe una categoría con ID 1 en tu DB de prueba.
                1, // ProveedorID: Asume que existe un proveedor con ID 1 en tu DB de prueba.
                "MarcaTest_" + rand.nextInt(1000), // Nombre de marca único
                "ModeloTest_" + rand.nextInt(1000), // Nombre de modelo único
                numeroSerie,
                850.00,
                "Observaciones de prueba para " + numeroSerie
        );

        // Llamar a create. El DAO asignará automáticamente la fecha y el ID.
        Computadora createdComputadora = computadoraDAO.create(computadoraNueva);

        assertNotNull(createdComputadora, "La computadora creada no debería ser nula.");
        assertTrue(createdComputadora.getComputadoraID() > 0, "El ID de la computadora debe ser generado y mayor que 0.");
        assertEquals(computadoraNueva.getMarca(), createdComputadora.getMarca(), "Las marcas deben coincidir.");
        assertEquals(computadoraNueva.getModelo(), createdComputadora.getModelo(), "Los modelos deben coincidir.");
        assertEquals(computadoraNueva.getNumeroSerie(), createdComputadora.getNumeroSerie(), "Los números de serie deben coincidir.");
        assertEquals(computadoraNueva.getPrecio(), createdComputadora.getPrecio(), 0.001, "Los precios deben coincidir.");
        assertEquals(computadoraNueva.getObservaciones(), createdComputadora.getObservaciones(), "Las observaciones deben coincidir.");
        assertEquals(computadoraNueva.getCategoriaID(), createdComputadora.getCategoriaID(), "Los IDs de categoría deben coincidir.");
        assertEquals(computadoraNueva.getProveedorID(), createdComputadora.getProveedorID(), "Los IDs de proveedor deben coincidir.");


        // Almacenar el ID para limpieza posterior en AfterEach
        idsCreadosParaLimpieza.add(createdComputadora.getComputadoraID());

        return createdComputadora;
    }

    /**
     * Método auxiliar para actualizar una computadora y verificar la actualización.
     * @param computadoraOriginal La computadora a actualizar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void updateAndAssertComputadora(Computadora computadoraOriginal) throws SQLException {
        // Modifica algunos atributos
        String nuevaMarca = "HP_Actualizada_" + new Random().nextInt(1000);
        String nuevoModelo = "ProBook_Actualizado_" + new Random().nextInt(1000);
        computadoraOriginal.setMarca(nuevaMarca);
        computadoraOriginal.setModelo(nuevoModelo);
        computadoraOriginal.setEstado(Computadora.ESTADO_AGOTADO); // Cambiar estado
        computadoraOriginal.setPrecio(1200.50); // Cambiar precio
        computadoraOriginal.setObservaciones("Obs. Actualizadas"); // Cambiar observaciones

        boolean updated = computadoraDAO.update(computadoraOriginal);
        assertTrue(updated, "La actualización debería ser exitosa.");

        // Recuperar la computadora actualizada de la base de datos
        Computadora updatedComputadora = computadoraDAO.getById(computadoraOriginal.getComputadoraID());
        assertNotNull(updatedComputadora, "La computadora actualizada no debería ser nula.");
        assertEquals(nuevaMarca, updatedComputadora.getMarca(), "La marca actualizada debe coincidir.");
        assertEquals(nuevoModelo, updatedComputadora.getModelo(), "El modelo actualizado debe coincidir.");
        assertEquals(Computadora.ESTADO_AGOTADO, updatedComputadora.getEstado(), "El estado actualizado debe coincidir.");
        assertEquals(1200.50, updatedComputadora.getPrecio(), 0.001, "El precio actualizado debe coincidir.");
        assertEquals("Obs. Actualizadas", updatedComputadora.getObservaciones(), "Las observaciones actualizadas deben coincidir.");
        // La fecha de compra no debería cambiar en un update a menos que lo especifiquemos explícitamente en el DAO
        assertEquals(computadoraOriginal.getFechaCompra(), updatedComputadora.getFechaCompra(), "La fecha de compra no debería cambiar en el update.");
    }

    /**
     * Método auxiliar para eliminar una computadora y verificar la eliminación.
     * @param idComputadoraAEliminar El ID de la computadora a eliminar.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void deleteAndAssertComputadora(int idComputadoraAEliminar) throws SQLException {
        boolean deleted = computadoraDAO.delete(idComputadoraAEliminar);
        assertTrue(deleted, "La eliminación debería ser exitosa.");

        // Intentar obtenerla de nuevo para confirmar que no existe
        Computadora deletedComputadora = computadoraDAO.getById(idComputadoraAEliminar);
        assertNull(deletedComputadora, "La computadora debería haber sido eliminada y no encontrada.");
    }

    /**
     * Método auxiliar para buscar computadoras y verificar los resultados.
     * @param query La cadena de búsqueda (marca, modelo o número de serie).
     * @param expectedComputadoraInResults La computadora que se espera encontrar en los resultados.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void searchAndAssertComputadora(String query, Computadora expectedComputadoraInResults) throws SQLException {
        // CORREGIDO: Usar List<Computadora> para el resultado de computadoraDAO.search()
        List<Computadora> foundComputadoras = computadoraDAO.search(query);
        assertFalse(foundComputadoras.isEmpty(), "La búsqueda por '" + query + "' debería devolver al menos una computadora.");
        assertTrue(foundComputadoras.stream().anyMatch(c -> c.getComputadoraID() == expectedComputadoraInResults.getComputadoraID()), "Debería encontrar la computadora esperada en los resultados de búsqueda.");
    }

    /**
     * Método auxiliar para obtener una computadora por ID y verificar su consistencia.
     * @param expectedComputadora La computadora que se espera obtener.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void getByIdAndAssertComputadora(Computadora expectedComputadora) throws SQLException {
        Computadora foundComputadora = computadoraDAO.getById(expectedComputadora.getComputadoraID());
        assertNotNull(foundComputadora, "La computadora debería ser encontrada por ID.");
        assertEquals(expectedComputadora.getComputadoraID(), foundComputadora.getComputadoraID(), "Los IDs deben coincidir.");
        assertEquals(expectedComputadora.getMarca(), foundComputadora.getMarca(), "La marca debe coincidir.");
        assertEquals(expectedComputadora.getModelo(), foundComputadora.getModelo(), "El modelo debe coincidir.");
        assertEquals(expectedComputadora.getNumeroSerie(), foundComputadora.getNumeroSerie(), "El número de serie debe coincidir.");
        assertNotNull(foundComputadora.getFechaCompra(), "La fecha de compra no debe ser nula.");
        assertEquals(expectedComputadora.getFechaCompra(), foundComputadora.getFechaCompra(), "La fecha de compra debe coincidir.");
        assertEquals(expectedComputadora.getPrecio(), foundComputadora.getPrecio(), 0.001, "El precio debe coincidir.");
        assertEquals(expectedComputadora.getEstado(), foundComputadora.getEstado(), "El estado debe coincidir.");
        assertEquals(expectedComputadora.getObservaciones(), foundComputadora.getObservaciones(), "Las observaciones deben coincidir.");
        assertEquals(expectedComputadora.getCategoriaID(), foundComputadora.getCategoriaID(), "Los IDs de categoría deben coincidir.");
        assertEquals(expectedComputadora.getProveedorID(), foundComputadora.getProveedorID(), "Los IDs de proveedor deben coincidir.");
    }


    // ---------------------- TEST CASES (@Test methods) ----------------------

    @Test
    @DisplayName("Test: Crear una nueva computadora y verificar ID y fecha automática")
    void testCreate() throws SQLException {
        Computadora computadora = createAndAssertComputadora();
        // Ya se verificó el ID > 0 y otros atributos en createAndAssertComputadora
        assertNotNull(computadora.getFechaCompra(), "La fecha de compra no debe ser nula.");
        // Opcional: Verifica que la fecha esté cerca de la hora actual (margen de 5 segundos)
        assertTrue(computadora.getFechaCompra().isAfter(LocalDateTime.now().minusSeconds(5)), "La fecha de compra debe ser reciente.");
        assertTrue(computadora.getFechaCompra().isBefore(LocalDateTime.now().plusSeconds(5)), "La fecha de compra debe ser reciente.");
    }

    @Test
    @DisplayName("Test: Actualizar una computadora existente")
    void testUpdate() throws SQLException {
        Computadora computadoraOriginal = createAndAssertComputadora(); // Crea una para actualizar
        updateAndAssertComputadora(computadoraOriginal);
    }

    @Test
    @DisplayName("Test: Eliminar una computadora")
    void testDelete() throws SQLException {
        Computadora computadoraAEliminar = createAndAssertComputadora(); // Crea una para eliminar
        deleteAndAssertComputadora(computadoraAEliminar.getComputadoraID());
    }

    @Test
    @DisplayName("Test: Buscar computadoras por marca, modelo o número de serie")
    void testSearch() throws SQLException {
        Random rand = new Random();
        String uniqueMarca = "SearchBrand" + rand.nextInt(1000);
        String uniqueModelo = "SearchModel" + rand.nextInt(1000);
        String uniqueSN = "SEARCH-SN-" + System.currentTimeMillis() + "-" + rand.nextInt(1000);

        // Crear computadoras específicas para la prueba de búsqueda
        Computadora comp1 = new Computadora(1, 1, uniqueMarca, uniqueModelo, uniqueSN, 900.0, "Test search 1");
        comp1 = computadoraDAO.create(comp1);
        idsCreadosParaLimpieza.add(comp1.getComputadoraID()); // Asegura limpieza

        Computadora comp2 = new Computadora(1, 1, "Otra" + uniqueMarca, "Otro" + uniqueModelo, "Otro" + uniqueSN, 950.0, "Test search 2");
        comp2 = computadoraDAO.create(comp2);
        idsCreadosParaLimpieza.add(comp2.getComputadoraID()); // Asegura limpieza

        // Test de búsqueda por marca
        searchAndAssertComputadora(uniqueMarca, comp1);
        searchAndAssertComputadora(uniqueMarca, comp2); // También debería encontrar la otra si su marca contiene el query

        // Test de búsqueda por modelo
        searchAndAssertComputadora(uniqueModelo, comp1);
        searchAndAssertComputadora(uniqueModelo, comp2);

        // Test de búsqueda por número de serie
        searchAndAssertComputadora(uniqueSN, comp1);
        searchAndAssertComputadora(uniqueSN, comp2);

        // Test de búsqueda que no debería devolver resultados
        List<Computadora> notFound = computadoraDAO.search("NonExistentBrand123" + rand.nextInt(1000));
        assertTrue(notFound.isEmpty(), "La búsqueda de una marca inexistente no debería devolver resultados.");
    }

    @Test
    @DisplayName("Test: Obtener una computadora por su ID")
    void testGetById() throws SQLException {
        Computadora computadoraCreada = createAndAssertComputadora(); // Crea una para buscar
        getByIdAndAssertComputadora(computadoraCreada);
    }

    @Test
    @DisplayName("Test: Obtener todas las computadoras")
    void testGetAllComputadoras() throws SQLException {
        // Crear algunas computadoras para asegurar que getAllComputadoras funcione
        Computadora comp1 = createAndAssertComputadora();
        Computadora comp2 = createAndAssertComputadora();

        List<Computadora> allComputadoras = computadoraDAO.getAllComputadoras(); // Asume que este método existe
        assertNotNull(allComputadoras, "La lista de todas las computadoras no debería ser nula.");
        assertTrue(allComputadoras.size() >= 2, "Debería haber al menos 2 computadoras después de la creación para este test.");

        // Verificar que las computadoras creadas estén en la lista
        assertTrue(allComputadoras.stream().anyMatch(c -> c.getComputadoraID() == comp1.getComputadoraID()), "Debería contener la computadora 1.");
        assertTrue(allComputadoras.stream().anyMatch(c -> c.getComputadoraID() == comp2.getComputadoraID()), "Debería contener la computadora 2.");
    }
}
package esfe.persistencia;

import esfe.dominio.Computadora;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach para limpieza
import org.junit.jupiter.api.DisplayName; // Opcional: para nombres más descriptivos en los tests

import java.sql.SQLException;
import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ComputadoraDAOTest {
    private ComputadoraDAO computadoraDAO;
    private ArrayList<Integer> idsCreadosParaLimpieza; // Para almacenar IDs y limpiarlos después

    @BeforeEach
    void setUp() {
        computadoraDAO = new ComputadoraDAO();
        idsCreadosParaLimpieza = new ArrayList<>(); // Inicializar la lista de IDs a limpiar
        // Asegúrate de que las tablas Categorias y Proveedores tengan datos para que los FK no fallen.
        // Por ejemplo, CategoriaID = 1 y ProveedorID = 1.
        // Si no existen, estos tests fallarán.
    }

    @AfterEach
    void tearDown() {
        // Limpieza: Intentar eliminar todas las computadoras creadas durante el test
        for (Integer id : idsCreadosParaLimpieza) {
            try {
                computadoraDAO.delete(id);
                System.out.println("Limpiado: Computadora con ID " + id + " eliminada.");
            } catch (SQLException e) {
                System.err.println("Error al limpiar computadora con ID " + id + ": " + e.getMessage());
                // No lanzamos la excepción para que otros tests puedan continuar la limpieza
            }
        }
        // Desconectar la conexión si ConnectionManager lo permite/requiere explícitamente al final de cada test
        // Aunque ConnectionManager.getInstance() suele manejarla, es buena práctica si hay un método close en ConnectionManager
        // try {
        //    if (computadoraDAO != null && computadoraDAO.getConnection() != null && !computadoraDAO.getConnection().isClosed()) {
        //        computadoraDAO.getConnection().close();
        //    }
        // } catch (SQLException e) {
        //    System.err.println("Error al cerrar conexión en tearDown: " + e.getMessage());
        // }
    }


    /**
     * Helper method para crear y persistir una computadora para los tests.
     * Genera un número de serie único y asigna valores válidos.
     * Almacena el ID para limpieza posterior.
     */
    private Computadora createComputadoraParaTest() throws SQLException {
        Random rand = new Random();
        // Asegura que el número de serie sea único con un timestamp y un número aleatorio
        String numeroSerie = "TEST-SN-" + System.currentTimeMillis() + "-" + rand.nextInt(100000);

        // Usamos el constructor sin ID ni fecha de compra, ya que estos se manejan automáticamente
        // Asegúrate de que CategoriaID y ProveedorID existan en tu base de datos para evitar fallos de FK.
        Computadora computadoraNueva = new Computadora(
                1, // CategoriaID: Asegúrate de que exista una categoría con ID 1 en tu DB de prueba.
                1, // ProveedorID: Asegúrate de que exista un proveedor con ID 1 en tu DB de prueba.
                "MarcaTest",
                "ModeloTest",
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

        // Almacenar el ID para limpieza posterior en AfterEach
        idsCreadosParaLimpieza.add(createdComputadora.getComputadoraID());

        return createdComputadora;
    }

    @Test
    @DisplayName("Test: Crear una nueva computadora y verificar ID y fecha automática")
    void testCreate() throws SQLException {
        Computadora computadora = createComputadoraParaTest();
        // Ya se verificó el ID > 0 en createComputadoraParaTest
        assertNotNull(computadora.getFechaCompra(), "La fecha de compra no debe ser nula.");
        // Opcional: Verifica que la fecha esté cerca de la hora actual
        assertTrue(computadora.getFechaCompra().isAfter(LocalDateTime.now().minusSeconds(5)), "La fecha de compra debe ser reciente.");
        assertTrue(computadora.getFechaCompra().isBefore(LocalDateTime.now().plusSeconds(5)), "La fecha de compra debe ser reciente.");
    }

    @Test
    @DisplayName("Test: Actualizar una computadora existente")
    void testUpdate() throws SQLException {
        Computadora computadoraOriginal = createComputadoraParaTest(); // Crea una para actualizar

        // Modifica algunos atributos
        String nuevaMarca = "HP_Actualizada";
        String nuevoModelo = "ProBook_Actualizado";
        computadoraOriginal.setMarca(nuevaMarca);
        computadoraOriginal.setModelo(nuevoModelo);
        computadoraOriginal.setEstado(Computadora.ESTADO_AGOTADO); // Cambiar estado

        boolean updated = computadoraDAO.update(computadoraOriginal);
        assertTrue(updated, "La actualización debería ser exitosa.");

        // Recuperar la computadora actualizada de la base de datos
        Computadora updatedComputadora = computadoraDAO.getById(computadoraOriginal.getComputadoraID());
        assertNotNull(updatedComputadora, "La computadora actualizada no debería ser nula.");
        assertEquals(nuevaMarca, updatedComputadora.getMarca(), "La marca actualizada debe coincidir.");
        assertEquals(nuevoModelo, updatedComputadora.getModelo(), "El modelo actualizado debe coincidir.");
        assertEquals(Computadora.ESTADO_AGOTADO, updatedComputadora.getEstado(), "El estado actualizado debe coincidir.");
        // La fecha de compra no debería cambiar en un update a menos que lo especifiquemos explícitamente en el DAO
        assertEquals(computadoraOriginal.getFechaCompra(), updatedComputadora.getFechaCompra(), "La fecha de compra no debería cambiar en el update.");
    }

    @Test
    @DisplayName("Test: Eliminar una computadora")
    void testDelete() throws SQLException {
        Computadora computadoraAEliminar = createComputadoraParaTest(); // Crea una para eliminar
        int idEliminar = computadoraAEliminar.getComputadoraID();

        boolean deleted = computadoraDAO.delete(idEliminar);
        assertTrue(deleted, "La eliminación debería ser exitosa.");

        // Intentar obtenerla de nuevo para confirmar que no existe
        Computadora deletedComputadora = computadoraDAO.getById(idEliminar);
        assertNull(deletedComputadora, "La computadora debería haber sido eliminada y no encontrada.");
    }

    @Test
    @DisplayName("Test: Buscar computadoras por marca, modelo o número de serie")
    void testSearch() throws SQLException {
        // Creamos una computadora con una marca y modelo específicos para buscar
        Random rand = new Random();
        String uniqueMarca = "SearchBrand" + rand.nextInt(1000);
        String uniqueModelo = "SearchModel" + rand.nextInt(1000);
        String uniqueSN = "SEARCH-SN-" + System.currentTimeMillis() + "-" + rand.nextInt(1000);

        Computadora comp1 = new Computadora(1, 1, uniqueMarca, uniqueModelo, uniqueSN, 900.0, "Test search 1");
        comp1 = computadoraDAO.create(comp1);
        idsCreadosParaLimpieza.add(comp1.getComputadoraID());

        // Test de búsqueda por marca
        ArrayList<Computadora> foundByBrand = computadoraDAO.search(uniqueMarca);
        assertFalse(foundByBrand.isEmpty(), "La búsqueda por marca debería devolver al menos una computadora.");
        assertTrue(foundByBrand.stream().anyMatch(c -> c.getMarca().equals(uniqueMarca)), "Debería encontrar la computadora por su marca.");

        // Test de búsqueda por modelo
        ArrayList<Computadora> foundByModel = computadoraDAO.search(uniqueModelo);
        assertFalse(foundByModel.isEmpty(), "La búsqueda por modelo debería devolver al menos una computadora.");
        assertTrue(foundByModel.stream().anyMatch(c -> c.getModelo().equals(uniqueModelo)), "Debería encontrar la computadora por su modelo.");

        // Test de búsqueda por número de serie (si tu search lo soporta, y el DAO lo soporta)
        ArrayList<Computadora> foundBySN = computadoraDAO.search(uniqueSN);
        assertFalse(foundBySN.isEmpty(), "La búsqueda por número de serie debería devolver al menos una computadora.");
        assertTrue(foundBySN.stream().anyMatch(c -> c.getNumeroSerie().equals(uniqueSN)), "Debería encontrar la computadora por su número de serie.");

        // Test de búsqueda que no debería devolver resultados
        ArrayList<Computadora> notFound = computadoraDAO.search("NonExistentBrand123");
        assertTrue(notFound.isEmpty(), "La búsqueda de una marca inexistente no debería devolver resultados.");
    }

    @Test
    @DisplayName("Test: Obtener una computadora por su ID")
    void testGetById() throws SQLException {
        Computadora computadoraCreada = createComputadoraParaTest(); // Crea una para buscar

        Computadora foundComputadora = computadoraDAO.getById(computadoraCreada.getComputadoraID());
        assertNotNull(foundComputadora, "La computadora debería ser encontrada por ID.");
        assertEquals(computadoraCreada.getComputadoraID(), foundComputadora.getComputadoraID(), "Los IDs deben coincidir.");
        assertEquals(computadoraCreada.getMarca(), foundComputadora.getMarca(), "La marca debe coincidir.");
        assertEquals(computadoraCreada.getModelo(), foundComputadora.getModelo(), "El modelo debe coincidir.");
        assertEquals(computadoraCreada.getNumeroSerie(), foundComputadora.getNumeroSerie(), "El número de serie debe coincidir.");
        assertNotNull(foundComputadora.getFechaCompra(), "La fecha de compra no debe ser nula.");
        assertEquals(computadoraCreada.getFechaCompra(), foundComputadora.getFechaCompra(), "La fecha de compra debe coincidir.");
    }
}
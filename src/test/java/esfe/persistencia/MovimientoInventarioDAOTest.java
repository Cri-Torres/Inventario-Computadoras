package esfe.persistencia;

import esfe.dominio.MovimientoInventario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoInventarioDAOTest {
    private MovimientoInventarioDAO movimientoInventarioDAO;
    private ArrayList<Integer> idsCreadosParaLimpieza; // Lista para almacenar IDs creados para limpieza

    // ID de una computadora existente en la base de datos de prueba.
    // ¡IMPORTANTE! Asegúrate de que exista una computadora con este ID en tu tabla 'Computadoras'.
    private final int COMPUTADORA_ID_EXISTENTE = 6; // Este ID debe existir en tu DB de prueba

    @BeforeEach
    void setUp() {
        movimientoInventarioDAO = new MovimientoInventarioDAO();
        idsCreadosParaLimpieza = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        // Limpieza: Intentar eliminar todos los movimientos de inventario creados durante el test
        for (Integer id : idsCreadosParaLimpieza) {
            try {
                if (movimientoInventarioDAO.getById(id) != null) { // Solo intentar eliminar si aún existe
                    movimientoInventarioDAO.delete(id);
                    System.out.println("Limpiado: Movimiento de Inventario con ID " + id + " eliminado.");
                }
            } catch (SQLException e) {
                System.err.println("Error al limpiar movimiento de inventario con ID " + id + ": " + e.getMessage());
                // No lanzar la excepción para que la limpieza continúe
            }
        }
    }

    /**
     * Método auxiliar para crear y persistir un objeto MovimientoInventario para los tests.
     * Genera una descripción única y un tipo de movimiento aleatorio.
     * Almacena el ID generado para limpieza posterior.
     *
     * @return El objeto MovimientoInventario creado con su ID generado.
     * @throws SQLException Si ocurre un error durante la creación en la base de datos.
     */
    private MovimientoInventario createMovimientoInventarioParaTest() throws SQLException {
        Random rand = new Random();
        // Genera 1 (Entrada), 2 (Salida) o 3 (Mantenimiento)
        byte tipoMovimiento = (byte) (rand.nextInt(3) + 1);
        String descripcionUnica = "Movimiento de Prueba - " + System.currentTimeMillis() + "-" + rand.nextInt(10000);
        int cantidad = rand.nextInt(5) + 1; // Cantidad entre 1 y 5

        MovimientoInventario nuevoMovimiento = new MovimientoInventario(
                COMPUTADORA_ID_EXISTENTE,
                tipoMovimiento,
                cantidad,
                descripcionUnica
        );

        MovimientoInventario createdMovimiento = movimientoInventarioDAO.create(nuevoMovimiento);

        assertNotNull(createdMovimiento, "El movimiento creado no debería ser nulo.");
        assertTrue(createdMovimiento.getMovimientoID() > 0, "El ID del movimiento debe ser generado y mayor que 0.");
        assertEquals(nuevoMovimiento.getComputadoraID(), createdMovimiento.getComputadoraID(), "El ID de la computadora debe coincidir.");
        assertEquals(nuevoMovimiento.getTipoMovimiento(), createdMovimiento.getTipoMovimiento(), "El tipo de movimiento debe coincidir.");
        assertEquals(nuevoMovimiento.getCantidad(), createdMovimiento.getCantidad(), "La cantidad debe coincidir.");
        assertEquals(nuevoMovimiento.getDescripcion(), createdMovimiento.getDescripcion(), "La descripción debe coincidir.");
        assertNotNull(createdMovimiento.getFechaMovimiento(), "La fecha de movimiento no debe ser nula.");

        // Añadir el ID para la limpieza
        idsCreadosParaLimpieza.add(createdMovimiento.getMovimientoID());

        return createdMovimiento;
    }

    @Test
    @DisplayName("Test: Crear un nuevo movimiento de inventario y verificar ID y fecha automática")
    void testCreate() throws SQLException {
        MovimientoInventario movimiento = createMovimientoInventarioParaTest();

        // Verificar que la fecha de movimiento esté cerca de la hora actual (margen de 5 segundos)
        assertTrue(movimiento.getFechaMovimiento().isAfter(LocalDateTime.now().minusSeconds(5)), "La fecha de movimiento debe ser reciente.");
        assertTrue(movimiento.getFechaMovimiento().isBefore(LocalDateTime.now().plusSeconds(5)), "La fecha de movimiento debe ser reciente.");
    }

    @Test
    @DisplayName("Test: Actualizar un movimiento de inventario existente")
    void testUpdate() throws SQLException {
        MovimientoInventario movimientoOriginal = createMovimientoInventarioParaTest(); // Crea uno para actualizar

        // Modificar algunos atributos
        byte nuevoTipo = MovimientoInventario.TIPO_SALIDA;
        String nuevaDescripcion = "Descripción actualizada - " + System.currentTimeMillis();
        int nuevaCantidad = 5;

        movimientoOriginal.setTipoMovimiento(nuevoTipo);
        movimientoOriginal.setDescripcion(nuevaDescripcion);
        movimientoOriginal.setCantidad(nuevaCantidad);

        boolean updated = movimientoInventarioDAO.update(movimientoOriginal);
        assertTrue(updated, "La actualización debería ser exitosa.");

        // Recuperar el movimiento actualizado de la base de datos
        MovimientoInventario updatedMovimiento = movimientoInventarioDAO.getById(movimientoOriginal.getMovimientoID());
        assertNotNull(updatedMovimiento, "El movimiento actualizado no debería ser nulo.");
        assertEquals(nuevoTipo, updatedMovimiento.getTipoMovimiento(), "El tipo de movimiento actualizado debe coincidir.");
        assertEquals(nuevaDescripcion, updatedMovimiento.getDescripcion(), "La descripción actualizada debe coincidir.");
        assertEquals(nuevaCantidad, updatedMovimiento.getCantidad(), "La cantidad actualizada debe coincidir.");
        // La fecha de movimiento no debería cambiar en un update a menos que se especifique explícitamente en el DAO
        assertEquals(movimientoOriginal.getFechaMovimiento(), updatedMovimiento.getFechaMovimiento(), "La fecha de movimiento no debería cambiar en el update.");
    }

    @Test
    @DisplayName("Test: Eliminar un movimiento de inventario")
    void testDelete() throws SQLException {
        MovimientoInventario movimientoAEliminar = createMovimientoInventarioParaTest(); // Crea uno para eliminar
        int idEliminar = movimientoAEliminar.getMovimientoID();

        boolean deleted = movimientoInventarioDAO.delete(idEliminar);
        assertTrue(deleted, "La eliminación debería ser exitosa.");

        // Intentar obtenerlo de nuevo para confirmar que no existe
        MovimientoInventario deletedMovimiento = movimientoInventarioDAO.getById(idEliminar);
        assertNull(deletedMovimiento, "El movimiento debería haber sido eliminado y no encontrado.");
    }

    @Test
    @DisplayName("Test: Buscar movimientos de inventario por descripción")
    void testSearch() throws SQLException {
        // Creamos varios movimientos con descripciones específicas para buscar
        Random rand = new Random();
        String uniqueTerm = "BuscarTermino" + System.currentTimeMillis() + "-" + rand.nextInt(1000);
        String desc1 = uniqueTerm + " - Ejemplo 1";
        String desc2 = "Otro Movimiento con " + uniqueTerm;
        String desc3 = "Solo esta";

        MovimientoInventario mov1 = createMovimientoInventarioParaTest();
        mov1.setDescripcion(desc1);
        movimientoInventarioDAO.update(mov1); // Actualizar para usar la descripción única
        // El ID ya está en idsCreadosParaLimpieza

        MovimientoInventario mov2 = createMovimientoInventarioParaTest();
        mov2.setDescripcion(desc2);
        movimientoInventarioDAO.update(mov2);
        // El ID ya está en idsCreadosParaLimpieza

        MovimientoInventario mov3 = createMovimientoInventarioParaTest();
        mov3.setDescripcion(desc3);
        movimientoInventarioDAO.update(mov3);
        // El ID ya está en idsCreadosParaLimpieza


        // Test de búsqueda por un término común
        List<MovimientoInventario> foundByCommonTerm = movimientoInventarioDAO.search(uniqueTerm);
        assertFalse(foundByCommonTerm.isEmpty(), "La búsqueda por término común debería devolver resultados.");
        assertEquals(2, foundByCommonTerm.size(), "Debería encontrar 2 movimientos con el término común.");
        assertTrue(foundByCommonTerm.stream().anyMatch(m -> m.getDescripcion().equals(desc1)), "Debería encontrar el movimiento 1 con la descripción exacta.");
        assertTrue(foundByCommonTerm.stream().anyMatch(m -> m.getDescripcion().equals(desc2)), "Debería encontrar el movimiento 2 con la descripción exacta.");

        // Test de búsqueda por una descripción única (exacta)
        List<MovimientoInventario> foundByExactDesc = movimientoInventarioDAO.search(desc1);
        assertEquals(1, foundByExactDesc.size(), "La búsqueda por descripción exacta debería devolver 1 resultado.");
        assertEquals(desc1, foundByExactDesc.get(0).getDescripcion(), "Debería encontrar el movimiento con la descripción única.");


        // Test de búsqueda que no debería devolver resultados
        List<MovimientoInventario> notFound = movimientoInventarioDAO.search("TerminoInexistente" + rand.nextInt(1000));
        assertTrue(notFound.isEmpty(), "La búsqueda de un término inexistente no debería devolver resultados.");
    }

    @Test
    @DisplayName("Test: Obtener un movimiento de inventario por su ID")
    void testGetById() throws SQLException {
        MovimientoInventario movimientoCreado = createMovimientoInventarioParaTest();

        MovimientoInventario foundMovimiento = movimientoInventarioDAO.getById(movimientoCreado.getMovimientoID());
        assertNotNull(foundMovimiento, "El movimiento debería ser encontrado por ID.");
        assertEquals(movimientoCreado.getMovimientoID(), foundMovimiento.getMovimientoID(), "Los IDs deben coincidir.");
        assertEquals(movimientoCreado.getComputadoraID(), foundMovimiento.getComputadoraID(), "El ID de la computadora debe coincidir.");
        assertEquals(movimientoCreado.getTipoMovimiento(), foundMovimiento.getTipoMovimiento(), "El tipo de movimiento debe coincidir.");
        assertEquals(movimientoCreado.getCantidad(), foundMovimiento.getCantidad(), "La cantidad debe coincidir.");
        assertEquals(movimientoCreado.getFechaMovimiento(), foundMovimiento.getFechaMovimiento(), "La fecha de movimiento debe coincidir.");
        assertEquals(movimientoCreado.getDescripcion(), foundMovimiento.getDescripcion(), "La descripción debe coincidir.");
    }

    @Test
    @DisplayName("Test: Obtener todos los movimientos de inventario")
    void testGetAllMovimientoInventario() throws SQLException {
        // Asegurarse de que haya al menos 3 movimientos para probar creando algunos
        // Es mejor no depender de un estado inicial desconocido de la DB, sino crearlos para el test.
        MovimientoInventario mov1 = createMovimientoInventarioParaTest();
        MovimientoInventario mov2 = createMovimientoInventarioParaTest();
        MovimientoInventario mov3 = createMovimientoInventarioParaTest();

        List<MovimientoInventario> allMovimientos = movimientoInventarioDAO.getAllMovimientoInventario();

        assertFalse(allMovimientos.isEmpty(), "La lista de todos los movimientos no debe estar vacía.");
        // Verificar que la lista contenga los movimientos recién creados.
        // No asertamos un tamaño exacto ya que puede haber otros movimientos en la DB.
        assertTrue(allMovimientos.stream().anyMatch(m -> m.getMovimientoID() == mov1.getMovimientoID()), "El movimiento 1 debe estar en la lista.");
        assertTrue(allMovimientos.stream().anyMatch(m -> m.getMovimientoID() == mov2.getMovimientoID()), "El movimiento 2 debe estar en la lista.");
        assertTrue(allMovimientos.stream().anyMatch(m -> m.getMovimientoID() == mov3.getMovimientoID()), "El movimiento 3 debe estar en la lista.");
    }
}
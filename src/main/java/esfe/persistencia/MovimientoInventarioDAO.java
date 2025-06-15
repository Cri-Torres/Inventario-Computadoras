package esfe.persistencia;

import esfe.dominio.MovimientoInventario;
import java.sql.*;
import java.time.LocalDateTime; // Necesario para FechaMovimiento
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public MovimientoInventarioDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea un nuevo registro de movimiento de inventario en la base de datos.
     * La FechaMovimiento se genera automáticamente con la fecha y hora actuales.
     *
     * @param movimiento El objeto MovimientoInventario a crear.
     * @return El objeto MovimientoInventario con su ID generado, o null si falla.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public MovimientoInventario create(MovimientoInventario movimiento) throws SQLException {
        MovimientoInventario res = null;
        ResultSet generatedKeys = null;
        try {
            // Asignar la fecha y hora actual automáticamente antes de insertar
            movimiento.setFechaMovimiento(LocalDateTime.now());

            ps = conn.connect().prepareStatement(
                    "INSERT INTO MovimientosInventario (ComputadoraID, TipoMovimiento, Cantidad, FechaMovimiento, Descripcion) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setInt(1, movimiento.getComputadoraID());
            ps.setByte(2, movimiento.getTipoMovimiento());
            ps.setInt(3, movimiento.getCantidad());
            // Convertir LocalDateTime a java.sql.Timestamp para la base de datos
            ps.setTimestamp(4, Timestamp.valueOf(movimiento.getFechaMovimiento()));
            ps.setString(5, movimiento.getDescripcion());

            int affectedRows = ps.executeUpdate();
            if (affectedRows != 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    res = getById(idGenerado); // Recuperar el objeto completo con el ID generado
                } else {
                    throw new SQLException("Error al crear el movimiento, no se generó ID.");
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al crear el movimiento de inventario: " + ex.getMessage(), ex);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { System.err.println("Error closing generatedKeys: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Actualiza un registro de movimiento de inventario existente en la base de datos.
     *
     * @param movimiento El objeto MovimientoInventario con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public boolean update(MovimientoInventario movimiento) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "UPDATE MovimientosInventario SET ComputadoraID = ?, TipoMovimiento = ?, Cantidad = ?, FechaMovimiento = ?, Descripcion = ? WHERE MovimientoID = ?"
            );

            ps.setInt(1, movimiento.getComputadoraID());
            ps.setByte(2, movimiento.getTipoMovimiento());
            ps.setInt(3, movimiento.getCantidad());
            // Convertir LocalDateTime a java.sql.Timestamp para la base de datos
            ps.setTimestamp(4, Timestamp.valueOf(movimiento.getFechaMovimiento()));
            ps.setString(5, movimiento.getDescripcion());
            ps.setInt(6, movimiento.getMovimientoID());

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al modificar el movimiento de inventario: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Elimina un registro de movimiento de inventario de la base de datos por su ID.
     *
     * @param movimientoID El ID del movimiento a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public boolean delete(int movimientoID) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "DELETE FROM MovimientosInventario WHERE MovimientoID = ?"
            );
            ps.setInt(1, movimientoID);

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al eliminar el movimiento de inventario: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Busca movimientos de inventario en la base de datos por descripción.
     *
     * @param query La cadena de búsqueda para la descripción.
     * @return Una lista de objetos MovimientoInventario que coinciden con la búsqueda.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public List<MovimientoInventario> search(String query) throws SQLException {
        List<MovimientoInventario> records = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT MovimientoID, ComputadoraID, TipoMovimiento, Cantidad, FechaMovimiento, Descripcion FROM MovimientosInventario WHERE Descripcion LIKE ? ORDER BY FechaMovimiento DESC"
            );
            ps.setString(1, "%" + query + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                records.add(mapRowToMovimientoInventario(rs));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al buscar movimientos de inventario: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return records;
    }

    /**
     * Obtiene un registro de movimiento de inventario por su ID.
     *
     * @param id El ID del movimiento a obtener.
     * @return El objeto MovimientoInventario si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public MovimientoInventario getById(int id) throws SQLException {
        MovimientoInventario movimiento = null;
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT MovimientoID, ComputadoraID, TipoMovimiento, Cantidad, FechaMovimiento, Descripcion FROM MovimientosInventario WHERE MovimientoID = ?"
            );
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                movimiento = mapRowToMovimientoInventario(rs);
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener movimiento de inventario por ID: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return movimiento;
    }

    /**
     * Obtiene todos los movimientos de inventario de la base de datos.
     *
     * @return Una lista de todos los objetos MovimientoInventario.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public List<MovimientoInventario> getAllMovimientoInventario() throws SQLException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT MovimientoID, ComputadoraID, TipoMovimiento, Cantidad, FechaMovimiento, Descripcion FROM MovimientosInventario ORDER BY FechaMovimiento DESC"
            );
            rs = ps.executeQuery();

            while (rs.next()) {
                movimientos.add(mapRowToMovimientoInventario(rs));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener todos los movimientos de inventario: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return movimientos;
    }

    /**
     * Método auxiliar para mapear una fila de ResultSet a un objeto MovimientoInventario.
     *
     * @param rs El ResultSet actual.
     * @return Un objeto MovimientoInventario con los datos de la fila actual.
     * @throws SQLException Si ocurre un error al leer los datos del ResultSet.
     */
    private MovimientoInventario mapRowToMovimientoInventario(ResultSet rs) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMovimientoID(rs.getInt("MovimientoID"));
        movimiento.setComputadoraID(rs.getInt("ComputadoraID"));
        movimiento.setTipoMovimiento(rs.getByte("TipoMovimiento"));
        movimiento.setCantidad(rs.getInt("Cantidad"));

        // Convertir java.sql.Timestamp a LocalDateTime
        Timestamp fechaTs = rs.getTimestamp("FechaMovimiento");
        if (fechaTs != null) {
            movimiento.setFechaMovimiento(fechaTs.toLocalDateTime());
        } else {
            movimiento.setFechaMovimiento(null); // O manejar como prefieras si FechaMovimiento puede ser null en DB
        }

        movimiento.setDescripcion(rs.getString("Descripcion"));
        return movimiento;
    }
}

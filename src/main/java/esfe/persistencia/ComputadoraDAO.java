package esfe.persistencia;

import esfe.dominio.Computadora;

import java.sql.*;
import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.ArrayList;
import java.util.List;

public class ComputadoraDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public ComputadoraDAO() {
        conn = ConnectionManager.getInstance();
    }

    public Computadora create(Computadora computadora) throws SQLException {
        Computadora res = null;
        ResultSet generatedKeys = null;
        try {
            // Asignar la fecha y hora actual automáticamente antes de insertar
            computadora.setFechaCompra(LocalDateTime.now());

            ps = conn.connect().prepareStatement(
                    "INSERT INTO Computadoras (CategoriaID, ProveedorID, Marca, Modelo, NumeroSerie, FechaCompra, Precio, Estado, Observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, computadora.getCategoriaID());
            // ps.setObject(2, computadora.getProveedorID(), Types.INTEGER); es correcto si ProveedorID puede ser null
            if (computadora.getProveedorID() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, computadora.getProveedorID());
            }
            ps.setString(3, computadora.getMarca());
            ps.setString(4, computadora.getModelo());
            ps.setString(5, computadora.getNumeroSerie());
            // Usar setObject para LocalDateTime, que JDBC 4.2+ puede mapear a DATETIME/DATETIME2
            ps.setObject(6, computadora.getFechaCompra());
            ps.setDouble(7, computadora.getPrecio());
            ps.setByte(8, computadora.getEstado());
            ps.setString(9, computadora.getObservaciones());

            int affectedRows = ps.executeUpdate();
            if (affectedRows != 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    res = getById(idGenerado); // Recuperar el objeto completo con el ID generado
                } else {
                    throw new SQLException("Error al crear la computadora, no se generó ID.");
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al crear la computadora: " + ex.getMessage(), ex);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* Ignorar */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* Ignorar */ }
            conn.disconnect();
        }
        return res;
    }

    public boolean update(Computadora computadora) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "UPDATE Computadoras SET CategoriaID = ?, ProveedorID = ?, Marca = ?, Modelo = ?, NumeroSerie = ?, FechaCompra = ?, Precio = ?, Estado = ?, Observaciones = ? WHERE ComputadoraID = ?"
            );
            ps.setInt(1, computadora.getCategoriaID());
            if (computadora.getProveedorID() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, computadora.getProveedorID());
            }
            ps.setString(3, computadora.getMarca());
            ps.setString(4, computadora.getModelo());
            ps.setString(5, computadora.getNumeroSerie());
            // Usar setObject para LocalDateTime
            ps.setObject(6, computadora.getFechaCompra());
            ps.setDouble(7, computadora.getPrecio());
            ps.setByte(8, computadora.getEstado());
            ps.setString(9, computadora.getObservaciones());
            ps.setInt(10, computadora.getComputadoraID());

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al modificar la computadora: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* Ignorar */ }
            conn.disconnect();
        }
        return res;
    }

    public boolean delete(int computadoraID) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Computadoras WHERE ComputadoraID = ?"
            );
            ps.setInt(1, computadoraID);

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al eliminar la computadora: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* Ignorar */ }
            conn.disconnect();
        }
        return res;
    }

    public ArrayList<Computadora> search(String query) throws SQLException {
        ArrayList<Computadora> records = new ArrayList<>();
        // Mejora: Permite buscar también por Número de Serie
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT ComputadoraID, CategoriaID, ProveedorID, Marca, Modelo, NumeroSerie, FechaCompra, Precio, Estado, Observaciones FROM Computadoras WHERE Marca LIKE ? OR Modelo LIKE ? OR NumeroSerie LIKE ?"
            );
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%"); // Añadir búsqueda por Número de Serie
            rs = ps.executeQuery();

            while (rs.next()) {
                Computadora comp = new Computadora();
                comp.setComputadoraID(rs.getInt("ComputadoraID")); // Usar nombre de columna para claridad
                comp.setCategoriaID(rs.getInt("CategoriaID"));
                comp.setProveedorID(rs.getObject("ProveedorID", Integer.class));
                comp.setMarca(rs.getString("Marca"));
                comp.setModelo(rs.getString("Modelo"));
                comp.setNumeroSerie(rs.getString("NumeroSerie"));
                // Leer LocalDateTime directamente
                comp.setFechaCompra(rs.getObject("FechaCompra", LocalDateTime.class));
                comp.setPrecio(rs.getDouble("Precio"));
                comp.setEstado(rs.getByte("Estado"));
                comp.setObservaciones(rs.getString("Observaciones"));
                records.add(comp);
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al buscar computadoras: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* Ignorar */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* Ignorar */ }
            conn.disconnect();
        }
        return records;
    }

    public Computadora getById(int id) throws SQLException {
        Computadora comp = null;
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT ComputadoraID, CategoriaID, ProveedorID, Marca, Modelo, NumeroSerie, FechaCompra, Precio, Estado, Observaciones FROM Computadoras WHERE ComputadoraID = ?"
            );
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                comp = new Computadora();
                comp.setComputadoraID(rs.getInt("ComputadoraID"));
                comp.setCategoriaID(rs.getInt("CategoriaID"));
                comp.setProveedorID(rs.getObject("ProveedorID", Integer.class));
                comp.setMarca(rs.getString("Marca"));
                comp.setModelo(rs.getString("Modelo"));
                comp.setNumeroSerie(rs.getString("NumeroSerie"));
                // Leer LocalDateTime directamente
                comp.setFechaCompra(rs.getObject("FechaCompra", LocalDateTime.class));
                comp.setPrecio(rs.getDouble("Precio"));
                comp.setEstado(rs.getByte("Estado"));
                comp.setObservaciones(rs.getString("Observaciones"));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener computadora por ID: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* Ignorar */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* Ignorar */ }
            conn.disconnect();
        }
        return comp;
    }

    /**
     * Obtiene todas las computadoras de la base de datos.
     *
     * @return Una lista de todos los objetos Computadora.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public List<Computadora> getAllComputadoras() throws SQLException {
        List<Computadora> computadoras = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT ComputadoraID, CategoriaID, ProveedorID, Marca, Modelo, NumeroSerie, FechaCompra, Precio, Estado, Observaciones FROM Computadoras ORDER BY Marca, Modelo"
            );
            rs = ps.executeQuery();

            while (rs.next()) {
                computadoras.add(mapRowToComputadora(rs)); // Reutiliza el método de mapeo
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener todas las computadoras: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return computadoras;
    }

    /**
     * Método auxiliar para mapear una fila de ResultSet a un objeto Computadora.
     * Centraliza la lógica de lectura de datos desde el ResultSet.
     *
     * @param rs El ResultSet actual.
     * @return Un objeto Computadora con los datos de la fila actual.
     * @throws SQLException Si ocurre un error al leer los datos del ResultSet.
     */
    private Computadora mapRowToComputadora(ResultSet rs) throws SQLException {
        Computadora comp = new Computadora();
        comp.setComputadoraID(rs.getInt("ComputadoraID"));
        comp.setCategoriaID(rs.getInt("CategoriaID"));

        // Manejar ProveedorID que puede ser NULL
        Integer proveedorIdDb = rs.getObject("ProveedorID", Integer.class); // Usa getObject para Integer nulo
        comp.setProveedorID(proveedorIdDb);

        comp.setMarca(rs.getString("Marca"));
        comp.setModelo(rs.getString("Modelo"));
        comp.setNumeroSerie(rs.getString("NumeroSerie"));

        // Convertir java.sql.Timestamp a LocalDateTime
        Timestamp fechaTs = rs.getTimestamp("FechaCompra");
        if (fechaTs != null) {
            comp.setFechaCompra(fechaTs.toLocalDateTime());
        } else {
            comp.setFechaCompra(null);
        }

        comp.setPrecio(rs.getDouble("Precio"));
        comp.setEstado(rs.getByte("Estado"));
        comp.setObservaciones(rs.getString("Observaciones"));
        return comp;
    }
}

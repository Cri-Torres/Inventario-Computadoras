package esfe.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import esfe.dominio.Proveedor; // Asegúrate de que la clase Proveedor esté correctamente importada

public class ProveedorDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public ProveedorDAO() {
        conn = ConnectionManager.getInstance();
    }

    public List<Proveedor> getAllProveedores() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement("SELECT ProveedorID, Nombre, Telefono, Email, Direccion FROM Proveedores ORDER BY Nombre");
            rs = ps.executeQuery();
            while (rs.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setProveedorID(rs.getInt("ProveedorID"));
                proveedor.setNombre(rs.getString("Nombre"));
                proveedor.setTelefono(rs.getString("Telefono"));
                proveedor.setEmail(rs.getString("Email"));
                proveedor.setDireccion(rs.getString("Direccion"));
                proveedores.add(proveedor);
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener todos los proveedores de la base de datos: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return proveedores;
    }
    public Proveedor create(Proveedor proveedor) throws SQLException {
        Proveedor res = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.connect().prepareStatement(
                    "INSERT INTO Proveedores (Nombre, Telefono, Email, Direccion) VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getEmail());
            ps.setString(4, proveedor.getDireccion());

            int affectedRows = ps.executeUpdate();
            if (affectedRows != 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    res = getById(idGenerado);
                } else {
                    throw new SQLException("Error al crear el proveedor, no se generó ID.");
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al crear el proveedor: " + ex.getMessage(), ex);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return res;
    }

    public boolean update(Proveedor proveedor) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "UPDATE Proveedores SET Nombre = ?, Telefono = ?, Email = ?, Direccion = ? WHERE ProveedorId = ?"
            );
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getEmail());
            ps.setString(4, proveedor.getDireccion());
            ps.setInt(5, proveedor.getProveedorID());

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al modificar el proveedor: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return res;
    }

    public boolean delete(int proveedorId) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Proveedores WHERE ProveedorId = ?"
            );
            ps.setInt(1, proveedorId);

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al eliminar el proveedor: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return res;
    }

    public ArrayList<Proveedor> search(String nombre) throws SQLException {
        ArrayList<Proveedor> records = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT ProveedorId, Nombre, Telefono, Email, Direccion FROM Proveedores WHERE Nombre LIKE ?"
            );
            ps.setString(1, "%" + nombre + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Proveedor prov = new Proveedor();
                prov.setProveedorID(rs.getInt(1));
                prov.setNombre(rs.getString(2));
                prov.setTelefono(rs.getString(3));
                prov.setEmail(rs.getString(4));
                prov.setDireccion(rs.getString(5));
                records.add(prov);
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al buscar proveedores: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return records;
    }

    public Proveedor getById(int id) throws SQLException {
        Proveedor prov = null;
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT ProveedorId, Nombre, Telefono, Email, Direccion FROM Proveedores WHERE ProveedorId = ?"
            );
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                prov = new Proveedor();
                prov.setProveedorID(rs.getInt(1));
                prov.setNombre(rs.getString(2));
                prov.setTelefono(rs.getString(3));
                prov.setEmail(rs.getString(4));
                prov.setDireccion(rs.getString(5));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener proveedor por ID: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return prov;
    }
}

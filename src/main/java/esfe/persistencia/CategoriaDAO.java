package esfe.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import esfe.dominio.Categoria;

public class CategoriaDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public CategoriaDAO() {
        conn = ConnectionManager.getInstance();
    }


    // *** MÉTODO getAllCategorias() - NECESARIO PARA JComboBox ***
    public List<Categoria> getAllCategorias() throws SQLException {
        List<Categoria> categorias = new ArrayList<>(); // Usar la interfaz List para el tipo de la variable
        try {
            // No usamos WHERE, obtenemos todos los registros. Ordenar por nombre es buena práctica para JComboBox.
            ps = conn.connect().prepareStatement("SELECT CategoriaID, Nombre, Descripcion FROM Categorias ORDER BY Nombre");
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setCategoriaID(rs.getInt("CategoriaID")); // Puedes usar el nombre de la columna
                categoria.setNombre(rs.getString("Nombre"));
                categoria.setDescripcion(rs.getString("Descripcion"));
                categorias.add(categoria);
            }
        } catch (SQLException ex) {
            // Es crucial lanzar una nueva SQLException con un mensaje más descriptivo y la causa original.
            throw new SQLException("Error al obtener todas las categorías de la base de datos: " + ex.getMessage(), ex);
        } finally {
            // Asegurarse de cerrar los recursos en el bloque finally
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* Loguear o ignorar */ }
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { /* Loguear o ignorar */ }
            }
            conn.disconnect(); // Desconectar la conexión
        }
        return categorias;
    }
    public Categoria create(Categoria categoria) throws SQLException {
        Categoria res = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.connect().prepareStatement(
                    "INSERT INTO Categorias (nombre, descripcion) VALUES (?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());

            int affectedRows = ps.executeUpdate();
            if (affectedRows != 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    res = getById(idGenerado);
                } else {
                    throw new SQLException("Error al crear la categoría, no se generó ID.");
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al crear la categoría: " + ex.getMessage(), ex);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return res;
    }

    public boolean update(Categoria categoria) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "UPDATE Categorias SET nombre = ?, descripcion = ? WHERE categoriaId = ?"
            );
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getCategoriaID());

            if (ps.executeUpdate() > 0) {
                res = true;
            }
            ps.close();
        } catch (SQLException ex) {
            throw new SQLException("Error al modificar la categoría: " + ex.getMessage(), ex);
        } finally {
            ps = null;
            conn.disconnect();
        }
        return res;
    }

    public boolean delete(int categoriaId) throws SQLException {
        boolean res = false;
        try {
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Categorias WHERE categoriaId = ?"
            );
            ps.setInt(1, categoriaId);

            if (ps.executeUpdate() > 0) {
                res = true;
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al eliminar la categoría: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            conn.disconnect();
        }
        return res;
    }

    public ArrayList<Categoria> search(String nombre) throws SQLException {
        ArrayList<Categoria> records = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT categoriaId, nombre, descripcion FROM Categorias WHERE nombre LIKE ?"
            );
            ps.setString(1, "%" + nombre + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria cat = new Categoria();
                cat.setCategoriaID(rs.getInt(1));
                cat.setNombre(rs.getString(2));
                cat.setDescripcion(rs.getString(3));
                records.add(cat);
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            throw new SQLException("Error al buscar categorías: " + ex.getMessage(), ex);
        } finally {
            ps = null;
            rs = null;
            conn.disconnect();
        }
        return records;
    }

    public Categoria getById(int id) throws SQLException {
        Categoria cat = null;
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT categoriaId, nombre, descripcion FROM Categorias WHERE categoriaId = ?"
            );
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                cat = new Categoria();
                cat.setCategoriaID(rs.getInt(1));
                cat.setNombre(rs.getString(2));
                cat.setDescripcion(rs.getString(3));
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener categoría por ID: " + ex.getMessage(), ex);
        } finally {
            ps = null;
            rs = null;
            conn.disconnect();
        }
        return cat;
    }
}

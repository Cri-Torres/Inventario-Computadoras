package esfe.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List; // Importar List para el tipo de retorno

import esfe.dominio.User;
import esfe.utils.PasswordHasher;

public class UserDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public UserDAO(){
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user El objeto User que contiene la información del nuevo usuario a crear.
     * Se espera que el objeto User tenga los campos 'name', 'passwordHash',
     * 'email' y 'status' correctamente establecidos. El campo 'id' será
     * generado automáticamente por la base de datos.
     * @return El objeto User recién creado, incluyendo el ID generado por la base de datos,
     * o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la creación del usuario.
     */
    public User create(User user) throws SQLException {
        User res = null;
        ResultSet generatedKeys = null; // Declarar generatedKeys aquí para el finally
        try{
            // Preparar la sentencia SQL para la inserción de un nuevo usuario.
            // Se especifica que se retornen las claves generadas automáticamente.
            ps = conn.connect().prepareStatement( // Asignar a la variable de instancia ps
                    "INSERT INTO " +
                            "Users (name, passwordHash, email, status)" +
                            "VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, user.getName());
            ps.setString(2, PasswordHasher.hashPassword(user.getPasswordHash()));
            ps.setString(3, user.getEmail());
            ps.setByte(4, user.getStatus());

            // Ejecutar la sentencia de inserción y obtener el número de filas afectadas.
            int affectedRows = ps.executeUpdate();

            // Verificar si la inserción fue exitosa (al menos una fila afectada).
            if (affectedRows != 0) {
                // Obtener las claves generadas automáticamente por la base de datos (en este caso, el ID).
                generatedKeys = ps.getGeneratedKeys();
                // Mover el cursor al primer resultado (si existe).
                if (generatedKeys.next()) {
                    // Obtener el ID generado. Generalmente la primera columna contiene la clave primaria.
                    int idGenerado= generatedKeys.getInt(1);
                    // Recuperar el usuario completo utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación del usuario falló y no se obtuvo un ID.
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }catch (SQLException ex){
            throw new SQLException("Error al crear el usuario: " + ex.getMessage(), ex);
        } finally {
            // Asegurar que los recursos se liberen en el bloque finally
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { System.err.println("Error closing generatedKeys: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Actualiza la información de un usuario existente en la base de datos.
     *
     * @param user El objeto User que contiene la información actualizada del usuario.
     * Se requiere que el objeto User tenga los campos 'id', 'name', 'email' y 'status'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización del usuario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización del usuario.
     */
    public boolean update(User user) throws SQLException{
        boolean res = false;
        try{
            ps = conn.connect().prepareStatement(
                    "UPDATE Users " +
                            "SET name = ?, email = ?, status = ? " +
                            "WHERE id = ?"
            );

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setByte(3, user.getStatus());
            ps.setInt(4, user.getId());

            if(ps.executeUpdate() > 0){
                res = true;
            }
        }catch (SQLException ex){
            throw new SQLException("Error al modificar el usuario: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Elimina un usuario de la base de datos basándose en su ID.
     *
     * @param user El objeto User que contiene el ID del usuario a eliminar.
     * Se requiere que el objeto User tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación del usuario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación del usuario.
     */
    public boolean delete(User user) throws SQLException{
        boolean res = false;
        try{
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Users WHERE id = ?"
            );
            ps.setInt(1, user.getId());

            if(ps.executeUpdate() > 0){
                res = true;
            }
        }catch (SQLException ex){
            throw new SQLException("Error al eliminar el usuario: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Busca usuarios en la base de datos cuyo nombre contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el nombre del usuario contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluido en los resultados.
     *
     * @param name La cadena de texto a buscar dentro de los nombres de los usuarios.
     * @return Una lista de objetos User que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran usuarios con el nombre especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de usuarios.
     */
    public List<User> search(String name) throws SQLException{ // Cambiado a List<User>
        List<User> records  = new ArrayList<>(); // Cambiado a List<User>

        try {
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE name LIKE ?");

            ps.setString(1, "%" + name + "%");

            rs = ps.executeQuery();

            while (rs.next()){
                records.add(mapRowToUser(rs)); // Usar el método auxiliar
            }
        } catch (SQLException ex){
            throw new SQLException("Error al buscar usuarios: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return records;
    }

    /**
     * Obtiene un usuario de la base de datos basado en su ID.
     *
     * @param id El ID del usuario que se desea obtener.
     * @return Un objeto User si se encuentra un usuario con el ID especificado,
     * null si no se encuentra ningún usuario con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención del usuario.
     */
    public User getById(int id) throws SQLException{
        User user  = null; // Inicializar a null, no a new User()

        try {
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE id = ?");

            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                user = mapRowToUser(rs); // Usar el método auxiliar
            }
        } catch (SQLException ex){
            throw new SQLException("Error al obtener un usuario por id: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return user;
    }

    /**
     * Autentica a un usuario en la base de datos verificando su correo electrónico,
     * contraseña (comparando el hash) y estado (activo).
     *
     * @param user El objeto User que contiene el correo electrónico y la contraseña
     * del usuario que se intenta autenticar. Se espera que estos campos estén
     * correctamente establecidos.
     * @return Un objeto User si la autenticación es exitosa (se encuentra un usuario
     * con las credenciales proporcionadas y su estado es activo), o null si la
     * autenticación falla. El objeto User retornado contendrá el ID, nombre,
     * correo electrónico y estado del usuario autenticado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante el proceso de autenticación.
     */
    public User authenticate(User user) throws SQLException{
        User userAutenticate = null; // Inicializar a null, no a new User()

        try {
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE email = ? AND passwordHash = ? AND status = 1");

            ps.setString(1, user.getEmail());
            ps.setString(2, PasswordHasher.hashPassword(user.getPasswordHash()));
            rs = ps.executeQuery();

            if (rs.next()) {
                userAutenticate = mapRowToUser(rs); // Usar el método auxiliar
            }
        } catch (SQLException ex){
            throw new SQLException("Error al autenticar un usuario por id: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return userAutenticate;
    }

    /**
     * Actualiza la contraseña de un usuario existente en la base de datos.
     * La nueva contraseña proporcionada se hashea antes de ser almacenada.
     *
     * @param user El objeto User que contiene el ID del usuario cuya contraseña se
     * actualizará y la nueva contraseña (sin hashear) en el campo 'passwordHash'.
     * Se requiere que los campos 'id' y 'passwordHash' del objeto User estén
     * correctamente establecidos.
     * @return true si la actualización de la contraseña fue exitosa (al menos una
     * fila afectada), false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización de la contraseña.
     */
    public boolean updatePassword(User user) throws SQLException{
        boolean res = false;
        try{
            ps = conn.connect().prepareStatement(
                    "UPDATE Users " +
                            "SET passwordHash = ? " +
                            "WHERE id = ?"
            );
            ps.setString(1, PasswordHasher.hashPassword(user.getPasswordHash()));
            ps.setInt(2, user.getId());

            if(ps.executeUpdate() > 0){
                res = true;
            }
        }catch (SQLException ex){
            throw new SQLException("Error al modificar el password del usuario: " + ex.getMessage(), ex);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return res;
    }

    /**
     * Obtiene todos los usuarios de la base de datos.
     *
     * @return Una lista de todos los objetos User.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try {
            ps = conn.connect().prepareStatement(
                    "SELECT id, name, email, status FROM Users ORDER BY name"
            );
            rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapRowToUser(rs)); // Reutiliza el método de mapeo
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al obtener todos los usuarios: " + ex.getMessage(), ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { System.err.println("Error closing result set: " + e.getMessage()); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { System.err.println("Error closing prepared statement: " + e.getMessage()); }
            conn.disconnect();
        }
        return users;
    }

    /**
     * Método auxiliar para mapear una fila de ResultSet a un objeto User.
     * Centraliza la lógica de lectura de datos desde el ResultSet.
     *
     * @param rs El ResultSet actual.
     * @return Un objeto User con los datos de la fila actual.
     * @throws SQLException Si ocurre un error al leer los datos del ResultSet.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getByte("status"));
        // No se mapea passwordHash por seguridad
        return user;
    }
}

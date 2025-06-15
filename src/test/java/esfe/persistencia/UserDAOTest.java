package esfe.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;       // Anotación para indicar que el método es un caso de prueba.
import esfe.dominio.User;                // Clase que representa la entidad de usuario utilizada en las pruebas.

import java.util.ArrayList;              // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.
import java.util.Random;                 // Clase para generar números aleatorios, útil para crear datos de prueba.

import java.sql.SQLException;             // Clase para manejar excepciones relacionadas con la base de datos, aunque no se espera que las pruebas unitarias interactúen directamente con ella (idealmente se mockean las dependencias).

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.



class UserDAOTest {
    private UserDAO userDAO; // Instancia de la clase UserDAO que se va a probar.

    @BeforeEach
    void setUp(){
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de UserDAO para cada prueba.
        userDAO = new UserDAO();
    }
    private User create(User user) throws SQLException{
        // Llama al método 'create' del UserDAO para persistir el usuario en la base de datos (simulada).
        User res = userDAO.create(user);

        // Realiza aserciones para verificar que la creación del usuario fue exitosa
        // y que los datos del usuario retornado coinciden con los datos originales.
        assertNotNull(res, "El usuario creado no debería ser nulo."); // Verifica que el objeto retornado no sea nulo.
        assertEquals(user.getName(), res.getName(), "El nombre del usuario creado debe ser igual al original.");
        assertEquals(user.getEmail(), res.getEmail(), "El email del usuario creado debe ser igual al original.");
        assertEquals(user.getStatus(), res.getStatus(), "El status del usuario creado debe ser igual al original.");

        // Retorna el objeto User creado (tal como lo devolvió el UserDAO).
        return res;
    }

    private void update(User user) throws SQLException{
        // Modifica los atributos del objeto User para simular una actualización.
        user.setName(user.getName() + "_u"); // Añade "_u" al final del nombre.
        user.setEmail("u" + user.getEmail()); // Añade "u" al inicio del email.
        user.setStatus((byte)1);             // Establece el status a 1.

        // Llama al método 'update' del UserDAO para actualizar el usuario en la base de datos (simulada).
        boolean res = userDAO.update(user);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización del usuario debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        // Aunque el método 'getById' ya tiene sus propias aserciones, esta llamada adicional
        // ayuda a asegurar que la actualización realmente tuvo efecto en la capa de datos.
        getById(user);
    }

    private void getById(User user) throws SQLException {
        // Llama al método 'getById' del UserDAO para obtener un usuario por su ID.
        User res = userDAO.getById(user.getId());

        // Realiza aserciones para verificar que el usuario obtenido coincide
        // con el usuario original (o el usuario modificado en pruebas de actualización).
        assertNotNull(res, "El usuario obtenido por ID no debería ser nulo.");
        assertEquals(user.getId(), res.getId(), "El ID del usuario obtenido debe ser igual al original.");
        assertEquals(user.getName(), res.getName(), "El nombre del usuario obtenido debe ser igual al esperado.");
        assertEquals(user.getEmail(), res.getEmail(), "El email del usuario obtenido debe ser igual al esperado.");
        assertEquals(user.getStatus(), res.getStatus(), "El status del usuario obtenido debe ser igual al esperado.");
    }

    private void search(User user) throws SQLException {
        // Llama al método 'search' del UserDAO para buscar usuarios por nombre.
        ArrayList<User> users = userDAO.search(user.getName());
        boolean find = false; // Variable para rastrear si se encontró un usuario con el nombre buscado.

        // Itera sobre la lista de usuarios devuelta por la búsqueda.
        for (User userItem : users) {
            // Verifica si el nombre de cada usuario encontrado contiene la cadena de búsqueda.
            if (userItem.getName().contains(user.getName())) {
                find = true; // Si se encuentra una coincidencia, se establece 'find' a true.
            }
            else{
                find = false; // Si un nombre no contiene la cadena de búsqueda, se establece 'find' a false.
                break;      // Se sale del bucle, ya que se esperaba que todos los resultados contuvieran la cadena.
            }
        }

        // Realiza una aserción para verificar que todos los usuarios con el nombre buscado fue encontrado.
        assertTrue(find, "el nombre buscado no fue encontrado : " + user.getName());
    }

    private void delete(User user) throws SQLException{
        // Llama al método 'delete' del UserDAO para eliminar un usuario por su ID.
        boolean res = userDAO.delete(user);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación del usuario debería ser exitosa.");

        // Intenta obtener el usuario por su ID después de la eliminación.
        User res2 = userDAO.getById(user.getId());

        // Realiza una aserción para verificar que el usuario ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "El usuario debería haber sido eliminado y no encontrado por ID.");
    }

    private void autenticate(User user) throws SQLException {
        // Llama al método 'authenticate' del UserDAO para intentar autenticar un usuario.
        User res = userDAO.authenticate(user);

        // Realiza aserciones para verificar si la autenticación fue exitosa.
        assertNotNull(res, "La autenticación debería retornar un usuario no nulo si es exitosa.");
        assertEquals(res.getEmail(), user.getEmail(), "El email del usuario autenticado debe coincidir con el email proporcionado.");
        assertEquals(res.getStatus(), 1, "El status del usuario autenticado debe ser 1 (activo).");
    }

    private void autenticacionFails(User user) throws SQLException {
        // Llama al método 'authenticate' del UserDAO para intentar autenticar un usuario
        // con credenciales que se espera que fallen.
        User res = userDAO.authenticate(user);

        // Realiza una aserción para verificar que la autenticación falló,
        // lo cual se espera que se represente con el método 'authenticate'
        // retornando 'null' cuando las credenciales no son válidas o el usuario no está activo.
        assertNull(res, "La autenticación debería fallar y retornar null para credenciales inválidas.");
    }

    private void updatePassword(User user) throws SQLException{
        // Llama al método 'updatePassword' del UserDAO para actualizar la contraseña del usuario.
        boolean res = userDAO.updatePassword(user);

        // Realiza una aserción para verificar que la actualización de la contraseña fue exitosa.
        assertTrue(res, "La actualización de la contraseña debería ser exitosa.");

        // Llama al método 'autenticate' para verificar que la nueva contraseña es válida
        // y el usuario aún puede autenticarse con ella. Esto asume que el objeto 'user'
        // contiene la nueva contraseña (sin hashear) antes de llamar a 'updatePassword'.
        // Es importante asegurarse de que la prueba configure correctamente la nueva
        // contraseña en el objeto 'user' antes de esta llamada.
        autenticate(user);
    }
    @Test
    void testUserDAO() throws SQLException {
        // Crea una instancia de la clase Random para generar datos de prueba aleatorios.
        Random random = new Random();
        // Genera un número aleatorio entre 1 y 1000 para asegurar la unicidad del email en cada prueba.
        int num = random.nextInt(1000) + 1;
        // Define una cadena base para el email y le concatena el número aleatorio generado.
        String strEmail = "test" + num + "@example.com";
        // Crea un nuevo objeto User con datos de prueba. El ID se establece en 0 ya que será generado por la base de datos.
        User user = new User(0, "Test User", "password", strEmail, (byte) 2);

        // Llama al método 'create' para persistir el usuario de prueba en la base de datos (simulada) y verifica su creación.
        User testUser = create(user);

        // Llama al método 'update' para modificar los datos del usuario de prueba y verifica la actualización.
        update(testUser);

        // Llama al método 'search' para buscar usuarios por el nombre del usuario de prueba y verifica que se encuentre.
        search(testUser);

        // Restablece la contraseña original del usuario de prueba antes de intentar la autenticación exitosa.
        testUser.setPasswordHash(user.getPasswordHash());
        // Llama al método 'autenticate' para verificar que el usuario puede autenticarse con sus credenciales correctas.
        autenticate(testUser);

        // Intenta autenticar al usuario con una contraseña incorrecta para verificar el fallo de autenticación.
        testUser.setPasswordHash("12345");
        autenticacionFails(testUser);

        // Intenta actualizar la contraseña del usuario de prueba con una nueva contraseña.
        testUser.setPasswordHash("new_password"); // Establece la *nueva* contraseña para la actualización.
        updatePassword(testUser); // Llama al método para actualizar la contraseña en la base de datos.
        testUser.setPasswordHash("new_password"); // **Importante:** Actualiza el objeto 'testUser' con la *nueva* contraseña para la siguiente verificación.
        autenticate(testUser); // Verifica que la autenticación sea exitosa con la *nueva* contraseña.


        // Llama al método 'delete' para eliminar el usuario de prueba de la base de datos y verifica la eliminación.
        delete(testUser);
    }
    @Test
    void createUser() throws SQLException {
        User user = new User(0, "admin", "12345", "admin@gmail.com", (byte) 1);
        User res = userDAO.create(user);
        assertNotEquals(res,null);
    }
}
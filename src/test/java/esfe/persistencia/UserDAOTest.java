package esfe.persistencia;

import esfe.dominio.User;
import esfe.utils.PasswordHasher; // Asegúrate de que esta clase esté disponible
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach; // Para asegurar la limpieza después de cada test

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List; // Importar List
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5


class UserDAOTest {
    private UserDAO userDAO; // Instancia de la clase UserDAO que se va a probar.
    private User testUser; // Usuario de prueba para operaciones que lo requieran

    @BeforeEach
    void setUp(){
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba.
        userDAO = new UserDAO();
        // Crear un usuario de prueba único para cada test si es necesario
        // O crear un usuario base y modificarlo en cada test
        Random random = new Random();
        int num = random.nextInt(1000000) + 1; // Número grande para mayor unicidad
        String uniqueEmail = "testuser" + num + "@example.com";
        testUser = new User(0, "TestUser" + num, "testpass", uniqueEmail, (byte) 1);
    }

    @AfterEach
    void tearDown() {
        // Método que se ejecuta después de cada método de prueba (@Test).
        // Su propósito es limpiar los datos de prueba creados para evitar interferencias
        // entre tests y dejar la base de datos en un estado conocido.
        cleanUpTestUser(testUser);
    }

    // --- Métodos Auxiliares para Pruebas (Refinados) ---

    // Crea un usuario en la base de datos y realiza aserciones de creación
    private User createAndAssert(User user) throws SQLException {
        User createdUser = userDAO.create(user);
        assertNotNull(createdUser, "El usuario creado no debería ser nulo.");
        assertTrue(createdUser.getId() > 0, "El ID del usuario creado debería ser mayor que 0.");
        assertEquals(user.getName(), createdUser.getName(), "El nombre del usuario creado debe ser igual al original.");
        assertEquals(user.getEmail(), createdUser.getEmail(), "El email del usuario creado debe ser igual al original.");
        // No asertamos la contraseña hasheada directamente aquí, ya que el DAO la hashea
        assertEquals(user.getStatus(), createdUser.getStatus(), "El status del usuario creado debe ser igual al original.");
        return createdUser;
    }

    // Actualiza un usuario en la base de datos y realiza aserciones de actualización
    private void updateAndAssert(User userToUpdate) throws SQLException {
        // Modifica los atributos del objeto User para simular una actualización.
        userToUpdate.setName(userToUpdate.getName() + "_updated");
        userToUpdate.setEmail("updated_" + userToUpdate.getEmail());
        userToUpdate.setStatus((byte)2); // Cambiar a inactivo

        boolean updated = userDAO.update(userToUpdate);
        assertTrue(updated, "La actualización del usuario debería ser exitosa.");

        // Verificar los cambios recuperando el usuario de la DB
        User fetchedUser = userDAO.getById(userToUpdate.getId());
        assertNotNull(fetchedUser, "El usuario actualizado no debería ser nulo al recuperarlo.");
        assertEquals(userToUpdate.getName(), fetchedUser.getName(), "El nombre del usuario debería haber sido actualizado.");
        assertEquals(userToUpdate.getEmail(), fetchedUser.getEmail(), "El email del usuario debería haber sido actualizado.");
        assertEquals(userToUpdate.getStatus(), fetchedUser.getStatus(), "El status del usuario debería haber sido actualizado.");
    }

    // Obtiene un usuario por ID y realiza aserciones de consistencia
    private void getByIdAndAssert(User expectedUser) throws SQLException {
        User foundUser = userDAO.getById(expectedUser.getId());
        assertNotNull(foundUser, "El usuario obtenido por ID no debería ser nulo.");
        assertEquals(expectedUser.getId(), foundUser.getId(), "El ID del usuario obtenido debe ser igual al esperado.");
        assertEquals(expectedUser.getName(), foundUser.getName(), "El nombre del usuario obtenido debe ser igual al esperado.");
        assertEquals(expectedUser.getEmail(), foundUser.getEmail(), "El email del usuario obtenido debe ser igual al esperado.");
        assertEquals(expectedUser.getStatus(), foundUser.getStatus(), "El status del usuario obtenido debe ser igual al esperado.");
    }

    // Busca usuarios y realiza aserciones sobre los resultados
    private void searchAndAssert(String query, User expectedUserInResults) throws SQLException {
        // CORREGIDO: Usar List<User> para el resultado de userDAO.search()
        List<User> results = userDAO.search(query);
        assertFalse(results.isEmpty(), "La búsqueda debería encontrar al menos un resultado para '" + query + "'.");

        boolean foundSpecificUser = false;
        for (User userItem : results) {
            if (userItem.getId() == expectedUserInResults.getId()) {
                foundSpecificUser = true;
                break;
            }
        }
        assertTrue(foundSpecificUser, "El usuario de prueba '" + expectedUserInResults.getName() + "' no fue encontrado en los resultados de la búsqueda.");
        // Podrías añadir más aserciones aquí, como que todos los nombres en 'results' contengan 'query'
    }

    // Elimina un usuario y realiza aserciones de eliminación
    private void deleteAndAssert(User userToDelete) throws SQLException {
        boolean deleted = userDAO.delete(userToDelete);
        assertTrue(deleted, "La eliminación del usuario debería ser exitosa.");

        User fetchedUserAfterDelete = userDAO.getById(userToDelete.getId());
        assertNull(fetchedUserAfterDelete, "El usuario debería ser nulo después de la eliminación.");
    }

    // Intenta autenticar un usuario con éxito y realiza aserciones
    private void authenticateAndAssert(String email, String password) throws SQLException {
        User authAttemptUser = new User();
        authAttemptUser.setEmail(email);
        authAttemptUser.setPasswordHash(password); // Contraseña sin hashear

        User authenticatedUser = userDAO.authenticate(authAttemptUser);
        assertNotNull(authenticatedUser, "La autenticación debería retornar un usuario no nulo si es exitosa.");
        assertEquals(email, authenticatedUser.getEmail(), "El email del usuario autenticado debe coincidir con el email proporcionado.");
        assertEquals((byte) 1, authenticatedUser.getStatus(), "El status del usuario autenticado debe ser 1 (activo).");
    }

    // Intenta autenticar un usuario con fallo y realiza aserciones
    private void authenticateFailsAndAssert(String email, String password) throws SQLException {
        User authAttemptUser = new User();
        authAttemptUser.setEmail(email);
        authAttemptUser.setPasswordHash(password); // Contraseña sin hashear

        User authenticatedUser = userDAO.authenticate(authAttemptUser);
        assertNull(authenticatedUser, "La autenticación debería fallar y retornar null para credenciales inválidas.");
    }

    // Actualiza la contraseña de un usuario y realiza aserciones
    private void updatePasswordAndAssert(User userToUpdate, String newPassword) throws SQLException {
        User tempUser = new User();
        tempUser.setId(userToUpdate.getId());
        tempUser.setPasswordHash(newPassword); // Nueva contraseña sin hashear

        boolean updated = userDAO.updatePassword(tempUser);
        assertTrue(updated, "La actualización de la contraseña debería ser exitosa.");

        // Verificar autenticación con la nueva contraseña
        authenticateAndAssert(userToUpdate.getEmail(), newPassword);
    }

    // Método auxiliar para limpiar la base de datos de usuarios de prueba
    private void cleanUpTestUser(User user) {
        try {
            // Solo intentar eliminar si el usuario no es nulo y tiene un ID válido
            // Esto es importante si createAndAssert falla y 'user' es nulo
            if (user != null && user.getId() > 0) {
                userDAO.delete(user);
            }
        } catch (SQLException e) {
            System.err.println("Error al limpiar usuario de prueba con ID " + (user != null ? user.getId() : "null") + ": " + e.getMessage());
        }
    }

    // ---------------------- TEST CASES (@Test methods) ----------------------

    @Test
    void testCreateUser() throws SQLException {
        User createdUser = createAndAssert(testUser); // Usar el usuario de testUser inicializado en setUp()
        testUser.setId(createdUser.getId()); // Guardar el ID generado para limpieza en tearDown
    }

    @Test
    void testUpdateUser() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown
        updateAndAssert(createdUser);
    }

    @Test
    void testGetByIdUser() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown
        getByIdAndAssert(createdUser);
    }

    @Test
    void testSearchUserByName() throws SQLException {
        User createdUser1 = createAndAssert(new User(0, "UniqueSearchName", "pass1", "search1@test.com", (byte)1));
        User createdUser2 = createAndAssert(new User(0, "Another UniqueSearch", "pass2", "search2@test.com", (byte)1));

        // Asignar los IDs generados para limpieza en tearDown si los testUser no se usan
        // O si no quieres que tearDown limpie estos, hazlo manualmente aquí en un finally
        // Para este ejemplo, solo usaremos testUser en setUp/tearDown para un solo usuario.
        // Se recomienda que cada test sea autocontenido en su limpieza.
        try {
            searchAndAssert("UniqueSearch", createdUser1);
            searchAndAssert("UniqueSearch", createdUser2); // Verificar que ambos se encuentren
        } finally {
            cleanUpTestUser(createdUser1);
            cleanUpTestUser(createdUser2);
        }
    }

    @Test
    void testDeleteUser() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown

        deleteAndAssert(createdUser);
        // No es necesario llamar a cleanUpTestUser(createdUser) aquí porque deleteAndAssert ya verificó la eliminación
        // y tearDown se encargará si por alguna razón no se eliminó completamente.
    }

    @Test
    void testAuthenticateSuccess() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown
        authenticateAndAssert(createdUser.getEmail(), testUser.getPasswordHash()); // Usar la contraseña original
    }

    @Test
    void testAuthenticateFailsIncorrectPassword() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown
        authenticateFailsAndAssert(createdUser.getEmail(), "wrongpassword");
    }

    @Test
    void testAuthenticateFailsInactiveUser() throws SQLException {
        // Crear usuario inactivo directamente
        User inactiveUser = new User(0, "InactiveUser", "pass", "inactive@test.com", (byte) 2);
        User createdInactiveUser = createAndAssert(inactiveUser);
        // No se asigna a testUser para evitar conflictos con tearDown que espera testUser ser activo

        try {
            authenticateFailsAndAssert(createdInactiveUser.getEmail(), inactiveUser.getPasswordHash());
        } finally {
            cleanUpTestUser(createdInactiveUser);
        }
    }

    @Test
    void testUpdatePassword() throws SQLException {
        User createdUser = createAndAssert(testUser);
        testUser.setId(createdUser.getId()); // Guardar ID para tearDown

        String newPassword = "new_secure_password";
        updatePasswordAndAssert(createdUser, newPassword);
    }
}
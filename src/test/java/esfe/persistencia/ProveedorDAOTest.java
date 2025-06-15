package esfe.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import esfe.dominio.Proveedor; // Asegúrate de importar la clase Proveedor

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ProveedorDAOTest {
    private ProveedorDAO proveedorDAO; // Cambiar a ProveedorDAO

    @BeforeEach
    void setUp() {
        proveedorDAO = new ProveedorDAO(); // Inicializar ProveedorDAO
    }

    private Proveedor create(Proveedor proveedor) throws SQLException {
        Proveedor res = proveedorDAO.create(proveedor);
        assertNotNull(res, "El proveedor creado no debería ser nulo.");
        assertEquals(proveedor.getNombre(), res.getNombre(), "El nombre debe coincidir.");
        assertEquals(proveedor.getTelefono(), res.getTelefono(), "El teléfono debe coincidir.");
        assertEquals(proveedor.getEmail(), res.getEmail(), "El email debe coincidir.");
        assertEquals(proveedor.getDireccion(), res.getDireccion(), "La dirección debe coincidir.");
        return res;
    }

    private void update(Proveedor proveedor) throws SQLException {
        proveedor.setNombre(proveedor.getNombre() + "_editado");
        proveedor.setTelefono(proveedor.getTelefono() + "_editado");
        proveedor.setEmail(proveedor.getEmail() + "_editado");
        proveedor.setDireccion(proveedor.getDireccion() + "_editado");

        boolean res = proveedorDAO.update(proveedor);
        assertTrue(res, "La actualización debería ser exitosa.");

        getById(proveedor);
    }

    private void getById(Proveedor proveedor) throws SQLException {
        Proveedor res = proveedorDAO.getById(proveedor.getProveedorID());

        assertNotNull(res, "No se encontró el proveedor por ID.");
        assertEquals(proveedor.getProveedorID(), res.getProveedorID());
        assertEquals(proveedor.getNombre(), res.getNombre());
        assertEquals(proveedor.getTelefono(), res.getTelefono());
        assertEquals(proveedor.getEmail(), res.getEmail());
        assertEquals(proveedor.getDireccion(), res.getDireccion());
    }

    private void search(Proveedor proveedor) throws SQLException {
        ArrayList<Proveedor> proveedores = proveedorDAO.search(proveedor.getNombre());
        boolean encontrada = false;

        for (Proveedor p : proveedores) {
            if (p.getNombre().contains(proveedor.getNombre())) {
                encontrada = true;
                break; // Salir del bucle si encontramos el proveedor
            }
        }

        assertTrue(encontrada, "El proveedor buscado no fue encontrado: " + proveedor.getNombre());
    }

    private void delete(Proveedor proveedor) throws SQLException {
        boolean res = proveedorDAO.delete(proveedor.getProveedorID());
        assertTrue(res, "La eliminación debería ser exitosa.");

        Proveedor resultado = proveedorDAO.getById(proveedor.getProveedorID());
        assertNull(resultado, "El proveedor debería haber sido eliminado.");
    }

    @Test
    void testProveedorDAO() throws SQLException {
        Random rand = new Random();
        Proveedor proveedor = new Proveedor(0, "Proveedor", "123456789", "proveedor@example.com", "Direccion");

        Proveedor testProveedor = create(proveedor);
        update(testProveedor);
        search(testProveedor);
        delete(testProveedor);
    }
}
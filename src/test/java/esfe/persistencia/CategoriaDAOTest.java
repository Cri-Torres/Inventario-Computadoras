package esfe.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import esfe.dominio.Categoria;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaDAOTest {
    private CategoriaDAO categoriaDAO;

    @BeforeEach
    void setUp() {
        categoriaDAO = new CategoriaDAO();
    }

    private Categoria create(Categoria categoria) throws SQLException {
        Categoria res = categoriaDAO.create(categoria);
        assertNotNull(res, "La categoría creada no debería ser nula.");
        assertEquals(categoria.getNombre(), res.getNombre(), "El nombre debe coincidir.");
        assertEquals(categoria.getDescripcion(), res.getDescripcion(), "La descripcion debe coincidir.");
        return res;
    }

    private void update(Categoria categoria) throws SQLException {
        categoria.setNombre(categoria.getNombre() + "_editado");
        categoria.setDescripcion(categoria.getDescripcion() + "_editado");

        boolean res = categoriaDAO.update(categoria);
        assertTrue(res, "La actualización debería ser exitosa.");

        getById(categoria);
    }

    private void getById(Categoria categoria) throws SQLException {
        Categoria res = categoriaDAO.getById(categoria.getCategoriaID());

        assertNotNull(res, "No se encontró la categoría por ID.");
        assertEquals(categoria.getCategoriaID(), res.getCategoriaID());
        assertEquals(categoria.getNombre(), res.getNombre());
        assertEquals(categoria.getDescripcion(), res.getDescripcion());
    }

    private void search(Categoria categoria) throws SQLException {
        ArrayList<Categoria> categorias = categoriaDAO.search(categoria.getNombre());
        boolean encontrada = false;

        for (Categoria c : categorias) {
            if (c.getNombre().contains(categoria.getNombre())) {
                encontrada = true;
                break; // Salir del bucle si encontramos la categoría
            }
        }

        assertTrue(encontrada, "La categoría buscada no fue encontrada: " + categoria.getNombre());
    }

    private void delete(Categoria categoria) throws SQLException {
        // Llamar al método delete con el ID de la categoría
        boolean res = categoriaDAO.delete(categoria.getCategoriaID());
        assertTrue(res, "La eliminación debería ser exitosa.");

        Categoria resultado = categoriaDAO.getById(categoria.getCategoriaID());
        assertNull(resultado, "La categoría debería haber sido eliminada.");
    }

    @Test
    void testCategoriaDAO() throws SQLException {
        Random rand = new Random();
        Categoria categoria = new Categoria(0, "Categoria", "Descripcion");

        Categoria testCategoria = create(categoria);
        update(testCategoria);
        search(testCategoria);
        delete(testCategoria);
    }
}
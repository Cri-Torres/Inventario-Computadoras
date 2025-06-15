package esfe.dominio;

public class Categoria {
    private int categoriaID; // Asegúrate de que es categoriaID (con ID mayúsculas) si así lo usas en otros lugares
    private String nombre;
    private String descripcion;

    public Categoria() {
    }

    public Categoria(int categoriaID, String nombre, String descripcion) {
        this.categoriaID = categoriaID;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getCategoriaID() { // Asegúrate de que este getter sea consistente (CategoriaID o CategoriaId)
        return categoriaID;
    }

    public void setCategoriaID(int categoriaID) { // Asegúrate de que este setter sea consistente
        this.categoriaID = categoriaID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Este método es crucial para que el JComboBox muestre el nombre de la categoría
     * en lugar de la representación por defecto del objeto.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
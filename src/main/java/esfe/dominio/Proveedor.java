package esfe.dominio;

public class Proveedor {
    private Integer proveedorID; // CORRECCIÓN: Cambiado a Integer (objeto)
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;

    public Proveedor() {
    }

    public Proveedor(Integer proveedorID, String nombre, String telefono, String email, String direccion) { // CORRECCIÓN: Integer
        this.proveedorID = proveedorID;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    // --- Getters y Setters ---

    public Integer getProveedorID() { // CORRECCIÓN: getProveedorID (con ID en mayúscula para consistencia)
        return proveedorID;
    }

    public void setProveedorID(Integer proveedorID) { // CORRECCIÓN: setProveedorID (con ID en mayúscula)
        this.proveedorID = proveedorID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getDireccion(){
        return direccion;
    }

    public void setDireccion(String direccion){
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        // Es crucial para que el JComboBox muestre el nombre del proveedor
        return nombre;
    }
}

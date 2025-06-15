package esfe.dominio;

import java.time.LocalDateTime;

public class Computadora {
    private int computadoraID;
    private int categoriaID; // Relación con Categoria
    private Integer proveedorID; // Puede ser nulo, relación con Proveedor
    private String marca;
    private String modelo;
    private String numeroSerie;
    private LocalDateTime fechaCompra; // Usamos LocalDateTime para fecha y hora
    private double precio;
    private byte estado; // 1: Disponible, 2: Agotado
    private String observaciones;

    // Constantes para los estados de la computadora
    public static final byte ESTADO_DISPONIBLE = 1;
    public static final byte ESTADO_AGOTADO = 2;

    /**
     * Constructor por defecto.
     */
    public Computadora() {
    }

    /**
     * Constructor completo para crear un objeto Computadora,
     * útil cuando se recupera de la base de datos.
     */
    public Computadora(int computadoraID, int categoriaID, Integer proveedorID, String marca, String modelo,
                       String numeroSerie, LocalDateTime fechaCompra, double precio, byte estado, String observaciones) {
        this.computadoraID = computadoraID;
        this.categoriaID = categoriaID;
        this.proveedorID = proveedorID;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.fechaCompra = fechaCompra;
        this.precio = precio;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    /**
     * Constructor para crear un nuevo objeto Computadora antes de ser insertado en la base de datos.
     * No incluye computadoraID (generado por la DB) ni fechaCompra (se asignará automáticamente al registrar).
     * El estado por defecto será DISPONIBLE.
     */
    public Computadora(int categoriaID, Integer proveedorID, String marca, String modelo,
                       String numeroSerie, double precio, String observaciones) {
        this.categoriaID = categoriaID;
        this.proveedorID = proveedorID;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.precio = precio;
        this.estado = ESTADO_DISPONIBLE; // Por defecto al crear una nueva, se asume disponible
        this.observaciones = observaciones;
    }

    // --- Getters y Setters ---

    public int getComputadoraID() {
        return computadoraID;
    }

    public void setComputadoraID(int computadoraID) {
        this.computadoraID = computadoraID;
    }

    public int getCategoriaID() {
        return categoriaID;
    }

    public void setCategoriaID(int categoriaID) {
        this.categoriaID = categoriaID;
    }

    public Integer getProveedorID() {
        return proveedorID;
    }

    public void setProveedorID(Integer proveedorID) {
        this.proveedorID = proveedorID;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public byte getEstado() {
        return estado;
    }

    public void setEstado(byte estado) {
        this.estado = estado;
    }

    /**
     * Devuelve la descripción en String del estado actual de la computadora.
     */
    public String getStrEstado() {
        String str = "";
        switch (estado) {
            case ESTADO_DISPONIBLE:
                str = "Disponible";
                break;
            case ESTADO_AGOTADO:
                str = "Agotado";
                break;
            default:
                str = "Desconocido"; // En caso de un valor de estado no reconocido
        }
        return str;
    }

    /**
     * Devuelve la descripción en String de un valor de estado dado.
     */
    public String getStrEstado(byte estadoValue) {
        String str = "";
        switch (estadoValue) {
            case ESTADO_DISPONIBLE:
                str = "Disponible";
                break;
            case ESTADO_AGOTADO:
                str = "Agotado";
                break;
            default:
                str = "Desconocido";
        }
        return str;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Sobrescribe el método toString() para proporcionar una representación legible
     * de la computadora, que será utilizada por JComboBox y otros componentes de UI.
     */
    @Override
    public String toString() {
        return marca + " - " + modelo + " (SN: " + numeroSerie + ")";
    }
}

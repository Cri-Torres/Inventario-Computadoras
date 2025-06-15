package esfe.dominio;

import java.time.LocalDateTime;

public class MovimientoInventario {
    private int movimientoID;
    private int computadoraID; // Clave foránea a la tabla Computadoras
    private byte tipoMovimiento; // 1: Entrada, 2: Salida, 3: Mantenimiento
    private int cantidad;
    private LocalDateTime fechaMovimiento;
    private String descripcion;

    // Constantes para los tipos de movimiento
    public static final byte TIPO_ENTRADA = 1;
    public static final byte TIPO_SALIDA = 2;
    public static final byte TIPO_MANTENIMIENTO = 3;

    /**
     * Constructor por defecto.
     */
    public MovimientoInventario() {
        this.cantidad = 1; // Establecer valor por defecto consistente con la DB
    }

    /**
     * Constructor completo para crear un objeto MovimientoInventario,
     * útil cuando se recupera desde la base de datos.
     *
     * @param movimientoID ID único del movimiento.
     * @param computadoraID ID de la computadora asociada al movimiento.
     * @param tipoMovimiento Tipo de movimiento (Entrada, Salida, Mantenimiento).
     * @param cantidad Cantidad de unidades afectadas por el movimiento.
     * @param fechaMovimiento Fecha y hora en que ocurrió el movimiento.
     * @param descripcion Descripción adicional del movimiento.
     */
    public MovimientoInventario(int movimientoID, int computadoraID, byte tipoMovimiento,
                                int cantidad, LocalDateTime fechaMovimiento, String descripcion) {
        this.movimientoID = movimientoID;
        this.computadoraID = computadoraID;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fechaMovimiento = fechaMovimiento;
        this.descripcion = descripcion;
    }

    /**
     * Constructor para crear un nuevo objeto MovimientoInventario antes de ser insertado
     * en la base de datos. El MovimientoID se generará automáticamente por la DB
     * y la FechaMovimiento se asignará automáticamente al registrar.
     * La cantidad por defecto es 1.
     *
     * @param computadoraID ID de la computadora asociada al movimiento.
     * @param tipoMovimiento Tipo de movimiento (Entrada, Salida, Mantenimiento).
     * @param cantidad Cantidad de unidades afectadas por el movimiento (usa 1 si no se especifica).
     * @param descripcion Descripción adicional del movimiento.
     */
    public MovimientoInventario(int computadoraID, byte tipoMovimiento, int cantidad, String descripcion) {
        this.computadoraID = computadoraID;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        // fechaMovimiento se asignará en el DAO al momento de la creación
    }

    /**
     * Constructor alternativo si la cantidad no se especifica, asumiendo 1 por defecto.
     *
     * @param computadoraID ID de la computadora asociada al movimiento.
     * @param tipoMovimiento Tipo de movimiento (Entrada, Salida, Mantenimiento).
     * @param descripcion Descripción adicional del movimiento.
     */
    public MovimientoInventario(int computadoraID, byte tipoMovimiento, String descripcion) {
        this(computadoraID, tipoMovimiento, 1, descripcion); // Llama al constructor anterior con cantidad = 1
    }


    // --- Getters y Setters ---

    public int getMovimientoID() {
        return movimientoID;
    }

    public void setMovimientoID(int movimientoID) {
        this.movimientoID = movimientoID;
    }

    public int getComputadoraID() {
        return computadoraID;
    }

    public void setComputadoraID(int computadoraID) {
        this.computadoraID = computadoraID;
    }

    public byte getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(byte tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Devuelve la descripción en String del tipo de movimiento actual.
     */
    public String getStrTipoMovimiento() {
        return getStrTipoMovimiento(this.tipoMovimiento);
    }

    /**
     * Devuelve la descripción en String de un tipo de movimiento dado.
     * @param tipoValue El valor byte del tipo de movimiento.
     * @return La descripción en String del tipo de movimiento.
     */
    public String getStrTipoMovimiento(byte tipoValue) {
        String str = "";
        switch (tipoValue) {
            case TIPO_ENTRADA:
                str = "Entrada";
                break;
            case TIPO_SALIDA:
                str = "Salida";
                break;
            case TIPO_MANTENIMIENTO:
                str = "Mantenimiento";
                break;
            default:
                str = "Desconocido";
        }
        return str;
    }
}

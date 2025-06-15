package esfe.presentacion;

import esfe.dominio.MovimientoInventario;
import esfe.dominio.Computadora;
import esfe.persistencia.MovimientoInventarioDAO;
import esfe.persistencia.ComputadoraDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Font; // Para cambiar la fuente

public class MovimientoInventarioWriteForm extends JDialog {
    private JPanel mainPanel;
    private JComboBox<Computadora> cbComputadora;
    private JComboBox<String> cbTipoMovimiento;
    private JTextField txtCantidad;
    private JTextArea txtDescripcion;
    private JButton btnOk;
    private JButton btnCancel;
    private JLabel lblFechaMovimiento;

    private final MovimientoInventarioDAO movimientoInventarioDAO;
    private final ComputadoraDAO computadoraDAO;
    private final MovimientoInventario movimientoActual;
    private final MainForm mainForm;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MovimientoInventarioWriteForm(MainForm mainForm, MovimientoInventario movimiento) {
        this.mainForm = mainForm;
        this.movimientoActual = movimiento;

        movimientoInventarioDAO = new MovimientoInventarioDAO();
        computadoraDAO = new ComputadoraDAO();

        setContentPane(mainPanel);
        setModal(true);
        setTitle("Registrar/Editar Movimiento de Inventario");
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(550, 450)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Detalles del Movimiento", // Título del borde
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 14) // Fuente para el título del borde
                        )
                )
        );

        // 2. Ajustar el tamaño del JTextArea para la descripción
        txtDescripcion.setRows(4); // Establece 4 filas de altura
        txtDescripcion.setLineWrap(true); // Permite que el texto salte de línea
        txtDescripcion.setWrapStyleWord(true); // Salta de línea por palabras

        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);
        btnOk.setFont(buttonFont);
        btnCancel.setFont(buttonFont);
        btnOk.setBackground(new java.awt.Color(70, 130, 180)); // Color azul acero
        btnOk.setForeground(java.awt.Color.WHITE); // Texto blanco
        btnCancel.setBackground(new java.awt.Color(220, 20, 60)); // Color rojo carmesí
        btnCancel.setForeground(java.awt.Color.WHITE);

        // 4. Establecer un borde sutil para los JTextField y JComboBoxes para mejor visualización
        // Esto asume que tienes acceso directo a estos componentes en el .form.
        // Si no, tendrías que aplicar esto en cada componente individualmente.
        // Ejemplo para un campo: txtCantidad.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));

        // --- FIN: MEJORAS DE DISEÑO ---

        populateComboBoxes();
        loadMovimientoData();

        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMovimiento();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void populateComboBoxes() {
        try {
            List<Computadora> computadoras = computadoraDAO.getAllComputadoras();
            if (computadoras.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay computadoras registradas. Por favor, registre una computadora primero para poder crear movimientos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                btnOk.setEnabled(false);
            }
            for (Computadora comp : computadoras) {
                cbComputadora.addItem(comp);
            }

            cbTipoMovimiento.addItem(movimientoActual.getStrTipoMovimiento(MovimientoInventario.TIPO_ENTRADA));
            cbTipoMovimiento.addItem(movimientoActual.getStrTipoMovimiento(MovimientoInventario.TIPO_SALIDA));
            cbTipoMovimiento.addItem(movimientoActual.getStrTipoMovimiento(MovimientoInventario.TIPO_MANTENIMIENTO));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos para los ComboBoxes: " + ex.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadMovimientoData() {
        if (movimientoActual.getMovimientoID() > 0) {
            setTitle("Editar Movimiento de Inventario");
            txtCantidad.setText(String.valueOf(movimientoActual.getCantidad()));
            txtDescripcion.setText(movimientoActual.getDescripcion());

            if (movimientoActual.getFechaMovimiento() != null) {
                lblFechaMovimiento.setText(movimientoActual.getFechaMovimiento().format(DATE_TIME_FORMATTER));
            } else {
                lblFechaMovimiento.setText("N/A");
            }

            for (int i = 0; i < cbComputadora.getItemCount(); i++) {
                Computadora comp = cbComputadora.getItemAt(i);
                if (comp != null && comp.getComputadoraID() == movimientoActual.getComputadoraID()) {
                    cbComputadora.setSelectedItem(comp);
                    break;
                }
            }

            cbTipoMovimiento.setSelectedItem(movimientoActual.getStrTipoMovimiento());

        } else {
            setTitle("Registrar Nuevo Movimiento de Inventario");
            txtCantidad.setText("1");
            txtDescripcion.setText("");
            lblFechaMovimiento.setText("Se generará automáticamente al guardar");

            if (cbComputadora.getItemCount() > 0) cbComputadora.setSelectedIndex(0);
            if (cbTipoMovimiento.getItemCount() > 0) cbTipoMovimiento.setSelectedIndex(0);
        }
    }

    private void saveMovimiento() {
        if (cbComputadora.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una Computadora.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtCantidad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La Cantidad no puede estar vacía.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cbTipoMovimiento.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tipo de Movimiento.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Computadora selectedComputadora = (Computadora) cbComputadora.getSelectedItem();
            movimientoActual.setComputadoraID(selectedComputadora.getComputadoraID());

            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser un número positivo.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            movimientoActual.setCantidad(cantidad);

            String tipoMovimientoStr = (String) cbTipoMovimiento.getSelectedItem();
            if (tipoMovimientoStr != null) {
                if (tipoMovimientoStr.equals("Entrada")) {
                    movimientoActual.setTipoMovimiento(MovimientoInventario.TIPO_ENTRADA);
                } else if (tipoMovimientoStr.equals("Salida")) {
                    movimientoActual.setTipoMovimiento(MovimientoInventario.TIPO_SALIDA);
                } else if (tipoMovimientoStr.equals("Mantenimiento")) {
                    movimientoActual.setTipoMovimiento(MovimientoInventario.TIPO_MANTENIMIENTO);
                }
            }
            movimientoActual.setDescripcion(txtDescripcion.getText().trim());

            if (movimientoActual.getMovimientoID() == 0) {
                MovimientoInventario created = movimientoInventarioDAO.create(movimientoActual);
                if (created != null) {
                    JOptionPane.showMessageDialog(this, "Movimiento de inventario registrado exitosamente con ID: " + created.getMovimientoID(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo registrar el movimiento de inventario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                boolean updated = movimientoInventarioDAO.update(movimientoActual);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Movimiento de inventario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el movimiento de inventario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error de formato en la Cantidad. Asegúrate de que sea un número entero válido.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar el movimiento: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al guardar el movimiento: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
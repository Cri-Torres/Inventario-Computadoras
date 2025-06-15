package esfe.presentacion;

import esfe.dominio.MovimientoInventario;
import esfe.persistencia.MovimientoInventarioDAO;
import esfe.persistencia.ComputadoraDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Para estilizar el encabezado de la tabla
import javax.swing.border.EmptyBorder; // Para padding
import javax.swing.border.TitledBorder; // Para título del borde
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class MovimientoInventarioForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName; // Campo para buscar movimientos (por descripción)
    private JButton btnCreate;
    private JTable tableMoviminentoInventario; // Tabla para mostrar los movimientos
    private JButton btnUpdate;
    private JButton btnDelete;

    private MovimientoInventarioDAO movimientoInventarioDAO;
    private ComputadoraDAO computadoraDAO;
    private MainForm mainForm;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MovimientoInventarioForm(MainForm mainForm) {
        this.mainForm = mainForm;
        movimientoInventarioDAO = new MovimientoInventarioDAO();
        computadoraDAO = new ComputadoraDAO();

        setContentPane(mainPanel);
        setModal(true);
        setTitle("Lista de Movimientos de Inventario");
        // Establecer un tamaño preferido para la ventana de listado
        setPreferredSize(new java.awt.Dimension(800, 600)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Gestión de Movimientos de Inventario", // Título del borde
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 16), // Fuente para el título del borde
                                new Color(50, 50, 50) // Color del texto del título
                        )
                )
        );

        // 2. Mejorar la apariencia del campo de búsqueda
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 8, 5, 8) // Padding interno
        ));

        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);

        btnCreate.setFont(buttonFont);
        btnCreate.setBackground(new Color(60, 179, 113)); // Color verde menta
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false); // Quitar el borde de foco
        btnCreate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 150, 90)), // Borde ligeramente más oscuro
                new EmptyBorder(8, 15, 8, 15) // Padding interno
        ));

        btnUpdate.setFont(buttonFont);
        btnUpdate.setBackground(new Color(70, 130, 180)); // Color azul acero
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 110, 150)),
                new EmptyBorder(8, 15, 8, 15)
        ));

        btnDelete.setFont(buttonFont);
        btnDelete.setBackground(new Color(220, 20, 60)); // Color rojo carmesí
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 15, 50)),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // 4. Mejorar el estilo de la tabla
        tableMoviminentoInventario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMoviminentoInventario.setRowHeight(25); // Altura de las filas
        tableMoviminentoInventario.setGridColor(new Color(230, 230, 230)); // Color de las líneas de la cuadrícula
        tableMoviminentoInventario.setSelectionBackground(new Color(173, 216, 230)); // Color de selección (azul claro)
        tableMoviminentoInventario.setFillsViewportHeight(true); // Para que la tabla ocupe todo el espacio disponible

        // Estilo del encabezado de la tabla
        JTableHeader tableHeader = tableMoviminentoInventario.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(50, 50, 50));
        tableHeader.setReorderingAllowed(false); // Evitar que el usuario reordene columnas
        tableHeader.setResizingAllowed(true); // Permitir redimensionar columnas

        // Añadir JScrollPane para la tabla (si no lo tienes ya en el .form)
        // Esto es crucial para que la tabla tenga barras de desplazamiento si los datos exceden el tamaño visible
        // Si tu mainPanel ya contiene un JScrollPane que envuelve la tabla, esta línea no es necesaria.
        // Si tu mainPanel contiene directamente la JTable, necesitarías algo como:
        // JScrollPane scrollPane = new JScrollPane(tableMoviminentoInventario);
        // mainPanel.add(scrollPane, BorderLayout.CENTER); // O el layout manager adecuado

        // --- FIN: MEJORAS DE DISEÑO ---

        // Listener para buscar movimientos
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtName.getText().trim().isEmpty()) {
                    search(txtName.getText());
                } else {
                    refreshTable();
                }
            }
        });

        // ActionListener para crear un nuevo movimiento
        btnCreate.addActionListener(s -> {
            MovimientoInventario nuevoMovimiento = new MovimientoInventario();
            MovimientoInventarioWriteForm writeForm = new MovimientoInventarioWriteForm(this.mainForm, nuevoMovimiento);
            writeForm.setVisible(true);
            refreshTable();
        });

        // ActionListener para actualizar un movimiento
        btnUpdate.addActionListener(s -> {
            MovimientoInventario movimiento = getSelectedMovimiento();
            if (movimiento != null) {
                MovimientoInventarioWriteForm writeForm = new MovimientoInventarioWriteForm(this.mainForm, movimiento);
                writeForm.setVisible(true);
                refreshTable();
            }
        });

        // ActionListener para eliminar un movimiento
        btnDelete.addActionListener(s -> {
            MovimientoInventario movimiento = getSelectedMovimiento();
            if (movimiento != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este movimiento de inventario?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean deleted = movimientoInventarioDAO.delete(movimiento.getMovimientoID());
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "Movimiento eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            refreshTable();
                        } else {
                            JOptionPane.showMessageDialog(this, "No se pudo eliminar el movimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al eliminar el movimiento: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });

        refreshTable();
    }

    private void search(String query) {
        try {
            List<MovimientoInventario> movimientos = movimientoInventarioDAO.search(query);
            createTable(movimientos);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar movimientos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void createTable(List<MovimientoInventario> movimientos) {
        String[] columnNames = {"ID", "ID Computadora", "Tipo Movimiento", "Cantidad", "Fecha Movimiento", "Descripción"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tableMoviminentoInventario.setModel(model);

        for (MovimientoInventario movimiento : movimientos) {
            Object[] rowData = new Object[]{
                    movimiento.getMovimientoID(),
                    movimiento.getComputadoraID(),
                    movimiento.getStrTipoMovimiento(),
                    movimiento.getCantidad(),
                    movimiento.getFechaMovimiento() != null ? movimiento.getFechaMovimiento().format(DATE_TIME_FORMATTER) : "N/A",
                    movimiento.getDescripcion()
            };
            model.addRow(rowData);
        }

        hideCol(0); // Ocultar la columna "ID"
    }

    private void hideCol(int columnIndex) {
        this.tableMoviminentoInventario.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableMoviminentoInventario.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        this.tableMoviminentoInventario.getTableHeader().getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableMoviminentoInventario.getTableHeader().getColumnModel().getColumn(columnIndex).setMinWidth(0);
    }

    private MovimientoInventario getSelectedMovimiento() {
        int row = tableMoviminentoInventario.getSelectedRow();
        if (row != -1) {
            try {
                int id = (int) tableMoviminentoInventario.getModel().getValueAt(row, 0);
                return movimientoInventarioDAO.getById(id);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al obtener los detalles del movimiento seleccionado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                return null;
            }
        }
        JOptionPane.showMessageDialog(this, "Por favor, seleccione un movimiento de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void refreshTable() {
        if (!txtName.getText().trim().isEmpty()) {
            search(txtName.getText());
        } else {
            try {
                List<MovimientoInventario> allMovimientos = movimientoInventarioDAO.getAllMovimientoInventario();
                createTable(allMovimientos);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar todos los movimientos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

package esfe.presentacion;

import esfe.persistencia.ComputadoraDAO;
import esfe.persistencia.CategoriaDAO;
import esfe.persistencia.ProveedorDAO;
import esfe.dominio.Computadora;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Para estilizar el encabezado de la tabla
import javax.swing.border.EmptyBorder; // Para padding
import javax.swing.border.TitledBorder; // Para título del borde
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException; // Importar SQLException
import java.time.format.DateTimeFormatter; // Para formatear LocalDateTime
import java.util.ArrayList; // Mantener ArrayList si el DAO lo devuelve así
import java.util.List; // Usar List en la interfaz para flexibilidad
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class ComputadoraForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName; // Campo para buscar computadoras
    private JButton btnCreate;
    private JTable tableComputadora; // Tabla para mostrar las computadoras
    private JButton btnUpdate;
    private JButton btnDelete;

    private ComputadoraDAO computadoraDAO;
    private CategoriaDAO categoriaDAO;
    private ProveedorDAO proveedorDAO;
    private MainForm mainForm;

    // Formateador para la fecha/hora en la tabla
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ComputadoraForm(MainForm mainForm) {
        this.mainForm = mainForm;
        computadoraDAO = new ComputadoraDAO();
        categoriaDAO = new CategoriaDAO();
        proveedorDAO = new ProveedorDAO();

        setContentPane(mainPanel);
        setModal(true);
        setTitle("Lista de Computadoras");
        // Establecer un tamaño preferido para la ventana de listado
        setPreferredSize(new java.awt.Dimension(1000, 700)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Gestión de Computadoras", // Título del borde
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
                BorderFactory.createLineBorder(new Color(50, 150, 90)),
                new EmptyBorder(8, 15, 8, 15)
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
        tableComputadora.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableComputadora.setRowHeight(25); // Altura de las filas
        tableComputadora.setGridColor(new Color(230, 230, 230)); // Color de las líneas de la cuadrícula
        tableComputadora.setSelectionBackground(new Color(173, 216, 230)); // Color de selección (azul claro)
        tableComputadora.setFillsViewportHeight(true); // Para que la tabla ocupe todo el espacio disponible

        // Estilo del encabezado de la tabla
        JTableHeader tableHeader = tableComputadora.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(50, 50, 50));
        tableHeader.setReorderingAllowed(false); // Evitar que el usuario reordene columnas
        tableHeader.setResizingAllowed(true); // Permitir redimensionar columnas

        // Si no tienes JScrollPane en tu .form para la tabla, es crucial envolverla:
        // JScrollPane scrollPane = new JScrollPane(tableComputadora);
        // // Asume que mainPanel usa BorderLayout, si no, ajusta al layout manager de tu panel
        // mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- FIN: MEJORAS DE DISEÑO ---

        // Listener para buscar computadoras
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

        // ActionListener para crear una nueva computadora
        btnCreate.addActionListener(s -> {
            Computadora nuevaComputadora = new Computadora();
            nuevaComputadora.setEstado(Computadora.ESTADO_DISPONIBLE);

            ComputadoraWriteForm writeForm = new ComputadoraWriteForm(this.mainForm, nuevaComputadora);
            writeForm.setVisible(true);
            refreshTable();
        });

        // ActionListener para actualizar una computadora
        btnUpdate.addActionListener(s -> {
            Computadora computadora = getSelectedComputadora();
            if (computadora != null) {
                ComputadoraWriteForm writeForm = new ComputadoraWriteForm(this.mainForm, computadora);
                writeForm.setVisible(true);
                refreshTable();
            }
        });

        // ActionListener para eliminar una computadora
        btnDelete.addActionListener(s -> {
            Computadora computadora = getSelectedComputadora();
            if (computadora != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar la computadora con SN: " + computadora.getNumeroSerie() + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean deleted = computadoraDAO.delete(computadora.getComputadoraID());
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "Computadora eliminada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            refreshTable();
                        } else {
                            JOptionPane.showMessageDialog(this, "No se pudo eliminar la computadora.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al eliminar la computadora: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Inicializar la tabla al abrir el formulario
        refreshTable();
    }

    private void search(String query) {
        try {
            // Se mantiene ArrayList aquí si el DAO devuelve ArrayList,
            // pero se recomienda usar List en la interfaz del método en el DAO.
            ArrayList<Computadora> computadoras = computadoraDAO.search(query);
            createTable(computadoras);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar computadoras: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Se cambió el parámetro a List para mayor flexibilidad, aunque el DAO pueda devolver ArrayList
    public void createTable(List<Computadora> computadoras) {
        String[] columnNames = {"ID", "Categoría ID", "Proveedor ID", "Marca", "Modelo", "Número de Serie", "Fecha Compra", "Precio", "Estado", "Observaciones"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tableComputadora.setModel(model);

        for (Computadora computadora : computadoras) {
            Object[] rowData = new Object[]{
                    computadora.getComputadoraID(),
                    computadora.getCategoriaID(),
                    computadora.getProveedorID(),
                    computadora.getMarca(),
                    computadora.getModelo(),
                    computadora.getNumeroSerie(),
                    computadora.getFechaCompra() != null ? computadora.getFechaCompra().format(DATE_TIME_FORMATTER) : "N/A",
                    String.format("%.2f", computadora.getPrecio()),
                    computadora.getStrEstado(),
                    computadora.getObservaciones()
            };
            model.addRow(rowData);
        }

        hideCol(0); // Ocultar la columna "ID"
    }

    private void hideCol(int columnIndex) {
        this.tableComputadora.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableComputadora.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        this.tableComputadora.getTableHeader().getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableComputadora.getTableHeader().getColumnModel().getColumn(columnIndex).setMinWidth(0);
    }

    private Computadora getSelectedComputadora() {
        int row = tableComputadora.getSelectedRow();
        if (row != -1) {
            try {
                int id = (int) tableComputadora.getModel().getValueAt(row, 0);
                return computadoraDAO.getById(id);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al obtener los detalles de la computadora seleccionada: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                return null;
            }
        }
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una computadora de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void refreshTable() {
        // Se asegura de mostrar todas las computadoras si el campo de búsqueda está vacío
        if (!txtName.getText().trim().isEmpty()) {
            search(txtName.getText());
        } else {
            try {
                List<Computadora> allComputadoras = computadoraDAO.getAllComputadoras(); // Asegúrate de que este método exista en ComputadoraDAO
                createTable(allComputadoras);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar todas las computadoras: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

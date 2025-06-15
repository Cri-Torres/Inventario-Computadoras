package esfe.presentacion;

import esfe.persistencia.ProveedorDAO;
import esfe.dominio.Proveedor;
import esfe.utils.CUD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Para estilizar el encabezado de la tabla
import javax.swing.border.EmptyBorder; // Para padding
import javax.swing.border.TitledBorder; // Para título del borde
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.util.ArrayList; // Mantener ArrayList si el DAO lo devuelve así
import java.util.List; // Usar List en la interfaz para flexibilidad
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class ProveedorForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName; // Campo para buscar por nombre
    private JButton btnCreate;
    private JTable tableProveedor; // Tabla para mostrar los proveedores
    private JButton btnUpdate;
    private JButton btnDelete;

    private ProveedorDAO proveedorDAO;
    private MainForm mainForm;

    public ProveedorForm(MainForm mainForm) {
        this.mainForm = mainForm;
        proveedorDAO = new ProveedorDAO();
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Gestión de Proveedores"); // Título más descriptivo
        // Establecer un tamaño preferido para la ventana de listado
        setPreferredSize(new java.awt.Dimension(700, 500)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Gestión de Proveedores", // Título del borde
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
        tableProveedor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableProveedor.setRowHeight(25); // Altura de las filas
        tableProveedor.setGridColor(new Color(230, 230, 230)); // Color de las líneas de la cuadrícula
        tableProveedor.setSelectionBackground(new Color(173, 216, 230)); // Color de selección (azul claro)
        tableProveedor.setFillsViewportHeight(true); // Para que la tabla ocupe todo el espacio disponible

        // Estilo del encabezado de la tabla
        JTableHeader tableHeader = tableProveedor.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(50, 50, 50));
        tableHeader.setReorderingAllowed(false); // Evitar que el usuario reordene columnas
        tableHeader.setResizingAllowed(true); // Permitir redimensionar columnas

        // Si no tienes JScrollPane en tu .form para la tabla, es crucial envolverla:
        // JScrollPane scrollPane = new JScrollPane(tableProveedor);
        // // Asume que mainPanel usa BorderLayout, si no, ajusta al layout manager de tu panel
        // mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- FIN: MEJORAS DE DISEÑO ---

        // Listener para buscar por nombre
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtName.getText().trim().isEmpty()) {
                    search(txtName.getText());
                } else {
                    refreshTable(); // Refrescar para mostrar todos si el campo está vacío
                }
            }
        });

        // ActionListener para crear un nuevo proveedor
        btnCreate.addActionListener(s -> {
            ProveedorWriteForm proveedorWriteForm = new ProveedorWriteForm(this.mainForm, CUD.CREATE, new Proveedor());
            proveedorWriteForm.setVisible(true);
            refreshTable();
        });

        // ActionListener para actualizar un proveedor
        btnUpdate.addActionListener(s -> {
            Proveedor proveedor = getProveedorFromTableRow();
            if (proveedor != null) {
                ProveedorWriteForm proveedorWriteForm = new ProveedorWriteForm(this.mainForm, CUD.UPDATE, proveedor);
                proveedorWriteForm.setVisible(true);
                refreshTable();
            }
        });

        // ActionListener para eliminar un proveedor
        btnDelete.addActionListener(s -> {
            Proveedor proveedor = getProveedorFromTableRow();
            if (proveedor != null) {
                ProveedorWriteForm proveedorWriteForm = new ProveedorWriteForm(this.mainForm, CUD.DELETE, proveedor);
                proveedorWriteForm.setVisible(true);
                refreshTable();
            }
        });

        // Al iniciar el formulario, mostrar todos los proveedores
        refreshTable();
    }

    private void search(String query) {
        try {
            List<Proveedor> proveedores = proveedorDAO.search(query); // Cambiado a List<Proveedor>
            createTable(proveedores);
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al buscar proveedores: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        }
    }

    public void createTable(List<Proveedor> proveedores) { // Cambiado a List<Proveedor>
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("ID");
        model.addColumn("Nombre");
        model.addColumn("Teléfono");
        model.addColumn("Email");
        model.addColumn("Dirección");
        this.tableProveedor.setModel(model);

        for (Proveedor proveedor : proveedores) {
            model.addRow(new Object[]{proveedor.getProveedorID(), proveedor.getNombre(), proveedor.getTelefono(), proveedor.getEmail(), proveedor.getDireccion()});
        }

        hideCol(0); // Ocultar la columna ID
    }

    private void hideCol(int columnIndex) { // Cambiado pColumna a columnIndex para consistencia
        this.tableProveedor.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableProveedor.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        this.tableProveedor.getTableHeader().getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableProveedor.getTableHeader().getColumnModel().getColumn(columnIndex).setMinWidth(0);
    }

    private Proveedor getProveedorFromTableRow() {
        Proveedor proveedor = null;
        try {
            int filaSelect = this.tableProveedor.getSelectedRow();
            if (filaSelect != -1) {
                int id = (int) this.tableProveedor.getValueAt(filaSelect, 0);
                proveedor = proveedorDAO.getById(id);
                if (proveedor == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró ningún proveedor para el ID seleccionado.", "Validación", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            }
            return proveedor;
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al obtener proveedor de la fila seleccionada: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }

    private void refreshTable() {
        // Si el campo de búsqueda tiene texto, realiza una búsqueda, de lo contrario, muestra todos los proveedores
        if (!txtName.getText().trim().isEmpty()) {
            search(txtName.getText());
        } else {
            try {
                // Asumo que tienes este método en ProveedorDAO, si no, deberás agregarlo.
                List<Proveedor> allProveedores = proveedorDAO.getAllProveedores();
                createTable(allProveedores);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar todos los proveedores: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

package esfe.presentacion;

import esfe.persistencia.CategoriaDAO;
import esfe.dominio.Categoria;
import esfe.utils.CUD; // Importar la clase CUD (si no la tenías, la necesitarás)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Para estilizar el encabezado de la tabla
import javax.swing.border.EmptyBorder; // Para padding
import javax.swing.border.TitledBorder; // Para título del borde
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.util.ArrayList;
import java.util.List; // Usar List en lugar de ArrayList para flexibilidad
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class CategoriaForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName; // Campo para buscar por nombre
    private JButton btnCreate;
    private JTable tableCategoria; // Tabla para mostrar las categorías
    private JButton btnUpdate;
    private JButton btnDelete;

    private CategoriaDAO categoriaDAO;
    private MainForm mainForm;

    public CategoriaForm(MainForm mainForm) {
        this.mainForm = mainForm;
        categoriaDAO = new CategoriaDAO();
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Gestión de Categorías"); // Título más descriptivo
        // Establecer un tamaño preferido para la ventana de listado
        setPreferredSize(new java.awt.Dimension(600, 450)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Gestión de Categorías de Computadoras", // Título del borde
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

        // 3. Mejorar la apariencia de los botones (colores y fuentes consistentes)
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
        tableCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCategoria.setRowHeight(25); // Altura de las filas
        tableCategoria.setGridColor(new Color(230, 230, 230)); // Color de las líneas de la cuadrícula
        tableCategoria.setSelectionBackground(new Color(173, 216, 230)); // Color de selección (azul claro)
        tableCategoria.setFillsViewportHeight(true); // Para que la tabla ocupe todo el espacio disponible

        // Estilo del encabezado de la tabla
        JTableHeader tableHeader = tableCategoria.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(50, 50, 50));
        tableHeader.setReorderingAllowed(false); // Evitar que el usuario reordene columnas
        tableHeader.setResizingAllowed(true); // Permitir redimensionar columnas

        // Si no tienes JScrollPane en tu .form, se recomienda añadirlo en el código:
        // JScrollPane scrollPane = new JScrollPane(tableCategoria);
        // mainPanel.add(scrollPane, BorderLayout.CENTER); // Esto depende del Layout de tu mainPanel

        // --- FIN: MEJORAS DE DISEÑO ---

        // Listener para buscar por nombre
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtName.getText().trim().isEmpty()) {
                    search(txtName.getText());
                } else {
                    refreshTable(); // Refrescar para mostrar todas si el campo de búsqueda está vacío
                }
            }
        });

        // ActionListener para crear una nueva categoría
        btnCreate.addActionListener(s -> {
            CategoriaWriteForm categoriaWriteForm = new CategoriaWriteForm(this.mainForm, CUD.CREATE, new Categoria());
            categoriaWriteForm.setVisible(true);
            refreshTable();
        });

        // ActionListener para actualizar una categoría
        btnUpdate.addActionListener(s -> {
            Categoria categoria = getCategoriaFromTableRow();
            if (categoria != null) {
                CategoriaWriteForm categoriaWriteForm = new CategoriaWriteForm(this.mainForm, CUD.UPDATE, categoria);
                categoriaWriteForm.setVisible(true);
                refreshTable();
            }
        });

        // ActionListener para eliminar una categoría
        btnDelete.addActionListener(s -> {
            Categoria categoria = getCategoriaFromTableRow();
            if (categoria != null) {
                CategoriaWriteForm categoriaWriteForm = new CategoriaWriteForm(this.mainForm, CUD.DELETE, categoria);
                categoriaWriteForm.setVisible(true);
                refreshTable();
            }
        });

        // Al iniciar el formulario, mostrar todas las categorías
        refreshTable();
    }

    private void search(String query) {
        try {
            List<Categoria> categorias = categoriaDAO.search(query); // Cambiado a List<Categoria>
            createTable(categorias);
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al buscar categorías: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        }
    }

    public void createTable(List<Categoria> categorias) { // Cambiado a List<Categoria>
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("ID");
        model.addColumn("Nombre");
        model.addColumn("Descripción");
        this.tableCategoria.setModel(model);

        for (Categoria categoria : categorias) {
            model.addRow(new Object[]{categoria.getCategoriaID(), categoria.getNombre(), categoria.getDescripcion()});
        }

        hideCol(0); // Ocultar la columna ID
    }

    private void hideCol(int columnIndex) { // Cambiado pColumna a columnIndex para consistencia
        this.tableCategoria.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableCategoria.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        this.tableCategoria.getTableHeader().getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableCategoria.getTableHeader().getColumnModel().getColumn(columnIndex).setMinWidth(0);
    }

    private Categoria getCategoriaFromTableRow() {
        Categoria categoria = null;
        try {
            int filaSelect = this.tableCategoria.getSelectedRow();
            if (filaSelect != -1) {
                int id = (int) this.tableCategoria.getValueAt(filaSelect, 0);
                categoria = categoriaDAO.getById(id);
                if (categoria == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró ninguna categoría para el ID seleccionado.", "Validación", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            }
            return categoria;
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al obtener categoría de la fila seleccionada: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }

    private void refreshTable() {
        // Si el campo de búsqueda tiene texto, realiza una búsqueda, de lo contrario, muestra todas las categorías
        if (!txtName.getText().trim().isEmpty()) {
            search(txtName.getText());
        } else {
            try {
                List<Categoria> allCategorias = categoriaDAO.getAllCategorias(); // Asumo que tienes este método en CategoriaDAO
                createTable(allCategorias);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar todas las categorías: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

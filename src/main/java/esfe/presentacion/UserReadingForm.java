package esfe.presentacion;

import esfe.persistencia.UserDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Para estilizar el encabezado de la tabla
import esfe.dominio.User;
import esfe.utils.CUD;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.util.ArrayList; // Mantener ArrayList si el DAO lo devuelve así
import java.util.List; // Usar List en la interfaz para flexibilidad
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores
import javax.swing.border.EmptyBorder; // Para padding
import javax.swing.border.TitledBorder; // Para título del borde


public class UserReadingForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName; // Campo para buscar usuarios por nombre/email
    private JButton btnCreate;
    private JTable tableUsers; // Tabla para mostrar los usuarios
    private JButton btnUpdate;
    private JButton btnDelete;

    private UserDAO userDAO;
    private MainForm mainForm;

    public UserReadingForm(MainForm mainForm) {
        this.mainForm = mainForm;
        userDAO = new UserDAO();
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Gestión de Usuarios"); // Título más descriptivo
        setPreferredSize(new java.awt.Dimension(750, 550)); // Establecer un tamaño preferido
        pack();
        setLocationRelativeTo(mainForm); // Centrar la ventana

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Administración de Usuarios", // Título del borde
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
        tableUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableUsers.setRowHeight(25); // Altura de las filas
        tableUsers.setGridColor(new Color(230, 230, 230)); // Color de las líneas de la cuadrícula
        tableUsers.setSelectionBackground(new Color(173, 216, 230)); // Color de selección (azul claro)
        tableUsers.setFillsViewportHeight(true); // Para que la tabla ocupe todo el espacio disponible

        // Estilo del encabezado de la tabla
        JTableHeader tableHeader = tableUsers.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(50, 50, 50));
        tableHeader.setReorderingAllowed(false); // Evitar que el usuario reordene columnas
        tableHeader.setResizingAllowed(true); // Permitir redimensionar columnas

        // Si no tienes JScrollPane en tu .form para la tabla, es crucial envolverla:
        // JScrollPane scrollPane = new JScrollPane(tableUsers);
        // mainPanel.add(scrollPane, BorderLayout.CENTER); // Esto depende del Layout de tu mainPanel

        // --- FIN: MEJORAS DE DISEÑO ---

        // Agrega un listener de teclado al campo de texto txtName.
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtName.getText().trim().isEmpty()) {
                    search(txtName.getText());
                } else {
                    refreshTable(); // Refrescar la tabla para mostrar todos los usuarios
                }
            }
        });

        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            UserWriteForm userWriteForm = new UserWriteForm(this.mainForm, CUD.CREATE, new User());
            userWriteForm.setVisible(true);
            refreshTable(); // Refrescar la tabla después de la operación
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            User user = getUserFromTableRow();
            if (user != null) {
                UserWriteForm userWriteForm = new UserWriteForm(this.mainForm, CUD.UPDATE, user);
                userWriteForm.setVisible(true);
                refreshTable(); // Refrescar la tabla después de la operación
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            User user = getUserFromTableRow();
            if (user != null) {
                UserWriteForm userWriteForm = new UserWriteForm(this.mainForm, CUD.DELETE, user);
                userWriteForm.setVisible(true);
                refreshTable(); // Refrescar la tabla después de la operación
            }
        });

        // Al iniciar el formulario, mostrar todos los usuarios
        refreshTable();
    }

    private void search(String query) {
        try {
            // Asegúrate de que UserDAO.search devuelva List<User> o ArrayList<User>
            List<User> users = userDAO.search(query);
            createTable(users);
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al buscar usuarios: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        }
    }

    public void createTable(List<User> users) { // Cambiado a List<User>
        String[] columnNames = {"Id", "Nombre", "Email", "Estatus"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) { // Columnas definidas en el constructor
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tableUsers.setModel(model);

        for (User user : users) {
            Object[] rowData = new Object[]{
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getStrEstatus() // Asegúrate de que getStrEstatus() existe y retorna un String
            };
            model.addRow(rowData);
        }

        hideCol(0); // Ocultar la columna ID
    }

    private void hideCol(int columnIndex) { // Cambiado pColumna a columnIndex para consistencia
        this.tableUsers.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableUsers.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        this.tableUsers.getTableHeader().getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        this.tableUsers.getTableHeader().getColumnModel().getColumn(columnIndex).setMinWidth(0);
    }

    private User getUserFromTableRow() {
        User user = null;
        try {
            int filaSelect = this.tableUsers.getSelectedRow();
            if (filaSelect != -1) {
                int id = (int) this.tableUsers.getModel().getValueAt(filaSelect, 0);
                user = userDAO.getById(id);
                // La condición user.getId() == 0 no es robusta para saber si se encontró un usuario.
                // Es mejor verificar si el objeto 'user' devuelto por getById es null.
                if (user == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró ningún usuario para el ID seleccionado.", "Validación", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            }
            return user;
        } catch (SQLException ex) { // Capturar SQLException para mejor manejo
            JOptionPane.showMessageDialog(this, "Error al obtener usuario de la fila seleccionada: " + ex.getMessage(), "ERROR de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Refresca el contenido de la tabla, ejecutando una búsqueda con el texto actual del campo de búsqueda.
     * Si el campo de búsqueda está vacío, muestra todos los usuarios.
     */
    private void refreshTable() {
        if (!txtName.getText().trim().isEmpty()) {
            search(txtName.getText());
        } else {
            try {
                // Asumo que tu UserDAO tiene un método getAllUsers() que retorna List<User>
                List<User> allUsers = userDAO.getAllUsers();
                createTable(allUsers);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar todos los usuarios: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

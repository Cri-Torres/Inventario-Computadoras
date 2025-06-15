package esfe.presentacion;

import esfe.persistencia.UserDAO;
import esfe.utils.CBOption;
import esfe.utils.CUD;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores

import esfe.dominio.User;

public class UserWriteForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<CBOption> cbStatus; // Especificar el tipo genérico
    private JButton btnOk;
    private JButton btnCancel;
    private JLabel IbPassword; // Corregir a 'lblPassword' si es posible en el .form

    private UserDAO userDAO;
    private MainForm mainForm;
    private CUD cud;
    private User en; // 'en' es el objeto User que se está creando, actualizando o eliminando

    public UserWriteForm(MainForm mainForm, CUD cud, User user) {
        this.cud = cud;
        this.en = user;
        this.mainForm = mainForm;
        userDAO = new UserDAO();

        setContentPane(mainPanel);
        setModal(true);
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(450, 400));
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Detalles del Usuario", // Título del borde
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 16), // Fuente para el título del borde
                                new Color(50, 50, 50) // Color del texto del título
                        )
                )
        );

        // 2. Mejorar la apariencia de los campos de texto
        Font textFieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color borderColor = Color.LIGHT_GRAY;
        EmptyBorder paddingBorder = new EmptyBorder(5, 8, 5, 8); // Padding interno

        txtName.setFont(textFieldFont);
        txtName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtEmail.setFont(textFieldFont);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtPassword.setFont(textFieldFont);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        // JComboBox
        cbStatus.setFont(textFieldFont);
        cbStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));
        ((JLabel)cbStatus.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto en JComboBox

        // JLabel de contraseña (si quieres estilizarlo)
        IbPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        IbPassword.setForeground(Color.DARK_GRAY);

        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 13);

        btnOk.setFont(buttonFont);
        btnOk.setBackground(new Color(60, 179, 113)); // Verde por defecto para "Guardar"
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false); // Quitar el borde de foco
        btnOk.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 150, 90)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        btnCancel.setFont(buttonFont);
        btnCancel.setBackground(new Color(220, 20, 60)); // Rojo para "Cancelar"
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 15, 50)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        // --- FIN: MEJORAS DE DISEÑO ---

        init(); // Llama a init para configurar el formulario según la operación

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent s) {
                dispose();
            }
        });
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent s) {
                ok();
            }
        });
    }

    private void init() {
        initCBStatus(); // Inicializa el ComboBox de estatus

        switch (this.cud) {
            case CREATE:
                setTitle("Crear Nuevo Usuario");
                btnOk.setText("Guardar");
                btnOk.setBackground(new Color(60, 179, 113)); // Verde para crear
                // Contraseña visible y requerida para CREATE
                txtPassword.setVisible(true);
                IbPassword.setVisible(true);
                break;
            case UPDATE:
                setTitle("Modificar Usuario Existente");
                btnOk.setText("Actualizar");
                btnOk.setBackground(new Color(70, 130, 180)); // Azul para actualizar
                // Contraseña oculta para UPDATE
                txtPassword.setVisible(false);
                IbPassword.setVisible(false);
                break;
            case DELETE:
                setTitle("Eliminar Usuario");
                btnOk.setText("Eliminar");
                btnOk.setBackground(new Color(220, 20, 60)); // Rojo para eliminar
                // Contraseña oculta para DELETE
                txtPassword.setVisible(false);
                IbPassword.setVisible(false);
                break;
        }

        setValuesControls(this.en); // Carga los valores del usuario
    }

    private void initCBStatus() {
        DefaultComboBoxModel<CBOption> model = new DefaultComboBoxModel<>(); // Usar el constructor sin parámetros
        model.addElement(new CBOption("ACTIVO", (byte)1));
        model.addElement(new CBOption("INACTIVO", (byte)2));
        cbStatus.setModel(model); // Asignar el modelo al ComboBox
    }

    private void setValuesControls(User user) {
        txtName.setText(user.getName());
        txtEmail.setText(user.getEmail());

        // Seleccionar el estatus en el ComboBox 'cbStatus'.
        // Iterar para encontrar el CBOption que coincida con el status del usuario
        DefaultComboBoxModel<CBOption> model = (DefaultComboBoxModel<CBOption>) cbStatus.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            CBOption option = model.getElementAt(i);
            if (option.getValue().equals(user.getStatus())) { // Comparar el valor del byte
                cbStatus.setSelectedItem(option);
                break;
            }
        }

        // Si la operación actual es la creación de un nuevo usuario (CUD.CREATE).
        if (this.cud == CUD.CREATE) {
            // Establece el estatus seleccionado en 'cbStatus' como 'Activo'.
            cbStatus.setSelectedItem(new CBOption("ACTIVO", (byte)1)); // Asegúrate de que este objeto exista en el modelo
            txtPassword.setText(""); // Limpiar campo de contraseña para nueva creación
        }

        // Si la operación actual es la eliminación de un usuario (CUD.DELETE).
        if (this.cud == CUD.DELETE) {
            txtName.setEditable(false);
            txtEmail.setEditable(false);
            cbStatus.setEnabled(false);
            // Hacer que los campos no sean editables para Delete, y darles un color de fondo diferente
            Color disabledBg = new Color(240, 240, 240);
            txtName.setBackground(disabledBg);
            txtEmail.setBackground(disabledBg);
            cbStatus.setBackground(disabledBg); // Esto no cambia el fondo del JComboBox en todas las L&F
            cbStatus.setOpaque(true); // Necesario para que el color de fondo se muestre en algunos L&F
        }
    }

    private boolean getValuesControls() {
        // Validación de campos obligatorios
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo 'Nombre' es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo 'Email' es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        CBOption selectedOption = (CBOption) cbStatus.getSelectedItem();
        if (selectedOption == null || selectedOption.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un 'Estatus'.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        byte status = (byte) selectedOption.getValue();

        if (this.cud == CUD.CREATE) {
            String password = new String(txtPassword.getPassword()).trim();
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El campo 'Contraseña' es obligatorio para nuevos usuarios.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            this.en.setPasswordHash(password); // Asigna la contraseña sin hashear al objeto User
        }

        this.en.setName(txtName.getText().trim());
        this.en.setEmail(txtEmail.getText().trim());
        this.en.setStatus(status);

        return true;
    }

    private void ok() {
        try {
            if (!getValuesControls()) { // Si la validación falla, sale
                return;
            }

            boolean operationSuccessful = false;
            String message = "";
            String title = "Información";
            int messageType = JOptionPane.INFORMATION_MESSAGE;

            switch (this.cud) {
                case CREATE:
                    User createdUser = userDAO.create(this.en); // en.passwordHash ya contiene la contraseña sin hashear
                    if (createdUser != null && createdUser.getId() > 0) {
                        operationSuccessful = true;
                        message = "Usuario registrado exitosamente con ID: " + createdUser.getId();
                    } else {
                        message = "No se pudo registrar el usuario.";
                        messageType = JOptionPane.ERROR_MESSAGE;
                        title = "Error";
                    }
                    break;
                case UPDATE:
                    operationSuccessful = userDAO.update(this.en);
                    if (operationSuccessful) {
                        message = "Usuario actualizado exitosamente.";
                    } else {
                        message = "No se pudo actualizar el usuario.";
                        messageType = JOptionPane.ERROR_MESSAGE;
                        title = "Error";
                    }
                    break;
                case DELETE:
                    operationSuccessful = userDAO.delete(this.en);
                    if (operationSuccessful) {
                        message = "Usuario eliminado exitosamente.";
                    } else {
                        message = "No se pudo eliminar el usuario.";
                        messageType = JOptionPane.ERROR_MESSAGE;
                        title = "Error";
                    }
                    break;
            }

            JOptionPane.showMessageDialog(this, message, title, messageType);

            if (operationSuccessful) {
                this.dispose(); // Cerrar el formulario solo si la operación fue exitosa
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar el usuario: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al guardar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        }
    }
}

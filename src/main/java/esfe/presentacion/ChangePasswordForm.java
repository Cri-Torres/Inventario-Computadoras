package esfe.presentacion;

import esfe.dominio.User;
import esfe.persistencia.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores
import java.awt.event.ActionEvent; // Se mantiene para consistencia, aunque se usen lambdas
import java.awt.event.ActionListener; // Se mantiene para consistencia, aunque se usen lambdas
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores


public class ChangePasswordForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnChangePassword;

    private UserDAO userDAO;
    private MainForm mainForm;

    public ChangePasswordForm(MainForm mainForm) {
        this.mainForm = mainForm;
        userDAO = new UserDAO();
        txtEmail.setText(mainForm.getUserAutenticate().getEmail());

        setContentPane(mainPanel);
        setModal(true);
        setTitle("Cambiar Contraseña"); // Título más descriptivo
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(400, 280)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm); // Centrar la ventana

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Cambio de Contraseña de Usuario", // Título del borde
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

        txtEmail.setFont(textFieldFont);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));
        txtEmail.setEditable(false); // Mantenerlo no editable, solo pre-cargado
        txtEmail.setBackground(new Color(240, 240, 240)); // Fondo gris para indicar no editable

        txtPassword.setFont(textFieldFont);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        // 3. Mejorar la apariencia del botón
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        btnChangePassword.setFont(buttonFont);
        btnChangePassword.setBackground(new Color(70, 130, 180)); // Color azul acero
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFocusPainted(false); // Quitar el borde de foco
        btnChangePassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 110, 150)),
                new EmptyBorder(10, 20, 10, 20) // Padding interno del botón
        ));

        // --- FIN: MEJORAS DE DISEÑO ---

        // Agrega un ActionListener al botón btnChangePassword.
        btnChangePassword.addActionListener(e -> changePassword());
    }

    private void changePassword() {
        try {
            User userAut = mainForm.getUserAutenticate();
            User user = new User();
            user.setId(userAut.getId());
            user.setPasswordHash(new String(txtPassword.getPassword()));

            if (user.getPasswordHash().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                        "La contraseña es obligatoria.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean res = userDAO.updatePassword(user);

            if (res) {
                JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                        "Contraseña cambiada exitosamente. Por favor, inicie sesión con su nueva contraseña.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                // Asumiendo que quieres volver al LoginForm y que LoginForm lo puede manejar
                LoginForm loginForm = new LoginForm(this.mainForm); // Pasar mainForm si LoginForm lo necesita
                loginForm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                        "No se logró cambiar la contraseña.",
                        "Cambiar Contraseña", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) { // Capturar SQLException específicamente
            JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                    "Error de base de datos al cambiar la contraseña: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                    "Ocurrió un error inesperado al cambiar la contraseña: " + ex.getMessage(),
                    "Sistema", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

package esfe.presentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent; // Este import ahora estará activo
import java.awt.event.ActionListener; // Este import ahora estará activo
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

import esfe.dominio.User;
import esfe.persistencia.UserDAO;

/**
 * La clase LoginForm representa la ventana de inicio de sesión de la aplicación.
 * Extiende JDialog, lo que significa que es un cuadro de diálogo modal
 * que se utiliza para solicitar las credenciales del usuario (email y contraseña)
 * para acceder a la aplicación principal.
 */
public class LoginForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnSalir;

    private UserDAO userDAO;
    private MainForm mainForm;

    public LoginForm(MainForm mainForm){
        this.mainForm = mainForm;
        userDAO = new UserDAO();
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Login de Usuario"); // Título más descriptivo
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(400, 320)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm); // Centrar la ventana

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Acceso al Sistema", // Título del borde
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

        txtPassword.setFont(textFieldFont);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        btnLogin.setFont(buttonFont);
        btnLogin.setBackground(new Color(60, 179, 113)); // Color verde menta
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false); // Quitar el borde de foco
        btnLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 150, 90)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        btnSalir.setFont(buttonFont);
        btnSalir.setBackground(new Color(220, 20, 60)); // Color rojo carmesí
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 15, 50)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // --- FIN: MEJORAS DE DISEÑO ---

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void login() {
        try{
            User user = new User();
            user.setEmail(txtEmail.getText().trim()); // Usar trim() para eliminar espacios en blanco
            user.setPasswordHash(new String(txtPassword.getPassword()));

            // Validación de campos vacíos
            if (user.getEmail().isEmpty() || user.getPasswordHash().isEmpty()) {
                JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                        "Por favor, ingrese su correo electrónico y contraseña.",
                        "Validación de Login", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User userAut = userDAO.authenticate(user);

            if(userAut != null && userAut.getId() > 0 && userAut.getEmail().equals(user.getEmail())){ // Comprobación de email redundante si userAut no es null
                this.mainForm.setUserAutenticate(userAut);
                this.dispose();
            }
            else{
                JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                        "Correo electrónico o contraseña incorrectos.", // Mensaje más específico
                        "Login Fallido",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (SQLException ex){ // Capturar SQLException específicamente
            JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                    "Error de base de datos al intentar iniciar sesión: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimir la traza completa para depuración
        }
        catch (Exception ex){
            JOptionPane.showMessageDialog(this, // Usar 'this' para centrar
                    "Ocurrió un error inesperado durante el login: " + ex.getMessage(),
                    "Error del Sistema", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

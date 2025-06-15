package esfe;

import esfe.presentacion.LoginForm;
import esfe.presentacion.MainForm;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            // Utiliza el hilo de despacho de eventos (Event Dispatch Thread - EDT) para asegurar
            // que todas las operaciones relacionadas con la interfaz gráfica de usuario (Swing)
            // se realicen de forma segura y sin bloqueos.
            MainForm mainForm  = new MainForm(); // Crea una nueva instancia del formulario principal de la aplicación.
            mainForm.setVisible(true); // Hace visible el formulario principal. Inicialmente podría estar vacío o tener una interfaz de carga.
            LoginForm loginForm = new LoginForm(mainForm); // Crea una nueva instancia del formulario de inicio de sesión, pasándole la instancia del formulario principal como padre. Esto  para centrar la ventana de inicio de sesión relativa a la principal o para pasar datos entre ellas.
            loginForm.setVisible(true); // Hace visible la ventana de inicio de sesión, solicitando al usuario que ingrese sus credenciales.
        });
    }
}
package esfe.presentacion;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font; // Importar para cambiar la fuente
import java.awt.Color; // Importar para cambiar colores

import esfe.dominio.User;
import esfe.persistencia.UserDAO;

/**
 * La clase MainForm representa la ventana principal de la aplicación.
 * Es un JFrame que contiene la barra de menú y maneja la navegación
 * entre los diferentes formularios de gestión.
 */
public class MainForm extends JFrame {

    private User userAutenticate; // Almacena la información del usuario autenticado.

    public User getUserAutenticate() {
        return userAutenticate;
    }

    public void setUserAutenticate(User userAutenticate) {
        this.userAutenticate = userAutenticate;
    }

    public MainForm(){
        setTitle("Sistema de Gestión de Inventario de Computadoras"); // Título más descriptivo para la aplicación
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Inicia maximizada

        // --- INICIO: MEJORAS DE DISEÑO PARA LA VENTANA PRINCIPAL ---
        // Se puede establecer un color de fondo para el JFrame, aunque no sea visible si se añaden paneles
        getContentPane().setBackground(new Color(245, 245, 245)); // Un gris claro suave

        // Establecer un LookAndFeel moderno (Opcional, puede afectar otros componentes si no se diseñan para ello)
        // try {
        //     UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        //     SwingUtilities.updateComponentTreeUI(this);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // --- FIN: MEJORAS DE DISEÑO PARA LA VENTANA PRINCIPAL ---

        createMenu(); // Llama al método para crear y agregar la barra de menú.
    }

    private void createMenu() {
        // Barra de menú
        JMenuBar menuBar = new JMenuBar();
        // --- ESTILO DE LA BARRA DE MENÚ ---
        menuBar.setBackground(new Color(45, 130, 180)); // Color azul oscuro para la barra de menú
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding sutil
        // --- FIN ESTILO ---

        setJMenuBar(menuBar);

        // --- ESTILO GENERAL PARA MENÚS Y ELEMENTOS DE MENÚ ---
        Font menuFont = new Font("Segoe UI", Font.BOLD, 14);
        Font menuItemFont = new Font("Segoe UI", Font.PLAIN, 13);
        Color menuForeground = Color.WHITE; // Texto blanco para los menús principales
        Color menuItemBackground = Color.WHITE; // Fondo blanco para los items de menú
        Color menuItemSelectionBackground = new Color(173, 216, 230); // Azul claro para selección
        // --- FIN ESTILO GENERAL ---


        // Menú "Perfil"
        JMenu menuPerfil = new JMenu("Perfil");
        menuPerfil.setFont(menuFont);
        menuPerfil.setForeground(menuForeground);
        menuBar.add(menuPerfil);

        JMenuItem itemChangePassword = new JMenuItem("Cambiar contraseña");
        itemChangePassword.setFont(menuItemFont);
        itemChangePassword.setBackground(menuItemBackground);
        // itemChangePassword.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuPerfil.add(itemChangePassword);
        itemChangePassword.addActionListener(e -> {
            ChangePasswordForm changePassword = new ChangePasswordForm(this);
            changePassword.setVisible(true);
        });

        JMenuItem itemChangeUser = new JMenuItem("Cambiar de usuario");
        itemChangeUser.setFont(menuItemFont);
        itemChangeUser.setBackground(menuItemBackground);
        // itemChangeUser.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuPerfil.add(itemChangeUser);
        itemChangeUser.addActionListener(e -> {
            LoginForm loginForm = new LoginForm(this);
            loginForm.setVisible(true);
        });

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setFont(menuItemFont);
        itemSalir.setBackground(menuItemBackground);
        // itemSalir.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuPerfil.add(itemSalir);
        itemSalir.addActionListener(e -> System.exit(0));


        // Menú "Tablas"
        JMenu menuMantenimiento = new JMenu("Tablas");
        menuMantenimiento.setFont(menuFont);
        menuMantenimiento.setForeground(menuForeground);
        menuBar.add(menuMantenimiento);

        JMenuItem itemUsers = new JMenuItem("Usuarios");
        itemUsers.setFont(menuItemFont);
        itemUsers.setBackground(menuItemBackground);
        // itemUsers.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuMantenimiento.add(itemUsers);
        itemUsers.addActionListener(e -> {
            UserReadingForm userReadingForm=new UserReadingForm(this);
            userReadingForm.setVisible(true);
        });

        // Menú para Categorías
        JMenuItem itemCategorias = new JMenuItem("Categorías");
        itemCategorias.setFont(menuItemFont);
        itemCategorias.setBackground(menuItemBackground);
        // itemCategorias.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuMantenimiento.add(itemCategorias);
        itemCategorias.addActionListener(e -> {
            CategoriaForm categoriaReadingForm = new CategoriaForm(this);
            categoriaReadingForm.setVisible(true);
        });

        // Menú para Proveedores
        JMenuItem itemProveedores = new JMenuItem("Proveedores");
        itemProveedores.setFont(menuItemFont);
        itemProveedores.setBackground(menuItemBackground);
        // itemProveedores.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuMantenimiento.add(itemProveedores);
        itemProveedores.addActionListener(e -> {
            ProveedorForm proveedorForm = new ProveedorForm(this);
            proveedorForm.setVisible(true);
        });

        // Menú para Computadoras
        JMenuItem itemComputadoras = new JMenuItem("Computadoras");
        itemComputadoras.setFont(menuItemFont);
        itemComputadoras.setBackground(menuItemBackground);
        // itemComputadoras.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuMantenimiento.add(itemComputadoras);
        itemComputadoras.addActionListener(e -> {
            ComputadoraForm computadoraReadingForm = new ComputadoraForm(this);
            computadoraReadingForm.setVisible(true);
        });

        JMenuItem itemMovimientoInventario = new JMenuItem("Movimiento de Inventario"); // Nombre más descriptivo
        itemMovimientoInventario.setFont(menuItemFont);
        itemMovimientoInventario.setBackground(menuItemBackground);
        // itemMovimientoInventario.setArmedColor(menuItemSelectionBackground); // REMOVIDO: Este método no existe en JMenuItem
        menuMantenimiento.add(itemMovimientoInventario);
        itemMovimientoInventario.addActionListener(e -> {
            MovimientoInventarioForm movimientoInventarioForm = new MovimientoInventarioForm(this);
            movimientoInventarioForm.setVisible(true);
        });
    }
}

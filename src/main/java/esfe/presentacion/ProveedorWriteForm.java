package esfe.presentacion;

import esfe.persistencia.ProveedorDAO; // Importa la clase ProveedorDAO
import esfe.utils.CUD; // Importa el enum CUD
import esfe.dominio.Proveedor; // Importa la clase Proveedor

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.event.ActionEvent; // Se mantiene, aunque se usen lambdas para consistencia
import java.awt.event.ActionListener; // Se mantiene, aunque se usen lambdas para consistencia
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class ProveedorWriteForm extends JDialog {
    private JTextField txtName;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel mainPanel;

    private ProveedorDAO proveedorDAO;
    private MainForm mainForm;
    private CUD cud;
    private Proveedor proveedor;

    public ProveedorWriteForm(MainForm mainForm, CUD cud, Proveedor proveedor) {
        this.mainForm = mainForm;
        this.cud = cud;
        this.proveedor = proveedor;
        proveedorDAO = new ProveedorDAO();
        setContentPane(mainPanel);
        setModal(true);
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(500, 400)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Detalles del Proveedor", // Título del borde
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

        txtTelefono.setFont(textFieldFont);
        txtTelefono.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtEmail.setFont(textFieldFont);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtDireccion.setFont(textFieldFont);
        txtDireccion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

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

        init(); // Llama a init para configurar título y texto/color del botón

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
        switch (this.cud) {
            case CREATE:
                setTitle("Crear Nuevo Proveedor"); // Título más específico
                btnOk.setText("Guardar");
                btnOk.setBackground(new Color(60, 179, 113)); // Verde para crear
                break;
            case UPDATE:
                setTitle("Modificar Proveedor Existente"); // Título más específico
                btnOk.setText("Actualizar"); // Texto del botón más claro
                btnOk.setBackground(new Color(70, 130, 180)); // Azul para actualizar
                setValuesControls(this.proveedor);
                break;
            case DELETE:
                setTitle("Eliminar Proveedor");
                btnOk.setText("Eliminar");
                btnOk.setBackground(new Color(220, 20, 60)); // Rojo para eliminar
                setValuesControls(this.proveedor);
                break;
        }
    }

    private void setValuesControls(Proveedor proveedor) {
        txtName.setText(proveedor.getNombre());
        txtTelefono.setText(proveedor.getTelefono());
        txtEmail.setText(proveedor.getEmail());
        txtDireccion.setText(proveedor.getDireccion());

        if (this.cud == CUD.DELETE) {
            txtName.setEditable(false);
            txtTelefono.setEditable(false);
            txtEmail.setEditable(false);
            txtDireccion.setEditable(false);
            // Hacer que los campos no sean editables para Delete, y darles un color de fondo diferente
            Color disabledBg = new Color(240, 240, 240);
            txtName.setBackground(disabledBg);
            txtTelefono.setBackground(disabledBg);
            txtEmail.setBackground(disabledBg);
            txtDireccion.setBackground(disabledBg);
        }
    }

    private void ok() {
        try {
            String nombre = txtName.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();

            // Validación de campos obligatorios solo para CREATE y UPDATE
            if (this.cud != CUD.DELETE && (nombre.isEmpty() || telefono.isEmpty() || email.isEmpty() || direccion.isEmpty())) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            switch (this.cud) {
                case CREATE:
                    Proveedor nuevoProveedor = new Proveedor();
                    nuevoProveedor.setNombre(nombre);
                    nuevoProveedor.setTelefono(telefono);
                    nuevoProveedor.setEmail(email);
                    nuevoProveedor.setDireccion(direccion);
                    Proveedor created = proveedorDAO.create(nuevoProveedor);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this, "Proveedor creado exitosamente con ID: " + created.getProveedorID(), "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo crear el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case UPDATE:
                    proveedor.setNombre(nombre);
                    proveedor.setTelefono(telefono);
                    proveedor.setEmail(email);
                    proveedor.setDireccion(direccion);
                    boolean updated = proveedorDAO.update(proveedor);
                    if (updated) {
                        JOptionPane.showMessageDialog(this, "Proveedor actualizado exitosamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo actualizar el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case DELETE:
                    boolean deleted = proveedorDAO.delete(proveedor.getProveedorID());
                    if (deleted) {
                        JOptionPane.showMessageDialog(this, "Proveedor eliminado exitosamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo eliminar el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
            }

            this.dispose(); // Cerrar el formulario después de la operación
        } catch (SQLException ex) { // Capturar SQLException para errores de DB
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err); // Imprimir la traza completa a System.err
        } catch (Exception ex) { // Capturar cualquier otra excepción
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err); // Imprimir la traza completa a System.err
        }
    }
}

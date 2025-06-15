package esfe.presentacion;

import esfe.persistencia.CategoriaDAO; // Importa la clase CategoriaDAO
import esfe.utils.CUD; // Importa el enum CUD
import esfe.dominio.Categoria; // Importa la clase Categoria
import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException; // Importar SQLException para un mejor manejo de errores
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class CategoriaWriteForm extends JDialog {
    private JTextField txtName;
    private JTextField txtDescripcion;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel mainPanel;


    private CategoriaDAO categoriaDAO;
    private MainForm mainForm;
    private CUD cud;
    private Categoria categoria;

    public CategoriaWriteForm(MainForm mainForm, CUD cud, Categoria categoria) {
        this.mainForm = mainForm;
        this.cud = cud;
        this.categoria = categoria;
        categoriaDAO = new CategoriaDAO();
        setContentPane(mainPanel);
        setModal(true);
        setPreferredSize(new java.awt.Dimension(450, 300)); // Establecer un tamaño preferido para la ventana
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Detalles de Categoría", // Título del borde
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("Segoe UI", Font.BOLD, 14), // Fuente para el título del borde
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

        txtDescripcion.setFont(textFieldFont);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), paddingBorder));

        // Si txtDescripcion fuera un JTextArea, haríamos esto:
        // ((JTextArea)txtDescripcion).setLineWrap(true);
        // ((JTextArea)txtDescripcion).setWrapStyleWord(true);
        // ((JTextArea)txtDescripcion).setRows(3); // Ejemplo de altura


        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);

        btnOk.setFont(buttonFont);
        btnOk.setBackground(new Color(60, 179, 113)); // Color verde menta
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false); // Quitar el borde de foco
        btnOk.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 150, 90)),
                new EmptyBorder(8, 15, 8, 15)
        ));

        btnCancel.setFont(buttonFont);
        btnCancel.setBackground(new Color(220, 20, 60)); // Color rojo carmesí
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 15, 50)),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // --- FIN: MEJORAS DE DISEÑO ---

        init(); // Llama a init para configurar título y texto del botón

        btnCancel.addActionListener(s -> this.dispose());
        btnOk.addActionListener(s -> ok());
    }

    private void init() {
        switch (this.cud) {
            case CREATE:
                setTitle("Crear Nueva Categoría"); // Título más específico
                btnOk.setText("Guardar");
                btnOk.setBackground(new Color(60, 179, 113)); // Color verde para crear
                break;
            case UPDATE:
                setTitle("Modificar Categoría Existente"); // Título más específico
                btnOk.setText("Actualizar"); // Texto del botón más claro
                btnOk.setBackground(new Color(70, 130, 180)); // Color azul para actualizar
                setValuesControls(this.categoria);
                break;
            case DELETE:
                setTitle("Eliminar Categoría");
                btnOk.setText("Eliminar");
                btnOk.setBackground(new Color(220, 20, 60)); // Color rojo para eliminar
                setValuesControls(this.categoria);
                break;
        }
    }

    private void setValuesControls(Categoria categoria) {
        txtName.setText(categoria.getNombre());
        txtDescripcion.setText(categoria.getDescripcion());

        if (this.cud == CUD.DELETE) {
            txtName.setEditable(false);
            txtDescripcion.setEditable(false);
            // Hacer que los campos no sean editables para Delete, pero también un poco más oscuros visualmente
            txtName.setBackground(new Color(240, 240, 240));
            txtDescripcion.setBackground(new Color(240, 240, 240));
        }
    }

    private void ok() {
        try {
            String nombre = txtName.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            if (this.cud != CUD.DELETE && (nombre.isEmpty() || descripcion.isEmpty())) { // Validar solo para CREATE/UPDATE
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            switch (this.cud) {
                case CREATE:
                    Categoria nuevaCategoria = new Categoria();
                    nuevaCategoria.setNombre(nombre);
                    nuevaCategoria.setDescripcion(descripcion);
                    Categoria created = categoriaDAO.create(nuevaCategoria);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this, "Categoría creada exitosamente con ID: " + created.getCategoriaID(), "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo crear la categoría.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case UPDATE:
                    categoria.setNombre(nombre);
                    categoria.setDescripcion(descripcion);
                    boolean updated = categoriaDAO.update(categoria);
                    if (updated) {
                        JOptionPane.showMessageDialog(this, "Categoría actualizada exitosamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo actualizar la categoría.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case DELETE:
                    boolean deleted = categoriaDAO.delete(categoria.getCategoriaID());
                    if (deleted) {
                        JOptionPane.showMessageDialog(this, "Categoría eliminada exitosamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo eliminar la categoría.", "Error", JOptionPane.ERROR_MESSAGE);
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

package esfe.presentacion;

import esfe.dominio.Computadora;
import esfe.dominio.Categoria;
import esfe.dominio.Proveedor;
import esfe.persistencia.ComputadoraDAO;
import esfe.persistencia.CategoriaDAO;
import esfe.persistencia.ProveedorDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para padding
import javax.swing.border.TitledBorder; // Importar para título del borde
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Font; // Para cambiar la fuente
import java.awt.Color; // Para cambiar colores

public class ComputadoraWriteForm extends JDialog {
    private JPanel mainPanel;
    private JComboBox<Categoria> cbCategoria;
    private JComboBox<Proveedor> cbProveedor;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtNumeroSerie;
    private JLabel lblFechaCompra;
    private JTextField txtPrecio;
    private JComboBox<String> cbEstado;
    private JTextArea txtObservaciones;
    private JButton okButton;
    private JButton cancelarButton;

    private final ComputadoraDAO computadoraDAO;
    private final CategoriaDAO categoriaDAO;
    private final ProveedorDAO proveedorDAO;
    private final Computadora computadoraActual;
    private final MainForm mainForm;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ComputadoraWriteForm(MainForm mainForm, Computadora computadora) {
        this.mainForm = mainForm;
        this.computadoraActual = computadora;

        computadoraDAO = new ComputadoraDAO();
        categoriaDAO = new CategoriaDAO();
        proveedorDAO = new ProveedorDAO();

        setContentPane(mainPanel);
        setModal(true);
        // Establecer un tamaño preferido para la ventana
        setPreferredSize(new java.awt.Dimension(600, 550)); // Ancho, Alto
        pack();
        setLocationRelativeTo(mainForm);

        // --- INICIO: MEJORAS DE DISEÑO ---

        // 1. Añadir padding y un borde con título al panel principal
        mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(15, 15, 15, 15), // Padding alrededor del contenido
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), // Un borde simple
                                "Detalles de Computadora", // Título del borde
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

        txtMarca.setFont(textFieldFont);
        txtMarca.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtModelo.setFont(textFieldFont);
        txtModelo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtNumeroSerie.setFont(textFieldFont);
        txtNumeroSerie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        txtPrecio.setFont(textFieldFont);
        txtPrecio.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        // JTextArea para Observaciones
        txtObservaciones.setFont(textFieldFont);
        txtObservaciones.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));
        txtObservaciones.setRows(4); // Establece 4 filas de altura
        txtObservaciones.setLineWrap(true); // Permite que el texto salte de línea
        txtObservaciones.setWrapStyleWord(true); // Salta de línea por palabras

        // JComboBoxes
        cbCategoria.setFont(textFieldFont);
        cbCategoria.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        cbProveedor.setFont(textFieldFont);
        cbProveedor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        cbEstado.setFont(textFieldFont);
        cbEstado.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor), paddingBorder));

        // JLabel para FechaCompra
        lblFechaCompra.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFechaCompra.setForeground(Color.GRAY);

        // 3. Mejorar la apariencia de los botones
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 13);

        okButton.setFont(buttonFont);
        okButton.setBackground(new Color(60, 179, 113)); // Verde para "Guardar"
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 150, 90)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        cancelarButton.setFont(buttonFont);
        cancelarButton.setBackground(new Color(220, 20, 60)); // Rojo para "Cancelar"
        cancelarButton.setForeground(Color.WHITE);
        cancelarButton.setFocusPainted(false);
        cancelarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 15, 50)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        // --- FIN: MEJORAS DE DISEÑO ---

        populateComboBoxes();
        populateEstadoComboBox();
        loadComputadoraData();

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveComputadora();
            }
        });

        cancelarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void populateComboBoxes() {
        try {
            List<Categoria> categorias = categoriaDAO.getAllCategorias();
            cbCategoria.addItem(null); // Permite la opción "Ninguno" o "Nulo"
            for (Categoria cat : categorias) {
                cbCategoria.addItem(cat);
            }

            List<Proveedor> proveedores = proveedorDAO.getAllProveedores();
            cbProveedor.addItem(null); // Permite la opción "Ninguno" o "Nulo"
            for (Proveedor prov : proveedores) {
                cbProveedor.addItem(prov);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías/proveedores: " + ex.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void populateEstadoComboBox() {
        cbEstado.addItem(computadoraActual.getStrEstado(Computadora.ESTADO_DISPONIBLE));
        cbEstado.addItem(computadoraActual.getStrEstado(Computadora.ESTADO_AGOTADO));
        // Añade más estados si los defines en Computadora
    }

    private void loadComputadoraData() {
        if (computadoraActual.getComputadoraID() > 0) {
            setTitle("Editar Computadora");
            okButton.setText("Actualizar"); // Cambiar texto del botón para actualización
            okButton.setBackground(new Color(70, 130, 180)); // Color azul para actualizar

            txtMarca.setText(computadoraActual.getMarca());
            txtModelo.setText(computadoraActual.getModelo());
            txtNumeroSerie.setText(computadoraActual.getNumeroSerie());

            if (computadoraActual.getFechaCompra() != null) {
                lblFechaCompra.setText(computadoraActual.getFechaCompra().format(DATE_TIME_FORMATTER));
            } else {
                lblFechaCompra.setText("N/A");
            }

            txtPrecio.setText(String.format("%.2f", computadoraActual.getPrecio()));
            txtObservaciones.setText(computadoraActual.getObservaciones());

            for (int i = 0; i < cbCategoria.getItemCount(); i++) {
                Categoria cat = cbCategoria.getItemAt(i);
                if (cat != null && cat.getCategoriaID() == computadoraActual.getCategoriaID()) {
                    cbCategoria.setSelectedItem(cat);
                    break;
                }
            }

            if (computadoraActual.getProveedorID() != null) {
                for (int i = 0; i < cbProveedor.getItemCount(); i++) {
                    Proveedor prov = cbProveedor.getItemAt(i);
                    if (prov != null && computadoraActual.getProveedorID().equals(prov.getProveedorID())) {
                        cbProveedor.setSelectedItem(prov);
                        break;
                    }
                }
            } else {
                cbProveedor.setSelectedItem(null);
            }

            cbEstado.setSelectedItem(computadoraActual.getStrEstado());

        } else {
            setTitle("Registrar Nueva Computadora");
            okButton.setText("Guardar"); // Texto del botón para nueva creación
            okButton.setBackground(new Color(60, 179, 113)); // Color verde para guardar

            txtMarca.setText("");
            txtModelo.setText("");
            txtNumeroSerie.setText("");
            lblFechaCompra.setText("Se generará automáticamente");
            txtPrecio.setText("0.00");
            txtObservaciones.setText("");

            if (cbCategoria.getItemCount() > 0) cbCategoria.setSelectedIndex(0);
            if (cbProveedor.getItemCount() > 0) cbProveedor.setSelectedIndex(0);
            cbEstado.setSelectedItem(computadoraActual.getStrEstado(Computadora.ESTADO_DISPONIBLE));
        }
    }

    private void saveComputadora() {
        if (txtMarca.getText().trim().isEmpty() ||
                txtModelo.getText().trim().isEmpty() ||
                txtNumeroSerie.getText().trim().isEmpty() ||
                txtPrecio.getText().trim().isEmpty() ||
                cbCategoria.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios (Marca, Modelo, Número de Serie, Precio, Categoría).", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            computadoraActual.setMarca(txtMarca.getText().trim());
            computadoraActual.setModelo(txtModelo.getText().trim());
            computadoraActual.setNumeroSerie(txtNumeroSerie.getText().trim());
            computadoraActual.setObservaciones(txtObservaciones.getText().trim());

            double precio = Double.parseDouble(txtPrecio.getText().trim());
            computadoraActual.setPrecio(precio);

            Categoria selectedCategoria = (Categoria) cbCategoria.getSelectedItem();
            if (selectedCategoria != null) {
                computadoraActual.setCategoriaID(selectedCategoria.getCategoriaID());
            } else {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría válida.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Proveedor selectedProveedor = (Proveedor) cbProveedor.getSelectedItem();
            if (selectedProveedor != null) {
                computadoraActual.setProveedorID(selectedProveedor.getProveedorID());
            } else {
                computadoraActual.setProveedorID(null);
            }

            String estadoStr = (String) cbEstado.getSelectedItem();
            if (estadoStr != null) {
                if (estadoStr.equals("Disponible")) {
                    computadoraActual.setEstado(Computadora.ESTADO_DISPONIBLE);
                } else if (estadoStr.equals("Agotado")) {
                    computadoraActual.setEstado(Computadora.ESTADO_AGOTADO);
                }
            }

            if (computadoraActual.getComputadoraID() == 0) {
                Computadora created = computadoraDAO.create(computadoraActual);
                if (created != null) {
                    JOptionPane.showMessageDialog(this, "Computadora registrada exitosamente con ID: " + created.getComputadoraID(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo registrar la computadora.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                boolean updated = computadoraDAO.update(computadoraActual);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Computadora actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar la computadora.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error de formato en el Precio. Asegúrate de que sea un número válido.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar la computadora: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al guardar la computadora: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

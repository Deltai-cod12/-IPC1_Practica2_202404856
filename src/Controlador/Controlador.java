/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.DatosModelo;
import Vista.FrmPrincipal;
import Vista.FrmOpciones;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Controlador implements ActionListener {
    private FrmPrincipal vista;
    private DatosModelo modelo;
    private File archivoSeleccionado;

    public Controlador(FrmPrincipal vista) {
        this.vista = vista;
        this.modelo = new DatosModelo();

        // Agregar eventos a los botones
        this.vista.Buscar.addActionListener(this);
        this.vista.Aceptar.addActionListener(this);
        this.vista.Ordenar.addActionListener(this); // Ahora "Ordenar" usa actionPerformed
        
        // Asegurar que el panel usa un diseño adecuado
        this.vista.Panel.setLayout(new BorderLayout());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.Buscar) {
            seleccionarArchivo();
        } else if (e.getSource() == vista.Aceptar) {
            generarGrafica();
        } else if (e.getSource() == vista.Ordenar) { 
            abrirFrmOpciones();  // Llama a la función para abrir FrmOpciones
        }
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo .i1pec");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos i1pec (*.i1pec)", "i1pec"));

        int resultado = fileChooser.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            vista.Ruta.setText(archivoSeleccionado.getAbsolutePath());
        }
    }

    private void generarGrafica() {
        if (archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un archivo primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean datosCargados = modelo.cargarDesdeArchivo(archivoSeleccionado);
        if (!datosCargados) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tituloGrafica = vista.Titulo.getText();
        if (tituloGrafica.isEmpty()) {
            tituloGrafica = "Gráfico Generado";
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            dataset.addValue(modelo.getValoresY().get(i), modelo.getCategoriasX().get(i), modelo.getCategoriasX().get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                tituloGrafica, 
                modelo.getTituloX(), 
                modelo.getTituloY(), 
                dataset
        );

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            renderer.setSeriesPaint(i, new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 400));

        vista.Panel.removeAll();
        vista.Panel.add(chartPanel, BorderLayout.CENTER);
        vista.Panel.revalidate();
        vista.Panel.repaint();
    }
    
    private void abrirFrmOpciones() {
        FrmOpciones opciones = new FrmOpciones();
        opciones.setVisible(true); // Hace visible la nueva ventana
    }
}

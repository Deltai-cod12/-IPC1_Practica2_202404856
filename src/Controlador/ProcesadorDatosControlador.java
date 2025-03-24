/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Vista.FrmPrincipal;
import Modelo.DatosModelo;

public class ProcesadorDatosControlador {
    private FrmPrincipal vista;
    private DatosModelo modelo;
    private ExecutorService executor;

    public ProcesadorDatosControlador(FrmPrincipal vista, DatosModelo modelo) {
        this.vista = vista;
        this.modelo = modelo;
        this.executor = Executors.newSingleThreadExecutor();
        
        this.vista.getBtnBuscar().addActionListener(e -> seleccionarArchivo());
        this.vista.getBtnAceptar().addActionListener(e -> cargarDatos());
        this.vista.getBtnOrdenar().addActionListener(e -> iniciarOrdenamiento());
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        int opcion = fileChooser.showOpenDialog(vista);
        
        if (opcion == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            vista.getTxtRuta().setText(archivoSeleccionado.getAbsolutePath());
        }
    }

    private void cargarDatos() {
        String rutaArchivo = vista.getTxtRuta().getText();
        if (rutaArchivo.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Seleccione un archivo primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        modelo.cargarDatosDesdeCSV(rutaArchivo);
        vista.actualizarGrafica(modelo.getDatos());
    }

    private void iniciarOrdenamiento() {
        String algoritmo = vista.getSeleccionAlgoritmo();
        boolean ascendente = vista.isOrdenAscendente();
        int velocidad = vista.getVelocidadOrdenamiento();
        
        executor.submit(() -> {
            modelo.ordenarDatos(algoritmo, ascendente, velocidad);
            vista.actualizarGrafica(modelo.getDatos());
            generarReporte();
        });
    }
    
    private void generarReporte() {
        ReporteGenerador reporte = new ReporteGenerador();
        reporte.generarPDF(modelo.getDatos(), modelo.getInfoOrdenamiento());
        JOptionPane.showMessageDialog(vista, "Reporte generado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}

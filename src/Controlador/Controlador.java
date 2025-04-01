/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.DatosModelo;
import Vista.FrmPrincipal;
import Vista.FrmOpciones;
import Vista.FrmOrdenar;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Controlador implements ActionListener {
    private FrmPrincipal vista;
    private DatosModelo modelo;
    private File archivoSeleccionado;
    private String algoritmoSeleccionado;
    private String velocidadSeleccionada;
    private String tipoOrdenSeleccionado;
    private FrmOrdenar frmOrdenar;
    private long tiempoInicio;
    private int pasos;
    private JFreeChart initialChart;
    private JFreeChart finalChart;

    public Controlador(FrmPrincipal vista) {
        this.vista = vista;
        this.modelo = new DatosModelo();
        this.vista.Buscar.addActionListener(this);
        this.vista.Aceptar.addActionListener(this);
        this.vista.Ordenar.addActionListener(this);
        this.vista.Panel.setLayout(new BorderLayout());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.Buscar) {
            seleccionarArchivo();
        } else if (e.getSource() == vista.Aceptar) {
            generarGrafica();
        } else if (e.getSource() == vista.Ordenar) {
            abrirFrmOpciones();
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

        initialChart = ChartFactory.createBarChart(
                tituloGrafica,
                modelo.getTituloX(),
                modelo.getTituloY(),
                dataset
        );

        BarRenderer renderer = (BarRenderer) initialChart.getCategoryPlot().getRenderer();
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            renderer.setSeriesPaint(i, new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
        }

        ChartPanel chartPanel = new ChartPanel(initialChart);
        chartPanel.setPreferredSize(new Dimension(500, 400));

        vista.Panel.removeAll();
        vista.Panel.add(chartPanel, BorderLayout.CENTER);
        vista.Panel.revalidate();
        vista.Panel.repaint();
    }

    private void abrirFrmOpciones() {
        FrmOpciones opciones = new FrmOpciones();
        opciones.setVisible(true);

        opciones.Ordenar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obtenerOpcionesOrdenamiento(opciones);
                opciones.setVisible(false);
                abrirFrmOrdenar();
            }
        });

        opciones.Cancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opciones.setVisible(false);
            }
        });
    }

    private void obtenerOpcionesOrdenamiento(FrmOpciones opciones) {
        if (opciones.Bubble.isSelected()) {
            algoritmoSeleccionado = "Bubble";
        } else if (opciones.Insert.isSelected()) {
            algoritmoSeleccionado = "Insert";
        } else if (opciones.Merge.isSelected()) {
            algoritmoSeleccionado = "Merge";
        } else if (opciones.Quick.isSelected()) {
            algoritmoSeleccionado = "Quick";
        } else if (opciones.Select.isSelected()) {
            algoritmoSeleccionado = "Select";
        } else if (opciones.Shell.isSelected()) {
            algoritmoSeleccionado = "Shell";
        }

        tipoOrdenSeleccionado = opciones.Ascendente.isSelected() ? "Ascendente" : "Descendente";

        if (opciones.Alta.isSelected()) {
            velocidadSeleccionada = "Alta";
        } else if (opciones.Media.isSelected()) {
            velocidadSeleccionada = "Media";
        } else {
            velocidadSeleccionada = "Baja";
        }
    }

    private void abrirFrmOrdenar() {
        frmOrdenar = new FrmOrdenar();
        frmOrdenar.setVisible(true);
        generarGrafica();

        frmOrdenar.Algoritmo.setText("Algoritmo: " + algoritmoSeleccionado);
        frmOrdenar.Velocidad.setText("Velocidad: " + velocidadSeleccionada);
        frmOrdenar.Orden.setText("Orden: " + tipoOrdenSeleccionado);

        new Thread(() -> {
            tiempoInicio = System.currentTimeMillis();
            pasos = 0;

            switch (algoritmoSeleccionado) {
                case "Bubble":
                    bubbleSort(frmOrdenar);
                    break;
                case "Insert":
                    insertSort(frmOrdenar);
                    break;
                case "Merge":
                    mergeSort(frmOrdenar);
                    break;
                case "Quick":
                    quickSort(frmOrdenar);
                    break;
                case "Select":
                    selectSort(frmOrdenar);
                    break;
                case "Shell":
                    shellSort(frmOrdenar);
                    break;
            }

            generarPDF(modelo.getValoresY()); // Generar el PDF después de ordenar
        }).start();
    }

    private void actualizarLabels(FrmOrdenar frmOrdenar, long tiempoTranscurrido, int pasos) {
        if (frmOrdenar == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
        String tiempoFormateado = sdf.format(new Date(tiempoTranscurrido));

        frmOrdenar.Tiempo.setText("Tiempo: " + tiempoFormateado);
        frmOrdenar.Pasos.setText("Pasos: " + pasos);
    }

    private void bubbleSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        int n = datos.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(j) > datos.get(j + 1)) ||
                        (tipoOrdenSeleccionado.equals("Descendente") && datos.get(j) < datos.get(j + 1))) {
                    int temp = datos.get(j);
                    datos.set(j, datos.get(j + 1));
                    datos.set(j + 1, temp);

                    pasos++;
                    long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
                    SwingUtilities.invokeLater(() -> {
                        actualizarGraficaPasoAPaso(datos);
                        actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
                    });
                }

                try {
                    if (velocidadSeleccionada.equals("Alta")) {
                        Thread.sleep(100);
                    } else if (velocidadSeleccionada.equals("Media")) {
                        Thread.sleep(200);
                    } else {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void insertSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        for (int i = 1; i < datos.size(); i++) {
            int key = datos.get(i);
            int j = i - 1;
            while (j >= 0 && ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(j) > key) ||
                    (tipoOrdenSeleccionado.equals("Descendente") && datos.get(j) < key))) {
                datos.set(j + 1, datos.get(j));
                j--;
            }
            datos.set(j + 1, key);

            pasos++;
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            SwingUtilities.invokeLater(() -> {
                actualizarGraficaPasoAPaso(datos);
                actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
            });

            try {
                if (velocidadSeleccionada.equals("Alta")) {
                    Thread.sleep(100);
                } else if (velocidadSeleccionada.equals("Media")) {
                    Thread.sleep(200);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void quickSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        quickSortHelper(datos, 0, datos.size() - 1, frmOrdenar);
    }

    private void quickSortHelper(List<Integer> datos, int low, int high, FrmOrdenar frmOrdenar) {
        if (low < high) {
            int pivotIndex = partition(datos, low, high, frmOrdenar);
            quickSortHelper(datos, low, pivotIndex - 1, frmOrdenar);
            quickSortHelper(datos, pivotIndex + 1, high, frmOrdenar);
        }
    }

    private int partition(List<Integer> datos, int low, int high, FrmOrdenar frmOrdenar) {
        int pivot = datos.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(j) <= pivot) ||
                    (tipoOrdenSeleccionado.equals("Descendente") && datos.get(j) >= pivot)) {
                i++;
                int temp = datos.get(i);
                datos.set(i, datos.get(j));
                datos.set(j, temp);

                pasos++;
                long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
                SwingUtilities.invokeLater(() -> actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos));
            }
        }

        int temp = datos.get(i + 1);
        datos.set(i + 1, datos.get(high));
        datos.set(high, temp);

        pasos++;
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
        SwingUtilities.invokeLater(() -> {
            actualizarGraficaPasoAPaso(datos);
            actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
        });

        try {
            if (velocidadSeleccionada.equals("Alta")) {
                Thread.sleep(100);
            } else if (velocidadSeleccionada.equals("Media")) {
                Thread.sleep(200);
            } else {
                Thread.sleep(500);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return i + 1;
    }

    private void selectSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        for (int i = 0; i < datos.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < datos.size(); j++) {
                if ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(j) < datos.get(minIndex)) ||
                        (tipoOrdenSeleccionado.equals("Descendente") && datos.get(j) > datos.get(minIndex))) {
                    minIndex = j;
                }
            }

            int temp = datos.get(i);
            datos.set(i, datos.get(minIndex));
            datos.set(minIndex, temp);

            pasos++;
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            SwingUtilities.invokeLater(() -> {
                actualizarGraficaPasoAPaso(datos);
                actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
            });

            try {
                if (velocidadSeleccionada.equals("Alta")) {
                    Thread.sleep(100);
                } else if (velocidadSeleccionada.equals("Media")) {
                    Thread.sleep(200);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void shellSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        int n = datos.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int temp = datos.get(i);
                int j = i;
                while (j >= gap && ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(j - gap) > temp) ||
                        (tipoOrdenSeleccionado.equals("Descendente") && datos.get(j - gap) < temp))) {
                    datos.set(j, datos.get(j - gap));
                    j -= gap;
                }
                datos.set(j, temp);

                pasos++;
                long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
                SwingUtilities.invokeLater(() -> {
                    actualizarGraficaPasoAPaso(datos);
                    actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
                });

                try {
                    if (velocidadSeleccionada.equals("Alta")) {
                        Thread.sleep(100);
                    } else if (velocidadSeleccionada.equals("Media")) {
                        Thread.sleep(200);
                    } else {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void mergeSort(FrmOrdenar frmOrdenar) {
        List<Integer> datos = modelo.getValoresY();
        mergeSortHelper(datos, 0, datos.size() - 1, frmOrdenar);
    }

    private void mergeSortHelper(List<Integer> datos, int left, int right, FrmOrdenar frmOrdenar) {
        if (left < right) {
            int mid = (left + right) / 2;

            mergeSortHelper(datos, left, mid, frmOrdenar);
            mergeSortHelper(datos, mid + 1, right, frmOrdenar);

            merge(datos, left, mid, right, datos, frmOrdenar);
        }
    }

    private void merge(List<Integer> datos, int left, int mid, int right, List<Integer> originalDatos, FrmOrdenar frmOrdenar) {
        List<Integer> temp = new ArrayList<>();
        int i = left, j = mid + 1;

        while (i <= mid && j <= right) {
            if ((tipoOrdenSeleccionado.equals("Ascendente") && datos.get(i) <= datos.get(j)) ||
                    (tipoOrdenSeleccionado.equals("Descendente") && datos.get(i) >= datos.get(j))) {
                temp.add(datos.get(i));
                i++;
            } else {
                temp.add(datos.get(j));
                j++;
            }
        }

        while (i <= mid) {
            temp.add(datos.get(i));
            i++;
        }

        while (j <= right) {
            temp.add(datos.get(j));
            j++;
        }

        for (int k = left; k <= right; k++) {
            datos.set(k, temp.get(k - left));

            pasos++;
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            SwingUtilities.invokeLater(() -> {
                actualizarGraficaPasoAPaso(originalDatos);
                actualizarLabels(frmOrdenar, tiempoTranscurrido, pasos);
            });

            try {
                if (velocidadSeleccionada.equals("Alta")) {
                    Thread.sleep(100);
                } else if (velocidadSeleccionada.equals("Media")) {
                    Thread.sleep(200);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void actualizarGraficaPasoAPaso(List<Integer> datos) {
        if (frmOrdenar == null) return;

        String tituloGrafica = vista.Titulo.getText().trim();
        if (tituloGrafica.isEmpty()) {
            tituloGrafica = "Gráfico Ordenado";
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            dataset.addValue(datos.get(i), modelo.getCategoriasX().get(i), modelo.getCategoriasX().get(i));
        }

        finalChart = ChartFactory.createBarChart(
                tituloGrafica,
                modelo.getTituloX(),
                modelo.getTituloY(),
                dataset
        );

        BarRenderer renderer = (BarRenderer) finalChart.getCategoryPlot().getRenderer();
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            renderer.setSeriesPaint(i, new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
        }

        ChartPanel chartPanel = new ChartPanel(finalChart);
        chartPanel.setPreferredSize(new Dimension(500, 400));

        frmOrdenar.Mostrar.removeAll();
        frmOrdenar.Mostrar.add(chartPanel, BorderLayout.CENTER);
        frmOrdenar.Mostrar.revalidate();
        frmOrdenar.Mostrar.repaint();
    }

    private void generarPDF(List<Integer> datosOrdenados) {
    Document document = new Document();
    try {
        PdfWriter.getInstance(document, new FileOutputStream("Reporte_Ordenamiento.pdf"));
        document.open();

        // Información del estudiante
        document.add(new Paragraph("Angel Emanuel Rodriguez Corado"));
        document.add(new Paragraph("202404856"));
        document.add(Chunk.NEWLINE);

        // Información del ordenamiento
        document.add(new Paragraph("Algoritmo: " + algoritmoSeleccionado));
        document.add(new Paragraph("Velocidad: " + velocidadSeleccionada));
        document.add(new Paragraph("Orden: " + tipoOrdenSeleccionado));
        document.add(new Paragraph("" + frmOrdenar.Tiempo.getText()));
        document.add(new Paragraph("" + frmOrdenar.Pasos.getText()));
        document.add(Chunk.NEWLINE);

        // Dato mínimo y máximo
        int min = datosOrdenados.stream().min(Integer::compare).orElse(0);
        int max = datosOrdenados.stream().max(Integer::compare).orElse(0);
        document.add(new Paragraph("Dato mínimo: " + min));
        document.add(new Paragraph("Dato máximo: " + max));
        document.add(Chunk.NEWLINE);

        // Página 1: Datos y gráfica inicial (desordenados)
        document.add(new Paragraph("Datos iniciales (Desordenados):"));
        PdfPTable tablaInicial = new PdfPTable(2); // Dos columnas: X y Y
        tablaInicial.addCell(modelo.getTituloX()); // Título X
        tablaInicial.addCell(modelo.getTituloY()); // Título Y
        for (int i = 0; i < modelo.getCategoriasX().size(); i++) {
            tablaInicial.addCell(modelo.getCategoriasX().get(i)); // Valor de X
            tablaInicial.addCell(String.valueOf(modelo.getValoresY().get(i))); // Valor de Y
        }
        document.add(tablaInicial);
        document.add(Chunk.NEWLINE);

        // Gráfica inicial
        document.add(new Paragraph("Gráfica inicial:"));
        if (initialChart != null) {
            ByteArrayOutputStream chartImage = new ByteArrayOutputStream();
            ChartUtilities.writeChartAsPNG(chartImage, initialChart, 300, 200);
            com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(chartImage.toByteArray());
            document.add(image);
        }

        // Salto de página
        document.newPage();

        // Página 2: Datos y gráfica ordenados
        document.add(new Paragraph("Datos ordenados:"));
        PdfPTable tablaOrdenada = new PdfPTable(2); // Dos columnas: X y Y
        tablaOrdenada.addCell(modelo.getTituloX()); // Título X
        tablaOrdenada.addCell(modelo.getTituloY()); // Título Y
        for (int i = 0; i < datosOrdenados.size(); i++) {
            tablaOrdenada.addCell(String.valueOf(i)); // Índice como X
            tablaOrdenada.addCell(String.valueOf(datosOrdenados.get(i))); // Dato ordenado como Y
        }
        document.add(tablaOrdenada);
        document.add(Chunk.NEWLINE);

        // Gráfica final
        document.add(new Paragraph("Gráfica final:"));
        if (finalChart != null) {
            ByteArrayOutputStream chartImage = new ByteArrayOutputStream();
            ChartUtilities.writeChartAsPNG(chartImage, finalChart, 300, 200);
            com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(chartImage.toByteArray());
            document.add(image);
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        document.close();
    }
}

}
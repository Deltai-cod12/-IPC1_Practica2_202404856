/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatosModelo {
    private List<String> categoriasX; // Nombres de la primera columna
    private List<Integer> valoresY;   // Valores numéricos de la segunda columna
    private String tituloX;
    private String tituloY;

    public DatosModelo() {
        categoriasX = new ArrayList<>();
        valoresY = new ArrayList<>();
    }

    public boolean cargarDesdeArchivo(File archivo) {
    try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
        String linea = br.readLine(); // Leer la primera línea (encabezados)
        if (linea == null) {
            System.out.println("El archivo está vacío.");
            return false;
        }

        String[] encabezados = linea.split(",");
        if (encabezados.length < 2) {
            System.out.println("El archivo no tiene el formato correcto.");
            return false;
        }

        tituloX = encabezados[0];
        tituloY = encabezados[1];

        categoriasX.clear();
        valoresY.clear();

        String lineaDatos;
        while ((lineaDatos = br.readLine()) != null) {
            String[] partes = lineaDatos.split(",");
            if (partes.length < 2) continue;

            try {
                categoriasX.add(partes[0]); // Nombre de la categoría
                valoresY.add(Integer.parseInt(partes[1])); // Valor numérico
            } catch (NumberFormatException e) {
                System.out.println("Error al convertir: " + partes[1]);
                return false;
            }
        }

        return true; // Indica que la carga fue exitosa
    } catch (Exception e) {
        System.out.println("Error al leer el archivo: " + e.getMessage());
        return false;
    }
}


    public List<String> getCategoriasX() {
        return categoriasX;
    }

    public List<Integer> getValoresY() {
        return valoresY;
    }

    public String getTituloX() {
        return tituloX;
    }

    public String getTituloY() {
        return tituloY;
    }
}

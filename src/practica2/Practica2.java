/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package practica2;

import Controlador.Controlador;
import Vista.FrmPrincipal;
import javax.swing.SwingUtilities;

public class Practica2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmPrincipal vista = new FrmPrincipal();
            Controlador controlador = new Controlador(vista); // Guardamos la referencia del controlador
            vista.setVisible(true);
        });
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package screensframework;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javax.swing.JOptionPane;
import screensframework.DBConnect.DBConnection;
import org.apache.commons.codec.digest.DigestUtils;
import java.sql.PreparedStatement;
/**
 * FXML Controller class
 *
 * @author Wil
 */
public class LoginController implements Initializable, ControlledScreen {
    ScreensController controlador;
    private Validaciones validation = new Validaciones();
    private Connection conexion;
    
    public TextField tfUsuario;
    public PasswordField tfPass;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public void setScreenParent(ScreensController pantallaPadre) {
        controlador = pantallaPadre;
        
    }
    
    @FXML
    private void iniciarSesion(ActionEvent event){
        /********************************** 
         *         Area de validaciones 
         ***********************************/
//        if (!validation.validarVacios(tfUsuario.getText(), "USUARIO")) {
//            return;
//        }
//
//
//        if (!validation.validarMaximo(tfUsuario.getText(), "USUARIO", 20, 2)) {
//            return;
//        }
        
        /********************************** 
         *     Fin de las validaciones 
         ***********************************/
        
        //______________________________________________________
        /* SE HACE EL LLAMADO AL MODELO PARA ENTRAR AL SISTEMA */
//        try {
//            conexion = DBConnection.connect();
//            String sql = "SELECT * FROM "
//                    + " usuarios WHERE "
//                    + " usuario = '"+tfUsuario.getText()+"' AND "
//                    + " pass = '"+DigestUtils.sha1Hex(tfPass.getText())+"'";
//            ResultSet rs = conexion.createStatement().executeQuery(sql);
//
//            boolean existeUsuario = rs.next();
//
//            if (existeUsuario) {
//                tfUsuario.setText("");
//                tfPass.setText("");
//                controlador.setScreen(ScreensFramework.contenidoID);
//            } else {
//                JOptionPane.showMessageDialog(null, "Este usuario no está registrado");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Error " + e.getMessage());
//        }
        /**********************************
         *     Nuevo modelo Refactorizado
         ***********************************/
        if (!validarCampos()) {
            return;
        }

        try {
            if (autenticarUsuario(tfUsuario.getText(), tfPass.getText())) {
                limpiarCampos();
                navegarAPantalla(ScreensFramework.contenidoID);
            } else {
                mostrarMensaje("Este usuario no está registrado");
            }
        } catch (SQLException e) {
            manejarErrorBD(e);
        }
    }
    protected boolean validarCampos() {
        return validation.validarVacios(tfUsuario.getText(), "USUARIO")
                && validation.validarMaximo(tfUsuario.getText(), "USUARIO", 20, 2);
    }

    protected boolean autenticarUsuario(String usuario, String password) throws SQLException {
        conexion = DBConnection.connect();
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND pass = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, DigestUtils.sha1Hex(password));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    protected void limpiarCampos() {
        tfUsuario.setText("");
        tfPass.setText("");
    }

    protected void navegarAPantalla(String pantallaID) {
        controlador.setScreen(pantallaID);
    }

    protected void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje);
    }

    protected void manejarErrorBD(SQLException e) {
        System.out.println("Error " + e.getMessage());
    }

    /// /////
    
//    @FXML
//    private void irFormRegistro(ActionEvent event) {
//        controlador.setScreen(ScreensFramework.registroID);
//    }
//
//    @FXML
//    private void salir(ActionEvent event) {
//        Platform.exit();
//    }


    @FXML
    private void irFormRegistro(ActionEvent event) {
        navegarAPantalla(ScreensFramework.registroID);
    }

    @FXML
    private void salir(ActionEvent event) {
        Platform.exit();
    }

    // Setters para inyección en pruebas
    public void setValidation(Validaciones validation) {
        this.validation = validation;
    }
}

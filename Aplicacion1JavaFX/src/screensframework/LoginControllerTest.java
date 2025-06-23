package screensframework;

import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import screensframework.DBConnect.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class LoginControllerTest {
    private LoginController controller;

    @Mock
    private Validaciones mockValidation;

    @Mock
    private ScreensController mockScreensController;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStmt;

    @Mock
    private ResultSet mockResultSet;

    @BeforeAll
    static void initToolkit() {
        // Initialize JavaFX toolkit for controls
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setUp() {
        controller = new LoginController();
        // inject mocks
        controller.setValidation(mockValidation);
        controller.setScreenParent(mockScreensController);

        // initialize text fields
        controller.tfUsuario = new TextField();
        controller.tfPass = new PasswordField();
    }

    @Test
    void validarCampos_AllValidationsPass_ReturnsTrue() {
        when(mockValidation.validarVacios(anyString(), eq("USUARIO"))).thenReturn(true);
        when(mockValidation.validarMaximo(anyString(), eq("USUARIO"), eq(20), eq(2))).thenReturn(true);

        boolean result = controller.validarCampos();

        assertTrue(result, "validarCampos should return true when all validations pass");
        verify(mockValidation).validarVacios(controller.tfUsuario.getText(), "USUARIO");
        verify(mockValidation).validarMaximo(controller.tfUsuario.getText(), "USUARIO", 20, 2);
    }

    @Test
    void limpiarCampos_ClearsUsernameAndPasswordFields() {
        controller.tfUsuario.setText("user123");
        controller.tfPass.setText("secret");

        controller.limpiarCampos();

        assertEquals("", controller.tfUsuario.getText());
        assertEquals("", controller.tfPass.getText());
    }

    @Test
    void navegarAPantalla_CallsSetScreenOnController() {
        String screenId = "CONTENT_SCREEN";
        controller.navegarAPantalla(screenId);

        verify(mockScreensController).setScreen(screenId);
    }

    @Test
    void autenticarUsuario_UserExists_ReturnsTrue() throws SQLException {
        String user = "Rey";
        String pass = "rey123";
        String hashed = DigestUtils.sha1Hex(pass);

        try (MockedStatic<DBConnection> dbConnStatic = Mockito.mockStatic(DBConnection.class)) {
            dbConnStatic.when(DBConnection::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(1);

            boolean result = controller.autenticarUsuario(user, pass);
            assertTrue(result, "autenticarUsuario should return true when user count > 0");

            verify(mockConnection).prepareStatement("SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND pass = ?");
            verify(mockStmt).setString(1, user);
            verify(mockStmt).setString(2, hashed);
            verify(mockStmt).executeQuery();
        }
    }

    @Test
    void autenticarUsuario_UserNotExists_ReturnsFalse() throws SQLException {
        String user = "bob";
        String pass = "wrongpass";

        try (MockedStatic<DBConnection> dbConnStatic = Mockito.mockStatic(DBConnection.class)) {
            dbConnStatic.when(DBConnection::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(0);

            boolean result = controller.autenticarUsuario(user, pass);
            assertFalse(result, "autenticarUsuario should return false when user count is 0");
        }
    }

}

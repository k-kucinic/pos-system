package hr.tvz.possystem.authentication;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Prikazuje dijaloški prozor za unos administratorske lozinke radi autorizacije.
 * Koristi se za zaštitu radnji koje su ograničene na administratore.
 */
public class PasswordDialog extends Dialog<String> {
    /**
     * Inicijalizira dijaloški prozor za unos administratorske
     * lozinke s prilagođenom porukom.
     *
     * @param message poruka koja se prikazuje iznad polja za unos lozinke
     */
    public PasswordDialog(String message) {
        this.setTitle("Autorizacija");
        this.setHeaderText("Potrebna je autorizacija");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        try {
            Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
            stage.getIcons().add(icon);

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }

        DialogPane pane = this.getDialogPane();
        ButtonType saveButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Odustani", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(saveButton, cancelButton);
        pane.setMinWidth(400);
        pane.setPadding(new Insets(15));
        Label label = new Label(message);
        PasswordField pwd = new PasswordField();
        pwd.setPrefWidth(250);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(label, 0, 0);
        grid.add(pwd, 1, 0);
        pane.setContent(grid);
        this.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return pwd.getText();
            }
            return null;
        });

    }

}


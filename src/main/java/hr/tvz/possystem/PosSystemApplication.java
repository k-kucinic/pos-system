package hr.tvz.possystem;

import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.model.CisEcho;
import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;
import hr.tvz.possystem.printer.PosReceiptPrinter;
import hr.tvz.possystem.printer.PrintImagesForFun;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public class PosSystemApplication extends Application {

    public static Stage mainStage;

    public static User AppUser;

    public static ViewType viewType;
    public static Boolean multiRoles;
/*
    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(PosSystemApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("Blagajna");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
        mainStage = stage;
        setViewType(ViewType.LOGIN);

    }*/

    @Override
    public void start(Stage stage) throws IOException {
        setViewType(ViewType.ADMIN);
        System.out.println("Truststore: " + System.getProperty("javax.net.ssl.trustStore"));
        System.out.println("Truststore password: " + System.getProperty("javax.net.ssl.trustStorePassword"));
        System.out.println("Protocols: " + System.getProperty("https.protocols"));
        System.out.println("Printer uključen: " + PosReceiptPrinter.isPrinterConnected("POS-58(copy of 1)"));

        setMultiRoles(true);
        AppUser = new User.UserBuilder()
                .withId(1)
                .withUsername("blagajnik1")
                .withPasswordHash("$2a$10$V8DXcsXIkGuuCBhzciGq5uTDUNx1/.NQZcftS7CNEbPhrAoCwgYFK")
                .withActive(true)
                .withRoles(new ArrayList<>())
                .withCreatedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        AppUser.setRoles(List.of(new Role(1, "admin"), new Role(2, "cashier")));
        FXMLLoader fxmlLoader = new FXMLLoader(PosSystemApplication.class.getResource("/hr/tvz/possystem/admin/admin-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Blagajna");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        try {
            Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
            stage.getIcons().add(icon);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        try {
            String echoResp = CisEcho.callEcho("https://cistest.apis-it.hr:8449/FiskalizacijaServiceTest");
            if (echoResp != null) {
                System.out.println(echoResp);
            }
        } catch (Exception e) {
            System.out.println(e.getClass() + " : " + e.getMessage());
        }
        try {
            PrintImagesForFun.printImageEscPos(new File("src/main/resources/images/hq720-1.png"), 384);
        } catch (Exception e) {
            System.out.println("Ne radi");
        }


        stage.show();
        mainStage = stage;

    }

    public static void main(String[] args) {
        launch();

    }

    public static void setAppUser(User appUser) {
        AppUser = appUser;
    }

    public static void setViewType(ViewType viewType) {
        PosSystemApplication.viewType = viewType;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage mainStage) {
        PosSystemApplication.mainStage = mainStage;
    }

    public static User getAppUser() {
        return AppUser;
    }

    public static ViewType getViewType() {
        return viewType;
    }

    public static Boolean getMultiRoles() {
        return multiRoles;
    }

    public static void setMultiRoles(Boolean multiRoles) {
        PosSystemApplication.multiRoles = multiRoles;
    }
}
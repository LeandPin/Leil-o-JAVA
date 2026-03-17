package Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionGUI extends Application {
    private DataOutputStream out;
    private VBox notificationBox;
    private TilePane productGrid;
    private Scene scene;
    private final List<Scene> allScenes = new ArrayList<>();

    // Nome do usuário — definido uma vez na tela inicial
    private String nomeUsuarioGlobal = "";
    private Label lblNavUser;

    @Override
    public void start(Stage stage) {
        // --- TELA DE LOGIN: pede o nome antes de entrar ---
        Stage loginStage = new Stage();
        loginStage.setTitle("Leilão PB — Entrar");

        VBox loginBox = new VBox(16);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.setStyle("-fx-background-color: #0f172a;");

        Label loginTitle = new Label("🔨 Leilão PB");
        loginTitle.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #f8a000;");

        Label loginSub = new Label("Digite seu nome para participar");
        loginSub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");

        TextField loginField = new TextField();
        loginField.setPromptText("Seu nome...");
        loginField.setMaxWidth(260);
        loginField.setStyle(
                "-fx-background-color: #1e293b; -fx-text-fill: white; " +
                        "-fx-border-color: #334155; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14;"
        );

        Button btnEntrar = new Button("Entrar no Leilão →");
        btnEntrar.setMaxWidth(260);
        btnEntrar.setStyle(
                "-fx-background-color: #f8a000; -fx-text-fill: #0f172a; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        Label loginErro = new Label("");
        loginErro.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12;");

        loginBox.getChildren().addAll(loginTitle, loginSub, loginField, btnEntrar, loginErro);

        Scene loginScene = new Scene(loginBox, 380, 280);
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        loginStage.show();

        btnEntrar.setOnAction(e -> {
            String nome = loginField.getText().trim();
            if (nome.isEmpty()) {
                loginErro.setText("Por favor, insira um nome.");
                return;
            }
            nomeUsuarioGlobal = nome;
            loginStage.close();
            abrirJanelaPrincipal(stage);
        });

        // Enter no campo também confirma
        loginField.setOnAction(e -> btnEntrar.fire());
    }

    private void abrirJanelaPrincipal(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // --- NAVBAR ---
        HBox navbar = new HBox();
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPadding(new Insets(0, 24, 0, 24));
        navbar.setPrefHeight(60);
        navbar.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        Label logo = new Label("🔨 Leilão PB");
        logo.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #f8a000;");

        // Spacer empurra o nome do usuário para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblNavUser = new Label("👤 " + nomeUsuarioGlobal);
        lblNavUser.setStyle(
                "-fx-text-fill: #e2e8f0; -fx-font-size: 13; " +
                        "-fx-background-color: #334155; -fx-padding: 6 14 6 14; " +
                        "-fx-background-radius: 20;"
        );

        navbar.getChildren().addAll(logo, spacer, lblNavUser);

        // --- SIDEBAR ---
        VBox sidebar = new VBox(16);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(270);
        sidebar.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 0 1;");

        Label notifTitle = new Label("🔔 NOTIFICAÇÕES");
        notifTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-font-weight: bold;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");

        notificationBox = new VBox(6);
        ScrollPane notifScroll = new ScrollPane(notificationBox);
        notifScroll.setFitToWidth(true);
        notifScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        notifScroll.setPrefHeight(600);
        VBox.setVgrow(notifScroll, Priority.ALWAYS);

        sidebar.getChildren().addAll(notifTitle, sep, notifScroll);

        // --- GRID ---
        productGrid = new TilePane(20, 20);
        productGrid.setPadding(new Insets(24));
        productGrid.setAlignment(Pos.TOP_LEFT);
        productGrid.setStyle("-fx-background-color: #0f172a;");

        ScrollPane scroll = new ScrollPane(productGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(navbar);
        root.setCenter(scroll);
        root.setRight(sidebar);

        scene = new Scene(root, 1200, 800);
        allScenes.add(scene);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

        stage.setTitle("Leilão PB");
        stage.setScene(scene);
        stage.show();

        new Thread(this::connect).start();
    }

    private Node buildImage(String imagemPath, double w, double h) {
        try {
            File f = new File(imagemPath);
            if (f.exists()) {
                ImageView iv = new ImageView(new Image(f.toURI().toString()));
                iv.setFitWidth(w); iv.setFitHeight(h);
                iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(w, h);
                clip.setArcWidth(12); clip.setArcHeight(12);
                iv.setClip(clip);
                return iv;
            }
        } catch (Exception ignored) {}
        Rectangle r = new Rectangle(w, h, Color.web("#1e293b"));
        r.setArcWidth(12); r.setArcHeight(12);
        return r;
    }

    private VBox createProductCard(int id, String name, String imagemPath) {
        VBox card = new VBox(10);
        card.setId("card-" + id);
        card.setPrefWidth(240);
        card.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 14; " +
                        "-fx-border-color: #334155; -fx-border-radius: 14; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-cursor: hand;"
        );

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #263448; -fx-background-radius: 14; " +
                        "-fx-border-color: #f8a000; -fx-border-radius: 14; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 14; " +
                        "-fx-border-color: #334155; -fx-border-radius: 14; -fx-border-width: 1; " +
                        "-fx-padding: 12; -fx-cursor: hand;"
        ));

        Node img = buildImage(imagemPath, 216, 130);

        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        lblName.setWrapText(true);

        HBox details = new HBox(20);
        details.setAlignment(Pos.CENTER_LEFT);

        VBox bidInfo = new VBox(2);
        Label bidTitle = new Label("Lance Atual");
        bidTitle.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
        Label lblPrice = new Label("R$ --");
        lblPrice.setId("price-" + id);
        lblPrice.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #f8a000;");
        bidInfo.getChildren().addAll(bidTitle, lblPrice);

        VBox timeInfo = new VBox(2);
        Label timeTitle = new Label("Tempo");
        timeTitle.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b;");
        Label lblTime = new Label("--s");
        lblTime.setId("time-" + id);
        lblTime.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        timeInfo.getChildren().addAll(timeTitle, lblTime);

        details.getChildren().addAll(bidInfo, timeInfo);

        Button btnBid = new Button("🔨 Entrar na Sala");
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setId("main-btn-" + id);
        btnBid.setStyle(
                "-fx-background-color: #f8a000; -fx-text-fill: #0f172a; " +
                        "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnBid.setOnAction(e -> openRoomWindow(id, name, imagemPath));

        card.getChildren().addAll(img, lblName, details, btnBid);
        return card;
    }

    private void openRoomWindow(int id, String name, String imagemPath) {
        Stage roomStage = new Stage();
        roomStage.setTitle("Leilão PB — " + name);

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #0f172a;");

        // Header da sala
        Label roomTitle = new Label(name);
        roomTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Node img = buildImage(imagemPath, 412, 210);

        // Preço e Timer
        Label lblRoomPrice = new Label("R$ --");
        lblRoomPrice.setId("room-price-" + id);
        lblRoomPrice.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-text-fill: #f8a000;");

        Label lblRoomTime = new Label("--s");
        lblRoomTime.setId("room-time-" + id);
        lblRoomTime.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        VBox priceBox = new VBox(2, styledLabel("Lance Atual"), lblRoomPrice);
        VBox timeBox  = new VBox(2, styledLabel("Tempo Restante"), lblRoomTime);
        HBox priceRow = new HBox(40, priceBox, timeBox);
        priceRow.setPadding(new Insets(12, 0, 12, 0));
        priceRow.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 10; " +
                        "-fx-padding: 16; -fx-border-color: #334155; " +
                        "-fx-border-radius: 10; -fx-border-width: 1;"
        );

        // O nome já vem preenchido do login
        Label lblNomeFixed = new Label("Participando como: " + nomeUsuarioGlobal);
        lblNomeFixed.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        TextField valorField = new TextField();
        valorField.setPromptText("Valor do lance (ex: 500.00)");
        valorField.setStyle(
                "-fx-background-color: #1e293b; -fx-text-fill: white; " +
                        "-fx-border-color: #334155; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 13;"
        );

        Button btnLance = new Button("💰 Dar Lance");
        btnLance.setMaxWidth(Double.MAX_VALUE);
        btnLance.setId("room-btn-" + id);
        btnLance.setStyle(
                "-fx-background-color: #f8a000; -fx-text-fill: #0f172a; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnLance.setOnAction(e -> {
            String valor = valorField.getText().trim();
            if (valor.isEmpty()) return;
            send("ENTRAR " + id);
            send("LANCE " + nomeUsuarioGlobal + " " + valor);
            valorField.clear();
            addNotification("Lance R$" + valor + " em " + name);
        });

        // Log da sala
        Label logTitle = new Label("📋 HISTÓRICO DA SALA");
        logTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11; -fx-font-weight: bold;");

        VBox roomLog = new VBox(6);
        roomLog.setId("room-log-" + id);
        ScrollPane logScroll = new ScrollPane(roomLog);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(150);
        logScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator sep1 = darkSep();
        Separator sep2 = darkSep();

        layout.getChildren().addAll(
                roomTitle, img, priceRow,
                sep1,
                lblNomeFixed, valorField, btnLance,
                sep2,
                logTitle, logScroll
        );

        Scene roomScene = new Scene(layout, 460, 720);
        allScenes.add(roomScene);
        try { roomScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

        roomStage.setScene(roomScene);
        roomStage.show();
        roomStage.setOnCloseRequest(e -> allScenes.remove(roomScene));

        send("ENTRAR " + id);
    }

    // Helpers de estilo
    private Label styledLabel(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b;");
        return l;
    }

    private Separator darkSep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #334155;");
        return s;
    }

    private void addNotification(String msg) {
        Label n = new Label(msg);
        n.setWrapText(true);
        n.setPrefWidth(230);
        n.setStyle(
                "-fx-text-fill: #e2e8f0; -fx-font-size: 12; " +
                        "-fx-background-color: #1e293b; -fx-background-radius: 6; " +
                        "-fx-border-color: #334155; -fx-border-radius: 6; " +
                        "-fx-border-width: 1; -fx-padding: 6 10 6 10;"
        );
        Platform.runLater(() -> notificationBox.getChildren().add(0, n));
    }

    private void connect() {
        try (Socket s = new Socket("localhost", 12345);
             DataInputStream in = new DataInputStream(s.getInputStream())) {
            out = new DataOutputStream(s.getOutputStream());
            while (true) {
                String msg = in.readUTF();
                Platform.runLater(() -> handleMessage(msg));
            }
        } catch (Exception e) {
            addNotification("⚠️ Conexão encerrada.");
        }
    }

    private Node lookupAll(String cssId) {
        for (Scene s : allScenes) {
            Node n = s.lookup(cssId);
            if (n != null) return n;
        }
        return null;
    }

    private void handleMessage(String msg) {
        if (msg.startsWith("ITEM ")) {
            String[] p = msg.split(" ", 5);
            int    id     = Integer.parseInt(p[1]);
            String imagem = p[3];
            String nome   = p[4];
            if (scene.lookup("#card-" + id) == null) {
                productGrid.getChildren().add(createProductCard(id, nome, imagem));
            }

        } else if (msg.startsWith("TEMPO ")) {
            String[] p = msg.split(" ");
            Label main = (Label) lookupAll("#time-"      + p[1]);
            Label room = (Label) lookupAll("#room-time-" + p[1]);
            if (main != null) main.setText(p[2] + "s");
            if (room != null) room.setText(p[2] + "s");

        } else if (msg.startsWith("NOVO_VALOR ")) {
            String[] p = msg.split(" ");
            Label main = (Label) lookupAll("#price-"      + p[1]);
            Label room = (Label) lookupAll("#room-price-" + p[1]);
            if (main != null) main.setText("R$ " + p[2]);
            if (room != null) room.setText("R$ " + p[2]);

        } else if (msg.startsWith("VENDIDO ")) {
            String[] p = msg.split(" ", 3);
            String idSala = p[1], vencedor = p[2];

            Label mainPrice = (Label) lookupAll("#price-"      + idSala);
            Label roomPrice = (Label) lookupAll("#room-price-" + idSala);
            if (mainPrice != null) {
                mainPrice.setText("VENDIDO!");
                mainPrice.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            }
            if (roomPrice != null) {
                roomPrice.setText("🏆 " + vencedor);
                roomPrice.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            }

            VBox card = (VBox) lookupAll("#card-" + idSala);
            if (card != null) {
                card.setOpacity(0.45);
                Node btn = card.lookup("#main-btn-" + idSala);
                if (btn != null) btn.setDisable(true);
            }
            Node roomBtn = lookupAll("#room-btn-" + idSala);
            if (roomBtn != null) roomBtn.setDisable(true);

            addToRoomLog(idSala, "🏆 Vendido para " + vencedor);
            addNotification("🏆 Item " + idSala + " vendido para " + vencedor);

        } else if (msg.startsWith("MSG ")) {
            String[] p = msg.split(" ", 3);
            addNotification(p[2]);
            addToRoomLog(p[1], p[2]);

        } else if (msg.startsWith("SUCESSO") || msg.startsWith("ERRO") || msg.startsWith("NEGADO")) {
            addNotification(msg);
        }
    }

    private void addToRoomLog(String idSala, String texto) {
        VBox log = (VBox) lookupAll("#room-log-" + idSala);
        if (log == null) return;
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #cbd5e1;");
        log.getChildren().add(0, l);
    }

    private void send(String cmd) {
        try { out.writeUTF(cmd); } catch (Exception ignored) {}
    }

    public static void main(String[] args) { launch(args); }
}

import akka.actor.typed.ActorRef;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import akka.actor.typed.ActorSystem;
import java.util.HashMap;
import java.util.Map;


public class Main extends Application {

    TextField txtName;
    TextField txtInput;
    ScrollPane scrollPane;
    static TextArea txtAreaDisplay;
    //ActorRef членов чата
    static Map<String, ActorRef<User.Command>> membersChat = new HashMap<>();
    public static ActorSystem<User.Command> system;
    static String name;

    @Override
    public void start(Stage primaryStage) {
        VBox vBox = new VBox();
        scrollPane = new ScrollPane();   //панель для отображения текстовых сообщений
        HBox hBox = new HBox(); //панель для поля ввода и кнопки отправки

        txtAreaDisplay = new TextArea();
        txtAreaDisplay.setEditable(false);
        scrollPane.setContent(txtAreaDisplay);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        //текстовое поле и кнопку добавим в hBox
        txtName = new TextField();
        txtName.setPromptText("Write your name: " + name);
        txtName.setTooltip(new Tooltip("Write your name. "));
        txtInput = new TextField();
        txtInput.setPromptText("New message");
        txtInput.setTooltip(new Tooltip("Write your message. "));
        Button btnSend = new Button("Send");
        btnSend.setOnAction(new ButtonListener());

        hBox.getChildren().addAll(txtName, txtInput, btnSend);
        hBox.setHgrow(txtInput, Priority.ALWAYS);  //увеличения по мере увеличения размера окна

        vBox.getChildren().addAll(scrollPane, hBox);
        vBox.setVgrow(scrollPane, Priority.ALWAYS);

        //создание сцены
        Scene scene = new Scene(vBox, 450, 500);
        primaryStage.setTitle("Client: JavaFx Text Chat App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private class ButtonListener implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent e) {
                //get username and message
                String username = txtName.getText();
                String message = txtInput.getText();

                //if username is empty set it to 'Unknown'
                if (username.length() == 0) {
                    username = "Unknown";
                }
                //if message is empty, just return : don't send the message
                if (message.length() == 0) {
                    return;
                }
                //отправка приватных сообщений, проверяем валидность сообщения
            //которое должно иметь данную форму 'PM: username: message'
                if (message.startsWith("PM")) {
                    String[] a = message.split(": ");
                    if (a.length == 3) {
                        String namePM = a[1];
                        String msgPM = a[2];
                        if (membersChat.containsKey(namePM)) {
                            //отправляем сообщение в свой чат и в чат нужному нам юзеру
                            membersChat.get(namePM).tell(new User.PrivetMessage(msgPM, username));
                            system.tell(new User.PrivetMessage(msgPM, username));
                        }
                    }
                } else {
                    //если сообщение не приватное то рассылаем его всем участникам чата
                    system.tell(new User.Message(message, username));
                }
                txtInput.clear();
        }
    }

    private static void startup(String name, int port) {
        //Присоеденяем узел к кластеру
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        //overrides.put("akka.cluster.roles", Collections.singletonList("role"));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());
        system =
                ActorSystem.create(User.apply(name), "ClusterSystem", config);
    }

    public static void main(String[] args) {
        name = args[1];
        int port = Integer.parseInt(args[0]);

        startup(name, port); //Запускаем серверную часть
        launch(args); // Запускаем UI
    }
}




import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/*
File name: ClientChatUI.java
Author: Jamie Harnum #040898399
Course: CST8221 - JAP, Lab Section 303
Assignment: 2
Date: April 8
Professor: Daniel Cormier
Purpose: Provides the Client with a UI application
Class list: ClientChatUI, Controller, BorderedTitlePane
*/


/**
 Constructs the UI for the Client Chat
 @author Jamie Harnum
 @version 1
 @see Application
 @since 1.8.0_171
 */
public class ClientChatUI extends Application implements Accessible{
    TextField message;
    Button sendButton;
    TextArea display;
    ObjectOutputStream outputStream;
    Socket socket;
    ConnectionWrapper connection;
    Stage primaryStage;

    String title;
    Button connectButton;
    TextField hostInput;
    ComboBox comboBox;

    public ClientChatUI(String title){
        this.title = title;
    }

    public ClientChatUI(){
        title = "Jamie's ClientChatUI";
    }
    public TextArea getDisplay(){
        return display;
    }

    public void closeChat(){
        try {
            connection.closeConnection();
            Platform.runLater(()->primaryStage.close());

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     Starts the Client Chat UI
     @param primaryStage - the Stage to assign the application to
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(title);
        Scene clientScene = createScene();
        //sets size, location, and title before showing the stage
        //non-resizeable frame (588,500)
        this.primaryStage.setHeight(500);
        this.primaryStage.setWidth(588);
        this.primaryStage.setX(100);
        this.primaryStage.setY(0);
        this.primaryStage.setScene(clientScene);
        this.primaryStage.setResizable(false);

        this.primaryStage.show();
    }

    public void stop(){
        if(!socket.isClosed()){
            try {
                outputStream.writeObject(ChatProtocolConstants.CHAT_TERMINATOR);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void enableConnectButton(){
        connectButton.setDisable(false);
        connectButton.setStyle("-fx-base: red");
        sendButton.setDisable(true);
    }

    /**
     Creates the three main panes for the Stage
     */
    public Scene createScene(){
        VBox vbox = new VBox(1); //main pane for the stage, contains other three panes
        Scene clientScene = new Scene(vbox);
        Controller handler = new Controller();

        //connection section
        GridPane connectNode = new GridPane();
        connectNode.setHgap(5);
        connectNode.setVgap(5);
        ColumnConstraints column0 = new ColumnConstraints(40);
        ColumnConstraints column1 = new ColumnConstraints(100);
        ColumnConstraints column2 = new ColumnConstraints(100);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.ALWAYS); //stretch to fill the rest of the space

        connectNode.getColumnConstraints().addAll(column0,column1,column2,column3);

        Label host = new Label("_Host:");
        host.setMnemonicParsing(true);
        connectNode.add(host,0,0);

        hostInput = new TextField();
        hostInput.setPromptText("localhost");
        hostInput.setText("localhost");
        hostInput.positionCaret(0);
        hostInput.setPrefColumnCount(30);
        connectNode.add(hostInput,1,0,3,1);

        //Mnemonics for actions (like selecting a particular text field) are Accelerators in JavaFX
        KeyCombination hostKey = new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN);
        Runnable hostSelect = ()-> hostInput.requestFocus();
        clientScene.getAccelerators().put(hostKey,hostSelect);


        Label port = new Label("_Port:");
        port.setMnemonicParsing(true);
        connectNode.add(port,0,1);
        ObservableList<String> options = FXCollections.observableArrayList(
                "","8089", "65000", "65535"
        );

        comboBox = new ComboBox<>(options);
        comboBox.setPrefWidth(100);
        comboBox.setEditable(true);
        connectNode.add(comboBox,1,1);

        KeyCombination portKey = new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN);
        Runnable portSelect = ()-> comboBox.requestFocus();
        clientScene.getAccelerators().put(portKey,portSelect);

        connectButton = new Button("_Connect");
        connectButton.setMnemonicParsing(true);
        connectButton.setOnAction(handler);
        connectButton.setStyle("-fx-base: red");
        connectButton.setPrefWidth(100);
        connectNode.add(connectButton,2,1);

        BorderedTitlePane connection = new BorderedTitlePane("CONNECTION","left", Color.RED, connectNode);

        //message input section
        GridPane messageNode = new GridPane();
        messageNode.setHgap(3);
        ColumnConstraints columnM0 = new ColumnConstraints();
        ColumnConstraints columnM1 = new ColumnConstraints(100);

        columnM0.setHgrow(Priority.ALWAYS); //make the first column grow to fit the rest of the screen

        messageNode.getColumnConstraints().addAll(columnM0,columnM1);

        message = new TextField();
        message.setPromptText("Type a message");
        messageNode.add(message,0,0);

        //send message button: disabled on load
        sendButton = new Button("_Send");
        sendButton.setMnemonicParsing(true);
        sendButton.setPrefWidth(100);
        sendButton.setDisable(true);
        sendButton.setOnAction(handler);
        messageNode.add(sendButton,1,0);
        BorderedTitlePane message = new BorderedTitlePane("MESSAGE","left", Color.BLACK, messageNode);

        //chat display section
        GridPane displayNode = new GridPane();
        display = new TextArea();
        display.setEditable(false);
        displayNode.getChildren().add(display);
        BorderedTitlePane display = new BorderedTitlePane("CHAT DISPLAY", "center", Color.BLUE, displayNode);
        //a scene is created and returned at the end of the method
        //adds handler of type Controller to all buttons

        vbox.getChildren().addAll(connection, message, display);
        return clientScene;
    }

    /**
     Controller for the ClientChatUI
     @author Jamie Harnum
     @version 1
     @see EventHandler
     @since 1.8.0_171
     */
    private class Controller implements EventHandler<ActionEvent> {

        /**
         Handles events passed by the EventHandler
         @param event - the event to be handled
         */
        @Override
        public void handle(ActionEvent event) {
            boolean connected = false;

            if(event.getSource()== connectButton){
                String host;
                int port;

                host = hostInput.getText();
                try {
                    String portVal = comboBox.getValue().toString();
                    port = Integer.parseInt(portVal);
                } catch (NullPointerException e){ //if there's nothing in the combo box, go with default port
                    e.printStackTrace();
                    port = 65535;
                }

                connected = connect(host, port);

                if(connected){
                    connectButton.setDisable(true);
                    connectButton.setStyle("-fx-base: blue;");
                    sendButton.setDisable(false);
                    message.requestFocus();

                    Runnable run = new ChatRunnable<>(ClientChatUI.this,connection);
                    Thread thread = new Thread(run);
                    thread.start();
                } else {
                    return;
                }
            }

            if(event.getSource()==sendButton){
                send();
            }
        }

        boolean connect(String host, int port){

            try {
                Socket trySocket = new Socket();
                trySocket.connect(new InetSocketAddress(host, port), 10000);

                if(trySocket.isConnected()){
                    socket = trySocket;
                    if(socket.getSoLinger()!=-1) socket.setSoLinger(true,5);
                    if(!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

                    display.appendText(socket.toString() + ChatProtocolConstants.LINE_TERMINATOR);

                    connection = new ConnectionWrapper(socket);
                    connection.createStreams();
                    outputStream = connection.getOutputStream();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                display.appendText(e.getMessage() + ChatProtocolConstants.LINE_TERMINATOR);
                return false;
            }
        }

        private void send(){
            String sendMessage = message.getText();
            display.appendText(sendMessage + ChatProtocolConstants.LINE_TERMINATOR);
            try {
                outputStream = connection.getOutputStream();
                outputStream.writeObject(ChatProtocolConstants.DISPLACEMENT + sendMessage + ChatProtocolConstants.LINE_TERMINATOR);
                outputStream.flush();
            } catch (Exception e){
                enableConnectButton();
                display.appendText(e.getMessage() + ChatProtocolConstants.LINE_TERMINATOR);
            }

            //clear the text after sending
            message.setText("");
            message.requestFocus();
        }
    }

    /**
     Factory method to construct panes with borders and inline title labels
     @author Jamie Harnum
     @version 1
     @see StackPane
     @since 1.8.0_171
     */
    private class BorderedTitlePane extends StackPane {

        /**
         Inserts a pane into another pane with a colored title border based on the given parameters
         @param title - The title to display at the top of the border
         @param position - The alignment of the border //TODO change this to Pos. and modify the calls accordingly
         @param color - The Color to give the border
         @param innerContent - The Node to be surrounded by a colored border
         */
        BorderedTitlePane(String title, String position, Color color, Node innerContent){
            super.setPadding(new Insets(5));

            Label titleT = new Label(" " + title + " ");
            titleT.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            titleT.setStyle("-fx-background-color: #F1F1F1;");

           if (position.equalsIgnoreCase("center")|| position.equalsIgnoreCase("centre")){
               //defense against canadianisms with two potential spellings
               super.setAlignment(titleT, Pos.TOP_CENTER);

           } else { //assuming all non-centered titles will be left aligned
               super.setAlignment(titleT, Pos.TOP_LEFT);
               titleT.setTranslateX(15);
           }


            StackPane content = new StackPane();
            content.getChildren().add(innerContent);
            content.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(10))));
            content.setPadding(new Insets(10));

            super.getChildren().add(content);
            super.getChildren().add(titleT);

        }

    }
}

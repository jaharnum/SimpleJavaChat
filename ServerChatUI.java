import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
/*
File name: ServerChatUI.java
Author: Jamie Harnum #040898399
Course: CST8221 - JAP, Lab Section 303
Assignment: 2
Date: April 8
Professor: Daniel Cormier
Purpose: Provides the Server with a UI application
Class list: ClientChatUI, Controller
*/


/**
 Constructs the UI for the Server Chat
 @author Jamie Harnum
 @version 1
 @see package or class
 @since 1.8.0_171
 */
public class ServerChatUI extends Application implements Accessible{

    TextField message;
    Button sendButton;
    TextArea display;
    ObjectOutputStream outputStream;
    Socket socket;
    ConnectionWrapper connection;
    Stage primaryStage;
    String title;

    /*Server spec says to have a constructor with socket and title*/
    public ServerChatUI(Socket in, String title){
        socket = in;
        this.title = title;
    }

    /**
     Starts the Server Chat UI
     @param primaryStage - the Stage to assign the application to
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(title);
        Scene clientScene = createScene();

        //non-resizable screen that is centered
        primaryStage.setHeight(500);
        primaryStage.setWidth(588);
        primaryStage.centerOnScreen();
        primaryStage.setScene(clientScene);
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest((WindowEvent e)-> {
            System.out.print("Server UI Closed!");

            try {//TODO is this where we should close the connection?
                outputStream.writeChars(ChatProtocolConstants.DISPLACEMENT + ChatProtocolConstants.CHAT_TERMINATOR + ChatProtocolConstants.LINE_TERMINATOR);
                outputStream.flush();
            } catch (NullPointerException n){
                return;
            } catch(Exception ex){
                ex.printStackTrace();
            }
            });
        primaryStage.show();

        runClient();

    }

    public TextArea getDisplay(){
        return display;
    }

    public void closeChat() {
        try {
            connection.closeConnection();
        } catch (SocketException sock){
            System.out.print("Socket Closed" + ChatProtocolConstants.LINE_TERMINATOR);
        }
        catch (Exception e){
            e.printStackTrace();
        } finally {
            Platform.runLater(()->primaryStage.close());
        }
    }

    void runClient(){

        if(socket!=null) {
            connection = new ConnectionWrapper(socket);

            try {
                connection.createStreams();

                Runnable run = new ChatRunnable<>(this, connection);
                Thread thread = new Thread(run);
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Socket not connected");
        }
    }

    /**
     * Creates the two main panes for the Stage
     */
    public Scene createScene() {
        GridPane main = new GridPane();
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS); //make bottom row grow to fit the rest of the screen
        main.getRowConstraints().setAll(row1,row2);

        Scene clientScene = new Scene(main);
        Controller handler = new Controller();

        //message input section
        GridPane messageNode = new GridPane();
        messageNode.setHgap(3);
        ColumnConstraints columnM0 = new ColumnConstraints();
        ColumnConstraints columnM1 = new ColumnConstraints(100);

        columnM0.setHgrow(Priority.ALWAYS); //make the first column grow to fit the rest of the screen

        messageNode.getColumnConstraints().addAll(columnM0, columnM1);

        message = new TextField();
        message.setPromptText("Type a message");
        message.positionCaret(0);
        messageNode.add(message, 0, 0);

        //send message button
        sendButton = new Button("_Send");
        sendButton.setMnemonicParsing(true);
        sendButton.setPrefWidth(100);
        sendButton.setOnAction(handler);
        messageNode.add(sendButton, 1, 0);

        //add messageNode to a BorderedTitlePane
        BorderedTitlePane message = new BorderedTitlePane("MESSAGE", "left", Color.BLACK, messageNode);
        //then add to main Pane
        main.add(message,0,0);

        //chat display section
        GridPane displayNode = new GridPane();
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS); //fill the whole panel
        displayNode.getRowConstraints().add(row);

        display = new TextArea();
        display.setEditable(false);
        displayNode.add(display,0,0);

        //add displayNode to a BorderedTitlePane
        BorderedTitlePane display = new BorderedTitlePane("CHAT DISPLAY", "center", Color.BLUE, displayNode);
        //then add to main Pane
        main.add(display,0,1);

        return clientScene;
    }

    /**
     * Controller for the ServerChatUI
     *
     * @author Jamie Harnum
     * @version 1
     * @see EventHandler
     * @since 1.8.0_171
     */
    private class Controller implements EventHandler<ActionEvent> {

        /**
         * Handles events passed by the EventHandler
         *
         * @param event - the event to be handled
         */
        @Override
        public void handle(ActionEvent event) {
            if(event.getSource()==sendButton){
                send();
            }
        }

        private void send(){
            String sendMessage = message.getText();
            display.appendText(sendMessage + ChatProtocolConstants.LINE_TERMINATOR);
            try {
                outputStream = connection.getOutputStream();
                outputStream.writeObject(ChatProtocolConstants.DISPLACEMENT + sendMessage + ChatProtocolConstants.LINE_TERMINATOR);
                outputStream.flush();
            } catch (IOException e){
                display.appendText(e.getMessage() + ChatProtocolConstants.LINE_TERMINATOR);
            }

            //clear the text after sending
            message.setText("");
            message.requestFocus();
        }
    }

    /**
     * Factory method to construct panes with borders and inline title labels
     *
     * @author Jamie Harnum
     * @version 1
     * @see StackPane
     * @since 1.8.0_171
     */
    private class BorderedTitlePane extends StackPane {

        /**
         * Inserts a pane into another pane with a colored title border based on the given parameters
         *
         * @param title        - The title to display at the top of the border
         * @param position     - The alignment of the border //TODO change this to Pos. and modify the calls accordingly
         * @param color        - The Color to give the border
         * @param innerContent - The Node to be surrounded by a colored border
         */
        BorderedTitlePane(String title, String position, Color color, Node innerContent) {
            super.setPadding(new Insets(5));

            Label titleT = new Label(" " + title + " ");
            titleT.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            titleT.setStyle("-fx-background-color: #F1F1F1;");

            if (position.equalsIgnoreCase("center") || position.equalsIgnoreCase("centre")) {
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
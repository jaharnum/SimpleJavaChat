import javafx.application.Application;
import javafx.scene.control.TextArea;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/*
File name:
Author: Jamie Harnum #040898399
Course: CST8221 - JAP, Lab Section 303
Assignment: 1
Date:
Professor: (lab prof)
Purpose:
Class list: (if more than one in the file)
*/

/**
 Brief description of the purpose of the class
 @author Jamie Harnum
 @version 1
 @see package or class
 @since 1.8.0_171
 */
public class ChatRunnable<T extends Application & Accessible> implements Runnable {

    final T ui;
    final Socket socket;
    final ObjectInputStream inputStream;
    final ObjectOutputStream outputStream;
    final TextArea display;

    /** Constructor initializes the main variables
     * @param ui User Interface for this Chat
     * @param connection The socket connection for this Chat*/
    ChatRunnable(T ui, ConnectionWrapper connection){
        socket = connection.getSocket();
        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        this.ui = ui;
        display = ui.getDisplay();
    }

    /**
     The main logic for recieving messages between the Client/Server pairs
     */
    @Override
    public void run() {
        String strin = "";

        while(true){
            try {
                //check if the socket is closed before trying to get an object from the input stream
                if(!socket.isClosed()){
                    try{
                        strin = (String)inputStream.readObject();
                        strin = strin.trim();
                    } catch (SocketException sock){ //this exception is thrown whenever one window is closed
                        break;
                    }
                } else { break; }

                //get the local time and format it for chat output
                LocalDateTime time = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, kk:mm a");
                String timeText = time.format(formatter);

                if(strin.equals(ChatProtocolConstants.CHAT_TERMINATOR)){
                    final String terminate;
                    terminate = ChatProtocolConstants.DISPLACEMENT + timeText + ChatProtocolConstants.LINE_TERMINATOR + strin;
                    display.appendText(terminate);
                    break;
                } else {
                    final String append;
                    append = ChatProtocolConstants.DISPLACEMENT + timeText + ChatProtocolConstants.LINE_TERMINATOR + ChatProtocolConstants.DISPLACEMENT + strin + ChatProtocolConstants.LINE_TERMINATOR;
                    display.appendText(append);
                }

            } catch (IOException e){
                break;
            } catch (NullPointerException n){ //will be thrown whenever one window is closed while the other is still connected
                break;
            } catch (ClassNotFoundException c){
                break;
            } catch (Exception other){ //unexpected exceptions are printed before breaking the loop
                other.printStackTrace();
                break;
            }
        }

        //if a socket is closed, output a terminating statement to the other chat window
        if(!socket.isClosed()){
            try {
                outputStream.writeChars(ChatProtocolConstants.DISPLACEMENT +
                        ChatProtocolConstants.CHAT_TERMINATOR +
                        ChatProtocolConstants.LINE_TERMINATOR);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        ui.closeChat();
    }
}

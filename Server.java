import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
File name: Server.java
Author: Jamie Harnum #040898399
Course: CST8221 - JAP, Lab Section 303
Assignment: 2
Date: April 8
Professor: Daniel Cormier
Purpose: Currently: Launches the Server UI
*/


/**
 Launches the Server UI
 @author Jamie Harnum
 @version 1
 @since 1.8.0_171
 */
public class Server {

    /**
     Main method, identifies if a port has been specified and connects to a new ServerSocket
     */
    public static void main(String []args){
        int port;
        Socket socket;
        int friend = 0;

        if(args.length > 0){
            port = Integer.parseInt(args[0]);
            if(port < 0 || port > 65535){
                port = 65535;
                System.out.print("Unacceptable port number\n. Using default port 65535" + ChatProtocolConstants.LINE_TERMINATOR);
            }
        } else {
            port = 65535;
            System.out.print("Using default port 65535" + ChatProtocolConstants.LINE_TERMINATOR);
        }

        try {
            ServerSocket servSocket = new ServerSocket(port);
            Platform.setImplicitExit(false);

            while(true){
                socket = servSocket.accept();

                if(socket.getSoLinger()!=-1) socket.setSoLinger(true,5);
                if(!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

                System.out.println(socket.toString());
                ++friend;

                final String title = "Jamie's Friend " + friend;

                launchClient(socket,title);
            }


        } catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     Brief description of the method's purpose
     @param in Socket to connect to
     @param title the title to use for the ServerChatUI
     */
    private static void launchClient(Socket in, String title){
           //original spec starts the try here, but my IDE put up an error on start() if it wasn't immediately surrounding it
            new JFXPanel();
            Platform.runLater(() ->
            { try {
                new ServerChatUI(in, title).start(new Stage());
            } catch (Exception e){
                e.printStackTrace();
            }
            });
    }

}

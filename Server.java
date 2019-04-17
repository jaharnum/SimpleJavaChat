import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static javafx.application.Application.launch;

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


    public static void main(String []args){
        int port;
        Socket socket = null;
        int friend = 0;

        if(args.length > 0){
            port = Integer.parseInt(args[0]);
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

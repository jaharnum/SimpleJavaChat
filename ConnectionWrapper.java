import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
public class ConnectionWrapper {

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    /**
     Constructor assigns the socket passed in to this connectionWrapper
     */
    ConnectionWrapper(Socket socket){
        this.socket = socket;
    }

    /**
     Getter method for the socket
     @return The socket for this connection
     */
    public Socket getSocket(){
        return socket;
    }

    /**
     Getter method for the output stream
     @return The output stream for this connection
     */
    public ObjectOutputStream getOutputStream(){
        return outputStream;
    }

    /**
     Getter method for input stream
     @return The input stream for this connection
     */
    public ObjectInputStream getInputStream(){
        return inputStream;
    }

    /**
     Creates an object input stream for this connection
     @return the input stream
     */
    private ObjectInputStream createObjectIStreams() throws IOException {
        inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream;
    }

    /**
     Creates an object output stream for this connection
     @return the output stream
     */
    private ObjectOutputStream createObjectOStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        return outputStream;
    }

    /**
     Creates output and input streams
     @throws IOException if the object streams cannot be created
     */
    public void createStreams() throws IOException {
        outputStream = createObjectOStreams();
        outputStream.flush();
        inputStream = createObjectIStreams();
    }

    /**
     Closes the connection and the input/output streams for this ConnectionWrapper and prints a confirmation
     @throws IOException if the streams are not closed correctly
     */
    public void closeConnection() throws IOException {
        try {
            if(!socket.isClosed()){
                if(inputStream!=null){
                    inputStream.close();
                }
                if(outputStream!=null){
                    outputStream.close();
                }
                socket.close();
            }
        } catch (SocketException sock){
            System.out.print("Socket closed" + ChatProtocolConstants.LINE_TERMINATOR);
        }
    }
}

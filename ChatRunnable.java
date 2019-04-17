import javafx.application.Application;
import javafx.scene.control.TextArea;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Override
    public void run() {
        String strin = "";

        while(true){
            try {

                if(!socket.isClosed()){
                    try{
                        strin = (String)inputStream.readObject();
                        strin = strin.trim();
                    } catch (SocketException sock){
                        break;
                    }
                } else { break; }

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
                    append = ChatProtocolConstants.DISPLACEMENT + timeText + ChatProtocolConstants.LINE_TERMINATOR + strin + ChatProtocolConstants.LINE_TERMINATOR;
                    display.appendText(append);
                }

            } catch (IOException e){
                break;
            } catch (NullPointerException n){
                break;
            } catch (ClassNotFoundException c){
                break;
            } catch (Exception other){
                other.printStackTrace();
                break;
            }
        }

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

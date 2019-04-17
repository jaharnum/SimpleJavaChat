import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionWrapper {

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    ConnectionWrapper(Socket socket){
        this.socket = socket;
    }

    public Socket getSocket(){
        return socket;
    }

    public ObjectOutputStream getOutputStream(){
        return outputStream;
    }

    public ObjectInputStream getInputStream(){
        return inputStream;
    }

    private ObjectInputStream createObjectIStreams() throws IOException {
        inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream;
    }

    private ObjectOutputStream createObjectOStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        return outputStream;
    }

    public void createStreams() throws IOException {
        outputStream = createObjectOStreams();
        outputStream.flush();
        inputStream = createObjectIStreams();
    }

    public void closeConnection() throws IOException {
        if(!socket.isClosed()){
            if(inputStream!=null){
                inputStream.close();
            }
            if(outputStream!=null){
                outputStream.close();
            }
            socket.close();
        }
    }
}

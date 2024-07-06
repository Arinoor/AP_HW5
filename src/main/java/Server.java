import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.LinkPermission;
import java.util.HashMap;

public class Server {

    final static int port = 8090;
    static ObjectInputStream input;
    static ObjectOutputStream output;
    static HashMap<User, Boolean> loginUsers;

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            Server.getQuery();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getQuery() {
        try {
            String command = (String) input.readObject();
            switch (command) {
                case "Logout" :
                    Server.logoutQuery();
                case "Login" :
                    Server.loginQuery();
                case "Download" :
                    Server.downlaodQuery();
                case "Upload" :
                    Server.uploadQuery();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void uploadQuery() {
    }

    private static void downlaodQuery() {
    }

    private static void loginQuery() {
    }

    private static void logoutQuery() {
    }

    public static boolean isUserLogin(User user) {
        return loginUsers.get(user);
    }

    public static int getPort() {
        return port;
    }
}

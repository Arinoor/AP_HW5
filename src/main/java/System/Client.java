package System;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String HOST;
    private final int PORT;
    private Socket socket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream serverOutput;
    private User user;

    public Client(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.initializeSocket();
    }
    // TODO: search about how to categorize different methods of class (maybe using static subclass)
    private void initializeSocket() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.serverInput = new ObjectInputStream(socket.getInputStream());
            this.serverOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String authenticate(Authentication requestedAuthentication, String username, String password) throws IOException {
        serverOutput.writeObject(requestedAuthentication);
        serverOutput.writeObject(username);
        serverOutput.writeObject(password);
        String serverRespond = serverInput.readLine();
        if(serverRespond.contains("successful")) {
            setUser(user);
        }
        return serverRespond;
    }

    public String logOut() {
        if(getUser() == null) {
            return "You are not logged in";
        }
        setUser(null);
        return "You are logged out successfully";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}

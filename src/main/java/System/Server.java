package System;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import Exception.*;

public class Server {

    final int port = 8090;
    ServerSocket serverSocket;
    Socket socket;
    Scanner input;
    PrintWriter output;
    DataBaseManager dataBaseManager;

    public Server () {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream());
            dataBaseManager = new DataBaseManager();
            this.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Server();
    }

    public void run() {
        while (socket.isConnected()) {
            this.getQuery();
        }
    }

    private void getQuery() {
        try {
            String command = input.next();
            switch (command) {
                case "Register" :
                    this.RegisterHandler();
                case "Login" :
                    this.LoginHandler();
                case "Logout" :
                    this.LogoutHandler();
            }
        } catch (Exception e) {
            output.write(e.getMessage());
        }
    }

    private void LogoutHandler() throws Exception{
        User user = getUser();
        dataBaseManager.logOutUser(user);
        output.write(String.format("User Logged out successfully with username %s and password %s",
                user.getUsername(), user.getPassword()));
    }

    private void LoginHandler() throws Exception{
        User user = getUser();
        dataBaseManager.logInUser(user);
        output.write(String.format("User Logged in successfully with username %s and password %s",
                    user.getUsername(), user.getPassword()));
    }


    private void RegisterHandler() throws Exception{
        User user = getUser();
        dataBaseManager.registerUser(user);
        output.write(String.format("User registered successfully with username %s and password %s",
                user.getUsername(), user.getPassword()));
    }

    private User getUser() throws InvalidCredentialsException {
        String username, password;
        username = getUsername();
        password = getPassword();
        return new User(username, password);
    }

    private String getPassword() throws InvalidPasswordException {
        output.write("Enter your password");
        String password = input.next();
        checkPassword(password);
        return password;
    }

    private String getUsername() throws InvalidUsernameException {
        output.write("Enter your username");
        String username = input.next();
        checkUsername(username);
        return username;
    }


    private void checkPassword(String password) throws InvalidPasswordException {
    }

    private void checkUsername(String username) throws InvalidUsernameException {

    }
}

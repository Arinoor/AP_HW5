package System;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import Exception.*;

public class Client {
    private final String HOST;
    private final int PORT;
    private Socket socket;
    private Scanner serverInput, userInput;
    private PrintWriter serverOutput;
    private PrintStream userOutput;
    private User user;

    public Client(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.initializeClient();
        this.run();
    }
    // TODO: search about how to categorize different methods of class (maybe using static subclass)
    private void initializeClient() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.serverInput = new Scanner(socket.getInputStream());
            this.serverOutput = new PrintWriter(socket.getOutputStream(), true);
            this.userInput = new Scanner(System.in);
            this.userOutput = System.out;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if(user == null) {
            homePageNotLoggedInPage();
        }
        else {
            homePageLoggedInPage();
        }
    }


    private void homePageLoggedInPage() {
        userOutput.println("Welcome to our file manager. You are logged in as " + getLoggedInUser().getUsername() + "\nChoose one of options below\n[1] logout");
        String query = userInput.next().toLowerCase().trim();
        switch (query) {
            case "1":
            case "logout":
                logOutHandler();
                break;
        }
    }

    private void homePageNotLoggedInPage() {
        userOutput.println("Welcome to Home Page. Log in or Register to proceed\n[1] Login\n[2] Register");
        String query = userInput.next().toLowerCase().trim();
        switch (query) {
            case "1":
            case "login":
                logInHandler();
                break;
            case "2":
            case "register":
                registerHandler();
                break;
            default:
                userOutput.println("Please choose a valid option");
                homePageNotLoggedInPage();
        }

    }

    private void registerHandler() {
        try {
            serverOutput.write("Register");
            User user = getUser();
            sendUserToServer(user);
            String response = serverInput.next();
            userOutput.println(response);
            homePageNotLoggedInPage();
        } catch (Exception e) {
            serverOutput.println(e.getMessage());
        }
    }

    private void logInHandler() {
        try {
            serverOutput.write("Login");
            System.out.println("message is sent");
            User user = getUser();
            sendUserToServer(user);
            String response = serverInput.next();
            userOutput.println(response);
            if (response.contains("logged in")) {
                setLoggedInUser(user);
                homePageLoggedInPage();
            } else {
                homePageNotLoggedInPage();
            }
        } catch (Exception e) {
            serverOutput.println(e.getMessage());
        }
    }

    private void logOutHandler() {
        try {
            serverOutput.write("Logout");
            sendUserToServer(getLoggedInUser());
            String response = serverInput.next();
            if(response.contains("logged out")) {
                setLoggedInUser(null);
            } else {
                throw new ServerException("server was supposed to log out the user");
            }
            userOutput.println(response);
            homePageNotLoggedInPage();
        } catch (Exception e) {
            userOutput.println(e.getMessage());
        }

    }


    private boolean sendUserToServer(User user) throws ServerException{
        String response;
        if ((response = serverInput.next()).equals("Enter your username")) {
            serverOutput.write(user.getPassword());
        } else {
            throw new ServerException("Server was supposed to ask for username");
        }
        if ((response = serverInput.next()).equals("Enter your password")) {
            serverOutput.write(user.getPassword());
        } else {
            throw new ServerException("Server was supposed to ask for password");
        }
        return true;
    }

    private User getUser() {
        String username = getUsername();
        String password = getPassword();
        User user = new User(username, password);
        return user;
    }

    private String getUsername() {
        userOutput.print("Please enter your username : ");
        String username = userInput.next();
        return username;
    }

    private String getPassword() {
        userOutput.print("Please enter your password : ");
        String password = userInput.next();
        return password;
    }


    public User getLoggedInUser() {
        return user;
    }

    public void setLoggedInUser(User user) {
        this.user = user;
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 8090);
    }


}

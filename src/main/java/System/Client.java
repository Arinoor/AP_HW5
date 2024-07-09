package System;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import Exception.*;

public class Client {
    private final String HOST;
    private final int PORT;
    private Socket socket;
    private BufferedReader serverInput;
    private Scanner userInput;
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
            this.serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
        userOutput.println("Welcome to our file manager. You are logged in as " + getLoggedInUser().getUsername() + "\n" +
                "Choose one of options below\n" +
                "[1] Show files to download\n" +
                "[2] Upload new file\n" +
                "[3] Logout");
        String query = userInput.next().toLowerCase().trim();
        switch (query) {
            case "1":
            case "show files to download":
                //     downloadFilesHandler();
                break;
            case "3":
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
            case "Login":
                logInHandler();
                break;
            case "2":
            case "Register":
                registerHandler();
                break;
            default:
                userOutput.println("Please choose a valid option");
                homePageNotLoggedInPage();
        }

    }

    private void registerHandler() {
        try {
            serverOutput.println("Register");
            User user = getUser();
            sendUserToServer(user);
            String response = serverInput.readLine();
            userOutput.println(response);
            homePageNotLoggedInPage();
        } catch (Exception e) {
            serverOutput.println(e.getMessage());
        }
    }

    private void logInHandler() {
        try {
            serverOutput.println("Login");
            User user = getUser();
            sendUserToServer(user);
            String response = serverInput.readLine();
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
            serverOutput.println("Logout");
            sendUserToServer(getLoggedInUser());
            String response = serverInput.readLine();
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

    private void downloadFilesHandler() throws IOException {
        userOutput.println("Choose one of the names below to download the file\n");
        serverOutput.write("Show files");
        String respond = serverInput.readLine();
        String[] fileNames = respond.split(" ");
        userOutput.println(respond);
        userOutput.print("Enter your file name : ");
        String fileName = userInput.nextLine().trim();


    }

    private boolean sendUserToServer(User user) throws ServerException, IOException {
        String response;
        if ((response = serverInput.readLine()).equals("Enter your username")) {
            serverOutput.println(user.getUsername());
        } else {
            throw new ServerException("Server was supposed to ask for username");
        }
        if ((response = serverInput.readLine()).equals("Enter your password")) {
            serverOutput.println(user.getPassword());
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

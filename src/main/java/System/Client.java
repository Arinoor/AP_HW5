package System;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import Exception.*;
import Config.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.concurrent.CountDownLatch;


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

    private void showFilesPage() throws IOException {
        userOutput.println("Choose one of the names below to download the file\n");
        serverOutput.write("Show files");
        String respond = serverInput.readLine().trim();
        userOutput.println(respond);
        userOutput.print("Enter your file name : ");
        String fileName = userInput.nextLine().trim();
        String[] fileNames = respond.split(" ");
        if(Arrays.asList(fileNames).contains(fileName)) {
            downloadHandler(fileName);
        }
        else {
            userOutput.println("Please choose one of available file names");
            showFilesPage();
        }

    }

    private void downloadHandler(String fileName) {
        try {
            serverOutput.println("Download");
            sendUserToServer(getLoggedInUser());
            sendFileToServer(fileName);
            String respond = serverInput.readLine();
            userOutput.println(respond);
            if(!respond.contains("successful")){
                showFilesPage();
            }
            receiveFileFromServer(fileName);
        } catch (Exception e) {
            userOutput.println(e.getMessage());
        }
    }

    private void receiveFileFromServer(String fileName) throws IOException, CreateFileException, CreateDirectoryException, InterruptedException {
        RandomAccessFile downloadFile = createFile(fileName);
        int fileSize = Integer.parseInt(serverInput.readLine());
        int chunkNumber = fileSize / Config.CHUNK_SIZE;
        CountDownLatch latch = new CountDownLatch(chunkNumber);
        DatagramSocket socket = new DatagramSocket(Config.port);
        for(int i = 0; i < chunkNumber; i++) {
            new Thread(() -> {
                try {
                    receiveChunk(downloadFile, latch, socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            downloadFile.close();
            userOutput.println("File downloaded successfully");
            homePageLoggedInPage();
        }
    }

    public void receiveChunk(RandomAccessFile downloadFile, CountDownLatch latch, DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[Config.CHUNK_SIZE + 4]; // first 4 bytes indicate the chunk number
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        int chunkNumber = getChunkNumber(buffer);
        byte[] chunk = Arrays.copyOfRange(buffer, 4, buffer.length);
        // now we have the chunk number and the chunk itself we are going to write the chunk into download file
        synchronized (downloadFile) {
            downloadFile.seek((long) chunkNumber * Config.CHUNK_SIZE);
            downloadFile.write(chunk);
        }
        latch.countDown();
    }

    private RandomAccessFile createFile(String fileName) throws CreateDirectoryException, CreateFileException, FileNotFoundException {
        File directory = new File(Config.CLIENT_FILE_PATH);
        if(!directory.exists()) {
            boolean wasSuccessful = directory.mkdirs();
            if(!wasSuccessful) {
                throw new CreateDirectoryException("Could not create directory");
            }
        }
        File file = new File(Config.CLIENT_FILE_PATH + fileName);
        return new RandomAccessFile(file, "rw");
    }

    private int getChunkNumber(byte[] buffer) {
        byte[] chunkNumber = Arrays.copyOfRange(buffer, 0, 4);
        return ByteBuffer.wrap(chunkNumber).getInt();
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

    private void sendFileToServer(String fileName) throws ServerException, IOException {
        String response;
        if ((response = serverInput.readLine()).equals("Enter your file name")) {
            serverOutput.println(fileName);
        } else {
            throw new ServerException("Server was supposed to ask for file name");
        }
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

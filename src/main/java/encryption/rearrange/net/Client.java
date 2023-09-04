package encryption.rearrange.net;

import encryption.rearrange.SimpleEncryption;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static encryption.rearrange.GlobalConfiguration.*;

public class Client extends EntityModel {
    private static Client client;

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
            client.setKey(STANDARD_KEY);
            client.start(PORT);
        }
        return client;
    }

    @Override
    public void start(int port) {
        try {
            clientSocket = new Socket(IP, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String sendMessage(String msg) {
        try {
            out.println(msg);
            return in.readLine();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    @Override
    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input the message to the server: ");
        String msg;
        while ((msg = scanner.nextLine()) != null) {
            Client client = getInstance();
            if (msg.contains(SET_KEY)) {
                client.setGivenKeyResponse(msg);
                continue;
            }
            if (msg.contains(RANDOM_KEY)) {
                client.setRandomKeyResponse(msg);
                continue;
            }
            if (STOP_WORD.equals(msg)) {
                client.sendMessage(msg);
                System.out.println("Client shutdown...");
                break;
            }
            String encrypted = encrypt(msg, client.getKey());
            System.out.println("Sending the message to the server...");
            String response = client.sendMessage(encrypted);
            System.out.println("Response from the server: " + response);
        }
        client.stop();
    }

    @Override
    protected void setGivenKeyResponse(String msg) {
        setKeyResponse(msg);
    }

    @Override
    protected void setRandomKeyResponse(String msg) {
        setKeyResponse(msg);
    }

    private void setKeyResponse(String msg) {
        String response = client.sendMessage(msg);
        if (response.contains(ERROR_KEY)) {
            System.out.println(response.replace(ERROR_KEY + " ", ""));
        } else {
            SimpleEncryption key = client.generateKey(getTable(response));
            client.setKey(key);
            System.out.println("Key set.");
        }
    }

    private static String encrypt(String msg, SimpleEncryption key) {
        return SimpleEncryption.Encryptor.encrypt(msg, key);
    }

    private static String[] getTable(String s) {
        return Pattern.compile("\\d+")
                .matcher(s)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
    }
}

package encryption.permutation.net;

import encryption.commons.net.EntityModel;
import encryption.permutation.SimpleEncryption;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static encryption.permutation.GlobalConfiguration.*;

public class Client extends EntityModel<SimpleEncryption> {
    private static Client client;
    // A flag to indicate whether the client has started
    private static boolean started;

    /**
     * Singleton client.
     * @return new client instance if null, otherwise an existing client
     */

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
            client.setKey(STANDARD_KEY);
        }
        return client;
    }

    private Client() {

    }

    @Override
    public void start(int port) {
        try {
            clientSocket = new Socket(IP, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            started = true;
        } catch (Exception e) {
            logError.accept(e.getMessage());
        }
    }

    public String sendMessage(String msg) {
        try {
            out.println(msg);
            return in.readLine();
        } catch (Exception e) {
            logError.accept(e.getMessage());
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
            logError.accept(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        logInfo.accept("Input the message to the server: ");
        String msg;
        while ((msg = scanner.nextLine()) != null) {
            Client client = getInstance();
            client.startOnDemand();
            if (msg.startsWith(EXCLAMATION_MARK)) {
                if (msg.contains(SET_KEY)) {
                    client.setGivenKeyResponse(msg);
                } else if (msg.contains(RANDOM_KEY)) {
                    client.setRandomKeyResponse(msg);
                } else if (STOP_WORD.equals(msg)) {
                    client.sendMessage(msg);
                    logInfo.accept("Client shutdown...");
                    break;
                }
            } else {
                String status = client.sendMessage(META_DATA + prepareMetaData(msg));
                if (SUCCESS.equals(status)) {
                    String encrypted = encrypt(msg, client.getKey());
                    logInfo.accept("Sending the message to the server...");
                    String response = client.sendMessage(encrypted);
                    logInfo.accept("Response from the server: " + response);
                } else if (status.contains(ERROR_KEY)) {
                    logInfo.accept(status.replace(ERROR_KEY + WHITESPACE, ""));
                }
            }
        }
        client.stop();
    }

    public void startOnDemand() {
        if (client != null && !started) {
            client.start(PORT);
        }
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
            logInfo.accept(response.replace(ERROR_KEY + " ", ""));
        } else {
            SimpleEncryption key = client.generateKey(response);
            client.setKey(key);
            logInfo.accept("Key set.");
        }
    }

    public SimpleEncryption generateKey(String msg) {
        return SimpleEncryption.generateKey(this, getTable(msg));
    }

    private static String encrypt(String msg, SimpleEncryption key) {
        return new SimpleEncryption.Encryptor().encrypt(msg, key);
    }

    private static String prepareMetaData(String msg) {
        return SimpleEncryption.Encryptor.prepareMetaData(msg);
    }

    private static String[] getTable(String s) {
        return Pattern.compile("\\d+")
                .matcher(s)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
    }
}

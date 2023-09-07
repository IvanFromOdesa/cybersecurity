package encryption.rearrange.net;

import encryption.rearrange.SimpleEncryption;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static encryption.rearrange.GlobalConfiguration.*;

public class Server extends EntityModel {
    private ServerSocket serverSocket;
    private static Server server;

    public static Server getInstance() {
        if (server == null) {
            server = new Server();
            server.setKey(STANDARD_KEY);
        }
        return server;
    }

    private Server() {

    }

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server has started on port: " + PORT);
            System.out.println("Waiting for client's messages...");
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Message from the client: " + msg);
                if (msg.startsWith(EXCLAMATION_MARK)) {
                    if (AVAILABLE_COMMANDS.stream().noneMatch(msg::contains)) {
                        // Even if the client gets modified, the server won't proceed
                        out.println(ERROR_KEY + " Unrecognized command.");
                    } else if (msg.contains(SET_KEY)) {
                        setGivenKeyResponse(msg);
                    } else if (msg.contains(RANDOM_KEY)) {
                        setRandomKeyResponse(msg);
                    } else if (msg.contains(STOP_WORD)) {
                        System.out.println("Server shutdown...");
                        break;
                    } else if (msg.contains(META_DATA) && key.isNullMetaData()) {
                        key.setMetaData(msg.replace(META_DATA, ""));
                        out.println(SUCCESS);
                    }
                } else {
                    String decryptedMsg = new SimpleEncryption.Decryptor().decrypt(msg, key);
                    System.out.println("Sending decrypted message back...");
                    out.println(decryptedMsg);
                    System.out.println("Sent: " + decryptedMsg);
                }
            }
            stop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void setGivenKeyResponse(String msg) {
        msg = msg.replace(SET_KEY + WHITESPACE, "");
        key = generateKey(msg.split(WHITESPACE));
        if (key == STANDARD_KEY) {
            out.println(ERROR_KEY + " Error while parsing input string. Please, try again.");
        } else {
            System.out.println("Key set.");
            out.println(Arrays.toString(key.sequence().toArray()));
        }
    }

    @Override
    protected void setRandomKeyResponse(String msg) {
        List<String> res;
        Random random = new Random();
        int degree;
        if (msg.matches(RANDOM_KEY + WHITESPACE + "\\d+")) {
            try {
                degree = Integer.parseInt(msg.replace(RANDOM_KEY + WHITESPACE, ""));
            } catch (NumberFormatException e) {
                out.println(ERROR_KEY + " Error while parsing input string. Please, try again.");
                System.err.println(e.getMessage());
                return;
            }
        } else {
            System.out.println("Generating with a random degree...");
            degree = random.nextInt(MIN_DEGREE, MAX_DEGREE + 1);
        }
        res = new LinkedList<>();
        while (res.size() != degree) {
            int idx = random.nextInt(1, degree + 1);
            String o = String.valueOf(idx);
            if (!res.contains(o)) {
                res.add(o);
            }
        }
        key = generateKey(res.toArray(String[]::new));
        if (key == STANDARD_KEY) {
            out.println(ERROR_KEY + " Error while parsing input string. Please, try again.");
        } else {
            System.out.println("Key set.");
            out.println(Arrays.toString(key.sequence().toArray()));
        }
    }

    @Override
    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = getInstance();
        server.start(PORT);
    }
}

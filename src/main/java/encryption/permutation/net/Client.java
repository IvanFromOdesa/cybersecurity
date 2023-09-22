package encryption.permutation.net;

import encryption.commons.log.MessageLogger;
import encryption.commons.net.CryptExchange;
import encryption.commons.net.SocketModel;
import encryption.permutation.PermutationEncryption;
import encryption.permutation.PermutationEncryptionHelper;
import encryption.permutation.PermutationRqData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

import static encryption.permutation.Configuration.*;

public class Client extends SocketModel<PermutationEncryption, PermutationRqData> implements CryptExchange<PermutationEncryption, PermutationRqData> {
    private static Client client;
    // A flag to indicate whether the client has started
    private static boolean started;
    private static boolean needsKey;

    /**
     * Singleton client.
     * @return new client instance if null, otherwise an existing client
     */

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
            Optional<PermutationEncryption> key = client.encryptionHelper.getDefault(PROPERTIES);
            if (key.isEmpty()) {
                client.logger.warning("Error reading key. Will generate / get a random key when needed.");
                needsKey = true;
            } else {
                client.setKey(key.get());
                client.logger.info("Default key set.");
            }
        }
        return client;
    }

    @Override
    public SocketModel<PermutationEncryption, PermutationRqData> getEntity() {
        return getInstance();
    }

    private Client() {
        // Only needs encryptor
        encryptor = new PermutationEncryption.Encryptor();
        encryptionHelper = new PermutationEncryptionHelper();
    }

    @Override
    public void start(int port) {
        try {
            clientSocket = new Socket(PROPERTIES.get("ip.address"), port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            started = true;
        } catch (Exception e) {
            client.logger.severe(e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            client.logger.severe(e.getMessage());
        }
    }

    public static void main(String[] args) {
        getUserInputRecursively();
        client.stop();
    }

    private static void getUserInputRecursively() {
        Scanner scanner = new Scanner(System.in);
        MessageLogger.info("Input the message to the server: ");
        String msg;
        while ((msg = scanner.nextLine()) != null) {
            Client client = getInstance();
            client.startOnDemand();
            if (msg.startsWith(EXCLAMATION_MARK)) {
                if (msg.contains(SET_KEY)) {
                    client.setGivenKeyResponse(new PermutationRqData(msg));
                } else if (msg.contains(RANDOM_KEY)) {
                    client.setRandomKeyResponse(new PermutationRqData(msg));
                } else if (STOP_WORD.equals(msg)) {
                    client.sendMessageTwoWay(msg);
                    client.logger.info("Client shutdown...");
                    break;
                }
            } else {
                String status = client.sendMessageTwoWay(META_DATA + PermutationEncryption.Encryptor.prepareMetaData(msg));
                if (SUCCESS.equals(status)) {
                    String encrypted = client.encryptor.encrypt(msg, client.getKey());
                    client.logger.info("Sending the message to the server...");
                    String response = client.sendMessageTwoWay(encrypted);
                    client.logger.info("Response from the server: " + response);
                } else if (status.contains(ERROR_KEY)) {
                    client.logger.severe(status.replace(ERROR_KEY + WHITESPACE, ""));
                }
            }
        }
    }

    public void startOnDemand() {
        if (client != null && !started) {
            client.startIfValidPort();
            if (needsKey) {
                setRandomKeyResponse(new PermutationRqData(RANDOM_KEY));
                needsKey = false;
            }
        }
    }

    @Override
    public void setGivenKeyResponse(PermutationRqData rqData) {
        setKeyResponse(rqData.getToSend());
    }

    @Override
    public void setRandomKeyResponse(PermutationRqData rqData) {
        setKeyResponse(rqData.getToSend());
    }

    private void setKeyResponse(String msg) {
        String response = client.sendMessageTwoWay(msg);
        if (response.contains(ERROR_KEY)) {
            logger.severe(response.replace(ERROR_KEY + " ", ""));
        } else if (SUCCESS.equals(response)) {
            sendMessageOneWay("OK");
            PermutationEncryption newKey = deserializeKey();
            if (newKey == key) {
                // Should not get here
                logger.warning("Cannot read transmitted key.");
                client.stop();
            } else {
                client.setKey(newKey);
                logger.info("Key set: " + newKey.sequence());
            }
        }
    }
}

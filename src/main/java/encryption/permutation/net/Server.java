package encryption.permutation.net;

import encryption.commons.net.CryptExchange;
import encryption.commons.net.SocketModel;
import encryption.permutation.PermutationEncryption;
import encryption.permutation.PermutationEncryptionHelper;
import encryption.permutation.PermutationRqData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.function.Supplier;

import static encryption.permutation.Configuration.*;

public class Server extends SocketModel<PermutationEncryption, PermutationRqData> implements CryptExchange<PermutationEncryption, PermutationRqData> {
    private ServerSocket serverSocket;
    private static Server server;

    public static Server getInstance() {
        if (server == null) {
            server = new Server();
            Optional<PermutationEncryption> key = server.encryptionHelper.getDefault(PROPERTIES);
            if (key.isPresent()) {
                server.setKey(key.get());
                server.logger.info("Default key set.");
            } else {
                server.logger.warning("Error reading key. Will generate / get a random key when needed.");
            }
        }
        return server;
    }

    @Override
    public SocketModel<PermutationEncryption, PermutationRqData> getEntity() {
        return getInstance();
    }

    private Server() {
        decryptor = new PermutationEncryption.Decryptor();
        encryptionHelper = new PermutationEncryptionHelper();
    }

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server has started on port: " + PROPERTIES.get("port"));
            logger.info("Waiting for client's messages...");
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String msg;
            while ((msg = in.readLine()) != null) {
                logger.info("Message from the client: " + msg);
                if (msg.startsWith(EXCLAMATION_MARK)) {
                    if (AVAILABLE_COMMANDS.stream().noneMatch(msg::contains)) {
                        // Even if the client gets modified, the server won't proceed
                        sendMessageOneWay(ERROR_KEY + " Unrecognized command.");
                    } else if (msg.contains(SET_KEY)) {
                        setGivenKeyResponse(new PermutationRqData(msg));
                    } else if (msg.contains(RANDOM_KEY)) {
                        setRandomKeyResponse(new PermutationRqData(msg));
                    } else if (msg.contains(STOP_WORD)) {
                        logger.info("Server shutdown...");
                        break;
                    } else if (msg.contains(META_DATA) && key.isNullMetaData()) {
                        key.setMetaData(msg.replace(META_DATA, ""));
                        sendMessageOneWay(SUCCESS);
                    }
                } else {
                    String decryptedMsg = decryptor.decrypt(msg, key);
                    logger.info("Sending decrypted message back...");
                    sendMessageOneWay(decryptedMsg);
                    logger.info("Sent: " + decryptedMsg);
                }
            }
            stop();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void setGivenKeyResponse(PermutationRqData rqData) {
        rqData.setToSend(rqData.getToSend().replace(SET_KEY + WHITESPACE, ""));
        setKeyResponse(() -> encryptionHelper.generateKey(rqData).orElseGet(this::getKey));
    }

    @Override
    public void setRandomKeyResponse(PermutationRqData rqData) {
        setKeyResponse(() -> encryptionHelper.getRandomKey(rqData).orElseGet(this::getKey));
    }

    private void setKeyResponse(Supplier<PermutationEncryption> supplier) {
        PermutationEncryption newKey = supplier.get();
        if (key == newKey) {
            sendMessageOneWay(ERROR_KEY + " Error while parsing input string. Please, try again.");
        } else {
            key = newKey;
            logger.info("Key set.");
            if (!sendMessageTwoWay(SUCCESS).isEmpty()) {
                transmitKey();
            }
        }
    }

    private void transmitKey() {
        if (!serializeKey(key)) {
            sendMessageOneWay(ERROR_KEY + " Error transmitting a key.");
        } else {
            logger.info("Key sent.");
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
            logger.severe(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = getInstance();
        server.startIfValidPort();
    }
}

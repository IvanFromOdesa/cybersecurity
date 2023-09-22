package encryption.commons.net;

import encryption.commons.PropertiesLoader;
import encryption.commons.crypt.*;
import encryption.commons.log.LoggerFactory;
import encryption.commons.log.MessageLogger;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public abstract class SocketModel<KEY extends BaseEncryption, RD_HOLDER extends KeyRqData<?>> {
    protected KEY key;
    protected IEncryptor<KEY> encryptor;
    protected IDecryptor<KEY> decryptor;
    protected EncryptionHelper<KEY, RD_HOLDER> encryptionHelper;
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected final LoggerFactory logger;
    protected static final Map<String, String> PROPERTIES = PropertiesLoader.readProperties();

    public SocketModel() {
        logger = LoggerFactory.of(getClass().getName());
    }

    public abstract void stop();

    public abstract void start(int port);

    public String sendMessageTwoWay(String msg) {
        try {
            out.println(msg);
            return in.readLine();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        return "";
    }

    /**
     * No wait for the response. Non-blocking.
     * @param msg
     */
    public void sendMessageOneWay(String msg) {
        out.println(msg);
    }

    public KEY getKey() {
        return key;
    }

    public void setKey(KEY key) {
        this.key = key;
    }

    public LoggerFactory getLogger() {
        return logger;
    }

    protected void startIfValidPort() {
        String port = PROPERTIES.get("port");
        if (port != null && port.matches("\\d+")) {
            start(Integer.parseInt(port));
        } else {
            MessageLogger.error(new RuntimeException("Error reading port: " + port));
        }
    }
}

package encryption.commons.net;

import encryption.commons.crypt.BaseEncryption;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class EntityModel<KEY extends BaseEncryption> {
    protected KEY key;
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;

    public abstract void stop();

    public abstract void start(int port);

    /**
     * A command that is used to generate a key from the client input.
     * @param msg
     */
    protected abstract void setGivenKeyResponse(String msg);

    /**
     * A command that is used to generate a random key.
     * @param msg
     */
    protected abstract void setRandomKeyResponse(String msg);

    public KEY getKey() {
        return key;
    }

    public void setKey(KEY key) {
        this.key = key;
    }
}

package encryption.rearrange.net;

import encryption.rearrange.SimpleEncryption;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashSet;

import static encryption.rearrange.GlobalConfiguration.STANDARD_KEY;

public abstract class EntityModel {
    protected SimpleEncryption key;
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;

    protected SimpleEncryption generateKey(String[] table) {
        Integer[] order;
        try {
            int[] res = Arrays.stream(table).mapToInt(Integer::parseInt).toArray();
            order = Arrays.stream(res).boxed().toArray(Integer[]::new);
        } catch (NumberFormatException e) {
            System.out.println("Error generating a key.");
            return STANDARD_KEY;
        }
        System.out.println("Key generated successfully: " + Arrays.toString(order));
        return new SimpleEncryption((short) order.length, new LinkedHashSet<>(Arrays.asList(order)));
    }

    public abstract void stop();

    public abstract void start(int port);

    protected abstract void setGivenKeyResponse(String msg);

    protected abstract void setRandomKeyResponse(String msg);

    public SimpleEncryption getKey() {
        return key;
    }

    public void setKey(SimpleEncryption key) {
        this.key = key;
    }
}

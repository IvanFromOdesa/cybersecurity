import encryption.permutation.SimpleEncryption;
import encryption.permutation.net.Client;
import encryption.permutation.net.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static encryption.permutation.GlobalConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimplePermutationTests {
    private static Thread serverThread;
    private static Server server;
    private static Client client;
    private static final String INIT_MSG = "A sample text to encrypt.";

    @BeforeAll
    static void init() {
        server = Server.getInstance();
        serverThread = new Thread(() -> server.start(PORT));
        serverThread.start();
        // Wait until the server has started
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client = Client.getInstance();
        client.startOnDemand();
    }

    @AfterAll
    static void tearDown() {
        serverThread.interrupt();
        client.stop();
    }

    @Test
    void shouldCommunicateSuccess() {
        assertSentEqualsResponse(client);
    }

    @Test
    void shouldCommunicateSuccessSetKey() {
        String response = client.sendMessage(SET_KEY + WHITESPACE + "3 4 1 2 6 5");
        SimpleEncryption key = client.generateKey(response);
        client.setKey(key);
        assertEquals(server.getKey(), client.getKey());
        assertSentEqualsResponse(client);
    }

    @Test
    void shouldCommunicateSuccessSetRKey() {
        String response = client.sendMessage(RANDOM_KEY);
        SimpleEncryption key = client.generateKey(response);
        client.setKey(key);
        assertEquals(server.getKey(), client.getKey());
        assertSentEqualsResponse(client);
    }

    @Test
    void shouldCommunicateSuccessSetRKeyWithDegree() {
        short degree = 7;
        String response = client.sendMessage(RANDOM_KEY + WHITESPACE + degree);
        SimpleEncryption key = client.generateKey(response);
        assertEquals(degree, key.degree());
        assertEquals(server.getKey(), key);
    }

    @Test
    void shouldCommunicateFailSetInvalidKey() {
        String response = client.sendMessage(SET_KEY + "abc");
        assertTrue(response.contains(ERROR_KEY));
    }

    private void assertSentEqualsResponse(Client client) {
        String status = client.sendMessage(META_DATA + SimpleEncryption.Encryptor.prepareMetaData(INIT_MSG));
        assertEquals(SUCCESS, status);
        String encrypted = new SimpleEncryption.Encryptor().encrypt(INIT_MSG, client.getKey());
        String r = client.sendMessage(encrypted);
        assertEquals(INIT_MSG, r);
    }
}

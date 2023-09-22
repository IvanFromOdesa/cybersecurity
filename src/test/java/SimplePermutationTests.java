import encryption.permutation.PermutationEncryption;
import encryption.permutation.net.Client;
import encryption.permutation.net.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static encryption.permutation.Configuration.*;
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
        serverThread = new Thread(() -> server.start(8080));
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
        assertKeyEquals(SET_KEY + WHITESPACE + "3 4 1 2 6 5");
        assertSentEqualsResponse(client);
    }

    @Test
    void shouldCommunicateSuccessSetRKey() {
        assertKeyEquals(RANDOM_KEY);
        assertSentEqualsResponse(client);
    }

    @Test
    void shouldCommunicateSuccessSetRKeyWithDegree() {
        short degree = 7;
        PermutationEncryption key = assertKeyEquals(RANDOM_KEY + WHITESPACE + degree);
        assertEquals(degree, key.degree());
    }

    @Test
    void shouldCommunicateFailSetInvalidKey() {
        String response = client.sendMessageTwoWay(SET_KEY + "abc");
        assertTrue(response.contains(ERROR_KEY));
    }

    private PermutationEncryption assertKeyEquals(String msg) {
        String response = client.sendMessageTwoWay(msg);
        assertEquals(SUCCESS, response);
        client.sendMessageOneWay("OK");
        PermutationEncryption key = client.deserializeKey();
        client.setKey(key);
        assertEquals(server.getKey(), client.getKey());
        return key;
    }

    private void assertSentEqualsResponse(Client client) {
        String status = client.sendMessageTwoWay(META_DATA + PermutationEncryption.Encryptor.prepareMetaData(INIT_MSG));
        assertEquals(SUCCESS, status);
        String encrypted = new PermutationEncryption.Encryptor().encrypt(INIT_MSG, client.getKey());
        String r = client.sendMessageTwoWay(encrypted);
        assertEquals(INIT_MSG, r);
    }
}

package encryption.commons.net;

import encryption.commons.crypt.BaseEncryption;
import encryption.commons.crypt.KeyRqData;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * This is separate as not all the client-servers should have this functionality
 * @apiNote RD_HOLDER is the wrapper, not the actual type of msg
 */
public interface CryptExchange <KEY extends BaseEncryption, RD_HOLDER extends KeyRqData<?>> {
    /**
     * A command that is used to generate a key from the client input.
     * @param rqData
     */
    void setGivenKeyResponse(RD_HOLDER rqData);

    /**
     * A command that is used to generate a random key.
     * @param rqData
     */
    void setRandomKeyResponse(RD_HOLDER rqData);

    default boolean serializeKey(KEY key) {
        try {
            OutputStream outputStream = getEntity().clientSocket.getOutputStream();
            new ObjectOutputStream(outputStream).writeObject(key);
            return true;
        } catch (Exception e) {
            getEntity().logger.severe(e.getMessage());
        }
        return false;
    }

    /**
     * Deserializes transmitted key. Should be used only for the key serialization / deserialization.
     * @return
     */
    @SuppressWarnings("unchecked")
    default KEY deserializeKey() {
        try {
            InputStream inputStream = getEntity().clientSocket.getInputStream();
            return (KEY) new ObjectInputStream(inputStream).readObject();
        } catch (Exception e) {
            getEntity().logger.severe(e.getMessage());
            return getEntity().key;
        }
    }

    SocketModel<KEY, RD_HOLDER> getEntity();
}

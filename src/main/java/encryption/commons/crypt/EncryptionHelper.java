package encryption.commons.crypt;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for helper methods with da key.
 * @param <KEY>
 */
public interface EncryptionHelper<KEY extends BaseEncryption, RD_HOLDER extends KeyRqData<?>> {
    Optional<KEY> getDefault(Map<String, String> properties);
    Optional<KEY> generateKey(RD_HOLDER rdHolder);
    Optional<KEY> getRandomKey(RD_HOLDER rdHolder);

}

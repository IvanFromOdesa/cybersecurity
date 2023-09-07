package encryption.commons.crypt;

public interface IDecryptor<E extends BaseEncryption> {
    String decrypt(String msg, E key);
}

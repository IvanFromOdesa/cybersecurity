package encryption.commons;

public interface IDecryptor<E extends BaseEncryption> {
    String decrypt(String msg, E key);
}

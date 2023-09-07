package encryption.commons;

public interface IEncryptor<E extends BaseEncryption> {
    String encrypt(String msg, E key);
}

package encryption.commons.crypt;

public interface IEncryptor<E extends BaseEncryption> {
    String encrypt(String msg, E key);
}

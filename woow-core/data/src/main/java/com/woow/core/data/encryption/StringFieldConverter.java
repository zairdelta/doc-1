package com.woow.core.data.encryption;

import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class StringFieldConverter implements AttributeConverter<String, String> {
    private static final String AES = "AES";
    private static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public StringFieldConverter(@Value("${woo.bo.data.encryption.key}") String secret) throws Exception {
        if (secret == null || secret.length() != 16) {
            throw new IllegalArgumentException("Encryption key must be 16 characters for AES-128");
        }
        byte[] encryptionKey = secret.getBytes();
        Key key = new SecretKeySpec(encryptionKey, "AES");

        encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

        decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if(attribute == null) {
                return "";
            }
            return Base64.getEncoder().encodeToString(encryptCipher.doFinal(attribute.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if(dbData == null) {
                return "";
            }
            return new String(decryptCipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

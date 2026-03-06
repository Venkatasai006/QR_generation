package com.example.demo;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

@Service
public class KeyStoreService {

    private static final String KEYSTORE_FILE = "keystore.jceks";
    private static final String KEYSTORE_PASSWORD = "LabWeek@123$";
    private static final String KEY_ALIAS = "qr-login-key";
    private static final String KEY_PASSWORD = "LabWeek@123$";

    private volatile PrivateKey cachedKey;

    public PrivateKey getPrivateKey() throws Exception {

        if (cachedKey != null) {
            return cachedKey;
        }

        KeyStore ks = KeyStore.getInstance("JCEKS");

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(KEYSTORE_FILE);

        ks.load(is, KEYSTORE_PASSWORD.toCharArray());

        cachedKey = (PrivateKey) ks.getKey(
                KEY_ALIAS,
                KEY_PASSWORD.toCharArray()
        );

        return cachedKey;
    }
}
package com.naples.util;

import javax.annotation.PostConstruct;

import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;

public class KeyUtil {

    // TODO: Read key from config instead of generating a new key on startup
    private static final Key key = MacProvider.generateKey();

    public static Key getKey() {
        return key;
    }

}
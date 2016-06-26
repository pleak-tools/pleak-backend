package com.naples.generator;

import java.util.Base64;
import java.util.Random;

public class RandomStringGenerator implements FilePublicUriGenerator {
    private int length = 15;

    private char[] VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456879".toCharArray();

    public String getUri() {
        byte[] bytes = new byte[this.length];
        new Random().nextBytes(bytes);

        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
}

package com.vishal.pdfapi;

import java.util.Random;

public class TestFileUtil {

    public static byte[] generateBytes(int sizeInKB) {
        byte[] data = new byte[sizeInKB * 1024];
        new Random().nextBytes(data);
        return data;
    }
}

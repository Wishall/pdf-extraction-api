package com.vishal.pdfapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class TestFileUtil {

    public static byte[] generateBytes(int sizeInKB) {
        byte[] data = new byte[sizeInKB * 1024];
        new Random().nextBytes(data);
        return data;
    }

    public static byte[] load(String name) {
        try {
            return Files.readAllBytes(Paths.get("src/test/resources/pdfs/" + name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

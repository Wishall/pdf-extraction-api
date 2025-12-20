package com.vishal.pdfapi;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;

/**
 * Manual End-to-End Smoke Test.
 * 
 * Usage:
 * export RAPIDAPI_KEY=your_real_key_here
 * java ManualTest
 */
public class ManualTest {
    public static void main(String[] args) throws IOException {
        // 1. Read API Key from Environment Variable (NEVER COMMIT KEYS TO GIT)
        String apiKey = System.getenv("RAPIDAPI_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ ERROR: RAPIDAPI_KEY environment variable is not set.");
            System.err.println("Usage: export RAPIDAPI_KEY=your_key_here && mvn test -Dtest=ManualTest");
            return; // Exit safely
        }

        OkHttpClient client = new OkHttpClient();

        // Use the existing test file
        File file = new File("src/test/resources/pdfs/valid.pdf");
        
        if (!file.exists()) {
             System.err.println("❌ ERROR: Test file not found at " + file.getAbsolutePath());
             return;
        }

        System.out.println("🚀 Starting Smoke Test against Production...");
        System.out.println("Target: https://pdf-text-extractor3.p.rapidapi.com/api/extract-text");
        System.out.println("File: " + file.getName());

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/pdf"));

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://pdf-text-extractor3.p.rapidapi.com/api/extract-text")
                .post(body)
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", "pdf-text-extractor3.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("------------------------------------------------");
            System.out.println("Status Code: " + response.code());
            
            if (response.isSuccessful()) {
                System.out.println("✅ SUCCESS! API is working.");
                // Optional: Print snippet of body to verify
                String responseBody = response.body().string();
                System.out.println("Response Preview: " + responseBody.substring(0, Math.min(responseBody.length(), 100)) + "...");
            } else {
                System.err.println("❌ FAILURE! API returned error.");
                System.err.println("Body: " + response.body().string());
            }
            System.out.println("------------------------------------------------");
        }
    }
}

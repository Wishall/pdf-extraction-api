package com.vishal.pdfapi;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// This class is the entry point configured in the AWS Lambda console
public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // Initialize the handler statically for cold start optimization
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(PdfTextApiApplication.class);

        } catch (Exception e) {
            // Catch exceptions during Spring context init and force a new cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        // This proxies the input stream (API Gateway event) to the Spring application
        handler.proxyStream(inputStream, outputStream, context);
    }
}
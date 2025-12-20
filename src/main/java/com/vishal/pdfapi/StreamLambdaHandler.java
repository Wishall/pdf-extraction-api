package com.vishal.pdfapi;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(PdfTextApiApplication.class);
        } catch (ContainerInitializationException e) {
            logger.error("FATAL: Could not initialize Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        String requestString = null;
        try {
            // Read the input stream into a string. This is the raw event from API Gateway.
            requestString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            logger.info("<<<<<<<<<< RECEIVED RAW REQUEST FROM API GATEWAY >>>>>>>>>>");
            logger.info(requestString);
            logger.info("<<<<<<<<<< END OF RAW REQUEST >>>>>>>>>>");

            // After logging, use a new InputStream to pass to the handler
            InputStream newInputStream = new ByteArrayInputStream(requestString.getBytes(StandardCharsets.UTF_8));
            handler.proxyStream(newInputStream, outputStream, context);

        } catch (Exception e) {
            logger.error("<<<<<<<<<< EXCEPTION DURING REQUEST HANDLING >>>>>>>>>>", e);
            // Log the raw request again if it was captured, to help debug the failing request
            if (requestString != null) {
                logger.error("Failing Request Body: " + requestString);
            }
            // Ensure a valid response is sent to the client even in case of a low-level error
            outputStream.write("{\"message\":\"Internal server error during request processing.\"}".getBytes(StandardCharsets.UTF_8));
        }
    }
}

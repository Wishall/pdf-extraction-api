package com.vishal.pdfapi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String RESPONSE_HEADER_NAME = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 1. Generate a unique ID for this request
        // In a real Lambda environment, we might prefer the AWS Request ID, 
        // but a UUID is perfect for application-level tracing and works everywhere.
        String requestId = UUID.randomUUID().toString();

        try {
            // 2. Put it in the MDC so all logs (Controller, Service, etc.) include it automatically
            MDC.put(REQUEST_ID_KEY, requestId);

            // 3. Add it to the response header so the client (RapidAPI/User) can see it
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setHeader(RESPONSE_HEADER_NAME, requestId);
            }

            // 4. Continue processing
            chain.doFilter(request, response);

        } finally {
            // 5. Clean up to prevent memory leaks or data bleeding into other requests
            MDC.remove(REQUEST_ID_KEY);
        }
    }
}

//package com.vishal.pdfapi.config;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import jakarta.servlet.*;
//import jakarta.servlet.http.*;
//import java.io.IOException;
//import java.util.Set;
//
//@Component
//public class ApiKeyFilter extends OncePerRequestFilter {
//  private static final Set<String> VALID_KEYS = Set.of("demo-key-123","test-key-999");
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//  throws ServletException, IOException {
//    String key=req.getHeader("X-API-Key");
//    if(key==null || !VALID_KEYS.contains(key)){
//      res.setStatus(401);
//      res.getWriter().write("Invalid API Key");
//      return;
//    }
//    chain.doFilter(req,res);
//  }
//}
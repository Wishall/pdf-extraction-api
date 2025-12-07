package com.vishal.pdfapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        converters.stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(converter -> (MappingJackson2HttpMessageConverter) converter)
                .forEach(converter -> {

                    // 1. Get the existing (immutable) list
                    List<MediaType> originalTypes = converter.getSupportedMediaTypes();

                    // 2. CREATE A NEW, MODIFIABLE LIST, and add all original types to it.
                    List<MediaType> mutableTypes = new ArrayList<>(originalTypes);

                    // 3. ADD the new types to the MUTABLE list.
                    mutableTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                    mutableTypes.add(MediaType.ALL); // Keep ALL as a safety net

                    // 4. Set the new, modifiable list back to the converter.
                    converter.setSupportedMediaTypes(mutableTypes);
                });
    }
}
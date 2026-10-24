package com.petconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path petsDir = Paths.get("uploads", "pets");
        String petsUri = petsDir.toUri().toString();
        if (!petsUri.endsWith("/")) {
            petsUri += "/";
        }
        registry.addResourceHandler("/uploads/pets/**")
                .addResourceLocations(petsUri);

        Path sheltersDir = Paths.get("uploads", "shelters");
        String sheltersUri = sheltersDir.toUri().toString();
        if (!sheltersUri.endsWith("/")) {
            sheltersUri += "/";
        }
        registry.addResourceHandler("/uploads/shelters/**")
                .addResourceLocations(sheltersUri);
    }
}

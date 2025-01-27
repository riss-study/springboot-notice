package dev.riss.notice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${notice.attachment.directory}")
    private String attachmentDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/" + attachmentDirectory + "/**")
                .addResourceLocations("file:" + attachmentDirectory + "/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}

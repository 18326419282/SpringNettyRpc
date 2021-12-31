package com.example.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class PropertySourcesProcessor implements EnvironmentPostProcessor {

    private final PropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource("rpcApplication.yml");
        try {
            List<PropertySource<?>> properties = loader.load("rpcApplication.yml",resource);
            for(PropertySource propertySource: properties){
                environment.getPropertySources().addLast(propertySource);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

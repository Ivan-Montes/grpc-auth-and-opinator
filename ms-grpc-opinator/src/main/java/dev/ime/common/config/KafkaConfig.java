package dev.ime.common.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;

import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaConfig {

    @Bean
    ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(
            KafkaProperties kafkaProperties,
            SslBundles sslBundles) {
        
        Map<String, Object> props = kafkaProperties.buildProducerProperties(sslBundles);
       
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
        
    }
    
}

package com.so1s.pocapiserver;

import com.so1s.pocapiserver.service.BuildService;
import com.so1s.pocapiserver.service.KubernetesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public BuildService buildService() {
        return new BuildService();
    }
    @Bean
    public KubernetesService kubernetesService() {
        return new KubernetesService();
    }
}

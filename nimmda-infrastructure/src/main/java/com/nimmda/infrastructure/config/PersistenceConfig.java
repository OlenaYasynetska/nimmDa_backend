package com.nimmda.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EntityScan(basePackages = "com.nimmda.infrastructure")
@EnableJpaRepositories(basePackages = "com.nimmda.infrastructure")
@EnableMongoRepositories(basePackages = "com.nimmda.infrastructure")
public class PersistenceConfig {
}

package com.nimmda.infrastructure.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConnectionConfig {

    @Bean
    @Primary
    MongoDatabaseFactory mongoDatabaseFactory(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database:nimmda}") String database) {
        String normalized = withAuthSourceIfNeeded(uri);
        ConnectionString connection = new ConnectionString(normalized);
        String dbName = connection.getDatabase() != null ? connection.getDatabase() : database;
        return new SimpleMongoClientDatabaseFactory(MongoClients.create(connection), dbName);
    }

    static String withAuthSourceIfNeeded(String uri) {
        ConnectionString parsed = new ConnectionString(uri);
        if (parsed.getUsername() == null || uri.contains("authSource=")) {
            return uri;
        }
        return uri.contains("?") ? uri + "&authSource=admin" : uri + "?authSource=admin";
    }
}

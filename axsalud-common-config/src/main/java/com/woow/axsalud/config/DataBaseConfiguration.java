package com.woow.axsalud.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile(value = {"prod", "staging"})
@Slf4j
public class DataBaseConfiguration {

    @Value("${maxPoolSize:25}")
    private int maxPoolSize;

    @Value("${minimumIdle:25}")
    private int minimumIdle;

    @Bean
    public HikariDataSource getDataSource() throws URISyntaxException {
        String rawDbUrl = System.getenv("DATABASE_URL");
        log.info("Using DATABASE_URL: {}", rawDbUrl);

        URI dbUri = new URI(rawDbUrl);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String host = dbUri.getHost();
        int port = dbUri.getPort(); // get the port
        String dbName = dbUri.getPath().replaceFirst("/", ""); // remove leading slash

        // Include port in the JDBC URL
        String dbUrl = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false&serverTimezone=UTC", host, port, dbName);

        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url(dbUrl)
                .username(username)
                .password(password)
                .build();

        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(minimumIdle);

        return dataSource;
    }

}
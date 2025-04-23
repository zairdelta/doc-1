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

@Profile(value = {"prod", "staging"})
@Slf4j
public class DataBaseConfiguration {

    @Value("${maxPoolSize:25}")
    private int maxPoolSize;

    @Value("${minimumIdle:25}")
    private int minimumIdle;

    //@Bean
    public HikariDataSource getDataSource() throws URISyntaxException {

        URI dbUri = new URI(System.getenv("APP_DATABASE_URL"));
        log.info("Configuration added in bo-config task");
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath();

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url(dbUrl+"?autoReconnect=true");
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.type(HikariDataSource.class);
        HikariDataSource hikariDataSource = (HikariDataSource) dataSourceBuilder.build();
        hikariDataSource.setMaximumPoolSize(maxPoolSize);
        hikariDataSource.setMinimumIdle(minimumIdle);
        return hikariDataSource;
    }
}
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
@Configuration
public class DataBaseConfiguration {

    @Value("${maxPoolSize:25}")
    private int maxPoolSize;

    @Value("${minimumIdle:25}")
    private int minimumIdle;

    public HikariDataSource getDataSource() throws URISyntaxException {

        URI dbUri = new URI(System.getenv("APP_DATABASE_URL"));
        log.info("Configuration added in bo-config task");

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String host = dbUri.getHost();
        int port = dbUri.getPort(); // <-- Get the port
        String path = dbUri.getPath(); // includes the slash + db name

        // Now include the port in the JDBC URL
        String dbUrl = String.format("jdbc:mysql://%s:%d%s?autoReconnect=true", host, port, path);

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url(dbUrl);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.type(HikariDataSource.class);

        HikariDataSource hikariDataSource = (HikariDataSource) dataSourceBuilder.build();
        hikariDataSource.setMaximumPoolSize(maxPoolSize);
        hikariDataSource.setMinimumIdle(minimumIdle);

        return hikariDataSource;
    }

}
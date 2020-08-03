package ru.fa.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class KnBaseConfig {

    @Bean
    public DataSource dataSource(
            @Value("${knbase.db.driver}") String driverClassName,
            @Value("${knbase.db.user}") String user,
            @Value("${knbase.db.password}") String password,
            @Value("${knbase.db.url}") String url
    ) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setJdbcUrl(url);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

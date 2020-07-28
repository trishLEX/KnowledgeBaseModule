package ru.fa.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class KnBaseConfig {

    @Bean
    public DataSource dataSource() {
//        DataSource dataSource = DataSourceBuilder.create()
//                .driverClassName("org.postgresql.Driver")
//                .password("0212")
//                .username("postgres")
//                .url("jdbc:postgresql://localhost:5432/kn_base")
//                .build();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setPassword("0212");
        hikariConfig.setUsername("postgres");
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/kn_base");

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

package ru.fa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
@Profile("testing")
public class TestMain extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TestMain.class);

        ConfigurableEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("testing");
        app.setEnvironment(env);

        app.run(args);
    }
}

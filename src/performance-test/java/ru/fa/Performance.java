package ru.fa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
@Profile("performance")
public class Performance extends SpringBootServletInitializer {

    //скорость роста наблюдений: в большую сторону log_2(VERTICES)^COMPONENTS

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Performance.class);

        ConfigurableEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("performance");
        app.setEnvironment(env);

        app.run(args);
    }
}

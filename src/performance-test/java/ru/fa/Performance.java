package ru.fa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import ru.fa.model.Dimension;

@SpringBootApplication
@Profile("performanceTest")
public class Performance extends SpringBootServletInitializer {

    //скорость роста наблюдений: в большую сторону log_2(VERTICES)^COMPONENTS

    private static final int VERTICES = 15;
    private static final int COMPONENTS = 3;

    private static final int CHILD_SIZE = 2;

    private static final Map<Long, Dimension> DIMENSION_MAP = new HashMap<>(VERTICES * COMPONENTS);
    private static final List<Dimension> ROOTS = new ArrayList<>(COMPONENTS);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TestMain.class);

        ConfigurableEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("performanceTest");
        app.setEnvironment(env);

        app.run(args);
    }
}

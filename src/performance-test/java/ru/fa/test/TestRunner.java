package ru.fa.test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dto.QuestionRequest;
import ru.fa.loader.ComponentsGenerator;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;

@Service
@Profile("performance")
@DependsOn("questionController")
public class TestRunner {

    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    private static final int VERTICES = 15;
    private static final int COMPONENTS = 3;
    private static final int CHILD_SIZE = 2;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObservationDao observationDao;

    @Autowired
    private DimensionDao dimensionDao;

    @Autowired
    private ComponentsGenerator componentsGenerator;

    private void testExact() {
        Observation observation = observationDao.getObservation(3);

        Map<String, String> dimensions = observation.getDimensionMap()
                .values()
                .stream()
                .collect(Collectors.toMap(
                        Dimension::getDimensionSubType,
                        Dimension::getStrId
                ));

        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                dimensions
        );

        Stopwatch stopWatch = Stopwatch.createStarted();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080/question",
                questionRequest,
                String.class
        );
        stopWatch.stop();

        assert responseEntity.getStatusCode() == HttpStatus.OK;
        log.info(
                "Test exact: {}, dimensions: {}, observations; {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
    }

    @EventListener(ContextRefreshedEvent.class)
    public void runTests() {
        componentsGenerator.clearDb();
        componentsGenerator.loadComponents(VERTICES, COMPONENTS, CHILD_SIZE);
        testExact();
        SpringApplication.exit(applicationContext, () -> 0);
        System.exit(0);
    }

    @PreDestroy
    public void clear() {
        componentsGenerator.clearDb();
    }
}

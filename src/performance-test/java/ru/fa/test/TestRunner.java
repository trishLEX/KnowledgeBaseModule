package ru.fa.test;

import java.util.List;
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

    private static final int VERTICES = 30;
    private static final int COMPONENTS = 4;
    private static final int CHILD_SIZE = 3;

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
        log.info(responseEntity.getBody());
        log.info(
                "Test exact: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
    }

    private void testExactWithoutOneDimension() {
        Observation observation = observationDao.getObservation(3);

        Map<String, String> dimensions = observation.getDimensionMap()
                .values()
                .stream()
                .collect(Collectors.toMap(
                        Dimension::getDimensionSubType,
                        Dimension::getStrId
                ));
        String dimensionSubtype = dimensionDao.getAllSubtypes()
                .stream()
                .limit(1)
                .findFirst()
                .get();
        dimensions.remove(dimensionSubtype);

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
        log.info(responseEntity.getBody());
        log.info(
                "Test exact without one dimension: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
    }

    private void testOneDimension() {
        Observation observation = observationDao.getObservation(3);

        Map<String, String> dimensions = observation.getDimensionMap()
                .values()
                .stream()
                .collect(Collectors.toMap(
                        Dimension::getDimensionSubType,
                        Dimension::getStrId
                ));
        List<String> dimensionSubtypes = dimensionDao.getAllSubtypes()
                .stream()
                .limit(COMPONENTS - 1)
                .collect(Collectors.toList());
        dimensionSubtypes.forEach(dimensions::remove);

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
        log.info(responseEntity.getBody());
        log.info(
                "Test one dimension: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
    }

    @EventListener(ContextRefreshedEvent.class)
    public void runTests() {
        componentsGenerator.clearDb();
        componentsGenerator.loadComponents(VERTICES, COMPONENTS, CHILD_SIZE);
        log.info("Components loaded");
        testExact();
        testExactWithoutOneDimension();
        testOneDimension();
        SpringApplication.exit(applicationContext, () -> 0);
        System.exit(0);
    }

    @PreDestroy
    public void clear() {
        componentsGenerator.clearDb();
    }
}

package ru.fa.test;

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

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Profile("performance")
@DependsOn("questionController")
public class TestRunner {

    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    private static int HEIGHT = 3;
    private static int COMPONENTS = 4;
    private static int CHILD_SIZE = 3;

    private static final List<List<Integer>> PARAMS = List.of(
            List.of(3, 6, 2),
            List.of(3, 7, 2),
            List.of(3, 8, 2),
            List.of(3, 9, 2),
            List.of(3, 10, 2),

            List.of(4, 6, 2),
            List.of(4, 7, 2),
            List.of(4, 8, 2),
            List.of(4, 9, 2),
            List.of(4, 10, 2),

            List.of(3, 5, 3),

            List.of(4, 2, 3),
            List.of(4, 3, 3),
            List.of(4, 4, 3),
            List.of(4, 5, 4)
    );

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
        System.out.println("*****\n\n");
        Observation observation = observationDao.getObservation(0);

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

        log.info("Request: {}", questionRequest);
        Stopwatch stopWatch = Stopwatch.createStarted();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080/question",
                questionRequest,
                String.class
        );
        stopWatch.stop();

        assert responseEntity.getStatusCode() == HttpStatus.OK;
        log.info("Response: {}", responseEntity.getBody());
        log.info(
                "Test exact: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
        System.out.println("*****\n\n");
    }

    private void superExact() {
        System.out.println("*****\n\n");
        Set<Long> dims = dimensionDao.getFactDimensions(dimensionDao.getDimensionsTopConcepts().values());
        Map<String, String> dimensions = dimensionDao.getDimensions(dims)
                .values()
                .stream()
                .collect(Collectors.toMap(
                        Dimension::getDimensionSubType,
                        Dimension::getStrId,
                        (a, b) -> a
                ));

        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                dimensions
        );

        log.info("Request: {}", questionRequest);
        Stopwatch stopWatch = Stopwatch.createStarted();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080/question",
                questionRequest,
                String.class
        );
        stopWatch.stop();

        assert responseEntity.getStatusCode() == HttpStatus.OK;
        log.info("Response: {}", responseEntity.getBody());
        log.info(
                "Test superExact: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
        System.out.println("*****\n\n");
    }

    private void testExactWithoutOneDimension() {
        System.out.println("*****\n\n");
        Observation observation = observationDao.getObservation(0);

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

        log.info("Request: {}", questionRequest);
        Stopwatch stopWatch = Stopwatch.createStarted();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080/question",
                questionRequest,
                String.class
        );
        stopWatch.stop();

        assert responseEntity.getStatusCode() == HttpStatus.OK;
        log.info("Response: {}", responseEntity.getBody());
        log.info(
                "Test exact without one dimension: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
        System.out.println("*****\n\n");
    }

    private void testOneDimension() {
        System.out.println("*****\n\n");
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

        log.info("Request: {}", questionRequest);
        Stopwatch stopWatch = Stopwatch.createStarted();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080/question",
                questionRequest,
                String.class
        );
        stopWatch.stop();

        assert responseEntity.getStatusCode() == HttpStatus.OK;
        log.info("Response: {}", responseEntity.getBody());
        log.info(
                "Test one dimension: {} ms, dimensions: {}, observations: {}",
                stopWatch.elapsed(TimeUnit.MILLISECONDS),
                dimensionDao.countDimensions(),
                observationDao.countObservations()
        );
        System.out.println("*****\n\n");
    }

    @EventListener(ContextRefreshedEvent.class)
    public void runTests() {
        for (List<Integer> params : PARAMS) {
            HEIGHT = params.get(0);
            COMPONENTS = params.get(1);
            CHILD_SIZE = params.get(2);
            componentsGenerator.clearDb();
            componentsGenerator.loadComponents(HEIGHT, COMPONENTS, CHILD_SIZE);
            log.info("Components loaded: " + HEIGHT + " " + COMPONENTS + " " + CHILD_SIZE);
            superExact();
            testExactWithoutOneDimension();
            testOneDimension();
            superExact();
            testExactWithoutOneDimension();
            testOneDimension();
        }
        SpringApplication.exit(applicationContext, () -> 0);
        System.exit(0);
    }

    @PreDestroy
    public void clear() {
        componentsGenerator.clearDb();
    }
}

package ru.fa.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.exception.BadRequestException;
import ru.fa.model.Observation;
import ru.fa.service.ObservationConflictException;
import ru.fa.service.ObservationService;

import java.util.List;

@RestController
@RequestMapping("observations")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping("{id}")
    public Observation getObservation(@PathVariable("id") long id) {
        return observationService.getObservation(id);
    }

    @GetMapping
    public List<Observation> getObservations() {
        return observationService.getObservations();
    }

    @PutMapping("{id}")
    public void updateObservation(@PathVariable("id") long id, @RequestBody Observation observation) {
        if (observation.getId() != id) {
            throw new IllegalArgumentException("Wrong ids");
        }
        observationService.updateObservation(observation);
    }

    @PostMapping
    public void createObservation(@RequestBody Observation observation) {
        try {
            observationService.insertObservation(observation);
        } catch (ObservationConflictException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public void deleteObservation(@PathVariable("id") long id) {
        observationService.deleteObservation(id);
    }
}

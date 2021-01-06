package ru.fa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dao.ValueDao;
import ru.fa.model.ObservationValue;

import java.util.List;

@RestController
@RequestMapping("observations/values")
public class ObservationValueController {

    private final ValueDao valueDao;

    @Autowired
    public ObservationValueController(ValueDao valueDao) {
        this.valueDao = valueDao;
    }

    @GetMapping("{id}")
    public List<ObservationValue> getObservationValues(@PathVariable("id") long id) {
        return valueDao.getObservationValues(id);
    }

    @GetMapping
    public List<ObservationValue> getObservationValues() {
        return valueDao.getObservationValues();
    }

    @PutMapping("{id}")
    public void updateObservationValue(@PathVariable("id") long id, @RequestBody List<ObservationValue> value) {
        valueDao.updateObservationValues(id, value);
    }

    @PostMapping
    public void createObservationValue(@RequestBody ObservationValue value) {
        valueDao.insertObservationValue(value);
    }

    @DeleteMapping("{id}")
    public void deleteObservationValue(@PathVariable("id") long id) {
        valueDao.deleteObservationValues(id);
    }
}

package ru.fa.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dao.ValueDao;
import ru.fa.exception.BadRequestException;
import ru.fa.exception.NotFoundException;
import ru.fa.model.Value;

import java.util.List;

@RestController
@RequestMapping("values")
public class ValueController {

    private final ValueDao valueDao;

    @Autowired
    public ValueController(ValueDao valueDao) {
        this.valueDao = valueDao;
    }

    @GetMapping("{id}")
    public Value getValue(@PathVariable("id") long id) {
        try {
            return valueDao.getValue(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("No values with id: " + id);
        }
    }

    @GetMapping
    public List<Value> getValues(@RequestParam(value = "str_id", required = false) List<String> strIds) {
        if (CollectionUtils.isEmpty(strIds)) {
            return valueDao.getValues();
        }
        return valueDao.getValues(strIds);
    }

    @PutMapping("{id}")
    public void updateValue(@PathVariable("id") long id, @RequestBody Value value) {
        if (id != value.getId()) {
            throw new BadRequestException("Wrong ids: " + id + " and " + value.getId());
        }
        valueDao.updateValue(value);
    }

    @PostMapping
    public void createValue(@RequestBody Value value) {
        valueDao.createValue(value);
    }

    @DeleteMapping("{id}")
    public void deleteValue(@PathVariable("id") long id) {
        valueDao.deleteValue(id);
    }
}

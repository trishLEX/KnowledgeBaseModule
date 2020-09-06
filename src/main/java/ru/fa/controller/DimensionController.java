package ru.fa.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dao.DimensionDao;
import ru.fa.model.Dimension;

import java.util.List;

@RestController("dimensions")
public class DimensionController {

    private final DimensionDao dimensionDao;

    public DimensionController(DimensionDao dimensionDao) {
        this.dimensionDao = dimensionDao;
    }

    @GetMapping("{id}")
    public Dimension getDimension(@PathVariable("id") long id) {
        return dimensionDao.getDimension(id);
    }

    @GetMapping
    public List<Dimension> getDimension(@RequestParam("str_id") List<String> strIds) {
        return dimensionDao.getDimensionsByStrId(strIds);
    }

    @PutMapping("{id}")
    public void updateDimension(
            @PathVariable("id") long id,
            @RequestBody Dimension dimension
    ) {
        if (dimension.getId() != id) {
            throw new IllegalArgumentException("Wrong ids");
        }

        dimensionDao.updateDimension(dimension);
    }

    @PostMapping
    public void createDimension(@RequestBody Dimension dimension) {
        dimensionDao.createDimension(dimension);
    }

    @DeleteMapping("{id}")
    public void deleteDimension(@PathVariable("id") long id) {
        dimensionDao.deleteDimension(id);
    }
}

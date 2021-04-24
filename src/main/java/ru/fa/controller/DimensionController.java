package ru.fa.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dao.DimensionDao;
import ru.fa.exception.BadRequestException;
import ru.fa.exception.NotFoundException;
import ru.fa.model.Dimension;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("dimensions")
public class DimensionController {

    private final DimensionDao dimensionDao;

    public DimensionController(DimensionDao dimensionDao) {
        this.dimensionDao = dimensionDao;
    }

    @GetMapping("{id}")
    public Dimension getDimension(@PathVariable("id") long id) {
        try {
            return dimensionDao.getDimension(id);
        } catch (NoSuchElementException e) {
            throw new NotFoundException("No dimensions with id: " + id);
        }
    }

    @GetMapping
    public List<Dimension> getDimension(@RequestParam("str_id") List<String> strIds) {
        if (CollectionUtils.isEmpty(strIds)) {
            return dimensionDao.getDimensions();
        }
        return dimensionDao.getDimensionsByStrId(strIds);
    }

    @PutMapping("{id}")
    public void updateDimension(
            @PathVariable("id") long id,
            @RequestBody Dimension dimension
    ) {
        if (dimension.getId() != id) {
            throw new BadRequestException("Wrong ids: " + id + " and " + dimension.getId());
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

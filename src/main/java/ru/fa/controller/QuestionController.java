package ru.fa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dto.QuestionRequest;
import ru.fa.dto.QuestionResponse;
import ru.fa.service.DimensionService;
import ru.fa.service.QuestionService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RestController
public class QuestionController {

    private final QuestionService questionService;
    private final DimensionService dimensionService;

    @Autowired
    public QuestionController(QuestionService questionService, DimensionService dimensionService) {
        this.questionService = questionService;
        this.dimensionService = dimensionService;
    }

    @PostMapping("question")
    public QuestionResponse processQuestion(@RequestBody QuestionRequest questionRequest) {
        Map<String, Long> dimensions = getDimensions(questionRequest);
        Set<Long> factDimensions = getFactDimensions(dimensions.values());
        return questionService.processNotEmptyQuestion(questionRequest.getValueSubType(), dimensions, factDimensions);
    }

    private Map<String, Long> getDimensions(QuestionRequest questionRequest) {
        return dimensionService.getRequestDimensions(questionRequest.getDimensions());
    }

    private Set<Long> getFactDimensions(Collection<Long> inputIds) {
        return dimensionService.getFactDimensions(inputIds);
    }
}

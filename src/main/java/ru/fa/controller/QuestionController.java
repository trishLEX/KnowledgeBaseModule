package ru.fa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.fa.dto.QuestionRequest;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.DimensionSubType;
import ru.fa.service.DimensionService;
import ru.fa.service.QuestionService;

import java.util.Map;

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
        Map<DimensionSubType, Long> dimensions = getDimensions(questionRequest);
        return questionService.processNotEmptyQuestion(questionRequest.getValueSubType(), dimensions);
    }

    private Map<DimensionSubType, Long> getDimensions(QuestionRequest questionRequest) {
        if (questionRequest.getDimensions().isEmpty()) {
            return dimensionService.getRequestDimensions();
        } else {
            return dimensionService.getRequestDimensions(questionRequest);
        }
    }
}

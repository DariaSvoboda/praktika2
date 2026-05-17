package com.example.praktika2;

import java.util.List;
import java.util.Map;

public class QuestionModel {
    private String id;
    private String text;
    private String type; // "single" или "multiple"
    private List<String> options;
    private List<Integer> correctAnswers; // индексы правильных ответов

    public QuestionModel() { }

    public QuestionModel(String id, String text, String type, List<String> options, List<Integer> correctAnswers) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswers = correctAnswers;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public List<Integer> getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(List<Integer> correctAnswers) { this.correctAnswers = correctAnswers; }

    // Преобразование в Map для Firestore
    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "text", text,
                "type", type,
                "options", options,
                "correctAnswers", correctAnswers
        );
    }
}
package com.example.praktika2;

import java.util.List;

public class TestModel {
    private String testId;
    private String title;
    private String authorId;
    private List<QuestionModel> questions;
    private int questionCount;

    public TestModel() { }

    public TestModel(String testId, String title, String authorId, List<QuestionModel> questions) {
        this.testId = testId;
        this.title = title;
        this.authorId = authorId;
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
    }

    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public List<QuestionModel> getQuestions() { return questions; }
    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
        this.questionCount = questions != null ? questions.size() : 0;
    }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
}
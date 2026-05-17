package com.example.praktika2;

public class ClassModel {
    private String classId;
    private String name;
    private String code;
    private String creatorId;
    private boolean isCreator;

    public ClassModel() { }

    public ClassModel(String classId, String name, String code, String creatorId, boolean isCreator) {
        this.classId = classId;
        this.name = name;
        this.code = code;
        this.creatorId = creatorId;
        this.isCreator = isCreator;
    }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public boolean isCreator() { return isCreator; }
    public void setCreator(boolean creator) { isCreator = creator; }
}
package com.example.praktika2;

public class MaterialModel {
    private String materialId;
    private String type; // "text" или "link"
    private String content;
    private String title;
    private String authorId;

    public MaterialModel() { }

    public MaterialModel(String materialId, String type, String title, String content, String authorId) {
        this.materialId = materialId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
    }

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
}
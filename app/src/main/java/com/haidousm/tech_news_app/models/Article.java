package com.haidousm.tech_news_app.models;

public class Article {
    private Long ID;
    private String title;
    private String content;

    public Article(Long id, String title, String content) {

        ID = id;
        this.title = title;
        this.content = content;
    }

    public Article(Long id, String title) {
        ID = id;
        this.title = title;
    }

    public Long getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.title;
    }
}

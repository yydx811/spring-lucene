package org.example.lucene.domain;

public class Book {
    private long id;
    private String title;
    /**
     * @see BookStatus
     */
    private String status;
    private long time;

    public Book() {
    }

    public Book(long id, String title, String status, long time) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

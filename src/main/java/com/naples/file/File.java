package com.naples.file;

public class File implements java.io.Serializable {

    // Database
    Integer id;
    String title;
    Integer userId;

    // Other
    String lastModified;
    String md5Hash;
    String content;

    public File() {}

    public File(Integer id, String title, Integer userId) {
        this.id = id;
        this.title = title;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getMD5Hash() {
        return md5Hash;
    }

    public void setMD5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
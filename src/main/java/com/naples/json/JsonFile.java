package com.naples.json;

import java.util.HashSet;
import java.util.Set;

import com.naples.file.File;
import com.naples.file.FilePermission;

public class JsonFile implements Comparable<JsonFile>, java.io.Serializable {

    Integer id;
    String title;
    String content;

    String lastModified;
    String md5Hash;

    Boolean published;
    String uri;

    JsonUser user;
    Set<JsonFilePermission> filePermissions = new HashSet<JsonFilePermission>(0);

    public JsonFile() {}

    public JsonFile(File file) {
        this.id = file.getId();
        this.title = file.getTitle();
        this.content = file.getContent();

        this.lastModified = file.getLastModified();
        this.md5Hash = file.getMD5Hash();

        this.published = file.getPublished();
        if(this.published) this.uri = file.getUri();

        this.user = new JsonUser(file.getUser());
        for (FilePermission fp : file.getFilePermissions()) {
            this.filePermissions.add(new JsonFilePermission(fp));
        }
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

    public JsonUser getUser() {
        return user;
    }
    public void setUser(JsonUser user) {
        this.user = user;
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

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public Set<JsonFilePermission> getFilePermissions() {
        return filePermissions;
    }
    public void setFilePermissions(Set<JsonFilePermission> filePermissions) {
        this.filePermissions = filePermissions;
    }

    @Override
    public int compareTo(JsonFile file) {
        return this.id-file.getId();
    }

}

package com.naples.json;

import java.util.HashSet;
import java.util.Set;

import com.naples.file.File;
import com.naples.file.Permission;

public class JsonFile extends JsonPobject implements java.io.Serializable {

    String content;
    Boolean published;
    String uri;
    Long lastModified;
    JsonUser modifiedBy;
    String md5Hash;

    public JsonFile() {}

    public JsonFile(File file) {
        this.type = "file";
        this.id = file.getId();
        this.title = file.getTitle();
        this.content = file.getContent();

        this.lastModified = file.getLastModified().getTime();

        this.modifiedBy = new JsonUser();
        if (file.getModifiedBy() != null) this.modifiedBy = new JsonUser(file.getModifiedBy());

        this.md5Hash = file.getMD5Hash();

        this.directory = new JsonDirectory();
        if (file.getDirectory() != null) this.directory.setId(file.getDirectory().getId());

        this.published = file.getPublished();
        if (this.published) this.uri = file.getUri();

        this.user = new JsonUser(file.getUser());
        for (Permission p : file.getPermissions()) {
            this.permissions.add(new JsonPermission(p));
        }
    }

    public Long getLastModified() {
        return lastModified;
    }
    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }
    
    public JsonUser getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(JsonUser modifiedBy) {
        this.modifiedBy = modifiedBy;
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

    public Boolean getPublished() {
        return published;
    }
    public void setPublished(Boolean published) {
        this.published = published;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public void removeSensitiveData() {
        this.permissions.clear();
        this.user = null;
    }
}

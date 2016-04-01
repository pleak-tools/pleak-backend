package com.naples.file;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

import com.naples.user.User;

public class File implements Comparable<File>, java.io.Serializable {

    private ServletContext context;

    private FileHelper fh;

    // Database
    Integer id;
    String title;
    User user;

    // Other
    Path path;
    String lastModified;
    String md5Hash;
    String content;

    public File() {}

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void loadLastModified() {
        this.lastModified = this.fh.getFileLastModifiedString(path);
    }

    public String getMD5Hash() {
        return md5Hash;
    }

    public void loadMD5Hash() throws NoSuchAlgorithmException, IOException {
        this.md5Hash = this.fh.getMD5Hash(path.toString());
    }

    public String getContent() {
        return content;
    }

    public void loadContent() throws IOException {
        this.content = this.fh.getContent(path);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void save() throws FileException, NoSuchAlgorithmException, IOException {
        fh.saveFile(content, md5Hash, path.toString());
        loadLastModified();
        loadMD5Hash();
    }

    public void delete() throws FileException, IOException {
        fh.deleteFile(path.toString());
    }

    // Build with request context
    public void build(ServletContext context) {
        this.context = context;
        String bpmnFilesDir = this.context.getRealPath(this.context.getInitParameter("bpmn-files-dir"));
        this.fh = new FileHelper();
        this.path = Paths.get(bpmnFilesDir + "/" + id);
    }

    @Override
    public int compareTo(File file) {
        return this.id-file.getId();
    }


}
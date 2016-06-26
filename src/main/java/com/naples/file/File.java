package com.naples.file;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

import com.naples.generator.FilePublicUriGenerator;
import com.naples.user.User;
import com.naples.helper.Action;
import com.naples.json.JsonFilePermission;

public class File implements Comparable<File>, java.io.Serializable {

    private ServletContext context;

    private FileHelper fh;

    // Database
    Integer id;
    String title;
    User user;

    String publicToken;
    Set<FilePermission> filePermissions = new HashSet<FilePermission>(0);

    Boolean published;
    String uri;

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
    public void setMD5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }
    public void loadMD5Hash() throws NoSuchAlgorithmException, IOException {
        this.md5Hash = this.fh.getMD5Hash(path.toString());
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void loadContent() throws IOException {
        this.content = this.fh.getContent(path);
    }

    public Set<FilePermission> getFilePermissions() {
        return filePermissions;
    }
    public void setFilePermissions(Set<FilePermission> filePermissions) {
        this.filePermissions = filePermissions;
    }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public void createUri(FilePublicUriGenerator generator) {
        this.uri = generator.getUri();
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

    public void setPublic() {
        if (this.publicToken.length() == 0) {
            this.publicToken = UUID.randomUUID().toString().replaceAll("-", "");
        }
    }

    public void setPrivate() {
        if (this.publicToken.length() > 0) this.publicToken = "";
    }

    public boolean verifyPublicToken(String token) {
        return this.publicToken == token && this.publicToken.length() > 0;
    }

    public boolean canBeViewedBy(int userId) {
        if (this.user.getId() == userId) {
            return true;
        } else {
            Iterator iterator = filePermissions.iterator();
            while (iterator.hasNext()) {
                FilePermission fp = (FilePermission)iterator.next();
                if (fp.getUser().getId() == userId &&
                    (fp.getAction().getTitle().equals("view") ||
                     fp.getAction().getTitle().equals("edit")) ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(File file) {
        return this.id-file.getId();
    }

}

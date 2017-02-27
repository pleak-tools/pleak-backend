package com.naples.file;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;

import com.naples.generator.FilePublicUriGenerator;
import com.naples.user.User;
import com.naples.helper.Action;

public class File extends Pobject {

    private ServletContext context;
    private FileHelper fh;

    // Database
    Boolean published;
    String uri;

    // Other
    Path path;
    Date lastModified;
    User modifiedBy;
    String md5Hash;
    String content;

    public File() {}

    public Date getLastModified() {
        return lastModified;
    }
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    public void loadLastModified() {
        this.lastModified = this.fh.getFileLastModifiedDate(path);
    }
    
    public User getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
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

    public void save() throws FileException, NoSuchAlgorithmException, IOException {
        if (content == null) {
            fh.saveFile("", md5Hash, path.toString());
        } else {
            fh.saveFile(content, md5Hash, path.toString());
        }
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
        if (lastModified == null) {
            this.lastModified = this.fh.getFileLastModifiedDate(path);
        }
    }

    public boolean canBeViewedBy(int userId) {
        if (this.user.getId() == userId) {
            return true;
        } else {
            Iterator iterator = permissions.iterator();
            while (iterator.hasNext()) {
                Permission p = (Permission)iterator.next();
                if (p.getUser().getId() == userId &&
                    (p.getAction().getTitle().equals("view") ||
                     p.getAction().getTitle().equals("edit")) ) {
                    return true;
                }
            }
        }
        return false;
    }

}

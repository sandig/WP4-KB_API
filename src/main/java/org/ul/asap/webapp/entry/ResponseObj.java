package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResponseObj extends MyEntry {

    private int code;
    private String message;

    public ResponseObj(int code, String message) {
        super(null);
        this.code = code;
        this.message = message;
    }

    public ResponseObj() {
        super(null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRepositoryID() {
        return message;
    }

    public void setRepositoryID(String message) {
        this.message = message;
    }
}

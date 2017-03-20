package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class MyResponse {
    private int code;
    private String message;
    private List<ASAPEntry> resultList;
    private ASAPEntry resultObject;

    public MyResponse(int code, String message, List<ASAPEntry> resultList, ASAPEntry resultObject) {
        this.code = code;
        this.message = message;
        this.resultList = resultList;
        this.resultObject = resultObject;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ASAPEntry> getResultList() {
        return resultList;
    }

    public void setResultList(List<ASAPEntry> resultList) {
        this.resultList = resultList;
    }

    public ASAPEntry getResultObject() {
        return resultObject;
    }

    public void setResultObject(ASAPEntry resultObject) {
        this.resultObject = resultObject;
    }
}

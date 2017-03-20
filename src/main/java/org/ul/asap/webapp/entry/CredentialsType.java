package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public enum CredentialsType {
    //assigning the values of enum constants - http://examples.javacodegeeks.com/java-basics/java-enumeration-example/
    PASSWORD("PASSWORD"),
    CERTIFICATES("CERTIFICATES");


    private String value;
    private CredentialsType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

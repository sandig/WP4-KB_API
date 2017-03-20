package org.ul.asap.webapp.entry;

import org.ul.entice.webapp.entry.MyEntry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SimpleEntry extends MyEntry implements ASAPEntry {
    private String name;

    public SimpleEntry(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

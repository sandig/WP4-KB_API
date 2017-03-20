package org.ul.asap.webapp.entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ul.entice.webapp.entry.MyEntry;

import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class ApplicationComponent extends MyEntry {

    private String installScript;
    private boolean stateful;
    private String executionScript;
    private Object inputInterface;
    private Object developmentEvent;
    private Object outputInterface;
    private Object qualityAttribute;
    private String description;

    public ApplicationComponent() {
        super(null);

    }

    public ApplicationComponent(String id) {
        super(id);

    }

    public ApplicationComponent(String id, String installScript, boolean stateful, String executionScript, Object
            inputInterface, Object developmentEvent, Object outputInterface, Object qualityAttribute, String
            description) {
        super(id);
        this.installScript = installScript;
        this.stateful = stateful;
        this.executionScript = executionScript;
        this.inputInterface = inputInterface;
        this.developmentEvent = developmentEvent;
        this.outputInterface = outputInterface;
        this.qualityAttribute = qualityAttribute;
        this.description = description;
    }

    public String getInstallScript() {
        return installScript;
    }

    public void setInstallScript(String installScript) {
        this.installScript = installScript;
    }

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    public String getExecutionScript() {
        return executionScript;
    }

    public void setExecutionScript(String executionScript) {
        this.executionScript = executionScript;
    }

    public Object getInputInterface() {
        return inputInterface;
    }

    public void setInputInterface(Object inputInterface) {
        this.inputInterface = inputInterface;
    }

    public Object getDevelopmentEvent() {
        return developmentEvent;
    }

    public void setDevelopmentEvent(Object developmentEvent) {
        this.developmentEvent = developmentEvent;
    }

    public Object getOutputInterface() {
        return outputInterface;
    }

    public void setOutputInterface(Object outputInterface) {
        this.outputInterface = outputInterface;
    }

    public Object getQualityAttribute() {
        return qualityAttribute;
    }

    public void setQualityAttribute(Object qualityAttribute) {
        this.qualityAttribute = qualityAttribute;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

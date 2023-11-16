package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class Interpreter {

    private String name;
    private String version;
    private String timezone;
    private List<Constraint> constraints;
    private List<Element> elements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public void addConstraint(Constraint constraint) {
        if (this.constraints == null) {
            this.constraints = new ArrayList<>();
        }

        this.constraints.add(constraint);
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public void addElement(Element element) {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }

        this.elements.add(element);
    }
}

package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class Interpreter {

    private String name;
    private String version;
    private String timezone;
    private List<SpecConstraint> specConstraints;
    private List<FieldConstraint> fieldConstraints;
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

    public List<SpecConstraint> getSpecConstraints() {
        return specConstraints;
    }

    public void setSpecConstraints(List<SpecConstraint> specConstraints) {
        this.specConstraints = specConstraints;
    }

    public void addSpecConstraint(SpecConstraint specConstraint) {
        if (this.specConstraints == null) {
            this.specConstraints = new ArrayList<>();
        }

        this.specConstraints.add(specConstraint);
    }

    public List<FieldConstraint> getFieldConstraints() {
        return fieldConstraints;
    }

    public void setFieldConstraints(List<FieldConstraint> fieldConstraints) {
        this.fieldConstraints = fieldConstraints;
    }

    public void addFieldConstraint(FieldConstraint fieldConstraint) {
        if (this.fieldConstraints == null) {
            this.fieldConstraints = new ArrayList<>();
        }

        this.fieldConstraints.add(fieldConstraint);
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

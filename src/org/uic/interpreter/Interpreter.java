package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class Interpreter {

    private String name;
    private String version;
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

    public List<Constraint> getConditions() {
        return constraints;
    }

    public void setConditions(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public void addCondition(Constraint constraint) {
        if (this.constraints == null) {
            this.constraints = new ArrayList<>();
        }

        this.constraints.add(constraint);
    }

    public List<Element> getInstructions() {
        return elements;
    }

    public void setInstructions(List<Element> elements) {
        this.elements = elements;
    }

    public void addInstruction(Element element) {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }

        this.elements.add(element);
    }
}

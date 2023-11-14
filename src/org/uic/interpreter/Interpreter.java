package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class Interpreter {

    private String name;
    private String version;
    private List<Constraint> constraints;
    private List<Instruction> instructions;

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

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void addInstruction(Instruction instruction) {
        if (this.instructions == null) {
            this.instructions = new ArrayList<>();
        }

        this.instructions.add(instruction);
    }
}

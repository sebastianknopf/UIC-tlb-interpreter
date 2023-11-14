package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class Instruction {

    private String type;
    private String delimiter;
    private String format;
    private List<Field> fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }

        this.fields.add(field);
    }
}

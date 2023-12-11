package org.uic.interpreter;

import java.util.ArrayList;
import java.util.List;

class SpecConstraint {

    private String key;
    private List<String> values;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        if (this.values == null) {
            this.values = new ArrayList<>();
        }

        this.values.add(value);
    }
}

package org.uic.interpreter;

class Field {

    private int line;
    private int column;
    private boolean optional;
    private int substringStart;
    private int substringLength;
    private String prefix;
    private String suffix;

    Field() {
        this.substringStart = -1;
        this.substringLength = -1;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getSubstringStart() {
        return substringStart;
    }

    public void setSubstringStart(int substringStart) {
        this.substringStart = substringStart;
    }

    public int getSubstringLength() {
        return substringLength;
    }

    public void setSubstringLength(int substringLength) {
        this.substringLength = substringLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}

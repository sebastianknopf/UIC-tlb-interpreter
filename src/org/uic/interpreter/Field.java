package org.uic.interpreter;

class Field {

    private String line;
    private String column;
    private int substringStart;
    private int substringLength;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
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
}

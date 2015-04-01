package by.kate.model;

import java.util.List;

public class Signatory {

    private final int begin;
    private final int end;
    private final String text;
    private final List<String> lines;

    public Signatory(int begin, int end, List<String> lines, String text) {
        this.begin = begin;
        this.end = end;
        this.lines = lines;
        this.text = text;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public List<String> getLines() {
        return lines;
    }
}

package plugin.borealcore.functions.cooking.object;

public class Layout {

    private final int range;
    private final double[] successRate;
    private final int size;
    private final String start;
    private final String bar;
    private final String pointer;
    private final String offset;
    private final String end;
    private final String pointerOffset;
    private final String title;

    public Layout(int range, double[] successRate, int size, String start, String bar, String pointer, String offset, String end, String pointerOffset, String title) {
        this.range = range;
        this.successRate = successRate;
        this.size = size;
        this.start = start;
        this.bar = bar;
        this.pointer = pointer;
        this.offset = offset;
        this.end = end;
        this.pointerOffset = pointerOffset;
        this.title = title;
    }

    public int getRange() {
        return range;
    }

    public double[] getSuccessRate() {
        return successRate;
    }

    public int getSize() {
        return size;
    }

    public String getStart() {
        return start;
    }

    public String getBar() {
        return bar;
    }

    public String getPointer() {
        return pointer;
    }

    public String getOffset() {
        return offset;
    }

    public String getEnd() {
        return end;
    }

    public String getPointerOffset() {
        return pointerOffset;
    }

    public String getTitle() {
        return title;
    }
}


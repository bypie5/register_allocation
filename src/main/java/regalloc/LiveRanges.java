package regalloc;

import java.util.List;

public class LiveRanges {
    List<LiveRange> ranges;

    public LiveRanges(List<LiveRange> ranges) {
        this.ranges = ranges;
    }

    public LiveRange getRange(int i) {
        return ranges.get(i);
    }

    public int size() {
        return ranges.size();
    }

    public void print() {
        for (LiveRange lr : ranges) {
            lr.print();
        }
    }

    public void sortIncreaseStartPoint() {
        ranges.sort((o1, o2) -> o1.start < o2.start ? -1 : 1);
    }

    public List<LiveRange> getRanges() {
        return this.ranges;
    }
}

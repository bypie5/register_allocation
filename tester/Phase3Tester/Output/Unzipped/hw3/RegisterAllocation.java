

/*
 * Algorithm based on MASSIMILIANO POLETTO & VIVEK SARKAR Linear Scan Register Allocation
 * http://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
 */

import cs132.vapor.ast.VFunction;

import java.util.*;

public class RegisterAllocation {

    // General Purpose Registers
    // $s0...$s7 are callee-saved
    // $t0...$t8 are caller-saved
    public static String[] GPRs =
            {"$s0","$s1","$s2","$s3","$s4","$s5","$s6","$s7",
             "$t0","$t1","$t2","$t3","$t4","$t5","$t6","$t7","$t8"};

    VFunction currFunction;
    LiveRanges ranges;

    List<String> freePool;
    List<LiveRange> active;

    public RegisterAllocation(VFunction c, LiveRanges r) {
        currFunction = c;
        ranges = r;

        // All registers are available initially
        freePool = new ArrayList<>();
        freePool.addAll(Arrays.asList(GPRs));
        active = new ArrayList<>();
    }

    public void LinearScanRegisterAllocation() {
        ranges.sortIncreaseStartPoint();
        for (LiveRange i : ranges.getRanges()) {
            ExpireOldIntervals(i);
            if (active.size() == GPRs.length) {
                SpillAtInterval(i);
            } else {

            }
        }
    }

    public void ExpireOldIntervals(LiveRange i) {

    }

    public void SpillAtInterval(LiveRange i) {

    }

    void sortByIncreasingEndPoint(List<LiveRange> ranges) {
        Collections.sort(ranges, (o1, o2) -> o1.start < o2.start ? -1 : 1);
    }
}

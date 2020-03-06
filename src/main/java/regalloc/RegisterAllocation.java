package regalloc;

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
    int stackLoc;

    public RegisterAllocation(VFunction c, LiveRanges r) {
        currFunction = c;
        ranges = r;

        // All registers are available initially
        freePool = new ArrayList<>();
        freePool.addAll(Arrays.asList(GPRs));
        active = new ArrayList<>();
        stackLoc = 0;
    }

    public void print() {
        for (LiveRange lr : ranges.getRanges()) {
            lr.print();
        }
    }

    public void LinearScanRegisterAllocation() {
        ranges.sortIncreaseStartPoint();
        for (LiveRange i : ranges.getRanges()) {
            ExpireOldIntervals(i);
            if (active.size() == GPRs.length) {
                SpillAtInterval(i);
            } else {
                i.register = getRegFromPool();
                active.add(i);
                sortByIncreasingEndPoint(active);
            }
        }
    }

    public void ExpireOldIntervals(LiveRange i) {
        sortByIncreasingEndPoint(active);
        List<LiveRange> toRemove = new ArrayList<>();
        for (LiveRange j : active) {
            if (j.end >= i.start) {
                return;
            }
            addRegToPool(j.register);
            toRemove.add(j);
        }

        for (LiveRange r : toRemove) {
            active.remove(r);
        }
    }

    public void SpillAtInterval(LiveRange i) {
        sortByIncreasingEndPoint(active);
        LiveRange spill = active.get(active.size()-1);
        if (spill.end > i.end) {
            i.register = spill.register;
            spill.location = getNewStackLoc();
            active.remove(spill);
            active.add(i);
            sortByIncreasingEndPoint(active);
        } else {
            i.location = getNewStackLoc();
        }
    }

    // TODO: Verify this works
    void sortByIncreasingEndPoint(List<LiveRange> ranges) {
        Collections.sort(ranges, (o1, o2) -> o1.start > o2.start ? -1 : 1);
    }

    String getRegFromPool() {
        String reg = freePool.get(0);
        freePool.remove(0);
        return reg;
    }

    int getOrgPos(String reg) {
        int i = 0;
        for (String r : GPRs) {
            if (r.equals(reg))
                return i;
            i++;
        }

        return -1;
    }

    void addRegToPool(String reg) {
        freePool.add(reg);
        freePool.sort((o1, o2) -> getOrgPos(o1) < getOrgPos(o2) ? -1 : 1);
        for (String r : freePool) {
            System.out.print(r);
        }
        System.out.println();
    }

    int getNewStackLoc() {
        stackLoc++;
        return stackLoc;
    }
}



import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.List;

public class V2VM {
    public static void main(String[] args) {
        allocRegs();
    }

    public static void allocRegs() {
        try {
            VaporProgram tree = ParseVapor.parseVapor(System.in, System.err);
            LiveRangeVisitor<Exception> rangeVisitor = new LiveRangeVisitor();

            // One set of ranges per function
            List<LiveRanges> ranges = new ArrayList<>();
            for (int i = 0; i < tree.functions.length; i++) {
                VFunction currFunc = tree.functions[i];
                rangeVisitor.setCurrFunction(currFunc);

                for (int j = 0; j < currFunc.body.length; j++) {
                    tree.functions[i].body[j].accept(rangeVisitor);
                }

                ranges.add(rangeVisitor.getCurrRanges());
                rangeVisitor.inspect();
            }

            // For each function use LSRA to allocate registers
            List<RegisterAllocation> allocations = new ArrayList<>();
            for (int i = 0; i < tree.functions.length; i++) {
                RegisterAllocation currAlloc = new RegisterAllocation(tree.functions[i], ranges.get(i));
                currAlloc.LinearScanRegisterAllocation();
                allocations.add(currAlloc);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }
}

package regalloc;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.List;

public class V2VM {
    public static void main(String[] args) {
        try {
            VaporProgram tree = ParseVapor.parseVapor(System.in, System.err);
            LiveRangeVisitor<Exception> rangeVisitor = new LiveRangeVisitor();

            // For each function compute live range
            for (int i = 0; i < tree.functions.length; i++) {
                VFunction currFunc = tree.functions[i];
                rangeVisitor.setCurrFunction(currFunc);

                for (int j = 0; j < currFunc.body.length; j++) {
                    tree.functions[i].body[j].accept(rangeVisitor);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

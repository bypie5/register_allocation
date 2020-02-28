package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.List;
import java.util.ArrayList;

public class LiveRangeVisitor <E extends Throwable> extends Visitor<E> {

    VFunction currFunction;
    int currNodeIndex;
    List<CFGNode> nodes;

    public void setCurrFunction(VFunction currFunction) {
        this.currFunction = currFunction;
        currNodeIndex = 0;
        nodes = new ArrayList<>();
    }

    public void visit(VAssign a) throws E {
    }

    public void visit(VCall c) throws E {
    }

    public void visit(VBuiltIn c) throws E {
    }

    public void visit(VMemWrite w) throws E {
    }

    public void visit(VMemRead r) throws E {
    }

    public void visit(VBranch b) throws E {
    }

    public void visit(VGoto g) throws E {
    }

    public void visit(VReturn r) throws E {
    }

    class VariableSet {
        List<String> elements;

        public VariableSet () {
            elements = new ArrayList<>();
        }
    }

    class CFGNode {
        VariableSet in;
        VariableSet out;
        VariableSet def;

        public CFGNode() {
            in = new VariableSet();
            out = new VariableSet();
            def = new VariableSet();
        }
    }
}

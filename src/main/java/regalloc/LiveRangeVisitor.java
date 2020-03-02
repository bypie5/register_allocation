package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.List;
import java.util.ArrayList;

public class LiveRangeVisitor <E extends Throwable> extends Visitor<E> {

    VFunction currFunction;
    int currNodeIndex;
    List<CFGNode> nodes; // Each line is a node

    public void setCurrFunction(VFunction currFunction) {
        this.currFunction = currFunction;
        currNodeIndex = 0;
        nodes = new ArrayList<>();
    }

    // TODO: Make this return something useful
    public LiveRange getCurrRanges() {
        return new LiveRange();
    }

    public void visit(VAssign a) throws E {
        CFGNode currNode = new CFGNode();

        // defs
        currNode.def.addElement(a.dest.toString());

        // uses
        if (a.source instanceof VVarRef) {
            currNode.use.addElement(a.source.toString());
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VCall c) throws E {
        CFGNode currNode = new CFGNode();

        // defs
        currNode.def.addElement(c.dest.toString());

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VBuiltIn c) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VMemWrite w) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses
        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VMemRead r) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VBranch b) throws E {
        CFGNode currNode = new CFGNode();

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VGoto g) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VReturn r) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    class VariableSet {
        List<String> elements;

        public VariableSet () {
            elements = new ArrayList<>();
        }

        public void addElement(String key) {
            elements.add(key);
        }

        public List<String> getElements() {
            return this.elements;
        }
    }

    class CFGNode {
        public VariableSet in;
        public VariableSet out;
        public VariableSet def;
        public VariableSet use;

        public CFGNode() {
            in = new VariableSet();
            out = new VariableSet();
            def = new VariableSet();
            use = new VariableSet();
        }
    }
}

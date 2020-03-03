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

    public void inspect() {
        System.out.println(currFunction.ident);
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println("node " + i + ": ");
            nodes.get(i).inspect();
        }
    }

    // TODO: Make this return something useful
    public LiveRange getCurrRanges() {
        LiveRange liveRange = new LiveRange();

        // TODO: Compute live ranges

        return liveRange;
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
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.addElement(c.args[i].toString());
            }
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VBuiltIn c) throws E {
        CFGNode currNode = new CFGNode();

        // defs
        if (c.dest != null) {
            currNode.def.addElement(c.dest.toString());
        }

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.addElement(c.args[i].toString());
            }
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VMemWrite w) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses
        if (w.dest instanceof VMemRef.Global) {
            if (((VMemRef.Global) w.dest).base instanceof VAddr.Var) {
                currNode.use.addElement(((VMemRef.Global) w.dest).base.toString());
            }
        }

        if (w.source instanceof VVarRef) {
            currNode.use.addElement(w.source.toString());
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VMemRead r) throws E {
        CFGNode currNode = new CFGNode();

        // defs
        currNode.def.addElement(r.dest.toString());

        // uses
        if (r.source instanceof VMemRef.Global) {
            if (((VMemRef.Global) r.source).base instanceof VAddr.Var) {
                currNode.use.addElement(((VMemRef.Global) r.source).base.toString());
            }
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VBranch b) throws E {
        CFGNode currNode = new CFGNode();

        // defs

        // uses
        currNode.use.addElement(b.value.toString());

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
        if (r.value instanceof VVarRef) {
            currNode.use.addElement(r.value.toString());
        }

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

        public String inspect() {
            String val = "";
            for (int i = 0; i < elements.size(); i++) {
                if (i == 0)
                    val += elements.get(i);
                else
                    val += elements.get(i) + " ";
            }
            return val;
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

        public void inspect() {
            System.out.println("    in:  {"  + in.inspect() + "}");
            System.out.println("    out: {" + out.inspect() + "}");
            System.out.println("    def: {" + def.inspect() + "}");
            System.out.println("    use: {" + use.inspect() + "}");
        }
    }
}

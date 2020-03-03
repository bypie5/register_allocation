package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.*;

public class LiveRangeVisitor <E extends Throwable> extends Visitor<E> {

    VFunction currFunction;
    int currNodeIndex;
    List<CFGNode> nodes; // Each line is a node

    public void setCurrFunction(VFunction currFunction) {
        this.currFunction = currFunction;
        currNodeIndex = 0;
        nodes = new ArrayList<>();
    }

    // For debugging
    public void inspect() {
        System.out.println(currFunction.ident);
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println("node " + nodes.get(i).index + ": ");
            nodes.get(i).inspect();
        }
    }

    // Computes live ranges on CFGNodes in nodes list
    public void computeLiveRanges() {
        do {
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).inPrime.assign(nodes.get(i).in);
                nodes.get(i).outPrime.assign(nodes.get(i).out);
                nodes.get(i).in.assign(nodes.get(i).use.union(nodes.get(i).out.diff(nodes.get(i).def)));
            }
        } while(converged());
    }

    boolean converged() {
        return false;
    }

    // Creates data structure to be used later
    public LiveRange getCurrRanges() {
        LiveRange liveRange = new LiveRange();

        computeLiveRanges();

        // TODO: Feed CFGNode data into liveRange

        return liveRange;
    }

    public void visit(VAssign a) throws E {
        CFGNode currNode = new CFGNode(currNodeIndex);

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
        CFGNode currNode = new CFGNode(currNodeIndex);

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
        CFGNode currNode = new CFGNode(currNodeIndex);

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
        CFGNode currNode = new CFGNode(currNodeIndex);

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
        CFGNode currNode = new CFGNode(currNodeIndex);

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
        CFGNode currNode = new CFGNode(currNodeIndex);

        // defs

        // uses
        currNode.use.addElement(b.value.toString());

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VGoto g) throws E {
        CFGNode currNode = new CFGNode(currNodeIndex);

        // defs

        // uses

        nodes.add(currNode);
        currNodeIndex++;
    }

    public void visit(VReturn r) throws E {
        CFGNode currNode = new CFGNode(currNodeIndex);

        // defs

        // uses
        if (r.value instanceof VVarRef) {
            currNode.use.addElement(r.value.toString());
        }

        nodes.add(currNode);
        currNodeIndex++;
    }

    class VariableSet {
        Set<String> elements;

        public VariableSet () {
            elements = new HashSet<>();
        }

        public void addElement(String key) {
            elements.add(key);
        }

        public boolean equals(VariableSet comp) {
            return this.elements.equals(comp);
        }

        public VariableSet diff(VariableSet rhs) {
            VariableSet d = this;
            d.elements.removeAll(rhs.elements);

            return d;
        }

        public VariableSet union(VariableSet rhs) {
            VariableSet u = this;
            u.elements.addAll(rhs.elements);
            return u;
        }

        public void assign(VariableSet a) {
            this.elements.clear();
            this.elements.addAll(a.elements);
        }

        public String inspect() {
            String val = "";
            int count = 0;
            Iterator<String> el = elements.iterator();
            while(el.hasNext()) {
                if (count == 0)
                    val += el.next();
                else
                    val += el.next() + " ";
                count++;
            }
            return val;
        }
    }

    class CFGNode {
        public int index;
        public VariableSet in;
        public VariableSet out;
        public VariableSet def;
        public VariableSet use;
        public VariableSet inPrime;
        public VariableSet outPrime;

        List<Integer> succ;

        public CFGNode(int index) {
            this.index = index;
            in = new VariableSet();
            out = new VariableSet();
            def = new VariableSet();
            use = new VariableSet();
            inPrime = new VariableSet();
            outPrime = new VariableSet();
            succ = new ArrayList<>();
        }

        public void inspect() {
            System.out.println("    in:  {"  + in.inspect() + "}");
            System.out.println("    out: {" + out.inspect() + "}");
            System.out.println("    def: {" + def.inspect() + "}");
            System.out.println("    use: {" + use.inspect() + "}");
        }
    }
}

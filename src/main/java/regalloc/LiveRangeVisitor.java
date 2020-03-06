package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

import java.util.*;

public class LiveRangeVisitor <E extends Throwable> extends Visitor<E> {

    VFunction currFunction;
    List<CFGNode> nodes; // Each line is a node

    public void setCurrFunction(VFunction currFunction) {
        this.currFunction = currFunction;
        nodes = new ArrayList<>();
    }

    // For debugging
    public void inspect() {
        System.out.println(currFunction.ident);
        for (CFGNode node : nodes) {
            System.out.println("node " + node.index + ": ");
            node.inspect();
        }
    }

    public int getRelativePos(int sourcePos) {
        return (sourcePos - currFunction.sourcePos.line) - 1;
    }

    public void printCFG() {
        for (CFGNode node : nodes) {
            for (int succ : node.succ) {
                System.out.println(node.index + " -> " + succ);
            }
        }
    }

    public VariableSet diff(VariableSet lhs, VariableSet rhs) {
        VariableSet d = new VariableSet();
        d.elements = new HashSet<>(lhs.elements);
        d.elements.remove(rhs.elements);

        return d;
    }

    public VariableSet union(VariableSet lhs, VariableSet rhs) {
        VariableSet u = new VariableSet();
        u.elements = new HashSet<>(lhs.elements);
        u.elements.addAll(rhs.elements);

        return u;
    }

    public CFGNode getNodeFromIndex(int index) {
        for (CFGNode node : nodes) {
            if (node.index == index)
                return node;
        }

        return null;
    }

    public void cleanUpCFG() {
        List<Integer> actualNodes = new ArrayList<>();
        for (CFGNode node : nodes) {
            actualNodes.add(node.index);
        }

        // Make sure successors point to an actual node in nodes
        for (CFGNode node : nodes) {
            for (int i = 0; i < node.succ.size(); i++) {
                // Round up curr succ to nearest node in actualNodes
                if (!actualNodes.contains(node.succ.get(i))) {
                    for (int an : actualNodes) {
                        if (an > node.succ.get(i)) {
                            node.succ.set(i, an);
                            break;
                        }
                    }
                }
            }
        }
    }

    // Computes live ranges on CFGNodes in nodes list
    public void computeLiveRanges() {
        cleanUpCFG();

        do {
            for (int i = 0; i < nodes.size(); i++) {
                CFGNode currNode = nodes.get(i);

                currNode.inPrime.elements = nodes.get(i).in.elements;
                currNode.outPrime.elements = nodes.get(i).out.elements;

                currNode.in = union(currNode.use, diff(currNode.out, currNode.def));
                for (int j = 0; j < currNode.succ.size(); j++) {
                    currNode.out = union(currNode.out, getNodeFromIndex(currNode.succ.get(j)).in);
                }
            }
        } while(!converged());
    }

    boolean converged() {
        for (CFGNode node : nodes) {
            if (!node.inPrime.equals(node.in) || !node.outPrime.equals(node.out))
                return false;
        }
        return true;
    }

    // Creates data structure to be used later
    public LiveRange getCurrRanges() {
        LiveRange liveRange = new LiveRange();

        computeLiveRanges();
        // TODO: Feed CFGNode data into liveRange

        return liveRange;
    }

    public void visit(VAssign a) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(a.sourcePos.line));

        // defs
        currNode.def.addElement(a.dest.toString());

        // uses
        if (a.source instanceof VVarRef) {
            currNode.use.addElement(a.source.toString());
        }

        currNode.addSingleSucc(getRelativePos(a.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VCall c) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(c.sourcePos.line));

        // defs
        currNode.def.addElement(c.dest.toString());

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.addElement(c.args[i].toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(c.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VBuiltIn c) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(c.sourcePos.line));

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

        currNode.addSingleSucc(getRelativePos(c.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VMemWrite w) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(w.sourcePos.line));

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

        currNode.addSingleSucc(getRelativePos(w.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VMemRead r) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(r.sourcePos.line));

        // defs
        currNode.def.addElement(r.dest.toString());

        // uses
        if (r.source instanceof VMemRef.Global) {
            if (((VMemRef.Global) r.source).base instanceof VAddr.Var) {
                currNode.use.addElement(((VMemRef.Global) r.source).base.toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(r.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VBranch b) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(b.sourcePos.line));

        // defs

        // uses
        currNode.use.addElement(b.value.toString());

        currNode.addSingleSucc(getRelativePos(b.sourcePos.line));
        int targetPos = b.target.getTarget().sourcePos.line;
        currNode.succ.add(getRelativePos(targetPos));

        nodes.add(currNode);
    }

    public void visit(VGoto g) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(g.sourcePos.line));

        // defs

        // uses

        int targetPos = ((VAddr.Label)g.target).label.getTarget().sourcePos.line;
        currNode.succ.add(getRelativePos(targetPos));

        nodes.add(currNode);
    }

    public void visit(VReturn r) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(r.sourcePos.line));

        // defs

        // uses
        if (r.value instanceof VVarRef) {
            currNode.use.addElement(r.value.toString());
        }

        // Does not have any successors

        nodes.add(currNode);
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
            return this.elements.equals(comp.elements);
        }

        public void copy(VariableSet a) {
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
                    val += " " + el.next();
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
            System.out.println("    in:   {"  + in.inspect() + "}");
            System.out.println("    out:  {" + out.inspect() + "}");
            //System.out.println("    in':  {"  + inPrime.inspect() + "}");
            //System.out.println("    out': {" + outPrime.inspect() + "}");
            System.out.println("    def:  {" + def.inspect() + "}");
            System.out.println("    use:  {" + use.inspect() + "}");
        }

        public void addSingleSucc(int pos) {
            succ.add(pos + 1);
        }
    }
}

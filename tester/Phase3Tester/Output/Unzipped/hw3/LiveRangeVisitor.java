

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
    public void computeNodeSets() {

        cleanUpCFG();

        do {
            for (int i = 0; i < nodes.size(); i++) {
                CFGNode currNode = nodes.get(i);

                currNode.inPrime = new TreeSet<>();
                currNode.inPrime.addAll(currNode.in);
                currNode.outPrime = new TreeSet<>();
                currNode.outPrime.addAll(currNode.out);

                // diff
                Set<String> diff = new TreeSet<>(currNode.out);
                diff.removeAll(currNode.def);
                // union
                Set<String> newIn = new TreeSet<>(currNode.use);
                newIn.addAll(diff);
                currNode.in = new TreeSet<>();
                currNode.in.addAll(newIn);

                for (int j = 0; j < currNode.succ.size(); j++) {
                    Set<String> newOut = new TreeSet<>(currNode.out);
                    newOut.addAll(getNodeFromIndex(currNode.succ.get(j)).in);
                    currNode.out = new TreeSet<>();
                    currNode.out.addAll(newOut);
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

    LiveRange getRangeByIdent(String ident, List<LiveRange> ranges) {
        for (LiveRange range : ranges) {
            if (range.ident.equals(ident)) {
                return range;
            }
        }

        return null;
    }

    // Creates data structure to be used later
    public LiveRanges getCurrRanges() {
        List<LiveRange> finalRanges = new ArrayList<>();
        List<LiveRange> incompleteRanges = new ArrayList<>();

        computeNodeSets();

        // active[n] = in[n] union def[n]
        for (CFGNode node : nodes) {
            node.active = new TreeSet<>(node.in);
            node.active.addAll(node.def);
        }

        for (CFGNode node : nodes) {
            // Create or extend ranges
            for(String ident : node.active) {
                LiveRange temp = getRangeByIdent(ident, incompleteRanges);
                if (temp == null) {
                    incompleteRanges.add(new LiveRange(node.index, node.index, ident));
                } else {
                    temp.end++;
                }
            }

            // Add a terminated range to finalRanges
            for (int i = 0; i < incompleteRanges.size(); i++) {
                if (!node.active.contains(incompleteRanges.get(i).ident)) {
                    finalRanges.add(incompleteRanges.get(i));
                    incompleteRanges.remove(i);
                }
            }
        }

        // Add nodes that are active until the end
        for (LiveRange r : incompleteRanges) {
            r.end++;
            finalRanges.add(r);
        }

        return new LiveRanges(finalRanges);
    }

    public void visit(VAssign a) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(a.sourcePos.line));

        // defs
        currNode.def.add(a.dest.toString());

        // uses
        if (a.source instanceof VVarRef) {
            currNode.use.add(a.source.toString());
        }

        currNode.addSingleSucc(getRelativePos(a.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VCall c) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(c.sourcePos.line));

        // defs
        currNode.def.add(c.dest.toString());

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.add(c.args[i].toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(c.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VBuiltIn c) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(c.sourcePos.line));

        // defs
        if (c.dest != null) {
            currNode.def.add(c.dest.toString());
        }

        // uses
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                currNode.use.add(c.args[i].toString());
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
                currNode.use.add(((VMemRef.Global) w.dest).base.toString());
            }
        }

        if (w.source instanceof VVarRef) {
            currNode.use.add(w.source.toString());
        }

        currNode.addSingleSucc(getRelativePos(w.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VMemRead r) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(r.sourcePos.line));

        // defs
        currNode.def.add(r.dest.toString());

        // uses
        if (r.source instanceof VMemRef.Global) {
            if (((VMemRef.Global) r.source).base instanceof VAddr.Var) {
                currNode.use.add(((VMemRef.Global) r.source).base.toString());
            }
        }

        currNode.addSingleSucc(getRelativePos(r.sourcePos.line));

        nodes.add(currNode);
    }

    public void visit(VBranch b) throws E {
        CFGNode currNode = new CFGNode(getRelativePos(b.sourcePos.line));

        // defs

        // uses
        currNode.use.add(b.value.toString());

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
            currNode.use.add(r.value.toString());
        }

        // Does not have any successors

        nodes.add(currNode);
    }

    class CFGNode {
        public int index;
        public Set<String> in;
        public Set<String> out;
        public Set<String> def;
        public Set<String> use;
        public Set<String> inPrime;
        public Set<String> outPrime;
        public Set<String> active;

        List<Integer> succ;

        public CFGNode(int index) {
            this.index = index;
            in = new TreeSet();
            out = new TreeSet();
            def = new TreeSet();
            use = new TreeSet();
            inPrime = new TreeSet();
            outPrime = new TreeSet();
            active = new TreeSet<>();
            succ = new ArrayList<>();
        }

        public void inspect() {
            /*System.out.print("    in:    {");
            for (String s : in) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    out:   {");
            for (String s : out) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    use:   {");
            for (String s : use) {
                System.out.print(s);
            }
            System.out.println("}");

            System.out.print("    def:   {");
            for (String s : def) {
                System.out.print(s);
            }
            System.out.println("}");*/
            System.out.print("    active:   {");
            for (String s : active) {
                System.out.print(s);
            }
            System.out.println("}");
        }

        public void addSingleSucc(int pos) {
            succ.add(pos + 1);
        }
    }
}

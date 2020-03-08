package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr;

import java.util.List;
import java.util.ArrayList;

public class TranslationVisitor <E extends Throwable> extends VInstr.Visitor<E> {

    RegisterAllocation currAllocation;
    VFunction currFunction;

    List<String> buffer;
    int indentLevel;

    public TranslationVisitor() {
        buffer = new ArrayList<>();
    }

    public void appendBuffer(String str) {
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = 0; i < indentLevel * 4; i++)
            strBuilder.insert(0, " ");
        str = strBuilder.toString();

        if (str.charAt(str.length() - 1) != '\n')
            buffer.add(str + "\n");
        else
            buffer.add(str);
    }

    public void printBuffer() {
        for (String line : buffer) {
            System.out.print(line);
        }
        System.out.println();
    }

    public void insertLabels() {
        for (int i = 0; i < currFunction.labels.length; i++) {
            int pos = getRelativePos(currFunction.labels[i].sourcePos.line);
            // TODO: I don't know if this is right
            buffer.add(pos + 1, currFunction.labels[i].ident + ":\n");
        }
    }

    void increaseIndent() {
        //indentLevel++;
    }

    void decreaseIndent() {
        //indentLevel--;
    }

    public void setData(VFunction currFunction, RegisterAllocation currAllocation) {
        this.currFunction = currFunction;
        this.currAllocation = currAllocation;

        indentLevel = 0;
        appendBuffer("func " + currFunction.ident + " [in 0, out 0, local 0]");
        increaseIndent();
    }

    public int getRelativePos(int sourcePos) {
        return (sourcePos - currFunction.sourcePos.line) - 1;
    }

    public void visit(VAssign a) throws E {
        int sourcePos = getRelativePos(a.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, a.dest.toString());
        LiveRange srcAlloc = currAllocation.getAlloc(sourcePos, a.source.toString());

        String line = "";
        if (destAlloc != null) {
            line += destAlloc.getLoc();
        }

        line += " = ";

        if (srcAlloc != null) {
            line += srcAlloc.getLoc();
        } else {
            line += a.source.toString();
        }

        appendBuffer(line);
    }

    public void visit(VCall c) throws E {
        int sourcePos = getRelativePos(c.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, c.dest.toString());

        String line = "";
        if (destAlloc != null) {
            line += destAlloc.getLoc();
        }

        line += " = ";

        // TODO: Use allocation registers
        //c.

        appendBuffer(line);
    }

    public void visit(VBuiltIn c) throws E {
        int sourcePos = getRelativePos(c.sourcePos.line);

        String line = "";

        // Sometimes BuiltIn does not have a dest
        if (c.dest != null) {
            LiveRange destAlloc = currAllocation.getAlloc(sourcePos, c.dest.toString());

            if (destAlloc != null) {
                line += destAlloc.getLoc();
            }

            line += " = ";
        }

        line += c.op.name + "(";

        for (int i = 0; i < c.args.length; i++) {
            if (i != 0) line += " ";
            if (c.args[i] instanceof VVarRef) {
                LiveRange currArgAlloc = currAllocation.getAlloc(sourcePos, c.args[i].toString());
                if (currArgAlloc != null)
                    line += currArgAlloc.getLoc();
            } else if (c.args[i] instanceof VOperand.Static) {
                line += c.args[i].toString();
            }
        }

        line += ")";

        appendBuffer(line);
    }

    public void visit(VMemWrite w) throws E {
        int sourcePos = getRelativePos(w.sourcePos.line);
    }

    public void visit(VMemRead r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);
    }

    public void visit(VBranch b) throws E {
        int sourcePos = getRelativePos(b.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, b.value.toString());

        String line = "";
        if (destAlloc != null) {
            line += "if " + destAlloc.getLoc() + " goto :" + b.target.ident;
        }

        appendBuffer(line);
    }

    public void visit(VGoto g) throws E {
        int sourcePos = getRelativePos(g.sourcePos.line);


        appendBuffer("goto " + g.target.toString());
    }

    public void visit(VReturn r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);

        appendBuffer("ret");

        // Since this is the end of the function, insert
        // CF labels.
        insertLabels();
    }
}

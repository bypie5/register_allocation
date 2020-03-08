package regalloc;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr;

import java.util.ArrayList;

public class TranslationVisitor <E extends Throwable> extends VInstr.Visitor<E> {

    RegisterAllocation currAllocation;
    VFunction currFunction;

    public void setData(VFunction currFunction, RegisterAllocation currAllocation) {
        this.currFunction = currFunction;
        this.currAllocation = currAllocation;
    }

    public int getRelativePos(int sourcePos) {
        return (sourcePos - currFunction.sourcePos.line) - 1;
    }

    public void visit(VAssign a) throws E {
        int sourcePos = getRelativePos(a.sourcePos.line);
    }

    public void visit(VCall c) throws E {
        int sourcePos = getRelativePos(c.sourcePos.line);
    }

    public void visit(VBuiltIn c) throws E {
        int sourcePos = getRelativePos(c.sourcePos.line);
    }

    public void visit(VMemWrite w) throws E {
        int sourcePos = getRelativePos(w.sourcePos.line);
    }

    public void visit(VMemRead r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);
    }

    public void visit(VBranch b) throws E {
        int sourcePos = getRelativePos(b.sourcePos.line);
    }

    public void visit(VGoto g) throws E {
        int sourcePos = getRelativePos(g.sourcePos.line);
    }

    public void visit(VReturn r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);
    }
}

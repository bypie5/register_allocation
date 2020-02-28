

import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

public class VaporVisitor<E extends Throwable> extends Visitor<E> {
    public void visit(VAssign a) throws E {
        System.out.println(a.source.toString());

    }

    public void visit(VCall c) throws E {
        System.out.println(c.sourcePos);
    }

    public void visit(VBuiltIn c) throws E {
        System.out.println(c.sourcePos);
    }

    public void visit(VMemWrite w) throws E {
        System.out.println(w.sourcePos);
    }

    public void visit(VMemRead r) throws E {
        System.out.println(r.sourcePos);
    }

    public void visit(VBranch b) throws E {
        System.out.println(b.sourcePos);
    }

    public void visit(VGoto g) throws E {
        System.out.println(g.sourcePos);
    }

    public void visit(VReturn r) throws E {
        System.out.println(r.sourcePos);
    }
}

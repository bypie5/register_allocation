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

    public void setBuffer(int pos, String str) {
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = 0; i < indentLevel * 4; i++)
            strBuilder.insert(0, " ");
        str = strBuilder.toString();

        if (str.charAt(str.length() - 1) != '\n')
            buffer.set(pos, str + "\n");
        else
            buffer.set(pos, str);
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
            setBuffer(pos,currFunction.labels[i].ident + ":\n");
        }

        // Set up function header
        buffer.add(0, "func " + currFunction.ident + " [in 0, out 0, local 0]\n");
    }

    public void setData(VFunction currFunction, RegisterAllocation currAllocation) {
        this.currFunction = currFunction;
        this.currAllocation = currAllocation;

        // Set up buffer
        buffer = new ArrayList<>();
        for (int i = 0; i <= currFunction.body.length + currFunction.labels.length; i++) {
            buffer.add("");
        }

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

        setBuffer(sourcePos, line);
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

        setBuffer(sourcePos, line);
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
            } else if (c.args[i] instanceof VLitStr) {
                line += "\""+ ((VLitStr)c.args[i]).value + "\"";
            }
        }

        line += ")";

        setBuffer(sourcePos, line);
    }

    public void visit(VMemWrite w) throws E {
        int sourcePos = getRelativePos(w.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, ((VMemRef.Global) w.dest).base.toString());
        LiveRange srcAlloc = currAllocation.getAlloc(sourcePos, w.source.toString());

        String line = "";
        if (destAlloc != null) {
            line += "[" + destAlloc.getLoc() + "+" + ((VMemRef.Global) w.dest).byteOffset + "]";
        }

        line += " = ";

        if (srcAlloc != null) {
            line += srcAlloc.getLoc();
        } else {
            line += w.source.toString();
        }

        setBuffer(sourcePos, line);
    }

    public void visit(VMemRead r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, r.dest.toString());
        LiveRange srcAlloc = currAllocation.getAlloc(sourcePos, ((VMemRef.Global) r.source).base.toString());

        String line = "";
        if (destAlloc != null) {
            line += destAlloc.getLoc();
        }

        line += " = ";

        if (srcAlloc != null) {
            line += "[" + srcAlloc.getLoc() + "+" + ((VMemRef.Global) r.source).byteOffset + "]";
        } else {
            line += "[" +r.source.toString() + "]";
        }

        setBuffer(sourcePos, line);
    }

    public void visit(VBranch b) throws E {
        int sourcePos = getRelativePos(b.sourcePos.line);
        LiveRange destAlloc = currAllocation.getAlloc(sourcePos, b.value.toString());

        String line = "";
        if (destAlloc != null) {
            line += "if " + destAlloc.getLoc() + " goto :" + b.target.ident;
        }

        setBuffer(sourcePos, line);
    }

    public void visit(VGoto g) throws E {
        int sourcePos = getRelativePos(g.sourcePos.line);


        setBuffer(sourcePos, "goto " + g.target.toString());
    }

    public void visit(VReturn r) throws E {
        int sourcePos = getRelativePos(r.sourcePos.line);

        setBuffer(sourcePos, "ret");

        // Since this is the end of the function, insert
        // CF labels. This also set function header
        insertLabels();
    }
}

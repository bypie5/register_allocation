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
    List<String> usedSXRegs;

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
        StringBuilder funcHeader = new StringBuilder();


        int sxCount = 0;
        // Save all $sx registers
        sxCount = usedSXRegs.size();
        funcHeader = new StringBuilder("func " + currFunction.ident + " [in 0, out 0, local " + sxCount + "]\n");
        // Save $sx registers
        int stackLoc = 0;
        for (String reg : usedSXRegs) {
            funcHeader.append("local[").append(stackLoc).append("] = ").append(reg).append("\n");
            stackLoc++;
        }

        // Unload argument registers onto local variables
        for (int i = 0; i < currFunction.params.length; i++) {
            LiveRange currArg = currAllocation.getAlloc(-1, currFunction.params[i].ident);
            if (currArg != null)
                funcHeader.append(currArg.getLoc()).append(" = ").append("$a").append(i).append("\n");
        }

        buffer.add(0, funcHeader.toString());
    }

    public void setData(VFunction currFunction, RegisterAllocation currAllocation) {
        this.currFunction = currFunction;
        this.currAllocation = currAllocation;

        usedSXRegs = new ArrayList<>();
        for (LiveRange lr : currAllocation.ranges.getRanges()) {
            if (lr.getLoc().contains("s")) {
                if (!usedSXRegs.contains(lr.getLoc()))
                    usedSXRegs.add(lr.getLoc());
            }
        }


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

        // Set up arguments
        // TODO: Use actual argument registers
        for (int i = 0; i < c.args.length; i++) {
            if (c.args[i] instanceof VVarRef) {
                LiveRange currArgAlloc = currAllocation.getAlloc(sourcePos, c.args[i].toString());
                if (currArgAlloc != null)
                    line += "$a" + i + " = " + currArgAlloc.getLoc() + "\n";
            } else if (c.args[i] instanceof VOperand.Static) {
                line += "$a" + i + " = " + c.args[i].toString() + "\n";
            } else if (c.args[i] instanceof VLitStr) {
                line += "\""+ ((VLitStr)c.args[i]).value + "\"";
            }
        }

        LiveRange addrAlloc = currAllocation.getAlloc(sourcePos, c.addr.toString());
        line += "call " + addrAlloc.getLoc() + "\n";

        // Get the return value
        if (destAlloc != null) {
            line += destAlloc.getLoc();
        }

        line += " = $v0";

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

        StringBuilder retString = new StringBuilder();

        if (r.value != null) {
            LiveRange retAlloc = currAllocation.getAlloc(sourcePos, r.value.toString());
            if (retAlloc != null)
                retString.append("$v0 = ").append(retAlloc.getLoc()).append("\n");
        }


        // Restore used sx registers
        int count = 0;
        for (String reg : usedSXRegs) {
            retString.append(reg).append(" = local[").append(count).append("]\n");
            count++;
        }

        retString.append("ret");

        setBuffer(sourcePos, retString.toString());

        // Since this is the end of the function, insert
        // CF labels. This also set function header
        insertLabels();
    }
}

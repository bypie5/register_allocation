

import cs132.vapor.ast.VaporProgram;

public class V2VM {
    public static void main(String[] args) {
        try {
            VaporProgram tree = ParseVapor.parseVapor(System.in, System.err);
            VaporVisitor<Exception> vaporVisitor = new VaporVisitor();
            for (int i = 0; i < tree.functions.length; i++) {
                for (int j = 0; j < tree.functions[i].body.length; j++) {
                    tree.functions[i].body[j].accept(vaporVisitor);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}



import cs132.vapor.ast.VaporProgram;

public class V2VM {
    public static void main(String[] args) {
        try {
            VaporProgram tree = ParseVapor.parseVapor(System.in, System.err);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

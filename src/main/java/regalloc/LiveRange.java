package regalloc;

import java.util.Collections;

public class LiveRange {

    public int start;
    public int end;
    public String ident;

    // Used during register allocation
    public String register;
    public int location;

    public LiveRange(int start, int end, String ident) {
        this.start = start;
        this.end = end;
        this.ident = ident;

        register = null;
        location = -1;
    }

    void print() {
        System.out.println(ident + "(" + start + ", " + end + ")");
        System.out.println("    reg: " + register + " loc: " + location);
        for (int i = 0; i < start; i++) {
            System.out.print(".");
        }
        for (int i = start - 1; i < end; i++) {
            System.out.print("+");
        }
        System.out.println();
    }

    public int compareStartTo(LiveRange lhs) {
        return this.start - lhs.start;
    }
}

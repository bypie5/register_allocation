package regalloc;

public class LiveRange {

    public int start;
    public int end;
    public String ident;

    public LiveRange(int start, int end, String ident) {
        this.start = start;
        this.end = end;
        this.ident = ident;
    }

    void print() {
        System.out.println(ident + "(" + start + ", " + end + ")");
        for (int i = 0; i < start; i++) {
            System.out.print(".");
        }
        for (int i = start - 1; i < end; i++) {
            System.out.print("+");
        }
        System.out.println();
    }
}

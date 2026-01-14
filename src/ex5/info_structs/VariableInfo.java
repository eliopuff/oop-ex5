package ex5.info_structs;

public class VariableInfo {
    public static final String FINAL = "final ";
    public static final String EMPTY = "";
    public static final String ASSIGNED = "assigned";
    public static final String UNASSIGNED = "unassigned";
    public static final String SPACE = " ";
    public static final String COMMA = ", ";
    private final String name;
    private final String type;
    private final boolean isFinal;
    private boolean didAssign;

    public VariableInfo(String name, String type, boolean isFinal, boolean didAssign) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.didAssign = didAssign;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isAssigned() {
        return didAssign;
    }

    public void setAssigned(boolean didAssign) {
        this.didAssign = didAssign;
    }

    @Override
    public String toString() {
        String finalStr = isFinal ? FINAL : EMPTY;
        String assignStr = didAssign ? ASSIGNED : UNASSIGNED;
        return finalStr + type + SPACE + name + COMMA + assignStr;
    }
}

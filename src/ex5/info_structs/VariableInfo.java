package ex5.info_structs;

public class VariableInfo {
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
        String finalStr = isFinal ? "final " : "";
        String assignStr = didAssign ? "assigned" : "unassigned";
        return "VariableInfo{" + finalStr + type + " " + name + ", " + assignStr + "}";
    }
}

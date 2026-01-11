package ex5.Stucts;

public class VariableInfo {
    private final String name;
    private final String type;
    private final boolean isFinal;
    private final boolean didAssign;

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

    public boolean isDidAssign() {
        return didAssign;
    }

    @Override
    public String toString() {
        return "VariableInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isFinal=" + isFinal +
                ", didAssign=" + didAssign +
                '}';
    }
}

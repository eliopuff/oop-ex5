package ex5.info_structs;


/**
 * A class representing information about a variable, including its name, type, finality, and
 * assignment status.
 * @author eliooo,sagig
 */
public class VariableInfo {
    private static final String FINAL = "final ";
    private static final String EMPTY = "";
    private static final String ASSIGNED = "assigned";
    private static final String UNASSIGNED = "unassigned";
    private static final String SPACE = " ";
    private static final String COMMA = ", ";
    private final String name;
    private final String type;
    private final boolean isFinal;
    private boolean didAssign;

    /**
     * Constructor for VariableInfo.
     * @param name The name of the variable.
     * @param type The type of the variable.
     * @param isFinal A boolean indicating if the variable is final.
     * @param didAssign A boolean indicating if the variable has been assigned a value.
     */
    public VariableInfo(String name, String type, boolean isFinal, boolean didAssign) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.didAssign = didAssign;
    }

    /** Getter for the variable name.
     * @return The name of the variable.
     */
    public String getName() {
        return name;
    }

    /** Getter for the variable type.
     * @return The type of the variable.
     */
    public String getType() {
        return type;
    }

    /** Getter for the variable finality.
     * @return A boolean indicating if the variable is final.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /** Getter for the variable assignment status.
     * @return A boolean indicating if the variable has been assigned a value.
     */
    public boolean isAssigned() {
        return didAssign;
    }

    /** Setter for the variable assignment status.
     * @param didAssign A boolean indicating if the variable has been assigned a value.
     */
    public void setAssigned(boolean didAssign) {
        this.didAssign = didAssign;
    }

    /**
     * Returns a string representation of the variable, including its finality, type, name, and
     * assignment status.
     * @return A string representing the variable.
     */
    @Override
    public String toString() {
        String finalStr = isFinal ? FINAL : EMPTY;
        String assignStr = didAssign ? ASSIGNED : UNASSIGNED;
        return finalStr + type + SPACE + name + COMMA + assignStr;
    }
}

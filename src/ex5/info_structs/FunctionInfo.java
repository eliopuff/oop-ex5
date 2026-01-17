package ex5.info_structs;

/**
 * A class representing information about a function, including its name and parameters.
 * @author eliooo,sagig
 */
public class FunctionInfo {
    private static final String COMMA = ", ";
    private static final String OPEN = "(";
    private static final String END = ")";
    private final String name;
    private final VariableInfo[] parameters;

    /**
     * Constructor for FunctionInfo.
     * @param name The name of the function.
     * @param parameters An array of VariableInfo representing the function's parameters.
     */
    public FunctionInfo(String name, VariableInfo[] parameters) {
        this.name = name;
        this.parameters = parameters.clone();
    }

    /** Getter for the function name.
     * @return The name of the function.
     */
    public String getName() {
        return name;
    }

    /** Getter for the function parameters.
     * @return An array of VariableInfo representing the function's parameters.
     */
    public VariableInfo[] getParameters() {
        return parameters.clone();
    }

    /**
     * Returns a string representation of the function, including its name and parameters.
     * @return A string representing the function.
     */
    @Override
    public String toString() {
        StringBuilder paramsStr = new StringBuilder();
        for (VariableInfo param : parameters) {
            paramsStr.append(param.toString()).append(COMMA);
        }
        if (!paramsStr.isEmpty()) {
            paramsStr.setLength(paramsStr.length() - 2); // Remove trailing comma and space
        }
        return (name + OPEN + paramsStr + END);
    }
}

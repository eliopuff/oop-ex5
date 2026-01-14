package ex5.info_structs;


public class FunctionInfo {
    public static final String COMMA = ", ";
    public static final String OPEN = "(";
    public static final String END = ")";
    private final String name;
    private final VariableInfo[] parameters;

    public FunctionInfo(String name, VariableInfo[] parameters) {
        this.name = name;
        this.parameters = parameters.clone();
    }

    public String getName() {
        return name;
    }

    public VariableInfo[] getParameters() {
        return parameters.clone();
    }

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

package ex5.Stucts;

public class FunctionInfo {
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
        StringBuilder sb = new StringBuilder();
        sb.append("Function Name: ").append(name).append("\n");
        sb.append("Parameters:\n");
        for (VariableInfo param : parameters) {
            sb.append("  - ").append(param.toString()).append("\n");
        }
        return sb.toString();
    }
}

package ex5.process;

import ex5.Stucts.FunctionInfo;
import ex5.Stucts.VariableInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessor {


    private final Pattern varDeclarationPattern;
    private final FileReader fileReader;
    private final Pattern argsPattern;
    private final Pattern finalPattern;
    private final Pattern commentPattern;
    private final Pattern intValPattern;
    private final Pattern doubleValPattern;
    private final Pattern boolValPattern;
    private final Pattern charValPattern;
    private final Pattern stringValPattern;
    private final Pattern namePattern;
    private final Pattern functionCallPattern;
    private final Pattern functionDefinitionPattern;
    private final Pattern ifWhilePattern;
    private final Pattern ifWhileConditionPattern;
    private final Pattern returnPattern;
    private final List<FunctionInfo> functions;
    private final Deque<List<VariableInfo>> variables;
    private List<String> fileContents;


    //RegEx constants
    private static final String COMMENT_PREFIX = "//";
    private static final String COMMENT = "\\s*" + COMMENT_PREFIX + ".*";
    private static final String TYPE = "(int|boolean|char|double|String)";
    private static final String NAME = "([a-zA-Z]|_[a-zA-Z0-9])\\w*";
    private static final String METHOD_DECLARATION = "\\s*void\\s+(?<name>" + NAME + ")\\s*\\([^)]*\\)" +
            "\\s*\\{";
    private static final String VARIABLE_DECLARATION = "\\s*(final\\s+)?(?<type>" + TYPE + ")\\s+"+
            "(" + NAME + "\\s*(=[^,;]*)?,\\s*)*"+NAME+"\\s*(=[^,;]*)?;\\s*";
    private static final String PARAMETER_DECLARATION = "\\s*(final\\s*)?(?<type>" + TYPE + ")\\s+(?<name>"
            + NAME + ")\\s*";
    private static final String INT_VAL = "[+-]?\\d+";
    private static final String DOUBLE_VAL = "[+-]?(\\d+\\.\\d*|\\.\\d+|\\d+)";
    private static final String BOOL_VAL = "(true|false|" + INT_VAL + "|" + DOUBLE_VAL + ")";
    private static final String CHAR_VAL = "'[^'\"\\\\,]'";
    private static final String STRING_VAL = "\"[^\"\\\\,]*\"";
    private static final String VALUE = "(" + INT_VAL + "|" + DOUBLE_VAL + "|" + BOOL_VAL + "|" +
            CHAR_VAL + "|" + STRING_VAL + ")";
    private static final String OPEN_SCOPE = "{";
    private static final String CLOSE_SCOPE = "}";
    private static final String RETURN = "\\s*return\\s*;\\s*";
    private static final String IF_WHILE = "\\s*(if|while)\\s*\\(.*\\)\\s*\\{";
    private static final String ASSIGNMENT = "\\s+(" + NAME + "\\s*=[^,;]*,\\s*)*"
            + NAME +"\\s*=[^,;]*;\\s*";
    private static final String IF_WHILE_CONDITION =
            "\\s*" + BOOL_VAL + "|" + NAME+ "|" + INT_VAL + "|" + DOUBLE_VAL + "\\s*";

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String NAME_GROUP = "name";
    private static final String TYPE_GROUP = "type";
    private static final String FINAL = "final";
    private static final String INT = "int";
    private static final String DOUBLE = "double";
    private static final String BOOLEAN = "boolean";
    private static final String CHAR = "char";
    private static final String STRING = "String";
    private static final String COMMA = ",";
    private static final String SEMICOLON = ";";
    private static final String ASSIGN_OP = "=";


    public FileProcessor(FileReader fileReader) {
        this.fileReader= fileReader;
        functions = new ArrayList<>();
        variables = new LinkedList<>();
        finalPattern = Pattern.compile(FINAL);
        commentPattern = Pattern.compile(COMMENT);
        argsPattern = Pattern.compile(PARAMETER_DECLARATION);
        intValPattern = Pattern.compile(INT_VAL);
        doubleValPattern = Pattern.compile(DOUBLE_VAL);
        boolValPattern = Pattern.compile(BOOL_VAL);
        charValPattern = Pattern.compile(CHAR_VAL);
        stringValPattern = Pattern.compile(STRING_VAL);
        namePattern = Pattern.compile(NAME);
        returnPattern = Pattern.compile(RETURN);
        ifWhilePattern = Pattern.compile(IF_WHILE);
        ifWhileConditionPattern = Pattern.compile(IF_WHILE_CONDITION);
        functionCallPattern = Pattern.compile("(?<name>"+NAME+ ")\\s*\\([^)]*\\)"); // Added pattern for
        functionDefinitionPattern = Pattern.compile(METHOD_DECLARATION);
        // function
        // calls

        varDeclarationPattern = Pattern.compile(VARIABLE_DECLARATION);
    }

    public void processFile() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        fileContents = bufferedReader.lines().toList();
        validateScopesAndColons();
        findFuncAndGlobalVars();
        System.out.println(functions);
        System.out.println(variables);
        validateLegalCode();
    }

    public void validateScopesAndColons() throws Exception {
        int scopeCount = 0;
        for (String line : fileContents) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            } else if (line.endsWith(OPEN_SCOPE)) {
                scopeCount++;
            } else if (line.endsWith(CLOSE_SCOPE)) {
                scopeCount--;
                if (scopeCount < 0) {
                    throw new Exception("Unmatched closing brace found");
                }
            } else if (line.startsWith(COMMENT_PREFIX)){
                    continue;
            } else if (!line.endsWith(SEMICOLON) &&
                    !line.endsWith(OPEN_SCOPE) && !line.endsWith(CLOSE_SCOPE) &&
                    !functionCallPattern.matcher(line).matches()) {
                throw new Exception("Missing semicolon at the end of line: " + line);
            }
        }
    }


    private void findFuncAndGlobalVars() throws Exception {
        int depth = 0;
        variables.push(new ArrayList<>());
        Pattern funcPattern = Pattern.compile(METHOD_DECLARATION);
        for (String line : fileContents) {
            line = line.trim();
            if (line.endsWith(OPEN_SCOPE)) {
                Matcher matcher = funcPattern.matcher(line);
                if (matcher.matches()) {
                    if (depth != 0) {
                        throw new Exception("Nested functions are not allowed: " + line);
                    }
                    String funcName = matcher.group(NAME_GROUP);
                    String paramsString = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
                    VariableInfo[] parameters = parseParameters(paramsString);
                    FunctionInfo functionInfo = new FunctionInfo(funcName, parameters);
                    functions.add(functionInfo);
                }
                depth++;
            } else if (line.endsWith(CLOSE_SCOPE)) {
                depth--;
            } else if (depth == 0) {
                Matcher matcher = commentPattern.matcher(line);
                if (matcher.matches())
                    continue;
                if (matchDeclaration(line)){
                    continue;
                } else if (line.isEmpty()) {
                    continue;
                }else throw new Exception("Illegal global variable declaration: " + line);
            }
        }
    }

    private VariableInfo[] parseVariables(boolean isFinal, String varType ,String paramsString)
            throws Exception {
        if (paramsString.trim().isEmpty()) {
            return new VariableInfo[0];
        }
        String withoutSemicolon = paramsString.substring(0, paramsString.indexOf(SEMICOLON));
        String[] paramsArray = withoutSemicolon.split(COMMA);
        List<VariableInfo> parameters = new ArrayList<>();
        for (String param : paramsArray) {
            String[] parts = param.split(ASSIGN_OP);
            String varName = parts[0].trim();
            boolean didAssign = parts.length > 1;
            if (didAssign){
                String assignment = parts[1].trim();
                varAssignmentCheck(varType, assignment);
            }
            VariableInfo variableInfo = new VariableInfo(varName, varType, isFinal, didAssign);
            parameters.add(variableInfo);
        }
        return parameters.toArray(new VariableInfo[0]);
    }

    private VariableInfo[] parseParameters(String paramsString) {
        if (paramsString.trim().isEmpty()) {
            return new VariableInfo[0];
        }
        String[] paramsArray = paramsString.split(COMMA);
        List<VariableInfo> parameters = new ArrayList<>();
        for (String param : paramsArray) {
            Matcher matcher = argsPattern.matcher(param);
            if (matcher.matches()) {
                String varType = matcher.group(TYPE_GROUP);
                String varName = matcher.group(NAME_GROUP);
                boolean isFinal = finalPattern.matcher(param).find();
                VariableInfo variableInfo = new VariableInfo(varName, varType, isFinal, false);
                parameters.add(variableInfo);
            }
        }
        return parameters.toArray(new VariableInfo[0]);
    }

    private void varAssignmentCheck(String type, String assignment) throws Exception {
        switch (type) {
            case INT:
                Matcher intMatcher = intValPattern.matcher(assignment);
                if (intMatcher.matches()) {
                    return;
                }
                break;
            case DOUBLE:
                Matcher doubleMatcher = doubleValPattern.matcher(assignment);
                if (doubleMatcher.matches()) {
                    return;
                }
                break;
            case BOOLEAN:
                Matcher boolMatcher = boolValPattern.matcher(assignment);
                if (boolMatcher.matches()) {
                    return;
                }
                break;
            case CHAR:
                Matcher charMatcher = charValPattern.matcher(assignment);
                if (charMatcher.matches()) {
                    return;
                }
                break;
            case STRING:
                Matcher stringMatcher = stringValPattern.matcher(assignment);
                if (stringMatcher.matches()) {
                    return;
                }
                break;
        }
        Matcher nameMatcher = namePattern.matcher(assignment);
        if (nameMatcher.matches()) {
            for (List<VariableInfo> scopeVars : variables) {
                for (VariableInfo var : scopeVars) {
                    if (var.getName().equals(assignment) && var.getType().equals(type) && var.isAssigned()) {
                        return;
                    }
                }
            }
        }
        else throw new Exception("Type mismatch in assignment");
    }

    private void validateLegalCode() throws Exception {
        for (int i = 0; i < fileContents.size(); i++) {
            String line = fileContents.get(i);
            line = line.trim();
            Matcher matcher = functionDefinitionPattern.matcher(line);
            if (matcher.matches()){
                String funcName = matcher.group(NAME_GROUP);
                FunctionInfo curFunc = null;
                for (FunctionInfo func : functions) {
                    if (func.getName().equals(funcName)) {
                        curFunc = func;
                        break;
                    }
                }
                if (curFunc == null) {
                    throw new Exception("Function not found: " + funcName + "in line: " + i + "\n" + line);
                }
                i = validateScope(i+1, curFunc.getParameters());
            }
        }
    }

    private int validateScope(int ind, VariableInfo[] localVars) throws Exception {
        variables.push(new ArrayList<>(Arrays.asList(localVars)));
        for (; (ind< fileContents.size()) && !fileContents.get(ind).trim().equals(CLOSE_SCOPE); ind++) {
            String line = fileContents.get(ind);
            line = line.trim();
            if (matchDeclaration(line)) {
                continue;
            } else if (line.isEmpty()) {
                continue;
            } else if (checkForAssignments(line, ind)) {
                continue;
            } else if (ifWhilePattern.matcher(line).matches()) {
                boolean legal = checkIfWhileCondition(ind, line, localVars);
                if (!legal){
                    throw new Exception("Illegal condition in if/while statement in line: " + ind + "\n" +
                            line);
                }
            } else if (functionCallPattern.matcher(line).matches()) {
                continue;
            } else if (returnPattern.matcher(line).matches()) {
                continue;
            } else if (functionCallPattern.matcher(line).matches()) {
                String[] parts = line.split("\\s*\\(\\s*|\\s*\\)\\s*");
                String funcName = parts[0].trim();
                String[] args = parts.length > 1 ? parts[1].split(COMMA) : new String[0];
                FunctionInfo targetFunc = null;
                for (FunctionInfo func : functions) {
                    if (func.getName().equals(funcName)) {
                        targetFunc = func;
                        break;
                    }
                }
                if (targetFunc == null) {
                    throw new Exception("Function " + funcName + " not declared in line: " + ind + "\n" +
                            line);
                }
                if (args.length != targetFunc.getParameters().length) {
                    throw new Exception("Illegal number of arguments in function call to " + funcName +
                            " in line: " + ind + "\n" + line + "\nexpected " +
                            targetFunc.getParameters().length + " but got " + args.length);
                }
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i].trim();
                    String expectedType = targetFunc.getParameters()[i].getType();
                    varAssignmentCheck(expectedType, arg);
                }
            } else {
                throw new Exception("Illegal statement in line: " + ind + "\n" + line);
            }
        }
        if (ind >= fileContents.size() || !fileContents.get(ind).equals(CLOSE_SCOPE)) {
            throw new Exception("Missing closing brace for function scope in line: " + ind +"\n" +
                    fileContents.get(ind));
        }
        variables.pop();
        if (variables.size() == 1 && !returnPattern.matcher(fileContents.get(ind - 1).trim()).matches()) {
            throw new Exception("Missing return statement at the end of function in line: " + (ind - 1)
                    + "\n" + fileContents.get(ind - 1));
        }
        return ind + 1;
    }

    private boolean checkIfWhileCondition(int lineNum, String line, VariableInfo[] localVars)
            throws Exception {
        String condition = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        boolean legal = false;
        String splitType = "";
        String[] paramsArray = condition.split("\\s+(&&|\\|\\|)\\s+");
        if (condition.contains("&&") && condition.contains("||")){
            throw new Exception("Mixed && and || operators in if/while condition in line: " + lineNum + "\n" +
                    line);
        }
        if (condition.contains("&&")){
            splitType = "&&";
        }
        else if (condition.contains("||")){
            splitType = "||";
        }
        for (String param : paramsArray){
            String cleanParam = param.trim();
            if (cleanParam.isEmpty()){
                throw new Exception("Invalid condition in if/while statement in line: " + lineNum + "\n" +
                        line);
            }
            if (!ifWhileConditionPattern.matcher(cleanParam).matches()){
                throw new Exception("Invalid condition in if/while statement in line: " + lineNum + "\n" +
                        line);
            }
            if (namePattern.matcher(cleanParam).matches()){
                for (List<VariableInfo> scopeVars : variables) {
                    for (VariableInfo var : scopeVars) {
                        if (var.getName().equals(cleanParam)) {
                            if (!var.isAssigned()){
                                throw new Exception("Unassigned variable in if/while condition in line: "
                                        + lineNum + "\n" + line);
                            }
                            legal = true;
                            break;
                        }
                    }
                    if (legal){
                        break;
                    }
                }
            }
        }
        return legal;
    }

    private boolean updateConditionStatus(boolean currentStatus, String condition,
                                                   String splitType, VariableInfo[] localVars)
            throws Exception {
        //this function will check the condition and return the updated status
        return currentStatus;
    }

    private boolean checkForAssignments(String line, int lineNum) throws Exception {
        Pattern assignmentPattern = Pattern.compile(ASSIGNMENT);
        Matcher assignmentMatcher = assignmentPattern.matcher(line);
        if (assignmentMatcher.matches()) {
            String withoutSemicolon = line.substring(0, line.indexOf(SEMICOLON));
            String[] paramsArray = withoutSemicolon.split(COMMA);
            for (String param : paramsArray) {
                String[] parts = param.split(ASSIGN_OP);
                String varName = parts[0].trim();
                String assignment = parts[1].trim();
                VariableInfo targetVar = getVariableInfo(line, lineNum, varName);
                if (targetVar.isFinal()) {
                    throw new Exception("Cannot reassign final variable " + varName + " in line: " +
                            lineNum + "\n" + line);
                }
                varAssignmentCheck(targetVar.getType(), assignment);
                targetVar.setAssigned(true);
            }
            return true;
        }
        return false;
    }

    private VariableInfo getVariableInfo(String line, int lineNum, String varName) throws Exception {
        VariableInfo targetVar = null;
        for (List<VariableInfo> scopeVars : variables) {
            for (VariableInfo var : scopeVars) {
                if (var.getName().equals(varName)) {
                    targetVar = var;
                    break;
                }
            }
        }
        if (targetVar == null) {
            throw new Exception("Variable " + varName + " not declared in line: " + lineNum + "\n"
                    + line);
        }
        return targetVar;
    }

    private boolean matchDeclaration(String line) throws Exception {
        Matcher varMatcher = varDeclarationPattern.matcher(line);
        if (varMatcher.matches()) {
            String varType = varMatcher.group(TYPE_GROUP);
            boolean isFinal = finalPattern.matcher(line).find();
            assert variables.peek() != null;
            variables.peek().addAll(Arrays.asList(parseVariables(isFinal, varType,
                    line.substring(line.indexOf(varType) + varType.length()))));
            return true;
        }
        return false;
    }
}

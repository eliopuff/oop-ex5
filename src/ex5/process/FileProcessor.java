package ex5.process;

import ex5.info_structs.FunctionInfo;
import ex5.info_structs.VariableInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class responsible for processing and validating the contents of a source file.
 * It checks for correct syntax, variable declarations, function definitions, and scopes.
 * @author eliooo,sagig
 */
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
    private static final String FUNCTION_CALL = "(?<name>" + NAME + ")\\s*\\([^)]*\\)\\s*;";
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
            "\\s*" + BOOL_VAL + "|" + NAME+ "\\s*";
    private static final String AND_OR = "\\s+(&&|\\|\\|)\\s+";
    private static final String CALL_SEPARATOR = "\\s*\\(\\s*|\\s*\\)\\s*";

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
    private static final String OPEN_PARENTH = "(";
    private static final String CLOSE_PARENTH = ")";

    private static final String IF_WHILE_ERROR_MSG = "ERROR: Illegal if/while condition.";
    private static final String FUNCTION_ERROR_MSG = "ERROR: Illegal function call.";
    private static final String VARIABLE_ERROR_MSG = "ERROR: Illegal variable assignment.";
    private static final String MISSING_SEMICOLON_ERROR_MSG = "ERROR: Missing semicolon at the end of line.";
    private static final String UNMATCHED_BRACE_ERROR_MSG = "ERROR: Unmatched closing brace found.";
    private static final String NESTED_FUNCTION_ERROR_MSG = "ERROR: Nested functions are not allowed.";
    private static final String FUNCTION_DECLARED_ERROR_MSG = "ERROR: Function already declared. Name: ";
    private static final String GLOBAL_VAR_ERROR_MSG = "ERROR: Illegal global variable declaration.";
    private static final String TYPE_MISMATCH_ERROR_MSG = "ERROR: Type mismatch in assignment";
    private static final String FUNC_NOT_FOUND_ERROR_MSG = "ERROR: Function not found. Name: ";
    private static final String ILLEGAL_NUM_ARGS_ERROR = "ERROR: Illegal number of arguments in function " +
            "call.";
    private static final String ALREADY_DECLARED_ERROR_MSG = "ERROR: Variable already declared in the " +
            "same scope.";
    private static final String VAR_NOT_DECLARED_ERROR = "ERROR: Variable not declared.";
    private static final String REASSIGN_VAR_ERROR_MSG = "ERROR: Cannot reassign a final variable.";
    private static final String INVALID_IF_WHILE_ERROR_MSG = "ERROR: Invalid if/while condition.";
    private static final String ILLEGAL_STATEMENT_ERROR_MSG = "ERROR: Illegal statement in function body.";
    private static final String MISSING_BRACE_ERROR_MSG = "ERROR: Missing closing brace for" +
            " function.";
    private static final String MISSING_RETURN_ERROR_MSG = "ERROR: Missing return statement in function.";



    /**
     * Constructor for FileProcessor. Initiates all the pattern compilers for regex
     * @param fileReader A FileReader object to read the source file.
     */
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
        functionCallPattern = Pattern.compile(FUNCTION_CALL); // Added pattern for
        functionDefinitionPattern = Pattern.compile(METHOD_DECLARATION);

        varDeclarationPattern = Pattern.compile(VARIABLE_DECLARATION);
    }

    /**
     * Processes the source file, validating its contents for correct syntax and structure.
     * @throws SjavacException if any syntax or structural errors are found in the source file.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public void processFile() throws SjavacException, IOException {
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        fileContents = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            fileContents.add(line.trim());
        }
        validateScopesAndColons();
        findFuncAndGlobalVars();
        validateLegalCode();
    }

    /**
     * Validates that all scopes are properly opened and closed,
     * and that lines end with the correct characters (semicolons, braces).
     * @throws SjavacException if unmatched braces or missing semicolons are found.
     */
    public void validateScopesAndColons() throws SjavacException {
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
                    throw new SjavacException(UNMATCHED_BRACE_ERROR_MSG);
                }
            } else if (line.startsWith(COMMENT_PREFIX)){
                    continue;
            } else if (!line.endsWith(SEMICOLON) &&
                    !line.endsWith(OPEN_SCOPE) && !line.endsWith(CLOSE_SCOPE) &&
                    !functionCallPattern.matcher(line).matches()) {
                throw new SjavacException(MISSING_SEMICOLON_ERROR_MSG);
            }
        }
    }


    private void findFuncAndGlobalVars() throws SjavacException {
        int depth = 0;
        variables.push(new ArrayList<>());
        Pattern funcPattern = Pattern.compile(METHOD_DECLARATION);
        for (String line : fileContents) {
            line = line.trim();
            if (line.endsWith(OPEN_SCOPE)) {
                Matcher matcher = funcPattern.matcher(line);
                if (depth == 0) {
                    if (matcher.matches()) {

                        String funcName = matcher.group(NAME_GROUP);
                        String paramsString = line.substring(line.indexOf(OPEN_PARENTH) + 1,
                                line.indexOf(CLOSE_PARENTH));
                        VariableInfo[] parameters = parseParameters(paramsString);
                        FunctionInfo functionInfo = new FunctionInfo(funcName, parameters);
                        for (FunctionInfo func : functions) {
                            if (func.getName().equals(funcName)) {
                                throw new SjavacException(FUNCTION_DECLARED_ERROR_MSG + funcName);
                            }
                        }
                        functions.add(functionInfo);
                    }
                    else {
                        throw new SjavacException(FUNC_NOT_FOUND_ERROR_MSG);
                    }
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
                }else throw new SjavacException(GLOBAL_VAR_ERROR_MSG);
            }
        }
    }

    private VariableInfo[] parseVariables(boolean isFinal, String varType ,String paramsString)
            throws SjavacException {
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

    private void varAssignmentCheck(String type, String assignment) throws SjavacException {
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
        else throw new SjavacException(TYPE_MISMATCH_ERROR_MSG);
    }

    private void validateLegalCode() throws SjavacException {
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
                    throw new SjavacException(FUNC_NOT_FOUND_ERROR_MSG + funcName);
                }
                i = validateScope(i+1, curFunc.getParameters());
            }
        }
    }

    private int validateScope(int ind, VariableInfo[] localVars) throws SjavacException {
        variables.push(new ArrayList<>(Arrays.asList(localVars)));
        for (; (ind< fileContents.size()) && !fileContents.get(ind).trim().equals(CLOSE_SCOPE); ind++) {
            String line = fileContents.get(ind);
            line = line.trim();
            if (matchDeclaration(line) || line.isEmpty() || checkForAssignments(line, ind) ||
                    functionCallPattern.matcher(line).matches() || returnPattern.matcher(line).matches()) {
                continue;
            } else if (ifWhilePattern.matcher(line).matches()) {
                boolean legal = checkIfWhileCondition(ind, line, localVars);
                if (!legal){
                    throw new SjavacException(IF_WHILE_ERROR_MSG);
                }
            } else if (functionCallPattern.matcher(line).matches()) {
                validFuncCall(ind, line);
            } else {
                throw new SjavacException(ILLEGAL_STATEMENT_ERROR_MSG);
            }
        }
        if (ind >= fileContents.size() || !fileContents.get(ind).trim().equals(CLOSE_SCOPE)) {
            throw new SjavacException(MISSING_BRACE_ERROR_MSG);
        }
        if (variables.size() == 1 && !returnPattern.matcher(fileContents.get(ind - 1).trim()).matches()) {
            throw new SjavacException(MISSING_RETURN_ERROR_MSG);
        }
        variables.pop();
        return ind + 1;
    }

    private void validFuncCall(int ind, String line) throws SjavacException {
        String[] parts = line.split(CALL_SEPARATOR);
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
            throw new SjavacException(FUNC_NOT_FOUND_ERROR_MSG);
        }
        if (args.length != targetFunc.getParameters().length) {
            throw new SjavacException(ILLEGAL_NUM_ARGS_ERROR);
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            String expectedType = targetFunc.getParameters()[i].getType();
            varAssignmentCheck(expectedType, arg);
        }
    }

    private boolean checkIfWhileCondition(int lineNum, String line, VariableInfo[] localVars)
            throws SjavacException {
        String condition = line.substring(line.indexOf(OPEN_PARENTH) + 1, line.indexOf(CLOSE_PARENTH)).trim();
        boolean legal = false;
        String[] paramsArray = condition.split(AND_OR);
        for (String param : paramsArray){
            String cleanParam = param.trim();
            if (cleanParam.isEmpty()){
                throw new SjavacException(INVALID_IF_WHILE_ERROR_MSG);
            }
            if (!ifWhileConditionPattern.matcher(cleanParam).matches()){
                throw new SjavacException(INVALID_IF_WHILE_ERROR_MSG);
            }
            if (namePattern.matcher(cleanParam).matches()){
                for (List<VariableInfo> scopeVars : variables) {
                    for (VariableInfo var : scopeVars) {
                        if (var.getName().equals(cleanParam)) {
                            if (!var.isAssigned()){
                                throw new SjavacException(INVALID_IF_WHILE_ERROR_MSG);
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
            if (!legal){
                Matcher boolMatcher = boolValPattern.matcher(cleanParam);
                if (boolMatcher.matches()){
                    legal = true;
                }
            }
        }
        return legal;
    }


    private boolean checkForAssignments(String line, int lineNum) throws SjavacException {
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
                    throw new SjavacException(REASSIGN_VAR_ERROR_MSG);
                }
                varAssignmentCheck(targetVar.getType(), assignment);
                targetVar.setAssigned(true);
            }
            return true;
        }
        return false;
    }

    private VariableInfo getVariableInfo(String line, int lineNum, String varName) throws SjavacException {
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
            throw new SjavacException(VAR_NOT_DECLARED_ERROR);
        }
        return targetVar;
    }

    private boolean matchDeclaration(String line) throws SjavacException {
        Matcher varMatcher = varDeclarationPattern.matcher(line);
        if (varMatcher.matches()) {
            String varType = varMatcher.group(TYPE_GROUP);
            boolean isFinal = finalPattern.matcher(line).find();
            assert variables.peek() != null;
            VariableInfo[] parsedVars = parseVariables(isFinal, varType,
                    line.substring(line.indexOf(varType) + varType.length()));
            for (VariableInfo newVar : parsedVars) {
                assert variables.peek() != null;
                for (VariableInfo existingVar : variables.peek()) {
                    if (newVar.getName().equals(existingVar.getName())) {
                        throw new SjavacException(ALREADY_DECLARED_ERROR_MSG);
                    }
                }
            }
            assert variables.peek() != null;
            variables.peek().addAll(Arrays.asList(parsedVars));
            return true;
        }
        return false;
    }
}

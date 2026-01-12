package ex5.process;

import ex5.Stucts.FunctionInfo;
import ex5.Stucts.VariableInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessor {


    private final Pattern varDeclarationPattern;
    private FileReader fileReader;
    private final String sourceCode;
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
    private final List<FunctionInfo> functions;
    private final List<VariableInfo> variables;


    //RegEx constants
    private static final String COMMENT_PREFIX = "//";
    private static final String COMMENT = "\\s*" + COMMENT_PREFIX + ".*";
    private static final String TYPE = "(int|boolean|char|double|String)";
    private static final String NAME = "([a-zA-Z]|_[a-zA-Z0-9])\\w*";
    private static final String METHOD_DECLARATION = "\\s*void\\s+(?<name>" + NAME + ")\\s*\\([^)]*\\)" +
            "\\s*\\{";
    private static final String VARIABLE_DECLARATION = "\\s*(final\\s+)?(?<type>" + TYPE + ")\\s+("
            + NAME + "\\s*,\\s*)*"+NAME+"\\s*(=\\s*[^;]*)?;\\s*";
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
    //private static final String

    private static final String NAME_GROUP = "name";
    private static final String TYPE_GROUP = "type";
    private static final String FINAL = "final";


    public FileProcessor(FileReader fileReader, String sourceCode) {
        this.fileReader = fileReader;
        this.sourceCode = sourceCode;
        functions = new ArrayList<>();
        variables = new ArrayList<>();
        finalPattern = Pattern.compile(FINAL);
        commentPattern = Pattern.compile(COMMENT);
        argsPattern = Pattern.compile(PARAMETER_DECLARATION);
        intValPattern = Pattern.compile(INT_VAL);
        doubleValPattern = Pattern.compile(DOUBLE_VAL);
        boolValPattern = Pattern.compile(BOOL_VAL);
        charValPattern = Pattern.compile(CHAR_VAL);
        stringValPattern = Pattern.compile(STRING_VAL);
        namePattern = Pattern.compile(NAME);
        functionCallPattern = Pattern.compile("(?<name>"+NAME+ ")\\s*\\([^)]\\)"); // Added pattern for
        functionDefinitionPattern = Pattern.compile(METHOD_DECLARATION);
        // function
        // calls

        varDeclarationPattern = Pattern.compile(VARIABLE_DECLARATION);
    }

    public void processFile() throws Exception {
        validateScopesAndColons();
        this.fileReader.close();
        this.fileReader = new FileReader(sourceCode); // Resetting the FileReader
        findFuncAndGlobalVars();
        System.out.println(functions);
        System.out.println(variables);
        this.fileReader.close();
        this.fileReader = new FileReader(sourceCode);
        validateLegalCode();
    }

    public void validateScopesAndColons() throws Exception {
        int scopeCount = 0;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
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
            } else if (!line.endsWith(";") &&
                    !line.endsWith(OPEN_SCOPE) && !line.endsWith(CLOSE_SCOPE) &&
                    !functionCallPattern.matcher(line).matches()) {
                throw new Exception("Missing semicolon at the end of line: " + line);
            }
        }
    }


    private void findFuncAndGlobalVars() throws Exception {
        int depth = 0;
        //fileReader.reset();
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        Pattern funcPattern = Pattern.compile(METHOD_DECLARATION);
        while ((line = bufferedReader.readLine()) != null) {
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
                matcher = varDeclarationPattern.matcher(line);
                if (matcher.matches()) {
                    String varType = matcher.group(TYPE_GROUP);
                    boolean isFinal = finalPattern.matcher(line).find();
                    variables.addAll(Arrays.asList(parseVariables(isFinal, varType,
                            line.substring(line.indexOf(varType) + varType.length()))));
                } else if (line.isEmpty()) {
                    continue;
                }else throw new Exception("Illegal global variable declaration: " + line);
            }
        }
    }

    private VariableInfo[] parseVariables(boolean isFinal, String varType ,String paramsString) throws Exception {
        if (paramsString.trim().isEmpty()) {
            return new VariableInfo[0];
        }
        String withoutSemicolon = paramsString.substring(0, paramsString.indexOf(';'));
        String[] paramsArray = withoutSemicolon.split(",");
        List<VariableInfo> parameters = new ArrayList<>();
        for (String param : paramsArray) {
            String[] parts = param.split("=");
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
        String[] paramsArray = paramsString.split(",");
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
            case "int":
                Matcher intMatcher = intValPattern.matcher(assignment);
                if (intMatcher.matches()) {
                    return;
                }
                break;
            case "double":
                Matcher doubleMatcher = doubleValPattern.matcher(assignment);
                if (doubleMatcher.matches()) {
                    return;
                }
                break;
            case "boolean":
                Matcher boolMatcher = boolValPattern.matcher(assignment);
                if (boolMatcher.matches()) {
                    return;
                }
                break;
            case "char":
                Matcher charMatcher = charValPattern.matcher(assignment);
                if (charMatcher.matches()) {
                    return;
                }
                break;
            case "String":
                Matcher stringMatcher = stringValPattern.matcher(assignment);
                if (stringMatcher.matches()) {
                    return;
                }
                break;
        }
        Matcher nameMatcher = namePattern.matcher(assignment);
        if (nameMatcher.matches()) {
            for (VariableInfo var : variables) {
                if (var.getName().equals(assignment) && var.getType().equals(type) && var.isDidAssign()) {
                    return;
                }
            }
        }
        else throw new Exception("Type mismatch in assignment");
    }

    private void validateLegalCode() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
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
                validateScope(bufferedReader, curFunc.getParameters());
            }
        }
    }

    private void validateScope(BufferedReader bufferedReader, VariableInfo[] localVars) throws Exception {
        String line;

        while ((line = bufferedReader.readLine()) != null && !line.trim().equals("}")) {
            line = line.trim();
            Matcher varMatcher = varDeclarationPattern.matcher(line);
            if (varMatcher.matches()) {
                String varType = varMatcher.group(TYPE_GROUP);
                boolean isFinal = finalPattern.matcher(line).find();
                VariableInfo[] vars = parseVariables(isFinal, varType,
                        line.substring(line.indexOf(varType) + varType.length()));
                for (VariableInfo var : vars) {

        }
    }
}

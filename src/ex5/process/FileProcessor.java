package ex5.process;

import ex5.Stucts.FunctionInfo;
import ex5.Stucts.VariableInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessor {


    private final FileReader fileReader;
    private final Pattern varPattern;
    private final Pattern finalPattern;
    private final Pattern commentPattern;
    private final Pattern intValPattern;
    private final Pattern doubleValPattern;
    private final Pattern boolValPattern;
    private final Pattern charValPattern;
    private final Pattern stringValPattern;
    private final Pattern namePattern;
    private final Pattern functionCallPattern;
    private final List<FunctionInfo> functions;
    private final List<VariableInfo> globalVars;

    //RegEx constants
    private static final String COMMENT_PREFIX = "//";
    private static final String COMMENT = "\\s*" + COMMENT_PREFIX + ".*";
    private static final String TYPE = "(int|boolean|char|double|String)";
    private static final String NAME = "([a-zA-Z]|_[a-zA-Z0-9])\\w*";
    private static final String METHOD_DECLARATION = "\\s*void\\s+(?<name>" + NAME + ")\\s*\\([^)]*\\)" +
            "\\s*\\{";
    private static final String VARIABLE_DECLARATION = "\\s*(final\\s+)?(?<type>" + TYPE + ")\\s+(?<name>"
            + NAME + ")\\s*;\\s*";
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

    private static final String NAME_GROUP = "name";
    private static final String TYPE_GROUP = "type";
    private static final String FINAL = "final";


    public FileProcessor(FileReader fileReader) {
        this.fileReader = fileReader;
        functions = new ArrayList<>();
        globalVars = new ArrayList<>();
        finalPattern = Pattern.compile(FINAL);
        commentPattern = Pattern.compile(COMMENT);
        varPattern = Pattern.compile(PARAMETER_DECLARATION);
        intValPattern = Pattern.compile(INT_VAL);
        doubleValPattern = Pattern.compile(DOUBLE_VAL);
        boolValPattern = Pattern.compile(BOOL_VAL);
        charValPattern = Pattern.compile(CHAR_VAL);
        stringValPattern = Pattern.compile(STRING_VAL);
        namePattern = Pattern.compile(NAME);
        functionCallPattern = Pattern.compile("(?<name>"+NAME+ ")\\s*\\([^)]\\)"); // Added pattern for
        // function
        // calls

    }

    public void processFile() throws IOException {
        findFuncAndGlobalVars();
        System.out.println(functions);
        System.out.println(globalVars);
    }


    private void findFuncAndGlobalVars() throws IOException{
        int depth = 0;
        //fileReader.reset();
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        Pattern funcPattern = Pattern.compile(METHOD_DECLARATION);
        Pattern varPattern = Pattern.compile(VARIABLE_DECLARATION);
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.endsWith(OPEN_SCOPE)) {
                Matcher matcher = funcPattern.matcher(line);
                if (matcher.matches()) {
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
                matcher = varPattern.matcher(line);
                createVariableInfo(globalVars, line, matcher);
            }
        }
    }

    private VariableInfo[] parseParameters(String paramsString) {
        if (paramsString.trim().isEmpty()) {
            return new VariableInfo[0];
        }
        String[] paramsArray = paramsString.split(",");
        List<VariableInfo> parameters = new ArrayList<>();
        for (String param : paramsArray) {
            Matcher matcher = varPattern.matcher(param);
            createVariableInfo(parameters, param, matcher);
        }
        return parameters.toArray(new VariableInfo[0]);
    }

    private void createVariableInfo(List<VariableInfo> parameters, String param, Matcher matcher) {
        if (matcher.matches()) {
            String varType = matcher.group(TYPE_GROUP);
            String varName = matcher.group(NAME_GROUP);
            boolean isFinal = finalPattern.matcher(param).find();
            VariableInfo variableInfo = new VariableInfo(varName, varType, isFinal, false);
            parameters.add(variableInfo);
        }
    }

    private void varAssignmentCheck(String type, String assighment) throws Exception {
        switch (type) {
            case "int":
                Matcher intMatcher = intValPattern.matcher(assighment);
                if (intMatcher.matches()) {
                    return;
                }
                break;
            case "double":
                Matcher doubleMatcher = doubleValPattern.matcher(assighment);
                if (doubleMatcher.matches()) {
                    return;
                }
                break;
            case "boolean":
                Matcher boolMatcher = boolValPattern.matcher(assighment);
                if (boolMatcher.matches()) {
                    return;
                }
                break;
            case "char":
                Matcher charMatcher = charValPattern.matcher(assighment);
                if (charMatcher.matches()) {
                    return;
                }
                break;
            case "String":
                Matcher stringMatcher = stringValPattern.matcher(assighment);
                if (stringMatcher.matches()) {
                    return;
                }
                break;
        }
        Matcher nameMatcher = namePattern.matcher(assighment);
        if (nameMatcher.matches()) {
            for (VariableInfo var : globalVars) {
                if (var.getName().equals(assighment) && var.getType().equals(type) && var.isDidAssign()) {
                    return;
                }
            }
        }
        else throw new Exception("Type mismatch in assignment");
    }
}

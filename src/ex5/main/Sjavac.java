package ex5.main;
import ex5.process.FileProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;


public class Sjavac {
    private final static String ARG_ERROR_MSG = "ERROR: Illegal number of arguments.";
    private final static String FILE_FORMAT_ERROR_MSG = "ERROR: Illegal file format. Expected a .sjavac " +
            "file.";
    private final static String HELLO = "Hello, Sjavac!";
    private final static int LEGAL_PRINT = 0;
    private final static int ILLEGAL_PRINT = 1;
    private final static int IO_ERROR_PRINT = 2;
    private final static String REGEX_FILE_FORMAT = ".*\\.sjava$";

    public static void main(String[] args) {
        System.out.println(HELLO);
        if (args.length != 1){
            System.out.println(IO_ERROR_PRINT);
            System.err.println(ARG_ERROR_MSG);
            return;
        }
        Pattern pattern = Pattern.compile(REGEX_FILE_FORMAT);
        if (!pattern.matcher(args[0]).matches()) {
            System.out.println(IO_ERROR_PRINT);
            System.err.println(FILE_FORMAT_ERROR_MSG);
            return;
        }
        String sourceCode = args[0];
        try(FileReader fileReader = new FileReader(sourceCode)) {
            FileProcessor processor = new FileProcessor(fileReader);
            processor.processFile();
            System.out.println(LEGAL_PRINT);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(IO_ERROR_PRINT);
            System.err.println(e.getMessage());
        }
    }
}

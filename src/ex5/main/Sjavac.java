package ex5.main;
import ex5.process.FileProcessor;
import ex5.process.SjavacException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The main class for the Sjavac compiler.
 * It processes a source file and outputs the result of the compilation.
 * @author eliooo,sagig
 */
public class Sjavac {
    private final static String ARG_ERROR_MSG = "ERROR: Illegal number of arguments.";
    private final static String FILE_FORMAT_ERROR_MSG = "ERROR: Illegal file format. Expected a .sjavac " +
            "file.";
    private final static int LEGAL_PRINT = 0;
    private final static int ILLEGAL_PRINT = 1;
    private final static int IO_ERROR_PRINT = 2;
    private final static String REGEX_FILE_FORMAT = ".*\\.sjava$";

    /**
     * The main method for the Sjavac compiler. throws SjavacException if the source file is illegal.
     * @param args Command line arguments. Expects a single argument: the source file name.
     */
    public static void main(String[] args) {
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
            System.err.println(e.getMessage());
            System.out.println(IO_ERROR_PRINT);
        }
        catch (SjavacException e) {
            System.err.println(e.getMessage());
            System.out.println(ILLEGAL_PRINT);
        }
    }
}

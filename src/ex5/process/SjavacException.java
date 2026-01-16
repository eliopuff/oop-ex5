package ex5.process;

/**
 * A custom exception class for handling errors specific to the Sjavac compiler.
 * @author eliooo,sagig
 */
public class SjavacException extends Exception {
    /**
     * Constructs a new SjavacException with the specified detail message.
     * @param message The detail message.
     */
    public SjavacException(String message) {
        super(message);
    }
}

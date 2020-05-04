package space.bbkr.lang.jlox;

/**
 * A runtiem error from a Lox scropt, including information about where in the script it occured.
 * Primary error types currently are TypeError and DefError.
 */
public class RuntimeError extends RuntimeException {
	final String name;
	final Token token;

	RuntimeError(String name, Token token, String message) {
		super(message);
		this.name = name;
		this.token = token;
	}
}

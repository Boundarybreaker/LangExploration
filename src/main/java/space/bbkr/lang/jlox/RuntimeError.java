package space.bbkr.lang.jlox;

public class RuntimeError extends RuntimeException {
	final String name;
	final Token token;

	RuntimeError(String name, Token token, String message) {
		super(message);
		this.name = name;
		this.token = token;
	}
}

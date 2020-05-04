package space.bbkr.lang.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * The main class! Probably gonna get changed around when I convert this to a lib instead of standalone.
 */
public class Lox {
	private static final Interpreter interpreter = new Interpreter();
	private static boolean hadError = false;
	private static boolean hadRuntimeError = false;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()), false);
		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		System.out.println("Opening interactive shell. Run '::exit' or hit ^c to exit.");
		boolean running = true;

		while (running) {
			System.out.print("> ");
			String read = reader.readLine();
			if (read.equals("::exit")) {
				running = false;
			} else {
				run(reader.readLine(), true);
				hadError = false;
			}
		}

		System.out.println("Shell closed.");
		System.exit(0);
	}

	private static void run(String source, boolean repl) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		if (repl && !hasType(tokens, TokenType.SEMICOLON)) { //no semicolon, so they probably want an expression
			Expression expression = parser.parseExpression();
			if (hadError) return;

			List<Statement> statements = Collections.singletonList(
					new Statement.ExpressionStatement(
							new Expression.CallExpression(
									new Expression.VariableExpression(new Token(TokenType.IDENTIFIER,"print", null, 1, 0)),
									new Token(TokenType.LEFT_PAREN, "> ", null, 1, 6),
									Collections.singletonList(expression))
					)
			);

			Resolver resolver = new Resolver(interpreter);
			resolver.resolve(statements);

			if (hadError) return;

			interpreter.interpret(statements);
		} else {
			List<Statement> statements = parser.parse();
			if (hadError) return;

			Resolver resolver = new Resolver(interpreter);
			resolver.resolve(statements);

			if (hadError) return;

			interpreter.interpret(statements);
		}
	}

	static void error(int line, int column, String message) {
		report(line, column, "", message);
	}

	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, token.column, "at end", message);
		} else {
			report(token.line, token.column, "at '" + token.lexeme + "'", message);
		}
	}

	static void runtimeError(RuntimeError error) {
		System.err.println(error.name + ": " + error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

	private static void report(int line, int column, String where, String message) {
		System.err.println("[line " + line + ", column " + column + "] Error " + where + ": " + message);
		hadError = true;
	}

	private static boolean hasType(List<Token> tokens, TokenType type) {
		for (Token token : tokens) {
			if (token.type == type) {
				return true;
			}
		}
		return false;
	}

}

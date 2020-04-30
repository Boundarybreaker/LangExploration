package space.bbkr.lang.jlox;

import static space.bbkr.lang.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	List<Statement> parse() {
		List<Statement> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(declaration());
		}
		return statements;
	}

	Expression parseExpression() {
		return expression();
	}

	/**
	 * @return The next token in the script, incrementing the counter.
	 */
	private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}

	/**
	 * @return The next token in the script, not incrementing the counter.
	 */
	private Token peek() {
		return tokens.get(current);
	}

	/**
	 * @return The previous token in the script.
	 */
	private Token previous() {
		return tokens.get(current - 1);
	}

	/**
	 * @param type The type of token to consume.
	 * @param message The message to give if the token isn't found.
	 * @return The found token of the given type, or an exception is thrown.
	 */
	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();

		throw error(peek(), message);
	}

	//handling

	/**
	 * Log and create an error while parsing.
	 * @param token The token causing the error.
	 * @param message The cause of the error.
	 * @return The exception to throw.
	 */
	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	/**
	 * Continue to next safe statement after an error.
	 */
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return;

			switch (peek().type) {
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case RETURN:
					return;
			}

			advance();
		}
	}

	//checking
	/**
	 * @return If at the end of the script
	 */
	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	/**
	 * Increments.
	 * @param types The possible tokens to check for.
	 * @return Whether a token of any passed type was found.
	 */
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	/**
	 * Does not increment.
	 * @param type The type of token to check for.
	 * @return Whether that type was found.
	 */
	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}

	//actual parsing
	private Statement statement() {
		if (match(IF)) return ifStatement();
		if (match(RETURN)) return returnStatement();
		if (match(FOR)) return forStatement();
		if (match(WHILE)) return whileStatement();
		if (match(BREAK)) return breakStatement();
		if (match(LEFT_BRACE)) return new Statement.BlockStatement(blockStatement());

		return expressionStatement();
	}

	private Statement ifStatement() {
		Token keyword = previous();
		consume(LEFT_PAREN, "Expect '(' after 'if'.");
		Expression condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after if condition.");

		Statement thenBranch = statement();
		Statement elseBranch = null;
		if (match(ELSE)) {
			elseBranch = statement();
		}

		return new Statement.IfStatement(keyword, condition, thenBranch, elseBranch);
	}

	private Statement returnStatement() {
		Token keyword = previous();
		Expression value = null;
		if (!check(SEMICOLON)) {
			value = expression();
		}

		consume(SEMICOLON, "Expect ';' after return value.");
		return new Statement.ReturnStatement(keyword, value);
	}

	private Statement forStatement() {
		Token keyword = previous();
		consume(LEFT_PAREN, "Expect '(' after 'for'.");

		Statement initializer;
		if (match(SEMICOLON)) {
			initializer = null;
		} else if (match(VAR)) {
			initializer = varDeclaration();
		} else {
			initializer = expressionStatement();
		}

		Expression condition = null;
		if (!check(SEMICOLON)) {
			condition = expression();
		}
		consume(SEMICOLON, "Expect ';' after loop condition.");

		Expression increment = null;
		if (!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "Expect ')' after for clauses.");

		Statement body = statement();

		if (increment != null) {
			body = new Statement.BlockStatement(Arrays.asList(body, new Statement.ExpressionStatement(increment)));
		}

		if (condition == null) condition = new Expression.LiteralExpression(true);
		body = new Statement.WhileStatement(keyword, condition, body);

		if (initializer != null) {
			body = new Statement.BlockStatement(Arrays.asList(initializer, body));
		}

		return body;
	}

	private Statement whileStatement() {
		Token keyword = previous();
		consume(LEFT_PAREN, "Expect '(' after 'while'.");
		Expression condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
		Statement body = statement();

		return new Statement.WhileStatement(keyword, condition, body);
	}

	private Statement breakStatement() {
		Token keyword = previous();
		consume(SEMICOLON, "Expect ';' after break statement.");
		return new Statement.BreakStatement(keyword);
	}

	private List<Statement> blockStatement() {
		List<Statement> statements = new ArrayList<>();

		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");

		return statements;
	}

	private Statement declaration() {
		try {
			if (match(FUN)) return function("function");
			if (match(VAR)) return varDeclaration();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	//TODO: type definitions?
	private Statement.FunctionStatement function(String kind) { //TODO: type def of parameters, return
		Token name = null;
		if (match(IDENTIFIER)) {
			name = previous();
		}
		consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		List<Token> parameters = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size() >= 255) {
					error(peek(), "Cannot have more than 255 parameters.");
				}

				parameters.add(consume(IDENTIFIER, "Expect parameter name."));
			} while (match(COMMA));
		}
		consume(RIGHT_PAREN, "Expect ')' after parameters.");

		consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
		List<Statement> body = blockStatement();
		return new Statement.FunctionStatement(name, parameters, body);
	}

	private Statement varDeclaration() {
		Token name = consume(IDENTIFIER, "Expect variable name.");
		Expression initializer = null;

		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration");
		return new Statement.VarStatement(name, initializer);
	}

	private Statement expressionStatement() {
		Expression value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Statement.ExpressionStatement(value);
	}

	private Expression expression() {
		return assignment();
	}

	private Expression assignment() {
//		Expression expression = blockExpression();
		Expression expression = ternary();

		if (match(EQUAL)) {
			Token equals = previous();
			Expression value = assignment();

			if (expression instanceof Expression.VariableExpression) {
				Token name = ((Expression.VariableExpression)expression).name;
				return new Expression.AssignExpression(name, value);
			}

			throw error(equals, "Invalid assignment target.");
		}
		return expression;
	}

	//TODO: interferes with method calls, worth fixing?
	private Expression blockExpression() {
		Expression expression = ternary();

		while (match(COMMA)) {
			Expression right = ternary();
			expression = new Expression.BlockExpression(expression, right);
		}

		return expression;
	}

	private Expression ternary() {
		Expression expression = or();

		while (match(QUESTION)) {
			Token question = previous();
			Expression left = or();
			consume(COLON, "Expect ':' after ternary");
			Expression right = or();
			expression = new Expression.TernaryExpression(question, expression, left, right);
		}

		return expression;
	}

	private Expression or() {
		Expression expression = and();

		while (match(OR)) {
			Token operator = previous();
			Expression right = and();
			expression = new Expression.LogicalExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression and() {
		Expression expression = equality();

		while (match(AND)) {
			Token operator = previous();
			Expression right = equality();
			expression = new Expression.LogicalExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression equality() {
		Expression expression = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expression right = comparison();
			expression = new Expression.BinaryExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression comparison() {
		Expression expression = addition();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expression right = addition();
			expression = new Expression.BinaryExpression(expression, operator, right);
		}

		return expression;
	}

	//TODO: bitwise goes here

	private Expression addition() {
		Expression expression = multiplication();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expression right = multiplication();
			expression = new Expression.BinaryExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression multiplication() {
		Expression expression = unary();

		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expression right = unary();
			expression = new Expression.BinaryExpression(expression, operator, right);
		}

		return expression;
	}

	private Expression unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expression right = unary();
			return new Expression.UnaryExpression(operator, right);
		}

		//+, /, and * must have left-hand operands!
		if (match(PLUS, SLASH, STAR)) {
			throw error(previous(), "Operator '" + previous().lexeme + "' must have a left-hand operand.");
		}

		return call();
	}

	private Expression call() {
		Expression expression = primary();

		while (true) {
			if (match(LEFT_PAREN)) {
				expression = finishCall(expression);
			} else {
				break;
			}
		}

		return expression;
	}

	private Expression finishCall(Expression callee) {
		List<Expression> arguments = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size() >= 255) {
					error(peek(), "Cannot have more than 255 arguments.");
				}
				arguments.add(expression());
			} while (match(COMMA));
		}

		Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

		return new Expression.CallExpression(callee, paren, arguments);
	}

	private Expression primary() {
		if (match(FALSE)) return new Expression.LiteralExpression(false);
		if (match(TRUE)) return new Expression.LiteralExpression(true);
		if (match(NIL)) return new Expression.LiteralExpression(null);
		if (match(FUN)) return new Expression.FunctionExpression(function("function"));

		if (match(NUMBER, STRING)) {
			return new Expression.LiteralExpression(previous().literal);
		}

		if (match(IDENTIFIER)) {
			return new Expression.VariableExpression(previous());
		}

		if (match(LEFT_PAREN)) {
			Expression expression = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expression.GroupingExpression(expression);
		}

		throw error(peek(), "Expect expression.");
	}
}

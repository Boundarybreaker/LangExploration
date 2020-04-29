package space.bbkr.lang.jlox;

import static space.bbkr.lang.jlox.TokenType.*;

import java.util.ArrayList;
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

	//movement
	private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();

		throw error(peek(), message);
	}

	//handling
	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

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
				case PRINT:
				case RETURN:
					return;
			}

			advance();
		}
	}

	//checking
	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}

	//actual parsing
	private Statement statement() {
		if (match(PRINT)) return printStatement();
		if (match(LEFT_BRACE)) return new Statement.BlockStatement(blockStatement());

		return expressionStatement();
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
			if (match(VAR)) return varDelcaration();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Statement varDelcaration() {
		Token name = consume(IDENTIFIER, "Expect variable name.");
		Expression initializer = null;

		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration");
		return new Statement.VarStatement(name, initializer);
	}

	//TODO: remove once standard lib is working
	private Statement printStatement() {
		Expression value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Statement.PrintStatement(value);
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
		Expression expression = blockExpression();

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

	private Expression blockExpression() {
		Expression expression = ternary();

		while (match(COMMA)) {
			Expression right = ternary();
			expression = new Expression.BlockExpression(expression, right);
		}

		return expression;
	}

	private Expression ternary() {
		Expression expression = equality();

		while (match(QUESTION)) {
			Token question = previous();
			Expression left = equality();
			consume(COLON, "Expect ':' after ternary");
			Expression right = equality();
			expression = new Expression.TernaryExpression(question, expression, left, right);
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

		return primary();
	}

	private Expression primary() {
		if (match(FALSE)) return new Expression.LiteralExpression(false);
		if (match(TRUE)) return new Expression.LiteralExpression(true);
		if (match(NIL)) return new Expression.LiteralExpression(null);

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

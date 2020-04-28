package space.bbkr.lang.jlox;

import static space.bbkr.lang.jlox.TokenType.*;

import java.util.List;

import javax.annotation.Nullable;

public class Parser {
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	@Nullable
	Expression parse() {
		try {
			return expression();
		} catch (ParseError e) {
			return null;
		}
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
	private Expression expression() {
		return block();
	}

	private Expression block() {
		Expression expression = ternary();

		while (match(COMMA)) {
			Expression right = ternary();
			expression = new Expression.Block(expression, right);
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
			expression = new Expression.Ternary(question, expression, left, right);
		}

		return expression;
	}

	private Expression equality() {
		Expression expression = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expression right = comparison();
			expression = new Expression.Binary(expression, operator, right);
		}

		return expression;
	}

	private Expression comparison() {
		Expression expression = addition();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expression right = addition();
			expression = new Expression.Binary(expression, operator, right);
		}

		return expression;
	}

	//TODO: bitwise goes here

	private Expression addition() {
		Expression expression = multiplication();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expression right = multiplication();
			expression = new Expression.Binary(expression, operator, right);
		}

		return expression;
	}

	private Expression multiplication() {
		Expression expression = unary();

		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expression right = unary();
			expression = new Expression.Binary(expression, operator, right);
		}

		return expression;
	}

	private Expression unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expression right = unary();
			return new Expression.Unary(operator, right);
		}

		//+, /, and * must have left-hand operands!
		if (match(PLUS, SLASH, STAR)) {
			throw error(previous(), "Operator '" + previous().lexeme + "' must have a left-hand operand.");
		}

		return primary();
	}

	private Expression primary() {
		if (match(FALSE)) return new Expression.Literal(false);
		if (match(TRUE)) return new Expression.Literal(true);
		if (match(NIL)) return new Expression.Literal(null);

		if (match(NUMBER, STRING)) {
			return new Expression.Literal(previous().literal);
		}

		if (match(LEFT_PAREN)) {
			Expression expression = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expression.Grouping(expression);
		}

		throw error(peek(), "Expect expression.");
	}
}

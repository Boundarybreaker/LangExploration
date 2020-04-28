package space.bbkr.lang.jlox;

import static space.bbkr.lang.jlox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

class Scanner {
	private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			//single-char tokens
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ':': addToken(COLON); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case '?': addToken(QUESTION); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;
			//TODO: & and |

			//slash does comments as well
			case '/': {
				if (match('/')) {
					//a comment goes till the end of the line
					while (peek() != '\n' && !isAtEnd()) advance();
				} else if (match('*')) {
					//a block comment can span multiple lines
					while (!isAtEnd() && !(peek() == '*' && peekNext() == '/')) advance();
					current += 2;
				} else {
					addToken(SLASH);
				}
				break;
			}

			//1-2 char tokens
			case '!': addToken(match('=')? BANG_EQUAL : BANG); break;
			case '=': addToken(match('=')? EQUAL_EQUAL : EQUAL); break;
			case '>': addToken(match('=')? GREATER_EQUAL : GREATER); break;
			case '<': addToken(match('=')? LESS_EQUAL : LESS); break;

			//whitespace
			case ' ':
			case '\r':
			case '\t':
				break;
			case '\n':
				line++;
				break;

			//strings
			case '"':
				string();
				break;

			//check for literals and keywords, or error
			default:
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					//none, error
					Lox.error(line, "unexpected character '" + c + "'.");
				}
				break;
		}
	}

	//movement
	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	//building
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, @Nullable Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	//checking
	private boolean isAtEnd() {
		return current >= source.length();
	}

	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z')
				|| c == '_';
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}


	private boolean isAlphanumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	//constructing
	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++;
			advance();
		}

		//unterminated
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string");
			return;
		}

		//closing "
		advance();

		//trim surrounding quotes
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private void number() {
		while (isDigit(peek())) advance();

		//look for a fractional part
		if (peek() == '.' && isDigit(peekNext())) {
			//consume the "."
			advance();

			while (isDigit(peek())) advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private void identifier() {
		while (isAlphanumeric(peek())) advance();

		//see if the identifier is a reserved word
		String text = source.substring(start, current);
		addToken(KEYWORDS.getOrDefault(text, IDENTIFIER));
	}

	static {
		KEYWORDS.put("and", AND);
		KEYWORDS.put("class", CLASS);
		KEYWORDS.put("else", ELSE);
		KEYWORDS.put("false", FALSE);
		KEYWORDS.put("for", FOR);
		KEYWORDS.put("fun", FUN);
		KEYWORDS.put("if", IF);
		KEYWORDS.put("nil", NIL);
		KEYWORDS.put("or", OR);
		KEYWORDS.put("print", PRINT);
		KEYWORDS.put("return", RETURN);
		KEYWORDS.put("super", SUPER);
		KEYWORDS.put("this", THIS);
		KEYWORDS.put("true", TRUE);
		KEYWORDS.put("var", VAR);
		KEYWORDS.put("while", WHILE);
	}
}

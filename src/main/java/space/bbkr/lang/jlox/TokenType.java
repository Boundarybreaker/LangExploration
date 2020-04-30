package space.bbkr.lang.jlox;

enum TokenType {
	//single-char tokens
	//TODO: bitwise & and | once it with int types?
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COLON, COMMA, DOT, MINUS, PLUS, QUESTION, SEMICOLON, SLASH, STAR,

	//1-2 char tokens
	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	//literals
	IDENTIFIER, STRING, NUMBER,

	//types
	//TODO: arrays?
	NUM, BOOL, STR,

	//keywords
	AND, BREAK, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

	//eof
	EOF
}

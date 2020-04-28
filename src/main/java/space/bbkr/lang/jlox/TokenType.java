package space.bbkr.lang.jlox;

enum TokenType {
	//single-char tokens
	//TODO: bitwise & and | once it has int types
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COLON, COMMA, DOT, MINUS, PLUS, QUESTION, SEMICOLON, SLASH, STAR,

	//1-2 char tokens
	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	//literals
	IDENTIFIER, STRING, NUMBER,

	//keywords
	//TODO: move print to standard lib
	AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

	//eof
	EOF
}

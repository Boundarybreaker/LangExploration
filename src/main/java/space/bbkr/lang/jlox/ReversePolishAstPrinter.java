package space.bbkr.lang.jlox;

public class ReversePolishAstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitBinaryExpression(Expression.Binary expression) {
		return stack(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitGroupingExpression(Expression.Grouping expression) {
		return stack("group", expression.expression);
	}

	@Override
	public String visitLiteralExpression(Expression.Literal expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitUnaryExpression(Expression.Unary expression) {
		return stack(expression.operator.lexeme, expression.right);
	}

	private String stack(String name, Expression... expressions) {
		StringBuilder builder = new StringBuilder();
		for (Expression expression : expressions) {
			builder.append(expression.accept(this));
			builder.append(" ");
		}
		builder.append(name);
		return builder.toString();
	}
}

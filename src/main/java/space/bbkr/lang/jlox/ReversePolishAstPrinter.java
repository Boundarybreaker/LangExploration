package space.bbkr.lang.jlox;

class ReversePolishAstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitLiteralExpression(Expression.Literal expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitGroupingExpression(Expression.Grouping expression) {
		return stack("group", expression.expression);
	}

	@Override
	public String visitBlockExpression(Expression.Block expression) {
		StringBuilder builder = new StringBuilder();
		builder.append(expression.left).append(" ");
		builder.append(expression.right);
		return builder.toString();
	}

	@Override
	public String visitUnaryExpression(Expression.Unary expression) {
		return stack(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitBinaryExpression(Expression.Binary expression) {
		return stack(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitTernaryExpression(Expression.Ternary expression) {
		StringBuilder builder = new StringBuilder();
		builder.append(print(expression.positive)).append(" ");
		builder.append(print(expression.negative)).append(" ");
		builder.append(print(expression.condition)).append(" ?");
		return builder.toString();
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

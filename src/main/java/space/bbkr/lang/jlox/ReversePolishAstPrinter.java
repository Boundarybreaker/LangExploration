package space.bbkr.lang.jlox;

class ReversePolishAstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitAssignExpression(Expression.AssignExpression expression) {
		return expression.name.lexeme + print(expression.value) + " =";
	}

	@Override
	public String visitBlockExpression(Expression.BlockExpression expression) {
		StringBuilder builder = new StringBuilder();
		builder.append(expression.left).append(" ");
		builder.append(expression.right).append(" ");
		return builder.toString();
	}

	@Override
	public String visitTernaryExpression(Expression.TernaryExpression expression) {
		StringBuilder builder = new StringBuilder();
		builder.append(print(expression.positive)).append(" ");
		builder.append(print(expression.negative)).append(" ");
		builder.append(print(expression.condition)).append(" ?");
		return builder.toString();
	}

	@Override
	public String visitLogicalExpression(Expression.LogicalExpression expression) {
		return print(expression.left) + " " + print(expression.right) + " " + expression.operator.lexeme;
	}

	@Override
	public String visitBinaryExpression(Expression.BinaryExpression expression) {
		return stack(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitUnaryExpression(Expression.UnaryExpression expression) {
		return stack(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitCallExpression(Expression.CallExpression expression) {
		return stack("call", expression.arguments.toArray(new Expression[0]));
	}

	@Override
	public String visitLiteralExpression(Expression.LiteralExpression expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitVariableExpression(Expression.VariableExpression expression) {
		return expression.name.lexeme;
	}

	@Override
	public String visitGroupingExpression(Expression.GroupingExpression expression) {
		return stack("group", expression.expression);
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

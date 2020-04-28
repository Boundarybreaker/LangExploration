package space.bbkr.lang.jlox;

class Interpreter implements Expression.Visitor<Object> {
	private static class TypeError extends RuntimeError {
		TypeError(Token token, String message) {
			super("TypeError", token, message);
		}
	}

	void interpret(Expression expression) {
		try {
			Object value = evaluate(expression);
			System.out.println(stringify(value));
		} catch (TypeError e) {
			Lox.runtimeError(e);
		}
	}

	private Object evaluate(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public Object visitLiteralExpression(Expression.Literal expression) {
		return expression.value;
	}

	@Override
	public Object visitGroupingExpression(Expression.Grouping expression) {
		return evaluate(expression.expression);
	}

	@Override
	public Object visitBlockExpression(Expression.Block expression) {
		evaluate(expression.left);
		return evaluate(expression.right);
	}

	@Override
	public Object visitUnaryExpression(Expression.Unary expression) {
		Object right = evaluate(expression.right);
		switch (expression.operator.type) {
			case BANG:
				if (right instanceof Boolean) return !(boolean)right;
				else throw new TypeError(expression.operator, "Operand for '!' must be a boolean, but was " + right.toString() + " instead.");
			case MINUS:
				checkDoubleOperand(expression.operator, right);
				return -(double)right;
		}

		//unreachable
		return null;
	}

	@Override
	public Object visitBinaryExpression(Expression.Binary expression) {
		Object left = evaluate(expression.left);
		Object right = evaluate(expression.right);

		switch (expression.operator.type) {
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return (isEqual(left, right));
			case GREATER:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left > (double)right;
			case GREATER_EQUAL:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left >= (double)right;
			case LESS:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left < (double)right;
			case LESS_EQUAL:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left <= (double)right;
			case MINUS:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left - (double)right;
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					return (double)left + (double)right;
				} else if (left instanceof String || right instanceof String) {
					return left.toString() + right.toString();
				} else {
					throw new TypeError(expression.operator, "Operands for '+' must be two numbers or two strings.");
				}
			case SLASH:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left / (double)right;
			case STAR:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left * (double)right;
		}

		return null;
	}

	@Override
	public Object visitTernaryExpression(Expression.Ternary expression) {
		Object result = evaluate(expression.condition);
		if (result instanceof Boolean) {
			if ((boolean)result) {
				return evaluate(expression.positive);
			} else {
				return evaluate(expression.negative);
			}
		}
		throw new TypeError(expression.question, "Operand in ternary must be a boolean, but was " + result.toString() + " instead.");
	}

	private void checkDoubleOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new TypeError(operator, "Operand for '" + operator.lexeme + "' must be a number, but was " + operand.toString() + " instead.");
	}

	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null) return true;
		if (left == null) return false;
		return left.equals(right);
	}

	private String stringify(Object object) {
		if (object == null) return "nil";

		// Hack. Work around Java adding ".0" to integer-valued doubles.
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}

		return object.toString();
	}

}

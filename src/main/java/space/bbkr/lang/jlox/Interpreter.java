package space.bbkr.lang.jlox;

import java.util.List;

class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
	private Environment environment = new Environment();

	void interpret(List<Statement> statements) {
		try {
			for (Statement statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError e) {
			Lox.runtimeError(e);
		}
	}

	private Object evaluate(Expression expression) {
		return expression.accept(this);
	}

	private void execute(Statement statement) {
		statement.accept(this);
	}

	@Override
	public Object visitLiteralExpression(Expression.LiteralExpression expression) {
		return expression.value;
	}

	@Override
	public Object visitVariableExpression(Expression.VariableExpression expression) {
		return environment.get(expression.name);
	}

	@Override
	public Object visitAssignExpression(Expression.AssignExpression expression) {
		Object value = expression.value;

		environment.assign(expression.name, value);
		return value;
	}

	@Override
	public Object visitGroupingExpression(Expression.GroupingExpression expression) {
		return evaluate(expression.expression);
	}

	@Override
	public Object visitBlockExpression(Expression.BlockExpression expression) {
		evaluate(expression.left);
		return evaluate(expression.right);
	}

	@Override
	public Object visitUnaryExpression(Expression.UnaryExpression expression) {
		Object right = evaluate(expression.right);
		switch (expression.operator.type) {
			case BANG:
				if (right instanceof Boolean) return !(boolean)right;
				else throw new RuntimeError("TypeError", expression.operator, "Operand for '!' must be a boolean, but was " + right.toString() + " instead.");
			case MINUS:
				checkDoubleOperand(expression.operator, right);
				return -(double)right;
		}

		//unreachable
		return null;
	}

	@Override
	public Object visitBinaryExpression(Expression.BinaryExpression expression) {
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
					throw new RuntimeError("TypeError", expression.operator, "Operands for '+' must be two numbers or contain one string.");
				}
			case SLASH:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				if ((double)right == 0) throw new RuntimeError("MathError", expression.operator, "Cannot divide by zero.");
				return (double)left / (double)right;
			case STAR:
				checkDoubleOperand(expression.operator, left);
				checkDoubleOperand(expression.operator, right);
				return (double)left * (double)right;
		}

		return null;
	}

	@Override
	public Object visitTernaryExpression(Expression.TernaryExpression expression) {
		Object result = evaluate(expression.condition);
		if (result instanceof Boolean) {
			if ((boolean)result) {
				return evaluate(expression.positive);
			} else {
				return evaluate(expression.negative);
			}
		}
		throw new RuntimeError("TypeError", expression.question, "Operand in ternary must be a boolean, but was " + result.toString() + " instead.");
	}

	@Override
	public Void visitVarStatement(Statement.VarStatement statement) {
		Object value = null;
		if (statement.initializer != null) {
			value = evaluate(statement.initializer);
		}

		environment.define(statement.name.lexeme, value);
		return null;
	}

	@Override
	public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
		evaluate(statement.expression);
		return null;
	}

	@Override
	public Void visitPrintStatement(Statement.PrintStatement statement) {
		Object value = evaluate(statement.expression);
		System.out.println(stringify(value));
		return null;
	}

	private void checkDoubleOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError("TypeError", operator, "Operand for '" + operator.lexeme + "' must be a number, but was " + operand.toString() + " instead.");
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

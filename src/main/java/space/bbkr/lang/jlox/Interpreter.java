package space.bbkr.lang.jlox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
	final Environment globals = new Environment();
	private Environment environment = globals;

	Interpreter() {
		globals.define("print", new LoxCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				System.out.println(stringify(arguments.get(0)));
				return null;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});
		globals.define("clock", new LoxCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000d;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});
	}

	void interpret(List<Statement> statements) {
		try {
			for (Statement statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError e) {
			Lox.runtimeError(e);
		}
	}

	String stringEval(Expression expression) {
		return stringify(evaluate(expression));
	}

	private Object evaluate(Expression expression) {
		return expression.accept(this);
	}

	private void execute(Statement statement) {
		statement.accept(this);
	}

	void executeBlock(List<Statement> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;

			for (Statement statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	@Override
	public Object visitAssignExpression(Expression.AssignExpression expression) {
		Object value = evaluate(expression.value);

		environment.assign(expression.name, value);
		return value;
	}

	@Override
	public Object visitBlockExpression(Expression.BlockExpression expression) {
		evaluate(expression.left);
		return evaluate(expression.right);
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
		throw new RuntimeError("TypeError", expression.question, "Operand in ternary must be a boolean, but was '" + stringify(result) + "' instead.");
	}

	@Override
	public Object visitLogicalExpression(Expression.LogicalExpression expression) {
		boolean left = checkBooleanOperand(expression.operator, evaluate(expression.left));

		if (expression.operator.type == TokenType.OR) {
			if (left) return true;
		} else {
			if (!left) return false;
		}

		return checkBooleanOperand(expression.operator, stringEval(expression.right));
	}

	@Override
	public Object visitBinaryExpression(Expression.BinaryExpression expression) {
		Object left = evaluate(expression.left);
		Object right = evaluate(expression.right);
		if (expression.operator.type == TokenType.PLUS)  {
			if (left instanceof String || right instanceof String) {
				return stringify(left) + stringify(right);
			} else if (!(left instanceof Double && right instanceof Double)) { //just error handling
				throw new RuntimeError("TypeError", expression.operator,
						"Operands for '+' must be two numbers or contain one string, but were '"
								+ stringify(left) + "' and '" + stringify(right) + "' instead.");
			}
		} else if (expression.operator.type == TokenType.BANG_EQUAL) {
			return !isEqual(left, right);
		} else if (expression.operator.type == TokenType.EQUAL_EQUAL) {
			return isEqual(left, right);
		}

		double leftVal = checkDoubleOperand(expression.operator, left);
		double rightVal = checkDoubleOperand(expression.operator, right);

		switch (expression.operator.type) {
			case GREATER:
				return leftVal > rightVal;
			case GREATER_EQUAL:
				return leftVal >= rightVal;
			case LESS:
				return leftVal < rightVal;
			case LESS_EQUAL:
				return leftVal <= rightVal;
			case MINUS:
				return leftVal - rightVal;
			case PLUS:
				return leftVal + rightVal;
			case SLASH:
				if (rightVal == 0) throw new RuntimeError("MathError", expression.operator,
						"Cannot divide by zero.");
				return leftVal / rightVal;
			case STAR:
				return leftVal * rightVal;
		}

		//unreachable
		return null;
	}

	@Override
	public Object visitUnaryExpression(Expression.UnaryExpression expression) {
		Object right = evaluate(expression.right);
		switch (expression.operator.type) {
			case BANG:
				boolean bool = checkBooleanOperand(expression.operator, right);
				return !bool;
			case MINUS:
				double value = checkDoubleOperand(expression.operator, right);
				return -value;
		}

		//unreachable
		return null;
	}

	@Override
	public Object visitCallExpression(Expression.CallExpression expression) {
		Object callee = evaluate(expression.callee);

		if (!(callee instanceof LoxCallable)) {
			throw new RuntimeError("TypeError", expression.paren,
					"Can only call functions and classes.");
		}

		List<Object> arguments = new ArrayList<>();
		for (Expression argument : expression.arguments) {
			arguments.add(evaluate(argument));
		}

		LoxCallable function = (LoxCallable)callee;
		if (arguments.size() != function.arity()) {
			throw new RuntimeError("DefError", expression.paren, "Expected " + function.arity()
					+ " arguments but got " + arguments.size() + " instead.");
		}
		return function.call(this, arguments);
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
	public Object visitGroupingExpression(Expression.GroupingExpression expression) {
		return evaluate(expression.expression);
	}

	@Override
	public Void visitIfStatement(Statement.IfStatement statement) {
		boolean value = checkBooleanOperand(statement.keyword, evaluate(statement.condition));
		if (value) {
			execute(statement.thenBranch);
		} else {
			if (statement.elseBranch != null) execute(statement.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitReturnStatement(Statement.ReturnStatement statement) {
		Object value = null;
		if (statement.value != null) value = evaluate(statement.value);
		throw new Return(value);
	}

	@Override
	public Void visitWhileStatement(Statement.WhileStatement statement) {
		try {
			while (checkBooleanOperand(statement.keyword, evaluate(statement.condition))) {
				execute(statement.body);
			}
		} catch (Break ignored) { }
		return null;
	}

	@Override
	public Void visitBreakStatement(Statement.BreakStatement statement) {
		throw new Break();
	}

	@Override
	public Void visitBlockStatement(Statement.BlockStatement statement) {
		executeBlock(statement.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visitFunctionStatement(Statement.FunctionStatement statement) {
		LoxFunction function = new LoxFunction(statement, environment);
		environment.define(statement.name.lexeme, function);
		return null;
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

	private double checkDoubleOperand(Token operator, Object operand) {
		if (operand instanceof Double) return (double) operand;
		throw new RuntimeError("TypeError", operator, "Operand for '" + operator.lexeme
				+ "' must be a number, but was '" + stringify(operand) + "' instead.");
	}

	private boolean checkBooleanOperand(Token operator, Object operand) {
		if (operand instanceof Boolean) return (boolean) operand;
		throw new RuntimeError("TypeError", operator, "Operand for '" + operator.lexeme
				+ "' must be a boolean, but was '" + stringify(operand) + " instead.");
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

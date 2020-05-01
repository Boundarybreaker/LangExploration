package space.bbkr.lang.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//TODO: type inference and checking
class Resolver implements Expression.Visitor<LoxType>, Statement.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, LoxType>> scopes = new Stack<>();

	private FunctionType currentFunction = FunctionType.NONE;
	private ClassType currentClass = ClassType.NONE;
	private boolean currentWhile = false;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public LoxType visitAssignExpression(Expression.AssignExpression expression) {
		LoxType type = resolve(expression.value);
		//TODO: assignment type checking
		return resolveLocal(expression, expression.name, type);
	}

	@Override
	public LoxType visitTernaryExpression(Expression.TernaryExpression expression) {
		LoxType condition = resolve(expression.condition);
		if (!condition.matches(LoxType.BOOLEAN)) {
			Lox.error(expression.question, "Condition for ternary must be a boolean, but was " +
					condition.lexeme + " instead.");
			return LoxType.UNKNOWN;
		}
		LoxType positive = resolve(expression.positive);
		LoxType negative = resolve(expression.negative);
		if (!positive.matches(negative)) {
			Lox.error(expression.question, "Results for ternary must both be the same type, but were " +
					positive.lexeme + " and " + negative.lexeme + " instead.");
			return LoxType.UNKNOWN;
		}
		return positive;
	}

	@Override
	public LoxType visitLogicalExpression(Expression.LogicalExpression expression) {
		//TODO: typechecking
		LoxType left = resolve(expression.left);
		LoxType right = resolve(expression.right);
		if (!left.matches(LoxType.BOOLEAN) || !right.matches(LoxType.BOOLEAN)) {
			Lox.error(expression.operator, "Operands for '" + expression.operator.lexeme +
					"' must be two booleans, but were " + left.lexeme + " and " + right.lexeme + " instead.");
			return LoxType.UNKNOWN;
		}
		return LoxType.BOOLEAN;
	}

	@Override
	public LoxType visitBinaryExpression(Expression.BinaryExpression expression) {
		LoxType left = resolve(expression.left);
		LoxType right = resolve(expression.right);
		switch (expression.operator.type) {
			case PLUS:
				if (left.matches(LoxType.STRING) || right.matches(LoxType.STRING)) return LoxType.STRING;
				else if (left.matches(LoxType.NUMBER) && right.matches(LoxType.NUMBER)) return LoxType.NUMBER;
				else Lox.error(expression.operator,
							"Operands for '+' must be two numbers or contain one string, but were "
							+ left.lexeme + " and " + right.lexeme + " instead.");
				return LoxType.UNKNOWN;
			case MINUS:
			case STAR:
			case SLASH:
				if (!left.matches(LoxType.NUMBER) || !right.matches(LoxType.NUMBER)) {
					Lox.error(expression.operator, "Operands for '" + expression.operator.lexeme +
							"' must be two numbers, but were " + left.lexeme + " and " + right.lexeme + " instead.");
					return LoxType.UNKNOWN;
				}
			default:
				break;
		}
		return LoxType.NUMBER;
	}

	@Override
	public LoxType visitUnaryExpression(Expression.UnaryExpression expression) {
		LoxType right = resolve(expression.right);
		if (expression.operator.type == TokenType.BANG && !right.matches(LoxType.BOOLEAN)) {
			Lox.error(expression.operator, "Cannot negate a value that is not a boolean.");
			return LoxType.UNKNOWN;
		} else if (expression.operator.type == TokenType.MINUS && !right.matches(LoxType.NUMBER)) {
			Lox.error(expression.operator, "Cannot get the opposite of a value that is not a boolean.");
			return LoxType.UNKNOWN;
		}
		return right;
	}

	//TODO: arg validation, type return
	@Override
	public LoxType visitCallExpression(Expression.CallExpression expression) {
		resolve(expression.callee);
		for (Expression argument : expression.arguments) {
			resolve(argument);
		}
		return LoxType.UNKNOWN;
	}

	@Override
	public LoxType visitGetExpression(Expression.GetExpression expression) {
		return resolve(expression.object);
	}

	@Override
	public LoxType visitSetExpression(Expression.SetExpression expression) {
		LoxType ret = resolve(expression.value);
		resolve(expression.object);
		return ret;
	}

	@Override
	public LoxType visitLiteralExpression(Expression.LiteralExpression expression) {
		return expression.type;
	}

	@Override
	public LoxType visitThisExpression(Expression.ThisExpression expression) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expression.keyword, "Cannot use 'this' outside of a class.");
		}

		return resolveLocal(expression, expression.keyword, LoxType.UNKNOWN);
	}

	@Override
	public LoxType visitVariableExpression(Expression.VariableExpression expression) {
		if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == LoxType.NONE) { //boxed Boolean for nullability
			Lox.error(expression.name, "Cannot read local variable in its own initializer.");
			return LoxType.UNKNOWN;
		}

		return resolveLocal(expression, expression.name, LoxType.UNKNOWN);
//		return scopes.isEmpty()? LoxType.UNKNOWN : scopes.peek().getOrDefault(expression.name.lexeme, LoxType.UNKNOWN);
	}

	@Override
	public LoxType visitGroupingExpression(Expression.GroupingExpression expression) {
		return resolve(expression.expression);
	}

	@Override
	public LoxType visitClassExpression(Expression.ClassExpression expression) {
		resolve(expression.clazz);
		return LoxType.CLASS(expression.clazz.name);
	}

	@Override
	public LoxType visitFunctionExpression(Expression.FunctionExpression expression) {
		resolve(expression.function);
		return LoxType.FUNCTION;
	}

	@Override
	public Void visitIfStatement(Statement.IfStatement statement) {
		resolve(statement.condition);
		resolve(statement.thenBranch);
		if (statement.elseBranch != null) resolve(statement.elseBranch);
		return null;
	}

	//TODO: make sure return types are all the same
	@Override
	public Void visitReturnStatement(Statement.ReturnStatement statement) {
		if (currentFunction == FunctionType.NONE) {
			Lox.error(statement.keyword, "Cannot return from outside a function or method.");
		}
		if (statement.value != null) {
			if (currentFunction == FunctionType.INITIALIZER) {
				Lox.error(statement.keyword, "Cannot return a value from an initializer.");
			}
			resolve(statement.value);
		}
		return null;
	}

	@Override
	public Void visitWhileStatement(Statement.WhileStatement statement) {
		boolean enclosingWhile = currentWhile;
		currentWhile = true;
		resolve(statement.condition);
		resolve(statement.body);
		currentWhile = enclosingWhile;
		return null;
	}

	@Override
	public Void visitBreakStatement(Statement.BreakStatement statement) {
		if (!currentWhile) Lox.error(statement.keyword, "Cannot return from outside a while loop.");
		return null;
	}

	@Override
	public Void visitBlockStatement(Statement.BlockStatement statement) {
		beginScope();
		resolve(statement.statements);
		endScope();
		return null;
	}

	@Override
	public Void visitClassStatement(Statement.ClassStatement statement) {
		ClassType enclosingClass = currentClass;
		currentClass = ClassType.CLASS;
		if (statement.name.type == TokenType.IDENTIFIER) {
			declare(statement.name);
			define(statement.name, LoxType.CLASS(statement.name));
		}

		beginScope();
		scopes.peek().put("this", LoxType.CLASS(statement.name));

		for (Statement.FunctionStatement method : statement.methods) {
			FunctionType declaration = FunctionType.METHOD;
			if (method.name.lexeme.equals("init")) {
				declaration = FunctionType.INITIALIZER;
			}
			resolveFunction(method, declaration);
		}

		endScope();
		currentClass = enclosingClass;
		return null;
	}

	@Override
	public Void visitFunctionStatement(Statement.FunctionStatement statement) {
		if (statement.name.type == TokenType.IDENTIFIER) {
			declare(statement.name);
			define(statement.name, LoxType.FUNCTION);
		}

		resolveFunction(statement, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visitVarStatement(Statement.VarStatement statement) {
		declare(statement.name);
		LoxType type = LoxType.NONE;
		if (statement.initializer != null) {
			type = resolve(statement.initializer);
		}
		define(statement.name, type);
		return null;
	}

	@Override
	public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
		resolve(statement.expression);
		return null;
	}

	void resolve(List<Statement> statements) {
		for (Statement statement : statements) {
			resolve(statement);
		}
	}

	private void resolve(Statement statement) {
		statement.accept(this);
	}

	private LoxType resolve(Expression expression) {
		return expression.accept(this);
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		Map<String, LoxType> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Variable with this name already declared in this scope.");
		}
		scope.put(name.lexeme, LoxType.NONE);
	}

	private void define(Token name, LoxType type) {
		if (scopes.isEmpty()) return;
		scopes.peek().put(name.lexeme, type);
	}

	private LoxType resolveLocal(Expression expression, Token name, LoxType type) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				LoxType scopeType = scopes.get(i).get(name.lexeme);
				interpreter.resolve(expression, scopes.size() - 1 - i);
				if (type != LoxType.UNKNOWN) {
					if (scopeType == LoxType.UNKNOWN) {
						scopes.get(i).put(name.lexeme, type);
						return type;
					} else {
						if (!type.matches(scopeType)) {
							Lox.error(name, "Variable '" + name.lexeme + "' has an established type of " +
									scopeType.lexeme + " but a value of type " + type.lexeme +
									" was set instead.");
						}
					}
				}
				return scopeType;
			}
		}

		//Not found. Assume it's global.
		return LoxType.UNKNOWN;
	}

	//TODO: more advanced for type checking
	private void resolveFunction(Statement.FunctionStatement function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for (Token param : function.parms) {
			declare(param);
			define(param, LoxType.UNKNOWN); //TODO: fix
		}
		resolve(function.body);
		endScope();
		currentFunction = enclosingFunction;
	}

	private void beginScope() {
		scopes.push(new HashMap<>());
	}

	private void endScope() {
		scopes.pop();
	}

	private enum FunctionType {
		NONE,
		FUNCTION,
		INITIALIZER,
		METHOD
	}

	private enum ClassType {
		NONE,
		CLASS
	}
}

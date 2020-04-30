package space.bbkr.lang.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//TODO: type inference and checking
class Resolver implements Expression.Visitor<LoxType>, Statement.Visitor<LoxType> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;
	private boolean currentWhile = false;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public LoxType visitAssignExpression(Expression.AssignExpression expression) {
		resolve(expression.value);
		resolveLocal(expression, expression.name);
		return null;
	}

	@Override
	public LoxType visitTernaryExpression(Expression.TernaryExpression expression) {
		resolve(expression.condition);
		resolve(expression.positive);
		resolve(expression.negative);
		return null;
	}

	@Override
	public LoxType visitLogicalExpression(Expression.LogicalExpression expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public LoxType visitBinaryExpression(Expression.BinaryExpression expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public LoxType visitUnaryExpression(Expression.UnaryExpression expression) {
		resolve(expression.right);
		return null;
	}

	//TODO: arg validation
	@Override
	public LoxType visitCallExpression(Expression.CallExpression expression) {
		resolve(expression.callee);
		for (Expression argument : expression.arguments) {
			resolve(argument);
		}
		return null;
	}

	@Override
	public LoxType visitLiteralExpression(Expression.LiteralExpression expression) { //TODO: return type
		return null;
	}

	@Override
	public LoxType visitVariableExpression(Expression.VariableExpression expression) {
		if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) { //boxed Boolean for nullability
			Lox.error(expression.name, "Cannot read local variable in its own initializer.");
		}

		resolveLocal(expression, expression.name);
		return null;
	}

	@Override
	public LoxType visitGroupingExpression(Expression.GroupingExpression expression) {
		resolve(expression.expression);
		return null;
	}

	@Override
	public LoxType visitFunctionExpression(Expression.FunctionExpression expression) { //TODO: return function type
		return null;
	}

	@Override
	public LoxType visitIfStatement(Statement.IfStatement statement) {
		resolve(statement.condition);
		resolve(statement.thenBranch);
		if (statement.elseBranch != null) resolve(statement.elseBranch);
		return null;
	}

	//TODO: this is gonna make me have to have all statmenets return a LoxType, isn't it...
	@Override
	public LoxType visitReturnStatement(Statement.ReturnStatement statement) {
		if (currentFunction == FunctionType.NONE) {
			Lox.error(statement.keyword, "Cannot return from outside a function or method.");
		}
		if (statement.value != null) {
			resolve(statement.value);
		}
		return null;
	}

	@Override
	public LoxType visitWhileStatement(Statement.WhileStatement statement) {
		boolean enclosingWhile = currentWhile;
		currentWhile = true;
		resolve(statement.condition);
		resolve(statement.body);
		currentWhile = enclosingWhile;
		return null;
	}

	@Override
	public LoxType visitBreakStatement(Statement.BreakStatement statement) {
		if (!currentWhile) Lox.error(statement.keyword, "Cannot return from outside a while loop.");
		return null;
	}

	@Override
	public LoxType visitBlockStatement(Statement.BlockStatement statement) {
		beginScope();
		resolve(statement.statements);
		endScope();
		return null;
	}

	@Override
	public LoxType visitFunctionStatement(Statement.FunctionStatement statement) {
		declare(statement.name);
		define(statement.name);

		resolveFunction(statement, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public LoxType visitVarStatement(Statement.VarStatement statement) {
		declare(statement.name);
		if (statement.initializer != null) {
			resolve(statement.initializer);
		}
		define(statement.name);
		return null;
	}

	@Override
	public LoxType visitExpressionStatement(Statement.ExpressionStatement statement) {
		resolve(statement.expression);
		return null;
	}

	void resolve(List<Statement> statements) {
		for (Statement statement : statements) {
			resolve(statement);
		}
	}

	private LoxType resolve(Statement statement) {
		return statement.accept(this);
	}

	private LoxType resolve(Expression expression) {
		return expression.accept(this);
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Variable with this name already declared in this scope.");
		}
		scope.put(name.lexeme, false);
	}

	private void define(Token name) {
		if (scopes.isEmpty()) return;
		scopes.peek().put(name.lexeme, true);
	}

	private void resolveLocal(Expression expression, Token name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expression, scopes.size() - 1 - i);
				return;
			}
		}

		//Not found. Assume it's global.
	}

	//TODO: more advanced for type checking
	private void resolveFunction(Statement.FunctionStatement function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for (Token param : function.parms) {
			declare(param);
			define(param);
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
		FUNCTION
	}
}

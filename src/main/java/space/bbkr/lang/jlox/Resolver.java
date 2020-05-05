package space.bbkr.lang.jlox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Resolver for pre-run, post parse analysis. Primarily used for var definition and type checking.
 */
class Resolver implements Expression.Visitor<LoxType>, Statement.Visitor<Void> {
	private final Interpreter interpreter;
	private final Map<String, LoxType> globals = new HashMap<>();
	private final Stack<Map<String, LoxType>> scopes = new Stack<>();
	private final Map<String, Map<String, LoxType.FunctionLoxType>> classes = new HashMap<>();
	private final Map<String, List<LoxType>> functions = new HashMap<>();
	private final Map<String, String> heirarchy = new HashMap<>();

	private FunctionType currentFunction = FunctionType.NONE;
	private ClassType currentClass = ClassType.NONE;
	private boolean currentWhile = false;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
		//TODO: better stdlib
		globals.put("print", new LoxType.FunctionLoxType(Collections.singletonList(LoxType.UNKNOWN), LoxType.NONE));
		globals.put("clock", new LoxType.FunctionLoxType(Collections.emptyList(), LoxType.NUMBER));
	}

	@Override
	public LoxType visitAssignExpression(Expression.AssignExpression expression) {
		LoxType type = resolve(expression.value);
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
				return LoxType.NUMBER;
			case GREATER:
			case GREATER_EQUAL:
			case LESS:
			case LESS_EQUAL:
			case EQUAL_EQUAL:
			case BANG_EQUAL:
				if (!left.matches(LoxType.NUMBER) || !right.matches(LoxType.NUMBER)) {
					Lox.error(expression.operator, "Operands for '" + expression.operator.lexeme +
							"' must be two numbers, but were " + left.lexeme + " and " + right.lexeme + " instead.");
					return LoxType.UNKNOWN;
				}
				return LoxType.BOOLEAN;
			default:
				break;
		}
		//unreachable
		return LoxType.UNKNOWN;
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

	//TODO: arg validation, type return for functions/methods
	@Override
	public LoxType visitCallExpression(Expression.CallExpression expression) {
		LoxType type = resolve(expression.callee);
		if (!type.isCallable()) {
			Lox.error(expression.paren,
					"Only classes, functions, and methods can be called, but attempted to call " +
							type.lexeme + " instead.");
		}
		List<LoxType> params = new ArrayList<>();
		if (type instanceof LoxType.FunctionLoxType) {
			LoxType.FunctionLoxType functionType = (LoxType.FunctionLoxType)type;
			type = functionType.returnType;
			params = functionType.paramTypes;
		}
//		if (type instanceof LoxType.ClassLoxType) {
//			LoxType.ClassLoxType classType = (LoxType.ClassLoxType)type;
//			type = new LoxType.InstanceLoxType(classType.name, classType);
//			//TODO: args
//		}
		if (!params.isEmpty() && expression.arguments.size() != params.size()) {
			Lox.error(expression.paren, "Called function expected " + params.size() +
					" arguments, but was given " + expression.arguments.size() + " instead.");
		}
		if (!params.isEmpty()) {
			for (int i = 0; i < expression.arguments.size(); i++) {
				Expression argument = expression.arguments.get(i);
				LoxType argType = resolve(argument);
				LoxType paramType = params.get(i);
				if (!argType.matches(paramType)) {
					Lox.error(expression.paren, "Called function expected an arg of type '" + paramType.lexeme +
							"' but was given an arg of type '" + argType.lexeme + "' instead.");
					return LoxType.UNKNOWN;
				}
			}
		}
		return type;
	}

	@Override
	public LoxType visitGetExpression(Expression.GetExpression expression) {
		LoxType type = resolve(expression.object);
		if (!(type instanceof LoxType.InstanceLoxType)) {
			Lox.error(expression.name,
					"Only instances can have properties, but attempted to get a property on " + type.lexeme +
					" instead.");
			return LoxType.UNKNOWN;
		}
		String name = ((LoxType.InstanceLoxType)type).name.lexeme;
		if (classes.containsKey(name)) {
			Map<String, LoxType.FunctionLoxType> methods = classes.get(name);
			if (methods.containsKey(expression.name.lexeme)) {
				return methods.get(expression.name.lexeme);
			}
		}
		return type; //TODO: does this need to be fixed too?
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
	public LoxType visitSuperExpression(Expression.SuperExpression expression) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expression.keyword, "Cannot use 'super' outside of a class.");
			return LoxType.UNKNOWN;
		}
		if (currentClass == ClassType.CLASS) {
			Lox.error(expression.keyword, "Cannot use 'super' in a class with no superclass.");
		}
		LoxType type = resolveLocal(expression, expression.keyword, LoxType.UNKNOWN);
		if (!(type instanceof LoxType.InstanceLoxType)) {
			Lox.error(expression.keyword, "'super' not defined as an instance on this scope.");
			return LoxType.UNKNOWN;
		}
		String name = ((LoxType.InstanceLoxType)type).name.lexeme;
		if (classes.containsKey(name)) {
			Map<String, LoxType.FunctionLoxType> methods = classes.get(name);
			if (methods.containsKey(expression.method.lexeme)) {
				return methods.get(expression.method.lexeme);
			} else {
				Lox.error(expression.keyword, "Couldn't find method " + expression.method.lexeme +
						" to call super to.");
				return LoxType.UNKNOWN;
			}
		} else {
			Lox.error(expression.keyword, "Couldn't find class for super!");
			return LoxType.UNKNOWN;
		}
	}

	@Override
	public LoxType visitThisExpression(Expression.ThisExpression expression) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expression.keyword, "Cannot use 'this' outside of a class.");
			return LoxType.UNKNOWN;
		}

		return resolveLocal(expression, expression.keyword, LoxType.UNKNOWN);
	}

	@Override
	public LoxType visitVariableExpression(Expression.VariableExpression expression) {
		if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == LoxType.NONE) {
			Lox.error(expression.name, "Cannot read local variable in its own initializer.");
			return LoxType.UNKNOWN;
		}

		return resolveLocal(expression, expression.name, LoxType.UNKNOWN);
	}

	@Override
	public LoxType visitGroupingExpression(Expression.GroupingExpression expression) {
		return resolve(expression.expression);
	}

	@Override
	public LoxType visitClassExpression(Expression.ClassExpression expression) {
		resolve(expression.clazz);
		LoxType supertype = null;
		if (expression.clazz.superclass != null) {
			if (expression.clazz.name.lexeme.equals(expression.clazz.superclass.name.lexeme)) {
				Lox.error(expression.clazz.superclass.name, "A class cannot extend itself.");
			}
			supertype = resolve(expression.clazz.superclass);
			if (!(supertype instanceof LoxType.ClassLoxType)) { //TODO: fix
				Lox.error(expression.clazz.superclass.name, "A class cannot extend a non-class.");
				return null;
			}
		}
		return new LoxType.ClassLoxType(expression.clazz.name, (LoxType.ClassLoxType)supertype); //TODO: fix
	}

	@Override
	public LoxType visitFunctionExpression(Expression.FunctionExpression expression) {
		resolve(expression.function);
		List<LoxType> types = new ArrayList<>();
		for (Expression.ParameterExpression param : expression.function.params) {
			types.add(resolve(param));
		}
		return new LoxType.FunctionLoxType(types, expression.function.returnType);
	}

	@Override
	public LoxType visitParameterExpression(Expression.ParameterExpression expression) {
		return expression.type;
	}

	@Override
	public Void visitIfStatement(Statement.IfStatement statement) {
		LoxType condition = resolve(statement.condition);
		if (!condition.matches(LoxType.BOOLEAN)) {
			Lox.error(statement.keyword, "Condition for if statement must be a boolean, but was " +
					condition.lexeme + " instead.");
		}
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
		LoxType supertype = null; //TODO: still necessary?

		if (statement.superclass != null) {
			currentClass = ClassType.SUBCLASS;
			if (statement.name.lexeme.equals(statement.superclass.name.lexeme)) {
				Lox.error(statement.superclass.name, "A class cannot extend itself.");
			}
			supertype = resolve(statement.superclass);
			if (!(supertype instanceof LoxType.ClassLoxType)) {
				Lox.error(statement.superclass.name, "A class cannot extend a non-class.");
				return null;
			}
		}

		if (statement.name.type == TokenType.IDENTIFIER) {
			LoxType.FunctionLoxType type = new LoxType.FunctionLoxType(Collections.emptyList(), new LoxType.InstanceLoxType(statement.name));
			for (Statement.FunctionStatement method : statement.methods) {
				if (method.name.lexeme.equals("init")) {
					List<LoxType> params = new ArrayList<>();
					for (Expression.ParameterExpression param : method.params) {
						params.add(param.type);
					}
					type = new LoxType.FunctionLoxType(params, new LoxType.InstanceLoxType(statement.name));
				}
			}
			declare(statement.name);
			define(statement.name, new LoxType.ClassLoxType(statement.name, type));
		}

		if (statement.superclass != null) {
			beginScope();
			scopes.peek().put("super", new LoxType.InstanceLoxType(statement.superclass.name));
		}

		beginScope();
		scopes.peek().put("this", new LoxType.InstanceLoxType(statement.name));

		Map<String, LoxType.FunctionLoxType> methods = new HashMap<>();

		if (statement.superclass != null) {
			methods.putAll(classes.get(statement.superclass.name.lexeme));
		}

		for (Statement.FunctionStatement method : statement.methods) {
			FunctionType declaration = FunctionType.METHOD;
			if (method.name.lexeme.equals("init")) {
				declaration = FunctionType.INITIALIZER;
			}
			methods.put(method.name.lexeme, resolveFunction(method, declaration));
		}

		if (statement.name.type == TokenType.IDENTIFIER) {
			classes.put(statement.name.lexeme, methods);
		}

		endScope();
		if (statement.superclass != null) endScope();

		currentClass = enclosingClass;
		return null;
	}

	@Override
	public Void visitFunctionStatement(Statement.FunctionStatement statement) {
		if (statement.name.type == TokenType.IDENTIFIER) {
			declare(statement.name);
			List<LoxType> types = new ArrayList<>();
			for (Expression.ParameterExpression param : statement.params) {
				types.add(resolve(param));
			}
			define(statement.name, new LoxType.FunctionLoxType(types, statement.returnType));
			functions.put(statement.name.lexeme, types);
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
		if (scopes.isEmpty()) {
			globals.put(name.lexeme, LoxType.NONE);
			return;
		}

		Map<String, LoxType> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Variable with this name already declared in this scope.");
		}
		scope.put(name.lexeme, LoxType.NONE);
	}

	private void define(Token name, LoxType type) {
		if (scopes.isEmpty()) {
			globals.put(name.lexeme, type);
			return;
		}
		scopes.peek().put(name.lexeme, type);
	}

	private LoxType resolveLocal(Expression expression, Token name, LoxType type) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				LoxType scopeType = scopes.get(i).get(name.lexeme);
				interpreter.resolve(expression, scopes.size() - 1 - i);
				if (type != LoxType.UNKNOWN) {
					if (scopeType == LoxType.NONE) {
						scopes.get(i).put(name.lexeme, type);
						return type;
					} else {
						if (!type.matches(scopeType)) {
							Lox.error(name, "Variable '" + name.lexeme + "' has an established type of " +
									scopeType.lexeme + " but a value of type " + type.lexeme +
									" was assigned instead.");
						}
					}
				}
				return scopeType;
			}
		}

		//Not found. Assume it's global.
		if (type != LoxType.UNKNOWN) {
			if (!globals.containsKey(name.lexeme)) {
				globals.put(name.lexeme, type);
			} else {
				LoxType globalType = globals.get(name.lexeme);
				if (globalType == LoxType.NONE) {
					globals.put(name.lexeme, type);
					return type;
				} else {
					if (!type.matches(globalType)) {
						Lox.error(name, "Variable '" + name.lexeme + "'has an established type of " +
								globalType.lexeme + " but a value of type " + type.lexeme + " was assigned instead.");
					}
				}
			}
		} else {
			if (globals.containsKey(name.lexeme)) {
				return globals.get(name.lexeme);
			}
		}
		return type;
	}

	//TODO: more advanced for type checking
	private LoxType.FunctionLoxType resolveFunction(Statement.FunctionStatement function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		List<LoxType> paramTypes = new ArrayList<>();
		for (Expression.ParameterExpression param : function.params) {
			declare(param.name);
			define(param.name, param.type);
			paramTypes.add(param.type);
		}
		resolve(function.body);
		endScope();
		currentFunction = enclosingFunction;
		return new LoxType.FunctionLoxType(paramTypes, function.returnType);
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
		CLASS,
		SUBCLASS
	}
}

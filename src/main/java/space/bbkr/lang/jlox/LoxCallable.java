package space.bbkr.lang.jlox;

import java.util.List;

/**
 * Object which is callable by a {@link Expression.CallExpression}.
 */
interface LoxCallable {
	/**
	 * Will likely get removed as this is now checked at compile time
	 * @return How many arguments the callable takes.
	 */
	int arity();

	/**
	 * Might not be used at all, not yet sure
	 * @return The types of all parameters.
	 */
	List<LoxType> getParamTypes();

	/**
	 * Might not be used at all, not yet sure
	 * @return The type that is returned.
	 */
	LoxType getReturnType();

	/**
	 * Call!
	 * @param interpreter The interpreter running the callable.
	 * @param arguments The passed arguments.
	 * @return Whatever gets returned, or null if nothing.
	 */
	Object call(Interpreter interpreter, List<Object> arguments);
}

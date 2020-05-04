package space.bbkr.lang.jlox;

import java.util.List;

interface LoxCallable {
	int arity();
	List<LoxType> getParamTypes();
	LoxType getReturnType();
	Object call(Interpreter interpreter, List<Object> arguments);
}

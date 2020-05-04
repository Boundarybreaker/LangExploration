package space.bbkr.lang.jlox;

import javax.annotation.Nullable;

/**
 * We don't have a call stack in the interpreter, so no real easy way to back out of nested statements.
 * What's a quick, easy way to back out of nested execution in java? throwing!
 * We don't generate a stacktrace as to keep performance,
 * and catch this in the nearest function block.
 */
class Return extends RuntimeException {
	@Nullable
	final Object value;

	Return(@Nullable Object value) {
		super(null, null, false, false);
		this.value = value;
	}
}

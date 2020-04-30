package space.bbkr.lang.jlox;

import javax.annotation.Nullable;

class Return extends RuntimeException {
	@Nullable
	final Object value;

	Return(@Nullable Object value) {
		super(null, null, false, false);
		this.value = value;
	}
}

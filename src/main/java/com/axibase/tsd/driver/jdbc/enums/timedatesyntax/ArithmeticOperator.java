package com.axibase.tsd.driver.jdbc.enums.timedatesyntax;

import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;

public enum ArithmeticOperator implements ITimeDateConstant {
	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"){
		@Override
		public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
			if (secondPrevious == null) {
				throw new IllegalStateException("Expression cannot start with an arithmetic operator");
			}
			if (!(secondPrevious instanceof NumberConstant || secondPrevious instanceof IntervalUnit)) {
				throw new IllegalStateException("Only Number and IntervalUnit can be multiplied");
			}
		}
	};

	private String value;

	ArithmeticOperator(String value) {
		this.value = value;
	}

	@Override
	public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (secondPrevious == null) {
			throw new IllegalStateException("Expression cannot start with an arithmetic operator");
		}
		if (!(secondPrevious instanceof NumberConstant
				|| secondPrevious instanceof IntervalUnit
				|| secondPrevious instanceof EndTime)) {
			throw new IllegalStateException("Addition/substraction can be applied to only Number, IntervalUnit or EndTime constants");
		}

		if ((firstPrevious == PLUS || firstPrevious == MINUS)
				&& (secondPrevious instanceof NumberConstant || secondPrevious instanceof IntervalUnit)) {
			throw new IllegalStateException("You cannot add or substract raw numbers or intervals");
		}
	}

	@Override
	public String value() {
		return value;
	}
}

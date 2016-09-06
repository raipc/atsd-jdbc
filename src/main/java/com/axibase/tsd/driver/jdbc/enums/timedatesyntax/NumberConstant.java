package com.axibase.tsd.driver.jdbc.enums.timedatesyntax;

import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;

public class NumberConstant implements ITimeDateConstant {
	private long value;

	public NumberConstant(long value) {
		this.value = value;
	}

	@Override
	public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (!isValid(firstPrevious, secondPrevious)) {
			if (secondPrevious == ArithmeticOperator.MULTIPLY && !(firstPrevious instanceof IntervalUnit)) {
				throw new IllegalStateException("Only Number and IntervalUnit can be multiplied");
			}
			throw new IllegalStateException("Illegal sequence");
		}
	}

	private boolean isValid(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (secondPrevious == null) {
			return true;
		}
		if (secondPrevious == ArithmeticOperator.MULTIPLY) {
			return firstPrevious instanceof IntervalUnit;
		}
		return secondPrevious instanceof ArithmeticOperator
				&& (firstPrevious instanceof IntervalUnit
					|| firstPrevious instanceof NumberConstant
					|| firstPrevious instanceof EndTime);
	}

	@Override
	public String value() {
		return String.valueOf(value);
	}
}

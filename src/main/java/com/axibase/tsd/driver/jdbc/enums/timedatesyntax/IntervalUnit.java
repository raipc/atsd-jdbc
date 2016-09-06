package com.axibase.tsd.driver.jdbc.enums.timedatesyntax;

import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;

import java.util.Locale;

public enum IntervalUnit implements ITimeDateConstant {
	DAY,
	HOUR,
	MILLISECOND,
	MINUTE,
	MONTH,
	QUARTER,
	SECOND,
	WEEK,
	YEAR;

	@Override
	public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (!validateWithOperator(firstPrevious, secondPrevious)) {
			if (secondPrevious == ArithmeticOperator.MULTIPLY) {
				throw new IllegalStateException("Only Interval and Number can be multiplied");
			}
			throw new IllegalStateException("Illegal sequence");
		}
	}

	private static boolean validateWithOperator(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (secondPrevious == null
				|| (secondPrevious == ArithmeticOperator.MULTIPLY && firstPrevious instanceof NumberConstant)) {
			return true;
		}
		return secondPrevious instanceof ArithmeticOperator
				&& (firstPrevious instanceof IntervalUnit
				|| firstPrevious instanceof NumberConstant
				|| firstPrevious instanceof EndTime);
	}

	@Override
	public String value() {
		return this.name().toLowerCase(Locale.US);
	}
}

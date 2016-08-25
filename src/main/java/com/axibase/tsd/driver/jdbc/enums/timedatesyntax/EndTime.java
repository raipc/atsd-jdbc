package com.axibase.tsd.driver.jdbc.enums.timedatesyntax;

import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;

import java.util.Locale;

public enum EndTime implements ITimeDateConstant {
	CURRENT_DAY,
	CURRENT_HOUR,
	CURRENT_MINUTE,
	CURRENT_MONTH,
	CURRENT_QUARTER,
	CURRENT_WEEK,
	CURRENT_YEAR,
	FIRST_DAY,
	FIRST_VACATION_DAY,
	FIRST_WORKING_DAY,
	FRI,
	FRIDAY,
	LAST_VACATION_DAY,
	LAST_WORKING_DAY,
	MON,
	MONDAY,
	NEXT_DAY,
	NEXT_HOUR,
	NEXT_MINUTE,
	NEXT_MONTH,
	NEXT_QUARTER,
	NEXT_VACATION_DAY,
	NEXT_WEEK,
	NEXT_WORKING_DAY,
	NEXT_YEAR,
	NOW,
	PREVIOUS_DAY,
	PREVIOUS_HOUR,
	PREVIOUS_MINUTE,
	PREVIOUS_MONTH,
	PREVIOUS_QUARTER,
	PREVIOUS_VACATION_DAY,
	PREVIOUS_WEEK,
	PREVIOUS_WORKING_DAY,
	PREVIOUS_YEAR,
	SATURDAY,
	SUN,
	SUNDAY,
	THU,
	THURSDAY,
	TIME,
	TODAY,
	TOMORROW,
	TUE,
	TUESDAY,
	WED,
	WEDNESDAY,
	YESTERDAY;

	@Override
	public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (!isValid(firstPrevious, secondPrevious)) {
			if (secondPrevious instanceof ArithmeticOperator && secondPrevious != ArithmeticOperator.PLUS) {
				throw new IllegalStateException("Only addition to EndTime constant is supported");
			}
			if (secondPrevious == ArithmeticOperator.PLUS) {
				throw new IllegalStateException("IntervalUnit should be used for addition");
			}
			throw new IllegalStateException("Illegal sequence");
		}
	}

	private boolean isValid(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		return secondPrevious == null
				|| secondPrevious == ArithmeticOperator.PLUS
				&& (firstPrevious instanceof IntervalUnit || firstPrevious instanceof NumberConstant);

	}

	@Override
	public String value() {
		return this.name().toLowerCase(Locale.US);
	}
}

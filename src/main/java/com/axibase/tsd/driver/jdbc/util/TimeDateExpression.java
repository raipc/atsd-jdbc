package com.axibase.tsd.driver.jdbc.util;

import com.axibase.tsd.driver.jdbc.enums.timedatesyntax.EndTime;
import com.axibase.tsd.driver.jdbc.enums.timedatesyntax.IsoDateFormat;
import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;

public class TimeDateExpression {
	private ITimeDateConstant[] tokens;

	public TimeDateExpression(String expression) {
		validateExpression(expression);
	}

	private void validateExpression(String expression) {
		String[] strings = expression.split("\\s+");
		final int length = strings.length;
		if (length == 0) {
			throw new IllegalArgumentException("Expression is empty");
		}
		tokens = new ITimeDateConstant[length];
		ITimeDateConstant firstPrevious = null;
		ITimeDateConstant secondPrevious = null;
		ITimeDateConstant current;
		boolean hasEndTimeConstants = false;
		boolean hasIsoTimeExpression = false;
		int index = 0;
		try {
			for (String token : strings) {
				current = EnumUtil.getTimeDateConstantByName(token);
				tokens[index] = current;
				if (current instanceof EndTime) {
					if (hasEndTimeConstants) {
						throw new IllegalArgumentException("Expression should contain only one EndTime constant");
					}
					hasEndTimeConstants = true;
				}
				if (current instanceof IsoDateFormat) {
					if (hasIsoTimeExpression) {
						throw new IllegalArgumentException("Expression may contain only one date-format value");
					}
					hasIsoTimeExpression = true;
				}
				current.validateState(firstPrevious, secondPrevious);
				firstPrevious = secondPrevious;
				secondPrevious = current;
				++index;
			}
		} catch (IllegalStateException e) {
			throw new IllegalArgumentException(buildNewErrorMessage(e, strings, index), e);
		}

	}

	private String buildNewErrorMessage(Exception e, String[] token, int beginIndex) {
		StringBuilder builder = new StringBuilder(e.getMessage())
				.append(": ");
		int length = token.length;
		if (beginIndex <= 2) {
			builder.append("[");
		}
		for (int i = 0; i < length; ++i) {
			if (beginIndex - 2 == i) {
				builder.append("[");
			}
			builder.append(token[i]);
			if (beginIndex == i) {
				builder.append("]");
			}
			builder.append(" ");
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ITimeDateConstant token : tokens) {
			sb.append(token.value()).append(" ");
		}
		return sb.toString();
	}
}

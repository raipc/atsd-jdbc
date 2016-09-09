/*
* Copyright 2016 Axibase Corporation or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
* https://www.axibase.com/atsd/axibase-apache-2.0.pdf
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
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
			throw new IllegalStateException("Addition/subtraction can be applied to only Number, IntervalUnit or EndTime constants");
		}

		if ((firstPrevious == PLUS || firstPrevious == MINUS)
				&& (secondPrevious instanceof NumberConstant || secondPrevious instanceof IntervalUnit)) {
			throw new IllegalStateException("You cannot add or subtract raw numbers or intervals");
		}
	}

	@Override
	public String value() {
		return value;
	}
}

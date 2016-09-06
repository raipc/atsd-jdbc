package com.axibase.tsd.driver.jdbc.intf;

public interface ITimeDateConstant {
	void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious);
	String value();
}

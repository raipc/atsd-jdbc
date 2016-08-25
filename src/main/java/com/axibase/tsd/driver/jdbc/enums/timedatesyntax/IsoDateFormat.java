package com.axibase.tsd.driver.jdbc.enums.timedatesyntax;

import com.axibase.tsd.driver.jdbc.intf.ITimeDateConstant;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;

public class IsoDateFormat implements ITimeDateConstant {
	private static final String[] PATTERNS = {
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd'T'HH:mm:ss'Z'",
			"yyyy-MM-dd'T'HH:mm:ssZZ",
	};
	private String timestamp;

	public IsoDateFormat(String datetime) {
		try {
			int length = datetime.length();
			if (length < 2 || (datetime.charAt(0) != '\'' && datetime.charAt(length - 1) != '\'')) {
				throw new IllegalArgumentException("Unable to parse date-format");
			}
			DateUtils.parseDate(datetime.substring(1, length - 1), PATTERNS);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
		this.timestamp = datetime;
	}

	@Override
	public void validateState(ITimeDateConstant firstPrevious, ITimeDateConstant secondPrevious) {
		if (secondPrevious != null) {
			throw new IllegalStateException("time-format should be used alone in date-time expression");
		}
	}

	@Override
	public String value() {
		return timestamp;
	}
}

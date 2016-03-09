package com.axibase.tsd.driver.jdbc.protocol;

import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class ProtocolFactory {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ProtocolFactory.class);

	public static <T extends IContentProtocol> T create(Class<T> type, ContentDescription cd) {
		try {
			return type.getDeclaredConstructor(ContentDescription.class).newInstance(cd);
		} catch (final IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
			if (logger.isErrorEnabled())
				logger.error("Cannot get an instance from the factory: " + e.getMessage());
		}
		return null;
	}
}
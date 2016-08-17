package com.axibase.tsd.driver.jdbc.strategies;


import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractStrategy implements IStoreStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AbstractStrategy.class);

	protected IConsumer consumer = null;
	protected final StrategyStatus status;
	protected long position;

	protected AbstractStrategy() {
		this.status = new StrategyStatus();
		this.status.setInProgress(true);
		this.consumer = null;
		this.position = 0;
	}

	@Override
	public List<String[]> fetch(long from, int limit) throws AtsdException, IOException {
		final List<String[]> list = new ArrayList<>();
		final Iterator<String[]> iterator = consumer.getIterator();
		while (iterator.hasNext()) {
			final String[] next = iterator.next();
			if (next == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[fetch] no more");
				}
				break;
			}
			if (position < from) {
				if (logger.isTraceEnabled()) {
					logger.trace("[fetch] position less from: " + position + "->" + from);
				}
				position++;
				continue;
			}
			list.add(next);
			if (list.size() >= limit) {
				break;
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("[fetch] sublist size: " + list.size());
		}
		position = from + list.size();
		if (logger.isTraceEnabled()) {
			logger.trace("[fetch] updated position: " + position);
		}
		status.increaseProcessed(list.size());
		return Collections.unmodifiableList(list);
	}
}

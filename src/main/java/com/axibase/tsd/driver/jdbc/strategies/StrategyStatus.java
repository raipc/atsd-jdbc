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
package com.axibase.tsd.driver.jdbc.strategies;

import java.util.concurrent.CountDownLatch;

public class StrategyStatus {
	private boolean inProgress;
	private long lockPosition;
	private long lockSize;
	private long currentSize;
	private long processed;
	private CountDownLatch syncLatch = new CountDownLatch(1);

	public long getLockPosition() {
		return lockPosition;
	}

	public void setLockPosition(long lockPosition) {
		this.lockPosition = lockPosition;
	}

	public long getCurrentSize() {
		return currentSize;
	}

	public void setCurrentSize(long currentSize) {
		this.currentSize = currentSize;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public CountDownLatch getSyncLatch() {
		return syncLatch;
	}

	public void setSyncLatch(CountDownLatch syncLatch) {
		this.syncLatch = syncLatch;
	}

	public long getLockSize() {
		return lockSize;
	}

	public void setLockSize(long lockSize) {
		this.lockSize = lockSize;
	}

	public long getProcessed() {
		return processed;
	}
	
	public void increaseProcessed(int more) {
		this.processed += more;
	}

}
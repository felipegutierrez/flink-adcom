package org.apache.flink.streaming.api.functions.aggregation;

import java.io.Serializable;

public interface PreAggregateTrigger<T> extends Serializable {

	/**
	 * Register a callback which will be called once this trigger decides to finish this bundle.
	 */
	// void registerCallback(PreAggregateTriggerCallback callback);

	void onElement(final T element) throws Exception;

	/**
	 * Reset the trigger to its initiate status.
	 */
	void reset();

	void timeTrigger() throws Exception;

	String explain();

	int getMaxCount();

	long getMaxTime();

	void setMaxCount(int minCount, int subtaskIndex);

	// PreAggregateStrategy getPreAggregateStrategy();
}

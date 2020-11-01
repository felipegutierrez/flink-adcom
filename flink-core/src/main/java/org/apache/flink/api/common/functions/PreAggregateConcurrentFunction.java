package org.apache.flink.api.common.functions;

import org.apache.flink.annotation.Public;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;

/**
 * @param <K> The key to pre-aggregate elements.
 * @param <V> The value to pre-aggregate elements.
 * @param <T> Type of the input elements.
 * @param <O> Type of the output elements.
 */
@Public
public abstract class PreAggregateConcurrentFunction<K, V, T, O> implements Function {
	private static final long serialVersionUID = 1L;

	/**
	 * Adds the given input to the given value, returning the new bundle value.
	 *
	 * @param value the existing bundle value, maybe null
	 * @param input the given input, not null
	 * @throws Exception
	 */
	public abstract V addInput(@Nullable V value, T input) throws Exception;

	/**
	 * Called when a merge is finished. Transform a bundle to zero, one, or more
	 * output elements.
	 */
	public abstract void collect(ConcurrentMap<K, V> buffer, Collector<O> out) throws Exception;
}
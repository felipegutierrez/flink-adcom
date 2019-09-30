/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.examples.partitioning;

import com.google.common.base.Strings;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.examples.partitioning.util.WordSource;
import org.apache.flink.streaming.examples.partitioning.util.WordSourceType;
import org.apache.flink.util.Collector;

/**
 * Implements the "WordCount" program that computes a simple word occurrence
 * histogram over text files in a streaming fashion.
 *
 * <p>The input is a plain text file with lines separated by newline characters.</p>
 *
 * <p>Usage: <code>WordCount --input &lt;path&gt; --output &lt;path&gt;</code><br>
 * If no parameters are provided, the program is run with default data from
 * {@link WordCountPartitioning}.
 * </p>
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition [rebalance|broadcast|shuffle|forward|rescale|global|custom|partial|''] -skew-data-source [true|false] &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition '' -skew-data-source true&
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition rebalance -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition broadcast -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition shuffle -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition forward -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition rescale -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition global -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition custom -skew-data-source true &
 * ./bin/flink run examples/streaming/WordCountPartitioning.jar -partition partial -skew-data-source true &
 *
 * <p>This example shows how to:
 * <ul>
 * <li>write a simple Flink Streaming program,
 * <li>use tuple data types,
 * <li>write and use user-defined functions.
 * </ul>
 * </p>
 */
public class WordCountPartitioning {
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String POLLING_TIMES = "poolingTimes";
	private static final String PARTITION = "partition";
	private static final String PARTITION_TYPE_REBALANCE = "rebalance";
	private static final String PARTITION_TYPE_BROADCAST = "broadcast";
	private static final String PARTITION_TYPE_SHUFFLE = "shuffle";
	private static final String PARTITION_TYPE_FORWARD = "forward";
	private static final String PARTITION_TYPE_RESCALE = "rescale";
	private static final String PARTITION_TYPE_GLOBAL = "global";
	private static final String PARTITION_TYPE_CUSTOM = "custom";
	private static final String PARTITION_TYPE_PARTIAL = "partial";
	private static final String SKEW_DATA_SOURCE = "skew-data-source";

	// *************************************************************************
	// PROGRAM
	// *************************************************************************

	public static void main(String[] args) throws Exception {

		// Checking input parameters
		final ParameterTool params = ParameterTool.fromArgs(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
		env.disableOperatorChaining();

		// make parameters available in the web interface
		env.getConfig().setGlobalJobParameters(params);

		String partitionStrategy = "";
		long poolingTimes;
		String input = params.get(INPUT, WordSourceType.WORDS);
		partitionStrategy = params.get(PARTITION, "");
		poolingTimes = params.getLong(POLLING_TIMES, Long.MAX_VALUE);

		// get input data
		DataStream<String> text;
		if (params.has(INPUT) && !WordSourceType.WORDS.equals(input) && !WordSourceType.WORDS_SKEW.equals(input) && !WordSourceType.FEW_WORDS.equals(input)) {
			// read the text file from given input path
			text = env.readTextFile(params.get(INPUT));
		} else {
			System.out.println("Executing WordCount example with default input data set.");
			System.out.println("Use --input to specify file input.");
			// get default test text data
			text = env.addSource(new WordSource(input, poolingTimes)).name("source-" + partitionStrategy);
		}
		// split lines in strings
		DataStream<Tuple2<String, Integer>> tokenizer = text
			.flatMap(new Tokenizer()).name("tokenizer-" + partitionStrategy)
			.map(new WordReplaceMap()).name("map-" + partitionStrategy);

		// choose a partitioning strategy
		DataStream<Tuple2<String, Integer>> partitionedStream = null;
		DataStream<Tuple2<String, Integer>> partitionedStreamSkew = null;
		if (!Strings.isNullOrEmpty(partitionStrategy)) {
			if (PARTITION_TYPE_REBALANCE.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.rebalance();
			} else if (PARTITION_TYPE_BROADCAST.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.broadcast();
			} else if (PARTITION_TYPE_SHUFFLE.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.shuffle();
			} else if (PARTITION_TYPE_FORWARD.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.forward();
			} else if (PARTITION_TYPE_RESCALE.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.rescale();
			} else if (PARTITION_TYPE_GLOBAL.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.global();
			} else if (PARTITION_TYPE_CUSTOM.equalsIgnoreCase(partitionStrategy)) {
				partitionedStream = tokenizer.partitionCustom(new WordPartitioner(), new WordKeySelector());
			} else if (PARTITION_TYPE_PARTIAL.equalsIgnoreCase(partitionStrategy)) {
				// This implementation was done for KeyedStream @WordCountKeyPartitioning
			} else {
				partitionedStream = tokenizer;
			}
		} else {
			partitionedStream = tokenizer;
		}

		partitionedStream
			.keyBy(0)
			.sum(1).name("sum-" + partitionStrategy)
			.print().name("print-" + partitionStrategy);

		// execute program
		System.out.println(env.getExecutionPlan());
		env.execute(WordCountPartitioning.class.getSimpleName() + " strategy[" + partitionStrategy + "]");
	}

	// *************************************************************************
	// USER FUNCTIONS
	// *************************************************************************

	private static class WordReplaceMap implements MapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>> {
		@Override
		public Tuple2<String, Integer> map(Tuple2<String, Integer> value) throws Exception {
			String v = value.f0
				.replace(":", "")
				.replace(".", "")
				.replace(",", "")
				.replace(";", "")
				.replace("-", "")
				.replace("_", "")
				.replace("\'", "")
				.replace("\"", "");
			return Tuple2.of(v, value.f1);
		}
	}

	private static class WordPartitioner implements Partitioner<String> {
		@Override
		public int partition(String key, int numPartitions) {
			return key.hashCode() % numPartitions;
		}
	}

	private static final class WordKeySelector implements KeySelector<Tuple2<String, Integer>, String> {
		@Override
		public String getKey(Tuple2<String, Integer> value) throws Exception {
			return value.f0;
		}
	}

	/**
	 * Implements the string tokenizer that splits sentences into words as a
	 * user-defined FlatMapFunction. The function takes a line (String) and
	 * splits it into multiple pairs in the form of "(word,1)" ({@code Tuple2<String,
	 * Integer>}).
	 */
	public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
			// normalize and split the line
			String[] tokens = value.toLowerCase().split("\\W+");

			// emit the pairs
			for (String token : tokens) {
				if (token.length() > 0) {
					out.collect(new Tuple2<>(token, 1));
				}
			}
		}
	}
}
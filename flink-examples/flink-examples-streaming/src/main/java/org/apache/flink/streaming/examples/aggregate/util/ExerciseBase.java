package org.apache.flink.streaming.examples.aggregate.util;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class ExerciseBase {
	public final static String pathToRideData = "/home/flink/nycTaxiRides.gz";
	public final static String pathToFareData = "/home/flink/nycTaxiFares.gz";
	public static SourceFunction<TaxiRide> rides = null;
	public static SourceFunction<TaxiFare> fares = null;
	public static SourceFunction<String> strings = null;
	public static SinkFunction out = null;
	public static int parallelism = 4;

	public static SourceFunction<TaxiRide> rideSourceOrTest(SourceFunction<TaxiRide> source) {
		if (rides == null) {
			return source;
		}
		return rides;
	}

	public static SourceFunction<TaxiFare> fareSourceOrTest(SourceFunction<TaxiFare> source) {
		if (fares == null) {
			return source;
		}
		return fares;
	}

	public static SourceFunction<String> stringSourceOrTest(SourceFunction<String> source) {
		if (strings == null) {
			return source;
		}
		return strings;
	}

	public static void printOrTest(org.apache.flink.streaming.api.datastream.DataStream<?> ds) {
		if (out == null) {
			ds.print();
		} else {
			ds.addSink(out);
		}
	}

	public static void printOrTest(org.apache.flink.streaming.api.scala.DataStream<?> ds) {
		if (out == null) {
			ds.print();
		} else {
			ds.addSink(out);
		}
	}
}

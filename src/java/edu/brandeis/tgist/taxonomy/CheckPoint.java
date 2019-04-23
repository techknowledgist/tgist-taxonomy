package edu.brandeis.tgist.taxonomy;

import objectexplorer.ObjectGraphMeasurer;

public class CheckPoint {

	long lastTimestamp;
	long lastMemoryUsage;

	/**
	 * Initialize an instance without setting current timestamp and memory usage.
	 */
	CheckPoint() {
	}

	/**
	 * Initialize an instance while setting current timestamp and memory usage.
	 */
	CheckPoint(boolean initializeUsage) {
		if (initializeUsage)
			reset();
	}

	/**
	 * Reset the current timestamp and memory usage.
	 */
	public final void reset() {
		this.lastTimestamp = System.currentTimeMillis();
		this.lastMemoryUsage = getMemoryUsage();
	}

	private static long getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		memory = memory / (1024 * 1024);
		return memory;
	}

    /**
     * Print the difference between the current memory usage and the usage at the
     * last check point and reset the check point. Also print the time elapsed
     * between this checkpoint and the last one.
     * @param header String to print before printing the memory usage
	 */
	public void report(String header) {
		long currentTimestamp = System.currentTimeMillis();
		long currentMemoryUsage = getMemoryUsage();
		System.out.println("\n" + header);
		System.out.println(String.format(
				"   Time elapsed  =  %dms", currentTimestamp - this.lastTimestamp));
		System.out.println(String.format(
				"   Memory usage  =  %dMb", currentMemoryUsage - this.lastMemoryUsage));
		this.lastTimestamp = currentTimestamp;
		this.lastMemoryUsage = currentMemoryUsage;
	}

	public void showFootPrint(String header, Object obj) {
		System.out.println("\n" + header);
		System.out.println("   " + ObjectGraphMeasurer.measure(obj));
	}


}



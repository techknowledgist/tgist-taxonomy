package edu.brandeis.tgist.taxonomy;

public class Test {

	public static void main(String[] args) {
		doit(null, "");
	}

	static void doit(String x, String depth) {
		if ("        ".equals(depth)) System.exit(0);
		System.out.println(depth + "x=" + x);
		x = "hopsasa";
		for (int i =0; i < 3; i++)
			doit(x, depth + " ");
	}	
}


package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;

public class FeatureVector {

	String fileName;
	String id;
	String term;
	int year;
	String[] features;
	
	FeatureVector(String line) {
		String[] fields = line.split("\t");
		this.fileName = fields[0];
		this.id = fields[1];
		this.year = Integer.parseInt(fields[2]);
		this.term = fields[3];
		this.features = Arrays.copyOfRange(fields, 3, fields.length);
	}

	@Override
	public String toString() {
		return String.format(
				"<FeatureVector %s term='%s' features=%d>",
				this.fileName, this.term, this.features.length);
	}
}

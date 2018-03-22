
package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;

public class FeatureVector {

	String fileName;
	String id;
	String term;
	int year;
	String[] features;

	/**
	 * Create a feature vector from a tab-separated line. The format of the line
	 * is as follows:
	 *
	 *    filename identifier year term feature+
	 *
	 * @param line Tab-separated String.
	 */
	FeatureVector(String line) {
		String[] fields = line.split("\t");
		this.fileName = fields[0];
		this.id = fields[1];
		this.year = Integer.parseInt(fields[2]);
		this.term = fields[3];
		this.features = Arrays.copyOfRange(fields, 4, fields.length);
	}

	@Override
	public String toString() {
		return String.format(
				"<FeatureVector %s term='%s' features=%d>",
				this.fileName, this.term, this.features.length);
	}

	String asTabSeparatedFields() {
		return String.format("%s\t%s\t%d\t%s\t%s\n",
				this.fileName, this.id, this.year, this.term,
				String.join("\t", this.features));
	}
}

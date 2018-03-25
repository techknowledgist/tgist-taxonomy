
package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FeatureVector {

	String fileName;
	String id;
	String term;
	int year;

	/**
	 * A list of features, where each feature is something like "tag_sig=NN_NN"
	 * or "first_word=kalman".
	 */
	String[] features;

	/**
	 * An index of features. For ML purposes "tag_sig=NN_NN" and "tag_sig=DT_NN"
	 * are two different features, but we want to be able to split this and find
	 * the tag signature in a feature vector.
	 */
	Map<String, String> featuresIdx;

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
		this.featuresIdx = new HashMap<>();
		for (String feat : this.features) {
			String[] featval = feat.split("=", 2);
			this.featuresIdx.put(featval[0], featval[1]);
		}
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

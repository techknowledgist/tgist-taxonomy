
package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FeatureVector {

	// just to get all features
	// static Map<String, Integer> FEATS = new HashMap<>();

	static final String DOC_LOC = "doc_loc";
	static final String SECTION_LOC = "section_loc";
	static final String SENT_LOC = "sent_loc";
	static final String PLEN = "plen";
	static final String TAG_SIG = "tag_sig";
	static final String FIRST_WORD = "first_word";
	static final String LAST_WORD = "last_word";
	static final String SUFFIX2 = "suffix3";
	static final String SUFFIX3 = "suffix4";
	static final String SUFFIX4 = "suffix4";
	static final String SUFFIX5 = "suffix5";
	static final String NEXT2_TAGS = "next2_tags";
	static final String NEXT_N2 = "next_n2";
	static final String NEXT_N3 = "next_n3";
	static final String PREV_N2 = "prev_n2";
	static final String PREV_N3 = "prev_n3";
	static final String PREV_NPR = "prev_Npr";
	static final String PREV_VNP = "prev_VNP";
	static final String PREV_J = "prev_J";
	static final String PREV_JPR = "prev_Jpr";
	static final String PREV_V = "prev_V";

	static final String[] FEAT_LIST = {
		DOC_LOC, SECTION_LOC, SENT_LOC, PLEN, TAG_SIG, FIRST_WORD, LAST_WORD,
		SUFFIX2, SUFFIX3, SUFFIX4, SUFFIX5, NEXT2_TAGS, NEXT_N2, NEXT_N3,
		PREV_N2, PREV_N3, PREV_NPR, PREV_VNP, PREV_J, PREV_JPR, PREV_V };

	static Map<String, Integer> FEAT_IDX = new HashMap<>();

	static { setFeatureIndex(); }

	String fileName;
	String id;
	String term;
	String source;
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

	// NOTE: this is for the array where we can find a feature because a feature
	// has a fixed position
	// TODO: figure out how to do the feature access, maybe some kind of lazy
	// initialization of the index. Includes sorting out how we name all the fields.
	String[] featureList;


	/**
	 * Create a feature vector from a tab-separated line. The feature vector
	 * will not have a feature index, instead, the features are simply stored as
	 * an array.
	 *
	 * The format of the line is as follows:
	 *
	 *    filename identifier year term feature+
	 *
	 * This format is exactly how feature vectors are stored in the features.txt
     * file of a taxonomy.
     *
	 * @param line Tab-separated String.
	 */
	FeatureVector(String line) {
		this(line, false);
	}

	/**
	 * Create a feature vector from a tab-separated line. This is very similar to
	 * FeatureVector(String line) except that here we add a flag that determines
	 * whether the vector has a feature index.
     *
	 * @param line Tab-separated String.
	 * @param addIndex A boolean indicating whether to add an index.
	 */
	FeatureVector(String line, boolean addIndex) {

		String[] fields = line.split("\t");
		this.fileName = fields[0];
		this.id = fields[1];
		this.year = Integer.parseInt(fields[2]);
		this.term = fields[3];
		//this.source = line;
		this.features = Arrays.copyOfRange(fields, 4, fields.length);
		if (addIndex) {
			this.featuresIdx = new HashMap<>();
			this.featureList = new String[FEAT_LIST.length]; }
		for (String feat : this.features) {
			String[] featval = feat.split("=", 2);
			if (addIndex) {
				// this adds two kinds of indexes, the first the old map and the
				// second a less memory-intensive array
				// TODO: pick one of these, also having one of these makes the
				//features field expendable
				this.featuresIdx.put(featval[0], featval[1]);
				this.featureList[FEAT_IDX.get(featval[0])] = featval[1]; }
			//int count = FEATS.getOrDefault(featval[0], 0);
			//FEATS.put(featval[0], count + 1);
		}
		//System.out.print(this.featuresIdx);
	}

	static void setFeatureIndex() {
		for (int i = 0; i < FEAT_LIST.length; i++)
			FEAT_IDX.put(FEAT_LIST[i], i);
		//System.out.println(FEAT_IDX);
	}

	@Override
	public String toString() {
		return String.format(
				"<FeatureVector %s term='%s' features=%d>",
				this.fileName, this.term, this.features.length);
	}

	public String getFeature(String feat) {
		return this.featuresIdx.get(feat);
	}

	String asTabSeparatedFields() {
		return String.format("%s\t%s\t%d\t%s\t%s\n",
				this.fileName, this.id, this.year, this.term,
				String.join("\t", this.features));
	}

	public boolean potentiallyRelatedTo(FeatureVector other) {
		if (this.fileName.equals(other.fileName)
				&& this.featuresIdx.get(DOC_LOC).equals(other.featuresIdx.get(DOC_LOC))
				&& other.featuresIdx.get(PREV_V) != null)
			return true;
		else
			return false;
	}

}

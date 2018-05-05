package edu.brandeis.tgist.taxonomy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CooccurrenceWindow {

	List<FeatureVector> vectors;

	CooccurrenceWindow() {
		vectors = new ArrayList<>();
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ");
		for (FeatureVector vector : this.vectors)
			joiner.add(vector.term);
		return "<< " + joiner + " >>";
	}

	/**
	 * Add a vector the the right end of the window and determine whether the
	 * left side of the window needs to slide along.
	 *
	 * @param vector The FeatureVector to add.
	 */
	public void update(FeatureVector vector) {
		this.vectors.add(vector);
		adjustLeft(vector);
	}

	/**
	 * Given the vector just added, determine what elements on the left now fall
	 * outside of the window. This now simply uses a sliding window of two terms
	 * and what the last vector is does not matter, but in time we will look at
	 * the sentence offsets of the vectors.
	 *
	 * @param vector The FeatureVector just added.
	 */
	private void adjustLeft(FeatureVector vector) {
		if (this.vectors.size() > 2) {
			this.vectors.remove(0);
		}
	}

	/**
	 * Get all cooccurrence pairs of the vectors in the window. Given a list of
	 * vectors [V1, V2, ... Vn-1, Vn], this is defined as the list of pairs
	 * [[V1,Vn], [V2,Vn], .... [Vn-1,Vn]].
	 *
	 * @return ArrayList of String pairs.
	 */
	public ArrayList<String[]> cooccurrencePairs() {
		ArrayList<String []> pairs = new ArrayList();
		FeatureVector last = this.vectors.get(this.vectors.size() - 1);
		for (int i = 0; i < this.vectors.size() - 1; i++) {
			String[] pair = { this.vectors.get(i).term, last.term };
			if (! pair[0].equals(pair[1]))
				pairs.add(pair);
		}
		return pairs;
	}
}

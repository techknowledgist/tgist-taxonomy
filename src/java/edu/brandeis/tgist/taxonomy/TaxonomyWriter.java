
package edu.brandeis.tgist.taxonomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TaxonomyWriter {

	/**
	 * Write properties to disk.
	 *
	 * @param pFile
	 * @param lines
	 * @throws IOException
	 */
	static void writeProperties(String pFile, ArrayList<String> lines) throws IOException {
		Files.write(
				Paths.get(pFile),
				lines, StandardCharsets.UTF_8);
	}

	/**
	 * Write technologies to disk.
	 *
	 * @param tFile The File to write to.
	 * @param technologies Map with Technology instances to write to disk.
	 * @throws IOException
	 */
	static void writeTechnologies(
			File tFile, HashMap<String, Technology> technologies)
			throws IOException {

		tFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(tFile), StandardCharsets.UTF_8)) {
			for (String technology : technologies.keySet()) {
				Technology tech = technologies.get(technology);
				writer.write(tech.asTabSeparatedFields());
			}
		}
		System.out.println(String.format("Wrote technologies to %s", tFile));
	}

	/**
	 * Write feature vectors to disk.
	 *
	 * @param vFile  The File to write to.
	 * @param features List with FeatureVector instances to write to disk.
	 * @throws IOException
	 */
	static void writeFeatures(
			File vFile, List<FeatureVector> features)
			throws IOException {

		vFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(vFile), StandardCharsets.UTF_8)) {
			for (FeatureVector vector : features) {
				writer.write(vector.asTabSeparatedFields());
			}
		}
		System.out.println(String.format("Wrote features to %s", vFile));
	}

	/**
	 * Write the hierarchy to disk. All that is written are the isa relations for
	 * those technologies that have them. Note that isa relations are only stored
	 * on the source technologies, that is, we store isa(dog, animal) but we do not
	 * store isa(animal, dog).
	 *
	 * @param hFile
	 * @param taxonomy
	 * @throws IOException
	 */
	static void writeHierarchy(File hFile, Taxonomy taxonomy) throws IOException {
		hFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(hFile), StandardCharsets.UTF_8)) {
			for (Technology technology : taxonomy.technologies.values()) {
				if (technology.isaRelations.size() > 0) {
					writer.write(technology.name + "\n");
					for (IsaRelation isa : technology.isaRelations)
						writer.write(String.format(
								"\tisa\t%s",
								isa.asTabSeparatedString(technology)));
				}
			}
		}
	}

	static void writeRelations(File rFile, Taxonomy taxonomy) throws IOException {
		rFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(rFile), StandardCharsets.UTF_8)) {
			for (Technology technology : taxonomy.technologies.values()) {
				if (technology.relations.size() > 0) {
					writer.write(technology.name + "\n");
					for (String relatedTech : technology.relations.keySet()) {
						Relation rel = technology.relations.get(relatedTech);
						writer.write(String.format(
								"\trel\t%s",
								rel.asTabSeparatedString(technology)));
					}
				}
			}
		}
	}

}

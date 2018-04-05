
package edu.brandeis.tgist.taxonomy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

	static void writeTermsAsTable(String in, String out) throws IOException {
		// TODO: this code structure is repeated below, use functional programming
		System.out.println(out);
		BufferedReader reader = getReader(in);
		BufferedWriter writer = getWriter(out);
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				insertStatementForTechnologiesTable(writer, line);
				//writer.write(line + "\n");
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	static void writeHierarchyAsTable(String inFile, String outFile) throws IOException {
		System.out.println(outFile);
		BufferedReader reader = getReader(inFile);
		BufferedWriter writer = getWriter(outFile);
		try {
			String currentTerm = null;
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("\t")) {
					insertStatementForHierarchyTable(writer, currentTerm, line);
				} else {
					currentTerm = line.trim(); }
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	static void writeRelationsAsTable(String in, String outFile) throws IOException {
		System.out.println(outFile);
		BufferedReader reader = getReader(in);
		BufferedWriter writer = getWriter(outFile);
		try {
			String currentTerm = null;
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("\t")) {
					insertStatementForRelationsTable(writer, currentTerm, line);
					//writer.write(currentTerm + line + "\n");
				} else {
					currentTerm = line.trim(); }
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	private static BufferedReader getReader(String f) throws FileNotFoundException {
		return new BufferedReader(
				new InputStreamReader(
						new FileInputStream(new File(f)),
						StandardCharsets.UTF_8));
	}

	private static BufferedWriter getWriter(String f) throws FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(new File(f)),
						StandardCharsets.UTF_8));
	}

	private static void insertStatementForHierarchyTable(
			BufferedWriter writer, String currentTerm, String line)
			throws IOException {

		String[] fields = line.trim().split("\t");
		String s = String.format(
				"INSERT INTO hierarchy VALUES (%s, %s, %s, %s);\n",
				quote(currentTerm), // source
				quote(fields[0]),	// type
				quote(fields[1]),	// subtype
				quote(fields[2]));	// target
		writer.write(s);
	}

	private static void insertStatementForRelationsTable(
			BufferedWriter writer, String currentTerm, String line)
			throws IOException {

		String[] fields = line.trim().split("\t");
		String s = String.format(
				"INSERT INTO relations VALUES (%s, %s, %s, %s, %s);\n",
				quote(currentTerm), // source
				quote(fields[0]),	// type
				quote(fields[1]),	// subtype
				fields[2],			// count
				quote(fields[3]));	// target
		writer.write(s);
	}

	private static void insertStatementForTechnologiesTable(
			BufferedWriter writer, String line)
			throws IOException {

		//System.out.println(line);
		if (line.startsWith("\t")) return;
		String[] fields = line.trim().split("\t");
		String techName = quote(fields[0]);
		if (techName.endsWith(" ")) return;
		if (techName.contains("  ")) return;
		if (techName.endsWith("^")) return;
		String s = String.format(
				"INSERT INTO technologies VALUES (%s, %s, %s);\n",
				techName,		// name
				fields[1],		// tscore
				fields[2]);		// count
		writer.write(s);
	}

	private static String quote(String text) {
		text = text.replace("\\", "\\\\");
		text = text.replace("\"", "\\\"");
		return "\"" + text + "\"";
	}

	static void writeHierarchyTree(String outFile, Taxonomy taxonomy)
			throws FileNotFoundException, IOException {
		BufferedWriter writer = getWriter(outFile);
		for (Technology technology : taxonomy.technologies.values()) {
			technology.writeHierarchyFragment(writer, "");
		}
	}


}


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
	public static void writeProperties(String pFile, ArrayList<String> lines)
			throws IOException
	{
		Files.write(
				Paths.get(pFile),
				lines, StandardCharsets.UTF_8);
	}

	/**
	 * Write technologies to disk.
	 *
	 * @param termsFile The File to write to.
	 * @param technologies Map with Technology instances to write to disk.
	 * @throws IOException
	 */
	public static void writeTerms(
			String termsFile, HashMap<String, Technology> technologies)
			throws IOException
	{
		try (BufferedWriter writer = getWriter(termsFile)) {
			for (String technology : technologies.keySet()) {
				Technology tech = technologies.get(technology);
				writer.write(tech.asTabSeparatedFields()); }}
		System.out.println(String.format("Wrote technologies to %s", termsFile));
	}

	/**
	 * Write ACT terms to disk.
	 *
	 * Just writes the name of the term and nothing else.
	 *
	 * @param rolesFile
	 * @param acts
	 * @throws IOException
	 */
	public static void writeRoles(
			String rolesFile, List<Technology> acts)
			throws IOException
	{
		try (BufferedWriter writer = getWriter(rolesFile)) {
			for (Technology technology : acts) {
				writer.write(String.format(
						"%s\t%s\n", technology.role, technology.name)); }}
		System.out.println(String.format("Wrote term roles to %s", rolesFile));
	}

	/**
	 * Write feature vectors to disk.
	 *
	 * @param featuresFile  The File to write to.
	 * @param features List with FeatureVector instances to write to disk.
	 * @throws IOException
	 */
	public static void writeFeatures(
			String featuresFile, List<FeatureVector> features)
			throws IOException
	{
		// TODO: the following is a bit cryptic
		// if the list is empty then we already wrote the features
		if (features.isEmpty())
			return;
		try (BufferedWriter writer = getWriter(featuresFile)) {
				for (FeatureVector vector : features) {
					writer.write(vector.asTabSeparatedFields()); }}
		System.out.println(String.format("Wrote features to %s", featuresFile));
	}

	/**
	 * Write the hierarchy to disk. All that is written are the isa relations for
	 * those technologies that have them. Note that isa relations are only stored
	 * on the source technologies, that is, we store isa(dog, animal) but we do not
	 * store isa(animal, dog).
	 *
	 * @param hierarchyFile
	 * @param taxonomy
	 * @throws IOException
	 */
	public static void writeHierarchy(File hierarchyFile, Taxonomy taxonomy)
			throws IOException
	{
		hierarchyFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(hierarchyFile), StandardCharsets.UTF_8)) {
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

	public static void writeCooccurrenceRelations(File relationsFile, Taxonomy taxonomy)
			throws IOException
	{
		relationsFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(relationsFile), StandardCharsets.UTF_8)) {
			for (Technology technology : taxonomy.technologies.values()) {
				if (technology.relations.size() > 0) {
					writer.write(technology.name + "\n");
					for (String relatedTech : technology.relations.keySet()) {
						CooccurrenceRelation rel = technology.relations.get(relatedTech);
						writer.write(String.format(
								"\t%s",
								rel.asTabSeparatedString(technology)));
					}
				}
			}
		}
	}

	public static void writeTermRelations(File relationsFile, Taxonomy taxonomy)
			throws IOException
	{
		relationsFile.createNewFile();
		try (OutputStreamWriter writer =
				new OutputStreamWriter(
					new FileOutputStream(relationsFile), StandardCharsets.UTF_8)) {
			for (Technology technology : taxonomy.technologies.values()) {
				if (technology.termRelations.size() > 0) {
					for (TermRelation rel : technology.termRelations) {
						writer.write(String.format("%s\t%s\t%s\t%s\t%s\n", rel.document, rel.pred,
								rel.source.name, rel.target.name, rel.contextAsTabSeparatedString()));
					}
				}
			}
		}
	}

	// TODO: the following few methods have a lot of overlap, do something nicer

	public static void writeTermsAsTable(Taxonomy tax, String fileName)
			throws FileNotFoundException, IOException
	{
		System.out.println("Creating SQL export for terms...");
		BufferedReader reader = getReader(txtFile(tax.location, fileName));
		BufferedWriter writer = getWriter(sqlFile(tax.location, fileName));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				insertStatementForTechnologiesTable(writer, line);
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	public static void writeHierarchyAsTable(Taxonomy tax, String fileName)
			throws FileNotFoundException, IOException
	{
		System.out.println("Creating SQL export for isa relations...");
		BufferedReader reader = getReader(txtFile(tax.location, fileName));
		BufferedWriter writer = getWriter(sqlFile(tax.location, fileName));
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

	public static void writeCoocRelationsAsTable(Taxonomy tax, String fileName)
			throws FileNotFoundException, IOException
	{
		System.out.println("Creating SQL export for cooccurrence relations...");
		BufferedReader reader = getReader(txtFile(tax.location, fileName));
		BufferedWriter writer = getWriter(sqlFile(tax.location, fileName));
		try {
			String currentTerm = null;
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("\t")) {
					insertStatementForCoocRelationsTable(writer, currentTerm, line);
					//writer.write(currentTerm + line + "\n");
				} else {
					currentTerm = line.trim(); }
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	public static void writeTermRelationsAsTable(Taxonomy tax, String fileName)
			throws FileNotFoundException, IOException
	{
		System.out.println("Creating SQL export for term relations...");
		BufferedReader reader = getReader(txtFile(tax.location, fileName));
		BufferedWriter writer = getWriter(sqlFile(tax.location, fileName));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				insertStatementForTermRelationsTable(writer, line);
			}
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
		}
	}

	private static String txtFile(String directory, String filename)
	{
		return directory + File.separator + filename;
	}

	private static String sqlFile(String directory, String filename)
	{
		String filebase = filename.substring(0, filename.lastIndexOf('.'));
		return directory + File.separator + filebase + ".sql";
	}

	private static BufferedReader getReader(String fileName)
			throws FileNotFoundException
	{
		FileInputStream stream = new FileInputStream(new File(fileName));
		return new BufferedReader(
				new InputStreamReader(stream, StandardCharsets.UTF_8));
	}

	private static BufferedWriter getWriter(String fileName)
			throws FileNotFoundException
	{
		FileOutputStream stream = new FileOutputStream(new File(fileName), false);
		return new BufferedWriter(
				new OutputStreamWriter(stream, StandardCharsets.UTF_8));
	}

	private static void insertStatementForTechnologiesTable(
			BufferedWriter writer, String line)
			throws IOException
	{
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

	private static void insertStatementForHierarchyTable(
			BufferedWriter writer, String currentTerm, String line)
			throws IOException
	{
		String[] fields = line.trim().split("\t");
		String s = String.format(
				"INSERT INTO hierarchy VALUES (%s, %s, %s, %s);\n",
				quote(currentTerm), // source
				quote(fields[0]),	// type
				quote(fields[1]),	// subtype
				quote(fields[2]));	// target
		writer.write(s);
	}

	private static void insertStatementForCoocRelationsTable(
			BufferedWriter writer, String currentTerm, String line)
			throws IOException
	{
		String[] fields = line.trim().split("\t");
		String s = String.format(
				"INSERT INTO relations_cooc VALUES (%s, %s, %s, %s);\n",
				quote(currentTerm), // source
				fields[0],			// count
				fields[1],			// mi
				quote(fields[2]));	// target
		writer.write(s);
	}

	private static void insertStatementForTermRelationsTable(
			BufferedWriter writer, String line)
			throws IOException
	{
		String[] fields = line.trim().split("\t", 5);
		if (fields[1].equals("taken_from"))
			System.out.println(String.join(" --- ", fields));
		String s = String.format(
				"INSERT INTO relations_term VALUES (%s, %s, %s, %s, %s);\n",
				quote(fields[0]),		// document
				quote(fields[1]),		// pred
				quote(fields[2]),		// source
				quote(fields[3]),		// target
				quote(fields[4]));		// context
		writer.write(s);
	}

	private static String quote(String text)
	{
		text = text.replace("\\", "\\\\");
		text = text.replace("\"", "\\\"");
		return "\"" + text + "\"";
	}

	public static void writeHierarchyTree(String outFile, Taxonomy taxonomy)
			throws FileNotFoundException, IOException
	{
		BufferedWriter writer = getWriter(outFile);
		for (Technology technology : taxonomy.technologies.values()) {
			technology.writeHierarchyFragment(writer, "");
		}
	}

}


package edu.brandeis.tgist.taxonomy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class TaxonomyLoader {

	/**
	 * Load a property file.
	 *
	 * @param pFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static Properties loadProperties(String pFile)
			throws FileNotFoundException, IOException {

		Properties properties;
		try (FileInputStream fi = new FileInputStream(pFile)) {
			properties = new Properties();
			properties.load(fi); }
		return properties;
	}

	/**
	 * Load the technologies from disk.
	 *
	 * @param tFile
	 * @param taxonomy
	 * @throws FileNotFoundException
	 */
	static void loadTechnologies(String tFile, Taxonomy taxonomy) throws FileNotFoundException {

		if (new File(tFile).isFile()) {
			System.out.println("Reading technologies...");
			FileInputStream inputStream = new FileInputStream(tFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.split("\t");
				String term = fields[0];
				float score = Float.parseFloat(fields[1]);
				int count = Integer.parseInt(fields[2]);
				taxonomy.technologies.put(term, new Technology(term, score, count));
			}
		}
	}

	static void loadACT(String aFile, Taxonomy taxonomy) throws FileNotFoundException {

		if (new File(aFile).isFile()) {
			System.out.println("Reading ACT terms...");
			FileInputStream inputStream = new FileInputStream(aFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.split("\t");
				String term = fields[0];
				Technology technology = taxonomy.technologies.get(term);
				taxonomy.acts.add(technology);
			}
		}

	}

	/**
	 * Load the feature vectors from disk.
	 *
	 * @param vFile
	 * @param taxonomy
	 * @throws FileNotFoundException
	 */
	static void loadFeatures(String vFile, Taxonomy taxonomy) throws FileNotFoundException {

		if (new File(vFile).isFile()) {
			System.out.println("Reading features...");
			FileInputStream inputStream = new FileInputStream(vFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			CheckPoint checkpoint = new CheckPoint(true);
			int c = 0;
			while (sc.hasNextLine()) {
				c++;
				//if (c > 200) break;
				if ((c % 100000) == 0) System.out.println(String.format("%d", c));
				String line = sc.nextLine();
				taxonomy.features.add(new FeatureVector(line, true));
			}
			checkpoint.report("loadFeatures");
			//System.out.println(FeatureVector.FEATS);
		}
	}

	static void loadHierarchy(String hFile, Taxonomy taxonomy) throws FileNotFoundException {
		if (new File(hFile).isFile()) {
			System.out.println("Reading hierarchy...");
			FileInputStream inputStream = new FileInputStream(hFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			Technology currentTechnology = null;
			int l = 0;
			while (sc.hasNextLine()) {
				l++;
				String line = sc.nextLine();
				//System.out.print(l);
				//System.out.println(" ==> "+line);
				String[] fields = line.trim().split("\t");
				if (fields.length == 1) {
					currentTechnology = taxonomy.technologies.get(fields[0]);
					//System.out.println("  "+currentTechnology);
				} else {
					//System.out.println("  "+fields[0]);
					String reltype = fields[1];
					Technology target = taxonomy.technologies.get(fields[2]);
					// Sometimes the technology is null (and I assume that the target
					// can also be null). Skip these cases.
					// TODO: this is usually when the technology starts/end with
					// a space or tab, take care of those when first importing
					// technologies
					if (currentTechnology != null && target != null) {
						IsaRelation isa = new IsaRelation(reltype, currentTechnology, target);
						currentTechnology.isaRelations.add(isa);
						currentTechnology.hypernyms.add(target);
						target.hyponyms.add(currentTechnology);
					}
				}
			}
		}
	}

	static void loadCooccurrenceRelations(String crFile, Taxonomy taxonomy) throws FileNotFoundException {
		if (new File(crFile).isFile()) {
			System.out.println("Reading cooccurrence relations...");
			FileInputStream inputStream = new FileInputStream(crFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			Technology currentTechnology = null;
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.trim().split("\t");
				if (fields.length == 1) {
					currentTechnology = taxonomy.technologies.get(fields[0]);
				} else {
					int count = Integer.parseInt(fields[0]);
					float mi = Float.parseFloat(fields[1]);
					Technology target = taxonomy.technologies.get(fields[2]);
					currentTechnology.addCooccurrenceRelation(count, mi, target);
					target.addCooccurrenceRelation(count, mi, currentTechnology);
				}
			}
		}
	}

	static void loadTermRelations(String trFile, Taxonomy taxonomy) throws FileNotFoundException {
		if (new File(trFile).isFile()) {
			System.out.println("Reading term relations...");
			FileInputStream inputStream = new FileInputStream(trFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.trim().split("\t");
				String doc = fields[0];
				String pred = fields[1];
				String term1 = fields[2];
				String term2 = fields[3];
				Technology source = taxonomy.technologies.get(term1);
				Technology target = taxonomy.technologies.get(term2);
				TermRelation rel = new TermRelation(doc, pred, source, target);
				for (int i = 4 ;  i < fields.length ; i++)
					rel.addContextElement(fields[i]);
				source.addTermRelation(rel);
				target.addTermRelation(rel);
			}
		}
	}

	/**
	 * Read and add technologies from a file with terms.
	 *
	 * The input file includes the normalized term name (all lower case), the
	 * term count and the technology score. This file is external to the taxonomy
	 * and the terms in the file will be added to the taxonomy if the terms meet
	 * a few conditions on minimal frequency and minimal technology score.
	 *
	 * This adds the technologies to the technologies field but does not save them
	 * to disk, for the latter we use TaxonomyWriter.writeTechnologies().
	 *
	 * @param termsFile
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	static void importTechnologies(
			String termsFile, Taxonomy taxonomy, float minTechScore, int minCount)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {

		FileInputStream fileStream = new FileInputStream(termsFile);
		Reader decoder = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(decoder);
		String line;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split("\t");
			String term = fields[0];
			float score = Float.parseFloat(fields[1]);
			int count = Integer.parseInt(fields[2]);
			if (score >= minTechScore && count >= minCount) {
				Technology ti = new Technology(term, score, count);
		        taxonomy.technologies.put(term, ti);
			}
		}
		System.out.println(
				String.format("Imported %d technologies", taxonomy.technologies.size()));
	}

	/**
	 *
	 * @param actsFile
	 * @param taxonomy
	 * @param minTechScore
	 * @param minCount
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */

	static void importACTS(
			String actsFile, Taxonomy taxonomy)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {

		FileInputStream fileStream = new FileInputStream(actsFile);
		Reader decoder = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(decoder);
		String line;
		int c = 0;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split("\t");
			String term = fields[0].replace('_', ' ');
			String act = fields[1];
			//System.out.println(term);
			Technology technology = taxonomy.technologies.get(term);
			if (technology == null)
				continue;
			// we take them all right now since with these data we do not have enough
			// technologies that qualify as a task
			// if (act.equals("t")) {
			c++;
			taxonomy.acts.add(technology);
			//System.out.println(act + technology);
		}
		System.out.println(
				String.format("Imported %d ACT classes", c));
	}

	/**
	 * Read and add feature vectors.
	 *
	 * Only read the vectors for terms that occur in the technologies map. The
	 * vectors are read from a file that is external to the taxonomy and they
	 * are added if the vector is for a term that occurs in the taxonomy as a
	 * technology. The vectors are assumed to be in a gzipped file.
	 *
	 * Unlike importTechnologies(), this method does not save the loaded data in
	 * a local field, instead it writes the vectors it wants to keep immediately
	 * to the disk. This is because for larger corpora the list of features gets
	 * to be too large to keep in memory.
	 *
	 * @param featuresFile
	 * @throws IOException
	 */
	static void importFeatures(String featuresFile, Taxonomy taxonomy) throws IOException {

		BufferedReader buffered = getGzipReader(featuresFile);
		String fFile = taxonomy.location + File.separator + Taxonomy.FEATURES_FILE;
		System.out.println(fFile);
		FeatureWriter fWriter = new FeatureWriter(new File(fFile));
		String line;
		String filename = null; //, year = null, term = null;
		int c = 0;
		int vectorsAdded = 0;
		while ((line = buffered.readLine()) != null) {
			c++;
			if ((c % 100_000) == 0) System.out.println(c);
			//if (c > 1_000_000) break;
			String[] fields = line.split("\t");
			if ("".equals(fields[0])) {
				String term = fields[3];
				if (taxonomy.technologies.containsKey(term)) {
					// prefix the full filename because the vector initialization
					// code expects that
					FeatureVector vector = new FeatureVector(filename + line);
					fWriter.write(vector.asTabSeparatedFields());
					vectorsAdded++;
				}
			} else {
				filename = fields[0];
			}
		}
		fWriter.close();
		System.out.println(String.format("Imported %d vectors", vectorsAdded));
	}

	/**
	 * Utility to help read a gzipped file.
	 *
	 * @param fileName
	 * @return A BufferedReader
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static BufferedReader getGzipReader(String fileName)
			throws FileNotFoundException, IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(decoder);
		return reader;
	}

}

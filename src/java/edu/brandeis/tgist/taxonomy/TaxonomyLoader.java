
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
				//if ((c % 10000) == 0) System.out.println(".");
				if ((c % 100000) == 0) System.out.println(String.format("%d", c));
				//if (c > 100000) break;
				String line = sc.nextLine();
				taxonomy.features.add(new FeatureVector(line));
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

	static void loadRelations(String rFile, Taxonomy taxonomy) throws FileNotFoundException {
		if (new File(rFile).isFile()) {
			System.out.println("Reading relations...");
			FileInputStream inputStream = new FileInputStream(rFile);
			Scanner sc = new Scanner(inputStream, "UTF-8");
			Technology currentTechnology = null;
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.trim().split("\t");
				if (fields.length == 1) {
					currentTechnology = taxonomy.technologies.get(fields[0]);
					//System.out.println(currentTechnology);
				} else {
					//System.out.println(fields[0]);
					String reltype = fields[1];
					int count = Integer.parseInt(fields[2]);
					Technology target = taxonomy.technologies.get(fields[3]);
					//System.out.println(line);
					currentTechnology.addRelation(reltype, count, target);
					target.addRelation(reltype, count, currentTechnology);
					//Relation rel = new Relation(reltype, currentTechnology, target);
					//currentTechnology.relations.add(rel);
					//target.relations.add(rel);
				}
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
	 * Read and add feature vectors.
	 *
	 * Only read the vectors for terms that occur in the technologies map. The
	 * vectors are read from a file that is external to the taxonomy and they
	 * are added if the vector is for a term that occurs in the taxonomy as a
	 * technology. The vectors are assumed to be in a gzipped file.
	 *
	 * @param featuresFile
	 * @throws IOException
	 */
	static void importFeatures(String featuresFile, Taxonomy taxonomy) throws IOException {

		BufferedReader buffered = getGzipReader(featuresFile);
		String line;
		String filename = null; //, year = null, term = null;
		int c = 0;
		while ((line = buffered.readLine()) != null) {
			c++;
			if ((c % 10000) == 0) System.out.println(c);
			//if (c > 100_000) break;
			String[] fields = line.split("\t");
			if ("".equals(fields[0])) {
				String term = fields[3];
				if (taxonomy.technologies.containsKey(term)) {
					// prepend the full filename because the vector initialization
					// code expects that
					taxonomy.features.add(new FeatureVector(filename + line)); }
			} else {
				filename = fields[0];
			}
		}
		System.out.println(String.format("Imported %d vectors", taxonomy.features.size()));
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


package edu.brandeis.tgist.taxonomy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;


public class Utils
{
	static final String BLUE = "\u001B[34m";
	static final String GREEN = "\u001B[32m";
	static final String RED = "\u001B[31m";
	static final String BOLD = "\u001B[1m";
	static final String UNDER = "\u001B[4m";
	static final String END = "\u001B[0m";

	
	public static String bold(String text)
	{
		return BOLD + text + END;
	}

	public static String red(String text)
	{
		return RED + text + END;
	}

	public static String blue(String text)
	{
		return BLUE + text + END;
	}
	
	public static void warning(String msg) {
		// TODO: this does not work for the windows command prompt
		System.out.println(RED + msg + END);
	}


	/**
	 * Utility to read a file.
	 *
	 * @param fileName
	 * @return A BufferedReader for fileName
	 * @throws FileNotFoundException
	 */

	public static BufferedReader getReader(String fileName)
			throws FileNotFoundException
	{
		FileInputStream fileStream = new FileInputStream(fileName);
		Reader decoder = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(decoder);
		return reader;
	}

	/**
	 * Utility to read a gzipped file.
	 *
	 * @param fileName
	 * @return A BufferedReader
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public static BufferedReader getGzipReader(String fileName)
			throws FileNotFoundException, IOException
	{
		FileInputStream fileStream = new FileInputStream(fileName);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(decoder);
		return reader;
	}

}


package edu.brandeis.tgist.taxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Specialized class for writing features because we cannot just get the list of
 * all features and dump it to disk because that list is too large. 
 */
public class FeatureWriter {

	OutputStreamWriter writer;
	
	FeatureWriter(File file) throws FileNotFoundException {
		this.writer =
				new OutputStreamWriter(
					new FileOutputStream(file), StandardCharsets.UTF_8);
	}
	
	public void write(String text) throws IOException {
		this.writer.write(text);
	}
	
	public void close() throws IOException {
		this.writer.close();
	}	
}

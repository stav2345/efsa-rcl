package zip_manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZipManager {
	
	/**
	 * Write a file reading it from the zip stream
	 * @param zipStream
	 * @param filename
	 */
	public static void unzipStream(InputStream inputStream, File file) {
		
		try (ZipInputStream zipStream = new ZipInputStream(inputStream);
				FileOutputStream fos = new FileOutputStream(file);) {
			
			// get the next entry
			zipStream.getNextEntry();
			
			final byte[] buf = new byte[2000];
			
			int length;
			
			// write until there is something
			while ((length = zipStream.read(buf, 0, buf.length)) >= 0) {
				fos.write(buf, 0, length);
			}
			
			fos.close();
			zipStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

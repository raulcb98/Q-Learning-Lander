package raulcastilla215alu.mytools;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Defines an input-output module.
 * 
 * @author Ricardo Manuel Ruiz Diaz.
 * @author Raul Castilla Bravo.
 */
public class IOModule {

	/**
	 * Writes the content in the path.
	 * 
	 * @param path file to save the content.
	 * @param content information to be saved.
	 * @param append true to append the information at the end of the file.
	 */
	public static void write(String path, String content, boolean append) {
		
		FileWriter fichero = null;
		PrintWriter pw = null;
		
		try {
			
			fichero = new FileWriter(path,append);
			pw = new PrintWriter(fichero);

			pw.print(content);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fichero)
					fichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
}

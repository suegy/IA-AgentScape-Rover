/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is "jsave".
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2000. All Rights Reserved.
 *
 * Protege-2000 and "jsave" were developed by the Stanford Center for 
 * Biomedical Informatics Research (http://bmir.stanford.edu) at the 
 * Stanford University School of Medicine with support from the National 
 * Library of Medicine, the National Science Foundation, and the Defense 
 * Advanced Research Projects Agency. Current information about Protege can 
 * be obtained at http://protege.stanford.edu/.
 *
 * Contributor(s):
 *    The jsave extension to Protege-2000 was written by Samson Tu
 */
package bath.jsave;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

public class protege2java {

	private static JavaClsStorer jsave;
  
	public protege2java() {
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("testjsave needs initialization file as an argument");
			return;
		}
		String initfile = args[0];
		System.out.println("protege2java:initfile: " + initfile);
		Properties settings = new Properties();
		try {
			FileInputStream sf = new FileInputStream(initfile);
			settings.load(sf);
		} catch (Exception ex) {
			System.out.println("Exception during loading initialization file");
			System.out.println(ex.toString());
		}
		String kbURL = settings.getProperty("KBURL", "");
		String outputDir = settings.getProperty("OUTPUTDIR", "");
		String packageName = settings.getProperty("PACKAGENAME", "");
		String headerString = settings.getProperty("HEADERSTRING", "");
		String importString = settings.getProperty("IMPORT", "");
		String saveIncludedString = settings.getProperty("SAVEINCLUDED");
		String topClass = settings.getProperty("TOPCLASS");
		String saveFormat = settings.getProperty("SAVEFORMAT", "");

		int stringIndex = 0;
		boolean hasNext = true;
		Collection<String> topClasses = new ArrayList<String>();
		if (topClass == null) {
			System.out.println("Null top class!");
			System.exit(-1);
		}
		while (hasNext) {
			int endIndex = topClass.indexOf(",", stringIndex);
			if (endIndex < 0) {
				topClasses.add(topClass.substring(stringIndex, topClass
						.length()));
				hasNext = false;
			} else {
				topClasses.add(topClass.substring(stringIndex, endIndex));
				stringIndex = endIndex + 1;
			}
		}

		KnowledgeBase kb;
		Project project;
		Collection<String> error_messages = new ArrayList<String>();
		jsave = new JavaClsStorer();

		jsave.setSaveFormat(saveFormat);

		if (saveIncludedString.equals("true"))
			jsave.setSaveIncludedFlag(true);
		else
			jsave.setSaveIncludedFlag(false);

		try {
			jsave.setImportString(importString);
			jsave.setPackageName(packageName);
			jsave.setOutputDir(outputDir);
			jsave.setHeaderString(headerString);
			project = Project.loadProjectFromFile(kbURL, error_messages);
			kb = project.getKnowledgeBase();
			for (Iterator i = topClasses.iterator(); i.hasNext();) {
				String nextTopClass = (String) i.next();
				jsave.storeClses(kb, nextTopClass);
			}
			System.out.println("Finished");
		} catch (Throwable e) {
			System.out.println("Error loading project: " + kbURL);
			e.printStackTrace();
			System.exit(1);

		}
	}
}

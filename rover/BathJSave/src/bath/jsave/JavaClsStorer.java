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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;

/**
 * A class to save Protege Java classes as Java classes and stub 
 * implementation classes.
 *
 */
public class JavaClsStorer extends java.lang.Object {
    
	public enum SaveFormat {
		Regular, PoJo, Javabean, NoAccessMethod
	}

	private Collection itsStoredClses = new HashSet();
	private String defaultClassName = "Object";
	private Properties typeConversionTable;
	private Properties typeCastTable;
	private Properties typeAccessTable;

	// configuration parameters
	private String importString;
	private File directory = null;
	private String packageName;
	private String headerString;
	private SaveFormat saveFormat = SaveFormat.Regular; // can be Regular,
														// Javabean, or
														// NoAccessMethod
	private boolean saveIncluded = false;

	public JavaClsStorer() {
		this.typeConversionTable = new Properties();
		this.typeCastTable = new Properties();
		this.typeAccessTable = new Properties();

		typeConversionTable.put(ValueType.ANY.toString(), "Instance");
		typeConversionTable.put(ValueType.BOOLEAN.toString(), "boolean");
		typeConversionTable.put(ValueType.CLS.toString(), "Cls");
		typeConversionTable.put(ValueType.FLOAT.toString(), "float");
		typeConversionTable.put(ValueType.INSTANCE.toString(), "Instance");
		typeConversionTable.put(ValueType.INTEGER.toString(), "int");
		typeConversionTable.put(ValueType.STRING.toString(), "String");
		typeConversionTable.put(ValueType.SYMBOL.toString(), "String");

		typeCastTable.put(ValueType.ANY.toString(), "(Instance)");
		typeCastTable.put(ValueType.BOOLEAN.toString(), "(Boolean)");
		typeCastTable.put(ValueType.CLS.toString(), "(Cls)");
		typeCastTable.put(ValueType.FLOAT.toString(), "(Float)");
		typeCastTable.put(ValueType.INSTANCE.toString(), "(Instance)");
		typeCastTable.put(ValueType.INTEGER.toString(), "(Integer)");
		typeCastTable.put(ValueType.STRING.toString(), "(String)");
		typeCastTable.put(ValueType.SYMBOL.toString(), "(String)");

		typeAccessTable.put(ValueType.ANY.toString(), "");
		typeAccessTable.put(ValueType.BOOLEAN.toString(), ".booleanValue()");
		typeAccessTable.put(ValueType.CLS.toString(), "");
		typeAccessTable.put(ValueType.FLOAT.toString(), ".floatValue()");
		typeAccessTable.put(ValueType.INSTANCE.toString(), "");
		typeAccessTable.put(ValueType.INTEGER.toString(), ".intValue()");
		typeAccessTable.put(ValueType.STRING.toString(), "");
		typeAccessTable.put(ValueType.SYMBOL.toString(), "");
	}

	void setImportString(String importSrcString) throws IOException {
		importString = "";
		boolean hasNext = true;
		int stringIndex = 0;

		while (hasNext) {
			int endIndex = importSrcString.indexOf(";", stringIndex);
			if (endIndex < 0) {
				importString = importString
						+ "\n"
						+ importSrcString.substring(stringIndex,
								importSrcString.length()) + "\n";
				hasNext = false;
			} else {
				importString = importString
						+ "\n"
						+ (importSrcString.substring(stringIndex, 
								endIndex + 1));
				stringIndex = endIndex + 1;
			}
		}
	}

	void setSaveIncludedFlag(boolean saveIncluded) {
		this.saveIncluded = saveIncluded;
	}

	void setPackageName(String packageName) throws Exception {
		if (packageName.equals("")) {
			throw new Exception("Empty string is not a legal package name");
		} else {
			this.packageName = packageName;
		}
	}

	void setOutputDir(String path) throws FileNotFoundException {
		File pathFile = new File(path);
		if (pathFile.exists()) {
			this.directory = pathFile;
		} else
			throw new FileNotFoundException("Output directory " + path
					+ " not found");
	}

	void setHeaderString(String header) {
		headerString = header;
	}

	void setSaveFormat(String saveFormat) {
		for (SaveFormat format : SaveFormat.values()) {
			if (format.name().equals(saveFormat)) {
				this.saveFormat = format;
				return;
			}
		}
		throw new RuntimeException("Unknown save format " + saveFormat);
	}

	void storeClses(KnowledgeBase kb, String topClassName) {
		// System.out.println("import string: "+ importString + " package:
		// "+packageName+
		// " save directory: "+ directory + " header string: "+ headerString);
		Cls topClass = kb.getCls(topClassName);
		if (topClass == null) {
			System.out.println("Error: " + topClassName + " is not a class");
			Collection clses = kb.getClses();
			for (Iterator i = clses.iterator(); i.hasNext();) {
				System.out.println(((Cls) i.next()).getName() + " is a class");
			}
		} else {
			storeCls(topClass, true);
			itsStoredClses.add(topClass);
			storeSubclasses(topClass);
		}
	}

	private void storeClsAndSubclasses(Cls cls) {
		if (!isMetaclass(cls)) {
			storeCls(cls, false);
		}
		itsStoredClses.add(cls);
		storeSubclasses(cls);
	}

	private void storeSubclasses(Cls cls) {
		List subclasses = new ArrayList(cls.getDirectSubclasses());
		Iterator i = subclasses.iterator();
		while (i.hasNext()) {
			Cls subclass = (Cls) i.next();
			if (!itsStoredClses.contains(subclass)) {
				storeClsAndSubclasses(subclass);
			}
		}
	}

	private void storeHeaderInformation(PrintWriter itsWriter) {
		itsWriter.println("// Created on " + new Date().toString());
		itsWriter.println("// " + this.headerString);
	}

	private boolean isMetaclass(Cls cls) {
		String name = cls.getName();
		return name.equals(Model.Cls.CLASS) || name.equals(Model.Cls.SLOT)
				|| name.equals(Model.Cls.FACET);
	}

	private void storeCls(Cls cls, boolean isTopClass) {
		File classFile = new File(directory, cls.getName() + ".java");
		;
		File oldClassFile = new File(directory, cls.getName() + ".java~");
		;
		PrintWriter itsWriter = null; // Note that the writer changes for each class

		if (!(cls.isIncluded()) || saveIncluded) {

			// Check to see if class already exists
			if (classFile.exists()) {
				if (oldClassFile.exists()) {
					oldClassFile.delete();
				}
				if (!classFile.renameTo(oldClassFile)) {
					System.out.println(cls.getName() + ".java: "
							+ "problem saving old files! Exit.");
					System.exit(1);
				}
			}
			try {
				itsWriter = new PrintWriter(new FileWriter(classFile), true);

			} catch (Throwable e) {
				System.out.println("Error writing to" + classFile);
				e.printStackTrace();
				System.exit(1);
			}
			itsWriter.println();
			storeHeaderInformation(itsWriter);
			itsWriter.println();
			itsWriter.println("package " + packageName + ";");
			itsWriter.println(this.importString);
			writeClassComment(cls, itsWriter);
			if (cls.isAbstract())
				itsWriter.print("public abstract class ");
			else {
				itsWriter.print("public class ");
			}
			itsWriter.println(cls.getName() + " extends "
					+ getSuperclass(cls, isTopClass) + " implements Serializable {");

			writeClassConstructor(cls, itsWriter);

			writeClassSlots(cls, itsWriter);

			if (saveFormat == SaveFormat.Javabean) {
				// put in the code for listener here
				itsWriter.println("/* writing listener */");
				writeListener(itsWriter);
			}
			itsWriter.println("// __Code above is automatically generated. Do not change");
			if (oldClassFile.exists()) {
				preserverUserCode(oldClassFile, itsWriter);
			} else
				itsWriter.println('}');

		} // endif included
	}

	private void writeClassComment(Cls cls, PrintWriter itsWriter) {
		Collection comments = cls.getDocumentation();
		if (comments != null) {
			Iterator i = comments.iterator();
			itsWriter.println("/** ");
			while (i.hasNext()) {
				itsWriter.println(" *  " + (String) i.next());
			}
			itsWriter.println(" */");
		}
	}

	private String getSuperclass(Cls cls, boolean isTopClass) {
		if (isTopClass)
			return defaultClassName;
		List superclasses = new ArrayList(cls.getDirectSuperclasses());
		if (superclasses.size() == 1) {
			Iterator i = superclasses.iterator();
			if (i.hasNext()) {
				Cls superclass = (Cls) i.next();
				if (clsIsRoot(superclass)) {
					return defaultClassName;
				} else {
					return superclass.getName();
				}
			} else {
				System.out.println("Error: No legal superclass "
						+ cls.getName());
				return "Error";
			}
		} else if (superclasses.size() > 1) {
			String superclassToUse = getSpecifiedSuper(cls);
			if (superclassToUse != null)
				return superclassToUse;
		}
		System.out.println("Error: No legal superclass " + cls.getName());
		return "Error";
	}

	private String getSpecifiedSuper(Cls cls) {
		String commentString;
		String className = null;
		Collection comments = cls.getDocumentation();
		if (comments != null) {
			Iterator i = comments.iterator();
			while (i.hasNext()) {
				commentString = (String) i.next();
				String firstWord = getFirstWord(commentString);
				if (firstWord != null) {
					if (cls.getKnowledgeBase().getCls(firstWord) != null) {
						return firstWord;
					}
				}
			}
		}
		return className;
	}

	public String getFirstWord(String text) {
		int offset = 0;
		char[] textChars = new char[text.length()];
		text.getChars(0, text.length(), textChars, 0);
		for (int i = 0; i < text.length(); i++) {
			if (textChars[i] == ' ' || textChars[i] == '\t'
					|| textChars[i] == '\n' || textChars[i] == '\r') {
				offset = i;
				break;
			}
		}
		if (offset == 0)
			return text;
		else
			return text.substring(0, offset);
	}

  	private boolean isProperClass(Cls cls) {
		boolean properClass = true;
		String commentString;
		Collection comments = cls.getDocumentation();
		if (comments != null) {
			Iterator i = comments.iterator();
			while (i.hasNext()) {
				commentString = (String) i.next();
				if (commentString.indexOf("_interface_") >= 0)
					properClass = false;
			}
		}
		return properClass;
	}
  
  	private void writeClassSlots(Cls cls, PrintWriter itsWriter) {
		List slots = new ArrayList(cls.getTemplateSlots());
		Iterator i = slots.iterator();
		while (i.hasNext()) {
			Slot slot = (Slot) i.next();
			if (cls.hasDirectTemplateSlot(slot)) {
				//if (saveFormat == SaveFormat.Javabean) {
				//	writeClassSlotAsBean(cls, slot, itsWriter);
				//} else if (saveFormat == SaveFormat.Regular
				//		|| saveFormat == SaveFormat.PoJo) {
					writeClassSlotImp(cls, slot, itsWriter);
				//}
			}
		}
	}

  	// Write the accessor and mutator for the slot
  	private void writeClassSlotImp(Cls cls, Slot slot, PrintWriter itsWriter) {
		// write mutator
		String slotName = slot.getName();
		String variableName = variableName(slotName);
		String argType = argumentType(cls, slot);
		String newValue = variableName;
		// System.out.println("cls: "+cls.toString() + " slot:
		// "+slot.toString());

		if (argType.equals("boolean"))
			newValue = "new  Boolean(" + variableName + ")";
		else if (argType.equals("int"))
			newValue = "new  Integer(" + variableName + ")";
		else if (argType.equals("float"))
			newValue = "new Float(" + variableName + ")";

		String methodName = methodName(slotName);
		
		
		if (cls.getTemplateSlotAllowsMultipleValues(slot)) {
			itsWriter.print("\n\tprivate Collection<" + argType + "> " + variableName + ";");
			itsWriter.print("\n\tpublic void set" + methodName + "(");
			itsWriter.print("Collection<" + argType + "> " + variableName + ") {\n");
			itsWriter.print("\t\tthis." + variableName + " = " + variableName + ";\n");
		} else {
			itsWriter.print("\n\tprivate " + argType + " " + variableName + ";");
			itsWriter.print("\n\tpublic void set" + methodName + "(");
			itsWriter.print(argType + " " + variableName + ") {\n");
			itsWriter.print("\t\tthis." + variableName + " = " + variableName + ";\n");
			
		}
		itsWriter.print("\t}\n");

		// itsWriter.print("\t\tthis." + variableName + " = " + variableName +
		// ";\n\t}\n");

		// Write accessor
		itsWriter.print("\tpublic ");
		if (cls.getTemplateSlotAllowsMultipleValues(slot)) {
			itsWriter.print("Collection<" + argType + "> " + "get" + methodName + "(){\n");
			itsWriter
					.print("\t\treturn this." + variableName + ";\n\t}\n");
		} else {
			if (argumentType(cls, slot).equals("boolean")) {
				itsWriter.print("boolean is" + methodName + "() {\n");
			} else {
				itsWriter.print(argType + " get" + methodName
						+ "() {\n");
			}
			itsWriter.print("\t\treturn this." + variableName + ";\n\t}\n");
		}
		// itsWriter.print("\t\treturn this." + variableName + ";\n\t}\n");
	}

  	private void writeClassSlotAsBean(Cls cls, Slot slot, PrintWriter itsWriter) {
		String slotName = slot.getName();
		String variableName = variableName(slotName);
		String argType = argumentType(cls, slot);
		String methodName = methodName(slotName);
		String newValue = variableName;
		String type = argType;
		// System.out.println("cls: "+cls.toString() + " slot:
		// "+slot.toString());
		if (argType.equals("boolean")) {
			type = "Boolean";
			newValue = "new " + type + "(" + variableName + ")";
		} else if (argType.equals("int")) {
			type = "Integer";
			newValue = "new " + type + "(" + variableName + ")";
		} else if (argType.equals("float")) {
			type = "Float";
			newValue = "new " + type + "(" + variableName + ")";
		}

		// Write mutator
		itsWriter.print("\n\tpublic void set" + methodName + "(");
		if (cls.getTemplateSlotAllowsMultipleValues(slot)) {
			itsWriter.print(type + "[] " + variableName + ") {\n");
			itsWriter.println("\t\t" + type + "[] oldValue =  get" + methodName
					+ "();");
			itsWriter
					.println("\t\tCollection " + variableName + "Collection = "
							+ "Arrays.asList(" + variableName + "); ");
			itsWriter.print("\t\tModelUtilities.setOwnSlotValues(this, \""
					+ slotName + "\", " + variableName + "Collection);");
		} else {
			itsWriter.print(argType + " " + variableName + ") {\n");
			if (type.equals("Boolean"))
				itsWriter.println("\t\t" + type + " oldValue =  new " + type
						+ "(is" + methodName + "());");
			else if (type.equals("Integer") || type.equals("Float"))
				itsWriter.println("\t\t" + type + " oldValue =  new " + type
						+ "(get" + methodName + "());");
			else
				itsWriter.println("\t\t" + type + " oldValue =  get"
						+ methodName + "();");
			itsWriter.print("\t\tModelUtilities.setOwnSlotValue(this, \""
					+ slotName + "\", " + newValue + ");");
		}
		// write the code to invoke PropertyChange notification
		// if (cls.getTemplateSlotAllowsMultipleValues(slot)) {
		writePropertyChangeNotification(cls, slot, slotName, newValue,
				itsWriter);
		// } else {
		// writePropertyChangeNotification(cls, slot, slotName, newValue,
		// itsWriter);
		// }
		itsWriter.print("\t}\n");

		// itsWriter.print("\t\tthis." + variableName + " = " + variableName +
		// ";\n\t}\n");

		// Write accessor
		itsWriter.print("\tpublic ");
		if (cls.getTemplateSlotAllowsMultipleValues(slot)) {
			itsWriter.print(argType + "[] " + "get" + methodName + "(){\n");
			itsWriter.print("\t\treturn (" + argType + "[]) "
					+ " ModelUtilities.getOwnSlotValues(this, \"" + slotName
					+ "\")" + ".toArray(new " + argType + "[0])" + ";\n\t}\n");
		} else {
			if (argumentType(cls, slot).equals("boolean")) {
				itsWriter.print("boolean is" + methodName + "() {\n");
			} else {
				itsWriter.print(argumentType(cls, slot) + " get" + methodName
						+ "() {\n");
			}
			if (argumentType(cls, slot).equals("boolean")) {
				itsWriter
						.print("\t\tif (ModelUtilities.getOwnSlotValue(this, \""
								+ slotName
								+ "\") == null) return false;\n\t\telse \n");
			}
			itsWriter.print("\t\treturn (" + typeCast(cls, slot)
					+ " ModelUtilities.getOwnSlotValue(this, \"" + slotName
					+ "\"))" + typeAccess(cls, slot) + ";\n\t}\n");
		}
		// itsWriter.print("\t\treturn this." + variableName + ";\n\t}\n");
	}

  	private void writePropertyChangeNotification(Cls cls, Slot slot,
			String slotName, String newValue, PrintWriter itsWriter) {
		/*
		 * if (cls.getTemplateSlotAllowsMultipleValues(slot)) { oldValue=
		 * "ModelUtilities.getOwnSlotValues(this, \"" + slotName + "\")"; } else
		 * oldValue = "ModelUtilities.getOwnSlotValue(this, \"" + slotName +
		 * "\")";
		 */
		itsWriter.print("\n\t\tpcs.firePropertyChange(\"" + slotName
				+ "\", oldValue, " + newValue + ");\n");
	}

  	private String variableName(String slotName) {
		slotName = slotName.replace('-', '_');
		return slotName;
	}

	private String methodName(String slotName) {
		slotName = slotName.replace('-', '_');
		if (saveFormat == SaveFormat.Javabean || saveFormat == SaveFormat.PoJo) {
			return slotName.substring(0, 1).toUpperCase()
					+ slotName.substring(1);
		} else {
			return slotName + "Value";
		}
	}

	private void writeClassConstructor(Cls cls, PrintWriter itsWriter) {
		// Write Protege constructor #1
		itsWriter.print("\n\tpublic " + cls.getName()
				+ "() {\n\t}\n");
				

		if (saveFormat == SaveFormat.Javabean) {
			// Write Protege constructor #2
			//itsWriter.print("\n\tpublic " + cls.getName()
				//	+ "(KnowledgeBase kb, String name , Cls cls ) {\n"
					//+ "\t\tsuper(kb, name, cls);\n");
			//itsWriter.print("\t}\n");
			/*
			 * // Write constructor that hides Protege classes
			 * itsWriter.print("\tpublic " + cls.getName() +"("); Collection
			 * slots = cls.getTemplateSlots(); // Sort the slots List
			 * sortedSlots = new ArrayList(); sortedSlots.addAll(slots);
			 * Collections.sort(sortedSlots, (Comparator) new
			 * FrameComparator());
			 * 
			 * boolean first = true; for (Iterator i = sortedSlots.iterator();
			 * i.hasNext();) { Slot slot = (Slot) i.next(); if (!first)
			 * itsWriter.print(", \n\t\t\t"); else first = false; if
			 * (cls.getTemplateSlotAllowsMultipleValues(slot)) {
			 * itsWriter.print(argumentType(cls, slot)+"[] "+slot.getName()); }
			 * else itsWriter.print(argumentType(cls, slot)+" "+
			 * slot.getName()); } itsWriter.print(") {\n");
			 *  // call the first Protege constructor // Need to assume the
			 * existence of static variable that hold reference to // the
			 * KnowledgeBase // call set methods to initialize all slots
			 * 
			 * itsWriter.print("\t\tsuper(ProtegeKB.getKB(), null,
			 * ProtegeKB.getKB().getCls(\"" + cls.getName()+"\"));\n"); for
			 * (Iterator i = sortedSlots.iterator(); i.hasNext();) { Slot slot =
			 * (Slot) i.next();
			 * itsWriter.print("\t\tset"+methodName(slot.getName())+"("+
			 * slot.getName()+");\n"); }
			 * 
			 * itsWriter.print("\t}\n");
			 */
		}

	}

	void writeListener(PrintWriter itsWriter) {
		/*
		itsWriter.print("\n\tprivate PropertyChangeSupport pcs = new PropertyChangeSupport(this); \n\n"
			+ "\tpublic void addPropertyChangeListener(PropertyChangeListener pcl) {\n"
			+ "\t\tpcs.addPropertyChangeListener(pcl);\n\t}\n"
			+ "\tpublic void removePropertyChangeListener(PropertyChangeListener pcl) {\n"
			+ "\t\tpcs.removePropertyChangeListener(pcl); \n\t} \n");
			*/
	}

	private void preserverUserCode(File oldClassFile, PrintWriter itsWriter) {
		BufferedReader in;
		String s;
		boolean userCodeStarted = false;

		try {
			in = new BufferedReader(new FileReader(oldClassFile));

			while ((s = in.readLine()) != null) {
				if (userCodeStarted) {
					itsWriter.println(s);
				} else {
					if (s.indexOf("__Code above") > 0)
						userCodeStarted = true;
				}
			}
		} catch (Throwable e) {
			System.out.println("Error opening" + oldClassFile);
			e.printStackTrace();
			System.exit(1);
		}

	}

	private boolean clsIsRoot(Cls clas) {
		return clas.getName().equals(":THING");
	}

	private String typeCast(Cls cls, Slot slot) {
		// System.out.println("in typeCast: cls: "+cls.toString() + " slot:
		// "+slot.toString());
		return typeCastTable.getProperty(cls.getTemplateSlotValueType(slot).toString());

	}

	private String typeAccess(Cls cls, Slot slot) {
		// System.out.println("In typeAccess: cls: "+cls.toString() + " slot:
		// "+slot.toString());
		return typeAccessTable.getProperty(cls.getTemplateSlotValueType(slot).toString());
	}

	private String argumentType(Cls cls, Slot slot) {
		// System.out.println("in argumentType - cls: "+cls.toString() + " slot:
		// "+slot.toString());
		if(cls.getTemplateSlotValueType(slot).toString().equals(ValueType.CLS.toString()) ) {
			Object[] clses = slot.getAllowedParents().toArray();
			return ((Cls)clses[0]).getName();
		} else if(cls.getTemplateSlotValueType(slot).toString().equals(ValueType.INSTANCE.toString())) {
			Object[] clses = slot.getAllowedParents().toArray();
			return ((Cls)clses[0]).getName();
		} else {
			return typeConversionTable.getProperty(cls.getTemplateSlotValueType(slot).toString());	
		}
		
	}
}

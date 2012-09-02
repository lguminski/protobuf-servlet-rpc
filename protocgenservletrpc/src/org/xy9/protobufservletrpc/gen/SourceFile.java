package org.xy9.protobufservletrpc.gen;

import google.protobuf.compiler.Plugin.CodeGeneratorResponse.File;

public class SourceFile {

	private static final String JAVA_EXTENSION = ".java";

	private final String className;
	private final String packageName;
	private final StringBuilder classBody;
	private final String lineSeparator;

	public SourceFile(String className, String packageName) {
		this.className = className;
		this.packageName = packageName;
		this.classBody = new StringBuilder();
		this.lineSeparator = System.getProperty("line.separator");
	}

	public void writeln() {
		classBody.append(lineSeparator);
	}

	public void writeln(int indent, String format, Object... args) {
		classBody.append(indent(indent));
		classBody.append(String.format(format, args));
		classBody.append(lineSeparator);
	}

	private String indent(int indent) {
		StringBuilder tabs = new StringBuilder(indent);
		for (int i = 0; i < indent; i++) {
			tabs.append('\t');
		}
		return tabs.toString();
	}

	private String getContent() {
		StringBuilder content = new StringBuilder();
		if (packageName.length() > 0) {
			content.append(String.format("package %s;", packageName));
			content.append(lineSeparator);
			content.append(lineSeparator);
		}
		content.append(classBody);
		return content.toString();
	}

	public File getFile() {
		File.Builder file = File.newBuilder();
		file.setName(path() + className + JAVA_EXTENSION);
		file.setContent(getContent());
		return file.build();
	}

	private String path() {
		StringBuilder path = new StringBuilder();
		for (char c : packageName.toCharArray()) {
			if (c == '.') {
				path.append("/");
			} else {
				path.append(c);
			}
		}
		if (path.length() > 0) {
			path.append("/");
		}
		return path.toString();
	}
}

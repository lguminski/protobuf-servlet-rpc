package org.xy9.protobufservletrpc.gen;

public class Parameter {
	private final String typeName;
	private final String upperCaseName;
	private final String javaName;

	public Parameter(String typeName, String upperCaseName, String javaName) {
		this.typeName = typeName;
		this.upperCaseName = upperCaseName;
		this.javaName = javaName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getUpperCaseName() {
		return upperCaseName;
	}

	public String getJavaName() {
		return javaName;
	}
}

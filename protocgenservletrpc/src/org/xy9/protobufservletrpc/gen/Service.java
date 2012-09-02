package org.xy9.protobufservletrpc.gen;

import java.util.*;

public class Service {
	private final String name;
	private final String javaName;
	private final List<Method> methods;

	public Service(String name, String javaName, List<Method> methods) {
		this.name = name;
		this.javaName = javaName;
		this.methods = methods;
	}

	public String getName() {
		return name;
	}

	public String getJavaName() {
		return javaName;
	}

	public int getMethodCount() {
		return methods.size();
	}

	public List<Method> getMethods() {
		return new ArrayList<Method>(methods);
	}

	public Method getMethod(int index) {
		return methods.get(index);
	}
}

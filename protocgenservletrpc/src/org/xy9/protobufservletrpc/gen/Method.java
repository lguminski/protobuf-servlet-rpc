package org.xy9.protobufservletrpc.gen;

import java.util.*;

public class Method {

	private final String name;
	private final String returnType;
	private final List<Parameter> parameters;
	private final String requestType;

	public Method(String name, String returnType, String requestType, List<Parameter> parameters) {
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.requestType = requestType;
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getParameterString() {
		StringBuilder sb = new StringBuilder();
		for (Parameter parameter : parameters) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(parameter.getTypeName());
			sb.append(" ");
			sb.append(parameter.getJavaName());
		}
		return sb.toString();
	}

	public String getArgumentString() {
		StringBuilder sb = new StringBuilder();
		for (Parameter parameter : parameters) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("arguments.get");
			sb.append(parameter.getUpperCaseName());
			sb.append("()");
		}
		return sb.toString();
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getRequestType() {
		return requestType;
	}

}

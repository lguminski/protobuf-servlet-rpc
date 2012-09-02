package org.xy9.protobufservletrpc.gen;

public class Javanizer {

	public String javaNameOf(String symbol) {
		return camelCase(symbol);
	}

	public String javaTypeOf(String symbol) {
		if (symbol.startsWith(".")) {
			return symbol.substring(1, symbol.length());
		} else {
			return symbol;
		}
	}

	public String upperCaseNameOf(String symbol) {
		return firstCharToUpperCase(camelCase(symbol));
	}

	private String camelCase(String symbol) {
		StringBuilder result = new StringBuilder();
		boolean upperCase = false;
		for (int i = 0; i < symbol.length(); i++) {
			char c = symbol.charAt(i);
			if (c == '_') {
				upperCase = true;
			} else if (upperCase) {
				result.append(Character.toUpperCase(c));
				upperCase = false;
			} else if (i == 0) {
				result.append(Character.toLowerCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private String firstCharToUpperCase(String symbol) {
		StringBuilder result = new StringBuilder(symbol.length());
		for (int i = 0; i < symbol.length(); i++) {
			char c = symbol.charAt(i);
			if (i == 0) {
				result.append(Character.toUpperCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}

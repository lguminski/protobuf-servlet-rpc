package org.xy9.protobufservletrpc.gen;

import java.io.*;

public class Main {

	public static void main(String[] args) throws IOException {
		new Generator(System.in, System.out, new Parser(new Javanizer()), "protobufservletrpc").run();
	}

}

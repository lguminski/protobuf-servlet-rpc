package org.xy9.protobufservletrpc.gen;

import google.protobuf.compiler.Plugin.CodeGeneratorRequest;
import google.protobuf.compiler.Plugin.CodeGeneratorResponse;

import java.io.*;
import java.util.*;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

public class Generator {

	private final InputStream in;
	private final PrintStream out;
	private final Parser parser;
	private final String protobufServletRpcPackageName;

	private List<SourceFile> outputFiles;
	private String packageName;

	public Generator(InputStream in, PrintStream out, Parser parser, String protobufServletRpcPackageName) {
		this.in = in;
		this.out = out;
		this.parser = parser;
		this.protobufServletRpcPackageName = protobufServletRpcPackageName;
	}

	public void run() throws IOException {
		outputFiles = new ArrayList<SourceFile>();
		CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(in);
		generate(request.getProtoFileList());
		respond();
	}

	private void respond() throws IOException {
		CodeGeneratorResponse.Builder response = CodeGeneratorResponse.newBuilder();
		for (SourceFile file : outputFiles) {
			response.addFile(file.getFile());
		}
		response.build().writeTo(out);
	}

	private void generate(List<FileDescriptorProto> files) {
		for (FileDescriptorProto file : files) {
			generate(file);
		}
	}

	private void generate(FileDescriptorProto file) {
		this.packageName = parser.getPackage(file);
		for (Service service : parser.getServices(file)) {
			generateCodeFor(service);
		}
	}

	private void generateCodeFor(Service service) {
		generateInterface(service);
		generateConnector(service);
		generateServlet(service);
	}

	private void generateInterface(Service service) {
		String className = service.getName();
		SourceFile file = addFile(className);
		file.writeln(0, "public interface %s {", className);
		for (Method method : service.getMethods()) {
			file.writeln();
			file.writeln(1, "%s %s(%s) throws java.io.IOException;", method.getReturnType(), method.getName(), method.getParameterString());
		}
		file.writeln(0, "}");
	}

	private void generateConnector(Service service) {
		String className = service.getName() + "Connector";
		SourceFile file = addFile(className);
		file.writeln(0, "public class %s extends %s.AbstractConnector implements %s {", className, protobufServletRpcPackageName, service.getName());
		file.writeln();
		file.writeln(1, "public %s(java.net.URL url) {", className);
		file.writeln(2, "super(url);");
		file.writeln(1, "}");
		for (int i = 0; i < service.getMethodCount(); i++) {
			file.writeln();
			Method method = service.getMethod(i);
			file.writeln(1, "public %s %s(%s) throws java.io.IOException {", method.getReturnType(), method.getName(), method.getParameterString());
			StringBuilder builder = new StringBuilder();
			builder.append(method.getRequestType());
			builder.append(".newBuilder()");
			for (Parameter parameter : method.getParameters()) {
				builder.append(".set");
				builder.append(parameter.getUpperCaseName());
				builder.append("(");
				builder.append(parameter.getJavaName());
				builder.append(")");
			}
			file.writeln(2, "return %s.parseFrom(connect(%s, %d));", method.getReturnType(), builder, i);
			file.writeln(1, "}");
		}
		file.writeln(0, "}");
	}

	private void generateServlet(Service service) {
		String className = service.getName() + "Servlet";
		SourceFile file = addFile(className);
		file.writeln(0, "public class %s extends javax.servlet.http.HttpServlet {", className);
		file.writeln();
		file.writeln(1, "private final %s %sService;", service.getName(), service.getJavaName());
		file.writeln();
		file.writeln(1, "public %s(%s %sService) {", className, service.getName(), service.getJavaName());
		file.writeln(2, "this.%sService = %sService;", service.getJavaName(), service.getJavaName());
		file.writeln(1, "}");
		file.writeln();
		file.writeln(1, "@Override");
		file.writeln(1, "public void doPost(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws java.io.IOException {");
		file.writeln(2, "java.io.InputStream inputStream = req.getInputStream();");
		file.writeln(2, "%s.ServletRpcProtos.ServiceRequest serviceRequest = %s.ServletRpcProtos.ServiceRequest.parseFrom(inputStream);", protobufServletRpcPackageName, protobufServletRpcPackageName);
		file.writeln(2, "switch (serviceRequest.getMethodId()) {");
		for (int i = 0; i < service.getMethodCount(); i++) {
			Method method = service.getMethod(i);
			file.writeln(2, "case %d: {", i);
			file.writeln(3, "%s arguments = %s.parseFrom(serviceRequest.getArguments());", method.getRequestType(), method.getRequestType());
			file.writeln(3, "%s response = %sService.%s(%s);", method.getReturnType(), service.getJavaName(), method.getName(), method.getArgumentString());
			file.writeln(3, "response.writeTo(resp.getOutputStream());");
			file.writeln(3, "break;");
			file.writeln(2, "}");
		}
		file.writeln(2, "default: {");
		file.writeln(3, "throw new RuntimeException(\"Unknown method ID.\");");
		file.writeln(2, "}");
		file.writeln(2, "}");
		file.writeln(1, "}");
		file.writeln(0, "}");
	}

	private SourceFile addFile(String className) {
		SourceFile file = new SourceFile(className, packageName);
		outputFiles.add(file);
		return file;
	}

}

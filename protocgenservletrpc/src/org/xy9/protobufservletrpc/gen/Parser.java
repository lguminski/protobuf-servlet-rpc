package org.xy9.protobufservletrpc.gen;

import java.nio.file.*;
import java.util.*;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;

public class Parser {

	private final Javanizer javanizer;
	private FileDescriptorProto file;

	public Parser(Javanizer javanizer) {
		this.javanizer = javanizer;
	}

	public String getPackage(FileDescriptorProto file) {
		return file.getOptions().getJavaPackage();
	}

	public List<Service> getServices(FileDescriptorProto file) {
		this.file = file;
		List<Service> services = new ArrayList<Service>(file.getServiceCount());
		for (ServiceDescriptorProto service : file.getServiceList()) {
			services.add(parse(service));
		}
		return services;
	}

	private Service parse(ServiceDescriptorProto service) {
		String name = nameOf(service);
		return new Service(name, javanizer.javaNameOf(name), methodsOf(service));
	}

	private String nameOf(ServiceDescriptorProto service) {
		return service.getName();
	}

	private List<Method> methodsOf(ServiceDescriptorProto service) {
		List<Method> methods = new ArrayList<Method>();
		for (MethodDescriptorProto method : service.getMethodList()) {
			methods.add(new Method(nameOf(method), returnTypeOf(method), requestTypeOf(method), parametersOf(shortRequestTypeOf(method))));
		}
		return methods;
	}

	private String nameOf(MethodDescriptorProto method) {
		return javanizer.javaNameOf(method.getName());
	}

	private String returnTypeOf(MethodDescriptorProto method) {
		if (method.hasOutputType()) {
			return getQualifiedPath() + javanizer.javaTypeOf(method.getOutputType());
		} else {
			return "void";
		}
	}

	private String getQualifiedPath() {
		return getPackage(file) + "." + getOuterClassname() + ".";
	}

	private String getOuterClassname() {
		String outerClassname = file.getOptions().getJavaOuterClassname();
		if (outerClassname.length() > 0) {
			return outerClassname;
		} else {
			return classnameFromPath(Paths.get(file.getName()));
		}
	}

	private String classnameFromPath(Path path) {
		String fullName = path.getFileName().toString();
		String name = fullName.replace(".proto", "");
		String className = javanizer.upperCaseNameOf(name);
		return className;
	}

	private String requestTypeOf(MethodDescriptorProto method) {
		if (method.hasInputType()) {
			return getQualifiedPath() + javanizer.javaTypeOf(method.getInputType());
		} else {
			return "";
		}
	}

	private String shortRequestTypeOf(MethodDescriptorProto method) {
		if (method.hasInputType()) {
			return javanizer.javaTypeOf(method.getInputType());
		} else {
			return "";
		}
	}

	private List<Parameter> parametersOf(String requestType) {
		DescriptorProto message = findMessageByName(requestType);
		List<Parameter> parameters = new ArrayList<>(message.getFieldCount());
		for (FieldDescriptorProto field : message.getFieldList()) {
			parameters.add(new Parameter(typeNameFor(field), javanizer.upperCaseNameOf(field.getName()), javanizer.javaNameOf(field.getName())));
		}
		return parameters;
	}

	private DescriptorProto findMessageByName(String name) {
		for (DescriptorProto messageType : file.getMessageTypeList()) {
			if (messageType.getName().equals(name)) {
				return messageType;
			}
		}
		throw new RuntimeException("Message type " + name + "not found.");
	}

	private String typeNameFor(FieldDescriptorProto field) {
		Type type = field.getType();
		switch (type) {
		case TYPE_BOOL:
			return "boolean";
		case TYPE_BYTES:
			return "ByteString";
		case TYPE_DOUBLE:
			return "double";
		case TYPE_ENUM:
		case TYPE_MESSAGE:
			return getQualifiedPath() + javanizer.javaTypeOf(field.getTypeName());
		case TYPE_FIXED32:
		case TYPE_INT32:
		case TYPE_SFIXED32:
		case TYPE_SINT32:
		case TYPE_UINT32:
			return "int";
		case TYPE_FIXED64:
		case TYPE_INT64:
		case TYPE_SFIXED64:
		case TYPE_SINT64:
		case TYPE_UINT64:
			return "long";
		case TYPE_FLOAT:
			return "float";
		case TYPE_STRING:
			return "String";
		default:
			return "Object";
		}
	}
}

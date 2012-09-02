package org.xy9.protobufservletrpc.lib;

import java.io.*;
import java.net.*;

import org.xy9.protobufservletrpc.lib.ServletRpcProtos.ServiceRequest;

import com.google.protobuf.*;

public abstract class AbstractConnector {

	private final URL url;

	public AbstractConnector(URL url) {
		this.url = url;
	}

	protected InputStream connect(GeneratedMessage.Builder<?> builder, int methodId) throws IOException {
		Message requestMessage = builder.build();
		ServiceRequest serviceRequest = buildServiceRequest(requestMessage, methodId);
		InputStream inputStream = send(serviceRequest);
		return inputStream;
	}

	private ServiceRequest buildServiceRequest(Message message, int methodId) throws IOException {
		ServiceRequest.Builder serviceRequestBuilder = ServiceRequest.newBuilder();
		serviceRequestBuilder.setMethodId(methodId);
		serviceRequestBuilder.setArguments(messageToByteString(message));
		ServiceRequest serviceRequest = serviceRequestBuilder.build();
		return serviceRequest;
	}

	private ByteString messageToByteString(Message message) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		message.writeTo(byteArray);
		ByteString byteString = ByteString.copyFrom(byteArray.toByteArray());
		return byteString;
	}

	private InputStream send(ServiceRequest serviceRequest) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		serviceRequest.writeTo(connection.getOutputStream());
		InputStream inputStream = connection.getInputStream();
		return inputStream;
	}

}

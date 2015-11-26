# protobuf-servlet-rpc #

## Introduction ##

If you're familiar with [Protocol Buffers](http://code.google.com/p/protobuf/), then protobuf-servlet-rpc provides you with an easy way of tunneling your remote procedure calls over HTTP.

Quick facts:
  * supported language is Java
  * declare services and messages using [Protocol Buffers](http://code.google.com/p/protobuf/)
  * everything is tunneled over HTTP (or HTTPS)
  * designed for `javax.servlet` container environments
  * remote procedures can be called as if they were local...
  * ...all the boilerplate Java code is generated for you

## Quick Tutorial ##

### 1. Create Library Project ###

Create a new library project where you will define your services and messages. Add [protobuf](http://code.google.com/p/protobuf/) and the `protobuf-servlet-rpc.jar` to its build path.

Add a `.proto` file where you define services and messages, for instance `services.proto`, like so:

```
option java_package = "mylibrary";
option java_outer_classname = "Services";

service Calculator {
	rpc Add(TwoIntMessage) returns (SingleIntMessage);
	rpc Multiply(TwoIntMessage) returns (SingleIntMessage);
}

service StringOperations {
	rpc Reverse(SingleStringMessage) returns (SingleStringMessage);
	rpc CountChars(SingleStringMessage) returns (SingleIntMessage);
	rpc Join(TwoStringMessage) returns (SingleStringMessage);
}

message SingleIntMessage {
	required int32 my_int = 1;
}

message TwoIntMessage {
	required int32 first_int = 1;
	required int32 second_int = 2;
}

message SingleStringMessage {
	required string my_string = 1;
}

message TwoStringMessage {
	required string first_string = 1;
	required string second_string = 2;
}
```

Compile the `.proto` file. Make sure `protoc` and `protoc-gen-servlet-rpc-wrapper.bat` (see the FAQ at the end of this page) are in your path. Run the following command from the root directory of your library project, where `services.proto` is the name of your `.proto` file and `mylibrary` is the package where it resides:

```
protoc.exe
  --plugin=protoc-gen-servlet-rpc=protoc-gen-servlet-rpc-wrapper.bat
  --servlet-rpc_out=src-gen
  --proto_path=src
  --java_out=src-gen
  src\mylibrary\services.proto
```

protobuf-servlet-rpc just generated three things for you:
  * Java interfaces corresponding to the protobuf service declarations.
  * Servlets which will receive requests on the server side and pass them on to your service implementations.
  * Most importantly, "Connector" classes that allow your clients to call the remote procedures.

The generated files will be in the `src-gen` directory, so make sure to add it as a new source folder.

### 2. Create Server Project ###

Create a new server project. In this tutorial, we'll be using the [Jetty WebServer](http://jetty.codehaus.org) as our servlet container, so add Jetty, [protobuf](http://code.google.com/p/protobuf/) and the previously created library project to the build path of your server project.

When we compiled the `.proto` file in step 1, protobuf-servlet-rpc created two interfaces for us: `Calculator` and `StringOperations`. Now it's time to write actual implementations for those interfaces:

Add a class called `CalculatorService`:

```
public class CalculatorService implements Calculator {
	public SingleIntMessage add(int firstInt, int secondInt) {
		SingleIntMessage.Builder result = SingleIntMessage.newBuilder();
		result.setMyInt(firstInt + secondInt);
		return result.build();
	}
	
	public SingleIntMessage multiply(int firstInt, int secondInt) {
		SingleIntMessage.Builder result = SingleIntMessage.newBuilder();
		result.setMyInt(firstInt * secondInt);
		return result.build();
	}
}
```

And add a class called `StringOperationsService`:

```
public class StringOperationsService implements StringOperations {
	public SingleStringMessage reverse(String myString) {
		SingleStringMessage.Builder result = SingleStringMessage.newBuilder();
		StringBuilder reversed = new StringBuilder();
		for (int i = myString.length() - 1; i >= 0; i--) {
			reversed.append(myString.charAt(i));
		}
		result.setMyString(reversed.toString());
		return result.build();
	}
	
	public SingleIntMessage countChars(String myString) {
		SingleIntMessage.Builder result = SingleIntMessage.newBuilder();
		result.setMyInt(myString.length());
		return result.build();
	}
	
	public SingleStringMessage join(String firstString, String secondString) {
		SingleStringMessage.Builder result = SingleStringMessage.newBuilder();
		result.setMyString(firstString + secondString);
		return result.build();
	}
}
```

Note how protobuf-servlet-rpc translated the method parameters into their corresponding Java types. However, the return types remain unchanged -- this allows you to return more complex, protobuf-based messages, if necessary.

Now we require a main class which starts up Jetty and links our service implementations to the servlet classes which protobuf-servlet-rpc already generated for us. If you're familiar with Jetty, it's pretty straightforward:

```
public class Main {
	public static void main(String[] args) throws Exception {
		// Jetty stuff:
		Server server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(8080);
		ServletHandler servletHandler = new ServletHandler();
		
		// First service:
		Calculator calculator = new CalculatorService();
		Servlet calculatorServlet = new CalculatorServlet(calculator);
		servletHandler.addServletWithMapping(new ServletHolder(calculatorServlet), "/calculator/");

		// Second service:
		StringOperations stringOperations = new StringOperationsService();
		Servlet stringOperationsServlet = new StringOperationsServlet(stringOperations);
		servletHandler.addServletWithMapping(new ServletHolder(stringOperationsServlet), "/string-operations/");
		
		// More Jetty stuff:
		server.addConnector(connector);
		server.setHandler(servletHandler);
		server.start();
	}
}
```

You may run the server now.

### 3. Create Client Project ###

Last but not least, we create a new client project. Add [protobuf](http://code.google.com/p/protobuf/), `protobuf-servlet-rpc.jar` and the previously created library project to its build path.

```
public class Main {
	public static void main(String[] args) throws IOException {
		Calculator calculator = new CalculatorConnector(new URL("http://localhost:8080/calculator/")); 
		StringOperations stringOperations = new StringOperationsConnector(new URL("http://localhost:8080/string-operations/")); 
		System.out.println("2 + 2 is " + calculator.add(2, 2).getMyInt());
		System.out.println("3 * 3 is " + calculator.multiply(3, 3).getMyInt());
		System.out.println(stringOperations.reverse("!dlrow olleh").getMyString());		
		System.out.println(stringOperations.countChars("0123456789").getMyInt());		
		System.out.println(stringOperations.join("hello", " world!").getMyString());
	}
}
```

Now launch the client application. Your console will show the following output:

```
2 + 2 is 4
3 * 3 is 9
hello world!
10
hello world!
```

That's it.

## FAQ ##

#### How do I obtain `protoc-gen-servlet-rpc-wrapper.bat`? ####

On Windows, you can create it yourself. It should be something like:

```
@java -jar x:\path\to\protoc-gen-servlet-rpc.jar
```

On Unix, create a corresponding shell script.

#### Why the need for a wrapper? ####

`protoc `(currently) expects plug-ins to be native binaries, which protobuf-servlet-rpc -- being a Java application -- is not.


#### Why do I get "javax.servlet cannot be resolved to a type" and "Type mismatch: cannot convert from ... to Servlet" errors? ####

You are probably missing the Servlet classes which are part of Java EE.


#### How can I pass more complex objects as arguments? ####

Simply declare them as protobuf messages. Try protobuf-servlet-rpc with the example `.proto` file below and look at the generated interfaces:

```
service Foo {
	rpc Call(ComplexObject) returns (ComplexObject);
}

message ComplexObject {
	required SubObject one = 1;
	required SubObject two = 2;
	// ...
}

message SubObject {
	required string s1 = 1;
	// ...
}
```
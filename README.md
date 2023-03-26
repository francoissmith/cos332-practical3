World Clock Server
This is a simple Java program that implements an HTTP server to display the current time in different time zones.

The server listens on port 8888 and responds to two types of requests:

The root endpoint ("/") displays the current time in the local time zone (which is set to "GMT+2" in the code), and a list of cities with their respective time zones and the current time difference from the local time.

The "/city" endpoint takes a city name as a query parameter and displays the current time in the corresponding time zone.

Usage
To run the program, simply compile the WorldClockServer.java file and execute it with the following command:

ruby
Copy code
$ javac WorldClockServer.java
$ java WorldClockServer
Once the server is running, you can access it by visiting http://localhost:8888 in your web browser.

Dependencies
This program depends on the following libraries:

com.sun.net.httpserver.HttpServer
com.sun.net.httpserver.HttpHandler
com.sun.net.httpserver.HttpExchange
These libraries are included in the standard Java library.
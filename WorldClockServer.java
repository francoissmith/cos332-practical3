import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WorldClockServer {

    private static final int PORT = 8888;
    private static final String LOCAL_TIMEZONE = "GMT+2";

    private static final Map<String, String> CITY_TIMEZONES = new HashMap<>();
    static {
        CITY_TIMEZONES.put("New York", "GMT-4");
        CITY_TIMEZONES.put("London", "GMT+1");
        CITY_TIMEZONES.put("Tokyo", "GMT+9");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/city", new CityHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    // ****************************** RootHandler ****************************** //
    static class RootHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String response = generatePage();
            sendResponse(exchange, response);
        }
    }

    // ****************************** CityHandler ****************************** //
    static class CityHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String cityName = exchange.getRequestURI().getQuery();
            String response = generateCityPage(cityName);
            sendResponse(exchange, response);
        }
    }

    // ****************************** TimeObject ****************************** //
    static class TimeObject {
        int hour;
        int minute;
        int second;

        public TimeObject(String timezone) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
            this.minute = cal.get(Calendar.MINUTE);
            this.second = cal.get(Calendar.SECOND);
        }
    }

    // ****************************** Helper Methods ******************************
    // //
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // ---------------- generatePage ---------------- //
    private static String generatePage() {
        TimeObject time = new TimeObject(LOCAL_TIMEZONE);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta http-equiv=\"refresh\" content=\"1\">"); // refresh every second
        sb.append(
                "<style>body{font-family:Arial,Helvetica,sans-serif;font-size:20px;background-color:#f2f2f2}h1{font-size:30px;color:#333}p{color:#555}a{color:#333;text-decoration:none}a:hover{color:#555;text-decoration:underline}.container{display:flex;justify-content:center;align-items:center;width:800px;margin:auto;flex-direction:column}.world{background-color:#fff;border-radius:10px;box-shadow:0 0 5px #ccc;width:300px;margin:10px;padding:20px}.clockContainer{position:relative;margin:auto;height:220px;width:220px}.hour,.minute,.second{position:absolute;background:#000;border-radius:10px;transform-origin:bottom}.hour{width:1.8%;height:15%;top:35%;left:48.85%;opacity:.8}.minute{width:1.6%;height:25%;top:25%;left:49%;opacity:.8}.second{width:1%;height:25%;top:25%;left:49.25%;opacity:.8;background-color:red;border:#333}#Layer_1{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);height:100%;width:100%}</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class=\"container\">");
        sb.append("<div class=\"world\">");
        sb.append("<h1>South Africa</h1>");
        sb.append("<p>Timezone: " + LOCAL_TIMEZONE + "</p>");
        sb.append("<p>" + getCurrentTime(LOCAL_TIMEZONE) + "</p>");
        sb.append("<div class=\"clockContainer\">");
        sb.append("<div class=\"hour\"></div>");
        sb.append("<div class=\"minute\"></div>");
        sb.append("<div class=\"second\"></div>");
        sb.append("<svg id=\"Layer_1\" data-name=\"Layer 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1024 768\"> <defs> <style> .cls-1 { fill: none; } .cls-2 { fill: #fefefe; } .cls-3 { fill: #090909; } </style> </defs> <path class=\"cls-1\" d=\"M512,767.81H4.19c-1.33,0-3.19,.52-3.85-.15-.67-.67-.15-2.52-.15-3.85V4.19c0-1.33-.52-3.18,.15-3.85,.67-.67,2.52-.15,3.85-.15H1019.81c1.33,0,3.19-.52,3.85,.15,.67,.67,.15,2.52,.15,3.85V763.81c0,1.33,.52,3.19-.15,3.85s-2.52,.15-3.85,.15h-507.81Zm334.02-410.55c-.5,0-1,.03-1.5,0-8.15-.5-17.22,6.07-19.41,16.21-.17,.78-.41,2.02,.52,2.16,1.95,.29,3.98,.34,5.94,.12,1.5-.16,1.57-1.86,2.11-2.96,3.51-7.13,8.88-10.06,17.53-6.98,4.04,1.44,6.46,4.49,6.9,8.8,.45,4.4-1.51,7.55-5.41,9.66-2.76,1.49-5.52,2.85-8.73,2.81-1.52-.02-2.12,.6-1.98,2.07,.03,.33,0,.67,0,1-.06,4.27,.13,4.6,4.3,4.52,2.38-.04,4.66,.41,6.72,1.3,8.11,3.49,11.75,11.21,7.67,19.91-1.91,4.07-5.16,6.56-9.58,7.76-7.87,2.13-17.74-.27-20.49-10.18-.21-.77-.65-1.48-.87-2.26-.23-.8-.75-1.07-1.47-.92-1.88,.38-4.28-1.05-5.6,.88-1.05,1.54,.52,3.23,.94,4.86,.16,.64,.23,1.31,.49,1.91,2.19,5.16,5.56,9.68,10.68,11.72,11.3,4.5,22.73,2.88,31.04-7.49,3.26-4.07,4.89-9.34,4.47-14.13-.48-5.61-2.31-11.46-7.94-14.92-1.28-.79-3.36-1.46-3.55-3.05-.21-1.77,1.94-2.49,3.05-3.65,5.08-5.34,5.84-13.08,1.87-19.71-4.04-6.75-10.32-9.12-17.74-9.42l.04-.02ZM535.87,93.87v-.02h20.49c2.92,0,4.37-1.47,4.35-4.41-.03-3.1-.03-3.09-3.25-3.09h-26.48c-.66,0-1.55,.17-1.79-.45-.14-.35,.48-1.13,.91-1.56,7.71-7.77,15-15.93,22.11-24.24,3.97-4.63,6.96-9.92,7.47-16.13,.99-12.04-8.14-23.58-21.88-23.4-9,.12-16.51,3.6-20.91,12.18-1.68,3.28-2.91,6.55-2.89,10.27,.01,1.5,.51,2.15,2.01,2.02,.66-.06,1.33,0,2-.02,2.57-.05,3.21-.51,3.75-2.84,.11-.47-.1-1.02,0-1.49,1.43-6.2,4.91-10.62,11.24-12.24,5.67-1.45,10.84-.29,15,3.98,4.15,4.25,5.11,9.24,3.29,14.96-.98,3.09-2.75,5.57-4.72,7.96-6.12,7.41-12.6,14.5-19.3,21.39-4.86,4.99-9.2,10.47-14.28,15.26-.33,.31-.71,.84-.66,1.21,.13,.88,.91,.67,1.56,.67h21.98ZM180.61,357.11c-7.8-.29-14.48,2.56-19.04,8.61-5.44,7.22-6.6,18.04-.2,26.39,4.75,6.19,11.02,9.51,19.14,7.99,.81-.15,1.73-.44,2.32,.29,.69,.84-.14,1.48-.57,2.13-4.61,6.98-9.21,13.97-13.83,20.94-2.94,4.44-2.82,4.27,1.26,7.43,1.66,1.29,2.23,.46,3.19-.47,.6-.59,.82-1.4,1.26-2.05,4.45-6.63,9.11-13.13,13.47-19.82,3.91-6,8.21-11.8,11.21-18.32,3.24-7.04,3.73-14.27,.12-21.47-.83-1.66-2.51-2.45-3.4-3.9-3.46-5.62-8.77-7.46-14.94-7.75h0Zm335,394.43c2.58-.13,6.45-.85,9.78-2.99,5.81-3.74,9.73-8.6,10.77-15.95,1.42-10.05-3.95-19.93-13.49-23.33-3.04-1.08-6.1-1.11-9.22-1.18-1.44-.03-2.1-1.08-1.55-2.33,.32-.74,.94-1.34,1.39-2.02,4.36-6.68,8.72-13.37,13.07-20.06,1.86-2.85,1.85-2.87-1.04-4.78-.68-.45-1.45-.78-2.13-1.24-.99-.68-1.34-.65-2.15,.58-7.29,11.13-14.7,22.18-22.19,33.19-2.33,3.42-4.19,7.05-5.4,10.96-4.7,15.16,5.43,29.17,22.16,29.17v-.02ZM339.97,69.38c-.3,.17-.58,.33-.86,.5-3.97,2.31-7.95,4.62-11.91,6.95-1.07,.63-1.16,1.33-.38,2.41,.75,1.03,1.06,2.35,1.71,3.47,4.84,8.31,9.75,16.59,14.56,24.92,3.76,6.5,7.43,13.06,11.13,19.6,.88,1.56,1.68,1.91,3.53,.94,3.19-1.67,6.09-3.81,9.4-5.3,3.15-1.42,3.22-2.15,1.47-5.26-6.99-12.38-13.93-24.8-21.15-37.05-1.9-3.23-3.53-6.64-5.69-9.72-.45-.64-.8-1.5-1.81-1.46ZM203.92,201.26c-1.64,.09-2.13,1-2.7,2.1-1.72,3.33-3.6,6.58-5.45,9.84-.39,.69-1.52,1.05-1.35,1.94,.19,.99,1.16,1.34,2.06,1.83,6.17,3.36,12.29,6.82,18.38,10.34,9.91,5.72,19.77,11.51,29.66,17.26,.95,.55,1.78,.6,2.45-.56,2.2-3.84,4.55-7.6,6.66-11.49,.69-1.27,.85-2.49-1.12-3.49-5.51-2.83-10.85-6.01-16.23-9.1-10.32-5.92-20.63-11.87-30.93-17.83-.56-.32-.97-.9-1.42-.84h-.01Zm499.17,497.07c-.11-1.63-1-2.88-1.73-4.14-5.74-9.89-11.61-19.69-17.31-29.6-2.85-4.95-5.92-9.77-8.4-14.94-.85-1.77-2.11-2.71-4.47-1.24-3.05,1.9-6.25,3.57-9.39,5.31-3.16,1.75-3.16,1.77-1.43,4.65,2.94,4.9,5.9,9.78,8.78,14.71,6.26,10.7,12.52,21.41,18.69,32.16,.69,1.21,1.22,1.96,2.6,1.16,3.96-2.3,7.92-4.6,11.85-6.94,.38-.23,.57-.79,.81-1.13Zm78.91-165.21c-.97,.16-1.46,.87-1.87,1.56-2.23,3.8-4.37,7.65-6.65,11.42-.8,1.33,.12,1.99,.89,2.32,3.18,1.36,5.99,3.36,8.96,5.07,7.82,4.49,15.62,9.03,23.44,13.54,5.11,2.94,10.21,5.9,15.35,8.78,2.58,1.45,2.73,1.34,4.38-1.44,1.91-3.23,3.8-6.48,5.76-9.68,.88-1.44,.6-2.5-.79-3.31-2.13-1.23-4.3-2.4-6.43-3.64-8.21-4.79-16.39-9.62-24.61-14.39-5.38-3.12-10.81-6.17-16.23-9.23-.71-.4-1.49-.68-2.2-1.01h0Zm.15-289.3c.44-.28,.82-.65,1.28-.8,3.17-1.08,5.57-3.44,8.49-5,10.08-5.38,19.84-11.36,29.72-17.1,3.09-1.79,6.13-3.67,9.43-5.11,1.95-.85,1.99-1.99,.8-3.96-1.93-3.2-3.8-6.44-5.7-9.67-1.55-2.64-1.83-2.7-4.51-1.14-3.54,2.07-7.08,4.13-10.64,6.16-9.4,5.36-18.59,11.08-28.18,16.1-3.03,1.59-5.87,3.55-8.74,5.42-.85,.55-1.25,1.16-.44,2.41,2.29,3.54,4.29,7.26,6.38,10.92,.5,.87,.96,1.73,2.1,1.76h.01ZM205.22,578.02c.94-.4,2.05-.75,3.03-1.33,8.74-5.18,17.38-10.51,26.2-15.54,3.1-1.77,5.98-3.88,9.25-5.41,3.8-1.78,7.34-4.13,10.95-6.31,.76-.46,1.82-1.14,.8-2.33-2.35-2.75-3.46-6.23-5.47-9.19-2.81-4.16-2.7-4.24-6.93-1.8-.28,.16-.55,.35-.83,.51-7.62,4.48-15.25,8.95-22.87,13.44-7.32,4.31-14.62,8.68-21.99,12.91-1.18,.68-1.35,1.45-.84,2.35,2.19,3.82,4.52,7.56,6.71,11.39,.46,.81,.94,1.27,1.99,1.31h0ZM673.15,129.24c.95-.11,1.32-.71,1.76-1.48,3.41-5.95,6.92-11.85,10.37-17.78,5.51-9.48,10.98-18.99,16.51-28.45,1.78-3.04,1.57-3.28-1.3-5.31-3.4-2.39-7.73-3.11-10.7-6.16-.47-.48-.37-.37-.68,.12-2.17,3.47-4.53,6.81-6.61,10.33-4.58,7.74-9.04,15.56-13.53,23.36-3.09,5.37-6.12,10.77-9.26,16.11-.67,1.14-.54,1.53,.65,2.19,3.86,2.14,7.6,4.48,11.4,6.72,.41,.24,.95,.26,1.41,.37l-.02-.02ZM370.11,656.39c-.25-.41-.4-1.05-.76-1.2-4.41-1.86-8.27-4.72-12.52-6.87-1.33-.67-1.89,.29-2.34,1.08-2.79,4.78-5.5,9.61-8.24,14.42-3.41,5.96-6.83,11.92-10.24,17.88-3,5.25-5.88,10.58-9.01,15.75-.98,1.62-.55,2.38,.77,3.13,2.69,1.55,5.41,3.05,8.11,4.58,2.9,1.64,3.31,1.68,5.79-.47,1.28-1.12,2.4-2.39,3.27-3.94,3.36-6.01,6.9-11.92,10.33-17.89,4.54-7.89,9.06-15.8,13.57-23.72,.49-.85,.83-1.79,1.28-2.75h0ZM486.9,58.62c0-11.14,.02-22.28-.01-33.42,0-2.7-.07-2.72-2.68-2.73-2.66-.02-5.34,.21-7.98-.06-2.6-.26-4.37,.45-5.52,2.85-.69,1.44-2.59,3.26-2.2,4.04,.74,1.49,2.94,.46,4.51,.59,1.49,.13,2.99,.06,4.49,.05,1.25-.01,1.78,.61,1.77,1.82-.02,1.16,.01,2.33,.01,3.49v54.38c0,2.83,1.34,4.24,4.01,4.23,3.56-.02,3.58-.02,3.59-3.81,.01-10.48,0-20.95,0-31.43h.01Zm40.06,327.57c.16-7.26-5.21-12.83-12.25-13.28-7.36-.47-14.21,4.37-14.43,12.79-.2,7.29,4.84,13.29,12.93,13.96,7.21,.6,13.59-5.91,13.76-13.46h-.01Z\" /> <path class=\"cls-3\" d=\"M846.02,357.26c7.41,.3,13.7,2.67,17.74,9.42,3.97,6.63,3.21,14.37-1.87,19.71-1.11,1.16-3.26,1.89-3.05,3.65,.19,1.59,2.26,2.26,3.55,3.05,5.63,3.46,7.45,9.31,7.94,14.92,.41,4.79-1.21,10.05-4.47,14.13-8.31,10.37-19.74,11.99-31.04,7.49-5.13-2.04-8.49-6.56-10.68-11.72-.25-.6-.32-1.28-.49-1.91-.42-1.62-1.99-3.32-.94-4.86,1.32-1.93,3.72-.5,5.6-.88,.72-.15,1.24,.12,1.47,.92,.22,.77,.66,1.48,.87,2.26,2.75,9.91,12.61,12.31,20.49,10.18,4.42-1.19,7.67-3.69,9.58-7.76,4.08-8.7,.44-16.42-7.67-19.91-2.05-.88-4.34-1.34-6.72-1.3-4.18,.07-4.37-.25-4.3-4.52,0-.33,.03-.67,0-1-.15-1.47,.46-2.09,1.98-2.07,3.21,.04,5.97-1.32,8.73-2.81,3.9-2.11,5.86-5.26,5.41-9.66-.44-4.31-2.86-7.36-6.9-8.8-8.65-3.08-14.02-.14-17.53,6.98-.54,1.1-.61,2.8-2.11,2.96-1.96,.21-3.99,.17-5.94-.12-.94-.14-.69-1.38-.52-2.16,2.19-10.13,11.26-16.7,19.41-16.21,.5,.03,1,0,1.5,0l-.04,.02Z\" /> <path class=\"cls-3\" d=\"M535.87,93.87h-21.99c-.64,0-1.43,.21-1.56-.67-.05-.37,.33-.91,.66-1.21,5.08-4.79,9.43-10.26,14.28-15.26,6.71-6.89,13.18-13.98,19.3-21.39,1.98-2.39,3.74-4.88,4.72-7.96,1.82-5.72,.85-10.71-3.29-14.96-4.16-4.27-9.33-5.42-15-3.98-6.34,1.62-9.82,6.04-11.24,12.24-.11,.47,.11,1.02,0,1.49-.54,2.33-1.18,2.79-3.75,2.84-.67,0-1.34-.04-2,.02-1.5,.13-2-.52-2.01-2.02-.03-3.73,1.21-7,2.89-10.27,4.4-8.58,11.91-12.07,20.91-12.18,13.75-.18,22.88,11.37,21.88,23.4-.51,6.21-3.5,11.5-7.47,16.13-7.11,8.31-14.4,16.47-22.11,24.24-.43,.43-1.05,1.21-.91,1.56,.24,.61,1.13,.45,1.79,.45h26.48c3.23,0,3.23-.01,3.25,3.09,.03,2.95-1.42,4.42-4.35,4.41h-20.49v.02h0Z\" /> <path class=\"cls-3\" d=\"M180.61,357.11c6.18,.29,11.48,2.13,14.94,7.75,.89,1.44,2.57,2.24,3.4,3.9,3.6,7.21,3.12,14.44-.12,21.47-3,6.52-7.3,12.32-11.21,18.32-4.36,6.69-9.02,13.19-13.47,19.82-.44,.65-.65,1.46-1.26,2.05-.96,.93-1.52,1.76-3.19,.47-4.09-3.16-4.21-2.99-1.26-7.43,4.62-6.98,9.22-13.96,13.83-20.94,.43-.65,1.26-1.29,.57-2.13-.6-.73-1.52-.44-2.32-.29-8.13,1.52-14.4-1.8-19.14-7.99-6.4-8.35-5.24-19.17,.2-26.39,4.56-6.05,11.24-8.91,19.04-8.61h0Zm-.76,7.61c-8.52-.07-14.63,5.73-15.05,13.61-.43,8.25,5.61,15.43,14.33,15.61,8.07,.17,14.92-6.79,15.01-14.64,.09-7.95-7.39-15.59-14.29-14.58h0Z\" /> <path class=\"cls-3\" d=\"M515.61,751.54c-16.73,0-26.86-14.01-22.16-29.17,1.21-3.91,3.07-7.53,5.4-10.96,7.49-11,14.9-22.06,22.19-33.19,.8-1.22,1.15-1.26,2.15-.58,.68,.46,1.44,.79,2.13,1.24,2.9,1.91,2.9,1.93,1.04,4.78-4.35,6.69-8.71,13.38-13.07,20.06-.45,.69-1.06,1.29-1.39,2.02-.55,1.26,.1,2.3,1.55,2.33,3.12,.07,6.18,.1,9.22,1.18,9.54,3.4,14.92,13.28,13.49,23.33-1.04,7.35-4.96,12.21-10.77,15.95-3.33,2.14-7.2,2.86-9.78,2.99v.02Zm-15.82-22.22c-.03,8.52,5.76,14.47,14.11,14.49,8.82,.02,14.96-5.77,15.15-14,.19-7.79-5.87-14.96-14.4-15.09-7.74-.12-15.51,6.73-14.86,14.59h0Zm10.71-21.46c-.11,.11-.33,.25-.31,.33,.05,.24,.2,.45,.31,.67,.11-.11,.33-.25,.31-.33-.05-.24-.2-.45-.31-.67Z\" /> <path class=\"cls-3\" d=\"M339.97,69.38c1.01-.04,1.36,.82,1.81,1.46,2.16,3.08,3.79,6.48,5.69,9.72,7.22,12.25,14.16,24.66,21.15,37.05,1.75,3.11,1.68,3.84-1.47,5.26-3.31,1.49-6.21,3.63-9.4,5.3-1.84,.96-2.65,.62-3.53-.94-3.69-6.54-7.36-13.1-11.13-19.6-4.82-8.33-9.73-16.6-14.56-24.92-.65-1.11-.96-2.44-1.71-3.47-.78-1.08-.69-1.78,.38-2.41,3.96-2.33,7.94-4.63,11.91-6.95,.28-.16,.56-.32,.86-.5Z\" /> <path class=\"cls-3\" d=\"M203.92,201.26c.46-.06,.87,.52,1.42,.84,10.3,5.96,20.61,11.91,30.93,17.83,5.38,3.09,10.71,6.27,16.23,9.1,1.97,1.01,1.81,2.23,1.12,3.49-2.11,3.89-4.46,7.65-6.66,11.49-.67,1.16-1.5,1.12-2.45,.56-9.89-5.75-19.75-11.54-29.66-17.26-6.09-3.52-12.21-6.98-18.38-10.34-.9-.49-1.87-.84-2.06-1.83-.17-.89,.96-1.24,1.35-1.94,1.85-3.26,3.74-6.51,5.45-9.84,.57-1.1,1.06-2.01,2.7-2.1h.01Z\" /> <path class=\"cls-3\" d=\"M703.1,698.34c-.24,.35-.43,.91-.81,1.13-3.93,2.35-7.89,4.65-11.85,6.94-1.38,.8-1.9,.05-2.6-1.16-6.17-10.75-12.42-21.46-18.69-32.16-2.88-4.93-5.85-9.81-8.78-14.71-1.73-2.88-1.72-2.9,1.43-4.65,3.14-1.75,6.34-3.41,9.39-5.31,2.36-1.47,3.62-.53,4.47,1.24,2.48,5.17,5.56,9.99,8.4,14.94,5.7,9.91,11.57,19.71,17.31,29.6,.73,1.25,1.62,2.5,1.73,4.14Z\" /><path class=\"cls-3\" d=\"M782.01,533.13c.71,.32,1.49,.61,2.2,1.01,5.42,3.06,10.85,6.11,16.23,9.23,8.22,4.77,16.4,9.6,24.61,14.39,2.13,1.24,4.3,2.4,6.43,3.64,1.39,.81,1.67,1.87,.79,3.31-1.96,3.2-3.85,6.45-5.76,9.68-1.64,2.78-1.8,2.89-4.38,1.44-5.14-2.89-10.24-5.84-15.35-8.78-7.82-4.51-15.62-9.04-23.44-13.54-2.97-1.71-5.78-3.7-8.96-5.07-.77-.33-1.7-.99-.89-2.32,2.28-3.77,4.41-7.62,6.65-11.42,.41-.69,.9-1.4,1.87-1.56h0Z\" /><path class=\"cls-3\" d=\"M782.16,243.82c-1.14-.03-1.61-.89-2.1-1.76-2.09-3.66-4.09-7.38-6.38-10.92-.81-1.25-.4-1.86,.44-2.41,2.87-1.87,5.71-3.84,8.74-5.42,9.59-5.02,18.78-10.74,28.18-16.1,3.56-2.03,7.1-4.09,10.64-6.16,2.67-1.56,2.95-1.51,4.51,1.14,1.9,3.22,3.77,6.46,5.7,9.67,1.19,1.97,1.15,3.11-.8,3.96-3.3,1.44-6.34,3.32-9.43,5.11-9.89,5.74-19.64,11.72-29.72,17.1-2.92,1.56-5.32,3.92-8.49,5-.45,.15-.84,.52-1.28,.8h-.01Z\" /><path class=\"cls-3\" d=\"M205.22,578.02c-1.06-.04-1.53-.5-1.99-1.31-2.18-3.83-4.52-7.56-6.71-11.39-.51-.9-.34-1.67,.84-2.35,7.37-4.23,14.66-8.6,21.99-12.91,7.62-4.49,15.25-8.96,22.87-13.44,.28-.17,.55-.35,.83-.51,4.23-2.44,4.12-2.36,6.93,1.8,2,2.97,3.12,6.45,5.47,9.19,1.01,1.19-.04,1.86-.8,2.33-3.6,2.19-7.14,4.54-10.95,6.31-3.27,1.53-6.15,3.64-9.25,5.41-8.82,5.03-17.47,10.37-26.2,15.54-.98,.58-2.09,.92-3.03,1.33h0Z\" /><path class=\"cls-3\" d=\"M673.15,129.24c-.45-.11-1-.13-1.41-.37-3.8-2.23-7.54-4.58-11.4-6.72-1.19-.66-1.32-1.04-.65-2.19,3.14-5.34,6.17-10.74,9.26-16.11,4.49-7.8,8.95-15.62,13.53-23.36,2.08-3.51,4.45-6.86,6.61-10.33,.31-.5,.21-.61,.68-.12,2.97,3.05,7.3,3.77,10.7,6.16,2.87,2.02,3.08,2.26,1.3,5.31-5.53,9.47-11,18.97-16.51,28.45-3.45,5.93-6.96,11.83-10.37,17.78-.44,.77-.81,1.38-1.76,1.48l.02,.02Z\" /><path class=\"cls-3\" d=\"M370.11,656.39c-.44,.96-.79,1.89-1.28,2.75-4.51,7.92-9.02,15.83-13.57,23.72-3.44,5.97-6.97,11.88-10.33,17.89-.87,1.56-1.99,2.82-3.27,3.94-2.47,2.15-2.89,2.12-5.79,.47-2.7-1.53-5.42-3.04-8.11-4.58-1.32-.76-1.75-1.52-.77-3.13,3.14-5.17,6.02-10.49,9.01-15.75,3.4-5.97,6.83-11.92,10.24-17.88,2.75-4.81,5.45-9.64,8.24-14.42,.46-.78,1.01-1.75,2.34-1.08,4.25,2.15,8.11,5.01,12.52,6.87,.36,.15,.51,.78,.76,1.2h0Z\" /><path class=\"cls-3\" d=\"M486.9,58.62v31.43c0,3.8-.03,3.8-3.59,3.81-2.67,.01-4.01-1.4-4.01-4.23V35.25c0-1.16-.03-2.33-.01-3.49,.02-1.21-.52-1.83-1.77-1.82-1.5,.01-3,.08-4.49-.05-1.58-.14-3.78,.9-4.51-.59-.39-.79,1.51-2.61,2.2-4.04,1.15-2.4,2.92-3.11,5.52-2.85,2.63,.26,5.32,.04,7.98,.06,2.6,.02,2.67,.03,2.68,2.73,.03,11.14,.01,22.28,.01,33.42h-.01Z\" /><path class=\"cls-3\" d=\"M526.96,386.19c-.17,7.55-6.54,14.06-13.76,13.46-8.09-.67-13.13-6.67-12.93-13.96,.23-8.42,7.08-13.26,14.43-12.79,7.04,.45,12.41,6.02,12.25,13.28h.01Z\" /><path class=\"cls-2\" d=\"M179.85,364.72c6.9-1.01,14.38,6.63,14.29,14.58-.09,7.85-6.93,14.81-15.01,14.64-8.73-.18-14.76-7.36-14.33-15.61,.41-7.88,6.53-13.68,15.05-13.61h0Z\" /><path class=\"cls-2\" d=\"M499.79,729.31c-.65-7.86,7.12-14.71,14.86-14.59,8.53,.13,14.59,7.3,14.4,15.09-.2,8.23-6.33,14.02-15.15,14-8.34-.02-14.13-5.97-14.11-14.49h0Z\" /><path class=\"cls-2\" d=\"M510.5,707.84c.11,.22,.26,.44,.31,.67,.02,.08-.2,.22-.31,.33-.11-.22-.26-.44-.31-.67-.02-.08,.2-.22,.31-.33Z\" /></svg>");
        sb.append("</div></div>");
        sb.append("<div class=\"world\">");
        sb.append("<h1>World Clock</h1>");
        for (String city : CITY_TIMEZONES.keySet()) {
            String timezone = CITY_TIMEZONES.get(city);
            sb.append("<p><a href=\"/city?" + city + "\">" + city + "</a> (" + timezone + "): " + getCurrentTimeDifference(timezone) + "</p>");
        }
        sb.append("</div></div></div>");
        sb.append("</body>");
        sb.append("</html>");
        sb.append("<script>");
        sb.append("var hr = " + time.hour + ";");
        sb.append("var min =" + time.minute + ";");
        sb.append("var sec = " + time.second + ";");
        sb.append("hr_rotation = 30 * hr + min / 2;");
        sb.append("min_rotation = 6 * min;");
        sb.append("sec_rotation = 6 * sec;");
        sb.append("document.getElementsByClassName(\"hour\")[0].style.transform = `rotate(${hr_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"minute\")[0].style.transform = `rotate(${min_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"second\")[0].style.transform = `rotate(${sec_rotation}deg)`;");
        sb.append("</script>");
        return sb.toString();
    }

    // ---------------- generateCityPage ---------------- //
    private static String generateCityPage(String cityName) {
        TimeObject localTime = new TimeObject(LOCAL_TIMEZONE);
        TimeObject cityTime = new TimeObject(CITY_TIMEZONES.get(cityName));
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta http-equiv=\"refresh\" content=\"1\">"); // refresh every second
        sb.append(
                "<style>body{font-family:Arial,Helvetica,sans-serif;font-size:20px;background-color:#f2f2f2}h1{font-size:30px;color:#333}p{color:#555}a{color:#333;text-decoration:none}a:hover{color:#555;text-decoration:underline}.container{display:flex;justify-content:center;align-items:center;width:800px;margin:auto;flex-direction:column}.world{background-color:#fff;border-radius:10px;box-shadow:0 0 5px #ccc;width:300px;margin:10px;padding:20px}.clockContainer{position:relative;margin:auto;height:220px;width:220px}.hour,.minute,.second{position:absolute;background:#000;border-radius:10px;transform-origin:bottom}.hour{width:1.8%;height:15%;top:35%;left:48.85%;opacity:.8}.minute{width:1.6%;height:25%;top:25%;left:49%;opacity:.8}.second{width:1%;height:25%;top:25%;left:49.25%;opacity:.8;background-color:red;border:#333}#Layer_1{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);height:100%;width:100%}</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class=\"container\">");
        sb.append("<div style=\"display:flex;\">");
        for (int i = 0; i < 2; i++) {
            sb.append("<div class=\"world\">");
            if (i == 0) {
                sb.append("<h1>South Africa</h1>");
                sb.append("<p>Timezone: " + LOCAL_TIMEZONE + "</p>");
                sb.append("<p>" + getCurrentTime(LOCAL_TIMEZONE) + "</p>");
            } else {
                sb.append("<h1>" + cityName + "</h1>");
                String cityTimezone = CITY_TIMEZONES.get(cityName);
                sb.append("<p>Timezone: " + cityTimezone + "</p>");
                sb.append("<p>" + getCurrentTime(cityTimezone) + "</p>");
            }
            sb.append("<div class=\"clockContainer\">");
            sb.append("<div class=\"hour\"></div>");
            sb.append("<div class=\"minute\"></div>");
            sb.append("<div class=\"second\"></div>");
            sb.append(
                    "<svg id=\"Layer_1\" data-name=\"Layer 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1024 768\"> <defs> <style> .cls-1 { fill: none; } .cls-2 { fill: #fefefe; } .cls-3 { fill: #090909; } </style> </defs> <path class=\"cls-1\" d=\"M512,767.81H4.19c-1.33,0-3.19,.52-3.85-.15-.67-.67-.15-2.52-.15-3.85V4.19c0-1.33-.52-3.18,.15-3.85,.67-.67,2.52-.15,3.85-.15H1019.81c1.33,0,3.19-.52,3.85,.15,.67,.67,.15,2.52,.15,3.85V763.81c0,1.33,.52,3.19-.15,3.85s-2.52,.15-3.85,.15h-507.81Zm334.02-410.55c-.5,0-1,.03-1.5,0-8.15-.5-17.22,6.07-19.41,16.21-.17,.78-.41,2.02,.52,2.16,1.95,.29,3.98,.34,5.94,.12,1.5-.16,1.57-1.86,2.11-2.96,3.51-7.13,8.88-10.06,17.53-6.98,4.04,1.44,6.46,4.49,6.9,8.8,.45,4.4-1.51,7.55-5.41,9.66-2.76,1.49-5.52,2.85-8.73,2.81-1.52-.02-2.12,.6-1.98,2.07,.03,.33,0,.67,0,1-.06,4.27,.13,4.6,4.3,4.52,2.38-.04,4.66,.41,6.72,1.3,8.11,3.49,11.75,11.21,7.67,19.91-1.91,4.07-5.16,6.56-9.58,7.76-7.87,2.13-17.74-.27-20.49-10.18-.21-.77-.65-1.48-.87-2.26-.23-.8-.75-1.07-1.47-.92-1.88,.38-4.28-1.05-5.6,.88-1.05,1.54,.52,3.23,.94,4.86,.16,.64,.23,1.31,.49,1.91,2.19,5.16,5.56,9.68,10.68,11.72,11.3,4.5,22.73,2.88,31.04-7.49,3.26-4.07,4.89-9.34,4.47-14.13-.48-5.61-2.31-11.46-7.94-14.92-1.28-.79-3.36-1.46-3.55-3.05-.21-1.77,1.94-2.49,3.05-3.65,5.08-5.34,5.84-13.08,1.87-19.71-4.04-6.75-10.32-9.12-17.74-9.42l.04-.02ZM535.87,93.87v-.02h20.49c2.92,0,4.37-1.47,4.35-4.41-.03-3.1-.03-3.09-3.25-3.09h-26.48c-.66,0-1.55,.17-1.79-.45-.14-.35,.48-1.13,.91-1.56,7.71-7.77,15-15.93,22.11-24.24,3.97-4.63,6.96-9.92,7.47-16.13,.99-12.04-8.14-23.58-21.88-23.4-9,.12-16.51,3.6-20.91,12.18-1.68,3.28-2.91,6.55-2.89,10.27,.01,1.5,.51,2.15,2.01,2.02,.66-.06,1.33,0,2-.02,2.57-.05,3.21-.51,3.75-2.84,.11-.47-.1-1.02,0-1.49,1.43-6.2,4.91-10.62,11.24-12.24,5.67-1.45,10.84-.29,15,3.98,4.15,4.25,5.11,9.24,3.29,14.96-.98,3.09-2.75,5.57-4.72,7.96-6.12,7.41-12.6,14.5-19.3,21.39-4.86,4.99-9.2,10.47-14.28,15.26-.33,.31-.71,.84-.66,1.21,.13,.88,.91,.67,1.56,.67h21.98ZM180.61,357.11c-7.8-.29-14.48,2.56-19.04,8.61-5.44,7.22-6.6,18.04-.2,26.39,4.75,6.19,11.02,9.51,19.14,7.99,.81-.15,1.73-.44,2.32,.29,.69,.84-.14,1.48-.57,2.13-4.61,6.98-9.21,13.97-13.83,20.94-2.94,4.44-2.82,4.27,1.26,7.43,1.66,1.29,2.23,.46,3.19-.47,.6-.59,.82-1.4,1.26-2.05,4.45-6.63,9.11-13.13,13.47-19.82,3.91-6,8.21-11.8,11.21-18.32,3.24-7.04,3.73-14.27,.12-21.47-.83-1.66-2.51-2.45-3.4-3.9-3.46-5.62-8.77-7.46-14.94-7.75h0Zm335,394.43c2.58-.13,6.45-.85,9.78-2.99,5.81-3.74,9.73-8.6,10.77-15.95,1.42-10.05-3.95-19.93-13.49-23.33-3.04-1.08-6.1-1.11-9.22-1.18-1.44-.03-2.1-1.08-1.55-2.33,.32-.74,.94-1.34,1.39-2.02,4.36-6.68,8.72-13.37,13.07-20.06,1.86-2.85,1.85-2.87-1.04-4.78-.68-.45-1.45-.78-2.13-1.24-.99-.68-1.34-.65-2.15,.58-7.29,11.13-14.7,22.18-22.19,33.19-2.33,3.42-4.19,7.05-5.4,10.96-4.7,15.16,5.43,29.17,22.16,29.17v-.02ZM339.97,69.38c-.3,.17-.58,.33-.86,.5-3.97,2.31-7.95,4.62-11.91,6.95-1.07,.63-1.16,1.33-.38,2.41,.75,1.03,1.06,2.35,1.71,3.47,4.84,8.31,9.75,16.59,14.56,24.92,3.76,6.5,7.43,13.06,11.13,19.6,.88,1.56,1.68,1.91,3.53,.94,3.19-1.67,6.09-3.81,9.4-5.3,3.15-1.42,3.22-2.15,1.47-5.26-6.99-12.38-13.93-24.8-21.15-37.05-1.9-3.23-3.53-6.64-5.69-9.72-.45-.64-.8-1.5-1.81-1.46ZM203.92,201.26c-1.64,.09-2.13,1-2.7,2.1-1.72,3.33-3.6,6.58-5.45,9.84-.39,.69-1.52,1.05-1.35,1.94,.19,.99,1.16,1.34,2.06,1.83,6.17,3.36,12.29,6.82,18.38,10.34,9.91,5.72,19.77,11.51,29.66,17.26,.95,.55,1.78,.6,2.45-.56,2.2-3.84,4.55-7.6,6.66-11.49,.69-1.27,.85-2.49-1.12-3.49-5.51-2.83-10.85-6.01-16.23-9.1-10.32-5.92-20.63-11.87-30.93-17.83-.56-.32-.97-.9-1.42-.84h-.01Zm499.17,497.07c-.11-1.63-1-2.88-1.73-4.14-5.74-9.89-11.61-19.69-17.31-29.6-2.85-4.95-5.92-9.77-8.4-14.94-.85-1.77-2.11-2.71-4.47-1.24-3.05,1.9-6.25,3.57-9.39,5.31-3.16,1.75-3.16,1.77-1.43,4.65,2.94,4.9,5.9,9.78,8.78,14.71,6.26,10.7,12.52,21.41,18.69,32.16,.69,1.21,1.22,1.96,2.6,1.16,3.96-2.3,7.92-4.6,11.85-6.94,.38-.23,.57-.79,.81-1.13Zm78.91-165.21c-.97,.16-1.46,.87-1.87,1.56-2.23,3.8-4.37,7.65-6.65,11.42-.8,1.33,.12,1.99,.89,2.32,3.18,1.36,5.99,3.36,8.96,5.07,7.82,4.49,15.62,9.03,23.44,13.54,5.11,2.94,10.21,5.9,15.35,8.78,2.58,1.45,2.73,1.34,4.38-1.44,1.91-3.23,3.8-6.48,5.76-9.68,.88-1.44,.6-2.5-.79-3.31-2.13-1.23-4.3-2.4-6.43-3.64-8.21-4.79-16.39-9.62-24.61-14.39-5.38-3.12-10.81-6.17-16.23-9.23-.71-.4-1.49-.68-2.2-1.01h0Zm.15-289.3c.44-.28,.82-.65,1.28-.8,3.17-1.08,5.57-3.44,8.49-5,10.08-5.38,19.84-11.36,29.72-17.1,3.09-1.79,6.13-3.67,9.43-5.11,1.95-.85,1.99-1.99,.8-3.96-1.93-3.2-3.8-6.44-5.7-9.67-1.55-2.64-1.83-2.7-4.51-1.14-3.54,2.07-7.08,4.13-10.64,6.16-9.4,5.36-18.59,11.08-28.18,16.1-3.03,1.59-5.87,3.55-8.74,5.42-.85,.55-1.25,1.16-.44,2.41,2.29,3.54,4.29,7.26,6.38,10.92,.5,.87,.96,1.73,2.1,1.76h.01ZM205.22,578.02c.94-.4,2.05-.75,3.03-1.33,8.74-5.18,17.38-10.51,26.2-15.54,3.1-1.77,5.98-3.88,9.25-5.41,3.8-1.78,7.34-4.13,10.95-6.31,.76-.46,1.82-1.14,.8-2.33-2.35-2.75-3.46-6.23-5.47-9.19-2.81-4.16-2.7-4.24-6.93-1.8-.28,.16-.55,.35-.83,.51-7.62,4.48-15.25,8.95-22.87,13.44-7.32,4.31-14.62,8.68-21.99,12.91-1.18,.68-1.35,1.45-.84,2.35,2.19,3.82,4.52,7.56,6.71,11.39,.46,.81,.94,1.27,1.99,1.31h0ZM673.15,129.24c.95-.11,1.32-.71,1.76-1.48,3.41-5.95,6.92-11.85,10.37-17.78,5.51-9.48,10.98-18.99,16.51-28.45,1.78-3.04,1.57-3.28-1.3-5.31-3.4-2.39-7.73-3.11-10.7-6.16-.47-.48-.37-.37-.68,.12-2.17,3.47-4.53,6.81-6.61,10.33-4.58,7.74-9.04,15.56-13.53,23.36-3.09,5.37-6.12,10.77-9.26,16.11-.67,1.14-.54,1.53,.65,2.19,3.86,2.14,7.6,4.48,11.4,6.72,.41,.24,.95,.26,1.41,.37l-.02-.02ZM370.11,656.39c-.25-.41-.4-1.05-.76-1.2-4.41-1.86-8.27-4.72-12.52-6.87-1.33-.67-1.89,.29-2.34,1.08-2.79,4.78-5.5,9.61-8.24,14.42-3.41,5.96-6.83,11.92-10.24,17.88-3,5.25-5.88,10.58-9.01,15.75-.98,1.62-.55,2.38,.77,3.13,2.69,1.55,5.41,3.05,8.11,4.58,2.9,1.64,3.31,1.68,5.79-.47,1.28-1.12,2.4-2.39,3.27-3.94,3.36-6.01,6.9-11.92,10.33-17.89,4.54-7.89,9.06-15.8,13.57-23.72,.49-.85,.83-1.79,1.28-2.75h0ZM486.9,58.62c0-11.14,.02-22.28-.01-33.42,0-2.7-.07-2.72-2.68-2.73-2.66-.02-5.34,.21-7.98-.06-2.6-.26-4.37,.45-5.52,2.85-.69,1.44-2.59,3.26-2.2,4.04,.74,1.49,2.94,.46,4.51,.59,1.49,.13,2.99,.06,4.49,.05,1.25-.01,1.78,.61,1.77,1.82-.02,1.16,.01,2.33,.01,3.49v54.38c0,2.83,1.34,4.24,4.01,4.23,3.56-.02,3.58-.02,3.59-3.81,.01-10.48,0-20.95,0-31.43h.01Zm40.06,327.57c.16-7.26-5.21-12.83-12.25-13.28-7.36-.47-14.21,4.37-14.43,12.79-.2,7.29,4.84,13.29,12.93,13.96,7.21,.6,13.59-5.91,13.76-13.46h-.01Z\" /> <path class=\"cls-3\" d=\"M846.02,357.26c7.41,.3,13.7,2.67,17.74,9.42,3.97,6.63,3.21,14.37-1.87,19.71-1.11,1.16-3.26,1.89-3.05,3.65,.19,1.59,2.26,2.26,3.55,3.05,5.63,3.46,7.45,9.31,7.94,14.92,.41,4.79-1.21,10.05-4.47,14.13-8.31,10.37-19.74,11.99-31.04,7.49-5.13-2.04-8.49-6.56-10.68-11.72-.25-.6-.32-1.28-.49-1.91-.42-1.62-1.99-3.32-.94-4.86,1.32-1.93,3.72-.5,5.6-.88,.72-.15,1.24,.12,1.47,.92,.22,.77,.66,1.48,.87,2.26,2.75,9.91,12.61,12.31,20.49,10.18,4.42-1.19,7.67-3.69,9.58-7.76,4.08-8.7,.44-16.42-7.67-19.91-2.05-.88-4.34-1.34-6.72-1.3-4.18,.07-4.37-.25-4.3-4.52,0-.33,.03-.67,0-1-.15-1.47,.46-2.09,1.98-2.07,3.21,.04,5.97-1.32,8.73-2.81,3.9-2.11,5.86-5.26,5.41-9.66-.44-4.31-2.86-7.36-6.9-8.8-8.65-3.08-14.02-.14-17.53,6.98-.54,1.1-.61,2.8-2.11,2.96-1.96,.21-3.99,.17-5.94-.12-.94-.14-.69-1.38-.52-2.16,2.19-10.13,11.26-16.7,19.41-16.21,.5,.03,1,0,1.5,0l-.04,.02Z\" /> <path class=\"cls-3\" d=\"M535.87,93.87h-21.99c-.64,0-1.43,.21-1.56-.67-.05-.37,.33-.91,.66-1.21,5.08-4.79,9.43-10.26,14.28-15.26,6.71-6.89,13.18-13.98,19.3-21.39,1.98-2.39,3.74-4.88,4.72-7.96,1.82-5.72,.85-10.71-3.29-14.96-4.16-4.27-9.33-5.42-15-3.98-6.34,1.62-9.82,6.04-11.24,12.24-.11,.47,.11,1.02,0,1.49-.54,2.33-1.18,2.79-3.75,2.84-.67,0-1.34-.04-2,.02-1.5,.13-2-.52-2.01-2.02-.03-3.73,1.21-7,2.89-10.27,4.4-8.58,11.91-12.07,20.91-12.18,13.75-.18,22.88,11.37,21.88,23.4-.51,6.21-3.5,11.5-7.47,16.13-7.11,8.31-14.4,16.47-22.11,24.24-.43,.43-1.05,1.21-.91,1.56,.24,.61,1.13,.45,1.79,.45h26.48c3.23,0,3.23-.01,3.25,3.09,.03,2.95-1.42,4.42-4.35,4.41h-20.49v.02h0Z\" /> <path class=\"cls-3\" d=\"M180.61,357.11c6.18,.29,11.48,2.13,14.94,7.75,.89,1.44,2.57,2.24,3.4,3.9,3.6,7.21,3.12,14.44-.12,21.47-3,6.52-7.3,12.32-11.21,18.32-4.36,6.69-9.02,13.19-13.47,19.82-.44,.65-.65,1.46-1.26,2.05-.96,.93-1.52,1.76-3.19,.47-4.09-3.16-4.21-2.99-1.26-7.43,4.62-6.98,9.22-13.96,13.83-20.94,.43-.65,1.26-1.29,.57-2.13-.6-.73-1.52-.44-2.32-.29-8.13,1.52-14.4-1.8-19.14-7.99-6.4-8.35-5.24-19.17,.2-26.39,4.56-6.05,11.24-8.91,19.04-8.61h0Zm-.76,7.61c-8.52-.07-14.63,5.73-15.05,13.61-.43,8.25,5.61,15.43,14.33,15.61,8.07,.17,14.92-6.79,15.01-14.64,.09-7.95-7.39-15.59-14.29-14.58h0Z\" /> <path class=\"cls-3\" d=\"M515.61,751.54c-16.73,0-26.86-14.01-22.16-29.17,1.21-3.91,3.07-7.53,5.4-10.96,7.49-11,14.9-22.06,22.19-33.19,.8-1.22,1.15-1.26,2.15-.58,.68,.46,1.44,.79,2.13,1.24,2.9,1.91,2.9,1.93,1.04,4.78-4.35,6.69-8.71,13.38-13.07,20.06-.45,.69-1.06,1.29-1.39,2.02-.55,1.26,.1,2.3,1.55,2.33,3.12,.07,6.18,.1,9.22,1.18,9.54,3.4,14.92,13.28,13.49,23.33-1.04,7.35-4.96,12.21-10.77,15.95-3.33,2.14-7.2,2.86-9.78,2.99v.02Zm-15.82-22.22c-.03,8.52,5.76,14.47,14.11,14.49,8.82,.02,14.96-5.77,15.15-14,.19-7.79-5.87-14.96-14.4-15.09-7.74-.12-15.51,6.73-14.86,14.59h0Zm10.71-21.46c-.11,.11-.33,.25-.31,.33,.05,.24,.2,.45,.31,.67,.11-.11,.33-.25,.31-.33-.05-.24-.2-.45-.31-.67Z\" /> <path class=\"cls-3\" d=\"M339.97,69.38c1.01-.04,1.36,.82,1.81,1.46,2.16,3.08,3.79,6.48,5.69,9.72,7.22,12.25,14.16,24.66,21.15,37.05,1.75,3.11,1.68,3.84-1.47,5.26-3.31,1.49-6.21,3.63-9.4,5.3-1.84,.96-2.65,.62-3.53-.94-3.69-6.54-7.36-13.1-11.13-19.6-4.82-8.33-9.73-16.6-14.56-24.92-.65-1.11-.96-2.44-1.71-3.47-.78-1.08-.69-1.78,.38-2.41,3.96-2.33,7.94-4.63,11.91-6.95,.28-.16,.56-.32,.86-.5Z\" /> <path class=\"cls-3\" d=\"M203.92,201.26c.46-.06,.87,.52,1.42,.84,10.3,5.96,20.61,11.91,30.93,17.83,5.38,3.09,10.71,6.27,16.23,9.1,1.97,1.01,1.81,2.23,1.12,3.49-2.11,3.89-4.46,7.65-6.66,11.49-.67,1.16-1.5,1.12-2.45,.56-9.89-5.75-19.75-11.54-29.66-17.26-6.09-3.52-12.21-6.98-18.38-10.34-.9-.49-1.87-.84-2.06-1.83-.17-.89,.96-1.24,1.35-1.94,1.85-3.26,3.74-6.51,5.45-9.84,.57-1.1,1.06-2.01,2.7-2.1h.01Z\" /> <path class=\"cls-3\" d=\"M703.1,698.34c-.24,.35-.43,.91-.81,1.13-3.93,2.35-7.89,4.65-11.85,6.94-1.38,.8-1.9,.05-2.6-1.16-6.17-10.75-12.42-21.46-18.69-32.16-2.88-4.93-5.85-9.81-8.78-14.71-1.73-2.88-1.72-2.9,1.43-4.65,3.14-1.75,6.34-3.41,9.39-5.31,2.36-1.47,3.62-.53,4.47,1.24,2.48,5.17,5.56,9.99,8.4,14.94,5.7,9.91,11.57,19.71,17.31,29.6,.73,1.25,1.62,2.5,1.73,4.14Z\" /><path class=\"cls-3\" d=\"M782.01,533.13c.71,.32,1.49,.61,2.2,1.01,5.42,3.06,10.85,6.11,16.23,9.23,8.22,4.77,16.4,9.6,24.61,14.39,2.13,1.24,4.3,2.4,6.43,3.64,1.39,.81,1.67,1.87,.79,3.31-1.96,3.2-3.85,6.45-5.76,9.68-1.64,2.78-1.8,2.89-4.38,1.44-5.14-2.89-10.24-5.84-15.35-8.78-7.82-4.51-15.62-9.04-23.44-13.54-2.97-1.71-5.78-3.7-8.96-5.07-.77-.33-1.7-.99-.89-2.32,2.28-3.77,4.41-7.62,6.65-11.42,.41-.69,.9-1.4,1.87-1.56h0Z\" /><path class=\"cls-3\" d=\"M782.16,243.82c-1.14-.03-1.61-.89-2.1-1.76-2.09-3.66-4.09-7.38-6.38-10.92-.81-1.25-.4-1.86,.44-2.41,2.87-1.87,5.71-3.84,8.74-5.42,9.59-5.02,18.78-10.74,28.18-16.1,3.56-2.03,7.1-4.09,10.64-6.16,2.67-1.56,2.95-1.51,4.51,1.14,1.9,3.22,3.77,6.46,5.7,9.67,1.19,1.97,1.15,3.11-.8,3.96-3.3,1.44-6.34,3.32-9.43,5.11-9.89,5.74-19.64,11.72-29.72,17.1-2.92,1.56-5.32,3.92-8.49,5-.45,.15-.84,.52-1.28,.8h-.01Z\" /><path class=\"cls-3\" d=\"M205.22,578.02c-1.06-.04-1.53-.5-1.99-1.31-2.18-3.83-4.52-7.56-6.71-11.39-.51-.9-.34-1.67,.84-2.35,7.37-4.23,14.66-8.6,21.99-12.91,7.62-4.49,15.25-8.96,22.87-13.44,.28-.17,.55-.35,.83-.51,4.23-2.44,4.12-2.36,6.93,1.8,2,2.97,3.12,6.45,5.47,9.19,1.01,1.19-.04,1.86-.8,2.33-3.6,2.19-7.14,4.54-10.95,6.31-3.27,1.53-6.15,3.64-9.25,5.41-8.82,5.03-17.47,10.37-26.2,15.54-.98,.58-2.09,.92-3.03,1.33h0Z\" /><path class=\"cls-3\" d=\"M673.15,129.24c-.45-.11-1-.13-1.41-.37-3.8-2.23-7.54-4.58-11.4-6.72-1.19-.66-1.32-1.04-.65-2.19,3.14-5.34,6.17-10.74,9.26-16.11,4.49-7.8,8.95-15.62,13.53-23.36,2.08-3.51,4.45-6.86,6.61-10.33,.31-.5,.21-.61,.68-.12,2.97,3.05,7.3,3.77,10.7,6.16,2.87,2.02,3.08,2.26,1.3,5.31-5.53,9.47-11,18.97-16.51,28.45-3.45,5.93-6.96,11.83-10.37,17.78-.44,.77-.81,1.38-1.76,1.48l.02,.02Z\" /><path class=\"cls-3\" d=\"M370.11,656.39c-.44,.96-.79,1.89-1.28,2.75-4.51,7.92-9.02,15.83-13.57,23.72-3.44,5.97-6.97,11.88-10.33,17.89-.87,1.56-1.99,2.82-3.27,3.94-2.47,2.15-2.89,2.12-5.79,.47-2.7-1.53-5.42-3.04-8.11-4.58-1.32-.76-1.75-1.52-.77-3.13,3.14-5.17,6.02-10.49,9.01-15.75,3.4-5.97,6.83-11.92,10.24-17.88,2.75-4.81,5.45-9.64,8.24-14.42,.46-.78,1.01-1.75,2.34-1.08,4.25,2.15,8.11,5.01,12.52,6.87,.36,.15,.51,.78,.76,1.2h0Z\" /><path class=\"cls-3\" d=\"M486.9,58.62v31.43c0,3.8-.03,3.8-3.59,3.81-2.67,.01-4.01-1.4-4.01-4.23V35.25c0-1.16-.03-2.33-.01-3.49,.02-1.21-.52-1.83-1.77-1.82-1.5,.01-3,.08-4.49-.05-1.58-.14-3.78,.9-4.51-.59-.39-.79,1.51-2.61,2.2-4.04,1.15-2.4,2.92-3.11,5.52-2.85,2.63,.26,5.32,.04,7.98,.06,2.6,.02,2.67,.03,2.68,2.73,.03,11.14,.01,22.28,.01,33.42h-.01Z\" /><path class=\"cls-3\" d=\"M526.96,386.19c-.17,7.55-6.54,14.06-13.76,13.46-8.09-.67-13.13-6.67-12.93-13.96,.23-8.42,7.08-13.26,14.43-12.79,7.04,.45,12.41,6.02,12.25,13.28h.01Z\" /><path class=\"cls-2\" d=\"M179.85,364.72c6.9-1.01,14.38,6.63,14.29,14.58-.09,7.85-6.93,14.81-15.01,14.64-8.73-.18-14.76-7.36-14.33-15.61,.41-7.88,6.53-13.68,15.05-13.61h0Z\" /><path class=\"cls-2\" d=\"M499.79,729.31c-.65-7.86,7.12-14.71,14.86-14.59,8.53,.13,14.59,7.3,14.4,15.09-.2,8.23-6.33,14.02-15.15,14-8.34-.02-14.13-5.97-14.11-14.49h0Z\" /><path class=\"cls-2\" d=\"M510.5,707.84c.11,.22,.26,.44,.31,.67,.02,.08-.2,.22-.31,.33-.11-.22-.26-.44-.31-.67-.02-.08,.2-.22,.31-.33Z\" /></svg>");
            sb.append("</div></div>");
        }
        sb.append("</div>");
        sb.append("<div class=\"world\">");
        sb.append("<h1>World Clock</h1>");
        for (String city : CITY_TIMEZONES.keySet()) {
            String timezone = CITY_TIMEZONES.get(city);
            sb.append("<p><a href=\"/city?" + city + "\">" + city + "</a> (" + timezone + "): "
                    + getCurrentTimeDifference(timezone) + "</p>");
        }
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        sb.append("<script>");
        sb.append("var lHr = " + localTime.hour + ";");
        sb.append("var lMin =" + localTime.minute + ";");
        sb.append("var lSec = " + localTime.second + ";");
        sb.append("var cHr = " + cityTime.hour + ";");
        sb.append("var cMin =" + cityTime.minute + ";");
        sb.append("var cSec = " + cityTime.second + ";");
        sb.append("l_hr_rotation = 30 * lHr + lMin / 2;");
        sb.append("l_min_rotation = 6 * lMin;");
        sb.append("l_sec_rotation = 6 * lSec;");
        sb.append("c_hr_rotation = 30 * cHr + lMin / 2;");
        sb.append("c_min_rotation = 6 * cMin;");
        sb.append("c_sec_rotation = 6 * cSec;");
        sb.append("document.getElementsByClassName(\"hour\")[0].style.transform = `rotate(${l_hr_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"minute\")[0].style.transform = `rotate(${l_min_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"second\")[0].style.transform = `rotate(${l_sec_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"hour\")[1].style.transform = `rotate(${c_hr_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"minute\")[1].style.transform = `rotate(${c_min_rotation}deg)`;");
        sb.append("document.getElementsByClassName(\"second\")[1].style.transform = `rotate(${c_sec_rotation}deg)`;");
        sb.append("</script>");
        return sb.toString();
    }

    // ---------------- getCurrentTime ---------------- //
    private static String getCurrentTime(String timezone) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    // ---------------- getCurrentTimeDifference ---------------- //
    private static String getCurrentTimeDifference(String timezone) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        int hourDiff = cal.get(Calendar.HOUR_OF_DAY) - Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minuteDiff = cal.get(Calendar.MINUTE) - Calendar.getInstance().get(Calendar.MINUTE);
        return String.format("%+d:%02d", hourDiff, minuteDiff);
    }
}
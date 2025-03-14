// opendata.fmi.fi example by Timo Heimonen

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.Date;

public class WeatherData {
    public static void main(String[] args) {
        try {
            // This stored query fetches a point forecast from the Harmonie model
            String storedquery_id = "fmi::forecast::harmonie::surface::point::simple";
            String place = "Helsinki";

            // Define start and end times in UTC
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime later = now.plusDays(1); 
            
            // Build an ISO 8601 timestamp, e.g. 2025-03-14T12:00:00Z
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String startTime = now.format(timeFormatter);
            String endTime   = later.format(timeFormatter);

            // Construct the URL
            String urlString = "https://opendata.fmi.fi/wfs" +
                    "?service=WFS&version=2.0.0&request=getFeature" +
                    "&storedquery_id=" + URLEncoder.encode(storedquery_id, "UTF-8") +
                    "&place=" + URLEncoder.encode(place, "UTF-8") +
                    "&starttime=" + URLEncoder.encode(startTime, "UTF-8") +
                    "&endtime=" + URLEncoder.encode(endTime, "UTF-8");

            // Create the URI and URL
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            // Make the HTTP request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the XML response
            InputStream inputStream = connection.getInputStream();
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            // Fetch all weather data elements
            NodeList elements = document.getElementsByTagName("BsWfs:BsWfsElement");

            // Use SimpleDateFormat to parse the time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Loop through elements and print parameters
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
            
                String timeText = element
                        .getElementsByTagName("BsWfs:Time")
                        .item(0)
                        .getTextContent();
            
                String paramName = element
                        .getElementsByTagName("BsWfs:ParameterName")
                        .item(0)
                        .getTextContent();
            
                String paramValue = element
                        .getElementsByTagName("BsWfs:ParameterValue")
                        .item(0)
                        .getTextContent();
            
                // Convert the timestamp into a Date object (will display in the console in the local time zone)
                Date date = dateFormat.parse(timeText);
            
                // Only print if the parameter is 'Temperature'
                if ("Temperature".equalsIgnoreCase(paramName)) {
                    System.out.println(date + " | " + paramName + " = " + paramValue + " °C");
                }
            }

            // Disconnect the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
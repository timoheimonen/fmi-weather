import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.net.*;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatterBuilder;

public class WeatherData {
    public static void main(String[] args) {
        try {
            // This stored query fetches a point forecast from the Harmonie model
            String storedquery_id = "fmi::forecast::harmonie::surface::point::simple";
            String place = "Helsinki"; // Default value

            // Check if a place is provided as a command-line argument
            if (args.length > 0) {
                place = args[0];
            }

            // Define start and end times in UTC
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime later = now.plusDays(1);

            // Build an ISO 8601 timestamp, e.g. 2025-03-14T12:00:00Z
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String startTime = now.format(timeFormatter);
            String endTime = later.format(timeFormatter);

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

            // Use DateTimeFormatter to parse the time from the XML and for printing
            DateTimeFormatter parseFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .appendLiteral('Z')
                    .toFormatter();

            DateTimeFormatter printFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Print given location
            System.out.println(place);

            // Loop through elements and print parameters
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);

                String timeText = element.getElementsByTagName("BsWfs:Time").item(0).getTextContent();
                String paramName = element.getElementsByTagName("BsWfs:ParameterName").item(0).getTextContent();
                String paramValue = element.getElementsByTagName("BsWfs:ParameterValue").item(0).getTextContent();

                // Only print if the parameter is 'Temperature'
                if ("Temperature".equalsIgnoreCase(paramName)) {
                    try {
                         // Convert the timestamp to LocalDateTime in UTC
                        LocalDateTime localDateTime = LocalDateTime.parse(timeText, parseFormatter);
                        // Convert to local date and time
                        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"));
                        ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
                        
                        // Format the time to HH:mm
                        String formattedTime = localZonedDateTime.format(printFormatter);
                        System.out.println(formattedTime + " " + paramValue + " °C");
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date/time: " + timeText);
                        e.printStackTrace();
                    }
                }
            }

            // Disconnect the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

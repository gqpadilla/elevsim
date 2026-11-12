package elevatorsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CsvLoader {
    public static ArrayList <Request> load(String path) throws IOException {
        ArrayList <Request> requests = new ArrayList<>();
        int lineNum = 0;
        boolean headerCheck = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();

                // skip blank lines/comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                // header is 1st non-comment line "time,from,to"
                if (!headerCheck) {
                    if (trimmed.startsWith("\uFEFF")) {
                        trimmed = trimmed.substring(1); // remove hidden bom from csv
                    }

                    if (!trimmed.equals("time,from,to")) {
                        SimLogger.log("CSV line " + lineNum + " invalid header.");
                    }

                    headerCheck = true;
                    continue;
                }

                // skip if not 3 columns
                String[] parts = trimmed.split("[,\\s]+");
                if (parts.length != 3) {
                    SimLogger.log("CSV line " + lineNum + " invalid column count.");
                    continue;
                }

                int time, from, to;
                try {
                    time = Integer.parseInt(parts[0].trim());
                    from = Integer.parseInt(parts[1].trim());
                    to = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    SimLogger.log("CSV line " + lineNum + " non-integer values.");
                    continue;
                }

                requests.add(new Request(time, from, to));
            }
        }

        Collections.sort(requests, Comparator.comparingInt(Request::getTime)); // sort by time
        return requests;
    }
}

package fr.rugeri;

import com.google.gson.Gson;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PVGISDataProcessor {

    /**
     * @param args
     *  -i for each PVGIS hourly data csv file to merge
     *  -o output json filename
     */
    public static void main(String[] args) throws Exception {
        Input input = parseCommandLineParameters(args);

        try {
            input.check();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        List<DataPoint> mergedDataPoints = new ArrayList<>();

        for(String filePath : input.filePaths) {
            List<DataPoint> fileDataPoints = new ArrayList<>();
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                DataPoint p = parseLine(line);
                if (p != null) {
                    fileDataPoints.add(p);
                }
            }
            mergeDatePoints(mergedDataPoints, fileDataPoints);
        }

        Output output = processData(mergedDataPoints);

        FileWriter myWriter = new FileWriter(input.outputFilePath);
        myWriter.write(new Gson().toJson(output));
        myWriter.close();

        System.out.println("Program ended successfully. "+input.outputFilePath+" has been written.");
    }

    static Output processData(List<DataPoint> mergedDataPoints) {
        Output output = new Output();

        // month / hour -> avg monthly production
        output.data = mergedDataPoints.stream()
                .collect(Collectors.groupingBy(p -> p.month))
                .entrySet().stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey, // month
                        e -> e.getValue().stream().collect(Collectors.groupingBy(p -> p.hour))
                            .entrySet().stream().collect(
                                Collectors.toMap(
                                    Map.Entry::getKey, // hour
                                    e2 -> Double.valueOf(
                                            e2.getValue().stream()
                                            .mapToDouble(p -> p.production)
                                            .average().getAsDouble()
                                        ).intValue()
                                )
                            )
                    )
                );

        output.overallAverageYearlyProduction = Double.valueOf(
                    mergedDataPoints.stream()
                    .collect(Collectors.groupingBy(p -> p.year))
                    .entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey, // year
                            e -> e.getValue().stream().mapToDouble(p -> p.production).sum()
                        )
                    )
                    .values().stream()
                    .mapToDouble(d -> d)
                    .average().getAsDouble()
                ).intValue();

        return output;
    }

    private static Input parseCommandLineParameters(String[] args) {
        Options options = new Options();

        Option i = new Option("i", "input", true, "PVGIS input file paths to merge and compute.");
        i.setRequired(true);
        options.addOption(i);

        Option o = new Option("o", "output", true, "json output filepath");
        o.setRequired(true);
        options.addOption(o);

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            Input input = new Input();
            input.filePaths = Arrays.asList(cmd.getOptionValues("input"));
            input.outputFilePath = cmd.getOptionValue("output");
            return input;
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("pvgis-data-processor", options);
            System.exit(-1);
            return null;
        }
    }

    private static void mergeDatePoints(List<DataPoint> mergedDataPoints, List<DataPoint> fileDataPoints) {
        if (mergedDataPoints.isEmpty()) {
            mergedDataPoints.addAll(fileDataPoints);
            return;
        }
        for (DataPoint dp : fileDataPoints) {
            Optional<DataPoint> existingSamePeriodDP = mergedDataPoints.stream().filter(dp2 -> dp2.referencesSamePeriod(dp)).findFirst();
            if (existingSamePeriodDP.isEmpty()) {
                mergedDataPoints.add(dp);
            } else {
                existingSamePeriodDP.get().merge(dp);
            }
        }
    }

    //ex: 20140101:0010,0.0,0.0,0.0,6.97,2.26,0.0
    private final static Pattern p = Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2}):([0-9]{2})[0-9]{2},([0-9.]+),[.]*");
    private static DataPoint parseLine(String line) {
        Matcher m = p.matcher(line);
        if (m.find()) {
            DataPoint point = new DataPoint();
            point.year = Integer.parseInt(m.group(1));
            point.month = Integer.parseInt(m.group(2));
            point.day = Integer.parseInt(m.group(3));
            point.hour = Integer.parseInt(m.group(4));
            point.production = Float.parseFloat(m.group(5));
            return point;
        }
        return null; // invalid line, skip
    }

    static class Input {
        List<String> filePaths;
        String outputFilePath;

        public void check() throws FileNotFoundException {
            for (String fp : filePaths) {
                if (!new File(fp).exists()) {
                    throw new FileNotFoundException("Invalid -i filepath '"+fp+"', file not found.");
                }
            }
        }
    }

    static class Output {
        Map<Integer, Map<Integer, Integer>> data;
        Integer overallAverageYearlyProduction;
    }

    static class DataPoint {
        int year;
        int month;
        int day;
        int hour;
        float production;

        boolean referencesSamePeriod(DataPoint dp) {
            return year == dp.year && month == dp.month && day == dp.day && hour == dp.hour;
        }

        void merge(DataPoint dp) {
            production += dp.production;
        }
    }
}
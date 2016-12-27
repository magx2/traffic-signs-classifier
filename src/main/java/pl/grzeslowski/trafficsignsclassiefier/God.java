package pl.grzeslowski.trafficsignsclassiefier;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class God {
    private String pathToData = "D:\\Programowanie\\deep_learning\\znaki_drogowe";
    private String metaDataFileName = "traffic-signs.csv";
    private String separator = ",";

    public static void main(String[] args) throws Exception {
        final God god = new God();
        god.run();
    }

    private void run() throws Exception {
        final File metaData = new File(pathToData + File.separator + metaDataFileName);
        checkArgument(metaData.exists());

        Set<Example> examples = loadExamples(metaData);

        System.out.printf("# examples: %s%n", examples.size());
        System.out.printf("Signs types: %s%n",
                examples.stream()
                        .flatMap(e -> e.getSigns().stream())
                        .map(Sign::getType)
                        .distinct()
                        .collect(Collectors.joining(", ")));
        System.out.printf("Occurrences of each sign type %s%n",
                examples.stream()
                        .flatMap(e -> e.getSigns().stream())
                        .collect(
                                Collectors.groupingBy(
                                        Function.identity(), Collectors.counting()
                                )
                        ));
    }

    private Set<Example> loadExamples(File metaData) throws IOException {
        try (Stream<String> stream = Files.lines(metaData.toPath())) {
            return stream.map(line -> line.split(separator))
                    .map(this::parseExample)
                    .collect(Collectors.toSet());
        }
    }

    private Example parseExample(String[] line) {
        checkArgument(line.length >= 2, "Line length = " + line.length);
        final File photoFile = new File(pathToData + File.separator + line[0]);

        final Set<Sign> signs = new HashSet<>();
        for (int i = 1; i < line.length; i++) {
            String sign = line[i];
            if (sign.isEmpty()) {
                break;
            }
            checkArgument(sign.length() >= 2, "Sign = " + sign);
            final String type = sign.charAt(0) + "";
            final String subType = sign.substring(1);
            signs.add(new Sign(type, subType));
        }

        return new Example(photoFile, signs);
    }

}

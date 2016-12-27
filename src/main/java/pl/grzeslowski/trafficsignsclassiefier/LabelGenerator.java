package pl.grzeslowski.trafficsignsclassiefier;

import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.writable.BooleanWritable;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class LabelGenerator implements PathLabelGenerator {
    private final Sign sign;
    private Set<Example> examples;

    public LabelGenerator(Sign sign, Set<Example> examples) {
        this.sign = checkNotNull(sign);
        this.examples = checkNotNull(examples);
        checkArgument(examples.size() > 0);
    }

    @Override
    public Writable getLabelForPath(String path) {
        final File file = new File(path);
        final Example example = examples.stream()
                .filter(e -> file.getName().equalsIgnoreCase(e.getPhotoFile().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find file with path %s in examples!", file.getAbsolutePath())));
        return new BooleanWritable(
                example.getSigns().contains(sign)
        );
    }

    @Override
    public Writable getLabelForPath(URI uri) {
        return getLabelForPath(new File(uri).toString());
    }
}

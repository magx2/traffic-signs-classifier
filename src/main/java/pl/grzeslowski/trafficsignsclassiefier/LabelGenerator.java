package pl.grzeslowski.trafficsignsclassiefier;

import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.writable.BooleanWritable;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class LabelGenerator extends AbstractLabelGenerator {
    private final Sign sign;
    private Set<Example> examples;

    public LabelGenerator(Sign sign, Set<Example> examples) {
        super(examples);
        this.sign = checkNotNull(sign);
    }

    @Override
    public Writable getLabelForPath(String path) {
        final Example example = findExampleForPath(path);
        return new BooleanWritable(
                example.getSigns().contains(sign)
        );
    }

    @Override
    public Writable getLabelForPath(URI uri) {
        return getLabelForPath(new File(uri).toString());
    }
}

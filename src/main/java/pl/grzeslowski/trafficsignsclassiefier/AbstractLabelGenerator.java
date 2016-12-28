package pl.grzeslowski.trafficsignsclassiefier;

import com.google.common.collect.ImmutableSet;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.writable.Writable;

import java.io.File;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractLabelGenerator implements PathLabelGenerator {
    private final Set<Example> examples;

    AbstractLabelGenerator(Set<Example> examples) {
        this.examples = ImmutableSet.copyOf(checkNotNull(examples));
        checkArgument(examples.size() > 0);
    }

    Example findExampleForPath(String path) {
        final File file = new File(path);
        return examples.stream()
                .filter(e -> file.getName().equalsIgnoreCase(e.getPhotoFile().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find file with path %s in examples!", file.getAbsolutePath())));
    }

    @Override
    public Writable getLabelForPath(URI uri) {
        return getLabelForPath(new File(uri).toString());
    }
}

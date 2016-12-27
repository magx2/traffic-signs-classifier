package pl.grzeslowski.trafficsignsclassiefier;

import com.google.common.base.Joiner;

import java.io.File;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Example {
    private final File photoFile;
    private final Set<Sign> signs;

    public Example(File photoFile, Set<Sign> signs) {
        checkArgument(!signs.isEmpty());
        this.photoFile = checkNotNull(photoFile);
        this.signs = signs;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public Set<Sign> getSigns() {
        return signs;
    }

    @Override
    public String toString() {
        return "Example{" +
                "photoFile=" + photoFile.getAbsolutePath() +
                ", signs=" + Joiner.on(", ").join(signs) +
                '}';
    }
}

package pl.grzeslowski.trafficsignsclassiefier.labelgenerators;

import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
import pl.grzeslowski.trafficsignsclassiefier.B33Sign;
import pl.grzeslowski.trafficsignsclassiefier.Example;

import java.util.Optional;
import java.util.Set;

public class B33LabelGenerator extends AbstractLabelGenerator {
    public B33LabelGenerator(Set<Example> examples) {
        super(examples);
    }

    @Override
    public Writable getLabelForPath(String path) {
        final Optional<B33Sign> b33Sign = findExampleForPath(path).getSigns()
                .stream()
                .filter(s -> s instanceof B33Sign)
                .map(s -> (B33Sign) s)
                .findFirst();
        if (b33Sign.isPresent()) {
            return new Text(b33Sign.get().getSpeed() + "km/h");
        } else {
            return new Text("no B33");
        }
    }
}

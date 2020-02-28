package eu.quelltext.wget.bin.wget.options;

import eu.quelltext.wget.bin.wget.options.display.Strategy;

public class EnvironmentOptionBuilder extends ArgumentOptionBuilder {
    public EnvironmentOptionBuilder(String cmd, int nameId, int explanationId, Strategy displayStrategy) {
        super(cmd, nameId, explanationId, displayStrategy);
    }

    @Override
    public ArgumentOption to(String argument) {
        return new EnvironmentOption(manualId(), getNameId(), getExplanationId(), argument);
    }
}

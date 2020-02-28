package eu.quelltext.wget.bin.wget.options.display;

import eu.quelltext.wget.bin.wget.options.Option;

public interface DisplayableOption {

    void displayIn(Display section);

    String manualId();

    void fillWith(Display display, Option option);

    Option createNewFrom(Display display);

}

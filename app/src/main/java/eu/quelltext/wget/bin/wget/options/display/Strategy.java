package eu.quelltext.wget.bin.wget.options.display;

public interface Strategy {
    void displayIn(Display section);
    void setArgumentIn(Display display, String argument);
    String getArgument(Display display);
    String getDefaultArgument();
}

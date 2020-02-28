package eu.quelltext.wget.bin.wget.options.display;

public class Integer implements Strategy {
    private int defaultValue;

    public Integer(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void displayIn(Display display) {
        display.addNumber();
    }

    @Override
    public void setArgumentIn(Display display, String argument) {
        display.setNumber(argument);
    }

    @Override
    public String getArgument(Display display) {
        return display.getNumber();
    }

    @Override
    public String getDefaultArgument() {
        return java.lang.Integer.toString(defaultValue);
    }
}

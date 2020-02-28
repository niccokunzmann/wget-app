package eu.quelltext.wget.bin.wget.options.display;

public class Text implements Strategy {
    private final String text;

    public Text(String defaultValue) {
        text = defaultValue;
    }

    @Override
    public void displayIn(Display display) {
        display.addTextField(getDefaultArgument());
    }

    @Override
    public void setArgumentIn(Display display, String text) {
        display.setText(text);
    }

    @Override
    public String getArgument(Display display) {
        return display.getText();
    }

    @Override
    public String getDefaultArgument() {
        return text;
    }
}

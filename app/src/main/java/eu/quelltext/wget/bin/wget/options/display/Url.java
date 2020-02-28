package eu.quelltext.wget.bin.wget.options.display;

public class Url implements Strategy {
    private final String url;

    public Url(String defaultValue) {
        url = defaultValue;
    }

    @Override
    public void displayIn(Display display) {
        display.addUrlField(getDefaultArgument());
    }

    @Override
    public void setArgumentIn(Display display, String url) {
        display.setUrl(url);
    }

    @Override
    public String getArgument(Display display) {
        return display.getUrl();
    }

    @Override
    public String getDefaultArgument() {
        return url;
    }
}

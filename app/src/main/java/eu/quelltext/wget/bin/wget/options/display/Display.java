package eu.quelltext.wget.bin.wget.options.display;

public interface Display {

    void invalid();
    void addTitle(int nameId);
    void addExplanation(int explanationId);
    void addSwitch();
    void addNumberField(String defaultNumber);
    void addFileDialog(String file);
    void switchOn();
    void setNumber(String argument);
    void setPath(String argument);
    String getNumber();
    String getPath();
    void addDirectoryDialog(String directory);
    String getUrl();
    void setUrl(String url);
    void addUrlField(String defaultUrl);
    void addTextField(String defaultText);
    void setText(String text);
    String getText();
}

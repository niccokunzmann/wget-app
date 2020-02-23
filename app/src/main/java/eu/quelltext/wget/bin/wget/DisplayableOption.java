package eu.quelltext.wget.bin.wget;

public interface DisplayableOption {

    void displayIn(Display section);

    String manualId();

    void fillWith(Display display, Option option);

    Option createNewFrom(Display display);

    interface Display {

        void invalid();
        void addTitle(int nameId);
        void addExplanation(int explanationId);
        void addSwitch();
        void addNumber();
        void addFileDialog(String file);
        void switchOn();
        void setNumber(String argument);
        void setPath(String argument);
        String getNumber();
        String getPath();
        void addDirectoryDialog();
    }
}

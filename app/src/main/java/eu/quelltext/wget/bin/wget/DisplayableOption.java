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
        void addFileDialog();
        void switchOn();
        void setNumber(String argument);
        void setFile(String argument);
        String getNumber();
        String getFile();
    }
}

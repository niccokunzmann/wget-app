package eu.quelltext.wget.bin.wget;

import eu.quelltext.wget.activities.ConfigurationActivity;

public interface DisplayableOption {

    void displayIn(Display section);

    String manualId();

    void fillWith(Display display, Option option);

    interface Display {

        void invalid();
        void addTitle(int nameId);
        void addExplanation(int explanationId);
        void addSwitch();
        void addNumber();
        void addFileDialog();
        void switchOn();
        void setNumber(int i);
        void setFile(String argument);
    }
}

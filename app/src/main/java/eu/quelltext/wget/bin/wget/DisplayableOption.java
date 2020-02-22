package eu.quelltext.wget.bin.wget;

public interface DisplayableOption {

    void displayIn(Display section);

    interface Display {

        void invalid();

        void addTitle(int nameId);

        void addExplanation(int explanationId);

        void addSwitch();

        void addIntegerField();

        void addFileDialog();
    }
}

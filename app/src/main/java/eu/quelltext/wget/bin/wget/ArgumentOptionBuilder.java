package eu.quelltext.wget.bin.wget;

import org.json.JSONException;
import org.json.JSONObject;

class ArgumentOptionBuilder implements Options.Manual.ManualEntry, DisplayableOption {

    public static final String JSON_ARGUMENT = "argument";

    private final String cmd;
    private final int nameId;
    private final int explanationId;
    private final DisplayStrategy displayStrategy;

    public ArgumentOptionBuilder(String cmd, int nameId, int explanationId, DisplayStrategy displayStrategy) {
        super();
        this.cmd = cmd;
        this.nameId = nameId;
        this.explanationId = explanationId;
        this.displayStrategy = displayStrategy;
    }

    public ArgumentOption to(String argument) {
        return new ArgumentOption(this.cmd, this.nameId, this.explanationId, argument);
    }

    @Override
    public Option fromManualJSON(JSONObject json) throws JSONException {
        String argument = json.getString(JSON_ARGUMENT);
        Option option = new ArgumentOption(cmd, nameId, explanationId, argument);
        return option;
    }

    @Override
    public String manualId() {
        return cmd;
    }

    @Override
    public void fillWith(Display display, Option option) {
        display.switchOn();
        String argument = option.getArgument();
        displayStrategy.setArgumentIn(display, argument);
    }

    @Override
    public Option createNewFrom(Display display) {
        return to(displayStrategy.getArgument(display));
    }

    @Override
    public void displayIn(Display section) {
        section.addSwitch();
        section.addTitle(this.nameId);
        section.addExplanation(this.explanationId);
        displayStrategy.displayIn(section);
    }

    public interface DisplayStrategy{
        void displayIn(Display section);
        void setArgumentIn(Display display, String argument);
        String getArgument(Display display);
    }

    public static final DisplayStrategy INTEGER = new DisplayStrategy() {

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
    };

    public static final DisplayStrategy FILE = new DisplayStrategy() {

        @Override
        public void displayIn(Display section) {
            section.addFileDialog();
        }

        @Override
        public void setArgumentIn(Display display, String argument) {
            display.setPath(argument);
        }

        @Override
        public String getArgument(Display display) {
            return display.getPath();
        }
    };

    public static final DisplayStrategy DIRECTORY = new DisplayStrategy() {

        @Override
        public void displayIn(Display section) {
            section.addDirectoryDialog();
        }

        @Override
        public void setArgumentIn(Display display, String argument) {
            display.setPath(argument);
        }

        @Override
        public String getArgument(Display display) {
            return display.getPath();
        }
    };
}

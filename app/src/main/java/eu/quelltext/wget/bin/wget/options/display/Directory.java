package eu.quelltext.wget.bin.wget.options.display;

import android.os.Environment;

public class Directory implements Strategy {
    @Override
    public void displayIn(Display section) {
        section.addDirectoryDialog(getDefaultArgument());
    }

    @Override
    public void setArgumentIn(Display display, String argument) {
        display.setPath(argument);
    }

    @Override
    public String getArgument(Display display) {
        return display.getPath();
    }

    @Override
    public String getDefaultArgument() {
        // This is the directory wget is run in.
        return ".";
    }
}

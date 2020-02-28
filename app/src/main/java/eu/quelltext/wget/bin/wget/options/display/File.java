package eu.quelltext.wget.bin.wget.options.display;

public class File implements Strategy {

    private String file;

    public File(String file) {
        this.file = file;
    }

    @Override
    public void displayIn(Display section) {
        section.addFileDialog(file);
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
        return file;
    }

}

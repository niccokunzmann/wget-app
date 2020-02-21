package eu.quelltext.wget.bin;

import androidx.annotation.NonNull;

import java.io.IOException;

class BinaryWget implements IWget {
    private final Executable executable;

    public BinaryWget(Executable executable) {
        this.executable = executable;
    }

    @NonNull
    @Override
    public String version() {
        try {
            Executable.Result result = executable.run(new String[]{"--version"});
            result.waitFor();
            return result.getOutput();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean isValid() {
        String version = version();
        return !version.equals("");
    }

    @Override
    public Executable.Result run(String[] command) throws IOException {
        return executable.run(command);
    }
}

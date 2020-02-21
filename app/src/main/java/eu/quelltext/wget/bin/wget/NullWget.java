package eu.quelltext.wget.bin.wget;

import androidx.annotation.NonNull;

import java.io.IOException;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.Executable;
import eu.quelltext.wget.bin.wget.IWget;

public class NullWget implements IWget {
    @Override
    @NonNull
    public String version() {
        return "null";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Executable.Result run(String[] command) {
        return new NullResult();
    }

    private class NullResult implements Executable.Result {

        @Override
        public void waitFor() throws InterruptedException {
        }

        @Override
        public String getOutput() throws IOException {
            return "";
        }

        @Override
        public int getReturnCode() {
            return R.string.command_result_0;
        }
    }
}

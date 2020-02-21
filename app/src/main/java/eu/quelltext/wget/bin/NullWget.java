package eu.quelltext.wget.bin;

import androidx.annotation.NonNull;

public class NullWget implements IWget {
    @Override
    @NonNull
    public String version() {
        return "null";
    }
}

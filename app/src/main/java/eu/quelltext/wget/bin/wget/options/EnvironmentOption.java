package eu.quelltext.wget.bin.wget.options;

import android.os.Parcel;

public class EnvironmentOption extends ArgumentOption {


    public EnvironmentOption(String cmd, int nameId, int explanationId, String argument) {
        super(cmd, nameId, explanationId, argument);
    }

    public EnvironmentOption(Parcel in) {
        super(in);
    }

    @Override
    public String[] asArguments() {
        return new String[0];
    }

    @Override
    public String asEnvironmentVariable() {
        return manualId() + "=" + getArgument();
    }
}

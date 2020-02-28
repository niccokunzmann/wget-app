package eu.quelltext.wget.bin.wget.options;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import eu.quelltext.wget.bin.wget.Command;
import eu.quelltext.wget.bin.wget.options.display.Display;
import eu.quelltext.wget.bin.wget.options.display.DisplayableOption;

/* Base class for different options.
 *
 * If you create a new subclass, make sure you add serialization to this.Creator and
 *
 */
public class Option implements Parcelable, Options.Manual.ManualEntry, DisplayableOption {

    protected Option() {
    }

    protected Option(Parcel in) {
    }

    public static final Creator<Option> CREATOR = new Creator<Option>() {
        @Override
        public Option createFromParcel(Parcel in) {
            String name = in.readString();
            if (name.equals(getClass().getName())) {
                return new Option(in);
            } if (name.equals(BinaryOption.class.getName())){
                return new BinaryOption(in);
            } if (name.equals(ArgumentOption.class.getName())){
                return new ArgumentOption(in);
            } if (name.equals(Unrecorded.class.getName())){
                return new Unrecorded(in);
            } if (name.equals(EnvironmentOption.class.getName())){
                return new EnvironmentOption(in);
            }
            return new Unknown();
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };

    public static Option fromJSON(JSONObject jsonOption) throws JSONException {
        return Options.MANUAL.optionFromJSON(jsonOption);
    }

    public Option fromManualJSON(JSONObject jsonOption) {
        return new Unknown();
    }

    @Override
    @NonNull
    public String manualId() {
        return "";
    }

    public JSONObject toJSON() throws JSONException {
        return Options.MANUAL.toJSON(this);
    }

    public void toJSON(JSONObject json) throws JSONException {
        // write an option to json, the id is already saved
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getClass().getName());
    }

    public String[] asArguments() {
        return new String[0];
    }

    // return null or an env string used for a process "FOO=false"
    // see https://stackoverflow.com/a/8607281/1320237
    public String asEnvironmentVariable() {
        return null;
    }

    public String toShortText(Context context) {
        return "";
    }

    @Override
    public void displayIn(Display section) {
        section.invalid();
    }

    public void fillWith(Display display, Option option) {
        display.switchOn();
    }

    @Override
    public Option createNewFrom(Display builder) {
        return this;
    }

    /* return the argument if the option has one */
    @NonNull
    public String getArgument() {
        return "";
    }

    public boolean readsExternalStorage() {
        return false; //TODO: add input file option
    }

    public boolean revokesWritingToExternalStorage() {
        return (manualId().equals("-O") || manualId().equals("--output-document")) && getArgument().equals("-");
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (Option.class.isInstance(other)) {
            return ((Option)other).equalsOption(this);
        }
        return super.equals(other);
    }

    private boolean equalsOption(Option option) {
        return manualId().equals(option.manualId()) && getArgument().equals(option.getArgument());
    }

    @Override
    public int hashCode() {
        return manualId().hashCode() ^ getArgument().hashCode();
    }

    static class Unknown extends Option {
        @Override
        public String toShortText(Context context) {
            return "unknown";
        }

        @Override
        public String manualId() {
            return "unknown";
        }
    }

    public static class Unrecorded extends Option {
        private final String id;

        public Unrecorded(Parcel in) {
            super(in);
            id = in.readString();
        }

        public Unrecorded(String id) {
            this.id = id;
        }

        @Override
        public String manualId() {
            return id;
        }

        @Override
        public String toShortText(Context context) {
            return id;
        }
    }


}

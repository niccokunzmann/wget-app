package eu.quelltext.wget.bin.wget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/*
  Base class for different options.
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
            } if (name.equals(ArgumentOptionBuilder.ArgumentOption.class.getName())){
                return new ArgumentOptionBuilder.ArgumentOption(in);
            } if (name.equals(Unrecorded.class.getName())){
                return new Unrecorded(in);
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

    public String toShortText(Context context) {
        return "";
    }

    @Override
    public void displayIn(Display section) {
        section.invalid();
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

    static class Unrecorded extends Option {
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

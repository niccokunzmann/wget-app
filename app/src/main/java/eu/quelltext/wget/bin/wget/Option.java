package eu.quelltext.wget.bin.wget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import eu.quelltext.wget.R;

/*
  Base class for different options.
 */
public class Option implements Parcelable {

    public static Option VERSION = new BinaryOption("--version", R.string.command_name_version, R.string.command_explanation_version);
    public static Option CONTINUE = new BinaryOption("--continue", R.string.command_name_continue, R.string.command_explanation_continue);
    public static Option RECURSIVE = new BinaryOption("--recursive", R.string.command_name_recursive, R.string.command_explanation_recursive);
    public static Option MIRROR = new BinaryOption("--mirror", R.string.command_name_mirror, R.string.command_explanation_mirror);

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
            }
            return new Unknown(in);
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };

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

    static class Unknown extends Option {
        public Unknown(Parcel in) {
            super(in);
        }

        @Override
        public String toShortText(Context context) {
            return "unknown";
        }
    }
}

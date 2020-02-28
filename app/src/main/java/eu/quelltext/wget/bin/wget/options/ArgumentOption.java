package eu.quelltext.wget.bin.wget.options;

import android.content.Context;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

public class ArgumentOption extends Option {

    private final String cmd;
    private final int nameId;
    private final int explanationId;
    private final String argument;

    public ArgumentOption(String cmd, int nameId, int explanationId, String argument) {
        this.cmd = cmd;
        this.nameId = nameId;
        this.explanationId = explanationId;
        this.argument = argument;
    }
    public ArgumentOption(Parcel in) {
        cmd = in.readString();
        nameId = in.readInt();
        explanationId = in.readInt();
        argument = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(cmd);
        parcel.writeInt(nameId);
        parcel.writeInt(explanationId);
        parcel.writeString(argument);
    }

    @Override
    public String[] asArguments() {
        return new String[]{cmd, argument};
    }

    @Override
    public String toShortText(Context context) {
        return context.getResources().getString(nameId);
    }

    @Override
    public void toJSON(JSONObject json) throws JSONException {
        super.toJSON(json);
        json.put(ArgumentOptionBuilder.JSON_ARGUMENT, argument);
    }

    @Override
    public String manualId() {
        return cmd;
    }

    @Override
    public String getArgument() {
        return argument;
    }
}

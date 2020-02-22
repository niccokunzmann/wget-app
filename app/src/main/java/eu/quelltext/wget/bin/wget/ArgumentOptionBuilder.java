package eu.quelltext.wget.bin.wget;

import android.content.Context;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

class ArgumentOptionBuilder implements Options.Manual.ManualEntry {

    private static final String JSON_ARGUMENT = "argument";

    private final String cmd;
    private final int nameId;
    private final int explanationId;

    public ArgumentOptionBuilder(String cmd, int nameId, int explanationId) {
        super();
        this.cmd = cmd;
        this.nameId = nameId;
        this.explanationId = explanationId;
    }

    public ArgumentOption to(String argument) {
        return new ArgumentOption(this.cmd, this.nameId, this.explanationId, argument);
    }

    @Override
    public Option fromManualJSON(JSONObject json) throws JSONException {
        String argument = json.getString(JSON_ARGUMENT);
        Option option = new ArgumentOption(cmd, nameId, explanationId, argument);
        return option;
    }

    @Override
    public String manualId() {
        return cmd;
    }

    public static class ArgumentOption extends eu.quelltext.wget.bin.wget.Option {

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
            json.put(JSON_ARGUMENT, argument);
        }

        @Override
        public String manualId() {
            return cmd;
        }
    }
}

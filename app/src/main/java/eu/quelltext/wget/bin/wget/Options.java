package eu.quelltext.wget.bin.wget;

import android.graphics.Path;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.quelltext.wget.R;
import eu.quelltext.wget.activities.MainActivity;

public class Options {

    public static Option VERSION = addBinary("--version", R.string.command_name_version, R.string.command_explanation_version);
    public static Option CONTINUE = new BinaryOption("--continue", R.string.command_name_continue, R.string.command_explanation_continue);
    public static Option RECURSIVE = new BinaryOption("--recursive", R.string.command_name_recursive, R.string.command_explanation_recursive);
    public static Option MIRROR = new BinaryOption("--mirror", R.string.command_name_mirror, R.string.command_explanation_mirror);
    public static Option DEBUG = new BinaryOption("--debug", R.string.command_name_debug, R.string.command_explanation_debug);
    public static ArgumentOptionBuilder OUTPUT = addArgument("-O", R.string.command_name_output_document, R.string.command_explanation_output_document);

    public static Manual MANUAL = new Manual();

    private static ArgumentOptionBuilder addArgument(String id, int name, int text) {
        ArgumentOptionBuilder result = new ArgumentOptionBuilder(id, name, text);
        MANUAL.store(result);
        return result;
    }

    private static Option addBinary(String id, int name, int text) {
        BinaryOption option = new BinaryOption(id, name, text);
        MANUAL.store(option);
        return option;
    }

    public static class Manual {

        private static final String JSON_ID = "id";
        HashMap<String, ManualEntry> store = new HashMap<>();

        public void store(ManualEntry entry) {
            store.put(entry.manualId(), entry);
        }

        public JSONObject toJSON(Option option) throws JSONException {
            JSONObject json = new JSONObject();
            option.toJSON(json);
            json.put(JSON_ID, option.manualId());
            return json;
        }

        public Option optionFromJSON(JSONObject json) throws JSONException {
            String id = json.getString(JSON_ID);
            if (store.containsKey(id)) {
                ManualEntry entry = store.get(id);
                Option option = entry.fromManualJSON(json);
                return option;
            }
            return new Option.Unrecorded(id);
        }

        public interface ManualEntry {
            Option fromManualJSON(JSONObject json) throws JSONException;
            String manualId();
        }

    }

}

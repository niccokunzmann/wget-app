package eu.quelltext.wget.bin.wget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import eu.quelltext.wget.R;

public class Options {

    public static Manual MANUAL = new Manual();

    public static final Option VERSION = addBinary("--version", R.string.command_name_version, R.string.command_explanation_version);
    public static final Option HELP = addBinary("--help", R.string.command_name_help, R.string.command_explanation_help);

    public static final  Option CONTINUE = addBinary("--continue", R.string.command_name_continue, R.string.command_explanation_continue);
    public static final Option RECURSIVE = addBinary("--recursive", R.string.command_name_recursive, R.string.command_explanation_recursive);
    public static final Option MIRROR = addBinary("--mirror", R.string.command_name_mirror, R.string.command_explanation_mirror);
    public static final Option DEBUG = addBinary("--debug", R.string.command_name_debug, R.string.command_explanation_debug);

    public static final ArgumentOptionBuilder OUTPUT_DOCUMENT = addFileArgument("-O", R.string.command_name_output_document, R.string.command_explanation_output_document, "-");
    // using /proc/self/fd/1 for stdout, see https://stackoverflow.com/a/24598112/1320237
    public static final ArgumentOptionBuilder OUTPUT_FILE = addFileArgument("--output-file", R.string.command_name_output_file, R.string.command_explanation_output_file, "/proc/self/fd/1");
    public static final ArgumentOptionBuilder APPEND_OUTPUT = addFileArgument("--append-output", R.string.command_name_append_output, R.string.command_explanation_append_output, "/proc/self/fd/1");
    public static final ArgumentOptionBuilder DIRECTORY_PREFIX = addDirectoryArgument("--directory-prefix", R.string.command_name_directory_prefix, R.string.command_explanation_directory_prefix);
    public static final ArgumentOptionBuilder TRIES = addIntArgument("--tries", R.string.command_name_tries, R.string.command_explanation_tries);
    public static final ArgumentOptionBuilder DEPTH = addIntArgument("-l", R.string.command_name_depth, R.string.command_explanation_depth);

    private static ArgumentOptionBuilder addIntArgument(String id, int name, int text) {
        return addArgument(id, name, text, ArgumentOptionBuilder.INTEGER);
    }
    private static ArgumentOptionBuilder addFileArgument(String id, int name, int text, String file) {
        return addArgument(id, name, text, ArgumentOptionBuilder.FILE(file));
    }

    private static ArgumentOptionBuilder addDirectoryArgument(String id, int name, int text) {
        return addArgument(id, name, text, ArgumentOptionBuilder.DIRECTORY);
    }

    private static ArgumentOptionBuilder addArgument(String id, int name, int text, ArgumentOptionBuilder.DisplayStrategy displayStrategy) {
        ArgumentOptionBuilder result = new ArgumentOptionBuilder(id, name, text, displayStrategy);
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

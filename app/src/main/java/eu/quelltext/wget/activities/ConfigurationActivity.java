package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.wget.Command;
import eu.quelltext.wget.bin.wget.options.display.Display;
import eu.quelltext.wget.bin.wget.options.display.DisplayableOption;
import eu.quelltext.wget.bin.wget.options.Option;
import eu.quelltext.wget.bin.wget.options.Options;
import eu.quelltext.wget.state.CommandDB;
import eu.quelltext.wget.ui.AutoSuggestAdapter;

public class ConfigurationActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";
    public static final String RESULT_COMMAND = "command";
    private static final int ACTIVITY_CHOOSE_FILE = 0;
    private static final int ACTIVITY_CHOOSE_DIRECTORY = 1;

    // white space matching in regex
    // from https://stackoverflow.com/a/4731164/1320237
    private static final String WHITESPACE_CHARS =  ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL)
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD
            + "\\u2001" // EM QUAD
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;
    /* A \S that actually works for  Java’s native character set: Unicode */
    private static final String URL_CLASS = "[^" + WHITESPACE_CHARS + "\"']";
    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp)://" + URL_CLASS + "+");;

    private LinearLayout sectionsView;
    private Command command;
    private List<OptionValue> optionValues;
    private Set<Option> skipOptionsOnSave = new HashSet<>();
    private List<String> urls = new ArrayList<>();
    private LinearLayout urlsView;
    private List<UrlView> urlViews = new ArrayList<>();
    private Section.OptionBuilder reportPathTo = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        setTitle(R.string.activity_title_configuration);

        // get parcelable from intent https://stackoverflow.com/a/7181792/1320237
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        // create the command
        command = extras.getParcelable(ARG_COMMAND);
        if (command == null) {
            // activity is opened by an intent
            // see https://stackoverflow.com/a/9637366/1320237
            command = Command.createDefaultCommand();
        }

        // add urls to command
        Set<String> urls = new HashSet<>();
        // add data url
        Uri data = intent.getData();
        if (data != null) {
            String url = data.toString();
            urls.add(url);
        }
        // add extra text urls
        List<String> texts = new ArrayList<>();
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (extraText != null) {
            texts.add(extraText);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            // add clip urls
            ClipData clip = intent.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i ++) {
                    ClipData.Item item = clip.getItemAt(i);
                    CharSequence text = item.getText();
                    Uri uri = item.getUri();
                    if (text != null) {
                        texts.add(text.toString());
                    }
                    if (uri != null) {
                        try {
                            // load content from uri
                            InputStream in = getContentResolver().openInputStream(uri);
                            byte[] bytes = IOUtils.toByteArray(in);
                            texts.add(new String(bytes));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        // filter text for urls
        for (String text: texts) {
            Matcher m = URL_PATTERN.matcher(text);
            while(m.find()) {
                String url = m.group();
                if (!urls.contains(url)) {
                    urls.add(url);
                }
            }
        }
        // add urls to command
        for (String url: urls) {
            command.addUrl(url);
        }

        sectionsView = findViewById(R.id.sections);

        optionValues = new ArrayList<>();

        Section easy = new Section(R.string.section_title_favorite);
        easy.even();
        easy.add(Options.DIRECTORY_PREFIX);
        easy.add(Options.CONTINUE);
        easy.add(Options.MIRROR);

        Section startup = new Section(R.string.section_title_startup);
        startup.odd();
        startup.add(Options.VERSION);
        startup.add(Options.HELP);

        Section logging = new Section(R.string.section_title_logging);
        logging.even();
        logging.add(Options.OUTPUT_FILE);
        logging.add(Options.APPEND_OUTPUT);

        Section download = new Section(R.string.section_title_download);
        download.odd();
        download.add(Options.TRIES);
        download.add(Options.OUTPUT_DOCUMENT);
        //download.add(Options.CONTINUE); // in favorites

        Section recursive = new Section(R.string.section_title_recursive);
        recursive.even();
        recursive.add(Options.RECURSIVE);
        recursive.add(Options.DEPTH);
        //recursive.add(Options.MIRROR); // in favorites

        //Section directory = new Section(R.string.section_title_directory);
        //directory.odd();
        //directory.add(Options.DIRECTORY_PREFIX); // in favorites

        Section proxy = new Section(R.string.section_title_proxy);
        proxy.odd();
        proxy.add(Options.HTTP_PROXY);
        proxy.add(Options.HTTPS_PROXY);
        proxy.add(Options.FTP_PROXY);
        proxy.add(Options.NO_PROXY);


        Button buttonRun = findViewById(R.id.run);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Command command = saveCommand();
                runCommand(command);
            }
        });
        Button buttonSave = findViewById(R.id.save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCommand();
                finish();
            }
        });
        Button buttonCancel = findViewById(R.id.cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        urlsView = findViewById(R.id.urls);
        for (String url: command.getUrls()) {
            new UrlView(url).add();
        }
        if (command.getUrls().size() == 0) {
            new UrlView("").add();
        }
    }

    private Command saveCommand() {
        Command command = new Command();
        for (Option option: this.command.getOptions()) {
            if (!skipOptionsOnSave.contains(option)) {
                command.addOption(option);
            }
        }
        for (OptionValue value : optionValues) {
            value.save(command);
        }
        for (String url : urls) {
            command.addUrl(url);
        }
        for (UrlView urlView : urlViews) {
            if (urlView.isValid()) {
                command.addUrl(urlView.getUrl());
            }
        }
        // save the command for other views
        CommandDB commands = CommandDB.of(this);
        commands.load();
        commands.add(command);
        commands.save();
        // return command to calling activity
        // see https://stackoverflow.com/a/947560/1320237
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_COMMAND, command);
        setResult(Activity.RESULT_OK, resultIntent);
        return command;
    }

    private void runCommand(Command command) {
        // open a new activity, see https://stackoverflow.com/a/4186097/1320237
        Intent myIntent = new Intent(this, CommandActivity.class);
        myIntent.putExtra(CommandActivity.ARG_COMMAND, command); //Optional parameters
        startActivity(myIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUrlsForAutoCompletion();
    }

    private void setUrlsForAutoCompletion() {
        CommandDB commands = CommandDB.of(this);
        commands.load();
        List<String> possibleUrls = commands.getUrls();
        possibleUrls.addAll(command.getUrls());
        // set the autocomplete adapter
        // see https://developer.android.com/reference/android/widget/AutoCompleteTextView
        ArrayAdapter<String> urlsAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line, possibleUrls);
        for (UrlView urlView : urlViews) {
            urlView.setAutocompletAdapter(urlsAdapter);
        }
    }

    private class Section {

        private final View root;
        private final LinearLayout optionsView;
        private List<DisplayableOption> options = new ArrayList<>();
        private int optionsActivated = 0;

        public Section(int title) {
            // dynamically inflate view
            // https://stackoverflow.com/a/6070631/1320237
            // not combining the views (false as second argument)
            // see https://www.bignerdranch.com/blog/understanding-androids-layoutinflater-inflate/
            root = LayoutInflater.from(ConfigurationActivity.this)
                    .inflate(R.layout.section, sectionsView, false);
            TextView titleText = root.findViewById(R.id.title);
            titleText.setText(title);

            optionsView = root.findViewById(R.id.options);
            optionsView.setVisibility(View.GONE);
            updateEditImage();

            View header = root.findViewById(R.id.header);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    optionsView.setVisibility(optionsView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    createOptions();
                    updateEditImage();
                }
            });
            // how to add a view
            // see https://stackoverflow.com/a/4203731/1320237
            sectionsView.addView(root);
        }

        private void createOptions() {
            for (DisplayableOption option: options) {
                display(option);
            }
            options = new ArrayList<>();
        }

        private void updateEditImage() {
            // set a drawable from another package
            // see https://stackoverflow.com/a/7815835/1320237
            Drawable d = getResources().getDrawable(
                    optionsView.getVisibility() == View.GONE ?
                            android.R.drawable.ic_menu_edit :
                            android.R.drawable.ic_menu_close_clear_cancel);
            ImageView editImage = root.findViewById(R.id.edit_image);
            editImage.setImageDrawable(d);
            editImage.setBackgroundColor(optionsActivated > 0 ?
                    getResources().getColor(R.color.colorPrimary) : Color.TRANSPARENT);
        }

        public void add(DisplayableOption option) {
            options.add(option);
            Option optionFromCommand = getOptionFromCommand(option);
            if (optionFromCommand != null) {
                notifyOptionWasActivated();
            }
        }

        private void display(final DisplayableOption option) {
            final OptionBuilder builder = new OptionBuilder();
            option.displayIn(builder);
            Option optionFromCommand = getOptionFromCommand(option);
            if (optionFromCommand != null) {
                notifyOptionWasDeactivated();
                option.fillWith(builder, optionFromCommand);
                skipOptionsOnSave.add(optionFromCommand);
            }
            builder.done();
            optionValues.add(new OptionValue(){
                @Override
                public void save(Command command) {
                    if (builder.isSwitchedOn()) {
                        Option savedOption = option.createNewFrom(builder);
                        command.addOption(savedOption);
                    }
                }
            });
        }

        private Option getOptionFromCommand(DisplayableOption option) {
            return command.getOptionWithId(option.manualId());
        }

        private void notifyOptionWasActivated() {
            optionsActivated++;
            updateEditImage();
        }

        private void notifyOptionWasDeactivated() {
            optionsActivated--;
            updateEditImage();
        }

        public void odd() {
            root.setBackgroundColor(getResources().getColor(R.color.background_odd));
        }
        public void even() {
            root.setBackgroundColor(getResources().getColor(R.color.background_even));
        }

        class OptionBuilder implements Display {

            private final View optionView;
            private Switch toggle;
            private Set<Integer> hideViewsWithIds = new HashSet<>();
            private EditText textInput;
            private EditText fileInput;

            private OptionBuilder() {
                // dynamically inflate view
                // https://stackoverflow.com/a/6070631/1320237
                optionView = LayoutInflater.from(ConfigurationActivity.this)
                        .inflate(R.layout.option, optionsView, false);

                hideViewsWithIds.add(R.id.title);
                hideViewsWithIds.add(R.id.toggle);
                hideViewsWithIds.add(R.id.text);
                hideViewsWithIds.add(R.id.file);
                hideViewsWithIds.add(R.id.openFile);
                hideViewsWithIds.add(R.id.default_path);
                hideViewsWithIds.add(R.id.explanation);
                hideViewsWithIds.add(R.id.default_text);

            }

            public <T extends View> T showView(int id) {
                hideViewsWithIds.remove(id);
                return optionView.findViewById(id);
            }

            public void setText(int viewId, int textId) {
                TextView text = showView(viewId);
                text.setText(textId);
            }

            @Override
            public void invalid() {
                setText(R.id.explanation, R.string.invalid_option);
            }

            @Override
            public void addTitle(int nameId) {
                setText(R.id.title, nameId);
            }

            @Override
            public void addExplanation(int explanationId) {
                setText(R.id.explanation, explanationId);
            }

            @Override
            public void addSwitch() {
                toggle = showView(R.id.toggle);
                optionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggle.toggle();

                    }
                });
                // set change listener to update section
                // see https://stackoverflow.com/a/11278528/1320237
                toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked) {
                            notifyOptionWasActivated();
                        } else {
                            notifyOptionWasDeactivated();
                        }
                    }
                });
            }

            @Override
            public void addNumberField(String defaultNumber) {
                showTextInputWithType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, defaultNumber);
            }

            private void showTextInputWithType(int inputType, final String defaultText) {
                textInput = showView(R.id.text);
                // set number type
                // see https://stackoverflow.com/a/21603219/1320237
                textInput.setInputType(inputType);
                AppCompatImageButton backToDefaultButton = showView(R.id.default_text);
                backToDefaultButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        textInput.setText(defaultText);
                    }
                });
                textInput.setText(defaultText);
            }

            @Override
            public void addFileDialog(final String path) {
                fileInput = showView(R.id.file);
                ImageButton openFile = showView(R.id.openFile);
                View.OnClickListener click = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // from https://stackoverflow.com/q/41193219/1320237
                        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseFile.setType("*/*");// https://stackoverflow.com/a/41195531/1320237
                        String title = getResources().getString(R.string.choose_a_file);
                        Intent intent = Intent.createChooser(chooseFile, title);
                        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                        reportPathTo = OptionBuilder.this;
                    }
                };
                openFile.setOnClickListener(click);
                ImageButton defaultPath = showView(R.id.default_path);
                defaultPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setPath(path);
                    }
                });
                setPath(path);
            }

            @Override
            public void addDirectoryDialog(final String directory) {
                fileInput = showView(R.id.file);
                ImageButton openFile = showView(R.id.openFile);
                View.OnClickListener click = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // from https://github.com/mvbasov/lWS/blob/d945b4b5276ef8d0dba8072bfab6657464d8f0a3/app/src/main/java/net/basov/lws/PreferencesActivity.java#L85
                        Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
                        String title = getResources().getString(R.string.choose_a_directory);
                        intent.putExtra("org.openintents.extra.BUTTON_TEXT", title);
                        try {
                            startActivityForResult(intent, ACTIVITY_CHOOSE_DIRECTORY);
                            reportPathTo = OptionBuilder.this;
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ConfigurationActivity.this,
                                    R.string.invite_to_install_io_file_manager,
                                    Toast.LENGTH_LONG
                            ).show();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("market://details?id=org.openintents.filemanager"));
                            startActivity(i);
                        }
                    }
                };
                openFile.setOnClickListener(click);
                ImageButton defaultPath = showView(R.id.default_path);
                defaultPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // from https://stackoverflow.com/a/7908446/1320237
                        setPath(directory);
                    }
                });
                fileInput.setText(directory);
            }

            @Override
            public String getUrl() {
                return getText();
            }

            @Override
            public void setUrl(String url) {
                setText(url);
            }

            @Override
            public void addUrlField(String defaultUrl) {
                // https://developer.android.com/reference/android/text/InputType.html#TYPE_TEXT_VARIATION_URI
                showTextInputWithType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, defaultUrl);
            }

            @Override
            public void addTextField(String defaultText) {
                // https://developer.android.com/reference/android/text/InputType.html#TYPE_CLASS_TEXT
                showTextInputWithType(InputType.TYPE_CLASS_TEXT, defaultText);
            }

            @Override
            public void setText(String text) {
                textInput.setText(text);
            }

            @Override
            public String getText() {
                return textInput.getText().toString();
            }

            @Override
            public void switchOn() {
                toggle.setChecked(true);
            }

            @Override
            public void setNumber(String argument) {
                setText(argument);
            }

            @Override
            public void setPath(String argument) {
                fileInput.setText(argument);
            }

            @Override
            public String getNumber() {
                return getText();
            }

            @Override
            public String getPath() {
                return fileInput.getText().toString();
            }

            public void done() {
                for (int id: hideViewsWithIds) {
                    // remove a child from the parent view
                    // see https://stackoverflow.com/a/6538694/1320237
                    View hiddenView = optionView.findViewById(id);
                    ViewGroup parent = (ViewGroup) hiddenView.getParent();
                    parent.removeView(hiddenView);
                }
                // how to add a view
                // see https://stackoverflow.com/a/4203731/1320237
                optionsView.addView(optionView);
            }

            public boolean isSwitchedOn() {
                return toggle != null && toggle.isChecked();
            }
        }
    }
    interface OptionValue {
        void save(Command command);
    }

    private class UrlView {
        private final View root;
        private final AutoCompleteTextView text;

        private UrlView(String url) {
            // dynamically inflate view
            // https://stackoverflow.com/a/6070631/1320237
            root = LayoutInflater.from(ConfigurationActivity.this)
                    .inflate(R.layout.section_url_entry, urlsView, false);
            text = root.findViewById(R.id.url);
            text.setText(url);
            text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        text.setText(getUrl());
                    }
                }
            });
            ImageButton deleteButton = root.findViewById(R.id.delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    remove();
                }
            });
            ImageButton addButton = root.findViewById(R.id.add);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new UrlView("").add(urlViews.indexOf(UrlView.this) + 1);
                }
            });
        }

        public String getUrl() {
            String url = text.getText().toString();
            if (!url.contains("://")) {
                url = "http://" + url;
            }
            return url;
        }

        public boolean isValid() {
            if (getUrl().isEmpty()) {
                return false;
            }
            try {
                URL url = new URL(getUrl());
                return !url.getHost().isEmpty();
            } catch (MalformedURLException e) {
                return false;
            }
        }

        public void add() {
            urlsView.addView(root);
            urlViews.add(this);
        }

        public void remove() {
            if (urlViews.size() == 1) {
                text.setText("");
            } else {
                urlsView.removeView(root);
                urlViews.remove(this);
            }
        }

        private void add(int index) {
            urlsView.addView(root, index);
            urlViews.add(index,this);
            text.requestFocus();
        }

        public void setAutocompletAdapter(ArrayAdapter<String> urlsAdapter) {
            text.setAdapter(urlsAdapter);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if((requestCode == ACTIVITY_CHOOSE_DIRECTORY || requestCode == ACTIVITY_CHOOSE_FILE) && reportPathTo != null)
        {
            Uri uri = data.getData();
            String path = uri.getPath();
            int index = path.lastIndexOf(":");
            path = path.substring(index + 1);
            reportPathTo.setPath(path);
        }
    }
}

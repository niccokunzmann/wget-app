package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.wget.Command;
import eu.quelltext.wget.bin.wget.DisplayableOption;
import eu.quelltext.wget.bin.wget.Option;
import eu.quelltext.wget.bin.wget.Options;

public class ConfigurationActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";
    public static final String RESULT_COMMAND = "command";
    private static final int ACTIVITY_CHOOSE_FILE = 0;
    private static final int ACTIVITY_CHOOSE_DIRECTORY = 1;

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

        // get parcelable from intent https://stackoverflow.com/a/7181792/1320237
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        command = extras.getParcelable(ARG_COMMAND);
        if (command == null) {
            // activity is opened by an intent
            // see https://stackoverflow.com/a/9637366/1320237
            command = new Command();
            Uri data = getIntent().getData();
            if (data != null) {
                String url = data.toString();
                command.addUrl(url);
            }
        }
        sectionsView = findViewById(R.id.sections);

        optionValues = new ArrayList<>();
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
        download.add(Options.CONTINUE);

        Section recursive = new Section(R.string.section_title_recursive);
        recursive.even();
        recursive.add(Options.RECURSIVE);
        recursive.add(Options.DEPTH);
        recursive.add(Options.MIRROR);

        Section directory = new Section(R.string.section_title_directory);
        directory.odd();
        directory.add(Options.DIRECTORY_PREFIX);

        Button buttonRun = findViewById(R.id.run);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Command command = saveCommandAndExit();
                runCommand(command);
            }
        });
        Button buttonSave = findViewById(R.id.save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCommandAndExit();
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

    private Command saveCommandAndExit() {
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
        // return command to calling activity
        // see https://stackoverflow.com/a/947560/1320237
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_COMMAND, command);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        return command;
    }

    private void runCommand(Command command) {
        // open a new activity, see https://stackoverflow.com/a/4186097/1320237
        Intent myIntent = new Intent(this, CommandActivity.class);
        myIntent.putExtra(CommandActivity.ARG_COMMAND, command); //Optional parameters
        startActivity(myIntent);
    }

    private class Section {

        private final View root;
        private final LinearLayout optionsView;
        private List<DisplayableOption> options = new ArrayList<>();

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
        }

        public void add(DisplayableOption option) {
            options.add(option);
        }

        private void display(final DisplayableOption option) {
            final OptionBuilder builder = new OptionBuilder();
            option.displayIn(builder);
            Option prefilledOption = command.getOptionWithId(option.manualId());
            if (prefilledOption != null) {
                option.fillWith(builder, prefilledOption);
                skipOptionsOnSave.add(prefilledOption);
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

        public void odd() {
            root.setBackgroundColor(getResources().getColor(R.color.background_odd));
        }
        public void even() {
            root.setBackgroundColor(getResources().getColor(R.color.background_even));
        }

        class OptionBuilder implements DisplayableOption.Display {

            private final View optionView;
            private Switch toggle;
            private Set<Integer> hideViewsWithIds = new HashSet<>();
            private EditText numberView;
            private EditText fileView;
            private boolean fileDialog = true;

            private OptionBuilder() {
                // dynamically inflate view
                // https://stackoverflow.com/a/6070631/1320237
                optionView = LayoutInflater.from(ConfigurationActivity.this)
                        .inflate(R.layout.option, optionsView, false);

                hideViewsWithIds.add(R.id.title);
                hideViewsWithIds.add(R.id.toggle);
                hideViewsWithIds.add(R.id.number);
                hideViewsWithIds.add(R.id.file);
                hideViewsWithIds.add(R.id.openFile);
                hideViewsWithIds.add(R.id.default_path);
                hideViewsWithIds.add(R.id.explanation);
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
            }

            @Override
            public void addNumber() {
                numberView = showView(R.id.number);
            }

            @Override
            public void addFileDialog(final String path) {
                fileView = showView(R.id.file);
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
            public void addDirectoryDialog() {
                fileView = showView(R.id.file);
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
                final String downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                defaultPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // from https://stackoverflow.com/a/7908446/1320237
                        setPath(downloadDirectory);
                    }
                });
                fileView.setText(downloadDirectory);
            }

            @Override
            public void switchOn() {
                toggle.setChecked(true);
            }

            @Override
            public void setNumber(String argument) {
                numberView.setText(argument);
            }

            @Override
            public void setPath(String argument) {
                fileView.setText(argument);
            }

            @Override
            public String getNumber() {
                return numberView.getText().toString();
            }

            @Override
            public String getPath() {
                return fileView.getText().toString();
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
        private final EditText text;

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

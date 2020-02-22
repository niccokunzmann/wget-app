package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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
    private LinearLayout sectionsView;
    private Command command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // get parcelable from intent https://stackoverflow.com/a/7181792/1320237
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        command = extras.getParcelable(ARG_COMMAND);


        sectionsView = findViewById(R.id.sections);

        Section startup = new Section(R.string.section_title_startup);
        startup.odd();
        startup.add(Options.VERSION);
        startup.add(Options.HELP);

        Section download = new Section(R.string.section_title_download);
        download.even();
        download.add(Options.TRIES);
        download.add(Options.OUTPUT);
        download.add(Options.CONTINUE);

        Section recursive = new Section(R.string.section_title_recursive);
        recursive.odd();
        recursive.add(Options.RECURSIVE);
        recursive.add(Options.DEPTH);
        recursive.add(Options.MIRROR);

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

        private void display(DisplayableOption option) {
            OptionBuilder builder = new OptionBuilder();
            option.displayIn(builder);
            Option prefilledOption = command.getOptionWithId(option.manualId());
            if (prefilledOption != null) {
                option.fillWith(builder, prefilledOption);
            }
            builder.done();
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
            private TextView fileView;

            private OptionBuilder() {
                // dynamically inflate view
                // https://stackoverflow.com/a/6070631/1320237
                optionView = LayoutInflater.from(ConfigurationActivity.this)
                        .inflate(R.layout.option, optionsView, false);

                hideViewsWithIds.add(R.id.title);
                hideViewsWithIds.add(R.id.toggle);
                hideViewsWithIds.add(R.id.number);
                hideViewsWithIds.add(R.id.file);
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
            public void addFileDialog() {
                fileView = showView(R.id.file);
            }

            @Override
            public void switchOn() {
                toggle.setChecked(true);
            }

            @Override
            public void setNumber(int i) {
                numberView.setText(Integer.toString(i));
            }

            @Override
            public void setFile(String argument) {
                fileView.setText(argument);
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
        }
    }
}

package eu.quelltext.wget.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.Executable;
import eu.quelltext.wget.bin.wget.Command;

public class CommandActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";
    private static final long UPDATE_GUI_MILLIS = 100;
    private Handler handler;
    private TextView errorCode;
    private TextView commandText;
    private TextView output;
    private Executable.Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        output = findViewById(R.id.text_output);

        // get parcelable from intent https://stackoverflow.com/a/7181792/1320237
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Command command = extras.getParcelable(ARG_COMMAND);

        errorCode = findViewById(R.id.text_result_description);
        commandText = findViewById(R.id.command);
        commandText.setText(command.asCommandLineText());

        try {
            result = command.run(this);
        } catch (IOException e) {
            e.printStackTrace();
            result = new IOErrorResult();
        }

        // use a handler to update the gui
        // see http://www.mopri.de/2010/timertask-bad-do-it-the-android-way-use-a-handler/
        handler = new Handler();
        handler.postDelayed(new RunGuiUpdate(), UPDATE_GUI_MILLIS);
    }

    class RunGuiUpdate implements Runnable {
        @Override
        public void run() {
            try {
                output.setText(result.getOutput());
            } catch (IOException e) {
                e.printStackTrace();
                output.setText(R.string.error_io_exception_output);
            }
            errorCode.setText(result.getReturnCodeStringId());
            if (result.isRunning()) {
                handler.postDelayed(this, UPDATE_GUI_MILLIS);
            }
        }
    };

    private class IOErrorResult implements Executable.Result {
        @Override
        public void waitFor() throws InterruptedException {
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public String getOutput() throws IOException {
            return "";
        }

        @Override
        public int getReturnCodeStringId() {
            return R.string.error_io_exception_start;
        }
    }
}

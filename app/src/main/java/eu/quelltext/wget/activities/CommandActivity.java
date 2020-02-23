package eu.quelltext.wget.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.Executable;
import eu.quelltext.wget.bin.wget.Command;

public class CommandActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";
    private static final long UPDATE_GUI_MILLIS = 100;
    private static final int PERMISSION_REQUEST = 1;
    private static final int PERMISSION_ASK = 2;
    private int permissionRequestsToReceive;
    private Handler handler;
    private TextView errorCode;
    private TextView commandText;
    private TextView output;
    private Executable.Result result;
    private Command command;

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
        command = extras.getParcelable(ARG_COMMAND);

        errorCode = findViewById(R.id.text_result_description);
        commandText = findViewById(R.id.command);
        commandText.setText(command.asCommandLineText());

        ///////////////////////// sort permissions /////////////////////////
        final List<String> askForPermissions = new ArrayList<>();
        List<String> requestPermissions = new ArrayList<>();

        for (final String permission : command.getPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    askForPermissions.add(permission);
                } else {
                    // No explanation needed; request the permission
                    // PERMISSION_REQUEST is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                    requestPermissions.add(permission);
                }

            }
        }

        ///////////////////////// request permissions /////////////////////////
        permissionRequestsToReceive = 0;
        if (askForPermissions.size() > 0) {
            // show a dialog, see https://stackoverflow.com/a/5810118/1320237
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.explain_permission)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // convert list to array, see https://javadevnotes.com/java-list-to-array-examples/
                            ActivityCompat.requestPermissions(CommandActivity.this,
                                    askForPermissions.toArray(new String[]{}), PERMISSION_REQUEST);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            permissionRequestsToReceive += PERMISSION_REQUEST;
        }
        if (requestPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[]{}), PERMISSION_ASK);
            permissionRequestsToReceive += PERMISSION_ASK;
        }

        ///////////////////////// run command /////////////////////////
        if (permissionRequestsToReceive == 0) {
            runCommand();
        } else {
            result = new SpecialResult(R.string.waiting_for_permissions,true);
        }


        // use a handler to update the gui
        // see http://www.mopri.de/2010/timertask-bad-do-it-the-android-way-use-a-handler/
        handler = new Handler();
        RunGuiUpdate updatePoll = new RunGuiUpdate();
        updatePoll.run();
    }

    private void runCommand() {
        try {
            result = command.run(this);
        } catch (IOException e) {
            e.printStackTrace();
            result = new SpecialResult(R.string.error_io_exception_start, false);
        }
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

    private class SpecialResult implements Executable.Result {
        private final int errorCodeId;
        private final boolean running;

        SpecialResult(int errorCodeId, boolean running){
            this.errorCodeId = errorCodeId;
            this.running = running;

        }

        @Override
        public void waitFor() throws InterruptedException {
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public String getOutput() throws IOException {
            return "";
        }

        @Override
        public int getReturnCodeStringId() {
            return errorCodeId;
        }

        @Override
        public void kill() {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionRequestsToReceive -= requestCode;
        if (permissionRequestsToReceive == 0) {
            runCommand();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        result.kill();
    }
}

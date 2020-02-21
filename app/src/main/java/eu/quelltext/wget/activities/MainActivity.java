package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.BinaryAccess;
import eu.quelltext.wget.bin.IWget;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IWget wget = new BinaryAccess(this).wget();
        String version = wget.version();
        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText(version);
    }
}

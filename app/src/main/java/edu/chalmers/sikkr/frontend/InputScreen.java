package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.chalmers.sikkr.R;

/**
 * Created by Jingis on 2014-10-22.
 */
public class InputScreen extends Activity {
    private String savedNbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.inputscreen_layout);
        numberExist();
    }

    private void numberExist() {
        File f = new File(getFilesDir(), "number");
        if(f.exists()) {
            Log.i("Input", "File exist");
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }

    public void enterNbr(View view) {
        EditText tv = ((EditText)findViewById(R.id.editText));
        savedNbr = tv.getText().toString();
        final File parent = getFilesDir();
        final File file = new File(parent, "number");

        try {
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create parent folder");
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Could not create file");
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(savedNbr);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, "Exception when creating file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        numberExist();
    }

}

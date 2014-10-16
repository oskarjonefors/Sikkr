package edu.chalmers.sikkr.frontend;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.speech.RecognizerIntent;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.Window;
        import android.widget.GridView;

        import java.util.ArrayList;

        import edu.chalmers.sikkr.R;
        import edu.chalmers.sikkr.backend.util.SpeechRecognitionHelper;
        import edu.chalmers.sikkr.backend.util.SystemData;


public class ContactBookActivity extends Activity {

    private ArrayList<String> matches;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_book);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ButtonAdapter(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                Intent intent = new Intent(this, ContactGridActivity.class);
                intent.putExtra("initial_letter", matches.get(0).charAt(0));
                startActivity(intent);

            }
        }
    }

    public void voiceSearch(View view) {
        SpeechRecognitionHelper.run(this);
    }


}

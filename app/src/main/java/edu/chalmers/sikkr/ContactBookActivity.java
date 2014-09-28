package edu.chalmers.sikkr;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.GridView;
        import android.widget.ImageView;
        import android.widget.ListAdapter;
        import android.widget.Toast;

        import java.util.ArrayList;
        import java.util.Set;

        import edu.chalmers.sikkr.backend.ContactBook;


public class ContactBookActivity extends Activity {
    private ContactBook cb;
    private ArrayList<Character> al;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }






}

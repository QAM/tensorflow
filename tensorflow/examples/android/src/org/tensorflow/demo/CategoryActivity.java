package org.tensorflow.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.app.Activity;
import org.tensorflow.demo.Adapter.CategoryAdapter;
import org.tensorflow.demo.Model.PictureDBHelper;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import org.tensorflow.demo.Model.ModelPicture.PictureEntry;

import java.util.ArrayList;

/**
 * Created by liach on 10/14/2016.
 */

public class CategoryActivity extends Activity {
    private PictureDBHelper picDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        GridView gridview = (GridView) findViewById(R.id.category_view);

        picDBHelper = new PictureDBHelper(this);
        picDBHelper.openDB();


        final ArrayAdapter<String> adapter = new CategoryAdapter(
                                                    this, 
                                                    (ArrayList) picDBHelper.getCategoriesList());

        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent=new Intent(CategoryActivity.this,PictureActivity.class);
                intent.putExtra("category",adapter.getItem(position));
                startActivity(intent);
            }
        });
    }

}

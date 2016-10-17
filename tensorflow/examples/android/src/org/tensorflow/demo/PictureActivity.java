package org.tensorflow.demo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.demo.Adapter.ImageAdapter;
import org.tensorflow.demo.Model.FileOperation;
import org.tensorflow.demo.Model.PictureDBHelper;

import java.util.ArrayList;

/**
 * Created by liach on 10/14/2016.
 */

public class PictureActivity extends Activity {
    private PictureDBHelper picDBHelper;
    private ArrayAdapter<String> adapter;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        GridView gridview = (GridView) findViewById(R.id.picture_view);

        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        picDBHelper = new PictureDBHelper(this);
        picDBHelper.openDB();

        adapter = new ImageAdapter(this, (ArrayList)picDBHelper.getPathsList(category));
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                final Dialog dialog = new Dialog(PictureActivity.this);
                dialog.setContentView(R.layout.dialog_picture);
                dialog.setTitle(category);

                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                image.setImageBitmap(FileOperation.loadImageFromStorage(adapter.getItem(position), 500));

                dialog.show();
            }
        });
    }
}

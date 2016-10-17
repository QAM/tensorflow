package org.tensorflow.demo.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.demo.Model.FileOperation;

import java.util.ArrayList;
import org.tensorflow.demo.R;

/**
 * Created by liach on 10/15/2016.
 */

public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;

    public ImageAdapter(Context context, ArrayList<String> paths){
        super(context, 0, paths);
        mContext=context;
    }

    public int getCount() {
        return super.getCount();
    }

    public String getItem(int position) {
        return super.getItem(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter


    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) this.getContext())
                    .getLayoutInflater();
            convertView = inflater.inflate(R.layout.picture_info, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageBitmap(
            FileOperation.loadImageFromStorage(getItem(position), 
            100));
        
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
    }
}

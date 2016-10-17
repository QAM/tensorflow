package org.tensorflow.demo.Model;

import android.provider.BaseColumns;

/**
 * Created by liach on 10/14/2016.
 */

public final class ModelPicture {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ModelPicture() {}

    /* Inner class that defines the table contents */
    public static class PictureEntry implements BaseColumns {
        public static final String TABLE_NAME = "pictures";
        public static final String COLUMN_NAME_PIC_NAME = "name";
        public static final String COLUMN_NAME_PIC_CATEGORY = "category";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_CREATED_TIME = "created_time";
    }

}

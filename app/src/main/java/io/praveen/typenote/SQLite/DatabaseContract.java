package io.praveen.typenote.SQLite;

import android.provider.BaseColumns;

class DatabaseContract {

    static final class DatabaseEntry implements BaseColumns{
        static final String TABLE_NOTES = "notes";
        static final String KEY_ID = "id";
        static final String KEY_NOTE = "note";
        static final String KEY_DATE = "date";
        static final String KEY_STAR = "star";
    }

}

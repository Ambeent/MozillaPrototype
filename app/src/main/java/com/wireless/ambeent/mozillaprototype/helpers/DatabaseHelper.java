package com.wireless.ambeent.mozillaprototype.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wireless.ambeent.mozillaprototype.R;
import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;

import java.util.ArrayList;

/**
 * Created by Atakan on 13.07.2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper dbInstance;

    // Database Version. Increment this manually when database schema changes.
    private static final int DATABASE_VERSION = 8;

    // Database Name
    private static final String DATABASE_NAME = "MozillaProMessenger";

    // Table Names
    public static final String TABLE_MESSAGES = "messageTable";


    // Messages Table - column names
    public static final String KEY_MESSAGE_ID = "messageId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_MSG_TIMESTAMP = "msg_timestmap";


    //Messages creating query.
    public static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" +
            KEY_MESSAGE_ID + " TEXT, "+
            KEY_MESSAGE + " TEXT, "+
            KEY_SENDER + " TEXT, " +
            KEY_RECEIVER + " TEXT, " +
            KEY_MSG_TIMESTAMP +" REAL);";



    //Making SQLite singleton.
    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (dbInstance == null) {
            dbInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return dbInstance;
    }

    //Private constructor
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);

    }


    //Gets only the last message inserted
    public static MessageObject getLastMessageFromSQLite(Context mContext){

        //Get necessary columns from SQLiite and create MessageObjects
        String table = DatabaseHelper.TABLE_MESSAGES;
        String[] columns = {DatabaseHelper.KEY_MESSAGE_ID,
                DatabaseHelper.KEY_MESSAGE,
                DatabaseHelper.KEY_SENDER,
                DatabaseHelper.KEY_RECEIVER,
                DatabaseHelper.KEY_MSG_TIMESTAMP};

        Cursor cursor = DatabaseHelper.getInstance(mContext).getReadableDatabase()
                .query(table, columns, null, null, null, null, null, null);

        //Populate the messages HashSet
        if(cursor.moveToLast()){
            //Constructing every message and their attributes here.
            String messageId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE_ID));
            String message = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
            String sender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SENDER));
            String receiver = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_RECEIVER));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_MSG_TIMESTAMP));

            MessageObject messageObject = new MessageObject(messageId, message, sender, receiver, timestamp);

            return messageObject;
        }

        //Something is wrong...
        return null;


    }

    //Gets saved messages from SQLite database and populates them
    public static ArrayList<MessageObject> getMessagesFromSQLite(Context mContext){

        ArrayList<MessageObject> messages = new ArrayList<>();

        //Get necessary columns from SQLiite and create MessageObjects
        String table = DatabaseHelper.TABLE_MESSAGES;
        String[] columns = {DatabaseHelper.KEY_MESSAGE_ID,
                DatabaseHelper.KEY_MESSAGE,
                DatabaseHelper.KEY_SENDER,
                DatabaseHelper.KEY_RECEIVER,
                DatabaseHelper.KEY_MSG_TIMESTAMP};

        Cursor cursor = DatabaseHelper.getInstance(mContext).getReadableDatabase()
                .query(table, columns, null, null, null, null, null, null);

        //Populate the messages HashSet
        while(cursor.moveToNext()){

            //Constructing every message and their attributes here.
            String messageId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE_ID));
            String message = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
            String sender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SENDER));
            String receiver = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_RECEIVER));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_MSG_TIMESTAMP));

            MessageObject messageObject = new MessageObject(messageId, message, sender, receiver, timestamp);

            messages.add(messageObject);
        }

        return messages;
    }

    //Gets the messages that have a receiver from SQLite database and populates them
    public static ArrayList<MessageObject> getTargetedMessagesFromSQLite(Context mContext){

        ArrayList<MessageObject> messages = new ArrayList<>();

        //Get necessary columns from SQLiite and create MessageObjects
        String table = DatabaseHelper.TABLE_MESSAGES;
        String[] columns = {DatabaseHelper.KEY_MESSAGE_ID,
                DatabaseHelper.KEY_MESSAGE,
                DatabaseHelper.KEY_SENDER,
                DatabaseHelper.KEY_RECEIVER,
                DatabaseHelper.KEY_MSG_TIMESTAMP};

        //Receiver field is not empty
        String selection = DatabaseHelper.KEY_RECEIVER +"!=?";
        String[] args = {mContext.getResources().getString(R.string.message_with_no_receiver)};

        Cursor cursor = DatabaseHelper.getInstance(mContext).getReadableDatabase()
                .query(table, columns, selection, args, null, null, null, null);

        //Populate the messages HashSet
        while(cursor.moveToNext()){

            //Constructing every message and their attributes here.
            String messageId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE_ID));
            String message = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
            String sender = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SENDER));
            String receiver = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_RECEIVER));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_MSG_TIMESTAMP));

            MessageObject messageObject = new MessageObject(messageId, message, sender, receiver, timestamp);

            messages.add(messageObject);
        }

        return messages;
    }

    //Inserts a message to SQLite database if it is not already in there. Returns true if inserted
    public static boolean insertMessageToSQLite(Context mContext, MessageObject messageObject){

        //Check database to see whether the message is already inserted
        String table = DatabaseHelper.TABLE_MESSAGES;
        String[] columns = {DatabaseHelper.KEY_MESSAGE_ID};
        String selection = DatabaseHelper.KEY_MESSAGE_ID +"=?";
        String[] args = { messageObject.getId()};

        Log.i(TAG, "insertMessageToSQLite: " + args.toString());

        Cursor cursor = DatabaseHelper.getInstance(mContext).getReadableDatabase()
                .query(table, columns, selection, args, null, null, null, null);


        //If this returns true, the message is already in database
        if(cursor.moveToFirst()) {
            Log.i(TAG, "insertMessageToSQLite: Already have that message");
            return false;
        }

        //New message. Insert it to database.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_MESSAGE_ID, messageObject.getId());
        values.put(DatabaseHelper.KEY_MESSAGE, messageObject.getMessage());
        values.put(DatabaseHelper.KEY_SENDER, messageObject.getSender());
        values.put(DatabaseHelper.KEY_RECEIVER, messageObject.getReceiver());

        DatabaseHelper.getInstance(mContext)
                .getWritableDatabase()
                .insert(DatabaseHelper.TABLE_MESSAGES, null, values);
        return true;
    }

    //Called everytime the app is launched. Deletes the messages that are older than 10 minutes
   /*
    public static void databaseMaintenance(Context context){
        long tenMinutesAgo = getCurrentTimeMilis() - (1000 * 60 *//* *10*//*);// 1000 ms = 1 sec, 60 sec = 1 min, total of 10 mins

        String table = TABLE_CHATMESSAGES;
        String comparisonString = KEY_TIMESTAMP + "<= " + tenMinutesAgo;

        int rowsDeleted = getInstance(context)
                .getWritableDatabase().delete(table, comparisonString, null);

        Log.i(TAG, "sqliteMaintanance: rowsDeleted: " + rowsDeleted);
    }*/



}

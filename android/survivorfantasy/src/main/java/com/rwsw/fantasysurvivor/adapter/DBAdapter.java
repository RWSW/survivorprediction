package com.rwsw.fantasysurvivor.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.Util;

import java.io.File;


/**
 * Created by Overlordyorch on 18/03/14.
 */
public class DBAdapter {

    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "gobbler";
    private final static String TB_MESSAGE_NAME = "messages";

    private final static String CR_TABLE = "create table if not exists "
            + TB_MESSAGE_NAME + "(" + Columns.ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Columns.contact_id + " text not null, "
            + Columns.message_id + " text not null, "
            + Columns.message + " text not null, "
            + Columns.message_file + " text not null,"
            + Columns.date + " text, "
            + Columns.trigger_attach + " integer not null,"
            + Columns.status + " integer not null, "
            + Columns.incoming + " integer not null) ";

    private DBHelper dbHelper;

    public DBAdapter(Context context) {
        dbHelper = new DBHelper(context);
    }

    public boolean insertMessage(String contact_uid, String message_uid, String message, String messageJsonFilePath, Integer status, Integer incoming) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(Columns.contact_id, contact_uid);
            values.put(Columns.message_id, message_uid);
            values.put(Columns.message, message);
            values.put(Columns.message_file, messageJsonFilePath);
            values.put(Columns.date, Util.getLocalDate());
            values.put(Columns.status, status);
            values.put(Columns.incoming, incoming);

            long result = db.insertOrThrow(TB_MESSAGE_NAME, null, values);
            db.close();

            return (result != -1);
        }

        return false;
    }

    public boolean removeMessage(String messageID, String contactID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String messagePath = getFilePathOfMessage(messageID, contactID);
        File msg = new File(messagePath);
        msg.delete();

        String where = "message_id=? and contact_id=?";
        String[] args = {messageID, contactID};

        long result = db.delete(TB_MESSAGE_NAME, where, args);

        return (result > 0);
    }

    /**
     * Set a message as read in database
     *
     * @param message_id
     * @param contact_id
     */
    public void setMessageAsRead(String message_id, String contact_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
//        values.put("status", Message.READ_MESSAGE_STATUS);
        String where = "message_id=? and contact_id=?";
        String[] args = {message_id, contact_id};

        db.update(TB_MESSAGE_NAME, values, where, args);

    }

//    public Message latestIncomingMessage(String contact_id) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor cursor = null;
//        Message msg = null;
//
//        if (db != null) {
//            StringBuilder sb = new StringBuilder();
//            sb.append("select * from ");
//            sb.append(TB_MESSAGE_NAME);
//            sb.append(" where contact_id='");
//            sb.append(contact_id);
//            sb.append("' and incoming=");
//            sb.append(Message.INCOMING);
//
//            String query = sb.toString();
//            cursor = db.rawQuery(query, null);
//            cursor.moveToLast();
//        }
//        int columns = cursor.getPosition();
//
//        if (cursor != null && columns != -1) {
//            Contact contact = FantasySurvivor.getContactManagerService().getContactList().get(contact_id);
//            msg = new Message();
//            // ID
//            msg.setId(cursor.getString(cursor.getColumnIndex(Columns.message_id)));
//            // MESSAGE TEXT
//            msg.setMessageText(cursor.getString(cursor.getColumnIndex(Columns.message)));
//            // MESSAGE FILE PATH
//            msg.setJsonFilePath(cursor.getString(cursor.getColumnIndex(Columns.message_file)));
//            // DATE
//            msg.setDeliveredDate(cursor.getString(cursor.getColumnIndex(Columns.date)));
//            // STATUS
//            msg.setStatus(cursor.getInt(cursor.getColumnIndex(Columns.status)));
//            // INCOMING STATE
//            int incomingValue = cursor.getInt(cursor.getColumnIndex(Columns.incoming));
//            // If message is incoming set from username with contact username
//            if (incomingValue == Message.INCOMING) {
//                msg.setIncoming(true);
//                msg.setFromUsername(contact.getEmail());
//            } else {
//                // Otherwise message is outgoing and the username is current logged user
//                msg.setIncoming(false);
//                msg.setFromUsername(FantasySurvivor.getUsername());
//            }
//            msg.setContact(contact);
//        }
//        return msg;
//    }

    public Cursor getAllMessageCursor(String contact_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ");
        sb.append(TB_MESSAGE_NAME);
        sb.append(" where contact_id='");
        sb.append(contact_id);
        sb.append("'");
        sb.append(" ORDER BY ");
        sb.append(Columns._ID);
        sb.append(" ASC");

        String query = sb.toString();
        Cursor cursor;
        cursor = db.rawQuery(query, null);

        return cursor;
    }

//    public Message getMessage(String messageID, String contactID) {
//        Message msg = null;
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        if (db != null) {
//            StringBuilder sb = new StringBuilder();
//            sb.append("select * from ");
//            sb.append(TB_MESSAGE_NAME);
//            sb.append(" where ");
//            sb.append(Columns.contact_id);
//            sb.append("='");
//            sb.append(contactID);
//            sb.append("' and ");
//            sb.append(Columns.message_id);
//            sb.append("='");
//            sb.append(messageID);
//            sb.append("'");
//
//            String query = sb.toString();
//            Cursor cursor;
//            cursor = db.rawQuery(query, null);
//
//            if (cursor != null) {
//                cursor.moveToFirst();
//                Contact contact = FantasySurvivor.getContactManagerService().getContactList().get(contactID);
//                msg = new Message();
//                // ID
//                msg.setId(cursor.getString(cursor.getColumnIndex(Columns.message_id)));
//                // MESSAGE TEXT
//                msg.setMessageText(cursor.getString(cursor.getColumnIndex(Columns.message)));
//                // MESSAGE FILE PATH
//                msg.setJsonFilePath(cursor.getString(cursor.getColumnIndex(Columns.message_file)));
//                // DATE
//                msg.setDeliveredDate(cursor.getString(cursor.getColumnIndex(Columns.date)));
//                // STATUS
//                msg.setStatus(cursor.getInt(cursor.getColumnIndex(Columns.status)));
//                // INCOMING STATE
//                int incomingValue = cursor.getInt(cursor.getColumnIndex(Columns.incoming));
//                // If message is incoming set from username with contact username
//                if (incomingValue == Message.INCOMING) {
//                    msg.setIncoming(true);
//                    msg.setFromUsername(contact.getEmail());
//                } else {
//                    // Otherwise message is outgoing and the username is current logged user
//                    msg.setIncoming(false);
//                    msg.setFromUsername(FantasySurvivor.getUsername());
//                }
//                msg.setContact(contact);
//            }
//        }
//        return msg;
//    }

    public String getFilePathOfMessage(String messageID, String contactID) {
        String filePath = "";
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (db != null) {
            // select message from messages where contact_id='foo' and message_id='bar'
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append(Columns.message_file);
            sb.append(" from ");
            sb.append(TB_MESSAGE_NAME);
            sb.append(" where ");
            sb.append(Columns.contact_id);
            sb.append("='");
            sb.append(contactID);
            sb.append("' and ");
            sb.append(Columns.message_id);
            sb.append("='");
            sb.append(messageID);
            sb.append("'");

            String query = sb.toString();
            Cursor cursor;
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
        }

        return filePath;
    }

    private class Columns implements BaseColumns {
        public final static String ID = "_id";
        public final static String contact_id = "contact_id";
        public final static String message_id = "message_id";
        public final static String message = "message";
        public final static String message_file = "message_file";
        public final static String date = "date";
        public final static String trigger_attach = "trigger_attach";
        public final static String status = "status";
        public final static String incoming = "incoming";
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CR_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TB_MESSAGE_NAME);
            onCreate(db);
        }
    }
}

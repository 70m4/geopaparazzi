/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.geopaparazzi.database;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import eu.hydrologis.geopaparazzi.gpx.GpxItem;
import eu.hydrologis.geopaparazzi.util.Constants;
import eu.hydrologis.geopaparazzi.util.Note;
import eu.hydrologis.geopaparazzi.util.PointF3D;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class DaoNotes {

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LON = "lon";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_ALTIM = "altim";
    private static final String COLUMN_TS = "ts";
    private static final String COLUMN_TEXT = "text";

    public static final String TABLE_NOTES = "notes";

    private static final String TAG = "DAONOTES";

    private static long LASTINSERTEDNOTE_ID = -1;

    private static SimpleDateFormat dateFormatter = Constants.TIME_FORMATTER_SQLITE;

    public static void addNote( double lon, double lat, double altim, Date timestamp, String text ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        sqliteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LON, lon);
            values.put(COLUMN_LAT, lat);
            values.put(COLUMN_ALTIM, altim);
            values.put(COLUMN_TS, dateFormatter.format(timestamp));
            values.put(COLUMN_TEXT, text);
            LASTINSERTEDNOTE_ID = sqliteDatabase.insertOrThrow(TABLE_NOTES, null, values);

            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    public static void deleteNote( long id ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        sqliteDatabase.beginTransaction();
        try {
            // delete note
            String query = "delete from " + TABLE_NOTES + " where " + COLUMN_ID + " = " + id;
            SQLiteStatement sqlUpdate = sqliteDatabase.compileStatement(query);
            sqlUpdate.execute();

            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    public static void deleteLastInsertedNote() throws IOException {
        if (LASTINSERTEDNOTE_ID != -1) {
            deleteNote(LASTINSERTEDNOTE_ID);
        }
    }

    public static void importGpxToNotes( GpxItem gpxItem ) throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        sqliteDatabase.beginTransaction();
        try {
            List<PointF3D> points = gpxItem.read();
            List<String> names = gpxItem.getNames();
            for( int i = 0; i < points.size(); i++ ) {
                Date date = new Date(System.currentTimeMillis());
                String dateStrs = Constants.TIME_FORMATTER_SQLITE.format(date);
                PointF3D point = points.get(i);
                String name = names.get(i);
                ContentValues values = new ContentValues();
                values.put(COLUMN_LON, point.x);
                values.put(COLUMN_LAT, point.y);
                values.put(COLUMN_ALTIM, point.getZ());
                values.put(COLUMN_TS, dateStrs);
                values.put(COLUMN_TEXT, name);
                sqliteDatabase.insertOrThrow(TABLE_NOTES, null, values);
            }
            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    /**
     * Get the collected notes from the database inside a given bound.
     * 
     * @param n
     * @param s
     * @param w
     * @param e
     * @return the list of notes inside the bounds.
     * @throws IOException
     */
    public static List<Note> getNotesInWorldBounds( float n, float s, float w, float e ) throws IOException {

        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        String query = "SELECT lon, lat, altim, text, ts FROM XXX WHERE (lon BETWEEN XXX AND XXX) AND (lat BETWEEN XXX AND XXX)";
        // String[] args = new String[]{TABLE_NOTES, String.valueOf(w), String.valueOf(e),
        // String.valueOf(s), String.valueOf(n)};

        query = query.replaceFirst("XXX", TABLE_NOTES);
        query = query.replaceFirst("XXX", String.valueOf(w));
        query = query.replaceFirst("XXX", String.valueOf(e));
        query = query.replaceFirst("XXX", String.valueOf(s));
        query = query.replaceFirst("XXX", String.valueOf(n));

        Log.i(TAG, "Query: " + query);

        Cursor c = sqliteDatabase.rawQuery(query, null);
        List<Note> notes = new ArrayList<Note>();
        c.moveToFirst();
        while( !c.isAfterLast() ) {
            double lon = c.getDouble(0);
            double lat = c.getDouble(1);
            double altim = c.getDouble(2);
            String text = c.getString(3);
            String date = c.getString(4);
            Note note = new Note(text, date, lon, lat, altim);
            notes.add(note);
            c.moveToNext();
        }
        c.close();
        return notes;
    }

    /**
     * Get the list of notes from the db.
     * 
     * @return list of notes.
     * @throws IOException
     */
    public static List<Note> getNotesList() throws IOException {
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        List<Note> notesList = new ArrayList<Note>();
        String asColumnsToReturn[] = {COLUMN_LON, COLUMN_LAT, COLUMN_ALTIM, COLUMN_TS, COLUMN_TEXT};
        String strSortOrder = "_id ASC";
        Cursor c = sqliteDatabase.query(TABLE_NOTES, asColumnsToReturn, null, null, null, null, strSortOrder);
        c.moveToFirst();
        while( !c.isAfterLast() ) {
            double lon = c.getDouble(0);
            double lat = c.getDouble(1);
            double altim = c.getDouble(2);
            String date = c.getString(3);
            String text = c.getString(4);
            Note note = new Note(text, date, lon, lat, altim);
            notesList.add(note);
            c.moveToNext();
        }
        c.close();
        return notesList;
    }

    public static void createTables() throws IOException {
        StringBuilder sB = new StringBuilder();

        sB = new StringBuilder();
        sB.append("CREATE TABLE ");
        sB.append(TABLE_NOTES);
        sB.append(" (");
        sB.append(COLUMN_ID);
        sB.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sB.append(COLUMN_LON).append(" REAL NOT NULL, ");
        sB.append(COLUMN_LAT).append(" REAL NOT NULL,");
        sB.append(COLUMN_ALTIM).append(" REAL NOT NULL,");
        sB.append(COLUMN_TS).append(" DATE NOT NULL,");
        sB.append(COLUMN_TEXT).append(" TEXT NOT NULL ");
        sB.append(");");
        String CREATE_TABLE_NOTES = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX notes_ts_idx ON ");
        sB.append(TABLE_NOTES);
        sB.append(" ( ");
        sB.append(COLUMN_TS);
        sB.append(" );");
        String CREATE_INDEX_NOTES_TS = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX notes_x_by_y_idx ON ");
        sB.append(TABLE_NOTES);
        sB.append(" ( ");
        sB.append(COLUMN_LON);
        sB.append(", ");
        sB.append(COLUMN_LAT);
        sB.append(" );");
        String CREATE_INDEX_NOTES_X_BY_Y = sB.toString();

        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().getDatabase();
        Log.i(TAG, "Create the notes table.");
        sqliteDatabase.execSQL(CREATE_TABLE_NOTES);
        sqliteDatabase.execSQL(CREATE_INDEX_NOTES_TS);
        sqliteDatabase.execSQL(CREATE_INDEX_NOTES_X_BY_Y);
    }

}
package com.ph03nix_x.capacityinfo.databases

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.ph03nix_x.capacityinfo.R

const val DB_NAME = "History.db"
const val DB_TITLE = "History"
const val ID = "id"
const val DATE = "Date"
const val RESIDUAL_CAPACITY = "Residual_Capacity"

class HistoryDB(var context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $DB_TITLE ($ID INTEGER PRIMARY KEY DEFAULT 1, $DATE TEXT, $RESIDUAL_CAPACITY INTEGER)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun insertData(history: History) {
        try {
            val db = writableDatabase
            val cv = ContentValues()
            cv.put(DATE, history.date)
            cv.put(RESIDUAL_CAPACITY, history.residualCapacity)
            db.insert(DB_TITLE, null, cv)
            db.close()
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
        }
    }

    fun readDB(): MutableList<History> {
        val historyList: MutableList<History> = mutableListOf()
        return try {
            val db = readableDatabase
            val query = "Select * from $DB_TITLE"
            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val history = History()
                    history.apply {
                        id = cursor.getString(cursor.getColumnIndexOrThrow(ID)).toInt()
                        date = cursor.getString(cursor.getColumnIndexOrThrow(DATE))
                        residualCapacity = cursor.getInt(cursor.getColumnIndexOrThrow(RESIDUAL_CAPACITY))
                    }
                    historyList.add(history)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            historyList
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
            historyList
        }
    }

    fun clear() {
        try {
            val db = writableDatabase
            db.apply {
                delete(DB_TITLE, null, null)
                close()
            }
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
        }
    }

    fun removeFirstRow() {
        try {
            val db = writableDatabase
            val cursor = db.query(DB_TITLE, null, null, null, null,
                null, null)

            if (cursor.moveToFirst()) {
                val rowId: String = cursor.getString(cursor.getColumnIndexOrThrow(ID))
                db.delete(DB_TITLE, "$ID=?", arrayOf(rowId))
            }
            cursor.close()
            db.close()
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
        }
    }

    fun getCount(): Long {
        return try {
            val db = readableDatabase
            val count = DatabaseUtils.queryNumEntries(db, DB_TITLE)
            db.close()
            count
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
            -1
        }
    }

    fun remove(residualCapacity: Int) {
        try {
            val id = getId(residualCapacity)
            val db = writableDatabase
            val query = "Select * from $DB_TITLE"
            val result = db.rawQuery(query, null)
            db.delete(DB_TITLE, "$ID =?", arrayOf("$id"))
            result.close()
            db.close()
        }
        catch(_: SQLiteCantOpenDatabaseException) {
            Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
        }
    }

    private fun getId(residualCapacity: Int): Int {
       return try {
            val sqLiteDatabase = readableDatabase
            val cursor = sqLiteDatabase.rawQuery("Select * from $DB_TITLE", null)
            var currentId = -1
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(cursor.getColumnIndexOrThrow(RESIDUAL_CAPACITY)) ==
                        residualCapacity) {
                        currentId = cursor.getInt(cursor.getColumnIndexOrThrow(ID))
                        break
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            sqLiteDatabase.close()
            currentId
        }
       catch(_: SQLiteCantOpenDatabaseException) {
           Toast.makeText(context, R.string.error_opening_db, Toast.LENGTH_LONG).show()
           -1
       }
    }
}
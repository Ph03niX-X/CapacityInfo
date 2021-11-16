package com.ph03nix_x.capacityinfo.databases

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.play.core.assetpacks.db




const val dbName = "History.db"
const val dbTitle = "History"
const val id = "id"
const val date = "Date"
const val residualCapacity = "Residual_Capacity"

class HistoryDB(var context: Context) : SQLiteOpenHelper(context, dbName, null, 1) {
    
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = "CREATE TABLE $dbTitle ($id INTEGER PRIMARY KEY DEFAULT 1, $date TEXT, $residualCapacity INTEGER)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun insertData(history: History) {

        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(date, history.date)
        cv.put(residualCapacity, history.residualCapacity)
        db.insert(dbTitle, null, cv)
        db.close()
    }

    fun readDB(): MutableList<History> {

        val historyList: MutableList<History> = mutableListOf()
        val db = this.readableDatabase
        val query = "Select * from $dbTitle"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val history = History()
                history.id = cursor.getString(cursor.getColumnIndex(id)).toInt()
                history.date = cursor.getString(cursor.getColumnIndex(date))
                history.residualCapacity = cursor.getInt(cursor.getColumnIndex(residualCapacity))
                historyList.add(history)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historyList
    }

    fun clear() {

        val db = this.writableDatabase
        db.delete(dbTitle, null, null)
        db.close()
    }

    fun removeFirstRow() {
        val db = writableDatabase
        val cursor = db.query(dbTitle, null, null, null, null,
            null, null)

        if (cursor.moveToFirst()) {
            val rowId: String = cursor.getString(cursor.getColumnIndex(id))
            db.delete(dbTitle, "$id=?", arrayOf(rowId))
        }

        cursor.close()
        db.close()
    }

    fun getCount(): Long {
        val db = this.readableDatabase
        val count = DatabaseUtils.queryNumEntries(db, dbTitle)
        db.close()
        return count
    }
}
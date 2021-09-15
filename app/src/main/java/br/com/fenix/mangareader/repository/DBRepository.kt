package br.com.fenix.mangareader.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import br.com.fenix.mangareader.constants.GeneralConsts

class DBRepository private constructor(context: Context) {

    // Singleton - One database initialize only
    private var mDataBaseHelper: DataBaseHelper = DataBaseHelper(context)

    companion object {
        private lateinit var repository: DBRepository
        fun getInstance(context: Context): DBRepository {

            // valide is initialize repositorie
            if (!::repository.isInitialized)
                repository = DBRepository(context)

            return DBRepository(context)
        }
    }

    fun save(table: String, contents: ContentValues): Long {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.insert(table, null, contents)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "InsertSQL: Table: $table - Contents: ${contents.toString()}"
            )
            -1
        }
    }

    fun update(
        table: String,
        contents: ContentValues,
        where: String,
        args: Array<String>
    ): Boolean {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.update(table, contents, where, args)
            true
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "UpdateSQL: Table: $table - Contents: ${contents.toString()} - Where: $where - Args: ${args.toString()}"
            )
            false
        }
    }

    fun delete(table: String, where: String, args: Array<String>): Boolean {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.delete(table, where, args)
            true
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "DeleteSQL: Table: $table - Where: $where - Args: ${args.toString()}"
            )
            false
        }
    }

    fun query(
        table: String, columns: Array<String>, where: String?, args: Array<String>?
    ): Cursor? {
        return try {
            val db = mDataBaseHelper.readableDatabase
            db.query(table, columns, where, args, null, null, null)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "QuerySQL: Table: $table - Columns: ${columns.toString()} - Where: $where - Args: ${args.toString()}"
            )
            null
        }
    }

    fun query(
        table: String, columns: Array<String>, where: String, args: Array<String>,
        groupBy: String, orderBy: String, having: String
    ): Cursor? {
        return try {
            val db = mDataBaseHelper.readableDatabase
            db.query(table, columns, where, args, groupBy, having, orderBy)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "QuerySQL: Table: $table - Columns: ${columns.toString()} - Where: $where " +
                        "- Args: ${args.toString()} - GroupBy: $groupBy - OrderBy: $orderBy - Having: $having"
            )
            null
        }
    }

    fun query(
        table: String, columns: Array<String>, where: String, args: Array<String>,
        groupBy: String, orderBy: String, having: String, limit: String
    ): Cursor? {
        return try {
            val db = mDataBaseHelper.readableDatabase
            db.query(table, columns, where, args, groupBy, having, orderBy, limit)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(
                GeneralConsts.TAG.LOG,
                "QuerySQL: Table: $table - Columns: ${columns.toString()} - Where: $where " +
                        "- Args: ${args.toString()} - GroupBy: $groupBy - OrderBy: $orderBy " +
                        "- Having: $having - Limit: $limit"
            )
            null
        }
    }

    fun execSQL(sql: String): Boolean {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.execSQL(sql)
            true
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(GeneralConsts.TAG.LOG, "ExecSQL: $sql")
            false
        }
    }

    fun selectSQL(sql: String): Cursor? {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.rawQuery(sql, null)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(GeneralConsts.TAG.LOG, "ExecSQL: $sql")
            null
        }
    }

    fun selectSQL(sql: String, selectArgs : Array<String>): Cursor? {
        return try {
            val db = mDataBaseHelper.writableDatabase
            db.rawQuery(sql, selectArgs)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Exception: $e")
            Log.e(GeneralConsts.TAG.LOG, "ExecSQL: $sql")
            null
        }
    }

}
package com.susion.rabbit.base.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.susion.rabbit.base.entities.RabbitExceptionInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "RABBIT_EXCEPTION_INFO".
*/
public class RabbitExceptionInfoDao extends AbstractDao<RabbitExceptionInfo, Long> {

    public static final String TABLENAME = "RABBIT_EXCEPTION_INFO";

    /**
     * Properties of entity RabbitExceptionInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property CrashTraceStr = new Property(1, String.class, "crashTraceStr", false, "CRASH_TRACE_STR");
        public final static Property SimpleMessage = new Property(2, String.class, "simpleMessage", false, "SIMPLE_MESSAGE");
        public final static Property ThreadName = new Property(3, String.class, "threadName", false, "THREAD_NAME");
        public final static Property ExceptionName = new Property(4, String.class, "exceptionName", false, "EXCEPTION_NAME");
        public final static Property Time = new Property(5, Long.class, "time", false, "TIME");
        public final static Property PageName = new Property(6, String.class, "pageName", false, "PAGE_NAME");
    }


    public RabbitExceptionInfoDao(DaoConfig config) {
        super(config);
    }
    
    public RabbitExceptionInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"RABBIT_EXCEPTION_INFO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"CRASH_TRACE_STR\" TEXT," + // 1: crashTraceStr
                "\"SIMPLE_MESSAGE\" TEXT," + // 2: simpleMessage
                "\"THREAD_NAME\" TEXT," + // 3: threadName
                "\"EXCEPTION_NAME\" TEXT," + // 4: exceptionName
                "\"TIME\" INTEGER," + // 5: time
                "\"PAGE_NAME\" TEXT);"); // 6: pageName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"RABBIT_EXCEPTION_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RabbitExceptionInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String crashTraceStr = entity.getCrashTraceStr();
        if (crashTraceStr != null) {
            stmt.bindString(2, crashTraceStr);
        }
 
        String simpleMessage = entity.getSimpleMessage();
        if (simpleMessage != null) {
            stmt.bindString(3, simpleMessage);
        }
 
        String threadName = entity.getThreadName();
        if (threadName != null) {
            stmt.bindString(4, threadName);
        }
 
        String exceptionName = entity.getExceptionName();
        if (exceptionName != null) {
            stmt.bindString(5, exceptionName);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(6, time);
        }
 
        String pageName = entity.getPageName();
        if (pageName != null) {
            stmt.bindString(7, pageName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RabbitExceptionInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String crashTraceStr = entity.getCrashTraceStr();
        if (crashTraceStr != null) {
            stmt.bindString(2, crashTraceStr);
        }
 
        String simpleMessage = entity.getSimpleMessage();
        if (simpleMessage != null) {
            stmt.bindString(3, simpleMessage);
        }
 
        String threadName = entity.getThreadName();
        if (threadName != null) {
            stmt.bindString(4, threadName);
        }
 
        String exceptionName = entity.getExceptionName();
        if (exceptionName != null) {
            stmt.bindString(5, exceptionName);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(6, time);
        }
 
        String pageName = entity.getPageName();
        if (pageName != null) {
            stmt.bindString(7, pageName);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RabbitExceptionInfo readEntity(Cursor cursor, int offset) {
        RabbitExceptionInfo entity = new RabbitExceptionInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // crashTraceStr
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // simpleMessage
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // threadName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // exceptionName
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // time
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // pageName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RabbitExceptionInfo entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setCrashTraceStr(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSimpleMessage(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setThreadName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setExceptionName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setTime(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setPageName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RabbitExceptionInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RabbitExceptionInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RabbitExceptionInfo entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}

package com.atomcity.maimai.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _songDao: Lazy<SongDao> = lazy {
    SongDao_Impl(this)
  }


  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2,
        "f4e3840ff35a086b0aa390685acd311c", "b1fa04c5e62e5e7a4a7b2de4d8b74010") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `song` (`id` INTEGER, `code` TEXT, `name_en` TEXT, `name_jp` TEXT, `artist_en` TEXT, `artist_jp` TEXT, `matchedTitle` TEXT, `matchedBy` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_song_code` ON `song` (`code`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `level` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT, `songId` INTEGER NOT NULL, `diffIndex` INTEGER, `internal_level` TEXT, `level` TEXT, FOREIGN KEY(`songId`) REFERENCES `song`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_level_songId` ON `level` (`songId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_level_songId` ON `level` (`songId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f4e3840ff35a086b0aa390685acd311c')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `song`")
        connection.execSQL("DROP TABLE IF EXISTS `level`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsSong: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSong.put("id", TableInfo.Column("id", "INTEGER", false, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("code", TableInfo.Column("code", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("name_en", TableInfo.Column("name_en", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("name_jp", TableInfo.Column("name_jp", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("artist_en", TableInfo.Column("artist_en", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("artist_jp", TableInfo.Column("artist_jp", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("matchedTitle", TableInfo.Column("matchedTitle", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSong.put("matchedBy", TableInfo.Column("matchedBy", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSong: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSong: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSong.add(TableInfo.Index("idx_song_code", false, listOf("code"), listOf("ASC")))
        val _infoSong: TableInfo = TableInfo("song", _columnsSong, _foreignKeysSong, _indicesSong)
        val _existingSong: TableInfo = read(connection, "song")
        if (!_infoSong.equals(_existingSong)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |song(com.atomcity.maimai.db.SongEntity).
              | Expected:
              |""".trimMargin() + _infoSong + """
              |
              | Found:
              |""".trimMargin() + _existingSong)
        }
        val _columnsLevel: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLevel.put("uid", TableInfo.Column("uid", "INTEGER", false, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLevel.put("songId", TableInfo.Column("songId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLevel.put("diffIndex", TableInfo.Column("diffIndex", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLevel.put("internal_level", TableInfo.Column("internal_level", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLevel.put("level", TableInfo.Column("level", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLevel: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysLevel.add(TableInfo.ForeignKey("song", "CASCADE", "NO ACTION", listOf("songId"),
            listOf("id")))
        val _indicesLevel: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesLevel.add(TableInfo.Index("idx_level_songId", false, listOf("songId"),
            listOf("ASC")))
        _indicesLevel.add(TableInfo.Index("index_level_songId", false, listOf("songId"),
            listOf("ASC")))
        val _infoLevel: TableInfo = TableInfo("level", _columnsLevel, _foreignKeysLevel,
            _indicesLevel)
        val _existingLevel: TableInfo = read(connection, "level")
        if (!_infoLevel.equals(_existingLevel)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |level(com.atomcity.maimai.db.LevelEntity).
              | Expected:
              |""".trimMargin() + _infoLevel + """
              |
              | Found:
              |""".trimMargin() + _existingLevel)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "song", "level")
  }

  public override fun clearAllTables() {
    super.performClear(true, "song", "level")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SongDao::class, SongDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun songDao(): SongDao = _songDao.value
}

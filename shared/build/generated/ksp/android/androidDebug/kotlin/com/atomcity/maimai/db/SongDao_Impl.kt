package com.atomcity.maimai.db

import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SongDao_Impl(
  __db: RoomDatabase,
) : SongDao {
  private val __db: RoomDatabase
  init {
    this.__db = __db
  }

  public override suspend fun getSongById(id: Int): SongEntity? {
    val _sql: String = "SELECT * FROM song WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _cursorIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _cursorIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _cursorIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _cursorIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _cursorIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _cursorIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: SongEntity?
        if (_stmt.step()) {
          val _tmpId: Int?
          if (_stmt.isNull(_cursorIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_cursorIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_cursorIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_cursorIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_cursorIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_cursorIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_cursorIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_cursorIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_cursorIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_cursorIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_cursorIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_cursorIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_cursorIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_cursorIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_cursorIndexOfMatchedBy)
          }
          _result =
              SongEntity(_tmpId,_tmpCode,_tmpName_en,_tmpName_jp,_tmpArtist_en,_tmpArtist_jp,_tmpMatchedTitle,_tmpMatchedBy)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSongByCode(code: String): SongEntity? {
    val _sql: String = "SELECT * FROM song WHERE code = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, code)
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _cursorIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _cursorIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _cursorIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _cursorIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _cursorIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _cursorIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: SongEntity?
        if (_stmt.step()) {
          val _tmpId: Int?
          if (_stmt.isNull(_cursorIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_cursorIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_cursorIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_cursorIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_cursorIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_cursorIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_cursorIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_cursorIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_cursorIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_cursorIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_cursorIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_cursorIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_cursorIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_cursorIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_cursorIndexOfMatchedBy)
          }
          _result =
              SongEntity(_tmpId,_tmpCode,_tmpName_en,_tmpName_jp,_tmpArtist_en,_tmpArtist_jp,_tmpMatchedTitle,_tmpMatchedBy)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun searchSongs(q: String): List<SongEntity> {
    val _sql: String =
        "SELECT * FROM song WHERE name_en LIKE '%' || ? || '%' OR name_jp LIKE '%' || ? || '%' OR artist_en LIKE '%' || ? || '%' OR artist_jp LIKE '%' || ? || '%'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, q)
        _argIndex = 2
        _stmt.bindText(_argIndex, q)
        _argIndex = 3
        _stmt.bindText(_argIndex, q)
        _argIndex = 4
        _stmt.bindText(_argIndex, q)
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _cursorIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _cursorIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _cursorIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _cursorIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _cursorIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _cursorIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: MutableList<SongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongEntity
          val _tmpId: Int?
          if (_stmt.isNull(_cursorIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_cursorIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_cursorIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_cursorIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_cursorIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_cursorIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_cursorIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_cursorIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_cursorIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_cursorIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_cursorIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_cursorIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_cursorIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_cursorIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_cursorIndexOfMatchedBy)
          }
          _item =
              SongEntity(_tmpId,_tmpCode,_tmpName_en,_tmpName_jp,_tmpArtist_en,_tmpArtist_jp,_tmpMatchedTitle,_tmpMatchedBy)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLevelsForSong(songId: Int?): List<LevelEntity> {
    val _sql: String = "SELECT * FROM level WHERE songId = ? ORDER BY diffIndex"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (songId == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, songId.toLong())
        }
        val _cursorIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _cursorIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _cursorIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _cursorIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _cursorIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: MutableList<LevelEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LevelEntity
          val _tmpUid: Long?
          if (_stmt.isNull(_cursorIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_cursorIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_cursorIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_cursorIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_cursorIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_cursorIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_cursorIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_cursorIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_cursorIndexOfLevel)
          }
          _item = LevelEntity(_tmpUid,_tmpSongId,_tmpDiffIndex,_tmpInternal_level,_tmpLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSongLevels(): List<SongLevelRow> {
    val _sql: String = """
        |
        |        SELECT
        |            song.matchedTitle,
        |            song.name_en,
        |            song.name_jp,
        |            song.code,
        |            level.diffIndex,
        |            level.level,
        |            level.internal_level
        |        FROM level
        |        JOIN song ON song.id = level.songId
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfMatchedTitle: Int = 0
        val _cursorIndexOfNameEn: Int = 1
        val _cursorIndexOfNameJp: Int = 2
        val _cursorIndexOfCode: Int = 3
        val _cursorIndexOfDiffIndex: Int = 4
        val _cursorIndexOfLevel: Int = 5
        val _cursorIndexOfInternalLevel: Int = 6
        val _result: MutableList<SongLevelRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongLevelRow
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_cursorIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_cursorIndexOfMatchedTitle)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_cursorIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_cursorIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_cursorIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_cursorIndexOfNameJp)
          }
          val _tmpCode: String?
          if (_stmt.isNull(_cursorIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_cursorIndexOfCode)
          }
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_cursorIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_cursorIndexOfDiffIndex).toInt()
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_cursorIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_cursorIndexOfLevel)
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_cursorIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_cursorIndexOfInternalLevel)
          }
          _item =
              SongLevelRow(_tmpMatchedTitle,_tmpName_en,_tmpName_jp,_tmpCode,_tmpDiffIndex,_tmpLevel,_tmpInternal_level)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLevelsByTitle(title: String): LevelEntity? {
    val _sql: String = """
        |
        |        SELECT level.*
        |        FROM level
        |        JOIN song ON song.id = level.songId
        |        WHERE lower(COALESCE(song.matchedTitle, song.name_en, song.code, song.name_jp)) = lower(?)
        |        ORDER BY level.diffIndex
        |        LIMIT 1
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, title)
        val _cursorIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _cursorIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _cursorIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _cursorIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _cursorIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: LevelEntity?
        if (_stmt.step()) {
          val _tmpUid: Long?
          if (_stmt.isNull(_cursorIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_cursorIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_cursorIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_cursorIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_cursorIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_cursorIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_cursorIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_cursorIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_cursorIndexOfLevel)
          }
          _result = LevelEntity(_tmpUid,_tmpSongId,_tmpDiffIndex,_tmpInternal_level,_tmpLevel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSongs(): List<SongEntity> {
    val _sql: String = "SELECT * FROM song"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _cursorIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _cursorIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _cursorIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _cursorIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _cursorIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _cursorIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: MutableList<SongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongEntity
          val _tmpId: Int?
          if (_stmt.isNull(_cursorIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_cursorIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_cursorIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_cursorIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_cursorIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_cursorIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_cursorIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_cursorIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_cursorIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_cursorIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_cursorIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_cursorIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_cursorIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_cursorIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_cursorIndexOfMatchedBy)
          }
          _item =
              SongEntity(_tmpId,_tmpCode,_tmpName_en,_tmpName_jp,_tmpArtist_en,_tmpArtist_jp,_tmpMatchedTitle,_tmpMatchedBy)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLevelByDifficulty(songId: Int, difficultyValue: Int):
      LevelEntity? {
    val _sql: String = "SELECT * FROM level WHERE songId = ? AND diffIndex = ? ORDER BY uid LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, songId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, difficultyValue.toLong())
        val _cursorIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _cursorIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _cursorIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _cursorIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _cursorIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: LevelEntity?
        if (_stmt.step()) {
          val _tmpUid: Long?
          if (_stmt.isNull(_cursorIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_cursorIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_cursorIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_cursorIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_cursorIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_cursorIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_cursorIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_cursorIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_cursorIndexOfLevel)
          }
          _result = LevelEntity(_tmpUid,_tmpSongId,_tmpDiffIndex,_tmpInternal_level,_tmpLevel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

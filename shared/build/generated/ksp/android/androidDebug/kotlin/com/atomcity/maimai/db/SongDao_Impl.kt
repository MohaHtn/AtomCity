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
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _columnIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _columnIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _columnIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _columnIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _columnIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: SongEntity?
        if (_stmt.step()) {
          val _tmpId: Int?
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_columnIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_columnIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_columnIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_columnIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_columnIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_columnIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_columnIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_columnIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_columnIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_columnIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_columnIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_columnIndexOfMatchedBy)
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
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _columnIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _columnIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _columnIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _columnIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _columnIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: SongEntity?
        if (_stmt.step()) {
          val _tmpId: Int?
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_columnIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_columnIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_columnIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_columnIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_columnIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_columnIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_columnIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_columnIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_columnIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_columnIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_columnIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_columnIndexOfMatchedBy)
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
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _columnIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _columnIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _columnIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _columnIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _columnIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: MutableList<SongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongEntity
          val _tmpId: Int?
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_columnIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_columnIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_columnIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_columnIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_columnIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_columnIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_columnIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_columnIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_columnIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_columnIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_columnIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_columnIndexOfMatchedBy)
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
        val _columnIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _columnIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: MutableList<LevelEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LevelEntity
          val _tmpUid: Long?
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_columnIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_columnIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_columnIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_columnIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_columnIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_columnIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_columnIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_columnIndexOfLevel)
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
        val _columnIndexOfMatchedTitle: Int = 0
        val _columnIndexOfNameEn: Int = 1
        val _columnIndexOfNameJp: Int = 2
        val _columnIndexOfCode: Int = 3
        val _columnIndexOfDiffIndex: Int = 4
        val _columnIndexOfLevel: Int = 5
        val _columnIndexOfInternalLevel: Int = 6
        val _result: MutableList<SongLevelRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongLevelRow
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_columnIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_columnIndexOfMatchedTitle)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_columnIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_columnIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_columnIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_columnIndexOfNameJp)
          }
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_columnIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_columnIndexOfDiffIndex).toInt()
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_columnIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_columnIndexOfLevel)
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_columnIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_columnIndexOfInternalLevel)
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
        val _columnIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _columnIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: LevelEntity?
        if (_stmt.step()) {
          val _tmpUid: Long?
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_columnIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_columnIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_columnIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_columnIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_columnIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_columnIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_columnIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_columnIndexOfLevel)
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
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "name_en")
        val _columnIndexOfNameJp: Int = getColumnIndexOrThrow(_stmt, "name_jp")
        val _columnIndexOfArtistEn: Int = getColumnIndexOrThrow(_stmt, "artist_en")
        val _columnIndexOfArtistJp: Int = getColumnIndexOrThrow(_stmt, "artist_jp")
        val _columnIndexOfMatchedTitle: Int = getColumnIndexOrThrow(_stmt, "matchedTitle")
        val _columnIndexOfMatchedBy: Int = getColumnIndexOrThrow(_stmt, "matchedBy")
        val _result: MutableList<SongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongEntity
          val _tmpId: Int?
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null
          } else {
            _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          }
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpName_en: String?
          if (_stmt.isNull(_columnIndexOfNameEn)) {
            _tmpName_en = null
          } else {
            _tmpName_en = _stmt.getText(_columnIndexOfNameEn)
          }
          val _tmpName_jp: String?
          if (_stmt.isNull(_columnIndexOfNameJp)) {
            _tmpName_jp = null
          } else {
            _tmpName_jp = _stmt.getText(_columnIndexOfNameJp)
          }
          val _tmpArtist_en: String?
          if (_stmt.isNull(_columnIndexOfArtistEn)) {
            _tmpArtist_en = null
          } else {
            _tmpArtist_en = _stmt.getText(_columnIndexOfArtistEn)
          }
          val _tmpArtist_jp: String?
          if (_stmt.isNull(_columnIndexOfArtistJp)) {
            _tmpArtist_jp = null
          } else {
            _tmpArtist_jp = _stmt.getText(_columnIndexOfArtistJp)
          }
          val _tmpMatchedTitle: String?
          if (_stmt.isNull(_columnIndexOfMatchedTitle)) {
            _tmpMatchedTitle = null
          } else {
            _tmpMatchedTitle = _stmt.getText(_columnIndexOfMatchedTitle)
          }
          val _tmpMatchedBy: String?
          if (_stmt.isNull(_columnIndexOfMatchedBy)) {
            _tmpMatchedBy = null
          } else {
            _tmpMatchedBy = _stmt.getText(_columnIndexOfMatchedBy)
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
        val _columnIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfDiffIndex: Int = getColumnIndexOrThrow(_stmt, "diffIndex")
        val _columnIndexOfInternalLevel: Int = getColumnIndexOrThrow(_stmt, "internal_level")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _result: LevelEntity?
        if (_stmt.step()) {
          val _tmpUid: Long?
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null
          } else {
            _tmpUid = _stmt.getLong(_columnIndexOfUid)
          }
          val _tmpSongId: Int
          _tmpSongId = _stmt.getLong(_columnIndexOfSongId).toInt()
          val _tmpDiffIndex: Int?
          if (_stmt.isNull(_columnIndexOfDiffIndex)) {
            _tmpDiffIndex = null
          } else {
            _tmpDiffIndex = _stmt.getLong(_columnIndexOfDiffIndex).toInt()
          }
          val _tmpInternal_level: String?
          if (_stmt.isNull(_columnIndexOfInternalLevel)) {
            _tmpInternal_level = null
          } else {
            _tmpInternal_level = _stmt.getText(_columnIndexOfInternalLevel)
          }
          val _tmpLevel: String?
          if (_stmt.isNull(_columnIndexOfLevel)) {
            _tmpLevel = null
          } else {
            _tmpLevel = _stmt.getText(_columnIndexOfLevel)
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

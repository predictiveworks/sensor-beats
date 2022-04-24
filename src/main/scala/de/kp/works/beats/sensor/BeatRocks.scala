package de.kp.works.beats.sensor

/**
 * Copyright (c) 2020 - 2022 Dr. Krusche & Partner PartG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Stefan Krusche, Dr. Krusche & Partner PartG
 *
 */

import org.rocksdb.{ColumnFamilyDescriptor, ColumnFamilyHandle, ColumnFamilyOptions, DBOptions, ReadOptions, RocksDB}

import java.util
import java.util.{List => JList}
import java.util.Objects
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable

object BeatRocksApi {

  private var instance:Option[BeatRocksApi] = None

  def getInstance: BeatRocksApi = instance.get

  def getInstance(tables:Seq[String], folder:String):BeatRocksApi = {
    if (instance.isEmpty) instance = Some(new BeatRocksApi(tables, folder))
    instance.get
  }

  def isInstance:Boolean = instance.nonEmpty

}

class BeatRocksApi(tables:Seq[String], folder:String) {
  /**
   * Initialize RocksDB with provided tables; every table
   * refers to a certain column family.
   */
  BeatRocks.getOrCreate(tables, folder)

  def put(table:String, time:Long, value:String):Unit = {
    BeatRocks.putTs(table, time, value)
  }

  def scan(table:String):Seq[(Long, String)] = {
    BeatRocks.scanTs(table)
  }

}

object BeatRocks {

  private var rocksDB: RocksDB = _
  private var rocksHandles: Map[String, ColumnFamilyHandle] = _

  private val CHARSET = "UTF-8"

  def isInit:Boolean =  !Objects.isNull(rocksDB)

  def getOrCreate(tables:Seq[String], path: String): Unit = {

    if (Objects.isNull(rocksDB)) {
      RocksDB.loadLibrary()
      /*
       * Open RocksDB in such a way that all the tables
       * specified can be accessed
       */
      val tableOptions = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()
      /*
       * The first entry of all the supported column family
       * descriptors must be the default column family
       */
      val defaultDescriptor =
        new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, tableOptions)

      val tableDescriptors:JList[ColumnFamilyDescriptor] = (
        List(defaultDescriptor) ++ tables.map(table => {
        new ColumnFamilyDescriptor(table.getBytes(CHARSET), tableOptions)
      })).asJava

      val rocksOptions = new DBOptions()
        .setCreateIfMissing(true).setCreateMissingColumnFamilies(true)

      val tableHandles = new java.util.ArrayList[ColumnFamilyHandle]()
      rocksDB = RocksDB.open(rocksOptions, path, tableDescriptors, tableHandles)
      /*
       * Prepare table handles
       */
      rocksHandles = tableHandles.map(tableHandle => {

        val tableName = new String(tableHandle.getName, CHARSET)
        (tableName, tableHandle)

      }).toMap

    }

  }

  def deleteTs(table:String, ts:Long):Unit = {
    delete(table, ts.toString)
  }

  def delete(table:String, key:String):Unit = {

    validate(table)

    val tableHandle = rocksHandles(table)
    rocksDB.delete(tableHandle, key.getBytes(CHARSET))

  }

  def deleteTsRange(table:String, start:Long, end:Long):Unit = {
    deleteRange(table, start.toString, end.toString)
  }

  def deleteRange(table:String, skey:String, ekey:String):Unit = {

    validate(table)

    val tableHandle = rocksHandles(table)
    rocksDB.deleteRange(tableHandle, skey.getBytes(CHARSET), ekey.getBytes(CHARSET))

  }

  def getTs(table:String, ts:Long):String = {
    get(table, ts.toString)
  }

  def get(table:String, key:String):String = {

    validate(table)

    val tableHandle = rocksHandles(table)
    val bytes = rocksDB.get(tableHandle, key.getBytes(CHARSET))

    new String(bytes, CHARSET)

  }

  def getTsRange(table:String, ts:Seq[Long]):Seq[(Long,String)] = {

    val keys = ts.map(_.toString)
    val result = getRange(table, keys)

    result.map{case(k,v) => (k.toLong, v)}

  }

  def getRange(table:String, keys:Seq[String]):Seq[(String,String)] = {

    validate(table)

    val tableHandle = rocksHandles(table)
    val handles:JList[ColumnFamilyHandle] = List(tableHandle).asJava

    val bytes = keys.map(key => key.getBytes(CHARSET)).asJava
    val values = rocksDB.multiGetAsList(handles, bytes)

    keys.zip(values).map{case(k,v) => (k, new String(v, CHARSET))}

  }

  /**
   * Public method to write a timeseries dot
   * into the internal RocksDB
   */
  def putTs(table:String, ts:Long, value:String):Unit = {
    val key = ts.toString
    put(table, key, value)
  }
  /**
   * Public method to write a single (key, value)
   * pair into the internal RocksDB
   */
  def put(table:String, key:String, value:String):Unit = {

    validate(table)

    val tableHandle = rocksHandles(table)
    rocksDB.put(tableHandle, key.getBytes(CHARSET), value.getBytes(CHARSET))

  }

  def scanTs(table:String):Seq[(Long, String)] = {
    scan(table).map{case(k,v) => (k.toLong, v)}
  }

  def scan(table:String):Seq[(String,String)] = {

    validate(table)

    val tableHandle = rocksHandles(table)
    val rocksIter = rocksDB.newIterator(tableHandle)
    /*
     * Start scan with the first entry
     */
    rocksIter.seekToFirst()

    val result = mutable.ArrayBuffer.empty[(String,String)]
    while (rocksIter.isValid) {
      rocksIter.next()

      val k = new String(rocksIter.key(), CHARSET)
      val v = new String(rocksIter.value(), CHARSET)

      val kv = (k,v)
      result += kv

    }

    result

  }

  def scanTsRange(table:String, start:Long, end:Long):Seq[(Long, String)] = {

    val skey = start.toString
    val ekey = end.toString

    val result = scanRange(table, skey, ekey)
    result.map{case(k,v) => (k.toLong, v)}

  }

  def scanRange(table:String, skey:String, ekey:String):Seq[(String, String)] = {

    validate(table)

    val sbytes = skey.getBytes(CHARSET)
    val ebytes = ekey.getBytes(CHARSET)

    val tableHandle = rocksHandles(table)

    val readOptions = new ReadOptions()
    readOptions.setTotalOrderSeek(true)

    val rocksIter = rocksDB.newIterator(tableHandle, readOptions)
    /*
     * Start scan with the provided `skey`
     */
    rocksIter.seek(sbytes)
    var scan = true

    val result = mutable.ArrayBuffer.empty[(String,String)]
    while(rocksIter.isValid && scan) {
      rocksIter.next()

      val kbytes = rocksIter.key()
      val vbytes = rocksIter.value()

      val k = new String(kbytes, CHARSET)
      val v = new String(vbytes, CHARSET)

      val kv = (k,v)
      result += kv

      if (util.Arrays.equals(kbytes, ebytes)) scan = false

    }

    result

  }

  private def validate(table:String):Unit = {

    if (Objects.isNull(rocksDB))
      throw new Exception(s"[BeatRocks] Database does not exist.")

    if (Objects.isNull(rocksHandles))
      throw new Exception(s"[BeatRocks] No tables registered.")

    if (!rocksHandles.contains(table))
      throw new Exception(s"[BeatRocks] Table `$table` is not registered.")

  }

  def close(): Unit = {

    if (!Objects.isNull(rocksDB)) {
      /*
       * Release the column families before
       * releasing the database
       */
      if (!Objects.isNull(rocksHandles)) {
        rocksHandles.foreach{case(_, handle) => handle.close()}
      }

      rocksDB.close()
      rocksDB = null
    }

  }

}

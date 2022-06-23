package de.kp.works.beats.sensor.ditto
/**
 * Copyright (c) 2019 - 2022 Dr. Krusche & Partner PartG. All rights reserved.
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
import java.util.Optional
import com.google.gson._
import de.kp.works.beats.sensor.{BeatAttr, BeatInfos, BeatSensor}
import org.eclipse.ditto.client.changes._
import org.eclipse.ditto.client.live.messages.RepliableMessage
import org.eclipse.ditto.json
import org.eclipse.ditto.json.JsonFactory
import org.eclipse.ditto.things.model.{Feature, FeatureProperties, Features, Thing, ThingId, ThingsModelFactory}

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConverters.asJavaIterableConverter
/**
 * [DittoTransform] is built to transform change events
 * of things either into JSON objects or BeatSensors
 * for further processing
 */
object DittoTransform {

  def buildAttributes(sensor:BeatSensor, namespace:String):Thing = {

    /***** CREATE (STATIC) THING ATTRIBUTES *****/

    /*
     * Build `Thing` identifier from configured
     * namespace and provided sensor identifier
     */
    val thingId = ThingId.of(s"$namespace:${sensor.sensorId}")
    /*
     * Leverage the [ThingsModelFactory] to build
     * a new Ditto `thing`
     */
    val thingsBuilder = ThingsModelFactory.newThingBuilder()
    thingsBuilder.setId(thingId)
    /*
     * The parameters sensorBrand, sensorInfo and
     * sensorType are described as attributes
     */
    val sensorBrand = JsonFactory.newPointer("brand")
    thingsBuilder.setAttribute(sensorBrand, JsonFactory.newValue(sensor.sensorBrand))

    val sensorType = JsonFactory.newPointer("type")
    thingsBuilder.setAttribute(sensorType, JsonFactory.newValue(sensor.sensorType))

    val sensorInfo = JsonFactory.newPointer("info")
    thingsBuilder.setAttribute(sensorInfo, JsonFactory.newValue(sensor.sensorInfo.toString))

    thingsBuilder.build

  }
  /**
   * feature = {
   *    id (attribute name) = {
   *      definition = [Optional: semantic representation e.g. from Smart Data Models],
   *      properties = {
   *        createdAt = Sensor time [Long]
   *        type = Attribute data type [String]
   *        value = Attribute value [Double]
   *    }
   * }
   */
  def buildFeature(sensorTime:Long, beatAttr:BeatAttr):Feature = {

    val properties = ThingsModelFactory.newFeaturePropertiesBuilder
      .set("createdAt", sensorTime)
      .set("type",      beatAttr.attrType)
      .set("value",     beatAttr.attrValue.doubleValue())
      .build

    ThingsModelFactory.newFeatureBuilder()
      .properties(properties)
      .withId(beatAttr.attrName)
      .build

  }
  /**
   * Helper method to transform sensor attributes
   * into Ditto features
   */
  def buildFeatures(sensorTime:Long, sensorAttrs:Seq[BeatAttr]):Features = {

    val features = sensorAttrs.map(sensorAttr => {
      buildFeature(sensorTime,sensorAttr)
    }).asJava

    ThingsModelFactory.newFeaturesBuilder(features).build

  }
  /**
   * Helper method to transform a Ditto [ThingChange]
   * event into a JSON representation
   */
  def thing2Gson(change: ThingChange): String = {

    if (!changeGuard(change)) return null

    val thing = change.getThing.get
    val gson = new JsonObject()
    
    /* Timestamp of change */
    val ts = getTime(change)
    gson.addProperty("timestamp", ts)

    /* Thing identifier */

    val thingId = getThingId(thing.getEntityId)
    if (thingId.nonEmpty)
      gson.addProperty("id", thingId.get)

    /* Attributes */

    val attributes = thing.getAttributes
    if (attributes.isPresent) {
      attributes.get.toSeq.foreach(attribute => {

        val attrName = attribute.getKeyName
        val attrValu = attribute.getValue

        if (attrValu.isBoolean) {

          if (attrValu.isDouble)
            gson.addProperty(attrName, attrValu.asBoolean)

          else if (attrValu.isDouble)
            gson.addProperty(attrName, attrValu.asDouble)

          else if (attrValu.isInt)
            gson.addProperty(attrName, attrValu.asInt)

          else if (attrValu.isLong)
            gson.addProperty(attrName, attrValu.asLong)

        }
        else if (attrValu.isString) {
          gson.addProperty(attrName, attrValu.asString)
        }

      })
    }

    /* Features */
    
    val features = features2Gson(thing.getFeatures.get)
    gson.add("features", features)
    
    gson.getAsString

  }

  private def changeGuard(change:ThingChange):Boolean = {
    /*
     * The thing consumer listens to thing updates,
     * i.e. create & delete actions are ignored
     */
    val action = change.getAction
    if (action.name != "UPDATED") return false

    val thing = change.getThing
    if (!thing.isPresent) return false

    val entity = thing.get
    /*
     * The thing consumer listens to things with
     * features assigned
     */
    if (!entity.getFeatures.isPresent) return false

    true
  }

  def features2Gson(change: FeaturesChange): String = {
    
    val gson = new JsonObject()
    
    /* Timestamp of change */
    val ts = getTime(change)
    gson.addProperty("timestamp", ts)
    
    val features = features2Gson(change.getFeatures)
    gson.add("features", features)
    
    gson.toString
    
  }
    
  def feature2Gson(change: FeatureChange): String = {
    
    val gson = new JsonObject()
    
    /* Timestamp of change */
    val ts = getTime(change)
    gson.addProperty("timestamp", ts)

    val feature = change.getFeature   
    
    /* Feature identifier */
    val gFeature = new JsonObject()      
    gFeature.addProperty("id", feature.getId)
     
    /* Properties */
    if (feature.getProperties.isPresent) {
      
      val properties = properties2Gson(feature.getProperties.get)
      gFeature.add("properties", properties)
      
    } else
      gFeature.add("properties", new JsonArray())
      
    gson.add("feature", gFeature)
    gson.toString
    
  }
  /**
   * Helper method to transform Ditto live messages
   * into a JSON representation.
   *
   * Note, the current implementation of the SensorBeat
   * does not support `live messages` yet.
   */
  def message2Gson(message:RepliableMessage[String, Any]): String = {
    
    val gson = new JsonObject()
    
    val payload = message.getPayload
    if (!payload.isPresent) return null
    
    val content = payload.get

    /* Timestamp of the message */    
    val ts = {
       if (message.getTimestamp.isPresent) {
         message.getTimestamp.get.toInstant.toEpochMilli
         
       } else
         new java.util.Date().getTime
    }

    gson.addProperty("timestamp", ts)
    
    /* Name and namespace of thing */

    val entityId = message.getEntityId

    gson.addProperty("name", entityId.getName)
    gson.addProperty("namespace", entityId.getNamespace)
    
    gson.addProperty("subject", message.getSubject)
    gson.addProperty("payload", content)
    
    gson.toString
    
  }
  
  def features2Gson(features: Features): JsonArray = {
    
    val gFeatures = new JsonArray()

    /* Extract features */
    val iter = features.stream.iterator
    while (iter.hasNext) {

      val feature = iter.next
      
      /* Feature identifier */
      val gFeature = new JsonObject()      
      gFeature.addProperty("id", feature.getId)
     
      /* Properties */
      if (feature.getProperties.isPresent) {
        
        val properties = properties2Gson(feature.getProperties.get)
        gFeature.add("properties", properties)
        
      } else
        gFeature.add("properties", new JsonArray())
        
      gFeatures.add(gFeature)
      
    }
    
    gFeatures
    
  }

  private def properties2Gson(properties:FeatureProperties):JsonArray = {
    
    val gProperties = new JsonArray()
    
    /* Extract properties */
    val iter = properties.stream.iterator
    while (iter.hasNext) {
      
      val property = iter.next
      gProperties.add(property2Gson(property))
      
    }
    gProperties

  }

  private def property2Gson(property: json.JsonField): JsonObject = {

    val gson = new JsonObject()
    
    val name = property.getKeyName
    gson.addProperty("name", name)

    val value = property.getValue  
    value2Gson(gson, value)
    
    gson
    
  }
  
  private def value2Gson(gson: JsonObject, value: json.JsonValue):Unit = {

    if (value.isNull) {
      
      gson.addProperty("type", "NULL")
      gson.add("value", JsonNull.INSTANCE)
      
    }  
    else if (value.isBoolean) {
      
      gson.addProperty("type", "BOOLEAN")
      gson.addProperty("value", value.asBoolean)
      
    }
    else if (value.isDouble) {
      
      gson.addProperty("type", "DOUBLE")
      gson.addProperty("value", value.asDouble)
      
    }
    else if (value.isInt) {
      
      gson.addProperty("type", "INTEGER")
      gson.addProperty("value", value.asInt)
      
    }
    else if (value.isLong) {
      
      gson.addProperty("type", "LONG")
      gson.addProperty("value", value.asLong)
      
    }
    else if (value.isString) {
      
      gson.addProperty("type", "STRING")
      gson.addProperty("value", value.asString)
      
    }
    else if (value.isArray) {

      gson.addProperty("type", "ARRAY")
      gson.add("value", array2Gson(value.asArray))
      
    }
    else if (value.isObject) {

      gson.addProperty("type", "OBJECT")
      gson.add("value", object2Gson(value.asObject))
      
    }
    
  }
  
  private def array2Gson(value: json.JsonArray): JsonArray = {
    
    val gson = new JsonArray()
    
    val iter = value.iterator
    while (iter.hasNext) {
    
      val item = new JsonObject()
      value2Gson(item, iter.next)

      gson.add(item)
    
    }

    gson
    
  }
  
  private def object2Gson(obj: json.JsonObject): JsonObject = {
    
    val gson = new JsonObject()
    
    val names = obj.getKeys.iterator
    while (names.hasNext) {
      
      val name = names.next
      val value = obj.getValue(name)
      
      val inner = new JsonObject()
      if (value.isPresent) {
        
        value2Gson(inner, value.get)
        
      } else {
        
        inner.addProperty("type", "NULL")
        inner.add("value", JsonNull.INSTANCE)
        
      }
      
      gson.add(name.toString, inner)
      
    }
    
    gson

  }

  private def getThingId(thingId: Optional[ThingId]): Option[String] = {
    if (!thingId.isPresent) None else Some(thingId.get.toString)
  }
  
  /*
   * A helper method to extract the timestamp 
   * of a certain change
   */
  private def getTime(change:Change): Long = {
    
    val instant = change.getTimestamp

    if (instant.isPresent) {
      instant.get.toEpochMilli
    
    } else new java.util.Date().getTime
    
  }
  /**
   * This method transforms a thing change event
   * from another SensorBeat into a [BeatSensor]
   * to support
   */
  def thing2Sensor(change:ThingChange, namespace:String):Option[BeatSensor] = {

    if (!changeGuard(change)) return None

    val thing = change.getThing.get
    /*
     * Retrieve sensorId from the [ThingId]
     */
    val sensorId = {
      val thingId = thing.getEntityId

      val value = if (!thingId.isPresent) "" else thingId.get.toString
      value.replace(s"$namespace:","")
    }

    if (sensorId.isEmpty) return None
    /*
     * Retrieve sensorTime, which is extracted
     * from the timestamp the Thing change happens
     */
    val sensorTime = getTime(change)
    /*
     * Retrieve sensorBrand, sensorType and sensorInfo
     * from the Thing's attributes
     */
    val attributes = thing.getAttributes
    if (!attributes.isPresent) return None

    var sensorBrand:String = null
    var sensorType:String  = null

    var sensorInfo:String = null
    attributes.get.toSeq.foreach(attribute => {

      val attrName = attribute.getKeyName
      if (attrName == "brand")
        sensorBrand = attribute.getValue.asString

      else if (attrName == "type")
        sensorType = attribute.getValue.asString

      else if (attrName == "info")
        sensorInfo = attribute.getValue.asString

    })

    if (sensorBrand == null || sensorType == null || sensorInfo == null) return None
    /*
     * Retrieve sensor attributes from the Thing
     * features
     */
    val features = thing.getFeatures
    if (!features.isPresent) return None

    val sensorAttrs = features.get
      .map(feature => {
        try {

          val attrName = feature.getId

          var attrType:String  = null
          var attrValue:Double = Double.NaN

          val properties = feature.getProperties.get
          properties.foreach(property => {

            val k = property.getKeyName
            val v = property.getValue

            if (k == "type")
              attrType = v.asString

            else if (k == "value")
              attrValue = v.asDouble

          })

          if (attrType == null || attrValue == Double.NaN)
            throw new Exception(s"Unpacking property failed")

          Some(BeatAttr(
            attrName  = attrName,
            attrType  = attrType,
            attrValue = attrValue))

        } catch {
          case _:Throwable => None
        }

      })
      .filter(sensorAttr => sensorAttr.nonEmpty)
      .map(sensorAttr => sensorAttr.get)
      .toSeq

    val sensor = BeatSensor(
      sensorId    = sensorId,
      sensorType  = sensorType,
      sensorBrand = sensorBrand,
      sensorInfo  = BeatInfos.withName(sensorInfo),
      sensorTime  = sensorTime,
      sensorAttrs = sensorAttrs)

    Some(sensor)

  }
}

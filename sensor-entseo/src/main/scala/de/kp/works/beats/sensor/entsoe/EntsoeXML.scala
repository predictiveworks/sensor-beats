package de.kp.works.beats.sensor.entsoe

import java.time.Instant
import java.util.Calendar
import scala.collection.mutable
import scala.xml.{Elem, XML}

object EntsoeXML {

  val loadSample =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<GL_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-6:generationloaddocument:3:0">
      |    <mRID>88eb1a2d150f4a0dbe438c708b599e5e</mRID>
      |    <revisionNumber>1</revisionNumber>
      |    <type>A65</type>
      |    <process.processType>A16</process.processType>
      |    <sender_MarketParticipant.mRID codingScheme="A01">10X1001A1001A450</sender_MarketParticipant.mRID>
      |    <sender_MarketParticipant.marketRole.type>A32</sender_MarketParticipant.marketRole.type>
      |    <receiver_MarketParticipant.mRID codingScheme="A01">10X1001A1001A450</receiver_MarketParticipant.mRID>
      |    <receiver_MarketParticipant.marketRole.type>A33</receiver_MarketParticipant.marketRole.type>
      |    <createdDateTime>2022-08-18T16:02:29Z</createdDateTime>
      |    <time_Period.timeInterval>
      |        <start>2022-08-18T00:00Z</start>
      |        <end>2022-08-18T16:00Z</end>
      |    </time_Period.timeInterval>
      |    <TimeSeries>
      |        <mRID>1</mRID>
      |        <businessType>A04</businessType>
      |        <objectAggregation>A01</objectAggregation>
      |        <outBiddingZone_Domain.mRID codingScheme="A01">10Y1001A1001A83F</outBiddingZone_Domain.mRID>
      |        <quantity_Measure_Unit.name>MAW</quantity_Measure_Unit.name>
      |        <curveType>A01</curveType>
      |        <Period>
      |            <timeInterval>
      |                <start>2022-08-18T00:00Z</start>
      |                <end>2022-08-18T13:30Z</end>
      |            </timeInterval>
      |            <resolution>PT15M</resolution>
      |            <Point>
      |                <position>1</position>
      |                <quantity>43986</quantity>
      |            </Point>
      |            <Point>
      |                <position>2</position>
      |                <quantity>43898</quantity>
      |            </Point>
      |            <Point>
      |                <position>3</position>
      |                <quantity>43963</quantity>
      |            </Point>
      |            <Point>
      |                <position>4</position>
      |                <quantity>43872</quantity>
      |            </Point>
      |            <Point>
      |                <position>5</position>
      |                <quantity>43675</quantity>
      |            </Point>
      |            <Point>
      |                <position>6</position>
      |                <quantity>43632</quantity>
      |            </Point>
      |            <Point>
      |                <position>7</position>
      |                <quantity>43635</quantity>
      |            </Point>
      |            <Point>
      |                <position>8</position>
      |                <quantity>43483</quantity>
      |            </Point>
      |            <Point>
      |                <position>9</position>
      |                <quantity>43681</quantity>
      |            </Point>
      |            <Point>
      |                <position>10</position>
      |                <quantity>43649</quantity>
      |            </Point>
      |            <Point>
      |                <position>11</position>
      |                <quantity>44071</quantity>
      |            </Point>
      |            <Point>
      |                <position>12</position>
      |                <quantity>44375</quantity>
      |            </Point>
      |            <Point>
      |                <position>13</position>
      |                <quantity>45768</quantity>
      |            </Point>
      |            <Point>
      |                <position>14</position>
      |                <quantity>46282</quantity>
      |            </Point>
      |            <Point>
      |                <position>15</position>
      |                <quantity>47474</quantity>
      |            </Point>
      |            <Point>
      |                <position>16</position>
      |                <quantity>48120</quantity>
      |            </Point>
      |            <Point>
      |                <position>17</position>
      |                <quantity>50764</quantity>
      |            </Point>
      |            <Point>
      |                <position>18</position>
      |                <quantity>51864</quantity>
      |            </Point>
      |            <Point>
      |                <position>19</position>
      |                <quantity>52586</quantity>
      |            </Point>
      |            <Point>
      |                <position>20</position>
      |                <quantity>53743</quantity>
      |            </Point>
      |            <Point>
      |                <position>21</position>
      |                <quantity>55484</quantity>
      |            </Point>
      |            <Point>
      |                <position>22</position>
      |                <quantity>56457</quantity>
      |            </Point>
      |            <Point>
      |                <position>23</position>
      |                <quantity>57481</quantity>
      |            </Point>
      |            <Point>
      |                <position>24</position>
      |                <quantity>58546</quantity>
      |            </Point>
      |            <Point>
      |                <position>25</position>
      |                <quantity>59170</quantity>
      |            </Point>
      |            <Point>
      |                <position>26</position>
      |                <quantity>59565</quantity>
      |            </Point>
      |            <Point>
      |                <position>27</position>
      |                <quantity>60205</quantity>
      |            </Point>
      |            <Point>
      |                <position>28</position>
      |                <quantity>60485</quantity>
      |            </Point>
      |            <Point>
      |                <position>29</position>
      |                <quantity>60946</quantity>
      |            </Point>
      |            <Point>
      |                <position>30</position>
      |                <quantity>61280</quantity>
      |            </Point>
      |            <Point>
      |                <position>31</position>
      |                <quantity>62402</quantity>
      |            </Point>
      |            <Point>
      |                <position>32</position>
      |                <quantity>62905</quantity>
      |            </Point>
      |            <Point>
      |                <position>33</position>
      |                <quantity>63061</quantity>
      |            </Point>
      |            <Point>
      |                <position>34</position>
      |                <quantity>63023</quantity>
      |            </Point>
      |            <Point>
      |                <position>35</position>
      |                <quantity>63203</quantity>
      |            </Point>
      |            <Point>
      |                <position>36</position>
      |                <quantity>63282</quantity>
      |            </Point>
      |            <Point>
      |                <position>37</position>
      |                <quantity>63973</quantity>
      |            </Point>
      |            <Point>
      |                <position>38</position>
      |                <quantity>64611</quantity>
      |            </Point>
      |            <Point>
      |                <position>39</position>
      |                <quantity>64752</quantity>
      |            </Point>
      |            <Point>
      |                <position>40</position>
      |                <quantity>65333</quantity>
      |            </Point>
      |            <Point>
      |                <position>41</position>
      |                <quantity>65140</quantity>
      |            </Point>
      |            <Point>
      |                <position>42</position>
      |                <quantity>65570</quantity>
      |            </Point>
      |            <Point>
      |                <position>43</position>
      |                <quantity>64533</quantity>
      |            </Point>
      |            <Point>
      |                <position>44</position>
      |                <quantity>65492</quantity>
      |            </Point>
      |            <Point>
      |                <position>45</position>
      |                <quantity>65222</quantity>
      |            </Point>
      |            <Point>
      |                <position>46</position>
      |                <quantity>64829</quantity>
      |            </Point>
      |            <Point>
      |                <position>47</position>
      |                <quantity>64877</quantity>
      |            </Point>
      |            <Point>
      |                <position>48</position>
      |                <quantity>64076</quantity>
      |            </Point>
      |            <Point>
      |                <position>49</position>
      |                <quantity>63547</quantity>
      |            </Point>
      |            <Point>
      |                <position>50</position>
      |                <quantity>62846</quantity>
      |            </Point>
      |            <Point>
      |                <position>51</position>
      |                <quantity>62679</quantity>
      |            </Point>
      |            <Point>
      |                <position>52</position>
      |                <quantity>61928</quantity>
      |            </Point>
      |            <Point>
      |                <position>53</position>
      |                <quantity>62564</quantity>
      |            </Point>
      |            <Point>
      |                <position>54</position>
      |                <quantity>61920</quantity>
      |            </Point>
      |        </Period>
      |    </TimeSeries>
      |    <TimeSeries>
      |        <mRID>2</mRID>
      |        <businessType>A04</businessType>
      |        <objectAggregation>A01</objectAggregation>
      |        <outBiddingZone_Domain.mRID codingScheme="A01">10Y1001A1001A83F</outBiddingZone_Domain.mRID>
      |        <quantity_Measure_Unit.name>MAW</quantity_Measure_Unit.name>
      |        <curveType>A01</curveType>
      |        <Period>
      |            <timeInterval>
      |                <start>2022-08-18T13:45Z</start>
      |                <end>2022-08-18T15:15Z</end>
      |            </timeInterval>
      |            <resolution>PT15M</resolution>
      |            <Point>
      |                <position>1</position>
      |                <quantity>60031</quantity>
      |            </Point>
      |            <Point>
      |                <position>2</position>
      |                <quantity>59762</quantity>
      |            </Point>
      |            <Point>
      |                <position>3</position>
      |                <quantity>59748</quantity>
      |            </Point>
      |            <Point>
      |                <position>4</position>
      |                <quantity>59441</quantity>
      |            </Point>
      |            <Point>
      |                <position>5</position>
      |                <quantity>59559</quantity>
      |            </Point>
      |            <Point>
      |                <position>6</position>
      |                <quantity>60022</quantity>
      |            </Point>
      |        </Period>
      |    </TimeSeries>
      |</GL_MarketDocument>
      |""".stripMargin

  def main(args:Array[String]):Unit = {
    getSeries(loadSample)
  }

  def getSeries(document:String):EntsoeSeries = {
    val xml = XML.loadString(document)
    getTimeSeries(xml)
  }

  /**
   * A public method to retrieve the complete
   * time period of the XML document
   */
  def getTimePeriod(xml:Elem):(Long,Long) = {
    /*
     * Extract start and end time
     *
     * Sample:
     *
     * <time_Period.timeInterval>
     *   <start>2022-08-18T00:00Z</start>
     *   <end>2022-08-18T16:00Z</end>
     * </time_Period.timeInterval>
     *
     */

    val timePeriod = xml \\ "time_Period.timeInterval"

    val startTime = (timePeriod \\ "start").head.text.replace("Z", ":00Z")
    val endTime   = (timePeriod \\ "end").head.text.replace("Z", ":00Z")

    val startMillis = Instant.parse(startTime).toEpochMilli
    val endMillis   = Instant.parse(endTime).toEpochMilli

    (startMillis, endMillis)

  }
  /**
   * Public method to retrieve all TimeSeries from
   * the XML document and merge them into a single
   * timeseries result.
   *
   * Intermediate missing values are added.
   */
  def getTimeSeries(xml:Elem):EntsoeSeries = {
    /*
     * The ENTSOE API provides more than one timeseries
     * if an intermediate value cannot be provided.
     *
     * This must be taken into account before concatenating
     * the respective timeseries
     */
    val timeseriesList = (xml \\ "TimeSeries").map(n => {

      /* Extract unit of measure */
      val unitOfMeasure = (n \ "quantity_Measure_Unit.name").head.text

      /* Extract `Period` */
      val period = (n \ "Period").head

      /* Extract Time Interval */
      val timeInterval = (period \ "timeInterval").head

      val startInstant = Instant.parse(
        (timeInterval \\ "start").head.text.replace("Z", ":00Z"))

      val endInstant = Instant.parse(
        (timeInterval \\ "end").head.text.replace("Z", ":00Z"))

      /* Extract resolution */
      val resolution = (period \ "resolution").head.text

      val startTime = startInstant.toEpochMilli
      val endTime   = endInstant.toEpochMilli

      val intervals = buildIntervals(startTime, endTime, resolution)

      /* Extract points */
      val points = (period \ "Point")
        .map(p => {
          /* Position counting starts with `1`*/
          val position = (p \ "position").head.text.toInt
          val quantity = (p \ "quantity").head.text.toInt

          (position, quantity)
        })
        .sortBy{case(position, _) => position}

      val values = points.map{case(_, quantity) => quantity}
      val dots = intervals.zip(values)
        .map{case((start, end), quantity) => EntsoeDot(start, end, quantity)}

      val timeseries = EntsoeSeries(
        startTs = startTime, endTs = endTime,
        resolution = resolution, unitOfMeasure = unitOfMeasure, dots = dots)

      timeseries

    })

    /*
     * Concatenate multiple timeseries
     */
    val len = timeseriesList.length
    if (len == 1)
      timeseriesList.head

    else {
      /*
       * Initialize final timeseries
       */
      val resolution    = timeseriesList.head.resolution
      val unitOfMeasure = timeseriesList.head.unitOfMeasure

      val startTs = timeseriesList.head.startTs
      val endTs   = timeseriesList.last.endTs

      var dots = Seq.empty[EntsoeDot]
      var timeseries = EntsoeSeries(
        startTs, endTs, resolution, unitOfMeasure, dots)

      var i = 0
      while (i < len) {
        /*
         * Append current timeseries to the final one
         */
        val curr = timeseriesList(i)
        if (resolution == curr.resolution && unitOfMeasure == curr.unitOfMeasure) {
          dots = dots ++ curr.dots
        }
        /*
         * Check whether an intermediate timeseries
         * must be added
         */
        if (i + 1 < len) {
          val next = timeseriesList(i+1)
          if (curr.endTs != next.startTs) {
            /*
             * There are intermediate values missing
             * and these are added to the final series
             */
            val intervals = buildIntervals(curr.endTs, next.startTs, resolution)
            val intermediate = intervals
              .map{case(startTs, endTs) =>
                EntsoeDot(startTs, endTs, Int.MinValue)
              }

            dots = dots ++ intermediate

          }
        }

        i += 1
      }

      val timeseriesS = timeseries.copy(dots = dots)
      timeseriesS

    }
  }

  private def buildIntervals(startTime:Long,endTime:Long, resolution:String):Seq[(Long,Long)] = {
    /*
      * Build timestamp series
      */
    val cal = Calendar.getInstance()
    cal.setTimeInMillis(startTime)

    val ts = mutable.ArrayBuffer.empty[Long]

    var time = startTime
    while (time < endTime) {

      ts += time
      resolution match {
        case "PT15M" =>
          cal.add(Calendar.MINUTE, 15)
        case _ =>
          throw new Exception(s"Resolution not supported.")
      }

      time = cal.getTimeInMillis

    }

    ts += endTime
    /*
     * ENT-SOE specifies quantities in a time
     * interval
     */
    val intervals = ts.zip(ts.tail)
    intervals
  }

}

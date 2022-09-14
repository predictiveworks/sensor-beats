package de.kp.works.beats.sensor.entsog

/**
 * Any API account that exceeds 60 API calls per minute
 * will automatically be put in a waiting queue and get
 * a 'too many requests' error message for a duration of
 * 60 seconds, each time.
 *
 * We will also monitor the accounts in this queue and if
 * data calls are not adjusted properly and timely, we will
 * affect a permanent ban on these accounts, by placing these
 * accounts on our blacklist as 'DDOS attack' users.
 */
object GIELimiter {
  /**
   * Timestamp in milliseconds of the last request
   */
  private var lastReq:Long = 0L
  /**
   * The number of requests within the last
   * minute
   */
  private var numReq:Int = 0
  /**
   * The minimum time delay defined by the
   * limiter
   */
  private val MIN_DELAY_MS:Long = 250
  private val MINUTE_MS:Long = 60 * 1000

  private val MAX_REQ:Int = 60

  def getDelay:Long = {

    val now = System.currentTimeMillis()
    if (lastReq == 0L) {
      /*
       * The initial (or first) request
       */
      lastReq = now
      numReq  = 1

      MIN_DELAY_MS

    } else {
      /*
       * The remaining milli seconds
       * until the next minute begins
       */
      val diff_ms = now - lastReq
      /*
       * Check whether this request falls
       * within the last minute
       */
      if (diff_ms > MINUTE_MS) {
        lastReq = now
        numReq = 1
        /*
         * The first request within this
         * new time window retrieves the
         * minimum delay
         */
        MIN_DELAY_MS
      }
      else {
        /*
         * Another request within the last
         * minute window
         */
        numReq += 1
        if (numReq >= MAX_REQ) {
          /*
           * Delay request until the next
           * minute window begins
           */
          var delay = (MINUTE_MS - diff_ms) + 100
          if (delay > MINUTE_MS) delay = MINUTE_MS

          delay

        } else {

          val diff_req = MAX_REQ - numReq

          val delay = (MINUTE_MS.toDouble / diff_req).toLong
          delay

        }
      }
    }

  }
}

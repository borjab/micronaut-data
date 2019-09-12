package io.micronaut.data.runtime.intercept

import io.micronaut.core.convert.ConversionService
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DataInitializerSpec extends Specification {

    static def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

    // All this dates represent the same instant: just 1 day after UNIX Epoch time
    static def MILLISECONDS = 24 * 60 * 60 * 1000L
    static def DATE = new Date(MILLISECONDS)
    static def SQL_DATE = new java.sql.Date(MILLISECONDS)
    static def INSTANT = Instant.ofEpochMilli(MILLISECONDS)
    static def TIMESTAMP = new Timestamp(MILLISECONDS)
    static def LOCAL_DATE = LocalDate.of(1970, 1, 2)
    static def ZONED_DATE_TIME = LOCAL_DATE
            .atTime(LocalTime.of(2, 0))
            .atZone(ZoneId.of("Etc/GMT-2"))
    static def OFFSET_DATE_TIME = LOCAL_DATE
            .atTime(LocalTime.of(2, 0))
            .atOffset(ZoneOffset.of("+02:00"))

    // This fields depend on the defaults configured in the locale
    static def DEFAULT_DATE = DATE_FORMAT.parse("1970-01-02")
    static def DEFAULT_ZONE = ZoneOffset.systemDefault()

    @Unroll
    def "test date conversion #obj to #targetType"() {
        given:
        new DataInitializer()
        ConversionService<?> conversionService = ConversionService.SHARED

        when:
        def expectedValue = conversionService.convert(obj, targetType)
        then:
        result == expectedValue.get()
        where:
        obj                       || targetType     || result
        DEFAULT_DATE              || LocalDate      || LOCAL_DATE
        DEFAULT_DATE              || LocalDateTime  || LOCAL_DATE.atStartOfDay()
        DEFAULT_DATE              || ZonedDateTime  || LOCAL_DATE.atStartOfDay().atZone(DEFAULT_ZONE)
        DEFAULT_DATE              || OffsetDateTime || LOCAL_DATE.atStartOfDay().atZone(DEFAULT_ZONE).toOffsetDateTime()
        LOCAL_DATE                || Date           || DEFAULT_DATE
        LOCAL_DATE.atStartOfDay() || Date           || DEFAULT_DATE
        DATE                      || Instant        || INSTANT
        INSTANT                   || Date           || DATE
        TIMESTAMP                 || Instant        || INSTANT
        OFFSET_DATE_TIME          || java.sql.Date  || SQL_DATE
        OFFSET_DATE_TIME          || Date           || DATE
        OFFSET_DATE_TIME          || Long           || MILLISECONDS
        OFFSET_DATE_TIME          || Timestamp      || TIMESTAMP
        OFFSET_DATE_TIME          || LocalDateTime  || LOCAL_DATE.atTime( LocalTime.of(2,0))
        ZONED_DATE_TIME           || java.sql.Date  || SQL_DATE
        ZONED_DATE_TIME           || Date           || DATE
        ZONED_DATE_TIME           || Long           || MILLISECONDS
        ZONED_DATE_TIME           || Timestamp      || TIMESTAMP
        ZONED_DATE_TIME           || LocalDateTime  || LOCAL_DATE.atTime( LocalTime.of(2,0))
    }

    // TODO: add test for all permutations
    // TODO: add tests nanoseconds  Instant -> Timestamp
}
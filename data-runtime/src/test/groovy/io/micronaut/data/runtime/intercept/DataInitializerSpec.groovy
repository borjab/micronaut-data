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

class DataInitializerSpec extends Specification {

    static def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

    // All this dates represent just 1 day after UNIX Epoch time
    static def TEST_MILLISECONDS = 24 * 60 * 60 * 1000L
    static def TEST_DATE = new Date(TEST_MILLISECONDS)
    static def TEST_SQL_DATE = new java.sql.Date(TEST_MILLISECONDS)
    static def TEST_INSTANT = Instant.ofEpochMilli(TEST_MILLISECONDS)
    static def TEST_TIMESTAMP = new Timestamp(TEST_MILLISECONDS)
    static def TEST_LOCAL_DATE = LocalDate.of(1970, 1, 2)
    static def TEST_OFFSET_DATE_TIME = TEST_LOCAL_DATE.atTime(LocalTime.of(2, 0))
            .atOffset(ZoneOffset.of("+02:00"))

    // This date will be built according with the default timezone depending on the locale
    static def TEST_DATE_AT_LOCAL_ZONE = DATE_FORMAT.parse("1970-01-02")

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
            obj                            || targetType     || result
            TEST_DATE_AT_LOCAL_ZONE        || LocalDate      || TEST_LOCAL_DATE
            TEST_DATE_AT_LOCAL_ZONE        || LocalDateTime  || TEST_LOCAL_DATE.atStartOfDay()
            TEST_DATE_AT_LOCAL_ZONE        || OffsetDateTime || TEST_LOCAL_DATE.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()
            TEST_LOCAL_DATE                || Date           || TEST_DATE_AT_LOCAL_ZONE
            TEST_LOCAL_DATE.atStartOfDay() || Date           || TEST_DATE_AT_LOCAL_ZONE
            TEST_DATE                      || Instant        || TEST_INSTANT
            TEST_INSTANT                   || Date           || TEST_DATE
            TEST_OFFSET_DATE_TIME          || java.sql.Date  || TEST_SQL_DATE
            TEST_OFFSET_DATE_TIME          || Date           || TEST_DATE
            TEST_OFFSET_DATE_TIME          || Long           || TEST_MILLISECONDS
            TEST_OFFSET_DATE_TIME          || Timestamp      || TEST_TIMESTAMP
            TEST_OFFSET_DATE_TIME          || LocalDateTime  || TEST_LOCAL_DATE.atTime( LocalTime.of(2,0))
    }
}
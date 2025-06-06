package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    private static final LocalDateTime START = LocalDateTime.of(2023, 10, 5, 12, 0);
    private static final LocalDateTime END = START.plusDays(1);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerialize() throws IOException {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(START)
                .end(END)
                .bookerId(1L)
                .itemId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(START.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(END.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\":1,\"start\":\"2023-10-05T12:00:00\",\"end\":\"2023-10-06T12:00:00\"," +
                "\"itemId\":2,\"bookerId\":1,\"status\":\"APPROVED\"}";

        BookingDto result = json.parse(jsonContent).getObject();

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("start", START)
                .hasFieldOrPropertyWithValue("end", END)
                .hasFieldOrPropertyWithValue("itemId", 2L)
                .hasFieldOrPropertyWithValue("bookerId", 1L)
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
    }
}

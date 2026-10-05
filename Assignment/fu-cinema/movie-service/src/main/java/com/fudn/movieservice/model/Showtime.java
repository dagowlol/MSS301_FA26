package com.fudn.movieservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.*;

@Document(collection = "showtimes")
@CompoundIndex(name = "room_start_idx", def = "{'roomId': 1, 'startTime': 1}")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Showtime {
    @Id private String showtimeId;
    @Indexed private String movieId;
    private String roomId;
    private LocalDateTime startTime, endTime;
    @Field(targetType = FieldType.DECIMAL128) private BigDecimal ticketPrice;
    private ShowtimeStatus showtimeStatus;
}

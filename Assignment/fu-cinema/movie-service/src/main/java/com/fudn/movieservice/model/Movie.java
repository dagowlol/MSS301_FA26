package com.fudn.movieservice.model;

import java.time.LocalDate;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Movie {
    @Id private String movieId;
    private String title, description, director, language;
    private Integer durationMinutes;
    private AgeRating ageRating;
    private LocalDate releaseDate;
    @Indexed private String genreId;
    private MovieStatus movieStatus;
}

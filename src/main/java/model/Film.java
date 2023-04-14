package model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Film {

    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private LocalTime duration;

}

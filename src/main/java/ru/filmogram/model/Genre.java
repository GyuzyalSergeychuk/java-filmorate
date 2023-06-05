package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {

    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

package ru.filmogram.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mpa {

    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Mpa{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

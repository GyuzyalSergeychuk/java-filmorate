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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre that = (Genre) o;
        return id.equals(that.id);
    }
}

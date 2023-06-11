package ru.filmogram.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.filmogram.exceptions.ObjectNotFoundException;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.MpaStorage;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class MpaDbStorageImpl implements MpaStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpaId(Long id) {

        Mpa mpa = null;

        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(
                "SELECT rating_id, " +
                        "rating_name " +
                        "FROM rating " +
                        "WHERE rating_id = ?", id);
        if (mpaRows.next()) {
            mpa = Mpa.builder()
                    .id(mpaRows.getLong("rating_id"))
                    .name(mpaRows.getString("rating_name"))
                    .build();
        } else {
            throw new ObjectNotFoundException(String.format("Mpa {} не найден", id));
        }
        return mpa;
    }

    @Override
    public List<Mpa> findAllMpa() {
        ArrayList<Mpa> mpas = new ArrayList<>();

        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(
                "SELECT rating_id, " +
                        "rating_name " +
                        "FROM rating ");
        while (mpaRows.next()) {
            Mpa mpa = Mpa.builder()
                    .id(mpaRows.getLong("rating_id"))
                    .name(mpaRows.getString("rating_name"))
                    .build();
            mpas.add(mpa);
        }
        return mpas;
    }
}

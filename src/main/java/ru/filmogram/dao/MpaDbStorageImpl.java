package ru.filmogram.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.filmogram.model.Mpa;
import ru.filmogram.storage.film.MpaStorage;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MpaDbStorageImpl implements MpaStorage {

    private Logger log = LoggerFactory.getLogger(getClass());

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
                        "name " +
                        "FROM rating " +
                        "WHERE rating_id = ?", id);
        if (mpaRows.next()) {
            mpa = Mpa.builder()
                    .id(mpaRows.getLong("rating_id"))
                    .name(mpaRows.getString("name"))
                    .build();
        }
        return mpa;
    }

    @Override
    public List<Mpa> findAllMpa() {
        ArrayList<Mpa> mpas = new ArrayList<>();

        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(
                "SELECT rating_id, " +
                        "name " +
                        "FROM rating ");
        while (mpaRows.next()) {
            Mpa mpa = Mpa.builder()
                    .id(mpaRows.getLong("rating_id"))
                    .name(mpaRows.getString("name"))
                    .build();
            mpas.add(mpa);
        }
        return mpas;
    }
}

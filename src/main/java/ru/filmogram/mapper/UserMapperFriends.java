package ru.filmogram.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.filmogram.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMapperFriends implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {

        return User.builder()
                .id(rs.getLong("user_id"))
                .name(rs.getString("user_name"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .birthday(LocalDate.parse(rs.getString("birthday")))
                .friends(Stream.of(rs.getString("friendsUser").split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet()))
                .build();
    }
}

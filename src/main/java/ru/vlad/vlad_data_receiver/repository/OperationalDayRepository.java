package ru.vlad.vlad_data_receiver.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OperationalDayRepository {

    private final JdbcTemplate jdbcTemplate;

    public int countDaysInPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM vlad_db.operational_day WHERE date BETWEEN ? AND ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, start, end);
        return count != null ? count : 0;
    }

    public void deleteDaysInPeriod(LocalDate start, LocalDate end) {
        String sql = "DELETE FROM vlad_db.operational_day WHERE date BETWEEN ? AND ?";
        jdbcTemplate.update(sql, start, end);
    }

    public void saveAll(List<LocalDate> dates, int stateId) {
        String sql = "INSERT INTO vlad_db.operational_day (date, state_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, dates.get(i));
                ps.setInt(2, stateId);
            }

            @Override
            public int getBatchSize() {
                return dates.size();
            }
        });
    }
}
package org.shanoir.ng.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.shanoir.ng.examination.model.Examination;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ExaminationReader extends JdbcCursorItemReader<Examination> implements ItemReader<Examination> {

	public ExaminationReader(@Autowired DataSource dataSource) {
		setDataSource(dataSource);
		setSql("SELECT id, comment FROM examination");
		setFetchSize(100);
		setRowMapper(new ExaminationRowMapper());
	}

	public class ExaminationRowMapper implements RowMapper<Examination> {
		@Override
		public Examination mapRow(ResultSet rs, int rowNum) throws SQLException {
			Examination examination = new Examination();
			examination.setId(rs.getLong("id"));
			examination.setComment(rs.getString("comment"));
			return examination;
		}
	}

}

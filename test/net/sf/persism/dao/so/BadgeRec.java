package net.sf.persism.dao.so;

import net.sf.persism.annotations.Table;

import java.sql.Timestamp;
@Table("Badges")
public record BadgeRec(Integer id, String name, Integer userId, Timestamp date) {
}

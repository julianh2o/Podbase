# --- !Ups
ALTER TABLE ImageAttribute MODIFY attribute char(60);
ALTER TABLE ImageAttribute MODIFY value char(255);

# --- !Downs
ALTER TABLE ImageAttribute MODIFY attribute varchar(255);
ALTER TABLE ImageAttribute MODIFY value varchar(255);

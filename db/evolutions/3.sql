# --- !Ups
ALTER TABLE ImageAttribute MODIFY value text;

# --- !Downs
ALTER TABLE ImageAttribute MODIFY value char(255);

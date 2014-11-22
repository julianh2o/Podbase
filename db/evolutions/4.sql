# --- !Ups
ALTER TABLE User ADD lastActive datetime;

# --- !Downs
ALTER TABLE User DROP COLUMN lastActive;

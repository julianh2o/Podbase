# --- !Ups
   create table AttributeHistoryItem (
        id bigint not null auto_increment,
        created datetime,
        modified datetime,
        newValue text,
        note text,
        previousValue text,
        attribute_id bigint,
        user_id bigint,
        primary key (id)
    );

    alter table AttributeHistoryItem
        add index FKB91021AB47140EFE (user_id),
        add constraint FKB91021AB47140EFE
        foreign key (user_id)
        references User (id);

    alter table AttributeHistoryItem
        add index FKB91021AB96FE0143 (attribute_id),
        add constraint FKB91021AB96FE0143
        foreign key (attribute_id)
        references ImageAttribute (id);

# --- !Downs
DROP TABLE AttributeHistoryItem

# --- !Ups
create table Activation (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    activationCode varchar(255),
    expirationDate datetime,
    primary key (id)
);

create table DatabaseImage (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    hash varchar(255),
    imported bit not null,
    path varchar(255),
    primary key (id)
);

create table Directory (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    path varchar(255),
    project_id bigint,
    primary key (id)
);

create table ImageAttribute (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    attribute varchar(255),
    data bit not null,
    hidden bit not null,
    ordering integer not null,
    value varchar(255),
    image_id bigint,
    linkedAttribute_id bigint,
    project_id bigint,
    primary key (id)
);

create table ImageSet (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    name varchar(255),
    paper_id bigint,
    primary key (id)
);

create table ImageSetMembership (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    image_id bigint,
    imageset_id bigint,
    primary key (id)
);

create table Permission (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    access integer,
    model_id bigint,
    user_id bigint,
    primary key (id)
);

create table PermissionedModel (
    DTYPE varchar(31) not null,
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    dataMode bit,
    name varchar(255),
    imageset_id bigint,
    primary key (id)
);

create table ProjectVisibleImage (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    image_id bigint,
    project_id bigint,
    primary key (id)
);

create table Template (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    name varchar(255),
    project_id bigint,
    primary key (id)
);

create table TemplateAssignment (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    path varchar(255),
    project_id bigint,
    template_id bigint,
    primary key (id)
);

create table TemplateAttribute (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    description varchar(255),
    hidden bit not null,
    name varchar(255),
    sort integer,
    type varchar(255),
    template_id bigint,
    primary key (id)
);

create table User (
    id bigint not null auto_increment,
    created datetime,
    modified datetime,
    email varchar(255),
    password varchar(255),
    special bit not null,
    activation_id bigint,
    primary key (id)
);

alter table Directory 
    add index FK3E123E4D856AF776 (project_id), 
    add constraint FK3E123E4D856AF776 
    foreign key (project_id) 
    references PermissionedModel (id);

alter table ImageAttribute 
    add index FKA18F43A13196367B (image_id), 
    add constraint FKA18F43A13196367B 
    foreign key (image_id) 
    references DatabaseImage (id);

alter table ImageAttribute 
    add index FKA18F43A1856AF776 (project_id), 
    add constraint FKA18F43A1856AF776 
    foreign key (project_id) 
    references PermissionedModel (id);

alter table ImageAttribute 
    add index FKA18F43A15D4749DC (linkedAttribute_id), 
    add constraint FKA18F43A15D4749DC 
    foreign key (linkedAttribute_id) 
    references ImageAttribute (id);

alter table ImageSet 
    add index FKD09DBD27ED9FE596 (paper_id), 
    add constraint FKD09DBD27ED9FE596 
    foreign key (paper_id) 
    references PermissionedModel (id);

alter table ImageSetMembership 
    add index FK1E1C1C5D3196367B (image_id), 
    add constraint FK1E1C1C5D3196367B 
    foreign key (image_id) 
    references DatabaseImage (id);

alter table ImageSetMembership 
    add index FK1E1C1C5D1FB0175E (imageset_id), 
    add constraint FK1E1C1C5D1FB0175E 
    foreign key (imageset_id) 
    references ImageSet (id);

alter table Permission 
    add index FK57F7A1EF47140EFE (user_id), 
    add constraint FK57F7A1EF47140EFE 
    foreign key (user_id) 
    references User (id);

alter table Permission 
    add index FK57F7A1EF8616BAD9 (model_id), 
    add constraint FK57F7A1EF8616BAD9 
    foreign key (model_id) 
    references PermissionedModel (id);

alter table Permission 
    add index FK57F7A1EFD5F08D66 (model_id), 
    add constraint FK57F7A1EFD5F08D66 
    foreign key (model_id) 
    references PermissionedModel (id);

alter table Permission 
    add index FK57F7A1EF80676C68 (model_id), 
    add constraint FK57F7A1EF80676C68 
    foreign key (model_id) 
    references PermissionedModel (id);

alter table PermissionedModel 
    add index FK3A852CFB1FB0175E (imageset_id), 
    add constraint FK3A852CFB1FB0175E 
    foreign key (imageset_id) 
    references ImageSet (id);

alter table ProjectVisibleImage 
    add index FK866C9A23196367B (image_id), 
    add constraint FK866C9A23196367B 
    foreign key (image_id) 
    references DatabaseImage (id);

alter table ProjectVisibleImage 
    add index FK866C9A2856AF776 (project_id), 
    add constraint FK866C9A2856AF776 
    foreign key (project_id) 
    references PermissionedModel (id);

alter table Template 
    add index FKB515309A856AF776 (project_id), 
    add constraint FKB515309A856AF776 
    foreign key (project_id) 
    references PermissionedModel (id);

alter table TemplateAssignment 
    add index FK45EA1B07856AF776 (project_id), 
    add constraint FK45EA1B07856AF776 
    foreign key (project_id) 
    references PermissionedModel (id);

alter table TemplateAssignment 
    add index FK45EA1B07B6306A9E (template_id), 
    add constraint FK45EA1B07B6306A9E 
    foreign key (template_id) 
    references Template (id);

alter table TemplateAttribute 
    add index FK6428A722B6306A9E (template_id), 
    add constraint FK6428A722B6306A9E 
    foreign key (template_id) 
    references Template (id);

alter table User 
    add index FK285FEB8B369C1E (activation_id), 
    add constraint FK285FEB8B369C1E 
    foreign key (activation_id) 
    references Activation (id);


# --- !Downs
DROP DATABASE

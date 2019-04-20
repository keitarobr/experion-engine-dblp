create table author
(
	id serial not null
		constraint author_pkey
			primary key,
	name varchar(200) not null
		constraint author_name_key
			unique
)
;

alter table author owner to dblp
;

create index idx_author_name
	on author (name)
;

create table author_map
(
	id serial not null
		constraint author_map_pkey
			primary key,
	name varchar(512)
		constraint author_map_name_key
			unique
)
;

alter table author_map owner to dblp
;

create table author_map_author
(
	author_map_id integer not null,
	author_id integer not null,
	constraint author_map_author_pkey
		primary key (author_map_id, author_id)
)
;

alter table author_map_author owner to dblp
;

create table document
(
	id serial not null
		constraint document_pkey
			primary key,
	title varchar(2048),
	year varchar(10),
	ee varchar(2048),
	key varchar(255) not null
		constraint document_key_key
			unique,
	type varchar(20) not null
)
;

alter table document owner to dblp
;

create table author_document
(
	document_id integer not null
		constraint fk_author_document_document
			references document,
	author_id integer not null
		constraint fk_author_document_author
			references author,
	constraint author_document_pkey
		primary key (document_id, author_id)
)
;

alter table author_document owner to dblp
;

create index idx_author_document_author
	on author_document (author_id)
;


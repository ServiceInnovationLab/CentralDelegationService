create table clients
(
  id bigint not null
    constraint clients_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  access_key varchar(255),
  admin boolean not null,
  crn varchar(255) not null,
  external_id varchar(255),
  privacy_domain varchar(255) not null,
  secret_key varchar(255),
  service varchar(255) not null
)
;

create table delegation_types
(
  id bigint not null
    constraint delegation_types_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  crn varchar(255)
    constraint uk_r5uddd64yd905sle4xignw1dg
    unique,
  metadata_keys varchar(255),
  name varchar(255),
  client_id bigint
    constraint fk6cpt8cefph7j8045f8nmcnpcg
    references clients,
  template_id bigint
)
;

create table delegations
(
  id bigint not null
    constraint delegations_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  crn varchar(255),
  delegate_consent_time timestamp,
  metadata varchar(255),
  owner_consent_time timestamp,
  client_id bigint
    constraint fkjngd4jufy3t8w9btglsefg3fc
    references clients,
  delegate_id integer,
  delegation_type_id bigint not null
    constraint fkb0hvw3wijrqwdwh5s9dj7fol
    references delegation_types,
  owner_id integer,
  rendezvous_id bigint,
  resource_id bigint
)
;

create table hibernate_sequences
(
  sequence_name varchar(255) not null
    constraint hibernate_sequences_pkey
    primary key,
  next_val bigint
)
;

create table policies
(
  id integer not null
    constraint policies_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  external_id varchar(255),
  policy_type integer
)
;

create table rendezvouses
(
  id bigint not null
    constraint rendezvouses_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  crn varchar(255),
  delegate_code varchar(255),
  delegate_code_consumed boolean,
  delegate_email varchar(255),
  owner_code varchar(255),
  owner_code_consumed boolean,
  owner_email varchar(255),
  client_id bigint
    constraint fkpnhduhvl1ig22p4mafnbigpgv
    references clients
)
;

alter table delegations
  add constraint fk7qibahgevqb0nr9a5o1n7nw65
foreign key (rendezvous_id) references rendezvouses
;

create table resources
(
  id bigint not null
    constraint resources_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  crn varchar(255)
    constraint uk_ghu09shk9hn9llyruudxq3gt9
    unique,
  openam_id varchar(255),
  client_id bigint
    constraint fkcrt4ckqeqvsosfexo9r3hrfx7
    references clients,
  user_id integer
)
;

alter table delegations
  add constraint fkabh302e97xv8qc7lbg4f4h4e8
foreign key (resource_id) references resources
;

create table templates
(
  id bigint not null
    constraint templates_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  content text,
  crn varchar(255),
  is_default boolean,
  name varchar(255),
  version integer,
  client_id bigint
    constraint fkqk2e6vp88fpiglhfoxmo91jiu
    references clients
)
;

alter table delegation_types
  add constraint fk88dfcpc9wt8naeftrsts66vg8
foreign key (template_id) references templates
;

create table uma_tokens
(
  id integer not null
    constraint uma_tokens_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  token_type integer,
  token_value varchar(255),
  client_id bigint
    constraint fkgs5xj2h9xhcyv2tn0jp41l5hx
    references clients,
  user_id integer
)
;

create table users
(
  id integer not null
    constraint users_pkey
    primary key,
  create_time timestamp,
  delete_time timestamp,
  update_time timestamp,
  email varchar(255),
  openam_password varchar(255),
  openam_uid varchar(255),
  realme_flt varchar(255),
  uma_pat varchar(255)
)
;

alter table delegations
  add constraint fk9cq04aid0p5mivi9or4idxj3r
foreign key (delegate_id) references users
;

alter table delegations
  add constraint fk3yefbf09dy1b9pifhb9es4w60
foreign key (owner_id) references users
;

alter table resources
  add constraint fkcoba1blh4w96p6n34i4xfoiyp
foreign key (user_id) references users
;

alter table uma_tokens
  add constraint fkgvjkb2hckwvriwpbco80bujwy
foreign key (user_id) references users
;


-- Build test database

-- open: cd test/excavators/odesk/db, mysql -u root -p
-- run: source db.sql;

-- select * from odesk_excavators_log;
-- select * from odesk_found_jobs;
-- select * from odesk_jobs;
-- select * from odesk_jobs_changes;
-- select * from odesk_jobs_applicants;
-- select * from odesk_jobs_hired;
-- select * from odesk_clients_works_history;
-- select * from odesk_found_freelancers;

-- drop database freelance_analytics;


create database freelance_analytics;
use freelance_analytics;

create table odesk_excavators_log(
  id bigint primary key auto_increment,
  create_date timestamp not null,
  name varchar(255) not null,
  msg varchar(6000));

create table odesk_found_jobs(
  id bigint primary key auto_increment,
  o_url varchar(1000) not null,
  found_by varchar(10) not null,
  create_date timestamp not null,
  priority integer not null,
  job_skills varchar(4000) not null,
  n_freelancers integer);

create table odesk_jobs(
  id bigint primary key auto_increment,
  o_url varchar(1000) not null,
  found_by varchar(10) not null,
  found_date timestamp not null,
  create_date timestamp not null,
  post_date timestamp,
  deadline timestamp,
  dae_date timestamp,
  delete_date timestamp,
  next_check_date timestamp,
  n_freelancers integer,
  job_title varchar(1000),
  job_type varchar(10),
  job_payment_type varchar(10) not null,
  job_price float(53),
  job_employment varchar(10) not null,
  job_length varchar(128),
  job_required_level varchar(10) not null,
  job_skills varchar(10) not null,
  job_qualifications varchar(4000) not null,
  job_description varchar(6000));

create table odesk_jobs_changes(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date timestamp not null,
  last_viewed timestamp,
  n_applicants integer,
  applicants_avg float(53),
  rate_min float(53),
  rate_avg float(53),
  rate_max float(53),
  n_interviewing integer,
  interviewing_avg float(53),
  n_hires integer);

create table odesk_clients_changes(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date timestamp not null,
  name varchar(1000),
  logo blob,
  url varchar(1000),
  description varchar(6000),
  payment_method varchar(10),
  rating float(53),
  n_reviews integer,
  location varchar(255),
  time varchar(24),
  n_jobs integer,
  hire_rate integer,
  n_open_jobs integer,
  total_spend integer,
  n_hires integer,
  n_active integer,
  avg_rate float(53),
  hours integer,
  registration_date timestamp);

create table odesk_jobs_applicants(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date timestamp not null,
  up_date timestamp,
  name varchar(255),
  initiated_by varchar(10),
  freelancer_url varchar(1000),
  freelancer_id bigint);

create table odesk_jobs_hired(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date timestamp not null,
  name varchar(255),
  freelancer_url varchar(1000),
  freelancer_id bigint);

create table odesk_clients_works_history(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date timestamp not null,
  o_url varchar(1000),
  title varchar(1000),
  in_progress varchar(10),
  start_date timestamp,
  end_date timestamp,
  payment_type varchar(10),
  billed float(53),
  hours integer,
  rate float(53),
  freelancer_feedback_text varchar(5000),
  freelancer_feedback float(53),
  freelancer_name varchar(255),
  freelancer_url varchar(1000),
  freelancer_id bigint,
  client_feedback float(53));

create table odesk_found_freelancers(
  id bigint primary key auto_increment,
  o_url varchar(1000) not null,
  create_date timestamp not null,
  priority integer);





























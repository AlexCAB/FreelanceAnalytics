-- Build test database

-- open: cd test/excavators/odesk/db, mysql -u root -p
-- run: source db.sql;

-- select * from excavators_log;
-- select * from odesk_found_jobs;
-- select * from odesk_jobs;
-- select * from odesk_jobs_changes;
-- select * from odesk_jobs_applicants;
-- select * from odesk_jobs_hired;
-- select * from odesk_clients_works_history;

-- drop database freelance_analytics;


create database freelance_analytics;
use freelance_analytics;

create table excavators_log(
  id bigint primary key auto_increment,
  create_date date not null,
  name varchar(255) not null,
  msg varchar(6000));

create table odesk_found_jobs(
  id bigint primary key auto_increment,
  o_url varchar(1000) not null,
  found_by varchar(10) not null,
  create_date date not null,
  priority integer,
  job_skills varchar(4000),
  n_freelancers integer);

create table odesk_jobs(
  id bigint primary key auto_increment,
  o_url varchar(1000) not null,
  found_by varchar(10) not null,
  found_date date not null,
  create_date date not null,
  post_date date,
  deadline date,
  dae_date date,
  delete_date date,
  next_check_date date,
  n_freelancers integer,
  job_title varchar(1000),
  job_tipe varchar(10),
  job_payment_type varchar(10),
  job_price float(53),
  job_employment varchar(10),
  jobt_length varchar(128),
  job_required_level varchar(10),
  job_skills varchar(10),
  job_qualifications varchar(4000),
  job_description varchar(6000));

create table odesk_jobs_changes(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date date not null,
  last_viewed date,
  n_applicants integer,
  applicants_avg float(53),
  rate_min float(53),
  rate_avg float(53),
  rate_max float(53),
  n_interviewing integer,
  interviewing_avg float(53),
  n_hires integer,
  client_name varchar(1000),
  client_logo blob,
  client_url varchar(1000),
  client_description varchar(6000),
  client_payment_method varchar(10),
  client_rating float(53),
  client_n_reviews integer,
  client_location varchar(255),
  client_time varchar(24),
  client_n_jobs integer,
  client_hire_rate integer,
  client_n_open_jobs integer,
  client_total_spend integer,
  client_n_hires integer,
  client_n_active integer,
  client_avg_rate float(53),
  client_hours integer,
  client_registration_date date);

create table odesk_jobs_applicants(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date date not null,
  up_date date,
  name varchar(255),
  initiated_by varchar(10),
  freelancer_ur varchar(1000),
  freelancer_id bigint);

create table odesk_jobs_hired(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date date not null,
  name varchar(255),
  freelancer_url varchar(1000),
  freelancer_id bigint);

create table odesk_clients_works_history(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date date not null,
  o_url varchar(1000),
  title varchar(1000),
  in_progress varchar(10),
  start_date date,
  end_date date,
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

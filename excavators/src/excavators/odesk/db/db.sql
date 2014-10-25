-- Build test database

-- open: cd src/excavators/odesk/db, mysql -u root -p
-- run: source db.sql;

-- select * from odesk_new_job_excavator_param;
-- select * from odesk_excavators_log;
-- select * from odesk_excavators_error_pages;
-- select o_url,msg from odesk_excavators_error_pages;
-- select * from odesk_found_jobs;
-- select count(*) from odesk_found_jobs;
-- select * from odesk_jobs;
-- select count(*) from odesk_jobs;
-- select id,post_date,o_url,deadline,job_title,job_type,job_payment_type,job_price,job_employment,job_length,job_required_level,job_qualifications from odesk_jobs;
-- select * from odesk_jobs_changes;
-- select * from odesk_clients_changes;
-- select id,job_id,create_date,name,url,description,payment_method,rating,n_reviews,location,n_jobs,
--   hire_rate,n_open_jobs,total_spend,n_hires,n_active,avg_rate,hours,registration_date from odesk_clients_changes;
-- select * from odesk_jobs_applicants;
-- select * from odesk_jobs_hired;
-- select * from odesk_clients_works_history;
-- select * from odesk_found_freelancers;

-- drop database freelance_analytics;

create database freelance_analytics;
use freelance_analytics;

create table odesk_job_excavators_param(
  id bigint primary key auto_increment,
  p_key varchar(256) not null,
  p_value varchar(1024) not null,
  is_active boolean default false,
  create_date timestamp not null,
  comment varchar(255) default null);

insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('param_need_update',                   'NeedUpdate',                     true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('consoleLoggingLevel',                 'info|debug|worn|error',          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('dbLoggingLevel',                      'info|debug|worn|error',          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('runAfterStart',                       'false',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobSearchURL',                        'https://www.odesk.com/jobs/?q=', true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('foundFreelancersPriority',            '100',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('collectJobsTaskPriority',             '3',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('buildJobsScrapingTaskPriority',       '4',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobsFoundBySearchScrapTaskPriority',  '2',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobsFoundByAnaliseScrapTaskPriority', '1',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobsFoundBySearchPriority',           '100',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobsFoundByAnalisePriority',          '100',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('toTrackingJobPriority',               '1',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('searchNewJobTimeout',                 '3000000',                        true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('buildJobsScrapingTaskTimeout',        '1800000',                        true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('nextJobCheckTimeout',                 '3600000',                        true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('numberOfJobToScripInIteration',       '200',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('maxNumberOfCheckedJob',               '30',                             true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('overloadFoundJobTableRowNumber',      '100000',                         true);
insert into odesk_job_excavators_param(p_key, p_value, is_active,comment) values ('logoImageCoordinates',        '7|7|108|108',                    true, '//x,y,w,h');
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('wornParsingQualityLevel',             '0.8',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('errorParsingQualityLevel',            '0.5',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('notSaveParsingQualityLevel',          '0.2',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('scrapTryMaxNumber',                   '10',                             true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('scrapTryTimeout',                     '5000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadPageMaxTime',                     '60000',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadPageTimeOut',                     '3000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadTryMaxTime',                      '10000',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('confirmTimeOut',                      '2000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('retryTimeOut',                        '1000',                           true);

create table odesk_excavators_log(
  id bigint primary key auto_increment,
  create_date datetime not null,
  type varchar(20) not null,
  name varchar(255) not null,
  msg varchar(6000) default null);

create table odesk_excavators_error_pages(
  id bigint primary key auto_increment,
  create_date datetime not null,
  o_url varchar(255) not null,
  msg varchar(1000) not null,
  html text(60000) not null);

create table odesk_found_jobs(
  id bigint primary key auto_increment,
  o_url varchar(255) not null unique,
  found_by varchar(20) not null,
  create_date datetime not null,
  priority integer not null,
  job_skills varchar(4000) not null,
  n_freelancers integer default null);

create table odesk_jobs(
  id bigint primary key auto_increment,
  o_url varchar(255) not null unique,
  found_by varchar(20) not null,
  found_date datetime not null,
  create_date datetime not null,
  post_date datetime default null,
  deadline datetime default null,
  dae_date datetime default null,
  delete_date datetime default null,
  next_check_date datetime default null,
  n_freelancers integer default null,
  job_title varchar(1000) default null,
  job_type varchar(510) default null,
  job_payment_type varchar(20) not null,
  job_price float(53) default null,
  job_employment varchar(20) not null,
  job_length varchar(128) default null,
  job_required_level varchar(20) not null,
  job_skills varchar(4000) not null,
  job_qualifications varchar(4000) not null,
  job_description mediumtext default null);

create table odesk_jobs_changes(
  id bigint primary key auto_increment,
  job_id bigint not null,
  available varchar(20) not null,
  create_date datetime not null,
  last_viewed datetime default null,
  n_applicants integer default null,
  applicants_avg float(53) default null,
  rate_min float(53) default null,
  rate_avg float(53) default null,
  rate_max float(53) default null,
  n_interviewing integer default null,
  interviewing_avg float(53) default null,
  n_hires integer default null);

create table odesk_clients_changes(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date datetime not null,
  name varchar(1000) default null,
  logo blob default null,
  url varchar(255) default null,
  description varchar(6000) default null,
  payment_method varchar(20) default null,
  rating float(53) default null,
  n_reviews integer default null,
  location varchar(255) default null,
  time varchar(24) default null,
  n_jobs integer default null,
  hire_rate integer default null,
  n_open_jobs integer default null,
  total_spend integer default null,
  n_hires integer default null,
  n_active integer default null,
  avg_rate float(53) default null,
  hours integer default null,
  registration_date datetime default null);

create table odesk_jobs_applicants(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date datetime not null,
  up_date datetime default null,
  name varchar(255) default null,
  initiated_by varchar(20) default null,
  freelancer_url varchar(255) default null,
  freelancer_id bigint default null);

create table odesk_jobs_hired(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date datetime not null,
  name varchar(255) default null,
  freelancer_url varchar(255) default null,
  freelancer_id bigint default null);

create table odesk_clients_works_history(
  id bigint primary key auto_increment,
  job_id bigint not null,
  create_date datetime not null,
  o_url varchar(255) default null,
  title varchar(1000) default null,
  in_progress varchar(20) default null,
  start_date datetime default null,
  end_date datetime default null,
  payment_type varchar(20) default null,
  billed float(53) default null,
  hours integer default null,
  rate float(53) default null,
  freelancer_feedback_text varchar(5000) default null,
  freelancer_feedback float(53) default null,
  freelancer_name varchar(255) default null,
  freelancer_url varchar(255) default null,
  freelancer_id bigint default null,
  client_feedback float(53) default null);

create table odesk_found_freelancers(
  id bigint primary key auto_increment,
  o_url varchar(255) not null,
  create_date datetime not null,
  priority integer not null);





























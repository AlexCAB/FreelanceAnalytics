-- Build test database

-- open: cd src\util\db, mysql -u root -p
-- run: source db.sql;

-- select * from odesk_job_excavators_param;
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


-- drop database freelance_analytics_test;

create database freelance_analytics_test;
use freelance_analytics_test;

create table odesk_job_excavators_param(
  id bigint primary key auto_increment,
  p_key varchar(256) not null,
  p_value varchar(1024) not null,
  is_active boolean default false,
  create_date timestamp not null,
  comment varchar(255) default null);

insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('param_need_update',                   'NeedUpdate',                     true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('excavatorsStates',                    '',                               true);
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
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('numberOfJobToScripInIteration',       '20',                             true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('maxNumberOfCheckedJob',               '30',                             true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('overloadFoundJobTableRowNumber',      '100000',                         true);
insert into odesk_job_excavators_param(p_key, p_value, is_active,comment) values ('logoImageCoordinates',        '7|7|108|108',                    true, '//x,y,w,h');
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('wornParsingQualityLevel',             '0.8',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('errorParsingQualityLevel',            '0.5',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('notSaveParsingQualityLevel',          '0.2',                            true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('scrapTryMaxNumber',                   '10',                             true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('scrapTryTimeout',                     '5000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadPageMaxTime',                     '60000',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadPageTimeOut',                     '4000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('loadTryMaxTime',                      '10000',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('confirmTimeOut',                      '2000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('retryTimeOut',                        '1000',                           true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('jobsFoundBySearchExcavatorNumber',    '1',                              true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('excavatorsManagementTimeout',         '20000',                          true);
insert into odesk_job_excavators_param(p_key, p_value, is_active) values ('buildJobsScrapingTimeout',            '20000',                          true);

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
  payment_method varchar(20) not null,
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
  initiated_by varchar(20) not null,
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
  in_progress varchar(20) not null,
  start_date datetime default null,
  end_date datetime default null,
  payment_type varchar(20) not null,
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

create table odesk_freelancers(
  id bigint primary key auto_increment,
  create_date datetime not null,
  o_url varchar(255) not null unique);

create table odesk_freelancers_raw_html(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  html mediumtext not null);

create table odesk_freelancers_raw_job_json(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  json mediumtext not null);

create table odesk_freelancers_raw_portfolio_json(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  json mediumtext not null);

create table odesk_freelancers_main_change(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  name varchar(500) default null,
  profile_access varchar(100) default null,
  link varchar(500) default null,
  expose_full_name varchar(20) not null,
  role varchar(100) default null,
  video_url varchar(500) default null,
  is_invite_interview_allowed varchar(20) not null,
  location varchar(100) default null,
  time_zone int default null,
  email_verified varchar(20) not null,
  photo blob default null,
  company_url varchar(500) default null,
  company_logo blob default null);

create table odesk_freelancers_additional_change(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  title varchar(200) default null,
  availability varchar(20) not null,
  available_again varchar(100) default null,
  responsiveness_score varchar(100) default null,
  overview varchar(5000) default null,
  languages varchar(4000) not null,
  rate float(53) default null,
  rent_percent int default null,
  rating float(53) default null,
  all_time_jobs int default null,
  all_time_hours int default null,
  skills varchar(4000) not null);

create table odesk_freelancers_work(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  payment_type varchar(20) not null,
  status varchar(20) not null,
  start_date datetime default null,
  end_date datetime default null,
  from_full datetime default null,
  to_full datetime default null,
  opening_title varchar(500) default null,
  engagement_title varchar(500) default null,
  skills varchar(4000) not null,
  open_access varchar(20) not null,
  cny_status varchar(100) default null,
  financial_privacy varchar(100) default null,
  is_hidden varchar(20) not null,
  agency_name varchar(500) default null,
  segmentation_data varchar(4000) not null);

create table odesk_freelancers_work_additional_data(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  work_id bigint not null,
  create_date datetime not null,
  as_type varchar(100) default null,
  total_hours int default null,
  rate float(53) default null,
  total_cost float(53) default null,
  charge_rate float(53) default null,
  amount float(53) default null,
  total_hours_precise float(53) default null,
  cost_rate float(53) default null,
  total_charge float(53) default null,
  job_contractor_tier int default null,
  job_url varchar(500) default null,
  job_description varchar(5000) default null,
  job_category varchar(100) default null,
  job_engagement varchar(100) default null,
  job_duration varchar(100) default null,
  job_amount float(53) default null);

create table odesk_freelancers_work_feedback(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  work_id bigint not null,
  create_date datetime not null,
  ff_scores varchar(4000) not null,
  ff_is_public varchar(20) not null,
  ff_comment varchar(1000) default null,
  ff_private_point int default null,
  ff_reasons varchar(4000) not null,
  ff_response varchar(100) default null,
  ff_score float(53) default null,
  cf_scores varchar(4000) not null,
  cf_is_public varchar(20) not null,
  cf_comment varchar(1000) default null,
  cf_response varchar(1000) default null,
  cf_score float(53) default null);

create table odesk_freelancers_work_linked_project_data(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  work_id bigint not null,
  create_date datetime not null,
  lp_title varchar(1000) default null,
  lp_thumbnail varchar(500) default null,
  lp_is_public varchar(20) not null,
  lp_description varchar(5000) default null,
  lp_recno varchar(100) default null,
  lp_cat_level_1 varchar(100) default null,
  lp_cat_recno varchar(100) default null,
  lp_cat_level_2 varchar(100) default null,
  lp_completed varchar(100) default null,
  lp_large_thumbnail varchar(500) default null,
  lp_url varchar(500) default null,
  lp_project_contract_link_state varchar(100) default null);

create table odesk_freelancers_work_clients(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  work_id bigint not null,
  create_date datetime not null,
  client_total_feedback int default null,
  client_score float(53) default null,
  client_total_charge float(53) default null,
  client_total_hires int default null,
  client_active_contract int default null,
  client_country varchar(100) default null,
  client_city varchar(100) default null,
  client_time varchar(100) default null,
  client_member_since datetime not null,
  client_profile_logo blob default null,
  client_profile_name varchar(500) default null,
  client_profile_url varchar(500) default null,
  client_profile_summary varchar(1000) default null);

create table odesk_freelancers_portfolio(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  project_date datetime not null,
  title varchar(1000) default null,
  description varchar(5000) default null,
  is_public varchar(20) not null,
  attachments varchar(4000) not null,
  creation_ts datetime not null,
  category varchar(100) default null,
  sub_category varchar(100) default null,
  skills varchar(4000) not null,
  is_client varchar(20) not null,
  flag_comment varchar(1000) default null,
  project_url varchar(500) default null,
  img_url blob default null);

create table odesk_freelancers_tests(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  details_url varchar(500) default null,
  title varchar(1000) default null,
  score float(53) default null,
  time_complete int default null);

create table odesk_freelancers_certification(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  details_url varchar(500) default null,
  rid varchar(100) default null,
  name varchar(100) default null,
  custom_data varchar(1000) default null,
  score varchar(100) default null,
  logo_url varchar(500) default null,
  cert_url varchar(500) default null,
  is_cert_verified varchar(20) not null,
  is_verified varchar(20) not null,
  description varchar(5000) default null,
  provider varchar(500) default null,
  skills varchar(4000) not null,
  date_earned varchar(100) default null);

create table odesk_freelancers_employment(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  details_url varchar(500) default null,
  record_id varchar(100) default null,
  title varchar(1000) default null,
  company varchar(1000) default null,
  date_from datetime default null,
  date_to datetime default null,
  role varchar(100) default null,
  company_country varchar(100) default null,
  company_city varchar(100) default null,
  description varchar(5000) default null);

create table odesk_freelancers_education(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  details_url varchar(500) default null,
  school varchar(1000) default null,
  area_of_study varchar(1000) default null,
  degree varchar(100) default null,
  date_from datetime default null,
  date_to datetime default null,
  comments varchar(5000) default null);

create table odesk_freelancers_other_experience(
  id bigint primary key auto_increment,
  freelancer_id bigint not null,
  create_date datetime not null,
  subject varchar(1000) default null,
  description varchar(5000) default null);

















































































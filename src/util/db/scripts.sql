-- Fix bug in 'odesk_jobs_changes', note:
-- onenote:///D:\Progects\Little%20projects\!Notes\Программы.one#%3eFreelance%20analytics&section-id={2EC25AF8-8B10-4445-B54F-6B56D915AC11}&page-id={55C1974A-B9EB-4EF9-973F-DCA773220EBA}&object-id={7A971BCE-4D0C-0C31-3512-E4A5403E77D3}&24
select * from odesk_jobs_changes where n_applicants = applicants_avg and create_date < '2014-10-26 23:59:59';
update odesk_jobs_changes set applicants_avg = null where n_applicants = applicants_avg and create_date < '2014-10-26 23:59:59';

-- Add 'f_key' column to odesk_found_freelancers and odesk_freelancers,then fill
-- onenote:///D:\Progects\Little%20projects\!Notes\Программы.one#%3eFreelance%20analytics&section-id={2EC25AF8-8B10-4445-B54F-6B56D915AC11}&page-id={55C1974A-B9EB-4EF9-973F-DCA773220EBA}&object-id={2770B65D-B0D2-0F6D-1EE3-8ED4CC6FDB7C}&B
alter table odesk_found_freelancers drop index o_url;
alter table odesk_freelancers drop index o_url;
alter table odesk_found_freelancers add f_key varchar(50) not null after o_url;
update odesk_found_freelancers set f_key = substring_index(o_url,'%7E',-1);
alter table odesk_freelancers add f_key varchar(50) not null;
update odesk_freelancers set f_key = substring_index(o_url,'%7E',-1);
alter ignore table odesk_found_freelancers add unique (f_key);
alter ignore table odesk_freelancers add unique (f_key);
delete from odesk_found_freelancers where f_key in (select f_key from odesk_freelancers);




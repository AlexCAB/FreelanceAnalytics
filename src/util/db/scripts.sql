-- Fix bug in 'odesk_jobs_changes', note:
-- onenote:///D:\Progects\Little%20projects\!Notes\Программы.one#%3eFreelance%20analytics&section-id={2EC25AF8-8B10-4445-B54F-6B56D915AC11}&page-id={55C1974A-B9EB-4EF9-973F-DCA773220EBA}&object-id={7A971BCE-4D0C-0C31-3512-E4A5403E77D3}&24

select * from odesk_jobs_changes where n_applicants = applicants_avg and create_date < '2014-10-26 23:59:59';
update odesk_jobs_changes set applicants_avg = null where n_applicants = applicants_avg and create_date < '2014-10-26 23:59:59';
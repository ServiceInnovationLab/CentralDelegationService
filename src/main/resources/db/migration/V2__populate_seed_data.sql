insert into clients(id, create_time, update_time, crn, external_id, privacy_domain, access_key, secret_key, service, admin)
    values(1, now(), now(), 'crn:dia:123', 'ext:987', 'dia', 'access123','secret456', 'delegations', true);
insert into clients(id, create_time, update_time, crn, external_id, privacy_domain, access_key, secret_key, service, admin)
    values(2, now(), now(), 'crn:dia:124', 'ext:987', 'dia', 'access124','secret456', 'delegations', true);
insert into clients(id, create_time, update_time, crn, external_id, privacy_domain, access_key, secret_key, service, admin)
    values(3, now(), now(), 'crn:selenity:001', 'ext:654', 'selenity', 'selenity123','secret456', 'delegations', true);

insert into templates(id, create_time, update_time, crn, name, client_id, is_default, content, version)
	values(1, now(), now(), 'crn:dia:123::templates/default', 'Default', 1, true, '<!DOCTYPE html><html xmlns:th="http://www.thymeleaf.org"><head><title th:remove="all">Template for HTML email with inline image</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head><body><p>This is the default email template for DIA Test</p></body></html>', 1);
insert into templates(id, create_time, update_time, crn, name, client_id, is_default, content, version)
	values(2, now(), now(), 'crn:dia:124::templates/default', 'Default', 2, true, '<!DOCTYPE html><html xmlns:th="http://www.thymeleaf.org"><head><title th:remove="all">Template for HTML email with inline image</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head><body><p>This is the default email template for DIA Test</p></body></html>', 1);
insert into templates(id, create_time, update_time, crn, name, client_id, is_default, content, version)
	values(3, now(), now(), 'crn:selenity:001::templates/default', 'Default', 3, true, '<!DOCTYPE html><html xmlns:th="http://www.thymeleaf.org"><head><title th:remove="all">Template for HTML email with inline image</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head><body><p>This is the default email template for DIA Test</p></body></html>', 1);
    
insert into users(id, email) VALUES (1, 'user1@example.com');
insert into users(id, email) VALUES (2, 'user2@example.com');

insert into resources(id, client_id, user_id, crn) VALUES (1, 1, 1, 'crn:dia:user1:123456789012:templates/passport-1');
insert into resources(id, client_id, user_id, crn) VALUES (2, 2, 2, 'crn:dia:user2:123456789012:templates/passport-1');
insert into resources(id, client_id, user_id, crn, delete_time) VALUES (3, 1, 1, 'crn:dia:user1:123456789012:templates/passport-OLD', now());

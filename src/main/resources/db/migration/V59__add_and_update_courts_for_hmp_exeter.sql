insert into enabled_court (id, name) values
('BARSPP', 'Probation - Barkingside (PPOC)'),
('HGHBPP', 'Probation - Highbury Corner (PPOC)'),
('THMSPP', 'Probation - Thames (PPOC)');

update enabled_court set name = 'Probation - Snaresbrook (PPOC)' where id = 'PPOCCT';
update enabled_court set name = 'Probation - Field (PPOC)' where id = 'PPOCFD';



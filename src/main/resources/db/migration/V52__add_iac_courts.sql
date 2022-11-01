-- These don't exist in court register so adding a common suffix
insert into enabled_court (id, name) values
(  'NWPIAC', 'IAC Newport'),
(  'GLAIAC', 'IAC Glasgow'),
(  'BRAIAC', 'IAC Bradford'),
(  'MNCIAC', 'IAC Manchester'),
(  'BDFIAC', 'IAC Yarl''s Wood'),
(  'TAYIAC', 'IAC Taylor House'),
(  'HATIAC', 'IAC Hatton Cross');

update enabled_court set name='IAC Birmingham' where id = 'BIRAIT';

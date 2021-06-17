create table enabled_court (
                               id varchar(20)   not null primary key,
                               name varchar(50) not null
);

-- Some court names don't match anything in the court register
-- Some court names (Wolverhampton, Coventry) for appointments made_by_the_court don't match any court name in application.properties.

insert into enabled_court (id, name) values
(  'BIRAIT', 'Birmingham Immigration'),             -- Doesn't exist in court register
(  'BROMMC', 'Bromley'),                            -- {"id":"BROMMC","name":"Bromley Magistrates Court"}
(  'CRNRCT', 'Caernarfon County'),                  -- {"id":"CRNRCT","name":"Caernarfon County Court"}
(  'CRNRCC', 'Caernarfon Crown'),                   -- {"id":"CRNRCC","name":"Caernarfon Crown Court"}
(  'CRNHMC', 'Caernarfon Magistrates'),             -- {"id":"CRNHMC","name":"Caernarfon Magistrates Court"}
(  'CHELMC', 'Cheltenham'),                         -- {"id":"CHELMC","name":"Cheltenham Magistrates Court"}
(  'CHSTMC', 'Chesterfield Justice Centre'),        -- Doesn't exist in court register
(  'CITYMC', 'City of London'),                     -- {"id":"CITYMC","name":"City of London (Queen Victoria St) Magistrates Court"}
(  'CVNTCC', 'Coventry Crown'),                     -- {"id":"CVNTCC","name":"Coventry Crown Court"}
(  'CVNTMC', 'Coventry Magistrates'),               -- {"id":"CVNTMC","name":"Coventry Magistrates Court"}
(  'CRYDCC', 'Croydon Crown'),                      -- {"id":"CRYDCC","name":"Croydon Crown Court"}
(  'CRYDMC', 'Croydon Magistrates'),                -- {"id":"CRYDMC","name":"Croydon Magistrates Court"}
(  'DRBYCC', 'Derby Crown'),                        -- {"id":"DRBYCC","name":"Derby Crown Court"}
(  'DRBYJC', 'Derby Justice Centre'),               -- Doesn't exist in court register
(  'DUDLMC', 'Dudley'),                             -- {"id":"DUDLMC","name":"Dudley Magistrates Court"}
(  'HERFCC', 'Hereford Crown'),                     -- {"id":"HERFCC","name":"Hereford Crown Court"}
(  'HERFMC', 'Hereford Magistrates'),               -- {"id":"HERFMC","name":"Hereford Magistrates Court"}
(  'INNRCC', 'Inner London'),                       -- {"id":"INNRCC","name":"Inner London Sessions House Crown Court"}
(  'KIDDMC', 'Kidderminster'),                      -- {"id":"KIDDMC","name":"Kidderminster Magistrates Court"}
(  'KNGTCC', 'Kingston upon Thames'),               -- {"id":"KNGTCC","name":"Kingston upon Thames Crown Court"}
(  'LEAMMC', 'Leamington'),                         -- {"id":"LEAMMC","name":"Leamington Spa Magistrates Court"}
(  'LLNWMC', 'Llandrindod Wells Justice Centre'),   -- {"id":"LLNWMC","name":"Llandrindod Wells Magistrates Court"}
(  'LLDUMC', 'Llandudno'),                          -- {"id":"LLDUMC","name":"Llandudno Magistrates Court"}
(  'MNSFMC', 'Mansfield Justice Centre'),           -- {"id":"MNSFMC","name":"Mansfield Magistrates Court"}
(  'MOLDCT', 'Mold County'),                        -- {"id":"MOLDCT","name":"Mold County Court"}
(  'MOLDCC', 'Mold Crown'),                         -- {"id":"MOLDCC","name":"Mold Crown Court"}
(  'MOLDMC', 'Mold Magistrates'),                   -- {"id":"MOLDMC","name":"Mold Magistrates Court"}
(  'NOTTCC', 'Nottingham Crown'),                   -- {"id":"NOTTCC","name":"Nottingham Crown Court"}
(  'NOTTMC', 'Nottingham Justice Centre'),          -- {"id":"NOTTMC","name":"Nottingham Magistrates Court"}
(  'PRSTMC', 'Prestatyn County'),                   -- Doesn't exist in court register
(  'RDDTMC', 'Redditch'),                           -- {"id":"RDDTMC","name":"Redditch Magistrates Court"}
(  'SHRWCC', 'Shrewsbury Crown'),                   -- {"id":"SHRWCC","name":"Shrewsbury Crown Court"}
(  'SHREMC', 'Shrewsbury Magistrates'),             -- {"id":"SHREMC","name":"Shrewsbury Magistrates Court"}
(  'DRBYMC', 'South Derbyshire'),                   -- {"id":"DRBYMC","name":"Southern Derbyshire Magistrates Court (Derby)"}
(  'STHWCC', 'Southwark'),                          -- {"id":"STHWCC","name":"Southwark Crown Court"}
(  'TELFMC', 'Telford'),                            -- {"id":"TELFMC","name":"Telford & South Shropshire Magistrates Court"}
(  'THMSMC', 'Thames'),                             -- {"id":"THMSMC","name":"Thames Magistrates Court"}
(  'WLSSMC', 'Walsall'),                            -- {"id":"WLSSMC","name":"Walsall Magistrates Court"}
(  'WRWKCC', 'Warwick'),                            -- {"id":"WRWKCC","name":"Warwick Crown Court"}
(  'WLSHCT', 'Welshpool County'),                   -- {"id":"WLSHCT","name":"Welshpool and Newtown County Court"}
(  'WLSHMC', 'Welshpool Magistrates'),              -- {"id":"WLSHMC","name":"Welshpool Magistrates Court"}
(  'CRT034', 'Westminster'),                        -- {"id":"CRT034","name":"Westminster Magistrates Court"}
(  'WMBLMC', 'Wimbledon'),                          -- {"id":"WMBLMC","name":"Wimbledon Magistrates Court"}
(  'WLVRCC', 'Wolverhampton Crown'),                -- {"id":"WLVRCC","name":"Wolverhampton Crown Court"}
(  'WLVRMC', 'Wolverhampton Magistrates'),          -- {"id":"WLVRMC","name":"Wolverhampton Magistrates Court"}
(  'WDGRCC', 'Wood Green'),                         -- {"id":"WDGRCC","name":"Wood Green Crown Court"}
(  'WOOLCC', 'Woolwich'),                           -- {"id":"WOOLCC","name":"Woolwich Crown Court"}
(  'WRCSCC', 'Worcester Crown'),                    -- {"id":"WRCSCC","name":"Worcester Crown Court"}
(  'WRCSMC', 'Worcester Magistrates'),              -- {"id":"WRCSMC","name":"Worcester (South Worcestershire) Magistrates Court"}
(  'WREXCT', 'Wrexham County'),                     -- {"id":"WREXCT","name":"Wrexham County Court"}
(  'WREXMC', 'Wrexham Magistrates');                -- {"id":"WREXMC","name":"Wrexham Magistrates Court"}



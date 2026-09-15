CREATE TABLE locations (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    name_normalized VARCHAR(128) NOT NULL,
    region VARCHAR(128) NOT NULL DEFAULT '',
    country VARCHAR(2) NOT NULL DEFAULT 'AT',
    postal_code VARCHAR(16) NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    UNIQUE KEY uk_locations_name_region_country (name_normalized, region, country),
    INDEX idx_locations_name (name_normalized),
    INDEX idx_locations_postal (postal_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE listings
    ADD COLUMN latitude DECIMAL(10, 7) NULL,
    ADD COLUMN longitude DECIMAL(10, 7) NULL,
    ADD INDEX idx_listings_geo (latitude, longitude);

INSERT INTO locations (id, name, name_normalized, region, country, postal_code, latitude, longitude) VALUES
('loc-linz', 'Linz', 'linz', 'Oberösterreich', 'AT', '4020', 48.3069000, 14.2858000),
('loc-wels', 'Wels', 'wels', 'Oberösterreich', 'AT', '4600', 48.1575000, 14.0289000),
('loc-steyr', 'Steyr', 'steyr', 'Oberösterreich', 'AT', '4400', 48.0428000, 14.4213000),
('loc-amstetten', 'Amstetten', 'amstetten', 'Niederösterreich', 'AT', '3300', 48.1229000, 14.8720000),
('loc-leonding', 'Leonding', 'leonding', 'Oberösterreich', 'AT', '4060', 48.2792000, 14.2531000),
('loc-traun', 'Traun', 'traun', 'Oberösterreich', 'AT', '4050', 48.2265000, 14.2396000),
('loc-enns', 'Enns', 'enns', 'Oberösterreich', 'AT', '4470', 48.2135000, 14.4789000),
('loc-ansfelden', 'Ansfelden', 'ansfelden', 'Oberösterreich', 'AT', '4052', 48.2097000, 14.2903000),
('loc-marchtrenk', 'Marchtrenk', 'marchtrenk', 'Oberösterreich', 'AT', '4614', 48.1917000, 14.1106000),
('loc-gmunden', 'Gmunden', 'gmunden', 'Oberösterreich', 'AT', '4810', 47.9185000, 13.7994000),
('loc-voecklabruck', 'Vöcklabruck', 'vöcklabruck', 'Oberösterreich', 'AT', '4840', 48.0087000, 13.6556000),
('loc-perg', 'Perg', 'perg', 'Oberösterreich', 'AT', '4320', 48.2503000, 14.6339000),
('loc-freistadt', 'Freistadt', 'freistadt', 'Oberösterreich', 'AT', '4240', 48.5117000, 14.5036000),
('loc-ried', 'Ried im Innkreis', 'ried im innkreis', 'Oberösterreich', 'AT', '4910', 48.2107000, 13.4884000),
('loc-schaerding', 'Schärding', 'schärding', 'Oberösterreich', 'AT', '4780', 48.4569000, 13.4317000),
('loc-braunau', 'Braunau am Inn', 'braunau am inn', 'Oberösterreich', 'AT', '5280', 48.2563000, 13.0434000),
('loc-kirchdorf', 'Kirchdorf an der Krems', 'kirchdorf an der krems', 'Oberösterreich', 'AT', '4560', 47.9054000, 14.1225000),
('loc-bad-ischl', 'Bad Ischl', 'bad ischl', 'Oberösterreich', 'AT', '4820', 47.7115000, 13.6239000),
('loc-eferding', 'Eferding', 'eferding', 'Oberösterreich', 'AT', '4070', 48.3087000, 14.0222000),
('loc-grieskirchen', 'Grieskirchen', 'grieskirchen', 'Oberösterreich', 'AT', '4710', 48.1551000, 13.8319000),
('loc-rohrbach', 'Rohrbach-Berg', 'rohrbach-berg', 'Oberösterreich', 'AT', '4150', 48.5736000, 13.9897000),
('loc-wien', 'Wien', 'wien', 'Wien', 'AT', '1010', 48.2082000, 16.3738000),
('loc-graz', 'Graz', 'graz', 'Steiermark', 'AT', '8010', 47.0707000, 15.4395000),
('loc-salzburg', 'Salzburg', 'salzburg', 'Salzburg', 'AT', '5020', 47.8095000, 13.0550000),
('loc-innsbruck', 'Innsbruck', 'innsbruck', 'Tirol', 'AT', '6020', 47.2692000, 11.4041000),
('loc-klagenfurt', 'Klagenfurt', 'klagenfurt', 'Kärnten', 'AT', '9020', 46.6249000, 14.3050000),
('loc-villach', 'Villach', 'villach', 'Kärnten', 'AT', '9500', 46.6111000, 13.8558000),
('loc-st-poelten', 'St. Pölten', 'st. pölten', 'Niederösterreich', 'AT', '3100', 48.2047000, 15.6356000),
('loc-bregenz', 'Bregenz', 'bregenz', 'Vorarlberg', 'AT', '6900', 47.5031000, 9.7471000),
('loc-eisenstadt', 'Eisenstadt', 'eisenstadt', 'Burgenland', 'AT', '7000', 47.8456000, 16.5233000),
('loc-wiener-neustadt', 'Wiener Neustadt', 'wiener neustadt', 'Niederösterreich', 'AT', '2700', 47.8151000, 16.2465000),
('loc-dornbirn', 'Dornbirn', 'dornbirn', 'Vorarlberg', 'AT', '6850', 47.4125000, 9.7417000),
('loc-feldkirch', 'Feldkirch', 'feldkirch', 'Vorarlberg', 'AT', '6800', 47.2378000, 9.5981000);

UPDATE listings l
INNER JOIN locations loc
    ON loc.country = 'AT'
    AND loc.name_normalized = LOWER(TRIM(l.location))
SET l.latitude = loc.latitude,
    l.longitude = loc.longitude
WHERE l.latitude IS NULL;

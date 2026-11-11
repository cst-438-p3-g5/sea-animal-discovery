-- Species catalog seed data (PRD-4).
-- spring.sql.init.mode=always runs this file on every startup, so each insert
-- only adds a species if its id is not already in the table.

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-001', 'Ochre sea star',
       'Five thick arms covered in small white spines that form a net-like pattern. Color ranges from purple to orange or brown. Usually 10 to 25 cm across.',
       'Rocky intertidal zones from Alaska to Baja California, often clinging to rocks near mussel beds in the mid to low tide zone.',
       'Not listed as endangered, but populations crashed after sea star wasting disease spread along the Pacific coast starting in 2013. Many areas are slowly recovering.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-001');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-002', 'Giant green anemone',
       'Bright green column and tentacles, colored by algae living in its tissue. Can reach about 25 cm across with a wide, flat oral disk.',
       'Tidepools, surge channels and rocky shores in the low intertidal zone, usually where it gets plenty of light and wave action.',
       'Common and not considered threatened. Anemones can live for many years, so leave them in place and avoid poking or touching them.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-002');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-003', 'Aggregating anemone',
       'Small anemone, usually under 8 cm across, with greenish tentacles often tipped in pink or purple. Often covered in bits of shell and gravel when the tide is out.',
       'Mid intertidal rocks, where it forms dense carpets of genetically identical clones.',
       'Very common along the Pacific coast and not threatened. Step carefully, since large colonies can be crushed by foot traffic.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-003');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-004', 'Purple sea urchin',
       'Round purple body covered in short, sharp spines. Usually 5 to 10 cm across. Often sits in a small pit it has worn into the rock.',
       'Low intertidal rocks and shallow kelp forests along the Pacific coast, from Alaska to Baja California.',
       'Not threatened. Their numbers have exploded in parts of California after the loss of predators such as the sunflower sea star, turning some kelp forests into urchin barrens.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-004');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-005', 'Bat star',
       'Usually five short, wide arms connected by webbing, giving it a flat, almost pentagon shape. Comes in many colors, including red, orange, purple and yellow.',
       'Low intertidal pools and shallow subtidal areas on rocks, sand and kelp, from Alaska to Baja California.',
       'Common and not considered threatened. Like other sea stars, it can be affected by sea star wasting disease.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-005');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-006', 'Black turban snail',
       'Dark purple to black cone-shaped shell about 2 to 3 cm wide. The tip of the shell is often worn down, showing white or orange underneath.',
       'Mid to high intertidal rocks and tidepools, often in large groups grazing on algae.',
       'Very common and not threatened. These snails can live 20 years or more, and their empty shells become homes for hermit crabs.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-006');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-007', 'Hermit crab',
       'Small crab with a soft, curled abdomen that it protects by living inside an empty snail shell, often a black turban shell. It moves into a bigger shell as it grows.',
       'Tidepools and rocky shores in the mid to low intertidal zone, often in groups.',
       'Common and not threatened. Avoid collecting empty shells from tidepools, since hermit crabs depend on them for shelter.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-007');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-008', 'California mussel',
       'Blue-black oval shell with rough ridges, usually 8 to 13 cm long but sometimes much larger. Attaches to rocks with strong byssal threads.',
       'Dense beds on wave-exposed rocks in the mid intertidal zone along the Pacific coast.',
       'Common and not threatened. California has an annual quarantine on sport-harvested mussels, usually from May through October, because of the risk of paralytic shellfish poisoning.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-008');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-009', 'Gumboot chiton',
       'The largest chiton in the world, up to about 33 cm long. A reddish-brown, leathery outer layer completely covers its eight shell plates, so it looks like a loaf of bread.',
       'Low intertidal and shallow subtidal rocky areas, from Alaska to southern California.',
       'Not threatened. They grow slowly and can live 20 years or more, so it is best not to pick them up.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-009');

INSERT INTO species (id, common_name, identification_details, habitat, conservation_information)
SELECT 'sp-010', 'California sea hare',
       'Large, soft sea slug with a reddish-brown body and two ear-like tentacles on its head. Can grow over 30 cm long and releases purple ink when disturbed.',
       'Low intertidal pools, eelgrass beds and kelp areas, where it feeds on red and green algae.',
       'Common and not threatened. Scientists study it because its large nerve cells make it useful for research on learning and memory.'
WHERE NOT EXISTS (SELECT 1 FROM species WHERE id = 'sp-010');

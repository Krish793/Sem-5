-- Seed Data for Weavo Local Artisan Marketplace (From the loom to your living room.)
-- Demo account password across test accounts: Weavo@2026
INSERT INTO public.craft_categories (id, name, slug, description, region_focus) VALUES
('11111111-1111-1111-1111-111111111101', 'Handloom & Textiles', 'handloom-textiles', 'Traditional tie-dye Bandhani, Double Ikat Patola, and handwoven shawls', 'Jamnagar, Patan & Bhujodi, Gujarat'),
('11111111-1111-1111-1111-111111111102', 'Block Printing & Natural Dyes', 'block-printing', 'Hand-carved wooden block prints using natural indigo, madder, and pomegranate dyes', 'Dhamadka & Ajrakhpur, Kutch'),
('11111111-1111-1111-1111-111111111103', 'Pottery & Terracotta', 'pottery-terracotta', 'Wheel-thrown eco-friendly clay ware painted with mineral pigments', 'Gundiyali & Thangadh, Gujarat'),
('11111111-1111-1111-1111-111111111104', 'Embroidery & Home Decor', 'embroidery-decor', 'Kutchi mirror work, rabari stitch cushions, wall hangings, and torans', 'Kutch & Saurashtra, Gujarat'),
('11111111-1111-1111-1111-111111111105', 'Woodcraft & Lacquerware', 'woodcraft-lacquer', 'Lathe-turned teak wood artifacts coated in vibrant tin-foil lacquer', 'Sankheda & Idar, Gujarat')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO public.app_users (id, full_name, email, password_hash, role, phone, city, state, dpdp_consent) VALUES
('22222222-2222-2222-2222-222222222201', 'Aarav Patel', 'aarav.buyer@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'CUSTOMER', '+91-9876500001', 'Ahmedabad', 'Gujarat', TRUE),
('22222222-2222-2222-2222-222222222202', 'Meera Shah', 'meera.buyer@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'CUSTOMER', '+91-9876500002', 'Surat', 'Gujarat', TRUE),
('22222222-2222-2222-2222-222222222203', 'Rameshbhai Khatri', 'ramesh.artisan@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'ARTISAN', '+91-9876500003', 'Jamnagar', 'Gujarat', TRUE),
('22222222-2222-2222-2222-222222222204', 'Jiviben Ahir', 'jiviben.artisan@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'ARTISAN', '+91-9876500004', 'Bhujodi', 'Gujarat', TRUE),
('22222222-2222-2222-2222-222222222205', 'Kumbhar Ismail', 'ismail.artisan@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'ARTISAN', '+91-9876500005', 'Gundiyali', 'Gujarat', TRUE),
('22222222-2222-2222-2222-222222222206', 'Weavo Admin', 'admin@weavo.demo', '86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11', 'ADMIN', '+91-9876500099', 'Ahmedabad', 'Gujarat', TRUE)
ON CONFLICT (email) DO NOTHING;

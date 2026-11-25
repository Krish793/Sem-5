-- KalaSetu Local Artisan Marketplace Schema (PostgreSQL 17 / Supabase)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS public.app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('CUSTOMER', 'ARTISAN', 'ADMIN')),
    phone TEXT,
    city TEXT,
    state TEXT DEFAULT 'Gujarat',
    dpdp_consent BOOLEAN NOT NULL DEFAULT TRUE,
    consent_timestamp TIMESTAMPTZ DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.artisan_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
    workshop_name TEXT NOT NULL,
    village_cluster TEXT NOT NULL,
    district TEXT NOT NULL,
    state TEXT NOT NULL DEFAULT 'Gujarat',
    craft_specialty TEXT NOT NULL,
    gi_tag_number TEXT,
    kyc_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    subscription_tier TEXT NOT NULL DEFAULT 'STARTER_FREE' CHECK (subscription_tier IN ('STARTER_FREE', 'KARIGAR_PRO', 'COOP_STUDIO')),
    commission_rate NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    bio TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.craft_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    slug TEXT UNIQUE NOT NULL,
    description TEXT,
    region_focus TEXT
);

CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    artisan_id UUID NOT NULL REFERENCES public.artisan_profiles(id) ON DELETE CASCADE,
    category_id UUID REFERENCES public.craft_categories(id) ON DELETE SET NULL,
    sku TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    craft_type TEXT NOT NULL,
    origin_village TEXT NOT NULL,
    gi_certified BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT NOT NULL,
    price_inr NUMERIC(10,2) NOT NULL CHECK (price_inr > 0),
    stock_qty INT NOT NULL CHECK (stock_qty >= 0),
    image_url TEXT,
    storage_path TEXT,
    is_featured_festival BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (customer_id, product_id)
);

CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number TEXT UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES public.app_users(id),
    total_amount_inr NUMERIC(10,2) NOT NULL CHECK (total_amount_inr >= 0),
    platform_commission_inr NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    shipping_fee_inr NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    payment_method TEXT NOT NULL CHECK (payment_method IN ('UPI_TEST', 'CARD_TEST', 'WALLET_TEST')),
    payment_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID_TEST', 'FAILED_TEST', 'REFUNDED_TEST')),
    transaction_ref TEXT,
    order_status TEXT NOT NULL DEFAULT 'PLACED' CHECK (order_status IN ('PLACED', 'CONFIRMED', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    shipping_address TEXT NOT NULL,
    dpdp_consent_version TEXT NOT NULL DEFAULT 'v1.0-2026',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id),
    artisan_id UUID NOT NULL REFERENCES public.artisan_profiles(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_inr NUMERIC(10,2) NOT NULL CHECK (unit_price_inr > 0),
    line_total_inr NUMERIC(10,2) NOT NULL CHECK (line_total_inr > 0),
    artisan_payout_inr NUMERIC(10,2) NOT NULL CHECK (artisan_payout_inr >= 0)
);

CREATE TABLE IF NOT EXISTS public.verified_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES public.app_users(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT TRUE,
    moderation_status TEXT NOT NULL DEFAULT 'APPROVED' CHECK (moderation_status IN ('APPROVED', 'FLAGGED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, customer_id, order_id)
);

CREATE TABLE IF NOT EXISTS public.notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES public.orders(id) ON DELETE CASCADE,
    recipient_user_id UUID REFERENCES public.app_users(id) ON DELETE CASCADE,
    channel TEXT NOT NULL CHECK (channel IN ('EMAIL_SIM', 'WHATSAPP_SIM', 'IN_APP')),
    message TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'SENT_SIMULATED',
    worker_thread_name TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID REFERENCES public.app_users(id) ON DELETE SET NULL,
    event_type TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    details JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION public.decrement_product_stock(p_product_id UUID, p_qty INT)
RETURNS INT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_remaining INT;
BEGIN
    UPDATE public.products
    SET stock_qty = stock_qty - p_qty, updated_at = NOW()
    WHERE id = p_product_id AND is_active = TRUE AND stock_qty >= p_qty
    RETURNING stock_qty INTO v_remaining;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'OUT_OF_STOCK: Insufficient stock for product %', p_product_id;
    END IF;
    RETURN v_remaining;
END;
$$;

ALTER TABLE public.app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.artisan_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.craft_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.verified_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notification_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public can read craft categories" ON public.craft_categories FOR SELECT USING (true);
CREATE POLICY "Public can read active products" ON public.products FOR SELECT USING (is_active = true);
CREATE POLICY "Public can read verified artisan profiles" ON public.artisan_profiles FOR SELECT USING (kyc_status = 'VERIFIED');
CREATE POLICY "Public can read approved verified reviews" ON public.verified_reviews FOR SELECT USING (moderation_status = 'APPROVED');

INSERT INTO storage.buckets (id, name, public) VALUES ('product-images', 'product-images', true) ON CONFLICT (id) DO NOTHING;
CREATE POLICY "Public read access to product-images" ON storage.objects FOR SELECT USING (bucket_id = 'product-images');

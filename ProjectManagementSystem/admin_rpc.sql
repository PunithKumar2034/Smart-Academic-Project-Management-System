-- RPC Function to allow Admin to create new users safely
-- This function handles both auth.users and public.users
CREATE OR REPLACE FUNCTION admin_create_user(
    p_email TEXT,
    p_password TEXT,
    p_name TEXT,
    p_role TEXT
) RETURNS UUID AS $$
DECLARE
    v_user_id UUID;
BEGIN
    -- 1. Check if caller is admin
    IF NOT EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND role = 'admin') THEN
        RAISE EXCEPTION 'Only admins can create users';
    END IF;

    -- 2. Create user in auth.users (Supabase Auth)
    -- We use crypt to hash the password for Supabase
    INSERT INTO auth.users (
        instance_id,
        id,
        aud,
        role,
        email,
        encrypted_password,
        email_confirmed_at,
        recovery_sent_at,
        last_sign_in_at,
        raw_app_meta_data,
        raw_user_meta_data,
        created_at,
        updated_at,
        confirmation_token,
        email_change,
        email_change_token_new,
        recovery_token
    ) VALUES (
        '00000000-0000-0000-0000-000000000000',
        gen_random_uuid(),
        'authenticated',
        'authenticated',
        p_email,
        crypt(p_password, gen_salt('bf')),
        now(),
        now(),
        now(),
        '{"provider":"email","providers":["email"]}',
        jsonb_build_object('name', p_name, 'role', p_role),
        now(),
        now(),
        '',
        '',
        '',
        ''
    ) RETURNING id INTO v_user_id;

    -- 3. Create identity
    INSERT INTO auth.identities (
        id,
        user_id,
        identity_data,
        provider,
        last_sign_in_at,
        created_at,
        updated_at
    ) VALUES (
        gen_random_uuid(),
        v_user_id,
        jsonb_build_object('sub', v_user_id, 'email', p_email),
        'email',
        now(),
        now(),
        now()
    );

    -- 4. public.users will be updated by the existing trigger
    -- But we can return the ID
    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

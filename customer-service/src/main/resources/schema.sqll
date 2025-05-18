CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS tb_customers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    other_name VARCHAR(100), -- Optional
    email VARCHAR(150),      -- Optional
    phone_number VARCHAR(30) UNIQUE,  -- Optional,
    national_id VARCHAR(50) UNIQUE,   -- Optional,
    date_of_birth TIMESTAMP, -- Optional
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );


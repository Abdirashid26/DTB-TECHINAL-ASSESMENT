CREATE TABLE IF NOT EXISTS tb_customers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(150),
    phone_number VARCHAR(30),
    national_id VARCHAR(50),
    date_of_birth TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

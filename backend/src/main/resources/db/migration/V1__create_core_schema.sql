CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    website VARCHAR(255),
    industry VARCHAR(100),
    location VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_companies_user_id ON companies (user_id);
CREATE INDEX idx_companies_user_name ON companies (user_id, name);

CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    company_id BIGINT NOT NULL REFERENCES companies(id),
    job_title VARCHAR(150) NOT NULL,
    location VARCHAR(150),
    job_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    salary_min INTEGER,
    salary_max INTEGER,
    applied_date DATE,
    deadline DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_applications_user_id ON job_applications (user_id);
CREATE INDEX idx_applications_user_status ON job_applications (user_id, status);
CREATE INDEX idx_applications_user_company ON job_applications (user_id, company_id);
CREATE INDEX idx_applications_user_applied_date ON job_applications (user_id, applied_date);

CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES job_applications(id),
    type VARCHAR(30) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    interviewer VARCHAR(150),
    notes TEXT,
    result VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_interviews_application_id ON interviews (application_id);
CREATE INDEX idx_interviews_scheduled_at ON interviews (scheduled_at);

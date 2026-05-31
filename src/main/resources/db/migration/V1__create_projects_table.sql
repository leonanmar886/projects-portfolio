-- Migration: create projects table and join table project_members

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    estimated_end_date DATE NOT NULL,
    actual_end_date DATE,
    budget NUMERIC(19,2) NOT NULL,
    description TEXT,
    manager_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, member_id)
);

-- Foreign keys (assumes members table exists)
ALTER TABLE projects
    ADD CONSTRAINT fk_projects_manager
    FOREIGN KEY (manager_id) REFERENCES members(id);

ALTER TABLE project_members
    ADD CONSTRAINT fk_pm_project
    FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE project_members
    ADD CONSTRAINT fk_pm_member
    FOREIGN KEY (member_id) REFERENCES members(id);


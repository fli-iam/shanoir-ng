CREATE TABLE execution_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    pipeline_name VARCHAR(255),
    filter_combination VARCHAR(255),
    priority INT NOT NULL,
    study_id BIGINT,
    PRIMARY KEY (id),

    CONSTRAINT fk_executiontemplate_study
        FOREIGN KEY (study_id)
            REFERENCES study(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE
);

CREATE TABLE execution_template_filter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    field_name VARCHAR(255) NOT NULL,
    compared_regex VARCHAR(500),
    excluded BOOLEAN NOT NULL,
    identifier INT NOT NULL,
    execution_template_id BIGINT,
    PRIMARY KEY (id),

    CONSTRAINT fk_filter_executiontemplate
        FOREIGN KEY (execution_template_id)
            REFERENCES execution_template(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE execution_template_parameter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    execution_template_id BIGINT,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(1000),

    PRIMARY KEY (id),

    CONSTRAINT fk_parameter_executiontemplate
        FOREIGN KEY (execution_template_id)
            REFERENCES execution_template(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE planned_execution (
    id BIGINT NOT NULL AUTO_INCREMENT,
    acquisition_id BIGINT,
    template_id BIGINT,
    PRIMARY KEY (id)
);



CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY CHECK (id > 0 AND id < 9223372036854775807),
    name VARCHAR(255) NOT NULL UNIQUE CHECK (CHAR_LENGTH(TRIM(name)) > 0)
);

CREATE TABLE IF NOT EXISTS notes (
    id BIGINT PRIMARY KEY CHECK (id > 0 AND id < 9223372036854775807),
    name VARCHAR(255) NOT NULL,
    content CLOB NOT NULL,
    created_on DATE NOT NULL,
    category_id BIGINT NOT NULL,
    priority INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 5),
    note_type VARCHAR(16) NOT NULL CHECK (note_type IN ('TEXT', 'REMINDER', 'RECURRING')),
    reminder_hours DOUBLE PRECISION NOT NULL CHECK (reminder_hours >= 0),
    repeat_hours DOUBLE PRECISION NOT NULL CHECK (repeat_hours >= 0),
    CONSTRAINT fk_notes_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT ck_note_type CHECK (
        (note_type = 'TEXT' AND reminder_hours = 0 AND repeat_hours = 0) OR
        (note_type = 'REMINDER' AND reminder_hours > 0 AND repeat_hours = 0) OR
        (note_type = 'RECURRING' AND reminder_hours > 0 AND repeat_hours > 0)
    )
);

CREATE INDEX IF NOT EXISTS idx_notes_category ON notes(category_id);
CREATE INDEX IF NOT EXISTS idx_notes_name_priority ON notes(name, priority, id);

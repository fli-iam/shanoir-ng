# Use kv_key, as key is a special word
CREATE TABLE key_value (
    `kv_key` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`kv_key`)
);

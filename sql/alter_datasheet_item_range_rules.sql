-- 기종(슬롯) 선택에 따른 숫자 항목 min/max 범위 규칙
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'datasheet_item_range_rules')
BEGIN
    CREATE TABLE datasheet_item_range_rules (
        id                  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        item_id             BIGINT NOT NULL,
        depends_on_item_id  BIGINT NOT NULL,
        match_slot_index    INT NOT NULL,
        min_value           DECIMAL(18,6) NULL,
        max_value           DECIMAL(18,6) NULL,
        CONSTRAINT FK_range_rules_item FOREIGN KEY (item_id)
            REFERENCES datasheet_items(id) ON DELETE CASCADE,
        CONSTRAINT FK_range_rules_depends FOREIGN KEY (depends_on_item_id)
            REFERENCES datasheet_items(id),
        CONSTRAINT UQ_range_rules_item_dep_slot UNIQUE (item_id, depends_on_item_id, match_slot_index)
    );

    CREATE INDEX IX_range_rules_item_id ON datasheet_item_range_rules(item_id);
    CREATE INDEX IX_range_rules_depends_on ON datasheet_item_range_rules(depends_on_item_id);
END
GO

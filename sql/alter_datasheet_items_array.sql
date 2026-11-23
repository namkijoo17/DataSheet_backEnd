-- 배열 항목 지원: is_array / array_size + 요소별 좌표 테이블
IF COL_LENGTH('datasheet_items', 'is_array') IS NULL
BEGIN
    ALTER TABLE datasheet_items ADD is_array BIT NOT NULL
        CONSTRAINT DF_datasheet_items_is_array DEFAULT (0);
END
GO


IF COL_LENGTH('datasheet_items', 'array_size') IS NULL
BEGIN
    ALTER TABLE datasheet_items ADD array_size INT NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'datasheet_item_slots')
BEGIN
    CREATE TABLE datasheet_item_slots (
        id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        item_id         BIGINT NOT NULL,
        slot_index      INT NOT NULL,
        label           NVARCHAR(100) NULL,
        pdf_page        INT NOT NULL CONSTRAINT DF_datasheet_item_slots_pdf_page DEFAULT (1),
        pos_x           DECIMAL(10,2) NOT NULL,
        pos_y           DECIMAL(10,2) NOT NULL,
        pos_width       DECIMAL(10,2) NULL,
        pos_height      DECIMAL(10,2) NULL,
        CONSTRAINT FK_datasheet_item_slots_item FOREIGN KEY (item_id) REFERENCES datasheet_items(id) ON DELETE CASCADE,
        CONSTRAINT UQ_datasheet_item_slots_item_index UNIQUE (item_id, slot_index)
    );

    CREATE INDEX IX_datasheet_item_slots_item_id ON datasheet_item_slots(item_id);
END
GO

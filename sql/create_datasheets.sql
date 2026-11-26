IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'datasheets')
BEGIN
    CREATE TABLE datasheets (
        id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        name            NVARCHAR(200) NOT NULL,
        revision        INT NOT NULL CONSTRAINT DF_datasheets_revision DEFAULT (1),
        file_name       NVARCHAR(255) NOT NULL,
        page_width      DECIMAL(10,2) NULL,
        page_height     DECIMAL(10,2) NULL,
        active          BIT NOT NULL CONSTRAINT DF_datasheets_active DEFAULT (1),
        created_at      DATETIME2 NOT NULL CONSTRAINT DF_datasheets_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at      DATETIME2 NOT NULL CONSTRAINT DF_datasheets_updated_at DEFAULT (SYSUTCDATETIME())
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'datasheet_items')
BEGIN
    CREATE TABLE datasheet_items (
        id                  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        datasheet_id        BIGINT NOT NULL,
        item_no             INT NOT NULL,
        group_name          NVARCHAR(100) NULL,
        label               NVARCHAR(200) NOT NULL,
        -- 검사항목 여부: 1=검사항목(판정 대상), 0=정보/환경 등 비검사 입력
        is_inspection_item  BIT NOT NULL CONSTRAINT DF_datasheet_items_is_inspection DEFAULT (1),
        is_array            BIT NOT NULL CONSTRAINT DF_datasheet_items_is_array DEFAULT (0),
        array_size          INT NULL,
        input_type          VARCHAR(20) NOT NULL,
        unit                NVARCHAR(20) NULL,
        decimal_places      INT NULL,
        min_value           DECIMAL(18,6) NULL,
        max_value           DECIMAL(18,6) NULL,
        allow_negative      BIT NOT NULL CONSTRAINT DF_datasheet_items_allow_negative DEFAULT (0),
        required            BIT NOT NULL CONSTRAINT DF_datasheet_items_required DEFAULT (1),
        skippable           BIT NOT NULL CONSTRAINT DF_datasheet_items_skippable DEFAULT (0),
        formula             NVARCHAR(500) NULL,
        spec_description    NVARCHAR(500) NULL,
        pdf_page            INT NOT NULL CONSTRAINT DF_datasheet_items_pdf_page DEFAULT (1),
        pos_x               DECIMAL(10,2) NOT NULL,
        pos_y               DECIMAL(10,2) NOT NULL,
        pos_width           DECIMAL(10,2) NULL,
        pos_height          DECIMAL(10,2) NULL,
        sort_order          INT NOT NULL CONSTRAINT DF_datasheet_items_sort_order DEFAULT (0),
        created_at          DATETIME2 NOT NULL CONSTRAINT DF_datasheet_items_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at          DATETIME2 NOT NULL CONSTRAINT DF_datasheet_items_updated_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_datasheet_items_datasheet FOREIGN KEY (datasheet_id) REFERENCES datasheets(id),
        CONSTRAINT UQ_datasheet_items_sheet_item_no UNIQUE (datasheet_id, item_no)
    );

    CREATE INDEX IX_datasheet_items_datasheet_id ON datasheet_items(datasheet_id);
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

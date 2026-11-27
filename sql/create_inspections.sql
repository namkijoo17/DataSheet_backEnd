-- 검사 건(헤더) — 검사 시작 시 IN_PROGRESS 로 생성, 완료 시 COMPLETED
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inspections')
BEGIN
    CREATE TABLE inspections (
        id                      BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        datasheet_id            BIGINT NOT NULL,
        user_id                 BIGINT NOT NULL,
        mode                    VARCHAR(30) NOT NULL,
        status                  VARCHAR(20) NOT NULL CONSTRAINT DF_inspections_status DEFAULT ('IN_PROGRESS'),
        serial_no               NVARCHAR(100) NULL,
        unit                    NVARCHAR(100) NULL,
        line                    NVARCHAR(100) NULL,
        inspect_date            DATE NULL,
        inspector               NVARCHAR(100) NULL,
        inspector_change_reason NVARCHAR(200) NULL,
        temperature             NVARCHAR(20) NULL,
        humidity                NVARCHAR(20) NULL,
        completed_at            DATETIME2 NULL,
        result                  VARCHAR(10) NULL,
        ok_count                INT NULL,
        ng_count                INT NULL,
        skip_count              INT NULL,
        file_name               NVARCHAR(255) NULL,
        stored_file_name        VARCHAR(255) NULL,
        created_at              DATETIME2 NOT NULL CONSTRAINT DF_inspections_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at              DATETIME2 NOT NULL CONSTRAINT DF_inspections_updated_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_inspections_datasheet FOREIGN KEY (datasheet_id) REFERENCES datasheets(id),
        CONSTRAINT FK_inspections_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE INDEX IX_inspections_user_status ON inspections(user_id, status);
    CREATE INDEX IX_inspections_datasheet ON inspections(datasheet_id);
END
GO

-- 부품 번호 (TempNo, LCDNo1, ...)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inspection_parts')
BEGIN
    CREATE TABLE inspection_parts (
        inspection_id   BIGINT NOT NULL,
        part_key        VARCHAR(30) NOT NULL,
        part_value      NVARCHAR(100) NULL,
        CONSTRAINT PK_inspection_parts PRIMARY KEY (inspection_id, part_key),
        CONSTRAINT FK_inspection_parts_inspection FOREIGN KEY (inspection_id)
            REFERENCES inspections(id) ON DELETE CASCADE
    );
END
GO

-- 항목별 답변 (항목 1개 = 1행, 항목마다 저장)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inspection_answers')
BEGIN
    CREATE TABLE inspection_answers (
        id                  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        inspection_id       BIGINT NOT NULL,
        datasheet_item_id   BIGINT NOT NULL,
        skipped             BIT NOT NULL CONSTRAINT DF_inspection_answers_skipped DEFAULT (0),
        answer_value        NVARCHAR(1000) NULL,
        slot_index          INT NULL,
        judgment            VARCHAR(10) NULL,
        ng_action           VARCHAR(20) NULL,
        ng_occurred_at      DATETIME2 NULL,
        updated_at          DATETIME2 NOT NULL CONSTRAINT DF_inspection_answers_updated_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_inspection_answers_inspection FOREIGN KEY (inspection_id)
            REFERENCES inspections(id) ON DELETE CASCADE,
        CONSTRAINT UQ_inspection_answers_item UNIQUE (inspection_id, datasheet_item_id)
    );
END
GO

-- 배열 슬롯별 값
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inspection_answer_slot_values')
BEGIN
    CREATE TABLE inspection_answer_slot_values (
        answer_id       BIGINT NOT NULL,
        slot_index      INT NOT NULL,
        slot_value      NVARCHAR(500) NULL,
        CONSTRAINT PK_inspection_answer_slot_values PRIMARY KEY (answer_id, slot_index),
        CONSTRAINT FK_inspection_answer_slot_values_answer FOREIGN KEY (answer_id)
            REFERENCES inspection_answers(id) ON DELETE CASCADE
    );
END
GO

-- 재검사 이력 (항목을 다시 검사할 때 입력한 사유)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inspection_rechecks')
BEGIN
    CREATE TABLE inspection_rechecks (
        id                  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        inspection_id       BIGINT NOT NULL,
        datasheet_item_id   BIGINT NOT NULL,
        item_label          NVARCHAR(200) NULL,
        reason              NVARCHAR(200) NOT NULL,
        previous_summary    NVARCHAR(300) NULL,
        inspector           NVARCHAR(100) NULL,
        created_at          DATETIME2 NOT NULL CONSTRAINT DF_inspection_rechecks_created_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_inspection_rechecks_inspection FOREIGN KEY (inspection_id)
            REFERENCES inspections(id) ON DELETE CASCADE
    );

    CREATE INDEX IX_inspection_rechecks_inspection ON inspection_rechecks(inspection_id);
END
GO

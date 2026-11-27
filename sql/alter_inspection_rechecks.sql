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

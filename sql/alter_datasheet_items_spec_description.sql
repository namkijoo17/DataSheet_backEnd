-- 규격설명 (검사 시 안내용 텍스트)
IF COL_LENGTH('datasheet_items', 'spec_description') IS NULL
BEGIN
    ALTER TABLE datasheet_items ADD spec_description NVARCHAR(500) NULL;
END
GO

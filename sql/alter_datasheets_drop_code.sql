-- datasheets.code 제거 (더 이상 사용하지 않음)
IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_datasheets_code_revision' AND object_id = OBJECT_ID('datasheets'))
BEGIN
    ALTER TABLE datasheets DROP CONSTRAINT UQ_datasheets_code_revision;
END
GO

IF COL_LENGTH('datasheets', 'code') IS NOT NULL
BEGIN
    ALTER TABLE datasheets DROP COLUMN code;
END
GO

-- 검사 완료 결과(판정 집계)와 저장된 결과 PDF 파일
IF COL_LENGTH('inspections', 'result') IS NULL
    ALTER TABLE inspections ADD result VARCHAR(10) NULL;
GO
IF COL_LENGTH('inspections', 'ok_count') IS NULL
    ALTER TABLE inspections ADD ok_count INT NULL;
GO
IF COL_LENGTH('inspections', 'ng_count') IS NULL
    ALTER TABLE inspections ADD ng_count INT NULL;
GO
IF COL_LENGTH('inspections', 'skip_count') IS NULL
    ALTER TABLE inspections ADD skip_count INT NULL;
GO
-- 사용자에게 보이는 파일명
IF COL_LENGTH('inspections', 'file_name') IS NULL
    ALTER TABLE inspections ADD file_name NVARCHAR(255) NULL;
GO
-- 디스크에 저장된 파일명
IF COL_LENGTH('inspections', 'stored_file_name') IS NULL
    ALTER TABLE inspections ADD stored_file_name VARCHAR(255) NULL;
GO

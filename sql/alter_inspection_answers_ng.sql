-- NG 발생 시 처리 방법/발생 시각 (항목 답변과 함께 저장)
IF COL_LENGTH('inspection_answers', 'ng_action') IS NULL
    ALTER TABLE inspection_answers ADD ng_action VARCHAR(20) NULL;
GO

IF COL_LENGTH('inspection_answers', 'ng_occurred_at') IS NULL
    ALTER TABLE inspection_answers ADD ng_occurred_at DATETIME2 NULL;
GO

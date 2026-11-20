IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refresh_tokens')
BEGIN
    CREATE TABLE refresh_tokens (
        id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id         BIGINT NOT NULL,
        token_hash      VARCHAR(64) NOT NULL,
        expires_at      DATETIME2 NOT NULL,
        revoked         BIT NOT NULL CONSTRAINT DF_refresh_tokens_revoked DEFAULT (0),
        created_at      DATETIME2 NOT NULL CONSTRAINT DF_refresh_tokens_created_at DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT UQ_refresh_tokens_token_hash UNIQUE (token_hash)
    );

    CREATE INDEX IX_refresh_tokens_user_id ON refresh_tokens(user_id);
END
GO

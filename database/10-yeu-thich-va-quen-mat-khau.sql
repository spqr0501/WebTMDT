-- Nâng cấp sau file 09, không xóa dữ liệu người dùng.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.nguoi_dung','phien_xac_thuc') IS NULL
  ALTER TABLE dbo.nguoi_dung ADD phien_xac_thuc BIGINT NOT NULL CONSTRAINT df_nguoi_dung_xac_thuc DEFAULT 0;
 IF OBJECT_ID('dbo.banh_yeu_thich','U') IS NULL
 BEGIN
  CREATE TABLE dbo.banh_yeu_thich (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
   ngay_them DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   CONSTRAINT uq_yeu_thich UNIQUE(nguoi_dung_id,san_pham_id)
  );
  CREATE INDEX ix_yeu_thich_nguoi ON dbo.banh_yeu_thich(nguoi_dung_id,ngay_them DESC);
 END;
 IF OBJECT_ID('dbo.ma_dat_lai_mat_khau','U') IS NULL
 BEGIN
  CREATE TABLE dbo.ma_dat_lai_mat_khau (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   ma_bam VARCHAR(64) NOT NULL UNIQUE,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   het_han DATETIME2 NOT NULL,
   da_dung_luc DATETIME2 NULL
  );
  CREATE INDEX ix_ma_dat_lai_nguoi ON dbo.ma_dat_lai_mat_khau(nguoi_dung_id,ngay_tao DESC);
 END;
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung bánh yêu thích và dữ liệu khôi phục mật khẩu.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

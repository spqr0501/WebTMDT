-- Bổ sung đối soát thanh toán. Không tự ghi nhận tiền cho các đơn cũ.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.don_hang','ke_hoach_thanh_toan') IS NULL
  ALTER TABLE dbo.don_hang ADD ke_hoach_thanh_toan VARCHAR(20) NOT NULL CONSTRAINT df_don_ke_hoach DEFAULT 'ON_RECEIPT';
 IF COL_LENGTH('dbo.don_hang','can_tra_truoc') IS NULL
  ALTER TABLE dbo.don_hang ADD can_tra_truoc DECIMAL(14,0) NOT NULL CONSTRAINT df_don_tra_truoc DEFAULT 0;
 IF COL_LENGTH('dbo.don_hang','da_thanh_toan') IS NULL
  ALTER TABLE dbo.don_hang ADD da_thanh_toan DECIMAL(14,0) NOT NULL CONSTRAINT df_don_da_tra DEFAULT 0;
 IF COL_LENGTH('dbo.yeu_cau_banh_rieng','ke_hoach_thanh_toan') IS NULL
  ALTER TABLE dbo.yeu_cau_banh_rieng ADD ke_hoach_thanh_toan VARCHAR(20) NOT NULL CONSTRAINT df_tk_ke_hoach DEFAULT 'ON_RECEIPT';
 IF COL_LENGTH('dbo.yeu_cau_banh_rieng','can_tra_truoc') IS NULL
  ALTER TABLE dbo.yeu_cau_banh_rieng ADD can_tra_truoc DECIMAL(14,0) NOT NULL CONSTRAINT df_tk_tra_truoc DEFAULT 0;
 IF COL_LENGTH('dbo.yeu_cau_banh_rieng','da_thanh_toan') IS NULL
  ALTER TABLE dbo.yeu_cau_banh_rieng ADD da_thanh_toan DECIMAL(14,0) NOT NULL CONSTRAINT df_tk_da_tra DEFAULT 0;
 IF OBJECT_ID('dbo.lich_su_thanh_toan','U') IS NULL
 BEGIN
  CREATE TABLE dbo.lich_su_thanh_toan (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   don_hang_id BIGINT NULL REFERENCES dbo.don_hang(id),
   banh_rieng_id BIGINT NULL REFERENCES dbo.yeu_cau_banh_rieng(id),
   loai VARCHAR(20) NOT NULL CHECK(loai IN ('RECEIPT','REFUND')),
   so_tien DECIMAL(14,0) NOT NULL CHECK(so_tien>0),
   tham_chieu NVARCHAR(200) NOT NULL,
   nguoi_xac_nhan VARCHAR(190) NOT NULL,
   ma_chong_lap VARCHAR(36) NOT NULL UNIQUE,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   CONSTRAINT ck_thanh_toan_mot_don CHECK((don_hang_id IS NOT NULL AND banh_rieng_id IS NULL) OR (don_hang_id IS NULL AND banh_rieng_id IS NOT NULL))
  );
  CREATE INDEX ix_thanh_toan_don ON dbo.lich_su_thanh_toan(don_hang_id,ngay_tao);
  CREATE INDEX ix_thanh_toan_banh_rieng ON dbo.lich_su_thanh_toan(banh_rieng_id,ngay_tao);
 END;
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung tiền cọc, đối soát thanh toán và dữ liệu hóa đơn.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

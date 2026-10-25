-- Chạy sau 01-tao-day-du-sql-server.sql trong SQL Server Management Studio.
-- Chỉ bổ sung cấu trúc; có thể chạy lại, không xóa dữ liệu hiện có.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.san_pham','thanh_phan') IS NULL
  ALTER TABLE dbo.san_pham ADD thanh_phan NVARCHAR(1000) NULL;
 IF COL_LENGTH('dbo.san_pham','chat_gay_di_ung') IS NULL
  ALTER TABLE dbo.san_pham ADD chat_gay_di_ung NVARCHAR(500) NULL;
 IF COL_LENGTH('dbo.san_pham','bao_quan') IS NULL
  ALTER TABLE dbo.san_pham ADD bao_quan NVARCHAR(1000) NULL;
 IF COL_LENGTH('dbo.don_hang','ngay_giao') IS NULL
  ALTER TABLE dbo.don_hang ADD ngay_giao DATE NULL;
 IF COL_LENGTH('dbo.don_hang','khung_gio') IS NULL
  ALTER TABLE dbo.don_hang ADD khung_gio VARCHAR(20) NULL;
 IF COL_LENGTH('dbo.don_hang','loi_chuc') IS NULL
  ALTER TABLE dbo.don_hang ADD loi_chuc NVARCHAR(100) NULL;
 IF COL_LENGTH('dbo.chi_tiet_don_hang','tuy_chon') IS NULL
  ALTER TABLE dbo.chi_tiet_don_hang ADD tuy_chon NVARCHAR(150) NULL;
 IF OBJECT_ID('dbo.anh_san_pham','U') IS NULL
 BEGIN
  CREATE TABLE dbo.anh_san_pham (
   san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
   thu_tu INT NOT NULL,
   duong_dan VARCHAR(500) NOT NULL,
   CONSTRAINT pk_anh_san_pham PRIMARY KEY(san_pham_id,thu_tu)
  );
 END;
 IF OBJECT_ID('dbo.tuy_chon_banh','U') IS NULL
 BEGIN
  CREATE TABLE dbo.tuy_chon_banh (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
   kich_thuoc NVARCHAR(50) NOT NULL,
   huong_vi NVARCHAR(80) NOT NULL,
   gia_them DECIMAL(14,0) NOT NULL DEFAULT 0 CHECK(gia_them>=0),
   dang_ban BIT NOT NULL DEFAULT 1
  );
  CREATE INDEX ix_tuy_chon_san_pham ON dbo.tuy_chon_banh(san_pham_id,dang_ban);
 END;
 IF OBJECT_ID('dbo.danh_gia','U') IS NULL
 BEGIN
  CREATE TABLE dbo.danh_gia (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   so_sao INT NOT NULL CHECK(so_sao BETWEEN 1 AND 5),
   noi_dung NVARCHAR(2000) NOT NULL,
   anh_1 VARCHAR(500) NULL,
   anh_2 VARCHAR(500) NULL,
   anh_3 VARCHAR(500) NULL,
   hien_thi BIT NOT NULL DEFAULT 1,
   ly_do_an NVARCHAR(300) NULL,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   CONSTRAINT uq_danh_gia_nguoi_san_pham UNIQUE(san_pham_id,nguoi_dung_id)
  );
  CREATE INDEX ix_danh_gia_hien_thi ON dbo.danh_gia(san_pham_id,hien_thi,ngay_tao DESC);
 END;
 IF OBJECT_ID('dbo.lich_su_kho','U') IS NULL
 BEGIN
  CREATE TABLE dbo.lich_su_kho (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
   thay_doi INT NOT NULL,
   ton_sau INT NOT NULL CHECK(ton_sau>=0),
   ly_do NVARCHAR(300) NOT NULL,
   nguoi_thuc_hien VARCHAR(190) NOT NULL,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME()
  );
  CREATE INDEX ix_lich_su_kho_san_pham ON dbo.lich_su_kho(san_pham_id,ngay_tao DESC);
 END;
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung đánh giá, bộ ảnh, lựa chọn bánh, lịch giao và lịch sử kho.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

-- Chạy sau 08 trên database đang dùng. Không xóa đơn hoặc tự tạo khoản thu/hoàn.
USE cake_shop;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.don_hang','ngay_hoan_thanh') IS NULL ALTER TABLE dbo.don_hang ADD ngay_hoan_thanh DATETIME2 NULL;
 IF COL_LENGTH('dbo.yeu_cau_banh_rieng','ngay_hoan_thanh') IS NULL ALTER TABLE dbo.yeu_cau_banh_rieng ADD ngay_hoan_thanh DATETIME2 NULL;
 IF COL_LENGTH('dbo.don_hang','tong_da_hoan') IS NULL ALTER TABLE dbo.don_hang ADD tong_da_hoan DECIMAL(14,0) NOT NULL CONSTRAINT df_don_tong_hoan DEFAULT 0;
 IF COL_LENGTH('dbo.yeu_cau_banh_rieng','tong_da_hoan') IS NULL ALTER TABLE dbo.yeu_cau_banh_rieng ADD tong_da_hoan DECIMAL(14,0) NOT NULL CONSTRAINT df_tk_tong_hoan DEFAULT 0;
 IF OBJECT_ID('dbo.yeu_cau_doi_tra','U') IS NULL
 BEGIN
  CREATE TABLE dbo.yeu_cau_doi_tra (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   don_hang_id BIGINT NULL REFERENCES dbo.don_hang(id),
   banh_rieng_id BIGINT NULL REFERENCES dbo.yeu_cau_banh_rieng(id),
   ly_do NVARCHAR(2000) NOT NULL,
   phuong_an VARCHAR(30) NOT NULL CHECK(phuong_an IN ('REFUND','REPLACEMENT')),
   trang_thai VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' CHECK(trang_thai IN ('REQUESTED','APPROVED','REJECTED','COMPLETED')),
   tien_duyet_hoan DECIMAL(14,0) NOT NULL DEFAULT 0 CHECK(tien_duyet_hoan>=0),
   da_hoan DECIMAL(14,0) NOT NULL DEFAULT 0 CHECK(da_hoan>=0),
   phan_hoi NVARCHAR(2000) NULL,
   nguoi_xu_ly VARCHAR(190) NULL,
   ngay_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   ngay_xu_ly DATETIME2 NULL,
   ngay_hoan_tat DATETIME2 NULL,
   CONSTRAINT ck_doi_tra_so_tien CHECK(da_hoan<=tien_duyet_hoan),
   CONSTRAINT ck_doi_tra_mot_don CHECK((don_hang_id IS NOT NULL AND banh_rieng_id IS NULL) OR (don_hang_id IS NULL AND banh_rieng_id IS NOT NULL))
  );
  CREATE UNIQUE INDEX ux_doi_tra_don ON dbo.yeu_cau_doi_tra(don_hang_id) WHERE don_hang_id IS NOT NULL;
  CREATE UNIQUE INDEX ux_doi_tra_banh_rieng ON dbo.yeu_cau_doi_tra(banh_rieng_id) WHERE banh_rieng_id IS NOT NULL;
  CREATE INDEX ix_doi_tra_nguoi_dung ON dbo.yeu_cau_doi_tra(nguoi_dung_id,ngay_gui DESC);
 END;
 IF OBJECT_ID('dbo.anh_doi_tra','U') IS NULL
  CREATE TABLE dbo.anh_doi_tra(yeu_cau_id BIGINT NOT NULL REFERENCES dbo.yeu_cau_doi_tra(id),thu_tu INT NOT NULL,duong_dan VARCHAR(500) NOT NULL,PRIMARY KEY(yeu_cau_id,thu_tu));
 IF COL_LENGTH('dbo.lich_su_thanh_toan','doi_tra_id') IS NULL ALTER TABLE dbo.lich_su_thanh_toan ADD doi_tra_id BIGINT NULL REFERENCES dbo.yeu_cau_doi_tra(id);
 IF OBJECT_ID('dbo.phieu_ho_tro','U') IS NULL
 BEGIN
  CREATE TABLE dbo.phieu_ho_tro (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   chu_de NVARCHAR(150) NOT NULL,
   hoi_thoai_chia_se NVARCHAR(MAX) NULL,
   trang_thai VARCHAR(30) NOT NULL DEFAULT 'OPEN' CHECK(trang_thai IN ('OPEN','IN_PROGRESS','RESOLVED')),
   nhan_vien VARCHAR(190) NULL,
   ma_chong_lap VARCHAR(36) NOT NULL UNIQUE,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   ngay_cap_nhat DATETIME2 NOT NULL DEFAULT SYSDATETIME()
  );
  CREATE INDEX ix_ho_tro_nguoi_dung ON dbo.phieu_ho_tro(nguoi_dung_id,ngay_cap_nhat DESC);
 END;
 IF OBJECT_ID('dbo.tin_nhan_ho_tro','U') IS NULL
 BEGIN
  CREATE TABLE dbo.tin_nhan_ho_tro (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   phieu_id BIGINT NOT NULL REFERENCES dbo.phieu_ho_tro(id),
   tu_nhan_vien BIT NOT NULL DEFAULT 0,
   noi_dung NVARCHAR(2000) NOT NULL,
   nguoi_gui VARCHAR(190) NOT NULL,
   ngay_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME()
  );
  CREATE INDEX ix_tin_nhan_phieu ON dbo.tin_nhan_ho_tro(phieu_id,id);
 END;
 -- Khôi phục tổng hoàn từ sổ đối soát đã có; không thay đổi số tiền ròng đang giữ.
 EXEC(N'UPDATE o SET tong_da_hoan=COALESCE((SELECT SUM(p.so_tien) FROM dbo.lich_su_thanh_toan p WHERE p.don_hang_id=o.id AND p.loai=''REFUND''),0) FROM dbo.don_hang o;');
 EXEC(N'UPDATE o SET tong_da_hoan=COALESCE((SELECT SUM(p.so_tien) FROM dbo.lich_su_thanh_toan p WHERE p.banh_rieng_id=o.id AND p.loai=''REFUND''),0) FROM dbo.yeu_cau_banh_rieng o;');
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung đổi trả, ảnh riêng tư, phiếu hỗ trợ và mốc thời gian bàn giao.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

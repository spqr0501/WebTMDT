-- Nâng cấp dữ liệu hiện có, chạy sau file 04. Không xóa đơn hàng hoặc tài khoản.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.don_hang','hinh_thuc_nhan') IS NULL
  ALTER TABLE dbo.don_hang ADD hinh_thuc_nhan VARCHAR(30) NOT NULL CONSTRAINT df_don_hang_nhan DEFAULT 'DELIVERY';
 IF COL_LENGTH('dbo.don_hang','dat_truoc') IS NULL
  ALTER TABLE dbo.don_hang ADD dat_truoc BIT NOT NULL CONSTRAINT df_don_hang_dat_truoc DEFAULT 0;
 IF COL_LENGTH('dbo.chi_tiet_don_hang','tuy_chon_id') IS NULL
  ALTER TABLE dbo.chi_tiet_don_hang ADD tuy_chon_id BIGINT NULL;
 IF EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='ck_don_hang_trang_thai' AND parent_object_id=OBJECT_ID('dbo.don_hang'))
  ALTER TABLE dbo.don_hang DROP CONSTRAINT ck_don_hang_trang_thai;
 ALTER TABLE dbo.don_hang WITH CHECK ADD CONSTRAINT ck_don_hang_trang_thai
  CHECK(trang_thai IN ('PENDING','CONFIRMED','PREPARING','SHIPPING','READY_FOR_PICKUP','COMPLETED','CANCELLED'));
 IF OBJECT_ID('dbo.thong_bao','U') IS NULL
 BEGIN
  CREATE TABLE dbo.thong_bao (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   tieu_de NVARCHAR(180) NOT NULL,
   noi_dung NVARCHAR(1500) NOT NULL,
   lien_ket VARCHAR(200) NOT NULL,
   da_doc BIT NOT NULL DEFAULT 0,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME()
  );
  CREATE INDEX ix_thong_bao_nguoi_dung ON dbo.thong_bao(nguoi_dung_id,da_doc,ngay_tao DESC);
 END;
 IF OBJECT_ID('dbo.phu_kien','U') IS NULL
 BEGIN
  CREATE TABLE dbo.phu_kien (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   ten NVARCHAR(100) NOT NULL,
   gia DECIMAL(14,0) NOT NULL CHECK(gia>=0),
   ton_kho INT NOT NULL DEFAULT 0 CHECK(ton_kho>=0),
   dang_ban BIT NOT NULL DEFAULT 1,
   phien_ban BIGINT NOT NULL DEFAULT 0
  );
 END;
 IF OBJECT_ID('dbo.phu_kien_don_hang','U') IS NULL
 BEGIN
  CREATE TABLE dbo.phu_kien_don_hang (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   don_hang_id BIGINT NOT NULL REFERENCES dbo.don_hang(id),
   phu_kien_id BIGINT NOT NULL REFERENCES dbo.phu_kien(id),
   ten NVARCHAR(100) NOT NULL,
   don_gia DECIMAL(14,0) NOT NULL CHECK(don_gia>=0),
   so_luong INT NOT NULL CHECK(so_luong BETWEEN 1 AND 20)
  );
  CREATE INDEX ix_phu_kien_don_hang ON dbo.phu_kien_don_hang(don_hang_id);
 END;
 IF OBJECT_ID('dbo.yeu_cau_banh_rieng','U') IS NULL
 BEGIN
  CREATE TABLE dbo.yeu_cau_banh_rieng (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   nguoi_nhan NVARCHAR(100) NOT NULL,
   so_dien_thoai VARCHAR(20) NOT NULL,
   yeu_cau NVARCHAR(3000) NOT NULL,
   huong_vi NVARCHAR(100) NOT NULL,
   so_nguoi INT NOT NULL CHECK(so_nguoi BETWEEN 1 AND 300),
   ngan_sach DECIMAL(14,0) NOT NULL CHECK(ngan_sach>=10000),
   ngay_nhan DATE NOT NULL,
   khung_gio VARCHAR(20) NOT NULL,
   hinh_thuc_nhan VARCHAR(30) NOT NULL CHECK(hinh_thuc_nhan IN ('DELIVERY','PICKUP')),
   dia_chi NVARCHAR(500) NOT NULL,
   phuong_thuc_thanh_toan VARCHAR(30) NOT NULL CHECK(phuong_thuc_thanh_toan IN ('COD','BANK_TRANSFER')),
   anh_tham_khao VARCHAR(500) NULL,
   loi_chuc NVARCHAR(100) NULL,
   trang_thai VARCHAR(30) NOT NULL DEFAULT 'SENT' CHECK(trang_thai IN ('SENT','QUOTED','ACCEPTED','PREPARING','READY','COMPLETED','REJECTED','CANCELLED')),
   tong_bao_gia DECIMAL(14,0) NULL CHECK(tong_bao_gia IS NULL OR tong_bao_gia>=10000),
   phan_hoi NVARCHAR(1500) NULL,
   ma_chong_dat_trung VARCHAR(36) NOT NULL UNIQUE,
   ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   ngay_cap_nhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   phien_ban BIGINT NOT NULL DEFAULT 0
  );
  CREATE INDEX ix_banh_rieng_nguoi_dung ON dbo.yeu_cau_banh_rieng(nguoi_dung_id,ngay_tao DESC);
  CREATE INDEX ix_banh_rieng_trang_thai ON dbo.yeu_cau_banh_rieng(trang_thai,ngay_nhan);
 END;
 -- Phụ kiện mẫu: quản trị viên chỉnh giá và tồn thực tế trước khi bán.
 IF NOT EXISTS(SELECT 1 FROM dbo.phu_kien WHERE ten=N'Nến sinh nhật')
  INSERT dbo.phu_kien(ten,gia,ton_kho,dang_ban) VALUES(N'Nến sinh nhật',10000,0,1);
 IF NOT EXISTS(SELECT 1 FROM dbo.phu_kien WHERE ten=N'Thiệp chúc mừng')
  INSERT dbo.phu_kien(ten,gia,ton_kho,dang_ban) VALUES(N'Thiệp chúc mừng',15000,0,1);
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung thông báo, nhận tại cửa hàng, phụ kiện và bánh thiết kế riêng.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

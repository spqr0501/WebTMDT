/*
TẠO CƠ SỞ DỮ LIỆU TIỆM BÁNH - MICROSOFT SQL SERVER 2012 TRỞ LÊN
Cách chạy: mở file này trong SQL Server Management Studio, chọn toàn bộ rồi F5.
Tài khoản chạy cần quyền tạo cơ sở dữ liệu và tạo bảng.
File gồm 8 bảng, khóa chính/ngoại, ràng buộc, chỉ mục và dữ liệu mẫu.
Tên bảng/cột dùng tiếng Việt không dấu; nội dung dùng Unicode có dấu.
Không xóa dữ liệu. Nếu đã có một trong các bảng, file dừng để tránh ghi đè.
Không chạy các file 01-schema.sql / 02-seed.sql của MySQL trong SQL Server.
Các mã USER, ADMIN, PENDING... giữ nguyên để tương ứng với mã nguồn Java.
*/
USE [master];
GO
IF DB_ID(N'cake_shop') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [cake_shop] COLLATE Vietnamese_100_CI_AS');
END;
GO
USE [cake_shop];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Kiểm tra đúng cơ sở dữ liệu trước khi tạo bất kỳ bảng nào.
IF DB_NAME() <> N'cake_shop'
    THROW 50001, N'Chưa chọn được cơ sở dữ liệu cake_shop. Kiểm tra quyền tạo cơ sở dữ liệu.', 1;

IF EXISTS (
    SELECT 1 FROM sys.tables
    WHERE schema_id = SCHEMA_ID(N'dbo') AND name IN (
        N'nguoi_dung', N'danh_muc', N'san_pham', N'dia_chi',
        N'ma_giam_gia', N'don_hang', N'chi_tiet_don_hang', N'luot_su_dung_ma'
    )
)
    THROW 50002, N'Đã có bảng của tiệm bánh. File đã dừng để giữ dữ liệu; không cần tạo lại nếu lần trước đã thành công.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

-- 1. Tài khoản khách hàng và quản trị viên
CREATE TABLE dbo.nguoi_dung (
 id BIGINT IDENTITY(1,1) PRIMARY KEY,
 ten NVARCHAR(100) NOT NULL, email VARCHAR(190) NOT NULL UNIQUE,
 mat_khau VARCHAR(255) NOT NULL, vai_tro VARCHAR(20) NOT NULL DEFAULT 'USER', duoc_hoat_dong BIT NOT NULL DEFAULT 1,
 CONSTRAINT ck_nguoi_dung_vai_tro CHECK (vai_tro IN ('USER','ADMIN'))
);

-- 2. Danh mục bánh
CREATE TABLE dbo.danh_muc (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, ten NVARCHAR(100) NOT NULL UNIQUE, mo_ta NVARCHAR(1000)
);

-- 3. Sản phẩm và tồn kho
CREATE TABLE dbo.san_pham (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, ten NVARCHAR(150) NOT NULL, mo_ta NVARCHAR(MAX) NOT NULL,
 gia DECIMAL(14,0) NOT NULL, ton_kho INT NOT NULL DEFAULT 0, danh_muc_id BIGINT NOT NULL,
 duong_dan_anh VARCHAR(500), duong_dan_video VARCHAR(500), noi_bat BIT NOT NULL DEFAULT 0,
 dang_ap_dung BIT NOT NULL DEFAULT 1, phien_ban BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT fk_san_pham_danh_muc FOREIGN KEY(danh_muc_id) REFERENCES dbo.danh_muc(id),
 CONSTRAINT ck_san_pham_gia CHECK(gia >= 1000), CONSTRAINT ck_san_pham_ton_kho CHECK(ton_kho >= 0)
);

-- 4. Địa chỉ giao hàng
CREATE TABLE dbo.dia_chi (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, nguoi_dung_id BIGINT NOT NULL, nguoi_nhan NVARCHAR(100) NOT NULL,
 so_dien_thoai VARCHAR(20) NOT NULL, dia_chi_chi_tiet NVARCHAR(500) NOT NULL,
 CONSTRAINT fk_dia_chi_nguoi_dung FOREIGN KEY(nguoi_dung_id) REFERENCES dbo.nguoi_dung(id)
);

-- 5. Mã giảm giá
CREATE TABLE dbo.ma_giam_gia (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, ma VARCHAR(40) NOT NULL UNIQUE,
 loai VARCHAR(20) NOT NULL, gia_tri_giam DECIMAL(14,0) NOT NULL, gia_tri_don_toi_thieu DECIMAL(14,0) NOT NULL DEFAULT 0,
 giam_toi_da DECIMAL(14,0), bat_dau_luc DATETIME2(6) NOT NULL, ket_thuc_luc DATETIME2(6) NOT NULL,
 gioi_han_luot_dung INT NOT NULL, gioi_han_moi_nguoi INT NOT NULL, so_luot_da_dung INT NOT NULL DEFAULT 0, dang_ap_dung BIT NOT NULL DEFAULT 1,
 CONSTRAINT ck_ma_giam_gia_loai CHECK(loai IN ('PERCENT','FIXED')),
 CONSTRAINT ck_ma_giam_gia_gia_tri CHECK(gia_tri_giam>0 AND (loai<>'PERCENT' OR gia_tri_giam<=100)),
 CONSTRAINT ck_ma_giam_gia_don_toi_thieu CHECK(gia_tri_don_toi_thieu>=0),
 CONSTRAINT ck_ma_giam_gia_muc_toi_da CHECK(giam_toi_da IS NULL OR giam_toi_da>0),
 CONSTRAINT ck_ma_giam_gia_thoi_han CHECK(ket_thuc_luc>bat_dau_luc),
 CONSTRAINT ck_ma_giam_gia_gioi_han CHECK(gioi_han_luot_dung>0 AND gioi_han_moi_nguoi>0 AND so_luot_da_dung>=0 AND so_luot_da_dung<=gioi_han_luot_dung)
);

-- 6. Đơn hàng
CREATE TABLE dbo.don_hang (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, nguoi_dung_id BIGINT NOT NULL,
 trang_thai VARCHAR(30) NOT NULL DEFAULT 'PENDING', phuong_thuc_thanh_toan VARCHAR(20) NOT NULL,
 nguoi_nhan NVARCHAR(100) NOT NULL, so_dien_thoai VARCHAR(20) NOT NULL, dia_chi_giao_hang NVARCHAR(500) NOT NULL, ghi_chu NVARCHAR(500),
 tien_hang DECIMAL(14,0) NOT NULL, tien_giam DECIMAL(14,0) NOT NULL DEFAULT 0,
 phi_giao_hang DECIMAL(14,0) NOT NULL DEFAULT 0, tong_thanh_toan DECIMAL(14,0) NOT NULL,
 ma_uu_dai VARCHAR(40), ma_chong_dat_trung VARCHAR(36) NOT NULL UNIQUE, ngay_tao DATETIME2(6) NOT NULL,
 CONSTRAINT fk_don_hang_nguoi_dung FOREIGN KEY(nguoi_dung_id) REFERENCES dbo.nguoi_dung(id),
 CONSTRAINT ck_don_hang_trang_thai CHECK(trang_thai IN ('PENDING','PREPARING','SHIPPING','COMPLETED','CANCELLED')),
 CONSTRAINT ck_don_hang_thanh_toan CHECK(phuong_thuc_thanh_toan IN ('COD','BANK_TRANSFER')),
 CONSTRAINT ck_don_hang_tong_tien CHECK(tien_hang>=0 AND tien_giam>=0 AND tien_giam<=tien_hang AND phi_giao_hang>=0 AND tong_thanh_toan=tien_hang-tien_giam+phi_giao_hang)
);

-- 7. Chi tiết từng đơn hàng
CREATE TABLE dbo.chi_tiet_don_hang (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, don_hang_id BIGINT NOT NULL, san_pham_id BIGINT NOT NULL,
 ten_san_pham NVARCHAR(150) NOT NULL, don_gia DECIMAL(14,0) NOT NULL, so_luong INT NOT NULL,
 CONSTRAINT fk_chi_tiet_don_hang FOREIGN KEY(don_hang_id) REFERENCES dbo.don_hang(id),
 CONSTRAINT fk_chi_tiet_san_pham FOREIGN KEY(san_pham_id) REFERENCES dbo.san_pham(id),
 CONSTRAINT ck_chi_tiet_so_luong CHECK(so_luong BETWEEN 1 AND 99), CONSTRAINT ck_chi_tiet_don_gia CHECK(don_gia>=0)
);

-- 8. Lịch sử sử dụng mã giảm giá
CREATE TABLE dbo.luot_su_dung_ma (
 id BIGINT IDENTITY(1,1) PRIMARY KEY, ma_giam_gia_id BIGINT NOT NULL, nguoi_dung_id BIGINT NOT NULL, don_hang_id BIGINT NOT NULL UNIQUE,
 CONSTRAINT fk_luot_dung_ma_giam_gia FOREIGN KEY(ma_giam_gia_id) REFERENCES dbo.ma_giam_gia(id),
 CONSTRAINT fk_luot_dung_nguoi_dung FOREIGN KEY(nguoi_dung_id) REFERENCES dbo.nguoi_dung(id),
 CONSTRAINT fk_luot_dung_don_hang FOREIGN KEY(don_hang_id) REFERENCES dbo.don_hang(id)
);

-- Chỉ mục hỗ trợ tìm bánh, xem đơn và kiểm tra lượt dùng mã.
CREATE INDEX ix_san_pham_danh_muc_gia ON dbo.san_pham (dang_ap_dung,danh_muc_id,gia);
CREATE INDEX ix_don_hang_nguoi_dung_ngay ON dbo.don_hang (nguoi_dung_id,ngay_tao);
CREATE INDEX ix_don_hang_trang_thai ON dbo.don_hang (trang_thai,ngay_tao);
CREATE INDEX ix_luot_dung_ma_nguoi_dung ON dbo.luot_su_dung_ma (ma_giam_gia_id,nguoi_dung_id);
CREATE INDEX ix_dia_chi_nguoi_dung ON dbo.dia_chi(nguoi_dung_id);
CREATE INDEX ix_chi_tiet_don_hang ON dbo.chi_tiet_don_hang(don_hang_id);
CREATE INDEX ix_chi_tiet_san_pham ON dbo.chi_tiet_don_hang(san_pham_id);


-- DỮ LIỆU MẪU: 3 danh mục, 6 sản phẩm, 2 mã giảm giá.
INSERT INTO dbo.danh_muc(ten, mo_ta) VALUES
(N'Bánh kem', N'Bánh kem tươi cho những dịp đáng nhớ'),
(N'Bánh mousse', N'Mềm mịn, thanh nhẹ, tan trong miệng'),
(N'Bánh quy', N'Giòn thơm cho những buổi trà chiều');

DECLARE @banh_kem BIGINT = (SELECT id FROM dbo.danh_muc WHERE ten = N'Bánh kem');
DECLARE @banh_mousse BIGINT = (SELECT id FROM dbo.danh_muc WHERE ten = N'Bánh mousse');
DECLARE @banh_quy BIGINT = (SELECT id FROM dbo.danh_muc WHERE ten = N'Bánh quy');

INSERT INTO dbo.san_pham
(ten, mo_ta, gia, ton_kho, danh_muc_id, duong_dan_anh, noi_bat, dang_ap_dung, phien_ban)
VALUES
(N'Bánh kem dâu tây', N'Bánh kem dâu tây với cốt vani mềm, kem tươi và dâu. Kích thước 16 cm, phù hợp 4–6 người. Bảo quản lạnh 2–6°C. Có chứa sữa, trứng và gluten.', 320000, 12, @banh_kem, '/images/cake.svg', 1, 1, 0),
(N'Bánh mousse sô-cô-la', N'Bánh mousse sô-cô-la với hương ca cao đậm, vị ngọt vừa. Kích thước 16 cm. Có chứa sữa, trứng và gluten.', 350000, 8, @banh_mousse, '/images/mousse.svg', 1, 1, 0),
(N'Hộp bánh quy bơ', N'Hộp 12 bánh quy bơ thơm giòn. Dùng cùng trà hoặc cà phê. Có chứa sữa, trứng và gluten.', 125000, 25, @banh_quy, '/images/cookie.svg', 1, 1, 0),
(N'Bánh kem trà xanh', N'Bánh kem trà xanh, cốt bánh mềm và lớp kem trà xanh. Kích thước 16 cm. Có chứa sữa, trứng và gluten.', 380000, 4, @banh_kem, '/images/matcha.svg', 1, 1, 0),
(N'Bánh mousse dâu mọng', N'Bánh mousse dâu mọng với lớp thạch quả đỏ. Kích thước 16 cm. Có chứa sữa, trứng và gluten.', 365000, 7, @banh_mousse, '/images/berry.svg', 1, 1, 0),
(N'Bánh kem chanh vàng', N'Bánh kem chanh vàng dịu nhẹ, thơm mát. Kích thước 16 cm. Có chứa sữa, trứng và gluten.', 295000, 10, @banh_kem, '/images/lemon.svg', 1, 1, 0);

-- Giờ Việt Nam (UTC+7), không phụ thuộc múi giờ máy chủ.
DECLARE @hien_tai DATETIME2(6) = DATEADD(HOUR, 7, SYSUTCDATETIME());
INSERT INTO dbo.ma_giam_gia
(ma, loai, gia_tri_giam, gia_tri_don_toi_thieu, giam_toi_da, bat_dau_luc, ket_thuc_luc,
 gioi_han_luot_dung, gioi_han_moi_nguoi, so_luot_da_dung, dang_ap_dung)
VALUES
('WELCOME10', 'PERCENT', 10, 200000, 50000, @hien_tai, DATEADD(DAY, 90, @hien_tai), 100, 1, 0, 1),
('SWEET30', 'FIXED', 30000, 300000, NULL, @hien_tai, DATEADD(DAY, 90, @hien_tai), 50, 2, 0, 1);

-- Không tạo tài khoản với mật khẩu rõ hoặc mật khẩu giả.
-- Tạo khách hàng bằng trang đăng ký; tạo quản trị viên qua ADMIN_EMAIL/ADMIN_PASSWORD
-- sau khi ứng dụng Spring Boot đã được cấu hình kết nối SQL Server.

    COMMIT TRANSACTION;
    PRINT N'Đã tạo thành công 8 bảng, 3 danh mục, 6 bánh và 2 mã giảm giá.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- GIẢI THÍCH CÁC GIÁ TRỊ KỸ THUẬT TRONG BẢNG:
-- vai_tro: USER = Khách hàng; ADMIN = Quản trị viên.
-- loai: PERCENT = Giảm phần trăm; FIXED = Giảm số tiền cố định.
-- trang_thai: PENDING = Chờ xác nhận; PREPARING = Đang chuẩn bị;
-- SHIPPING = Đang giao; COMPLETED = Hoàn thành; CANCELLED = Đã hủy.
-- phuong_thuc_thanh_toan: COD = Trả tiền khi nhận hàng;
-- BANK_TRANSFER = Chuyển khoản ngân hàng.
-- Các trạng thái vẫn dùng mã kỹ thuật để khớp các enum của ứng dụng Java.


-- PHẦN BỔ SUNG: BỘ ẢNH, ĐÁNH GIÁ, LỊCH GIAO VÀ TỒN KHO
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


-- NÂNG CẤP THƯƠNG MẠI ĐIỆN TỬ
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


-- BỔ SUNG DANH MỤC BÁNH
-- Bổ sung danh mục, không thay đổi danh mục hoặc sản phẩm hiện có.
-- Có thể chạy lại; chỉ thêm tên chưa tồn tại.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 INSERT INTO dbo.danh_muc(ten,mo_ta)
 SELECT moi.ten,moi.mo_ta
 FROM (VALUES
  (N'Bánh bông lan',N'Bánh bông lan mềm xốp, thơm nhẹ, phù hợp bữa sáng và giờ trà.'),
  (N'Bánh phô mai',N'Các loại bánh phô mai béo mịn, kết hợp trái cây hoặc sô-cô-la.'),
  (N'Bánh su kem',N'Vỏ bánh su nhẹ, nhân kem mịn với nhiều hương vị.'),
  (N'Bánh tart',N'Đế bánh giòn với nhân trứng, kem hoặc trái cây.'),
  (N'Bánh cuộn',N'Cốt bánh mềm cuộn cùng kem, mứt hoặc trái cây.'),
  (N'Bánh sừng bò',N'Bánh ngàn lớp thơm bơ, có loại nguyên bản và các loại nhân.'),
  (N'Bánh cupcake',N'Bánh nhỏ xinh, trang trí kem, phù hợp tiệc và quà tặng.'),
  (N'Bánh tiramisu',N'Bánh vị cà phê và kem phô mai, phủ ca cao.'),
  (N'Bánh macaron',N'Bánh hạnh nhân nhỏ nhiều màu, kẹp nhân kem hoặc mứt.'),
  (N'Bánh donut',N'Bánh vòng mềm, phủ đường hoặc sô-cô-la với nhiều kiểu trang trí.')
 ) AS moi(ten,mo_ta)
 WHERE NOT EXISTS (
  SELECT 1 FROM dbo.danh_muc AS cu WITH (UPDLOCK,HOLDLOCK)
  WHERE cu.ten=moi.ten
 );
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung các danh mục bánh mới.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO


-- TIỀN CỌC, ĐỐI SOÁT VÀ HÓA ĐƠN
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

-- BỔ SUNG 08: DUYỆT MẪU VÀ LỊCH SẢN XUẤT
-- Chạy sau file 07. Bổ sung dữ liệu, không xóa hay đổi trạng thái đơn cũ.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF OBJECT_ID('dbo.mau_banh_thiet_ke','U') IS NULL
 BEGIN
  CREATE TABLE dbo.mau_banh_thiet_ke (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   banh_rieng_id BIGINT NOT NULL REFERENCES dbo.yeu_cau_banh_rieng(id),
   anh_mau VARCHAR(500) NOT NULL,
   noi_dung NVARCHAR(1500) NOT NULL,
   trang_thai VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK(trang_thai IN ('PENDING','APPROVED','CHANGES_REQUESTED')),
   y_kien_khach NVARCHAR(1000) NULL,
   nguoi_gui VARCHAR(190) NOT NULL,
   ngay_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   ngay_phan_hoi DATETIME2 NULL
  );
  CREATE INDEX ix_mau_banh_yeu_cau ON dbo.mau_banh_thiet_ke(banh_rieng_id,id DESC);
 END;
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.don_hang') AND name='ix_don_lich_giao')
  CREATE INDEX ix_don_lich_giao ON dbo.don_hang(ngay_giao,khung_gio);
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.yeu_cau_banh_rieng') AND name='ix_banh_rieng_lich_nhan')
  CREATE INDEX ix_banh_rieng_lich_nhan ON dbo.yeu_cau_banh_rieng(ngay_nhan,khung_gio);
 -- Một thông báo tổng hợp cho mỗi admin về kho thấp đã có trước đợt nâng cấp.
 IF EXISTS(SELECT 1 FROM dbo.san_pham WHERE dang_ap_dung=1 AND ton_kho<5)
  INSERT INTO dbo.thong_bao(nguoi_dung_id,tieu_de,noi_dung,lien_ket,da_doc,ngay_tao)
  SELECT u.id,N'Kiểm tra tồn kho khi nâng cấp',N'Có bánh sắp hết hoặc đã hết. Bấm để xem danh sách và điều chỉnh tồn kho.',
   '/admin/products?stock=warning',0,SYSDATETIME()
  FROM dbo.nguoi_dung u WHERE u.vai_tro='ADMIN' AND u.duoc_hoat_dong=1
  AND NOT EXISTS(SELECT 1 FROM dbo.thong_bao n WHERE n.nguoi_dung_id=u.id AND n.tieu_de=N'Kiểm tra tồn kho khi nâng cấp');
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung duyệt mẫu, chỉ mục lịch sản xuất và thông báo tồn kho.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

-- BỔ SUNG 09: ĐỔI TRẢ VÀ HỖ TRỢ HỘI THOẠI
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

-- BỔ SUNG 10: YÊU THÍCH VÀ QUÊN MẬT KHẨU
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

-- BỔ SUNG 11: MỖI DANH MỤC BA BÁNH
-- Chạy sau 10. Chỉ bổ sung để mỗi danh mục mẫu có ít nhất 3 bánh đang bán.
-- Không tạo bánh trung thu; không sửa giá, ảnh, tồn kho hoặc xóa bánh hiện có.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @mau TABLE(stt INT IDENTITY(1,1),danh_muc NVARCHAR(100),ten NVARCHAR(150),gia DECIMAL(14,0),anh VARCHAR(500),mo_ta NVARCHAR(1000));
 INSERT INTO @mau(danh_muc,ten,gia,anh,mo_ta) VALUES
 (N'Bánh kem',N'Bánh kem dâu tây',320000,'/images/berry.svg',N'Mẫu bánh kem vị dâu cho dịp sinh nhật.'),
 (N'Bánh kem',N'Bánh kem trà xanh',380000,'/images/matcha.svg',N'Mẫu bánh kem trà xanh, trang trí theo dịp.'),
 (N'Bánh kem',N'Bánh kem chanh vàng',295000,'/images/lemon.svg',N'Mẫu bánh kem chanh vàng dành cho buổi gặp mặt.'),
 (N'Bánh mousse',N'Bánh mousse sô-cô-la',350000,'/images/mousse.svg',N'Mẫu mousse sô-cô-la với các lớp bánh mềm.'),
 (N'Bánh mousse',N'Bánh mousse dâu mọng',365000,'/images/berry.svg',N'Mẫu mousse dâu dành cho những buổi tiệc nhỏ.'),
 (N'Bánh mousse',N'Bánh mousse chanh dây',340000,'/images/lemon.svg',N'Mousse chủ đề chanh dây, màu vàng tươi sáng.'),
 (N'Bánh quy',N'Hộp bánh quy bơ',125000,'/images/cookie.svg',N'Hộp bánh quy bơ dùng làm quà hoặc dùng cùng trà.'),
 (N'Bánh quy',N'Bánh quy hạt sô-cô-la',135000,'/images/cookie.svg',N'Hộp bánh quy chủ đề sô-cô-la, tiện chia sẻ.'),
 (N'Bánh quy',N'Bánh quy trà xanh',145000,'/images/matcha.svg',N'Hộp bánh quy trà xanh dành cho buổi trà chiều.'),
 (N'Bánh bông lan',N'Bông lan vani',95000,'/images/cake.svg',N'Mẫu bông lan vani cho bữa ăn nhẹ.'),
 (N'Bánh bông lan',N'Bông lan sô-cô-la',110000,'/images/mousse.svg',N'Mẫu bông lan vị sô-cô-la, đóng hộp mang đi.'),
 (N'Bánh bông lan',N'Bông lan chanh',105000,'/images/lemon.svg',N'Mẫu bông lan chanh cho buổi gặp bạn bè.'),
 (N'Bánh phô mai',N'Bánh phô mai cổ điển',285000,'/images/cake.svg',N'Mẫu bánh phô mai phong cách cổ điển.'),
 (N'Bánh phô mai',N'Bánh phô mai dâu',310000,'/images/berry.svg',N'Mẫu bánh phô mai trang trí chủ đề dâu.'),
 (N'Bánh phô mai',N'Bánh phô mai trà xanh',320000,'/images/matcha.svg',N'Mẫu bánh phô mai kết hợp chủ đề trà xanh.'),
 (N'Bánh su kem',N'Su kem vani',65000,'/images/cake.svg',N'Hộp su kem vị vani cho phần quà nhỏ.'),
 (N'Bánh su kem',N'Su kem sô-cô-la',75000,'/images/mousse.svg',N'Hộp su kem chủ đề sô-cô-la.'),
 (N'Bánh su kem',N'Su kem trà xanh',80000,'/images/matcha.svg',N'Hộp su kem chủ đề trà xanh cho buổi trà chiều.'),
 (N'Bánh tart',N'Tart trứng',55000,'/images/cake.svg',N'Mẫu tart trứng đóng hộp theo phần.'),
 (N'Bánh tart',N'Tart dâu tươi',85000,'/images/berry.svg',N'Mẫu tart trang trí chủ đề dâu tươi.'),
 (N'Bánh tart',N'Tart chanh',75000,'/images/lemon.svg',N'Mẫu tart chanh với sắc vàng cho bàn tiệc.'),
 (N'Bánh cuộn',N'Bánh cuộn vani',165000,'/images/cake.svg',N'Mẫu bánh cuộn vani để chia thành từng phần.'),
 (N'Bánh cuộn',N'Bánh cuộn dâu',185000,'/images/berry.svg',N'Mẫu bánh cuộn chủ đề dâu.'),
 (N'Bánh cuộn',N'Bánh cuộn trà xanh',195000,'/images/matcha.svg',N'Mẫu bánh cuộn chủ đề trà xanh.'),
 (N'Bánh sừng bò',N'Sừng bò bơ',45000,'/images/cake.svg',N'Mẫu bánh sừng bò bơ cho bữa sáng.'),
 (N'Bánh sừng bò',N'Sừng bò sô-cô-la',55000,'/images/mousse.svg',N'Mẫu bánh sừng bò chủ đề sô-cô-la.'),
 (N'Bánh sừng bò',N'Sừng bò hạnh nhân',65000,'/images/cookie.svg',N'Mẫu bánh sừng bò chủ đề hạnh nhân.'),
 (N'Bánh cupcake',N'Cupcake vani',45000,'/images/cake.svg',N'Cupcake vani cho quà tặng và tiệc nhỏ.'),
 (N'Bánh cupcake',N'Cupcake sô-cô-la',50000,'/images/mousse.svg',N'Cupcake chủ đề sô-cô-la, phần bánh nhỏ.'),
 (N'Bánh cupcake',N'Cupcake dâu',55000,'/images/berry.svg',N'Cupcake trang trí chủ đề dâu.'),
 (N'Bánh tiramisu',N'Tiramisu cà phê',285000,'/images/mousse.svg',N'Mẫu tiramisu chủ đề cà phê.'),
 (N'Bánh tiramisu',N'Tiramisu trà xanh',305000,'/images/matcha.svg',N'Mẫu tiramisu chủ đề trà xanh.'),
 (N'Bánh tiramisu',N'Tiramisu dâu',315000,'/images/berry.svg',N'Mẫu tiramisu chủ đề dâu cho dịp đặc biệt.'),
 (N'Bánh macaron',N'Hộp macaron vani',155000,'/images/cookie.svg',N'Hộp macaron chủ đề vani, làm quà nhỏ.'),
 (N'Bánh macaron',N'Hộp macaron sô-cô-la',165000,'/images/mousse.svg',N'Hộp macaron chủ đề sô-cô-la.'),
 (N'Bánh macaron',N'Hộp macaron dâu',175000,'/images/berry.svg',N'Hộp macaron chủ đề dâu với tông hồng.'),
 (N'Bánh donut',N'Donut đường',35000,'/images/cake.svg',N'Mẫu donut đường theo phần.'),
 (N'Bánh donut',N'Donut sô-cô-la',45000,'/images/mousse.svg',N'Mẫu donut chủ đề sô-cô-la.'),
 (N'Bánh donut',N'Donut dâu',45000,'/images/berry.svg',N'Mẫu donut chủ đề dâu, dùng cho tiệc nhỏ.');
 DECLARE @moi TABLE(id BIGINT,ton INT);
 DECLARE @i INT=1,@max INT=(SELECT MAX(stt) FROM @mau),@dm NVARCHAR(100),@ten NVARCHAR(150),@gia DECIMAL(14,0),@anh VARCHAR(500),@mota NVARCHAR(1000),@dm_id BIGINT;
 WHILE @i<=@max
 BEGIN
  SELECT @dm=danh_muc,@ten=ten,@gia=gia,@anh=anh,@mota=mo_ta FROM @mau WHERE stt=@i;
  SET @dm_id=NULL;
  SELECT @dm_id=id FROM dbo.danh_muc WITH(UPDLOCK,HOLDLOCK) WHERE ten=@dm;
  IF @dm_id IS NOT NULL AND (SELECT COUNT(*) FROM dbo.san_pham WHERE danh_muc_id=@dm_id AND dang_ap_dung=1)<3
    AND NOT EXISTS(SELECT 1 FROM dbo.san_pham WHERE danh_muc_id=@dm_id AND ten=@ten)
   INSERT INTO dbo.san_pham(ten,mo_ta,gia,ton_kho,danh_muc_id,duong_dan_anh,noi_bat,dang_ap_dung,phien_ban)
   OUTPUT inserted.id,inserted.ton_kho INTO @moi
   VALUES(@ten,@mota+N' Dữ liệu mẫu của cửa hàng; ảnh thực tế, thành phần, dị ứng và bảo quản sẽ được quản trị xác nhận.',@gia,12,@dm_id,@anh,0,1,0);
  SET @i=@i+1;
 END;
 INSERT INTO dbo.lich_su_kho(san_pham_id,thay_doi,ton_sau,ly_do,nguoi_thuc_hien,ngay_tao)
 SELECT id,ton,ton,N'Tồn ban đầu khi bổ sung bánh mẫu theo danh mục','SQL-11',SYSDATETIME() FROM @moi;
 SELECT COUNT(*) AS so_banh_vua_bo_sung FROM @moi;
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung bánh mẫu; không thêm bánh trung thu, không sửa sản phẩm có sẵn.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

-- BO SUNG 12: ANH VA MO TA BANH
-- Chạy sau 11. Ảnh Internet có nguồn tại /image-credits; không dùng ảnh AI.
-- Chỉ cập nhật 39 bánh mẫu đang dùng SVG hoặc bộ ảnh mẫu này. Giữ nguyên ảnh admin upload, giá, tồn kho và thông tin dị ứng.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @anh TABLE(ten NVARCHAR(150),anh VARCHAR(500),mo_ta NVARCHAR(MAX));
 INSERT INTO @anh VALUES
 (N'Bánh kem dâu tây','/images/banh/banh-kem-dau-tay.jpg',N'Bánh kem dâu tây kết hợp cốt bánh mềm, lớp kem mịn và sắc đỏ của dâu. Vị trái cây chua nhẹ giúp cân bằng phần kem, phù hợp cho sinh nhật, kỷ niệm hoặc một buổi gặp gỡ gia đình. Phong cách trang trí nhẹ nhàng, có thể ghi lời chúc khi đặt.'),
 (N'Bánh kem trà xanh','/images/banh/banh-kem-tra-xanh.jpg',N'Bánh kem trà xanh có các lớp cốt bánh xanh xen kem, mang hương trà dịu và hậu vị hơi đắng đặc trưng. Phù hợp với người thích bánh có hương vị trà rõ ràng và cách trang trí thanh nhã. Ảnh minh họa có thêm đậu đỏ; thành phần và trang trí thực tế cần xác nhận với cửa hàng.'),
 (N'Bánh kem chanh vàng','/images/banh/banh-kem-chanh-vang.jpg',N'Bánh kem chanh vàng kết hợp cốt bánh mềm và lớp kem sáng màu, nhấn bằng hương chanh thanh mát. Vị chua nhẹ tạo điểm cân bằng cho phần kem béo. Thích hợp cho tiệc trà, sinh nhật hoặc những buổi gặp mặt yêu thích phong cách trang trí đơn giản.'),
 (N'Bánh mousse sô-cô-la','/images/banh/mousse-so-co-la.jpg',N'Bánh mousse sô-cô-la nổi bật với lớp mousse mềm mượt và vị cacao đậm. Kết cấu nhẹ hơn bánh kem truyền thống, các lớp bánh và mousse tạo cảm giác tan mềm khi thưởng thức. Phù hợp làm món tráng miệng hoặc quà tặng cho người yêu thích sô-cô-la.'),
 (N'Bánh mousse dâu mọng','/images/banh/mousse-dau-mong.jpg',N'Bánh mousse dâu mọng có lớp mousse hồng mềm, xen cốt bánh mỏng và lớp phủ dâu tươi sáng. Hương dâu chua ngọt kết hợp phần kem dịu, thích hợp dùng sau bữa ăn hoặc cho buổi tiệc nhỏ. Mẫu minh họa dạng lát giúp nhìn rõ cấu trúc các lớp bánh.'),
 (N'Bánh mousse chanh dây','/images/banh/mousse-chanh-day.jpg',N'Bánh mousse chanh dây mang hương trái cây nhiệt đới, lớp mousse nhẹ và vị chua thanh. Lớp phủ vàng tạo điểm nhấn trên phần bánh sáng màu, phù hợp cho người muốn đổi vị từ các dòng bánh kem quen thuộc. Ảnh minh họa trang trí thêm quả mọng; mẫu thực tế được xác nhận khi đặt.'),
 (N'Hộp bánh quy bơ','/images/banh/banh-quy-bo.jpg',N'Hộp bánh quy bơ gồm những chiếc bánh nhỏ nướng vàng với hương bơ đặc trưng. Kết cấu giòn xốp phù hợp ăn nhẹ hoặc thưởng thức cùng trà và cà phê. Có thể chọn làm món quà giản dị cho bạn bè, đồng nghiệp; quy cách hộp và số lượng cần xác nhận trước khi mua.'),
 (N'Bánh quy hạt sô-cô-la','/images/banh/banh-quy-so-co-la.jpg',N'Bánh quy hạt sô-cô-la kết hợp nền bánh thơm bơ và các miếng sô-cô-la rải trên mặt. Sự tương phản giữa phần bánh và sô-cô-la tạo hương vị đậm đà, dễ chia sẻ trong các buổi trò chuyện. Phù hợp dùng cùng sữa, trà hoặc cà phê; quy cách đóng hộp theo xác nhận của cửa hàng.'),
 (N'Bánh quy trà xanh','/images/banh/banh-quy-tra-xanh.jpg',N'Bánh quy trà xanh có sắc xanh dịu và hương trà đặc trưng, kết hợp kết cấu bánh quy giòn xốp. Hậu vị trà hơi đắng tạo nét riêng so với bánh quy bơ truyền thống. Thích hợp cho buổi trà chiều và làm quà nhỏ cho người yêu thích matcha.'),
 (N'Bông lan vani','/images/banh/bong-lan-vani.jpg',N'Bông lan vani là dòng bánh mềm xốp với hương vani nhẹ, thích hợp dùng vào bữa sáng hoặc bữa phụ. Phong cách đơn giản giúp cảm nhận rõ phần cốt bánh, dễ kết hợp cùng trà, cà phê hay trái cây. Có thể chia thành các phần nhỏ để dùng trong gia đình.'),
 (N'Bông lan sô-cô-la','/images/banh/bong-lan-so-co-la.jpg',N'Bông lan sô-cô-la có cốt bánh nâu mềm xốp và hương cacao đặc trưng. Vị bánh đậm hơn dòng vani, phù hợp cho người yêu thích sô-cô-la và những buổi ăn nhẹ. Dùng riêng hoặc kết hợp đồ uống theo sở thích; hình trang trí và phần cắt thực tế tùy mẫu cửa hàng xác nhận.'),
 (N'Bông lan chanh','/images/banh/bong-lan-chanh.jpg',N'Bông lan chanh kết hợp độ mềm của cốt bánh với hương chanh tươi sáng. Vị chua nhẹ và mùi thơm thanh phù hợp dùng cùng trà vào buổi chiều. Dòng bánh đơn giản, dễ chia phần cho gia đình hoặc mang đến buổi gặp gỡ bạn bè.'),
 (N'Bánh phô mai cổ điển','/images/banh/pho-mai-co-dien.jpg',N'Bánh phô mai cổ điển có phần nhân mịn, vị phô mai béo nhẹ và hương thơm đặc trưng. Kết cấu chắc mềm tạo cảm giác khác biệt so với mousse hoặc bông lan. Phù hợp dùng làm món tráng miệng, thưởng thức cùng trà hay cà phê để cân bằng vị bánh.'),
 (N'Bánh phô mai dâu','/images/banh/pho-mai-dau.jpg',N'Bánh phô mai dâu kết hợp phần nhân phô mai mịn với lớp dâu chua ngọt. Sắc đỏ của dâu tạo điểm nhấn trên nền bánh sáng màu, mang phong cách tươi tắn cho bàn tiệc. Phù hợp cho dịp kỷ niệm, quà tặng hoặc những người yêu thích sự kết hợp giữa trái cây và phô mai.'),
 (N'Bánh phô mai trà xanh','/images/banh/pho-mai-tra-xanh.jpg',N'Bánh phô mai trà xanh phối hợp vị phô mai béo dịu với hương matcha và hậu vị trà đặc trưng. Sắc xanh giúp chiếc bánh có vẻ thanh nhã, phù hợp thưởng thức chậm cùng trà. Đây là lựa chọn đổi vị cho người đã quen với dòng phô mai cổ điển.'),
 (N'Su kem vani','/images/banh/su-kem-vani.jpg',N'Su kem vani có vỏ bánh nướng vàng, ruột rỗng để chứa phần kem vani mềm mịn. Hương vani nhẹ và kích thước từng chiếc thuận tiện để chia sẻ trong buổi họp mặt. Phù hợp làm món tráng miệng hoặc quà nhỏ; số chiếc trong hộp được cửa hàng xác nhận khi đặt.'),
 (N'Su kem sô-cô-la','/images/banh/su-kem-so-co-la.jpg',N'Su kem sô-cô-la kết hợp vỏ su nhẹ với phần kem hoặc lớp phủ sô-cô-la đậm vị. Mỗi chiếc bánh tạo sự cân bằng giữa vỏ và nhân mềm, thích hợp dùng cùng đồ uống ít ngọt. Quy cách hộp, loại nhân và trang trí theo mẫu được cửa hàng xác nhận.'),
 (N'Su kem trà xanh','/images/banh/su-kem-tra-xanh.jpg',N'Su kem trà xanh có vỏ su nướng vàng và phần kem matcha xanh dịu, nổi bật bằng hương trà cùng hậu vị hơi đắng. Phù hợp cho người muốn thử hương vị mới từ dòng su kem quen thuộc. Ảnh minh họa có trang trí và nhân phụ; thành phần thực tế cần xác nhận trước khi đặt.'),
 (N'Tart trứng','/images/banh/tart-trung.jpg',N'Tart trứng có đế bánh vàng giòn và phần nhân trứng sữa mịn ở giữa. Hương vị béo dịu cùng bề mặt nướng tạo nét đặc trưng của dòng tart. Kích thước từng chiếc tiện dùng cho bữa phụ, tiệc trà hoặc chia sẻ với gia đình; quy cách hộp được xác nhận khi đặt.'),
 (N'Tart dâu tươi','/images/banh/tart-dau.jpg',N'Tart dâu tươi kết hợp đế tart giòn với lớp kem nhân và dâu trang trí trên mặt. Vị dâu chua nhẹ làm cân bằng phần nhân béo, mang lại phong cách tươi sáng. Phù hợp dùng làm món tráng miệng hoặc quà tặng cho người yêu thích bánh trái cây.'),
 (N'Tart chanh','/images/banh/tart-chanh.jpg',N'Tart chanh có đế bánh giòn và phần nhân chanh vàng mịn, nổi bật bằng vị chua thanh cùng hương vỏ chanh. Sự kết hợp giữa đế và nhân tạo kết cấu rõ ràng, phù hợp thưởng thức cùng trà. Trang trí và phần cắt được cửa hàng xác nhận theo mẫu đặt.'),
 (N'Bánh cuộn vani','/images/banh/banh-cuon-vani.jpg',N'Bánh cuộn vani có lớp bông lan mềm ôm phần kem sáng màu thành vòng xoắn. Hương vani nhẹ và hình dáng gọn giúp dễ chia thành từng lát cho gia đình hoặc tiệc trà. Đây là dòng bánh phù hợp cho buổi gặp gỡ cần món tráng miệng đơn giản, dễ thưởng thức.'),
 (N'Bánh cuộn dâu','/images/banh/banh-cuon-dau.jpg',N'Bánh cuộn dâu kết hợp cốt bông lan mềm, lớp kem và hương dâu chua ngọt. Khi cắt lát, vòng cuộn và phần nhân tạo điểm nhấn rõ ràng, phù hợp bày bàn trà hoặc dùng sau bữa ăn. Trang trí trái cây và kích thước bánh theo mẫu cửa hàng xác nhận.'),
 (N'Bánh cuộn trà xanh','/images/banh/banh-cuon-tra-xanh.jpg',N'Bánh cuộn trà xanh có cốt bánh mang hương matcha, cuộn cùng lớp kem mềm thành vòng xoắn. Vị trà dịu giúp cân bằng phần kem, phù hợp người yêu thích hương vị thanh nhẹ. Có thể chia lát dùng cho buổi trà chiều, họp mặt gia đình hoặc quà tặng nhỏ.'),
 (N'Sừng bò bơ','/images/banh/sung-bo-bo.jpg',N'Sừng bò bơ có hình cong đặc trưng và các lớp bột xếp mỏng, nướng vàng trên bề mặt. Hương bơ và lớp vỏ giòn nhẹ phù hợp cho bữa sáng hoặc ăn cùng cà phê. Dòng bánh đơn giản để cảm nhận rõ cấu trúc nhiều lớp và mùi thơm sau khi nướng.'),
 (N'Sừng bò sô-cô-la','/images/banh/sung-bo-so-co-la.jpg',N'Sừng bò sô-cô-la kết hợp các lớp bột nướng vàng với phần sô-cô-la bên trong hoặc trang trí theo mẫu. Vị bơ thơm và cacao đậm tạo lựa chọn bữa sáng phong phú hơn dòng nguyên bản. Phù hợp thưởng thức cùng sữa hoặc cà phê; kiểu tạo hình thực tế được xác nhận khi đặt.'),
 (N'Sừng bò hạnh nhân','/images/banh/sung-bo-hanh-nhan.jpg',N'Sừng bò hạnh nhân có lớp bột nhiều tầng và phần hạnh nhân tạo hương thơm bùi. Các lát hạnh nhân trên mặt giúp tăng kết cấu, thích hợp cho bữa sáng hay tiệc trà. Người dị ứng với các loại hạt cần xác nhận đầy đủ thành phần với cửa hàng trước khi mua.'),
 (N'Cupcake vani','/images/banh/cupcake-vani.jpg',N'Cupcake vani là phần bánh nhỏ trong cốc giấy, kết hợp cốt vani mềm và lớp kem trang trí. Hình thức gọn, dễ chia phần cho sinh nhật, tiệc nhỏ hoặc quà tặng. Màu kem, họa tiết trang trí và số lượng trong mỗi phần bán được cửa hàng xác nhận khi đặt.'),
 (N'Cupcake sô-cô-la','/images/banh/cupcake-so-co-la.jpg',N'Cupcake sô-cô-la có cốt bánh cacao mềm và phần kem hoặc lớp phủ sô-cô-la theo mẫu. Hương vị đậm và kích thước từng chiếc phù hợp cho người yêu thích món ngọt nhỏ gọn. Có thể dùng cho bàn tiệc, quà tặng hoặc thưởng thức cùng đồ uống ít ngọt.'),
 (N'Cupcake dâu','/images/banh/cupcake-dau.jpg',N'Cupcake dâu mang hương dâu và sắc hồng tươi tắn, kết hợp cốt bánh mềm với phần kem trang trí. Dạng bánh nhỏ tiện bày tiệc và chia phần, phù hợp làm quà sinh nhật hoặc món tráng miệng. Trang trí dâu và màu kem theo mẫu cửa hàng xác nhận.'),
 (N'Tiramisu cà phê','/images/banh/tiramisu-ca-phe.jpg',N'Tiramisu cà phê nổi bật với các lớp bánh và kem xen kẽ, hương cà phê cùng lớp cacao trên mặt. Kết cấu mềm ẩm phù hợp dùng làm món tráng miệng sau bữa ăn. Thành phần cà phê và khả năng có rượu trong công thức cần xác nhận nếu đặt cho trẻ nhỏ hoặc người có yêu cầu ăn uống riêng.'),
 (N'Tiramisu trà xanh','/images/banh/tiramisu-tra-xanh.jpg',N'Tiramisu trà xanh biến tấu từ dòng bánh nhiều lớp với hương matcha, phần kem mềm và lớp bột trà trên mặt. Hậu vị trà hơi đắng giúp tạo sự cân bằng cho phần kem béo. Phù hợp cho người yêu thích trà xanh và muốn thử phong cách tiramisu khác dòng cà phê.'),
 (N'Tiramisu dâu','/images/banh/tiramisu-dau.jpg',N'Tiramisu dâu kết hợp các lớp bánh, kem và dâu chua ngọt, tạo phong cách trái cây tươi sáng. Mẫu minh họa được trình bày theo ly để dùng từng phần, thuận tiện cho món tráng miệng. Kiểu đóng hộp, quy cách và công thức thực tế được cửa hàng xác nhận khi đặt.'),
 (N'Hộp macaron vani','/images/banh/macaron-vani.jpg',N'Hộp macaron vani gồm những chiếc bánh nhỏ có hai lớp vỏ kẹp nhân vani ở giữa. Hình dáng tròn và màu sắc nhẹ nhàng phù hợp làm quà hoặc bày tiệc trà. Quy cách hộp, số chiếc và thành phần nhân cần xác nhận với cửa hàng; người dị ứng hạt cần kiểm tra kỹ công thức.'),
 (N'Hộp macaron sô-cô-la','/images/banh/macaron-so-co-la.jpg',N'Hộp macaron sô-cô-la nổi bật với vỏ màu cacao và phần nhân sô-cô-la đậm vị. Kết cấu vỏ và nhân tạo cảm giác thú vị trong từng chiếc bánh nhỏ. Phù hợp làm quà tặng cho người yêu thích cacao hoặc thưởng thức cùng trà; quy cách hộp theo xác nhận khi đặt.'),
 (N'Hộp macaron dâu','/images/banh/macaron-dau.jpg',N'Hộp macaron dâu có vỏ hồng và phần nhân mang hương dâu chua ngọt, thích hợp cho món quà nhỏ hoặc bàn tiệc. Ảnh minh họa là biến thể dâu phô mai; loại nhân thực tế được cửa hàng xác nhận. Vui lòng kiểm tra thành phần nếu dị ứng với hạt, sữa hoặc trứng.'),
 (N'Donut đường','/images/banh/donut-duong.jpg',N'Donut đường có hình vòng tròn đặc trưng, phần bánh mềm và lớp đường phủ trên mặt. Phong cách đơn giản phù hợp cho bữa phụ, buổi gặp bạn bè hoặc dùng cùng cà phê. Số lượng trong phần bán và quy cách đóng gói được cửa hàng xác nhận khi đặt.'),
 (N'Donut sô-cô-la','/images/banh/donut-so-co-la.jpg',N'Donut sô-cô-la kết hợp phần bánh mềm với lớp phủ sô-cô-la trên mặt. Vị cacao tạo điểm nhấn cho dòng bánh vòng quen thuộc, thích hợp dùng cùng sữa hoặc đồ uống ít ngọt. Trang trí và số lượng mỗi phần theo mẫu cửa hàng xác nhận.'),
 (N'Donut dâu','/images/banh/donut-dau.jpg',N'Donut dâu có lớp phủ hồng mang hương dâu, nổi bật trên phần bánh vòng mềm. Màu sắc tươi tắn phù hợp bày bàn tiệc hoặc làm món ăn nhẹ cho buổi họp mặt. Kiểu trang trí, số lượng và quy cách hộp được xác nhận với cửa hàng khi đặt.');
 UPDATE p SET p.duong_dan_anh=a.anh,p.mo_ta=a.mo_ta,p.phien_ban=p.phien_ban+1
 FROM dbo.san_pham p JOIN @anh a ON a.ten=p.ten
 WHERE (p.duong_dan_anh LIKE '/images/%.svg' OR p.duong_dan_anh LIKE '/images/banh/%')
 AND (p.duong_dan_anh<>a.anh OR p.mo_ta<>a.mo_ta);
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO

/* BÓNG BAKERY – SQL SERVER TỔNG, cập nhật 29/10/2026.
CHẠY TOÀN BỘ FILE TRONG SSMS (F5). Dừng ứng dụng Spring Boot trước khi đặt lại database.
Có sẵn 5 tài khoản, 5 tình huống đơn hàng, 2 bánh riêng, voucher và ảnh bánh.
Mật khẩu mẫu của 5 tài khoản: 123456. Xem docs/DU-LIEU-MAU-VA-TINH-HUONG.md.

TÙY CHỌN XÓA DỮ LIỆU CŨ:
- @XoaDuLieuCu = 0: mặc định, nếu database cake_shop đã tồn tại thì dừng và giữ dữ liệu.
- @XoaDuLieuCu = 1: XÓA TOÀN BỘ DATABASE cake_shop rồi tạo lại, mọi đơn/tài khoản cũ mất.
Chỉ đổi thành 1 khi bạn muốn đặt lại dữ liệu đồ án. File này không tự sao lưu.
Không chạy từng đoạn; không chạy file SQL lịch sử trong ZIP.
*/
USE [master];
GO
SET NOCOUNT ON;
EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
DECLARE @XoaDuLieuCu BIT=0; -- ĐỔI 0 THÀNH 1 NẾU MUỐN XÓA TOÀN BỘ DỮ LIỆU CŨ.
IF DB_ID(N'cake_shop') IS NOT NULL
BEGIN
 IF @XoaDuLieuCu<>1
 BEGIN
  PRINT N'Dừng: database cake_shop đã có. Dữ liệu giữ nguyên. Muốn tạo bộ mẫu mới, dừng website và đổi @XoaDuLieuCu=1 ở đầu file.';
  THROW 50000,N'Chưa bật tùy chọn xóa dữ liệu cũ; không có thay đổi database.',1;
 END;
 BEGIN TRY
  ALTER DATABASE [cake_shop] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE [cake_shop];
 END TRY
 BEGIN CATCH
  IF DB_ID(N'cake_shop') IS NOT NULL ALTER DATABASE [cake_shop] SET MULTI_USER WITH ROLLBACK IMMEDIATE;
  THROW;
 END CATCH;
 PRINT N'Đã xóa database cake_shop cũ theo tùy chọn @XoaDuLieuCu=1.';
END;
EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=1;
GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
USE [master];

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
BEGIN TRY
 IF DB_ID(N'cake_shop') IS NULL EXEC(N'CREATE DATABASE [cake_shop] COLLATE Vietnamese_100_CI_AS');
END TRY
BEGIN CATCH
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
USE [cake_shop];

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Kiểm tra đúng cơ sở dữ liệu trước khi tạo bất kỳ bảng nào.
IF DB_NAME() <> N'cake_shop'
BEGIN
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW 50001,N'Chưa chọn được cơ sở dữ liệu cake_shop. Kiểm tra quyền tạo cơ sở dữ liệu.',1;
END;

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
    PRINT N'Đã tạo cấu trúc nền. Tiếp tục thực hiện các phần tích hợp bên dưới.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- GIẢI THÍCH CÁC GIÁ TRỊ KỸ THUẬT TRONG BẢNG:
-- vai_tro: USER = Khách hàng; ADMIN = Quản trị viên.
-- loai: PERCENT = Giảm phần trăm; FIXED = Giảm số tiền cố định.
-- trang_thai: PENDING = Chờ xác nhận; PREPARING = Đang chuẩn bị;
-- SHIPPING = Đang giao; COMPLETED = Hoàn thành; CANCELLED = Đã hủy.
-- phuong_thuc_thanh_toan: COD = Trả tiền khi nhận hàng;
-- BANK_TRANSFER = Chuyển khoản ngân hàng.
-- Các trạng thái vẫn dùng mã kỹ thuật để khớp các enum của ứng dụng Java.


-- PHẦN BỔ SUNG: BỘ ẢNH, ĐÁNH GIÁ, LỊCH GIAO VÀ TỒN KHO
-- Phần tích hợp 04: thực thi ngay sau cấu trúc nền ở trên.
-- Chỉ bổ sung cấu trúc; có thể chạy lại, không xóa dữ liệu hiện có.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- NÂNG CẤP THƯƠNG MẠI ĐIỆN TỬ
-- Phần tích hợp 05: thông báo, phụ kiện và bánh thiết kế riêng.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- BỔ SUNG DANH MỤC BÁNH
-- Bổ sung danh mục, không thay đổi danh mục hoặc sản phẩm hiện có.
-- Có thể chạy lại; chỉ thêm tên chưa tồn tại.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- TIỀN CỌC, ĐỐI SOÁT VÀ HÓA ĐƠN
-- Bổ sung đối soát thanh toán. Không tự ghi nhận tiền cho các đơn cũ.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- BỔ SUNG 08: DUYỆT MẪU VÀ LỊCH SẢN XUẤT
-- Phần tích hợp 08: duyệt mẫu bánh và lịch sản xuất.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- BỔ SUNG 09: ĐỔI TRẢ VÀ HỖ TRỢ HỘI THOẠI
-- Phần tích hợp 09: đổi trả, hoàn tiền và phiếu hỗ trợ.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- BỔ SUNG 10: YÊU THÍCH VÀ QUÊN MẬT KHẨU
-- Phần tích hợp 10: yêu thích và khôi phục mật khẩu.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- BỔ SUNG 11: MỖI DANH MỤC BA BÁNH
-- Phần tích hợp 11: hoàn thiện mỗi danh mục mẫu có 3 bánh đang bán.
-- Không tạo bánh trung thu; không sửa giá, ảnh, tồn kho hoặc xóa bánh hiện có.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- PHẦN TÍCH HỢP 12: ẢNH VÀ MÔ TẢ BÁNH
-- Ảnh Internet có nguồn tại /image-credits; không dùng ảnh AI.
-- Chỉ cập nhật 39 bánh mẫu đang dùng SVG hoặc bộ ảnh mẫu này. Giữ nguyên ảnh admin upload, giá, tồn kho và thông tin dị ứng.
USE cake_shop;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
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
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- PHẦN TÍCH HỢP 13: BẢN ĐỒ, ĐỊA CHỈ GPS VÀ SHIPPER
USE [cake_shop];

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.dia_chi','vi_do') IS NULL ALTER TABLE dbo.dia_chi ADD vi_do FLOAT NULL;
 IF COL_LENGTH('dbo.dia_chi','kinh_do') IS NULL ALTER TABLE dbo.dia_chi ADD kinh_do FLOAT NULL;
 IF COL_LENGTH('dbo.nguoi_dung','dien_thoai_shipper') IS NULL ALTER TABLE dbo.nguoi_dung ADD dien_thoai_shipper VARCHAR(20) NULL;
 IF COL_LENGTH('dbo.nguoi_dung','phuong_tien_shipper') IS NULL ALTER TABLE dbo.nguoi_dung ADD phuong_tien_shipper NVARCHAR(100) NULL;
 IF COL_LENGTH('dbo.don_hang','vi_do_giao') IS NULL ALTER TABLE dbo.don_hang ADD vi_do_giao FLOAT NULL;
 IF COL_LENGTH('dbo.don_hang','kinh_do_giao') IS NULL ALTER TABLE dbo.don_hang ADD kinh_do_giao FLOAT NULL;
 IF COL_LENGTH('dbo.don_hang','shipper_id') IS NULL ALTER TABLE dbo.don_hang ADD shipper_id BIGINT NULL;
 IF COL_LENGTH('dbo.don_hang','vi_do_shipper') IS NULL ALTER TABLE dbo.don_hang ADD vi_do_shipper FLOAT NULL;
 IF COL_LENGTH('dbo.don_hang','kinh_do_shipper') IS NULL ALTER TABLE dbo.don_hang ADD kinh_do_shipper FLOAT NULL;
 IF COL_LENGTH('dbo.don_hang','do_chinh_xac_shipper') IS NULL ALTER TABLE dbo.don_hang ADD do_chinh_xac_shipper FLOAT NULL;
 IF COL_LENGTH('dbo.don_hang','vi_tri_cap_nhat_luc') IS NULL ALTER TABLE dbo.don_hang ADD vi_tri_cap_nhat_luc DATETIMEOFFSET(6) NULL;
 IF COL_LENGTH('dbo.don_hang','gps_ghi_nhan_luc') IS NULL ALTER TABLE dbo.don_hang ADD gps_ghi_nhan_luc DATETIMEOFFSET(6) NULL;
 IF COL_LENGTH('dbo.don_hang','dang_chia_se_vi_tri') IS NULL ALTER TABLE dbo.don_hang ADD dang_chia_se_vi_tri BIT NOT NULL CONSTRAINT df_don_hang_chia_se_vi_tri DEFAULT 0 WITH VALUES;
 IF COL_LENGTH('dbo.don_hang','shipper_da_giao_luc') IS NULL ALTER TABLE dbo.don_hang ADD shipper_da_giao_luc DATETIMEOFFSET(6) NULL;
 IF COL_LENGTH('dbo.don_hang','su_co_giao_hang') IS NULL ALTER TABLE dbo.don_hang ADD su_co_giao_hang NVARCHAR(500) NULL;
 IF COL_LENGTH('dbo.don_hang','phien_chia_se_vi_tri') IS NULL ALTER TABLE dbo.don_hang ADD phien_chia_se_vi_tri VARCHAR(36) NULL;
 IF EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='ck_nguoi_dung_vai_tro' AND parent_object_id=OBJECT_ID('dbo.nguoi_dung')) ALTER TABLE dbo.nguoi_dung DROP CONSTRAINT ck_nguoi_dung_vai_tro;
 ALTER TABLE dbo.nguoi_dung WITH CHECK ADD CONSTRAINT ck_nguoi_dung_vai_tro CHECK(vai_tro IN ('USER','ADMIN','SHIPPER'));
 IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='fk_don_hang_shipper') ALTER TABLE dbo.don_hang ADD CONSTRAINT fk_don_hang_shipper FOREIGN KEY(shipper_id) REFERENCES dbo.nguoi_dung(id);
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='ix_don_hang_shipper' AND object_id=OBJECT_ID('dbo.don_hang')) CREATE INDEX ix_don_hang_shipper ON dbo.don_hang(shipper_id,trang_thai,ngay_giao);

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
PRINT N'Đã bổ sung bản đồ và shipper. Không tạo tài khoản hoặc vị trí GPS giả.';

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- PHẦN TÍCH HỢP 14: THÔNG TIN LIÊN HỆ TRANG CÁ NHÂN
USE [cake_shop];

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
IF COL_LENGTH('dbo.nguoi_dung','so_dien_thoai') IS NULL
 ALTER TABLE dbo.nguoi_dung ADD so_dien_thoai VARCHAR(20) NULL;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
PRINT N'Đã bổ sung số điện thoại cá nhân. Không thay đổi địa chỉ hoặc đơn đã đặt.';

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- PHẦN TÍCH HỢP 15: STRIPE SANDBOX VÀ ĐỐI SOÁT THANH TOÁN
-- Không lưu khóa Stripe trong SQL. Không tự chuyển phương thức hoặc ghi nhận tiền cho đơn cũ.
USE [cake_shop];

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='ck_don_hang_thanh_toan' AND parent_object_id=OBJECT_ID('dbo.don_hang'))
  ALTER TABLE dbo.don_hang DROP CONSTRAINT ck_don_hang_thanh_toan;
 ALTER TABLE dbo.don_hang WITH CHECK ADD CONSTRAINT ck_don_hang_thanh_toan CHECK(phuong_thuc_thanh_toan IN ('COD','BANK_TRANSFER','STRIPE'));
 DECLARE @stripe_drop NVARCHAR(MAX)=N'';
 SELECT @stripe_drop=@stripe_drop+N'ALTER TABLE dbo.yeu_cau_banh_rieng DROP CONSTRAINT '+QUOTENAME(name)+N';'
 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.yeu_cau_banh_rieng')
 AND (parent_column_id=COLUMNPROPERTY(OBJECT_ID('dbo.yeu_cau_banh_rieng'),'phuong_thuc_thanh_toan','ColumnId') OR definition LIKE '%phuong_thuc_thanh_toan%');
 IF LEN(@stripe_drop)>0 EXEC sys.sp_executesql @stripe_drop;
 ALTER TABLE dbo.yeu_cau_banh_rieng WITH CHECK ADD CONSTRAINT ck_banh_rieng_thanh_toan_stripe CHECK(phuong_thuc_thanh_toan IN ('COD','BANK_TRANSFER','STRIPE'));
 IF OBJECT_ID('dbo.giao_dich_stripe','U') IS NULL
 BEGIN
  CREATE TABLE dbo.giao_dich_stripe(
   id VARCHAR(36) NOT NULL PRIMARY KEY,
   nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
   don_hang_id BIGINT NULL REFERENCES dbo.don_hang(id),
   banh_rieng_id BIGINT NULL REFERENCES dbo.yeu_cau_banh_rieng(id),
   so_tien DECIMAL(14,0) NOT NULL CHECK(so_tien>0),
   trang_thai VARCHAR(20) NOT NULL DEFAULT 'CREATING' CHECK(trang_thai IN ('CREATING','OPEN','PAID','EXPIRED','REVIEW','RESOLVED')),
   stripe_session_id VARCHAR(255) NULL,
   stripe_payment_intent VARCHAR(255) NULL,
   duong_dan_thanh_toan VARCHAR(2000) NULL,
   ngay_tao DATETIMEOFFSET(6) NOT NULL,
   het_han_luc DATETIMEOFFSET(6) NULL,
   xac_nhan_luc DATETIMEOFFSET(6) NULL,
   ghi_chu NVARCHAR(500) NULL,
   ket_qua_doi_soat NVARCHAR(1000) NULL,
   nguoi_doi_soat VARCHAR(190) NULL,
   doi_soat_luc DATETIMEOFFSET(6) NULL,
   CONSTRAINT ck_stripe_mot_don CHECK((don_hang_id IS NOT NULL AND banh_rieng_id IS NULL) OR (don_hang_id IS NULL AND banh_rieng_id IS NOT NULL))
  );
  CREATE UNIQUE INDEX ux_stripe_session ON dbo.giao_dich_stripe(stripe_session_id) WHERE stripe_session_id IS NOT NULL;
  CREATE INDEX ix_stripe_don_ngay ON dbo.giao_dich_stripe(don_hang_id,ngay_tao DESC);
  CREATE INDEX ix_stripe_banh_rieng_ngay ON dbo.giao_dich_stripe(banh_rieng_id,ngay_tao DESC);
  CREATE INDEX ix_stripe_trang_thai ON dbo.giao_dich_stripe(trang_thai,ngay_tao DESC);
 END;
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
PRINT N'Đã bổ sung Stripe Sandbox; giữ nguyên dữ liệu và các khoản thu/hoàn hiện có.';

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- 16. Tọa độ giao bánh thiết kế riêng (giữ nguyên các yêu cầu cũ).
IF COL_LENGTH(N'dbo.yeu_cau_banh_rieng',N'vi_do') IS NULL
 ALTER TABLE dbo.yeu_cau_banh_rieng ADD vi_do FLOAT NULL;
IF COL_LENGTH(N'dbo.yeu_cau_banh_rieng',N'kinh_do') IS NULL
 ALTER TABLE dbo.yeu_cau_banh_rieng ADD kinh_do FLOAT NULL;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- 17. BỘ MẪU THỰC HÀNH: 5 tài khoản, 5 tình huống đơn hàng, bánh riêng và voucher.
-- Mật khẩu chung cho dữ liệu mẫu: 123456 (BCrypt, chỉ dùng cho đồ án).
-- Thời hạn và lịch mẫu tính theo thời điểm chạy, múi giờ Việt Nam.
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @Luc DATETIME2=CONVERT(DATETIME2,SYSDATETIMEOFFSET() AT TIME ZONE 'SE Asia Standard Time');
 DECLARE @HomNay DATE=CONVERT(DATE,@Luc);
 DECLARE @MatKhau VARCHAR(60)='$2a$10$0I8ihkEEV0R1cOY4zEtRleXIfj.z436xLAp8O6vw8lW7F1tSFq0PW';
 INSERT dbo.nguoi_dung(ten,email,mat_khau,vai_tro,duoc_hoat_dong,so_dien_thoai,dien_thoai_shipper,phuong_tien_shipper) VALUES
 (N'Quản trị Bóng Bakery','admin@gmail.com',@MatKhau,'ADMIN',1,'0902592231',NULL,NULL),
 (N'Ngọc Anh – khách mẫu 1','khach1@gmail.com',@MatKhau,'USER',1,'0902592232',NULL,NULL),
 (N'Minh Khang – khách mẫu 2','khach2@gmail.com',@MatKhau,'USER',1,'0902592233',NULL,NULL),
 (N'Hoàng Nam – shipper mẫu 1','shipper1@gmail.com',@MatKhau,'SHIPPER',1,'0912345678','0912345678',N'Xe máy 59A1-123.45'),
 (N'Thảo Vy – shipper mẫu 2','shipper2@gmail.com',@MatKhau,'SHIPPER',1,'0976543210','0976543210',N'Xe máy 59B1-678.90');
 DECLARE @Khach1 BIGINT=(SELECT id FROM dbo.nguoi_dung WHERE email='khach1@gmail.com');
 DECLARE @Khach2 BIGINT=(SELECT id FROM dbo.nguoi_dung WHERE email='khach2@gmail.com');
 DECLARE @Shipper1 BIGINT=(SELECT id FROM dbo.nguoi_dung WHERE email='shipper1@gmail.com');
 DECLARE @Admin BIGINT=(SELECT id FROM dbo.nguoi_dung WHERE email='admin@gmail.com');
 INSERT dbo.dia_chi(nguoi_dung_id,nguoi_nhan,so_dien_thoai,dia_chi_chi_tiet,vi_do,kinh_do) VALUES
 (@Khach1,N'Ngọc Anh','0902592232',N'12 Lê Lợi, phường Bến Thành, TP. Hồ Chí Minh (địa chỉ mẫu)',10.7732,106.7012),
 (@Khach2,N'Minh Khang','0902592233',N'45 Nguyễn Huệ, phường Sài Gòn, TP. Hồ Chí Minh (địa chỉ mẫu)',10.7743,106.7037);
 -- Năm mã đang dùng + một mã hết hạn để thử tình huống không hợp lệ.
 INSERT dbo.ma_giam_gia(ma,loai,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,bat_dau_luc,ket_thuc_luc,gioi_han_luot_dung,gioi_han_moi_nguoi,so_luot_da_dung,dang_ap_dung) VALUES
 ('BONG10','PERCENT',10,100000,50000,DATEADD(DAY,-1,@Luc),DATEADD(DAY,90,@Luc),100,1,0,1),
 ('NGOT50K','FIXED',50000,300000,NULL,DATEADD(DAY,-1,@Luc),DATEADD(DAY,90,@Luc),100,2,0,1),
 ('SINHNHAT15','PERCENT',15,500000,120000,DATEADD(DAY,-1,@Luc),DATEADD(DAY,90,@Luc),50,1,0,1),
 ('BANH20K','FIXED',20000,150000,NULL,DATEADD(DAY,-1,@Luc),DATEADD(DAY,90,@Luc),100,3,0,1),
 ('BANMOI30K','FIXED',30000,250000,NULL,DATEADD(DAY,-1,@Luc),DATEADD(DAY,90,@Luc),1,1,0,1),
 ('HETHAN10','PERCENT',10,0,50000,DATEADD(DAY,-10,@Luc),DATEADD(DAY,-1,@Luc),100,1,0,1);
 -- Nạp tồn phụ kiện; đơn nhận tại quầy bên dưới sẽ trừ đúng một nến và một thiệp.
 UPDATE dbo.phu_kien SET ton_kho=30,dang_ban=1;
 DECLARE @Mau TABLE(stt INT PRIMARY KEY,san_pham_id BIGINT,ten NVARCHAR(150),gia DECIMAL(14,0),tuy_chon_id BIGINT,tuy_chon NVARCHAR(150));
 INSERT @Mau(stt,san_pham_id,ten,gia,tuy_chon_id,tuy_chon)
 SELECT TOP(5) ROW_NUMBER() OVER(ORDER BY p.id),p.id,p.ten,p.gia+COALESCE(o.gia_them,0),COALESCE(o.id,0),
 CASE WHEN o.id IS NULL THEN N'Tiêu chuẩn' ELSE o.kich_thuoc+N' · '+o.huong_vi END
 FROM dbo.san_pham p OUTER APPLY(SELECT TOP(1) id,kich_thuoc,huong_vi,gia_them FROM dbo.tuy_chon_banh WHERE san_pham_id=p.id AND dang_ban=1 ORDER BY id) o
 WHERE p.dang_ap_dung=1 AND p.ton_kho>=5 ORDER BY p.id;
 IF (SELECT COUNT(*) FROM @Mau)<>5 THROW 50020,N'Không đủ 5 bánh còn hàng để tạo bộ mẫu.',1;
 DECLARE @i INT=1,@BanGiao DATETIME2=DATEADD(HOUR,-2,@Luc);
 WHILE @i<=5
 BEGIN
  DECLARE @Pid BIGINT,@Ten NVARCHAR(150),@Gia DECIMAL(14,0),@Option BIGINT,@Nhan NVARCHAR(150);
  SELECT @Pid=san_pham_id,@Ten=ten,@Gia=gia,@Option=tuy_chon_id,@Nhan=tuy_chon FROM @Mau WHERE stt=@i;
  DECLARE @Qty INT=CASE WHEN @i=1 THEN 2 ELSE 1 END;
  DECLARE @Khach BIGINT=CASE WHEN @i=5 THEN @Khach2 ELSE @Khach1 END;
  DECLARE @ThanhPhan DECIMAL(14,0)=@Gia*@Qty;
  DECLARE @PhuKien DECIMAL(14,0)=CASE WHEN @i=3 THEN 25000 ELSE 0 END;
  DECLARE @Ship DECIMAL(14,0)=CASE WHEN @i=3 OR @ThanhPhan>=500000 THEN 0 ELSE 30000 END;
  DECLARE @Giam DECIMAL(14,0)=CASE WHEN @i=4 THEN CASE WHEN FLOOR(@ThanhPhan*0.1)>50000 THEN 50000 ELSE FLOOR(@ThanhPhan*0.1) END ELSE 0 END;
  DECLARE @Tong DECIMAL(14,0)=@ThanhPhan+@PhuKien-@Giam+@Ship;
  DECLARE @TraTruoc DECIMAL(14,0)=CASE WHEN @i=3 THEN CEILING(@Tong*0.5) WHEN @i IN(4,5) THEN @Tong ELSE 0 END;
  DECLARE @DaTra DECIMAL(14,0)=CASE WHEN @i IN(3,4) THEN @TraTruoc ELSE 0 END;
  INSERT dbo.don_hang(nguoi_dung_id,trang_thai,phuong_thuc_thanh_toan,nguoi_nhan,so_dien_thoai,dia_chi_giao_hang,ghi_chu,
   tien_hang,tien_giam,phi_giao_hang,tong_thanh_toan,ma_uu_dai,ma_chong_dat_trung,ngay_tao,ngay_giao,khung_gio,loi_chuc,
   hinh_thuc_nhan,dat_truoc,ke_hoach_thanh_toan,can_tra_truoc,da_thanh_toan,tong_da_hoan,ngay_hoan_thanh,
   shipper_id,vi_do_giao,kinh_do_giao,shipper_da_giao_luc)
  VALUES(@Khach,
   CASE @i WHEN 2 THEN 'SHIPPING' WHEN 3 THEN 'READY_FOR_PICKUP' WHEN 4 THEN 'COMPLETED' ELSE 'PENDING' END,
   CASE WHEN @i IN(3,4) THEN 'BANK_TRANSFER' WHEN @i=5 THEN 'STRIPE' ELSE 'COD' END,
   CASE WHEN @i=5 THEN N'Minh Khang' ELSE N'Ngọc Anh' END,CASE WHEN @i=5 THEN '0902592233' ELSE '0902592232' END,
   CASE WHEN @i=3 THEN N'123 Nguyễn Thị Minh Khai, phường Bến Thành, TP. Hồ Chí Minh (địa chỉ mẫu)' WHEN @i=5 THEN N'45 Nguyễn Huệ, phường Sài Gòn, TP. Hồ Chí Minh (địa chỉ mẫu)' ELSE N'12 Lê Lợi, phường Bến Thành, TP. Hồ Chí Minh (địa chỉ mẫu)' END,
   N'[MẪU '+CONVERT(NVARCHAR(1),@i)+N'/5] '+CASE @i WHEN 1 THEN N'Chờ xác nhận – thử hủy và hoàn kho.' WHEN 2 THEN N'Đang giao COD – shipper báo giao, admin ghi tiền rồi hoàn tất.' WHEN 3 THEN N'Nhận tại quầy – đã cọc 50%, có QR, còn tiền cần thu.' WHEN 4 THEN N'Hoàn thành – thử đánh giá, đổi trả, duyệt/từ chối/hoàn tiền.' ELSE N'Stripe chưa thanh toán – tạo phiên Sandbox thật, không giả giao dịch.' END,
   @ThanhPhan+@PhuKien,@Giam,@Ship,@Tong,CASE WHEN @i=4 THEN 'BONG10' ELSE NULL END,CONVERT(VARCHAR(36),NEWID()),
   CASE WHEN @i=4 THEN DATEADD(DAY,-1,@Luc) ELSE @Luc END,CASE WHEN @i IN(2,3,4) THEN @HomNay ELSE DATEADD(DAY,2,@HomNay) END,
   '13:00-17:00',N'Chúc một ngày ngọt ngào!',CASE WHEN @i=3 THEN 'PICKUP' ELSE 'DELIVERY' END,0,
   CASE WHEN @i=3 THEN 'DEPOSIT' WHEN @i IN(4,5) THEN 'FULL' ELSE 'ON_RECEIPT' END,@TraTruoc,@DaTra,0,CASE WHEN @i=4 THEN @BanGiao ELSE NULL END,
   CASE WHEN @i IN(2,4) THEN @Shipper1 ELSE NULL END,CASE WHEN @i=3 THEN NULL WHEN @i=5 THEN 10.7743 ELSE 10.7732 END,
   CASE WHEN @i=3 THEN NULL WHEN @i=5 THEN 106.7037 ELSE 106.7012 END,CASE WHEN @i=4 THEN TODATETIMEOFFSET(@BanGiao,'+07:00') ELSE NULL END);
  DECLARE @Don BIGINT=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_don_hang(don_hang_id,san_pham_id,ten_san_pham,don_gia,so_luong,tuy_chon,tuy_chon_id) VALUES(@Don,@Pid,@Ten,@Gia,@Qty,@Nhan,@Option);
  UPDATE dbo.san_pham SET ton_kho=ton_kho-@Qty WHERE id=@Pid;
  INSERT dbo.lich_su_kho(san_pham_id,thay_doi,ton_sau,ly_do,nguoi_thuc_hien,ngay_tao)
   SELECT id,-@Qty,ton_kho,N'Đơn mẫu #'+CONVERT(NVARCHAR(20),@Don),'DEMO_SQL',@Luc FROM dbo.san_pham WHERE id=@Pid;
  IF @i=3
  BEGIN
   INSERT dbo.phu_kien_don_hang(don_hang_id,phu_kien_id,ten,don_gia,so_luong) SELECT @Don,id,ten,gia,1 FROM dbo.phu_kien WHERE ten IN(N'Nến sinh nhật',N'Thiệp chúc mừng');
   UPDATE dbo.phu_kien SET ton_kho=ton_kho-1 WHERE ten IN(N'Nến sinh nhật',N'Thiệp chúc mừng');
  END;
  IF @DaTra>0 INSERT dbo.lich_su_thanh_toan(don_hang_id,loai,so_tien,tham_chieu,nguoi_xac_nhan,ma_chong_lap,ngay_tao)
   VALUES(@Don,'RECEIPT',@DaTra,N'DEMO-THU-'+CONVERT(NVARCHAR(20),@Don),'admin@gmail.com',CONVERT(VARCHAR(36),NEWID()),CASE WHEN @i=4 THEN @BanGiao ELSE @Luc END);
  IF @i=4
  BEGIN
   INSERT dbo.luot_su_dung_ma(ma_giam_gia_id,nguoi_dung_id,don_hang_id) SELECT id,@Khach1,@Don FROM dbo.ma_giam_gia WHERE ma='BONG10';
   UPDATE dbo.ma_giam_gia SET so_luot_da_dung=so_luot_da_dung+1 WHERE ma='BONG10';
  END;
  INSERT dbo.thong_bao(nguoi_dung_id,tieu_de,noi_dung,lien_ket,da_doc,ngay_tao) VALUES
   (@Khach,N'Đơn mẫu #'+CONVERT(NVARCHAR(20),@Don),N'Đây là dữ liệu thực hành, không phải giao dịch thật. Mở chi tiết để xem kịch bản.', '/orders/'+CONVERT(VARCHAR(20),@Don),0,@Luc),
   (@Admin,N'Đơn mẫu #'+CONVERT(NVARCHAR(20),@Don),N'Dữ liệu thực hành – xem ghi chú trong đơn để thao tác đúng luồng.', '/admin/orders/'+CONVERT(VARCHAR(20),@Don),0,@Luc);
  IF @i=2 INSERT dbo.thong_bao(nguoi_dung_id,tieu_de,noi_dung,lien_ket,da_doc,ngay_tao) VALUES(@Shipper1,N'Đơn giao mẫu',N'Bật chia sẻ GPS nếu muốn thực hành theo dõi; dữ liệu SQL không giả vị trí shipper.','/shipper/orders/'+CONVERT(VARCHAR(20),@Don),0,@Luc);
  SET @i+=1;
 END;
 -- Hai yêu cầu riêng: chờ báo giá, và nhận quầy đã chốt/duyệt/cọc để thử lịch sản xuất và QR.
 INSERT dbo.yeu_cau_banh_rieng(nguoi_dung_id,nguoi_nhan,so_dien_thoai,yeu_cau,huong_vi,so_nguoi,ngan_sach,ngay_nhan,khung_gio,hinh_thuc_nhan,dia_chi,phuong_thuc_thanh_toan,loi_chuc,trang_thai,tong_bao_gia,phan_hoi,ma_chong_dat_trung,ngay_tao,ngay_cap_nhat,ke_hoach_thanh_toan,can_tra_truoc,da_thanh_toan,vi_do,kinh_do) VALUES
 (@Khach2,N'Minh Khang','0902592233',N'[MẪU] Bánh sinh nhật màu xanh pastel, trang trí bóng bay và tên người nhận.',N'Vani và dâu',8,600000,DATEADD(DAY,7,@HomNay),'13:00-17:00','DELIVERY',N'45 Nguyễn Huệ, phường Sài Gòn, TP. Hồ Chí Minh (địa chỉ mẫu)','BANK_TRANSFER',N'Chúc mừng sinh nhật!','SENT',NULL,NULL,CONVERT(VARCHAR(36),NEWID()),@Luc,@Luc,'FULL',0,0,10.7743,106.7037),
 (@Khach1,N'Ngọc Anh','0902592232',N'[MẪU] Bánh kem dâu màu hồng, trang trí hoa nhỏ, lời chúc và nến đi cùng.',N'Dâu và vani',10,700000,DATEADD(DAY,5,@HomNay),'09:00-12:00','PICKUP',N'123 Nguyễn Thị Minh Khai, phường Bến Thành, TP. Hồ Chí Minh (địa chỉ mẫu)','BANK_TRANSFER',N'Mừng sinh nhật Ngọc Anh','ACCEPTED',650000,N'Báo giá mẫu 650.000 đ, gồm trang trí và phụ kiện. Khách đã chốt báo giá, duyệt mẫu và cọc 325.000 đ.',CONVERT(VARCHAR(36),NEWID()),@Luc,@Luc,'DEPOSIT',325000,325000,NULL,NULL);
 DECLARE @Rieng BIGINT=SCOPE_IDENTITY();
 INSERT dbo.mau_banh_thiet_ke(banh_rieng_id,anh_mau,noi_dung,trang_thai,y_kien_khach,nguoi_gui,ngay_gui,ngay_phan_hoi)
 SELECT @Rieng,duong_dan_anh,N'Ảnh bánh tham khảo trong đồ án, trang trí hồng và dâu; không phải cam kết ảnh đơn thực tế.','APPROVED',N'Đồng ý mẫu để thực hành.','admin@gmail.com',@Luc,@Luc FROM dbo.san_pham WHERE id=(SELECT san_pham_id FROM @Mau WHERE stt=3);
 INSERT dbo.lich_su_thanh_toan(banh_rieng_id,loai,so_tien,tham_chieu,nguoi_xac_nhan,ma_chong_lap,ngay_tao) VALUES(@Rieng,'RECEIPT',325000,N'DEMO-COC-BANH-RIENG','admin@gmail.com',CONVERT(VARCHAR(36),NEWID()),@Luc);
 -- Kho thấp/hết hàng ngoài các bánh đã dùng trong đơn, giúp thử dashboard.
 DECLARE @KhoMau TABLE(san_pham_id BIGINT,thay_doi INT,ton_sau INT,ly_do NVARCHAR(300),nguoi_thuc_hien VARCHAR(190),ngay_tao DATETIME2);
 ;WITH extra AS(SELECT TOP(2) id,ROW_NUMBER() OVER(ORDER BY id) rn FROM dbo.san_pham WHERE id NOT IN(SELECT san_pham_id FROM @Mau) ORDER BY id)
 UPDATE p SET ton_kho=CASE WHEN e.rn=1 THEN 2 ELSE 0 END
 OUTPUT inserted.id,inserted.ton_kho-deleted.ton_kho,inserted.ton_kho,N'Điều chỉnh tồn mẫu để thử cảnh báo','DEMO_SQL',@Luc
 INTO @KhoMau(san_pham_id,thay_doi,ton_sau,ly_do,nguoi_thuc_hien,ngay_tao)
 FROM dbo.san_pham p JOIN extra e ON e.id=p.id;
 INSERT dbo.lich_su_kho(san_pham_id,thay_doi,ton_sau,ly_do,nguoi_thuc_hien,ngay_tao) SELECT * FROM @KhoMau;
 INSERT dbo.thong_bao(nguoi_dung_id,tieu_de,noi_dung,lien_ket,da_doc,ngay_tao) VALUES
 (@Admin,N'Kho mẫu có bánh sắp hết/hết hàng',N'Bấm xem danh sách để thực hành nhập kho.','/admin/products?stock=warning',0,@Luc);
 COMMIT TRANSACTION;
 PRINT N'Đã tạo 5 tài khoản (mật khẩu mẫu 123456), 5 đơn, 2 yêu cầu bánh riêng, 5 voucher mới còn hạn và 1 voucher hết hạn.';
 SELECT email,vai_tro,ten FROM dbo.nguoi_dung ORDER BY id;
 SELECT id,ghi_chu,trang_thai,tong_thanh_toan,da_thanh_toan FROM dbo.don_hang ORDER BY id;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;

GO
IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- 18. Thông tin đối soát hoàn thực tế; không xóa hoặc thay số tiền cũ.
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
 IF COL_LENGTH(N'dbo.lich_su_thanh_toan',N'kenh_hoan') IS NULL ALTER TABLE dbo.lich_su_thanh_toan ADD kenh_hoan VARCHAR(30) NULL;
 IF COL_LENGTH(N'dbo.lich_su_thanh_toan',N'noi_nhan_hoan') IS NULL ALTER TABLE dbo.lich_su_thanh_toan ADD noi_nhan_hoan NVARCHAR(200) NULL;
 IF COL_LENGTH(N'dbo.lich_su_thanh_toan',N'ma_giao_dich_hoan') IS NULL ALTER TABLE dbo.lich_su_thanh_toan ADD ma_giao_dich_hoan NVARCHAR(100) NULL;
 -- Dynamic SQL prevents SQL Server compiling index references before new columns exist.
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.lich_su_thanh_toan') AND name=N'ux_ma_hoan_thuc_te')
 EXEC(N'CREATE UNIQUE INDEX ux_ma_hoan_thuc_te ON dbo.lich_su_thanh_toan(kenh_hoan,ma_giao_dich_hoan) WHERE kenh_hoan IS NOT NULL AND ma_giao_dich_hoan IS NOT NULL');
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;
GO

IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- 19. Giỏ theo tài khoản, địa chỉ mặc định và lựa chọn nhận gần nhất.
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
 IF COL_LENGTH(N'dbo.dia_chi',N'mac_dinh') IS NULL ALTER TABLE dbo.dia_chi ADD mac_dinh BIT NOT NULL CONSTRAINT df_dia_chi_mac_dinh DEFAULT 0 WITH VALUES;
 IF COL_LENGTH(N'dbo.nguoi_dung',N'hinh_thuc_nhan_gan_nhat') IS NULL ALTER TABLE dbo.nguoi_dung ADD hinh_thuc_nhan_gan_nhat VARCHAR(30) NULL;
 IF COL_LENGTH(N'dbo.nguoi_dung',N'khung_gio_nhan_gan_nhat') IS NULL ALTER TABLE dbo.nguoi_dung ADD khung_gio_nhan_gan_nhat VARCHAR(20) NULL;
 EXEC(N';WITH x AS(SELECT id,ROW_NUMBER() OVER(PARTITION BY nguoi_dung_id ORDER BY id DESC) rn FROM dbo.dia_chi WHERE nguoi_dung_id NOT IN(SELECT nguoi_dung_id FROM dbo.dia_chi WHERE mac_dinh=1)) UPDATE a SET mac_dinh=1 FROM dbo.dia_chi a JOIN x ON x.id=a.id WHERE x.rn=1');
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.dia_chi') AND name=N'ux_dia_chi_mac_dinh')
 EXEC(N'CREATE UNIQUE INDEX ux_dia_chi_mac_dinh ON dbo.dia_chi(nguoi_dung_id) WHERE mac_dinh=1');
 IF OBJECT_ID(N'dbo.dong_gio_hang',N'U') IS NULL
 CREATE TABLE dbo.dong_gio_hang(
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  nguoi_dung_id BIGINT NOT NULL REFERENCES dbo.nguoi_dung(id),
  san_pham_id BIGINT NOT NULL REFERENCES dbo.san_pham(id),
  tuy_chon_id BIGINT NOT NULL DEFAULT 0,
  so_luong INT NOT NULL CHECK(so_luong BETWEEN 1 AND 99),
  CONSTRAINT uq_dong_gio_hang UNIQUE(nguoi_dung_id,san_pham_id,tuy_chon_id)
 );
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;
GO

IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
-- Giỏ mẫu và lựa chọn nhận bánh (chỉ dùng khi khởi tạo đầy đủ file tổng).
BEGIN TRY
 BEGIN TRANSACTION;
 UPDATE dbo.nguoi_dung SET hinh_thuc_nhan_gan_nhat=CASE WHEN email='khach1@gmail.com' THEN 'PICKUP' ELSE 'DELIVERY' END,khung_gio_nhan_gan_nhat='13:00-17:00' WHERE email IN('khach1@gmail.com','khach2@gmail.com');
 INSERT dbo.dong_gio_hang(nguoi_dung_id,san_pham_id,tuy_chon_id,so_luong)
 SELECT u.id,p.id,COALESCE(o.id,0),1 FROM dbo.nguoi_dung u CROSS APPLY(SELECT TOP(1) id FROM dbo.san_pham WHERE dang_ap_dung=1 AND ton_kho>=5 ORDER BY id DESC) p
 OUTER APPLY(SELECT TOP(1) id FROM dbo.tuy_chon_banh WHERE san_pham_id=p.id AND dang_ban=1 ORDER BY id) o
 WHERE u.email IN('khach1@gmail.com','khach2@gmail.com');
 COMMIT TRANSACTION;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
 THROW;
END CATCH;
GO

IF COALESCE(TRY_CONVERT(INT,SESSION_CONTEXT(N'bong_bakery_setup_allowed')),0)<>1 RETURN;
PRINT N'Hoàn tất Bóng Bakery: 26 bảng, 39 bánh, 5 tài khoản, 5 đơn mẫu, 2 bánh riêng và voucher.';

GO

EXEC sys.sp_set_session_context @key=N'bong_bakery_setup_allowed',@value=0;
GO
